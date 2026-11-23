package ch.coachpoulpy.server.accounts

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionDeletedChange
import ch.coachpoulpy.core.model.SessionEventsChange
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncedEvent
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.data.local.StoredSession
import ch.coachpoulpy.server.sync.Sync
import ch.coachpoulpy.shared.sessionFitFile
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.datetime.TimeZone
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Export my data (docs/accounts.md): one .zip of everything that follows the account. `coach-poulpy.json` holds every change
 * the account stores, as sync pulls them; `sessions/` holds each finished session as a .FIT file for Garmin Connect or Strava.
 */
class AccountExport(private val sync: Sync, private val catalog: () -> Catalog) {
    fun zip(account: UUID): ByteArray {
        val changes = allChanges(account)
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(JSON_NAME))
            zip.write(pretty.encodeToString(JsonObject.serializer(), document(changes)).toByteArray())
            zip.closeEntry()
            val used = mutableSetOf<String>()
            fitFiles(changes).forEach { (name, bytes) ->
                val unique = generateSequence(name) { it.removeSuffix(".fit") + "-2.fit" }.first { used.add(it) }
                zip.putNextEntry(ZipEntry("sessions/$unique"))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private fun allChanges(account: UUID): List<SyncChange> = buildList {
        var after = 0L
        do {
            val page = sync.pull(account, after, PAGE)
            addAll(page.changes)
            after = page.cursor.toLong()
        } while (page.more)
    }

    private fun document(changes: List<SyncChange>) = JsonObject(
        mapOf(
            "format" to JsonPrimitive("coach-poulpy-export"),
            "version" to JsonPrimitive(1),
            "changes" to pretty.encodeToJsonElement(ListSerializer(SyncChange.serializer()), changes),
        ),
    )

    /** Each finished session still in the account, as its .FIT file name and bytes, oldest first. */
    private fun fitFiles(changes: List<SyncChange>): List<Pair<String, ByteArray>> {
        val sessions = linkedMapOf<String, SessionChange>()
        val events = mutableMapOf<String, MutableList<SyncedEvent>>()
        val workouts = mutableMapOf<WorkoutRef, Workout>()
        val deleted = mutableSetOf<String>()
        changes.forEach { change ->
            when (change) {
                is SessionChange -> sessions[change.id.value] = change
                is SessionEventsChange -> events.getOrPut(change.sessionId.value) { mutableListOf() } += change.events
                is SessionDeletedChange -> deleted += change.sessionId.value
                is WorkoutVersionChange -> workouts[WorkoutRef(change.workout.id, change.workout.version)] = change.workout
                else -> Unit
            }
        }
        val exercises = catalog()
        return sessions.values.filter { it.endedAt != null && it.id.value !in deleted }.sortedBy { it.startedAt }.mapNotNull { session ->
            val stored = StoredSession(SessionRecord(session.id, session.source, session.startedAt, session.endedAt, session.workout), session.title, session.format, totals = null)
            val sessionEvents = events[session.id.value].orEmpty().sortedBy { it.seq }.mapNotNull {
                runCatching { lenient.decodeFromJsonElement(SessionEvent.serializer(), it.payload) }.getOrNull()
            }
            sessionFitFile(stored, sessionEvents, session.workout?.let(workouts::get), exercises, TimeZone.UTC)?.let { it.fileName to it.bytes }
        }
    }

    companion object {
        const val JSON_NAME = "coach-poulpy.json"
        private const val PAGE = 500
        private val pretty = Json { prettyPrint = true }
        private val lenient = Json { ignoreUnknownKeys = true }
    }
}
