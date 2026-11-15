package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoaderSwingTest {
    @Test
    fun startsHalfATurnOneWayAndReachesHalfATurnTheOtherWayHalfwayThroughTheCycle() {
        assertEquals(-180f, LoaderSwing.degrees(0), 0.001f)
        assertEquals(180f, LoaderSwing.degrees(LoaderSwing.MILLIS / 2), 0.001f)
    }

    @Test
    fun passesUprightOnTheWayAndComesBackAndRepeatsForever() {
        assertEquals(0f, LoaderSwing.degrees(LoaderSwing.MILLIS / 4), 0.001f)
        assertEquals(-180f, LoaderSwing.degrees(LoaderSwing.MILLIS), 0.001f)
        assertEquals(LoaderSwing.degrees(300), LoaderSwing.degrees(300 + 5 * LoaderSwing.MILLIS), 0.001f)
    }

    @Test
    fun theWayBackMirrorsTheWayThere() {
        assertEquals(LoaderSwing.degrees(500), LoaderSwing.degrees(LoaderSwing.MILLIS - 500), 0.001f)
    }

    @Test
    fun easesIntoEachEndInsteadOfMovingAtASteadyRate() {
        // A steady rate would have gone a quarter of the way after a quarter of the way there, to -90; easing is behind it.
        val quarterOfTheWayThere = LoaderSwing.degrees(LoaderSwing.MILLIS / 8)
        assertTrue(quarterOfTheWayThere < -LoaderSwing.DEGREES / 2, "$quarterOfTheWayThere")
    }

    @Test
    fun neverLeavesTheSwing() {
        for (ms in 0L..LoaderSwing.REDUCED_MILLIS step 97) {
            val degrees = LoaderSwing.degrees(ms, LoaderSwing.REDUCED_MILLIS)
            assertTrue(degrees in -LoaderSwing.DEGREES - 0.001f..LoaderSwing.DEGREES + 0.001f, "$degrees at $ms ms")
        }
    }
}
