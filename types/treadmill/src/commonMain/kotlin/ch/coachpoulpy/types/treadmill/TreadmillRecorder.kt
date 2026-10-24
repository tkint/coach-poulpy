package ch.coachpoulpy.types.treadmill

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.Target
import kotlin.time.Instant

/**
 * Feeds treadmill readings into a session. Each reading becomes a sample in the
 * event log while the session runs; a distance block is completed once the
 * treadmill's distance counter has moved that far since the last reading before
 * the block began.
 * Returns the events added, which the caller writes to storage at once.
 */
class TreadmillRecorder(private val session: Session) {
    private var phaseIndex = -1
    private var phaseStartDistance: Double? = null
    private var lastDistance: Double? = null

    fun onReading(now: Instant, metrics: DeviceMetrics): List<SessionEvent> {
        // Tick first so a timed phase that just ran out is closed before this reading is
        // logged: the summary then reads each reading's phase from the log order.
        val added = session.tick(now).toMutableList()
        val state = session.state(now)
        if (state.status != SessionState.Status.RUNNING) return added

        added += session.recordSample(now, metrics)
        val distance = metrics.distanceMeters
        if (state.phaseIndex != phaseIndex) {
            phaseIndex = state.phaseIndex
            phaseStartDistance = lastDistance ?: distance
        }
        if (distance != null) lastDistance = distance

        val target = (state.phase as? Phase.Work)?.target as? Target.Distance
        val start = phaseStartDistance ?: distance.also { phaseStartDistance = it }
        if (target != null && start != null && distance != null && distance - start >= target.meters) {
            added += session.completeCurrent(now)
        }
        return added
    }
}
