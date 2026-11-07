package ch.coachpoulpy.server.sync

import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionDeletedChange
import ch.coachpoulpy.core.model.SessionEventsChange
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncedEvent
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutDeletedChange
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.server.db.Database
import migrations.Session
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** A change a push can't take: its place in the push, and why. */
class InvalidChange(val index: Int, override val message: String) : Exception(message)

/**
 * Workouts whose pushed version doesn't follow the server's latest: another phone got there
 * first. The phone pulls, keeps its own edit as a copy and pushes again (ADR 0025).
 */
class StaleBase(val workouts: List<WorkoutId>) : Exception("Stale base for ${workouts.joinToString()}")

/**
 * The account's side of sync (docs/accounts.md, Sync; ADR 0025). A push is stored whole or not
 * at all, under a lock on the account row; every row it writes takes the account's next change
 * number, which is what a pull's cursor counts. Applying a change twice changes nothing.
 */
class Sync(private val database: Database) {
    private val queries get() = database.queries.syncQueries

    /** Stores [changes] for [account], or throws [InvalidChange] or [StaleBase] and stores none of them. */
    fun push(account: UUID, changes: List<SyncChange>) = database.queries.transaction {
        queries.lockAccount(account).executeAsOne()
        val stale = linkedSetOf<WorkoutId>()
        changes.forEachIndexed { index, change ->
            when (change) {
                is SessionChange -> apply(account, index, change)
                is SessionEventsChange -> apply(account, index, change)
                is SessionDeletedChange -> apply(account, index, change)
                // Later versions of a stale workout are stale too; every one is named in the answer.
                is WorkoutVersionChange -> if (change.workout.id in stale || !apply(account, index, change)) stale += change.workout.id
                is WorkoutDeletedChange -> if (change.workoutId !in stale) apply(account, index, change)
            }
        }
        if (stale.isNotEmpty()) throw StaleBase(stale.toList())
    }

    /** The changes of [account] after change number [after], at most [limit] of them, oldest first. */
    fun pull(account: UUID, after: Long, limit: Int): SyncPage = database.queries.transactionWithResult {
        val fetch = limit + 1L
        val sessions = queries.sessionsAfter(account, after, fetch).executeAsList().map { Row(it.change_number, change = it.toChange()) }
        val events = queries.eventsAfter(account, after, fetch).executeAsList().map {
            Row(it.change_number, event = SessionId(it.session_id.toString()) to SyncedEvent(it.seq, it.at.toKotlin(), json.decodeFromString(JsonObject.serializer(), it.payload)))
        }
        val versions = queries.workoutVersionsAfter(account, after, fetch).executeAsList().map {
            val workout = json.decodeFromString(Workout.serializer(), it.content!!)
            Row(it.change_number, change = WorkoutVersionChange(workout, (it.version - 1).takeIf { base -> base > 0 }, it.created_at.toKotlin()))
        }
        val deletions = queries.workoutDeletionsAfter(account, after, fetch).executeAsList().map {
            Row(it.change_number, change = WorkoutDeletedChange(WorkoutId(it.workout_id.toString()), it.version, it.deleted_at.toKotlin()))
        }
        val rows = (sessions + events + versions + deletions).sortedBy { it.number }
        val page = rows.take(limit)
        val cursor = page.lastOrNull()?.number ?: after
        SyncPage(group(account, page, after), cursor.toString(), more = rows.size > limit)
    }

    private fun apply(account: UUID, index: Int, change: SessionChange) {
        val id = uuid(index, change.id.value)
        val workoutId = change.workout?.let { uuid(index, it.workoutId.value) }
        val stored = queries.sessionById(account, id).executeAsOneOrNull()
        if (stored?.deleted_at != null || stored?.toChange() == change) return
        val totals = change.totals?.let { json.encodeToString(JsonObject.serializer(), it) }
        val number = next(account)
        val startedAt = change.startedAt.toOffset()
        val endedAt = change.endedAt?.toOffset()
        if (stored == null) {
            queries.insertSession(
                account, id, change.source.name, startedAt, endedAt, workoutId, change.workout?.version,
                change.title, change.format?.name, totals, number,
            )
        } else {
            queries.updateSession(
                change.source.name, startedAt, endedAt, workoutId, change.workout?.version,
                change.title, change.format?.name, totals, number, account, id,
            )
        }
    }

    private fun apply(account: UUID, index: Int, change: SessionEventsChange) {
        val id = uuid(index, change.sessionId.value)
        val stored = queries.sessionById(account, id).executeAsOneOrNull()
            ?: throw InvalidChange(index, "The session's header must be pushed before its events")
        if (stored.deleted_at != null) return
        change.events.forEach { event ->
            if (!queries.hasEvent(account, id, event.seq).executeAsOne()) {
                val payload = json.encodeToString(JsonObject.serializer(), event.payload)
                queries.insertEvent(account, id, event.seq, event.at.toOffset(), payload, next(account))
            }
        }
    }

