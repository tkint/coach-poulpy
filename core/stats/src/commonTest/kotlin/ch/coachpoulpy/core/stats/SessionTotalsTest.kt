package ch.coachpoulpy.core.stats

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SessionTotalsTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    private fun work(index: Int) = Phase.Work(
        StepId("s"), ExerciseSlug("push-up"), index, 1, Target.Reps(10), Side.BOTH, CompletionRule.Manual, duration = null,
    )

    private val timeline = Timeline(listOf(work(0), Phase.Rest(45.seconds), work(1), Phase.Rest(45.seconds), work(2)))

    @Test
    fun activeTimeLeavesOutPauses() {
        val events = listOf(
            SessionEvent.Started(t0),
            SessionEvent.Paused(t0 + 5.minutes),
            SessionEvent.Resumed(t0 + 8.minutes),
            SessionEvent.Finished(t0 + 20.minutes),
        )
        assertEquals(17.minutes, SessionTotals.activeTime(events))
    }

    @Test
    fun finishingWhilePausedStopsTheClockAtThePause() {
        val events = listOf(SessionEvent.Started(t0), SessionEvent.Paused(t0 + 10.minutes), SessionEvent.Finished(t0 + 30.minutes))
        assertEquals(10.minutes, SessionTotals.activeTime(events))
    }

    @Test
    fun sessionNeverFinishedEndsAtItsLastEvent() {
        val events = listOf(SessionEvent.Started(t0), SessionEvent.PhaseCompleted(t0 + 2.minutes, 0))
        assertEquals(2.minutes, SessionTotals.activeTime(events))
    }

    @Test
    fun countsWorkPhasesOnlyNotRests() {
        val events = listOf(
            SessionEvent.Started(t0),
            SessionEvent.PhaseCompleted(t0 + 1.minutes, 0),
            SessionEvent.PhaseCompleted(t0 + 2.minutes, 1),
            SessionEvent.PhaseSkipped(t0 + 2.minutes, 2),
            SessionEvent.PhaseSkipped(t0 + 2.minutes, 3),
            SessionEvent.PhaseCompleted(t0 + 3.minutes, 4),
            SessionEvent.Finished(t0 + 3.minutes),
        )
        val totals = SessionTotals.of(events, timeline)
        assertEquals(2, totals.setsDone)
        assertEquals(1, totals.setsSkipped)
        assertEquals(3, totals.setsPlanned)
        assertEquals(3.minutes, totals.active)
    }

    @Test
    fun withoutThePlanSetsAreUnknown() {
        val totals = SessionTotals.of(listOf(SessionEvent.Started(t0), SessionEvent.Finished(t0 + 1.minutes)), timeline = null)
        assertNull(totals.setsDone)
        assertNull(totals.setsPlanned)
    }

    @Test
    fun sessionClosedBeforeItStartedIsEmpty() {
        val totals = SessionTotals.of(listOf(SessionEvent.Finished(t0)), timeline)
        assertEquals(Duration.ZERO, totals.active)
        assertTrue(totals.isEmpty)
    }
}
