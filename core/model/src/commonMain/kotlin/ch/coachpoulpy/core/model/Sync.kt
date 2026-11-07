package ch.coachpoulpy.core.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * One change a phone pushes to its account, or pulls from it (docs/accounts.md, Sync). Each kind
 * says on the wire what it is in `type`, and merges in its own way; applying one twice changes
 * nothing.
 */
@Serializable
sealed interface SyncChange

/**
 * The header of a session, as the phone that recorded it last wrote it. [format] is the
 * workout's format when it started and [totals] its saved `SessionTotals`.
 */
@Serializable
@SerialName("session")
data class SessionChange(
    val id: SessionId,
    val source: SessionSource,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val workout: WorkoutRef? = null,
    val title: String? = null,
    val format: WorkoutFormat? = null,
    val totals: JsonObject? = null,
) : SyncChange

/** Events of one session, each stored once by its [SyncedEvent.seq] (ADR 0003). */
@Serializable
@SerialName("session-events")
data class SessionEventsChange(val sessionId: SessionId, val events: List<SyncedEvent>) : SyncChange

/** A session event as stored: its place in the log, when it happened and the event itself. */
@Serializable
data class SyncedEvent(val seq: Long, val at: Instant, val payload: JsonObject)

/** A session deleted from History, gone from every phone. */
@Serializable
@SerialName("session-deleted")
data class SessionDeletedChange(val sessionId: SessionId, val deletedAt: Instant) : SyncChange

/**
 * A new version of a workout, made from [baseVersion] (none for the first). Versions never change
 * once made, so the server takes one only when it follows its own latest (ADR 0025).
 */
@Serializable
@SerialName("workout-version")
data class WorkoutVersionChange(val workout: Workout, val baseVersion: Int?, val createdAt: Instant) : SyncChange

/** A workout deleted at [version]; a newer version pushed by another phone wins over it (ADR 0025). */
@Serializable
@SerialName("workout-deleted")
data class WorkoutDeletedChange(val workoutId: WorkoutId, val version: Int, val deletedAt: Instant) : SyncChange

/** A planned workout put on [day], or moved there at [dayChangedAt]; the latest write wins. */
@Serializable
@SerialName("planned-workout")
data class PlannedWorkoutChange(
    val id: String,
    val workoutId: WorkoutId,
    val day: LocalDate,
    val createdAt: Instant,
    val dayChangedAt: Instant,
) : SyncChange

/** A planned workout taken off its day; it loses to a move made later. */
@Serializable
@SerialName("planned-workout-deleted")
data class PlannedWorkoutDeletedChange(val id: String, val deletedAt: Instant) : SyncChange

/** The [metric] logged on [day], or none to delete it; one per metric and day, the latest write wins. */
@Serializable
@SerialName("body-measurement")
data class BodyMeasurementChange(val metric: BodyMetric, val day: LocalDate, val value: Double?, val changedAt: Instant) : SyncChange

/** One profile field, the units or the language, or none to clear it; the latest write wins per key. */
@Serializable
@SerialName("setting")
data class SettingChange(val key: String, val value: String?, val changedAt: Instant) : SyncChange {
    companion object {
        /** The keys that follow the account. Theme, app icon and paired devices stay on each phone. */
        fun follows(key: String): Boolean = key.startsWith("profile.") || key in setOf("settings.units", "settings.language")
    }
}

/** What a phone pushes: its outbox, oldest first. */
@Serializable
data class SyncPush(val changes: List<SyncChange>)

/** One page of a pull: the changes after the cursor sent, the cursor to send next, and whether more wait. */
@Serializable
data class SyncPage(val changes: List<SyncChange>, val cursor: String, val more: Boolean)
