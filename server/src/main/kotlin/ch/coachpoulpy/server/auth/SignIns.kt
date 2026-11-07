package ch.coachpoulpy.server.auth

import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.server.accounts.Passwords
import ch.coachpoulpy.server.auth.Secrets.hashOf
import ch.coachpoulpy.server.auth.Secrets.newToken
import ch.coachpoulpy.server.db.Database
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

/** The tokens a sign-in or a refresh hands out (ADR 0020). */
data class Tokens(val accessToken: String, val refreshToken: String, val expiresIn: Duration)

sealed interface SignInResult {
    data class Signed(val tokens: Tokens) : SignInResult

    /** Wrong address or password, or an email not confirmed yet: one answer for all three. */
    data object Refused : SignInResult

    /** Too many failures for this address lately. */
    data class Throttled(val retryAfter: Duration) : SignInResult
}

/**
 * Signing in with an email and a password, refreshing and signing out (docs/accounts.md,
 * ADR 0020). Each phone or browser signed in is one sign-in: a 15-minute access token and a
 * refresh token that works once. Reusing a refresh token revokes its whole sign-in, so a stolen
 * one stops working as soon as either holder uses it again.
 */
class SignIns(
    private val database: Database,
    private val passwords: Passwords = Passwords(),
    private val clock: Clock = Clock.systemUTC(),
) {
    private val queries get() = database.queries.signInQueries

    /** Verified against when the address is unknown, so the answer takes as long either way. */
    private val decoy: String by lazy { passwords.hash("not anyone's password") }

    fun signIn(email: String, password: String, deviceName: String, client: Client = Client.App): SignInResult {
        val address = email.trim()
        val now = OffsetDateTime.now(clock)
        if (queries.failuresSince(address, now.minus(FAILURE_WINDOW)).executeAsOne() >= MAX_FAILURES) {
            return SignInResult.Throttled(FAILURE_WINDOW)
        }
        val account = database.queries.accountQueries.forSignIn(address).executeAsOneOrNull()
        val passwordMatches = passwords.verify(password, account?.password_hash ?: decoy)
        if (account == null || !passwordMatches || account.confirmed_at == null) {
            queries.recordFailure(address, now)
            return SignInResult.Refused
        }
        return database.queries.transactionWithResult {
            queries.forgetFailures(address, now.minus(FAILURE_WINDOW))
            val signIn = UUID.fromString(Ids.newUuidV7(nowMillis = clock.millis()))
            queries.insert(signIn, account.id, deviceName.trim(), client.value, now, now)
            SignInResult.Signed(issue(signIn, now))
        }
    }

    /**
     * New tokens for a refresh token, which then stops working. Null when it is unknown, expired
     * or its sign-in revoked; a refresh token used twice revokes its sign-in.
     */
    fun refresh(refreshToken: String): Tokens? = database.queries.transactionWithResult {
        val now = OffsetDateTime.now(clock)
        val hash = hashOf(refreshToken)
        val used = queries.useRefreshToken(now, hash).executeAsOneOrNull()
        if (used == null) {
            queries.refreshTokenSignIn(hash).executeAsOneOrNull()?.let { revoke(it, now) }
            return@transactionWithResult null
        }
        if (!used.expires_at.isAfter(now) || queries.isLive(used.sign_in_id).executeAsOneOrNull() != true) {
            return@transactionWithResult null
        }
        queries.deleteExpiredAccessTokens(used.sign_in_id, now)
        queries.refreshed(now, used.sign_in_id)
        issue(used.sign_in_id, now)
    }

    /**
     * Signs out the device [token] belongs to, an access or a refresh token: every token of its
     * sign-in stops working. Nothing happens for an unknown token.
     */
    fun revoke(token: String) = database.queries.transaction {
        val now = OffsetDateTime.now(clock)
        val hash = hashOf(token)
        val signIn = queries.liveCaller(hash, now).executeAsOneOrNull()?.id
            ?: queries.refreshTokenSignIn(hash).executeAsOneOrNull()
        signIn?.let { revoke(it, now) }
    }

    /** The `cp_at_` authenticator: a live access token gives its account, client and scopes. */
    val authenticator = Authenticator { token ->
        queries.liveCaller(hashOf(token), OffsetDateTime.now(clock)).executeAsOneOrNull()?.let {
            Caller.Signed(AccountId(it.account_id.toString()), Client(it.client), FIRST_PARTY_SCOPES)
        }
    }

    /** Signs every device of [account] out, as a new password does. Call it inside a transaction. */
    internal fun revokeAll(account: UUID, now: OffsetDateTime) {
        queries.revokeAll(now, account)
        queries.deleteAllAccessTokens(account)
    }

    private fun revoke(signIn: UUID, now: OffsetDateTime) {
        queries.revoke(now, signIn)
        queries.deleteAccessTokens(signIn)
    }

    private fun issue(signIn: UUID, now: OffsetDateTime): Tokens {
        val access = newToken(ACCESS_PREFIX)
        val refresh = newToken(REFRESH_PREFIX)
        queries.insertAccessToken(hashOf(access), signIn, now.plus(ACCESS_LIFETIME))
        queries.insertRefreshToken(hashOf(refresh), signIn, now.plus(REFRESH_LIFETIME))
        return Tokens(access, refresh, ACCESS_LIFETIME)
    }

    companion object {
        const val ACCESS_PREFIX = "cp_at_"
        const val REFRESH_PREFIX = "cp_rt_"
        val ACCESS_LIFETIME: Duration = Duration.ofMinutes(15)

        /** A phone unused this long signs in again. Each refresh starts it over. */
        val REFRESH_LIFETIME: Duration = Duration.ofDays(90)

        /** More failures than this for one address within [FAILURE_WINDOW] are refused unchecked. */
        const val MAX_FAILURES = 5L
        val FAILURE_WINDOW: Duration = Duration.ofMinutes(15)

        /** What the app and the website get: every user scope, and `sync` (ADR 0020). */
        val FIRST_PARTY_SCOPES: Set<Scope> = Scope.entries.toSet()
    }
}
