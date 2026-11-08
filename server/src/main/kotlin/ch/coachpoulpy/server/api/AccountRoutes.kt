package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.accounts.SignUpRules
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    /** The language of the account's emails, such as "fr"; the phone sends its own. */
    val language: String = "en",
    /** The person agreed to their health data being stored (docs/accounts.md). */
    val healthDataConsent: Boolean,
    /**
     * A random key of 32 to 128 characters the phone keeps, to ask later whether the link mailed
     * now was opened (`confirmation-status`). Optional.
     */
    val pendingKey: String? = null,
)

@Serializable
data class ConfirmRequest(val token: String)

@Serializable
data class ConfirmationStatusRequest(val key: String)

@Serializable
data class ConfirmationStatus(val confirmed: Boolean)

/** Why a request was refused: a short code the app turns into its own words. */
@Serializable
data class Problem(val error: String)

private val signUpBody = Body.auto<SignUpRequest>().toLens()
private val confirmBody = Body.auto<ConfirmRequest>().toLens()
private val problemBody = Body.auto<Problem>().toLens()
private val statusRequestBody = Body.auto<ConfirmationStatusRequest>().toLens()
private val statusBody = Body.auto<ConfirmationStatus>().toLens()

/** Long enough to be unguessable, short enough to bound what is hashed. */
internal val PENDING_KEY_LENGTH = 32..128

private val accountsTag = Tag("accounts")

/**
 * Sign-up: creates an unconfirmed account and mails it a link. The answer is the same whether the
 * address was free or not, so nobody learns who has an account.
 */
fun signUpRoute(accounts: Accounts): ContractRoute = "$API_PREFIX/accounts" meta {
    summary = "Sign up with an email and a password"
    description = "Creates an account whose email isn't confirmed yet and mails it a link to confirm it. " +
        "An address that already has an account gets the same 202, so addresses can't be probed. " +
        "The password has ${SignUpRules.PASSWORD_MIN} to ${SignUpRules.PASSWORD_MAX} characters, and healthDataConsent must be true."
    operationId = "signUp"
    tags += accountsTag
    security = Public
    receiving(
        signUpBody to SignUpRequest("guest@example.com", "correct horse battery", "fr", healthDataConsent = true, pendingKey = EXAMPLE_KEY),
        definitionId = "SignUp",
    )
    returning(ACCEPTED to "The confirmation email is on its way, if this address can get one")
    returning(
        BAD_REQUEST,
        problemBody to Problem("invalid-email"),
        "invalid-email, invalid-password, consent-required or invalid-key",
        definitionId = "Problem",
    )
} bindContract POST to { request ->
    val body = signUpBody(request)
    val signUp = SignUp(body.email, body.password, body.language, body.healthDataConsent, body.pendingKey)
    val problem = SignUpRules.problem(signUp) ?: "invalid-key".takeIf { body.pendingKey != null && body.pendingKey.length !in PENDING_KEY_LENGTH }
    when (problem) {
        null -> {
            accounts.signUp(signUp)
            Response(ACCEPTED)
        }
        else -> Response(BAD_REQUEST).with(problemBody of Problem(problem))
    }
}

/** Confirms the email of the account a link was mailed to, with the token from that link. */
fun confirmEmailRoute(accounts: Accounts): ContractRoute = "$API_PREFIX/accounts/confirm" meta {
    summary = "Confirm an account's email"
    description = "Takes the token from the link mailed at sign-up. A token works once, for " +
        "${Accounts.CONFIRMATION_LIFETIME.toHours()} hours; signing up again with the same address mails a new one."
    operationId = "confirmEmail"
    tags += accountsTag
    security = Public
    receiving(confirmBody to ConfirmRequest("the-token-after-the-hash-in-the-link"), definitionId = "ConfirmEmail")
    returning(NO_CONTENT to "The email is confirmed")
    returning(BAD_REQUEST, problemBody to Problem("invalid-token"), "invalid-token: unknown, already used or expired", definitionId = "Problem")
} bindContract POST to { request ->
    if (accounts.confirm(confirmBody(request).token)) {
        Response(NO_CONTENT)
    } else {
        Response(BAD_REQUEST).with(problemBody of Problem("invalid-token"))
    }
}

/**
 * Whether the link mailed at a sign-up was opened, asked with that sign-up's pending key: the phone
 * asks every few seconds while it shows "Check your email" (#319), so it moves on by itself when the
 * link is opened on another device.
 */
fun confirmationStatusRoute(accounts: Accounts): ContractRoute = "$API_PREFIX/accounts/confirmation-status" meta {
    summary = "Ask whether a sign-up's link was opened"
    description = "Takes the pendingKey sent at sign-up. Answers confirmed true once the link mailed in that sign-up " +
        "was opened, for ${Accounts.WATCH_LIFETIME.toDays()} days. An unknown or expired key answers false, as does one " +
        "sent for an address that was already confirmed, so a key never tells who has an account."
    operationId = "confirmationStatus"
    tags += accountsTag
    security = Public
    receiving(statusRequestBody to ConfirmationStatusRequest(EXAMPLE_KEY), definitionId = "ConfirmationStatusRequest")
    returning(OK, statusBody to ConfirmationStatus(confirmed = true), "Whether the link was opened", definitionId = "ConfirmationStatus")
} bindContract POST to { request ->
    val key = statusRequestBody(request).key
    val confirmed = key.length in PENDING_KEY_LENGTH && accounts.isConfirmed(key)
    Response(OK).with(statusBody of ConfirmationStatus(confirmed))
}

private const val EXAMPLE_KEY = "a-random-key-of-at-least-32-characters"