    private fun apply(account: UUID, index: Int, change: SessionDeletedChange) {
        val id = uuid(index, change.sessionId.value)
        val stored = queries.sessionById(account, id).executeAsOneOrNull() ?: return
        if (stored.deleted_at != null) return
        queries.deleteEvents(account, id)
        queries.markSessionDeleted(change.deletedAt.toOffset(), next(account), account, id)
    }

    /** Stores a workout version, or returns false when it doesn't follow the server's latest. */
    private fun apply(account: UUID, index: Int, change: WorkoutVersionChange): Boolean {
        val workout = change.workout
        val id = uuid(index, workout.id.value)
        if (workout.version != (change.baseVersion ?: 0) + 1) {
            throw InvalidChange(index, "Version ${workout.version} isn't made from version ${change.baseVersion}")
        }
        queries.workoutVersion(account, id, workout.version).executeAsOneOrNull()?.let { stored ->
            return json.decodeFromString(Workout.serializer(), stored) == workout
        }
        val latest = queries.latestWorkoutVersion(account, id).executeAsOne().latest ?: 0
        if (latest != workout.version - 1) {
            if (latest == 0) throw InvalidChange(index, "Version ${workout.version - 1} of ${workout.id.value} was never pushed")
            return false
        }
        val content = json.encodeToString(Workout.serializer(), workout)
        queries.insertWorkoutVersion(account, id, workout.version, workout.format.name, workout.name, content, change.createdAt.toOffset(), next(account))
        // The edit wins over a deletion pushed before it.
        queries.undeleteWorkout(account, id)
        return true
    }

    private fun apply(account: UUID, index: Int, change: WorkoutDeletedChange) {
        val id = uuid(index, change.workoutId.value)
        val latest = queries.latestWorkoutVersion(account, id).executeAsOne().latest ?: return
        if (latest > change.version || queries.workoutDeletion(account, id).executeAsOneOrNull() != null) return
        queries.insertWorkoutDeletion(account, id, change.version, change.deletedAt.toOffset(), next(account))
    }

    /**
     * Turns a page of rows into changes, joining the events of one session that follow each other.
     * Events whose header changed after the cursor come after their header's latest version, so
     * a phone never gets events for a session it doesn't have yet; that header comes again at its
     * own place, which changes nothing on the phone.
     */
    private fun group(account: UUID, page: List<Row>, after: Long): List<SyncChange> {
        val changes = mutableListOf<SyncChange>()
        val headersSent = mutableSetOf<SessionId>()
        page.forEach { row ->
            row.change?.let {
                changes += it
                if (it is SessionChange) headersSent += it.id
            }
            row.event?.let { (sessionId, event) ->
                val last = changes.lastOrNull()
                if (last is SessionEventsChange && last.sessionId == sessionId) {
                    changes[changes.lastIndex] = last.copy(events = last.events + event)
                } else {
                    if (sessionId !in headersSent) {
                        val header = queries.sessionById(account, UUID.fromString(sessionId.value)).executeAsOne()
                        if (header.change_number > after) {
                            header.toChange()?.let { changes += it }
                            headersSent += sessionId
                        }
                    }
                    changes += SessionEventsChange(sessionId, listOf(event))
                }
            }
        }
        return changes
    }

    private fun next(account: UUID): Long = queries.nextChange(account).executeAsOne()

    private fun uuid(index: Int, value: String): UUID = runCatching { UUID.fromString(value) }.getOrNull()
        ?.takeIf { it.toString() == value.lowercase() }
        ?: throw InvalidChange(index, "$value is not a UUID")

    private class Row(val number: Long, val change: SyncChange? = null, val event: Pair<SessionId, SyncedEvent>? = null)

    companion object {
        private val json = Json
    }
}

private fun Instant.toOffset(): OffsetDateTime = OffsetDateTime.ofInstant(toJavaInstant(), ZoneOffset.UTC)

private fun OffsetDateTime.toKotlin(): Instant = toInstant().toKotlinInstant()

/** The change a stored header stands for: its latest version, or its deletion. */
private fun Session.toChange(): SyncChange? = when {
    deleted_at != null -> SessionDeletedChange(SessionId(id.toString()), deleted_at.toKotlin())
    else -> sessionChange(id, source, started_at, ended_at, workout_id, workout_version, title, format, totals)
}

private fun ch.coachpoulpy.server.db.SessionsAfter.toChange(): SyncChange = when {
    deleted_at != null -> SessionDeletedChange(SessionId(id.toString()), deleted_at.toKotlin())
    else -> sessionChange(id, source, started_at, ended_at, workout_id, workout_version, title, format, totals)
}

private fun sessionChange(
    id: UUID, source: String, startedAt: OffsetDateTime, endedAt: OffsetDateTime?, workoutId: UUID?, workoutVersion: Int?,
    title: String?, format: String?, totals: String?,
) = SessionChange(
    id = SessionId(id.toString()),
    source = SessionSource.valueOf(source),
    startedAt = startedAt.toKotlin(),
    endedAt = endedAt?.toKotlin(),
    workout = workoutId?.let { WorkoutRef(WorkoutId(it.toString()), workoutVersion ?: 1) },
    title = title,
    format = format?.let(WorkoutFormat::valueOf),
    totals = totals?.let { Json.decodeFromString(JsonObject.serializer(), it) },
)
