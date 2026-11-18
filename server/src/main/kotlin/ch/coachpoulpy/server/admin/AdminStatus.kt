package ch.coachpoulpy.server.admin

import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.mail.SendResult
import ch.coachpoulpy.server.mail.SettingsMailer
import java.time.OffsetDateTime

/** Whether the server can send emails, and how the last one it tried went (null since a restart). */
data class MailStatus(val configured: Boolean, val last: SendResult?)

/** An account whose email isn't confirmed yet: when it signed up and when its latest link stops working (null when none is left). */
data class PendingRegistration(val email: String, val signedUpAt: OffsetDateTime, val linkExpiresAt: OffsetDateTime?)

/**
 * The admin's status view (#340): what the server can do right now, starting with mail, and the
 * accounts waiting for their confirmation. Only admins see it; the public status says up or down
 * and nothing else. It reads, and writes nothing.
 */
class AdminStatus(private val database: Database, private val mailer: SettingsMailer) {
    fun mail(): MailStatus = MailStatus(mailer.configured, mailer.last)

    /** The [limit] newest pending registrations, newest first. */
    fun pendingRegistrations(limit: Int = PENDING_LIMIT): List<PendingRegistration> =
        database.queries.accountQueries.pendingRegistrations().executeAsList()
            .groupBy { it.id }
            .values
            .map { rows -> PendingRegistration(rows.first().email, rows.first().created_at, rows.mapNotNull { it.expires_at }.maxOrNull()) }
            .take(limit)

    companion object {
        const val PENDING_LIMIT = 100
    }
}
