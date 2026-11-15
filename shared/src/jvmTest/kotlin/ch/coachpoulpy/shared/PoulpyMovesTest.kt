package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PoulpyMovesTest {
    @Test
    fun onlyHappyAndProudMove() {
        val moving = PoulpyDrawing.entries.filter { PoulpyMoves.moves(it) }
        assertEquals(listOf(PoulpyDrawing.MOOD_PROUD, PoulpyDrawing.MOOD_HAPPY), moving)
        assertFalse(PoulpyMoves.moves(PoulpyDrawing.MOOD_GRUMPY))
    }

    @Test
    fun namesTheSheetOfEachSide() {
        assertEquals("mood_happy_left", PoulpyMoves.sheet(PoulpyDrawing.MOOD_HAPPY, PoulpyMoves.Side.LEFT))
        assertEquals("mood_proud_right", PoulpyMoves.sheet(PoulpyDrawing.MOOD_PROUD, PoulpyMoves.Side.RIGHT))
        assertNull(PoulpyMoves.sheet(PoulpyDrawing.MOOD_FUMING, PoulpyMoves.Side.LEFT))
    }

    @Test
    fun showsEachFrameInTurnThenGoesBackToThePicture() {
        assertEquals(0, PoulpyMoves.frame(0))
        assertEquals(1, PoulpyMoves.frame(1_000L / PoulpyMoves.FPS + 1))
        assertEquals(PoulpyMoves.FRAMES - 1, PoulpyMoves.frame(PoulpyMoves.MILLIS - 1))
        assertNull(PoulpyMoves.frame(PoulpyMoves.MILLIS))
        assertNull(PoulpyMoves.frame(-1))
        assertTrue(PoulpyMoves.MILLIS in 1_000..1_500)
    }
}
