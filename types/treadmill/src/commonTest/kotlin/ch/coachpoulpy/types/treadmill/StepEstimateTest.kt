package ch.coachpoulpy.types.treadmill

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class StepEstimateTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun cadenceFollowsWalkingThenRunning() {
        assertEquals(0.0, StepEstimate.cadence(0.0))
        assertEquals(112.5, StepEstimate.cadence(5.0))
        assertEquals(170.0, StepEstimate.cadence(8.0))
        assertEquals(180.0, StepEstimate.cadence(12.0))
        // Halfway between walking (128.75) and running (168.75) at 7.5 km/h.
        assertEquals(148.75, StepEstimate.cadence(7.5))
    }

    @Test
    fun counterAddsUpOverTimeAndIgnoresLongGaps() {
        val counter = StepCounter()
        for (s in 0..60) counter.add(t0 + s.seconds, 12.0) // one minute at 180 steps a minute
        assertEquals(180, counter.steps)
        counter.add(t0 + 600.seconds, 12.0) // nine minutes without readings count as 3 s
        assertEquals(189, counter.steps)
    }
}
