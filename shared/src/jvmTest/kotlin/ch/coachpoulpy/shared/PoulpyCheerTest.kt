package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PoulpyCheerTest {
    @Test
    fun onlyTheChampionCelebrates() {
        assertEquals(listOf(PoulpyDrawing.CHAMPION), PoulpyDrawing.entries.filter { PoulpyCheer.celebrates(it) })
    }

    @Test
    fun theSheetsHoldEveryFrame() {
        assertEquals(PoulpyCheer.FRAMES, PoulpyCheer.SHEETS * PoulpyCheer.SHEET_FRAMES)
        assertEquals(listOf("pose_champion_cheer_1", "pose_champion_cheer_2", "pose_champion_cheer_3"), (0 until PoulpyCheer.SHEETS).map { PoulpyCheer.sheet(it) })
        assertFailsWith<IllegalArgumentException> { PoulpyCheer.sheet(PoulpyCheer.SHEETS) }
    }

    @Test
    fun findsEachFrameInItsSheet() {
        assertEquals(0 to (0 to 0), PoulpyCheer.sheetOf(0) to PoulpyCheer.cellOf(0))
        assertEquals(0 to (5 to 0), PoulpyCheer.sheetOf(5) to PoulpyCheer.cellOf(5))
        assertEquals(0 to (5 to 3), PoulpyCheer.sheetOf(23) to PoulpyCheer.cellOf(23))
        assertEquals(1 to (0 to 0), PoulpyCheer.sheetOf(24) to PoulpyCheer.cellOf(24))
        assertEquals(2 to (5 to 3), PoulpyCheer.sheetOf(PoulpyCheer.FRAMES - 1) to PoulpyCheer.cellOf(PoulpyCheer.FRAMES - 1))
    }

    @Test
    fun showsEachFrameInTurnThenGoesBackToThePicture() {
        assertEquals(0, PoulpyCheer.frame(0))
        assertEquals(1, PoulpyCheer.frame(1_000L / PoulpyCheer.FPS + 1))
        assertEquals(PoulpyCheer.FRAMES - 1, PoulpyCheer.frame(PoulpyCheer.MILLIS - 1))
        assertNull(PoulpyCheer.frame(PoulpyCheer.MILLIS))
        assertNull(PoulpyCheer.frame(-1))
        assertEquals(3_600L, PoulpyCheer.MILLIS)
    }
}
