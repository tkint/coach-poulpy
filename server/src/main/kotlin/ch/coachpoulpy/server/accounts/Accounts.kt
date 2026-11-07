package ch.coachpoulpy.server.accounts

import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.server.ServerText
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.mail.Email
import ch.coachpoulpy.server.mail.Mailer
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Base64
import java.util.UUID

/** What a guest gives to sign up (docs/accounts.md). */
data class SignUp(val email: String, val password: String, val language: String, val healthDataConsent: Boolean)

/**
 * Signing up and confirming the email (docs/accounts.md). An account starts unconfirmed; the
 * link mailed to it confirms it, and until then the phone stays a guest.
 *
 * [websiteUrl] is where the link points: `<websiteUrl>/confirm-email#<token>`. The token sits in
 * the fragment, which browsers never send, so it stays out of every access log on the way.
 */
class Accounts(
    private val database: Database,
    private val mailer: Mailer,
    private val websiteUrl: String,
    private val passwords: Passwords = Passwords(),
    private val clock: Clock = Clock.systemUTC(),
) {
    /**
     * Creates an unconfirmed account and mails it a confirmation link. An address already taken
     * changes nothing and gets the same answer, so addresses can't be probed: an unconfirmed one
     * is sent a new link, a confirmed one nothing. The password is hashed either way, so the
     * answer takes as long.
     *
     * Throws [IllegalArgumentException] when [signUp] breaks a rule in [SignUpRules].
     */
    fun signUp(signUp: SignUp) {
        SignUpRules.check(signUp)
        val address = signUp.email.trim()
        val hash = passwords.hash(signUp.password)
        val now = OffsetDateTime.now(clock)
        val token = newToken()
        val recipient: Recipient = database.queries.transactionWithResult<Recipient?> {
            val id = UUID.fromString(Ids.newUuidV7(nowMillis = clock.millis()))
            val language = SignUpRules.language(signUp.language)
            val created = database.queries.accountQueries.insert(id, address, hash, language, now, now).value > 0
            val account = if (created) null else database.queries.accountQueries.byEmail(address).executeAsOne()
            val recipient = when {
                account == null -> Recipient(id, address, language)
                account.confirmed_at != null -> null
                else -> Recipient(account.id, account.email, account.language)
            }
            recipient?.also { database.queries.confirmationTokenQueries.insert(hashOf(token), it.id, now.plus(CONFIRMATION_LIFETIME)) }
        } ?: return
        val link = "${websiteUrl.trimEnd('/')}/confirm-email#$token"
        val email = Email(
            to = recipient.email,
            subject = ServerText.emailConfirmSubject(recipient.language),
            text = ServerText.emailConfirmBody(recipient.language, link),
        )
        // The answer stays the same: signing up again sends a new link.
        runCatching { mailer.send(email) }.onFailure { System.err.println("Could not send the confirmation email: $it") }
    }

    /**
     * Marks the email of [token]'s account confirmed. False for a token that is unknown, already
     * used or more than [CONFIRMATION_LIFETIME] old; a token works once.
     */
    fun confirm(token: String): Boolean = database.queries.transactionWithResult {
        val used = database.queries.confirmationTokenQueries.consume(hashOf(token)).executeAsOneOrNull()
        val now = OffsetDateTime.now(clock)
        if (used == null || !used.expires_at.isAfter(now)) return@transactionWithResult false
        database.queries.accountQueries.confirm(now, used.account_id)
        database.queries.confirmationTokenQueries.deleteForAccount(used.account_id)
        true
    }

    private data class Recipient(val id: UUID, val email: String, val language: String)

    companion object {
        /** How long a confirmation link works. */
        val CONFIRMATION_LIFETIME: Duration = Duration.ofHours(24)

        private val random = SecureRandom()

        /** 32 random bytes, URL-safe. */
        private fun newToken(): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(random::nextBytes))

        /** Only this is stored, so a leaked table confirms nothing. */
        private fun hashOf(token: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
    }
}

/** What a sign-up must hold. */
object SignUpRules {
    const val PASSWORD_MIN = 8
    const val PASSWORD_MAX = 256
    const val EMAIL_MAX = 254

    /** The broken rule's code, as the API answers it, or null when [signUp] is fine. */
    fun problem(signUp: SignUp): String? {
        val email = signUp.email.trim()
        return when {
            email.length > EMAIL_MAX || !EMAIL.matches(email) -> "invalid-email"
            signUp.password.length !in PASSWORD_MIN..PASSWORD_MAX -> "invalid-password"
            !signUp.healthDataConsent -> "consent-required"
            else -> null
        }
    }

    fun check(signUp: SignUp) {
        problem(signUp)?.let { throw IllegalArgumentException(it) }
    }

    /** "fr" for "fr", "fr-CH" or "FR_ch", and "en" for anything that isn't a language code. */
    fun language(code: String): String =
        code.substringBefore('-').substringBefore('_').lowercase().takeIf { LANGUAGE.matches(it) } ?: "en"

    /** Something, an @, and a domain with a dot: the link mailed to it is the real check. */
    private val EMAIL = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
    private val LANGUAGE = Regex("[a-z]{2,3}")
}
