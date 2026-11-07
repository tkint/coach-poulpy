package ch.coachpoulpy.server.mail

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

/** A plain-text email. */
data class Email(val to: String, val subject: String, val text: String)

/** Sends an email, or throws when it can't. */
fun interface Mailer {
    fun send(email: Email)
}

/**
 * The SMTP server mail goes through (docs/accounts.md), from `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`,
 * `SMTP_PASSWORD` and `MAIL_FROM`; null when `SMTP_HOST` is unset, as on a laptop.
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
            setFrom(InternetAddress(settings.from))
            setRecipient(Message.RecipientType.TO, InternetAddress(email.to))
            setSubject(email.subject, "UTF-8")
            setText(email.text, "UTF-8")
        }
        Transport.send(message)
    }
}

/** Prints each email instead of sending it, for a server with no SMTP settings, such as on a laptop. */
object ConsoleMailer : Mailer {
    override fun send(email: Email) {
        println("Email to ${email.to}, not sent because SMTP_HOST is unset\nSubject: ${email.subject}\n\n${email.text}\n")
    }
}
