package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SessionTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    private fun work(target: Target, completion: CompletionRule) = Phase.Work(
        step = StepId("s"),
        exercise = ExerciseSlug("plank"),
        setIndex = 0,
        round = 1,
        target = target,
        side = Side.BOTH,
        completion = completion,
        duration = (target as? Target.Time)?.duration,
    )

    private val timeline = Timeline(
        listOf(
            work(Target.Time(30.seconds), CompletionRule.Timer),
            Phase.Rest(10.seconds),
            work(Target.Reps(10), CompletionRule.Manual),
        ),
    )

    @Test
    fun timedPhasesAdvanceOnTickAndPausesDoNotCount() {
        val session = Session(timeline)
        session.start(t0)
        session.pause(t0 + 10.seconds)
        session.resume(t0 + 20.seconds)

        // 30 s on the clock minus a 10 s pause: 20 s done, 10 s left.
        assertEquals(10.seconds, session.state(t0 + 30.seconds).phaseRemaining)

        // A late tick completes the 30 s phase (ends at 40 s, after the pause) and the
        // 10 s rest (ends at 50 s), each stamped when it ran out rather than at the tick.
        session.tick(t0 + 65.seconds)
        val state = session.state(t0 + 65.seconds)
        assertEquals(2, state.phaseIndex)
        assertEquals(15.seconds, state.phaseElapsed)
    }

    @Test
    fun manualLastPhaseFinishesTheSessionAndReplayGivesSameState() {
        val session = Session(timeline)
        session.start(t0)
        session.tick(t0 + 40.seconds)
        session.completeCurrent(t0 + 90.seconds)

        assertEquals(SessionState.Status.FINISHED, session.state(t0 + 90.seconds).status)

        val replayed = Session(timeline, session.events)
        assertEquals(session.state(t0 + 100.seconds), replayed.state(t0 + 100.seconds))
    }

    @Test
    fun anOpenEndedTimelineGrowsAStepAtATimeUntilFinished() {
        val free = Timeline(listOf(work(Target.Time(Duration.INFINITE), CompletionRule.Manual)), openEnded = true)
        val session = Session(free)
        session.start(t0)
        session.pause(t0 + 60.seconds)
        session.completeCurrent(t0 + 70.seconds)
        session.resume(t0 + 90.seconds)

        // The step ended during the pause: the new one counts from the resume, without the rest.
        val state = session.state(t0 + 120.seconds)
        assertEquals(SessionState.Status.RUNNING, state.status)
        assertEquals(1, state.phaseIndex)
        assertEquals(30.seconds, state.phaseElapsed)
        assertEquals(2, free.covering(session.events).phases.size)

        session.completeCurrent(t0 + 150.seconds)
        session.finish(t0 + 150.seconds)
        assertEquals(SessionState.Status.FINISHED, session.state(t0 + 150.seconds).status)
        assertEquals(2, free.covering(session.events).phases.size)
    }
}
