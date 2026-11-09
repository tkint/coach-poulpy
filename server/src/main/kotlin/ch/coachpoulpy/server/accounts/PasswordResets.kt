package ch.coachpoulpy.server.accounts

import ch.coachpoulpy.server.ServerText
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.Secrets.hashOf
import ch.coachpoulpy.server.auth.Secrets.newToken
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.mail.Email
import ch.coachpoulpy.server.mail.Mailer
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

/** Why setting a new password or deleting an account was refused, as the API answers it. */
enum class AccountProblem(val code: String) {
    InvalidToken("invalid-token"),
    InvalidPassword("invalid-password"),
    WrongPassword("wrong-password"),
    NotFirstParty("first-party-only"),
}

/**
 * A forgotten password (docs/accounts.md): a link mailed to a confirmed address sets a new
 * password once, within an hour, and signs every device out.
 *
 * The link reads `<websiteUrl>/reset-password#<token>`, the token in the fragment as for
 * confirming an email.
 */
class PasswordResets(
    private val database: Database,
    private val mailer: Mailer,
    private val websiteUrl: String,
    private val signIns: SignIns,
    private val passwords: Passwords = Passwords(),
    private val clock: Clock = Clock.systemUTC(),
) {
    private val queries get() = database.queries.passwordResetQueries

    /**
     * Mails a reset link when [email] belongs to a confirmed account. Every address gets the same
     * answer from the API, so this says nothing back. At most [MAX_LINKS_PER_HOUR] links an hour
     * go to one account, so nobody can flood a mailbox.
     */
    fun request(email: String) {
        val now = OffsetDateTime.now(clock)
        val account = database.queries.accountQueries.byEmail(email.trim()).executeAsOneOrNull() ?: return
        if (account.confirmed_at == null) return
        if (queries.sentSince(account.id, now.minus(Duration.ofHours(1))).executeAsOne() >= MAX_LINKS_PER_HOUR) return
        val token = newToken()
        queries.insert(hashOf(token), account.id, now, now.plus(LIFETIME))
        val link = "${websiteUrl.trimEnd('/')}/reset-password#$token"
        val message = Email(
            to = account.email,
            subject = ServerText.emailResetSubject(account.language),
            text = ServerText.emailResetBody(account.language, link),
        )
        runCatching { mailer.send(message) }.onFailure { System.err.println("Could not send the password reset email: $it") }
    }

    /** Sets [password] on the account of [token] and signs all its devices out; null when done. */
    fun reset(token: String, password: String): AccountProblem? {
        if (password.length !in SignUpRules.PASSWORD_MIN..SignUpRules.PASSWORD_MAX) return AccountProblem.InvalidPassword
        val hash = passwords.hash(password)
        return database.queries.transactionWithResult {
            val now = OffsetDateTime.now(clock)
            val used = queries.consume(hashOf(token)).executeAsOneOrNull()
            if (used == null || !used.expires_at.isAfter(now)) return@transactionWithResult AccountProblem.InvalidToken
            database.queries.accountQueries.setPassword(hash, used.account_id)
            queries.deleteForAccount(used.account_id)
            signIns.revokeAll(used.account_id, now)
            null
        }
    }

    companion object {
        val LIFETIME: Duration = Duration.ofHours(1)
        const val MAX_LINKS_PER_HOUR = 3L
    }
}

/** Leaving (docs/accounts.md): the account and every row of it go at once. */
class AccountDeletion(
    private val database: Database,
    private val passwords: Passwords = Passwords(),
) {
    /**
     * Deletes [caller]'s account and every row of it, once [password] is its own; its devices
     * are signed out with it. Only a first-party sign-in may (ADR 0020). Null when done.
     */
    fun deleteAccount(caller: Caller.Signed, password: String): AccountProblem? {
        if (caller.client !in FIRST_PARTY_CLIENTS) return AccountProblem.NotFirstParty
        val id = caller.accountId.value
        val stored = database.queries.accountQueries.passwordHashById(id).executeAsOneOrNull() ?: return AccountProblem.WrongPassword
        if (!passwords.verify(password, stored)) return AccountProblem.WrongPassword
        erase(id)
        return null
    }

    /** Removes the account [id] and every row of it. The user's own deletion and an admin's both end here. */
    fun erase(id: UUID) {
        database.queries.transaction {
            val email = database.queries.accountQueries.delete(id).executeAsOneOrNull()
            email?.let { database.queries.signInQueries.deleteFailures(it) }
        }
    }

    companion object {
        /** The clients signed in with the account's own password: the apps, later the website. */
        val FIRST_PARTY_CLIENTS = setOf(Client.App)
    }
}
