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
        // Slow walking takes short steps, so far fewer of them than a brisk walk.
        assertEquals(48.3, StepEstimate.cadence(1.0), 0.1)
        assertEquals(105.8, StepEstimate.cadence(4.0), 0.1)
        assertEquals(127.5, StepEstimate.cadence(7.0), 0.1)
        assertEquals(170.0, StepEstimate.cadence(8.0))
        assertEquals(180.0, StepEstimate.cadence(12.0))
        // Halfway between walking (129.9) and running (168.75) at 7.5 km/h.
        assertEquals(149.3, StepEstimate.cadence(7.5), 0.1)
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
