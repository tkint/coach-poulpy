package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.types.treadmill.TreadmillModule
import kotlin.time.Duration
import kotlin.time.Instant

/** The time between each start or resume and the next pause or finish, up to [now]. */
internal fun activeTime(events: List<SessionEvent>, now: Instant): Duration {
    var total = Duration.ZERO
    var runningSince: Instant? = null
    for (event in events) {
        when (event) {
            is SessionEvent.Started, is SessionEvent.Resumed -> if (runningSince == null) runningSince = event.at
            is SessionEvent.Paused, is SessionEvent.Finished -> runningSince?.let {
                total += event.at - it
                runningSince = null
            }
            else -> Unit
        }
    }
    return total + (runningSince?.let { now - it } ?: Duration.ZERO)
}

/** The plan [execution] followed, as far as the phone still has it, for its totals. */
internal fun LocalStore.timelineOf(execution: Execution, compiler: TimelineCompiler): Timeline? {
    val workout = execution.workout?.let { workout(it.workoutId, it.version) }
    return when {
        workout != null -> runCatching { compiler.compile(workout) }.getOrNull()
        execution.source == ExecutionSource.DEVICE_ONLY -> TreadmillModule.freeRun
        else -> null
    }
}

/**
 * Closes a session left open at its last event, never at its start, and sums it up, so it shows in
 * History like any finished session.
 */
internal fun LocalStore.closeLeftOpen(execution: Execution, timeline: Timeline?) {
    val id = execution.id.value
    val events = events(id)
    finishExecution(id, events.lastOrNull()?.at ?: execution.startedAt)
    saveTotals(id, sessionTotals(events, timeline))
}
