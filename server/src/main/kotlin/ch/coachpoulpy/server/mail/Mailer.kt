package ch.coachpoulpy.server.mail

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

/** Why the server writes: the status view says which kind of email the last send was (#340). */
enum class EmailKind(val code: String) {
    Confirmation("confirmation"),
    PasswordReset("password-reset"),
    Test("test"),
}

/** A plain-text email; [from] is filled in by [SettingsMailer] from the saved setting or `MAIL_FROM`. */
data class Email(val to: String, val subject: String, val text: String, val kind: EmailKind, val from: String? = null)

/** Sends an email, or throws when it can't. */
fun interface Mailer {
    fun send(email: Email)
}

/**
 * The SMTP server mail goes through (docs/accounts.md), from `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`,
 * `SMTP_PASSWORD` and `MAIL_FROM`; null when `SMTP_HOST` is unset, as on a laptop, and then
 * [NoMailServer] refuses every email.
 */
data class MailSettings(val host: String, val port: Int, val user: String, val password: String, val from: String) {
    override fun toString() = "MailSettings(host=$host, port=$port, user=$user, from=$from)"

    companion object {
        fun from(env: (String) -> String?): MailSettings? {
            val host = env("SMTP_HOST")?.takeIf { it.isNotBlank() } ?: return null
            return MailSettings(
                host = host,
                port = env("SMTP_PORT")?.takeIf { it.isNotBlank() }?.toInt() ?: 587,
                user = env("SMTP_USER").orEmpty(),
                password = env("SMTP_PASSWORD").orEmpty(),
                from = env("MAIL_FROM")?.takeIf { it.isNotBlank() } ?: error("Set MAIL_FROM along with SMTP_HOST"),
            )
        }
    }
}

/** Mail over SMTP: implicit TLS on port 465, STARTTLS required on any other. */
class SmtpMailer(private val settings: MailSettings) : Mailer {
    private val session: Session = Session.getInstance(
        Properties().apply {
            put("mail.smtp.host", settings.host)
            put("mail.smtp.port", settings.port.toString())
            put("mail.smtp.auth", settings.user.isNotEmpty().toString())
            if (settings.port == 465) {
                put("mail.smtp.ssl.enable", "true")
            } else {
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
            }
            put("mail.smtp.ssl.checkserveridentity", "true")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
        },
        object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(settings.user, settings.password)
        },
    )

    override fun send(email: Email) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(email.from ?: settings.from))
            setRecipient(Message.RecipientType.TO, InternetAddress(email.to))
            setSubject(email.subject, "UTF-8")
            setText(email.text, "UTF-8")
        }
        Transport.send(message)
        // Accepted by the SMTP server: if it never arrives, the provider's own log says why.
        println("Email \"${email.subject}\" to ${masked(email.to)} accepted by ${settings.host}")
    }
}

/** `a…@example.com`: enough to tell which sign-up a log line is about, without the whole address. */
fun masked(address: String): String =
    address.substringBefore('@').take(1) + "…@" + address.substringAfter('@', "")

/** Thrown for every email while the server has no mail server: `SMTP_HOST` is unset. */
class MailServerMissing : IllegalStateException("No mail server is set: SMTP_HOST is unset, so no email is sent")

/**
 * The mailer of a server with no SMTP settings, such as on a laptop: every email fails with
 * [MailServerMissing], and the admin's status view says mail isn't configured (#340). It never
 * prints the email, whose confirmation or reset link would end up in the logs.
 */
object NoMailServer : Mailer {
    override fun send(email: Email) {
        throw MailServerMissing()
    }
}
