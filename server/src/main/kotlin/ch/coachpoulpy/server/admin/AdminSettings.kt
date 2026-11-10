package ch.coachpoulpy.server.admin

import ch.coachpoulpy.server.ServerText
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.mail.Email
import ch.coachpoulpy.server.mail.Mailer
import ch.coachpoulpy.server.mail.smtpError
import dev.forkhandles.values.parseOrNull
import jakarta.mail.internet.AddressException
import jakarta.mail.internet.InternetAddress
import java.time.Clock
import java.time.OffsetDateTime
import org.http4k.events.Events

/**
 * The server settings an admin changes without a deploy (#339), in the `settings` table. Only
 * non-secret values live there: the SMTP host, port, user and password stay in the environment.
 * A setting never saved keeps its default from the environment, such as [defaultMailFrom]
 * (`MAIL_FROM`).
 */
class ServerSettings(
    private val database: Database,
    private val defaultMailFrom: String? = null,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val queries get() = database.queries.settingsQueries

    /** The from-address the admin saved, or null while none is. */
    fun savedMailFrom(): String? = queries.valueOf(MAIL_FROM).executeAsOneOrNull()

    /** The from-address emails leave from now: the saved one, else `MAIL_FROM`; null when neither is set. */
    fun mailFrom(): String? = savedMailFrom() ?: defaultMailFrom

    fun saveMailFrom(value: String) {
        queries.save(MAIL_FROM, value, OffsetDateTime.now(clock))
    }

    companion object {
        const val MAIL_FROM = "mail.from"
    }
}

/**
 * [text] as a from-address, such as `hello@example.com` or `Coach Poulpy <hello@example.com>`,
 * or null when it isn't one: one address, with a name before the `@` and a domain after it.
 */
fun mailFromOrNull(text: String): String? {
    val trimmed = text.trim()
    val address = try {
        InternetAddress(trimmed, true)
    } catch (_: AddressException) {
        return null
    }
    return trimmed.takeIf { EmailAddress.parseOrNull(address.address.orEmpty()) != null }
}

/** The settings as the admin's screen shows them: the from-address in use, and whether it is a saved one or `MAIL_FROM`. */
data class MailSettingsView(val mailFrom: String?, val saved: Boolean)

/** What the test email did: [error] is what the mail server said, null when it took the email. */
data class TestEmailResult(val error: String?)

/**
 * The admin's settings screen (#339): reads and saves the from-address, each change emitting an
 * [AdminAction] whose target is the masked address, and sends a test email to the admin.
 */
class AdminSettings(
    private val database: Database,
    private val settings: ServerSettings,
    private val mailer: Mailer,
    private val events: Events,
) {
    fun mail(): MailSettingsView = MailSettingsView(settings.mailFrom(), settings.savedMailFrom() != null)

    /** Saves [text] as the from-address of the next emails; false, and nothing saved, when it isn't an address. */
    fun saveMailFrom(actor: AccountId, text: String): Boolean {
        val from = mailFromOrNull(text) ?: return false
        settings.saveMailFrom(from)
        val address = EmailAddress.of(InternetAddress(from, true).address)
        events(AdminAction(actor, "admin.settings.mail-from", address.toString()))
        return true
    }

    /** Sends a test email to [admin]'s own address with the saved settings, and says what the mail server answered. */
    fun sendTestEmail(admin: AccountId): TestEmailResult {
        val account = database.queries.accountQueries.contactById(admin.value).executeAsOne()
        val email = Email(
            to = account.email,
            subject = ServerText.emailTestSubject(account.language),
            text = ServerText.emailTestBody(account.language),
        )
        return try {
            mailer.send(email)
            TestEmailResult(null)
        } catch (e: Exception) {
            TestEmailResult(smtpError(e))
        }
    }
}
