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
    fun comesBackToWhereItStartedAtTheEndOfTheCycle() {
        assertEquals(LoaderSwing.degrees(0), LoaderSwing.degrees(LoaderSwing.MILLIS), 0.001f)
    }

    @Test
    fun staysWithinHalfATurnEitherWay() {
        for (t in 0L until LoaderSwing.MILLIS step 50) assertTrue(LoaderSwing.degrees(t) in -180f..180f)
    }

    @Test
    fun takesTwelveSecondsWhenTheyAreAskedForLessMotion() {
        assertEquals(LoaderSwing.degrees(0), LoaderSwing.degrees(LoaderSwing.REDUCED_MILLIS, LoaderSwing.REDUCED_MILLIS), 0.001f)
        assertEquals(180f, LoaderSwing.degrees(LoaderSwing.REDUCED_MILLIS / 2, LoaderSwing.REDUCED_MILLIS), 0.001f)
    }
}

class LoaderClockwiseTest {
    @Test
    fun turnsClockwiseOnceEveryPeriod() {
        assertEquals(0f, LoaderSwing.clockwiseDegrees(0))
        assertEquals(90f, LoaderSwing.clockwiseDegrees(LoaderSwing.MILLIS / 4), 0.01f)
        assertEquals(0f, LoaderSwing.clockwiseDegrees(LoaderSwing.MILLIS))
    }
}

class LoaderFramesTest {
    @Test
    fun showsEachFrameInTurnAndStartsAgainAfterTheLast() {
        assertEquals(0, LoaderSwing.frame(0))
        assertEquals(LoaderSwing.FRAMES / 2, LoaderSwing.frame(LoaderSwing.MILLIS / 2))
        assertEquals(LoaderSwing.FRAMES - 1, LoaderSwing.frame(LoaderSwing.MILLIS - 1))
        assertEquals(0, LoaderSwing.frame(LoaderSwing.MILLIS))
    }

    @Test
    fun slowsDownWithTheCycle() {
        assertEquals(14, LoaderSwing.frame(LoaderSwing.MILLIS, LoaderSwing.REDUCED_MILLIS))
        assertEquals(LoaderSwing.FRAMES / 2, LoaderSwing.frame(LoaderSwing.REDUCED_MILLIS / 2, LoaderSwing.REDUCED_MILLIS))
    }
}
