package ch.coachpoulpy.server.admin

import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.db.Database
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID
import org.http4k.events.Event
import org.http4k.events.Events

/**
 * Something an admin did (#335), emitted through [Events] like any other event so that it can go to
 * several places: the database ([AdminAudit.events]) and the log. [actor] is null for the server
 * itself, as at start-up; [target] names what changed, such as an account id or an exercise slug.
 * It never holds a password or an email address: use a masked value for those.
 */
data class AdminAction(val actor: AccountId?, val action: String, val target: String) : Event

/** One row of the audit log: who ([actorEmail] is null once their account is gone), what and to what. */
data class AuditEntry(val actorEmail: String?, val action: String, val target: String, val at: OffsetDateTime)

/**
 * The audit log in Postgres (#335). Admin routes emit [AdminAction]s; [events] stores each one, and
 * `GET /api/v1/admin/audit` reads them back with [recent].
 */
class AdminAudit(private val database: Database, private val clock: Clock = Clock.systemUTC()) {
    private val queries get() = database.queries.adminAuditQueries

    /** Stores every [AdminAction] sent to it and ignores other events. Combine it with a log sink using `and`. */
    val events: Events = { event ->
        if (event is AdminAction) {
            queries.insert(
                UUID.fromString(Ids.newUuidV7(nowMillis = clock.millis())),
                event.actor?.value,
                event.action,
                event.target,
                OffsetDateTime.now(clock),
            )
        }
    }

    fun recent(limit: Long = 100): List<AuditEntry> =
        queries.recent(limit).executeAsList().map { AuditEntry(it.actor_email, it.action, it.target, it.at) }
}

/**
 * Makes the confirmed account with [email] an admin: how the first admin is made, from the
 * `ADMIN_EMAIL` setting at start-up, since no endpoint may do it. Emits an [AdminAction] and returns
 * true; nothing happens for an address with no confirmed account, or one that already is an admin.
 */
fun promoteFirstAdmin(database: Database, events: Events, email: EmailAddress): Boolean {
    val id = database.queries.accountQueries.promote(email.value).executeAsOneOrNull() ?: return false
    events(AdminAction(null, "admin.promote", id.toString()))
    return true
}
