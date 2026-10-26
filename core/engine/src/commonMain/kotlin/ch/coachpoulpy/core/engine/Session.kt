package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.DeviceMetrics
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Everything that happens during a session is an event, written to local storage
 * at once. The session state is the fold of those events, so a session killed by
 * the system resumes exactly where it was by replaying them.
 */
@Serializable
sealed interface SessionEvent {
    val at: Instant

    @Serializable @SerialName("started") data class Started(override val at: Instant) : SessionEvent
    @Serializable @SerialName("phaseCompleted") data class PhaseCompleted(override val at: Instant, val phaseIndex: Int) : SessionEvent
    @Serializable @SerialName("phaseSkipped") data class PhaseSkipped(override val at: Instant, val phaseIndex: Int) : SessionEvent
    @Serializable @SerialName("paused") data class Paused(override val at: Instant) : SessionEvent
    @Serializable @SerialName("resumed") data class Resumed(override val at: Instant) : SessionEvent
    @Serializable @SerialName("finished") data class Finished(override val at: Instant) : SessionEvent

    /** A reading from a connected machine, stamped by the phone's clock. It never moves the session on by itself. */
    @Serializable @SerialName("sample") data class Sample(override val at: Instant, val metrics: DeviceMetrics) : SessionEvent
}

/** What the UI renders. It only observes this; all decisions are made here. */
data class SessionState(
    val status: Status,
    val phaseIndex: Int,
    val phase: Phase?,
    /** Time spent in the current phase, excluding pauses. */
    val phaseElapsed: Duration,
    /** Time left in a timed phase, or null for a manual one. */
    val phaseRemaining: Duration?,
) {
    enum class Status { NOT_STARTED, RUNNING, PAUSED, FINISHED }
}

/**
 * The execution state machine. It is pure: [events] plus the current time give
 * the state, and [tick] says which events the clock has made due. The clock is
 * passed in, so tests never wait.
 */
class Session(val timeline: Timeline, events: List<SessionEvent> = emptyList()) {
    private val log = events.toMutableList()
    val events: List<SessionEvent> get() = log

    fun start(now: Instant): List<SessionEvent> = record(SessionEvent.Started(now))
    fun pause(now: Instant): List<SessionEvent> = record(SessionEvent.Paused(now))
    fun resume(now: Instant): List<SessionEvent> = record(SessionEvent.Resumed(now))
    fun completeCurrent(now: Instant): List<SessionEvent> = record(SessionEvent.PhaseCompleted(now, state(now).phaseIndex))
    fun skipCurrent(now: Instant): List<SessionEvent> = record(SessionEvent.PhaseSkipped(now, state(now).phaseIndex))

    /** Ends the session early; the phases not reached simply stay undone. */
    fun finish(now: Instant): List<SessionEvent> = record(SessionEvent.Finished(now))

    /** Appends a machine reading. Unlike the other actions it skips the fold, as it arrives every second. */
    fun recordSample(now: Instant, metrics: DeviceMetrics): SessionEvent.Sample =
        SessionEvent.Sample(now, metrics).also { log += it }

    /** Completes timed phases whose time is up. Call it from the UI clock, for example every 200 ms. */
    fun tick(now: Instant): List<SessionEvent> {
        val added = mutableListOf<SessionEvent>()
        while (true) {
            val state = state(now)
            val duration = state.phase?.duration ?: break
            if (state.status != SessionState.Status.RUNNING || state.phaseElapsed < duration) break
            // Stamp the completion at the moment the timer ran out, not at the tick,
            // so a late tick (app in background) does not shift the next phases.
            added += record(SessionEvent.PhaseCompleted(now - (state.phaseElapsed - duration), state.phaseIndex))
        }
        return added
    }

    private fun record(event: SessionEvent): List<SessionEvent> {
        log += event
        if (event !is SessionEvent.Finished && timeline.phaseAt(state(event.at).phaseIndex) == null) {
            log += SessionEvent.Finished(event.at)
            return listOf(event, log.last())
        }
        return listOf(event)
    }

    fun state(now: Instant): SessionState {
        var status = SessionState.Status.NOT_STARTED
        var index = 0
        var phaseStart: Instant? = null
        var pausedAt: Instant? = null
        var pausedTotal = Duration.ZERO

        for (event in log) when (event) {
            is SessionEvent.Started -> { status = SessionState.Status.RUNNING; phaseStart = event.at }
            is SessionEvent.Paused -> { status = SessionState.Status.PAUSED; pausedAt = event.at }
            is SessionEvent.Resumed -> {
                pausedAt?.let { pausedTotal += event.at - it }
                pausedAt = null
                status = SessionState.Status.RUNNING
            }
            is SessionEvent.PhaseCompleted, is SessionEvent.PhaseSkipped -> {
                index++
                phaseStart = event.at
                pausedTotal = Duration.ZERO
                // Ended during a pause: the new phase's share of the pause starts here.
                if (pausedAt != null) pausedAt = event.at
            }
            is SessionEvent.Finished -> status = SessionState.Status.FINISHED
            is SessionEvent.Sample -> Unit
        }

        val phase = timeline.phaseAt(index)
        val elapsed = when (val start = phaseStart) {
            null -> Duration.ZERO
            else -> ((pausedAt ?: now) - start - pausedTotal).coerceAtLeast(Duration.ZERO)
        }
        val remaining = phase?.duration?.let { (it - elapsed).coerceAtLeast(Duration.ZERO) }
        return SessionState(status, index, phase.takeIf { status != SessionState.Status.FINISHED }, elapsed, remaining)
    }
}

/**
 * The phases [events] went through: an open-ended timeline grows by one phase per step ended,
 * plus the one still running.
 */
fun Timeline.covering(events: List<SessionEvent>): Timeline {
    val ended = events.count { it is SessionEvent.PhaseCompleted || it is SessionEvent.PhaseSkipped }
    val running = if (events.any { it is SessionEvent.Finished }) 0 else 1
    return grownTo(ended + running)
}
