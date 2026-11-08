package ch.coachpoulpy.server.admin

import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.db.Database
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

/** One thing an admin did: who ([actorEmail] is null once their account is gone), what and to what. */
data class AuditEntry(val actorEmail: String?, val action: String, val target: String, val at: OffsetDateTime)

/**
 * The record of admin writes (#335). Every admin route that changes something calls [record];
 * `GET /api/v1/admin/audit` reads it back.
 */
class AdminAudit(private val database: Database, private val clock: Clock = Clock.systemUTC()) {
    private val queries get() = database.queries.adminAuditQueries

    /** Notes that [actor] did [action] to [target]; a null actor is the server itself, as at start-up. */
    fun record(actor: AccountId?, action: String, target: String) {
        queries.insert(
            UUID.fromString(Ids.newUuidV7(nowMillis = clock.millis())),
            actor?.let { UUID.fromString(it.value) },
            action,
            target,
            OffsetDateTime.now(clock),
        )
    }

    fun recent(limit: Long = 100): List<AuditEntry> =
        queries.recent(limit).executeAsList().map { AuditEntry(it.actor_email, it.action, it.target, it.at) }
}

/**
 * Makes the confirmed account with [email] an admin: how the first admin is made, from the
 * `ADMIN_EMAIL` setting at start-up, since no endpoint may do it. Nothing happens for an address
 * with no confirmed account, or one that already is an admin.
 */
fun promoteFirstAdmin(database: Database, audit: AdminAudit, email: String): Boolean {
    val id = database.queries.accountQueries.promote(email.trim()).executeAsOneOrNull() ?: return false
    audit.record(null, "admin.promote", id.toString())
    return true
}
