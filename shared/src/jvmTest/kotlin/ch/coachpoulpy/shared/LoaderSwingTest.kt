package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoaderSwingTest {
    @Test
    fun startsUprightAndReachesAFortyFiveDegreeTurnHalfwayThroughTheCycle() {
        assertEquals(0f, LoaderSwing.degrees(0))
        assertEquals(45f, LoaderSwing.degrees(LoaderSwing.MILLIS / 2), 0.001f)
    }

    @Test
    fun turnsBackToUprightAndRepeatsForever() {
        assertEquals(0f, LoaderSwing.degrees(LoaderSwing.MILLIS), 0.001f)
        assertEquals(LoaderSwing.degrees(300), LoaderSwing.degrees(300 + 5 * LoaderSwing.MILLIS), 0.001f)
    }

    @Test
    fun theWayBackMirrorsTheWayThere() {
        assertEquals(LoaderSwing.degrees(500), LoaderSwing.degrees(LoaderSwing.MILLIS - 500), 0.001f)
    }

    @Test
    fun easesIntoEachEndInsteadOfMovingAtASteadyRate() {
        // A steady rate would have turned a quarter of the way after a quarter of the way there; easing is behind it.
        val quarterOfTheWayThere = LoaderSwing.degrees(LoaderSwing.MILLIS / 8)
        assertTrue(quarterOfTheWayThere < LoaderSwing.DEGREES / 4, "$quarterOfTheWayThere")
    }

    @Test
    fun neverLeavesTheSwing() {
        for (ms in 0L..LoaderSwing.REDUCED_MILLIS step 97) {
            val degrees = LoaderSwing.degrees(ms, LoaderSwing.REDUCED_MILLIS)
            assertTrue(degrees in 0f..LoaderSwing.DEGREES + 0.001f, "$degrees at $ms ms")
        }
    }
}
