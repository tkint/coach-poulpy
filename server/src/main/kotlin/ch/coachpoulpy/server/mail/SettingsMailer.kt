package ch.coachpoulpy.server.mail

import ch.coachpoulpy.server.admin.EmailAddress
import dev.forkhandles.values.parseOrNull
import java.time.Clock
import java.time.Instant

/**
 * How the last email the server tried to send went: when, to whom (masked), and what the mail
 * server said when it refused it, or null when it took it.
 */
data class SendResult(val at: Instant, val to: EmailAddress?, val error: String?) {
    val sent: Boolean get() = error == null
}

/**
 * Every email the server sends goes through here (#339): it leaves from the from-address [from]
 * gives at that moment (the admin's saved setting, else `MAIL_FROM`), so a saved change applies
 * to the next email with no restart. The result of the last try stays in [last] for the admin's
 * status view (#340); it is kept in memory, so a restart forgets it.
 */
class SettingsMailer(
    private val transport: Mailer,
    private val from: () -> String?,
    private val clock: Clock = Clock.systemUTC(),
) : Mailer {
    @Volatile
    var last: SendResult? = null
        private set

    override fun send(email: Email) {
        val to = EmailAddress.parseOrNull(email.to)
        try {
            transport.send(email.copy(from = email.from ?: from()))
        } catch (e: Exception) {
            last = SendResult(clock.instant(), to, smtpError(e))
            throw e
        }
        last = SendResult(clock.instant(), to, null)
    }
}

/** What the mail server said, on one line, such as `535 Authentication failed`. */
fun smtpError(e: Throwable): String =
    (e.message ?: e::class.simpleName ?: "Unknown error").lines().map(String::trim).filter(String::isNotEmpty).joinToString(" ")
