package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PoulpyTemperTest {
    private fun PoulpyTemper.tapAndFinish(): TemperReaction? = tap()?.also { finished(it) }

    @Test
    fun theFifthTapInARowTurnsHimAnnoyed() {
        val temper = PoulpyTemper()
        repeat(4) { assertNull(temper.tapAndFinish(), "the happy Poulpy plays his usual jump") }
        assertEquals(TemperLevel.CALM, temper.level)
        assertEquals(TemperReaction.TO_ANNOYED, temper.tapAndFinish())
        assertEquals(TemperLevel.ANNOYED, temper.level)
    }

    @Test
    fun threeMoreTapsTurnHimFuming() {
        val temper = PoulpyTemper()
        repeat(5) { temper.tapAndFinish() }
        assertEquals(TemperReaction.SHAKE, temper.tapAndFinish())
        assertEquals(TemperReaction.SHAKE, temper.tapAndFinish())
        assertEquals(TemperReaction.TO_FUMING, temper.tapAndFinish())
        assertEquals(TemperLevel.FUMING, temper.level)
        assertEquals(TemperReaction.SHUDDER, temper.tapAndFinish())
        assertEquals(TemperLevel.FUMING, temper.level)
    }

    @Test
    fun leftAloneHeImprovesHisMoodAStepAtATime() {
        val temper = PoulpyTemper()
        repeat(8) { temper.tapAndFinish() }
        assertEquals(TemperLevel.FUMING, temper.level)
        assertEquals(TemperReaction.CALM_DOWN, temper.calm()?.also { temper.finished(it) })
        assertEquals(TemperLevel.ANNOYED, temper.level)
        assertEquals(TemperReaction.BACK_TO_NORMAL, temper.calm()?.also { temper.finished(it) })
        assertEquals(TemperLevel.CALM, temper.level)
        assertNull(temper.calm())
    }

    @Test
    fun theCountOfTapsStartsOverWhenHeCalmsDown() {
        val temper = PoulpyTemper()
        repeat(4) { temper.tapAndFinish() }
        assertNull(temper.calm())
        repeat(4) { assertNull(temper.tapAndFinish()) }
        assertEquals(TemperReaction.TO_ANNOYED, temper.tapAndFinish())
        // Annoyed again after calming from fuming: three taps to fume, not what was left over.
        repeat(3) { temper.tapAndFinish() }
        temper.calm()?.also { temper.finished(it) }
        assertEquals(TemperReaction.SHAKE, temper.tapAndFinish())
    }

    @Test
    fun onlyTheHappyPictureCanGetAngry() {
        assertEquals(listOf(PoulpyDrawing.MOOD_HAPPY), PoulpyDrawing.entries.filter { PoulpyTemper.canGetAngry(it) })
    }

    @Test
    fun eachMoveHasItsFramesInASheetAndGoesBackToThePicture() {
        for (reaction in TemperReaction.entries) {
            assertEquals(0, reaction.frame(0))
            assertEquals(reaction.frames - 1, reaction.frame(reaction.millis - 1))
            assertNull(reaction.frame(reaction.millis))
            assertEquals(true, reaction.rows * PoulpyMoves.COLUMNS >= reaction.frames)
        }
        assertEquals(5 to 1, TemperReaction.SHAKE.cellOf(11))
        assertEquals(1_500L, TemperReaction.TO_ANNOYED.millis)
    }
}
