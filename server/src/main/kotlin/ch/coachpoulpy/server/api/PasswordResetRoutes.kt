package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.accounts.AccountDeletion
import ch.coachpoulpy.server.accounts.PasswordResets
import ch.coachpoulpy.server.accounts.SignUpRules
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

@Serializable
data class PasswordResetRequest(val email: String)

@Serializable
data class NewPasswordRequest(val token: String, val password: String)

@Serializable
data class DeleteAccountRequest(val password: String)

private val resetBody = Body.auto<PasswordResetRequest>().toLens()
private val newPasswordBody = Body.auto<NewPasswordRequest>().toLens()
private val deleteBody = Body.auto<DeleteAccountRequest>().toLens()
private val problemBody = Body.auto<Problem>().toLens()

private val accountsTag = Tag("accounts")

/** A forgotten password: mails a reset link. The answer is the same for every address. */
fun passwordResetRoute(resets: PasswordResets): ContractRoute = "$API_PREFIX/password-resets" meta {
    summary = "Ask for a link to choose a new password"
    description = "Mails a link to the address when it belongs to a confirmed account. Every address gets the same 202, " +
        "so addresses can't be probed. At most ${PasswordResets.MAX_LINKS_PER_HOUR} links an hour go to one account."
    operationId = "requestPasswordReset"
    tags += accountsTag
    security = Public
    receiving(resetBody to PasswordResetRequest("ana@example.com"), definitionId = "PasswordReset")
    returning(ACCEPTED to "The link is on its way, if this address can get one")
} bindContract POST to { request ->
    resets.request(resetBody(request).email)
    Response(ACCEPTED)
}

/** Sets the new password with the token from a reset link, and signs every device out. */
fun newPasswordRoute(resets: PasswordResets): ContractRoute = "$API_PREFIX/password-resets/confirm" meta {
    summary = "Choose a new password"
    description = "Takes the token from the link mailed for a forgotten password. A token works once, for " +
        "${PasswordResets.LIFETIME.toHours()} hour. The password has ${SignUpRules.PASSWORD_MIN} to " +
        "${SignUpRules.PASSWORD_MAX} characters. Every device of the account is signed out."
    operationId = "resetPassword"
    tags += accountsTag
    security = Public
    receiving(newPasswordBody to NewPasswordRequest("the-token-after-the-hash-in-the-link", "correct horse battery"), definitionId = "NewPassword")
    returning(NO_CONTENT to "The password is changed")
    returning(BAD_REQUEST, problemBody to Problem("invalid-token"), "invalid-token or invalid-password", definitionId = "Problem")
} bindContract POST to { request ->
    val body = newPasswordBody(request)
    when (val problem = resets.reset(body.token, body.password)) {
        null -> Response(NO_CONTENT)
        else -> Response(BAD_REQUEST).with(problemBody of Problem(problem.code))
    }
}

/** Leaving: deletes the caller's account and everything in it, once they give its password. */
fun deleteMeRoute(deletion: AccountDeletion): ContractRoute = "$API_PREFIX/me" meta {
    summary = "Delete your account"
    description = "Deletes the account and every row of it at once, and signs its devices out. The account's password " +
        "must be sent again, and only Coach Poulpy's own apps may do it."
    operationId = "deleteMe"
    tags += accountsTag
    security = requires()
    receiving(deleteBody to DeleteAccountRequest("correct horse battery"), definitionId = "DeleteAccount")
    returning(NO_CONTENT to "The account is gone")
    returning(FORBIDDEN, problemBody to Problem("wrong-password"), "wrong-password or first-party-only", definitionId = "Problem")
} bindContract DELETE to { request ->
    when (val problem = deletion.deleteAccount(request.caller as Caller.Signed, deleteBody(request).password)) {
        null -> Response(NO_CONTENT)
        else -> Response(FORBIDDEN).with(problemBody of Problem(problem.code))
    }
}
