package ch.coachpoulpy.core.stats

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * What one finished session amounts to. It is worked out from the event log once and
 * saved next to the session, so the history opens without replaying every reading.
 * Machine values are null when no machine sent them.
 */
@Serializable
data class SessionTotals(
    /** From start to finish, without the pauses. */
    val active: Duration,
    /** Work phases done, skipped and planned; null when the plan is no longer on the phone. */
    val setsDone: Int? = null,
    val setsSkipped: Int? = null,
    val setsPlanned: Int? = null,
    val distanceMeters: Double? = null,
    val averageSpeedKmh: Double? = null,
    val averageHeartRateBpm: Int? = null,
    val maxHeartRateBpm: Int? = null,
    val energyKcal: Int? = null,
    val estimatedSteps: Int? = null,
) {
    /** Nothing was done: started and stopped at once, or closed before it started. */
    val isEmpty: Boolean get() = active == Duration.ZERO && (setsDone ?: 0) == 0 && distanceMeters == null

    companion object {
        /** Time and sets of a session; [timeline] is the plan it followed, when still known. */
        fun of(events: List<SessionEvent>, timeline: Timeline?): SessionTotals {
            val phases = timeline?.phases
            fun workCount(pick: (SessionEvent) -> Int?) =
                phases?.let { p -> events.mapNotNull(pick).count { p.getOrNull(it) is Phase.Work } }
            return SessionTotals(
                active = activeTime(events),
                setsDone = workCount { (it as? SessionEvent.PhaseCompleted)?.phaseIndex },
                setsSkipped = workCount { (it as? SessionEvent.PhaseSkipped)?.phaseIndex },
                setsPlanned = phases?.count { it is Phase.Work },
            )
        }

        /**
         * Time between the start and the finish, without pauses. A session the app never
         * finished (killed, then closed later) ends at its last event.
         */
        fun activeTime(events: List<SessionEvent>): Duration {
            var start: Instant? = null
            var end: Instant? = null
            var pausedAt: Instant? = null
            var paused = Duration.ZERO
            for (event in events) {
                when (event) {
                    is SessionEvent.Started -> if (start == null) start = event.at
                    is SessionEvent.Paused -> if (pausedAt == null) pausedAt = event.at
                    is SessionEvent.Resumed -> {
                        pausedAt?.let { paused += event.at - it }
                        pausedAt = null
                    }
                    is SessionEvent.Finished -> { end = event.at; break }
                    else -> Unit
                }
            }
            val from = start ?: return Duration.ZERO
            val to = end ?: events.maxOf { it.at }
            val openPause = pausedAt?.let { to - it } ?: Duration.ZERO
            return (to - from - paused - openPause).coerceAtLeast(Duration.ZERO)
        }
    }
}
