package ch.coachpoulpy.core.model

import kotlin.time.Instant
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

/** What a phone pushes: its outbox, oldest first. */
@Serializable
data class SyncPush(val changes: List<SyncChange>)

/** One page of a pull: the changes after the cursor sent, the cursor to send next, and whether more wait. */
@Serializable
data class SyncPage(val changes: List<SyncChange>, val cursor: String, val more: Boolean)
