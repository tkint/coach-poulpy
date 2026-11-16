package ch.coachpoulpy.shared

/**
 * Poulpy the champion on the end-of-session screen: he jumps for joy three times with his medal and a burst of confetti each time, then stands
 * on his still picture. Frame by frame from the 3D model, like [PoulpyMoves] (design/build-poulpy-pictures.mjs, into design/poulpy-3d/moves).
 * The [FRAMES] pictures are in [SHEETS] sheets of [SHEET_FRAMES], [COLUMNS] across, shown at [FPS] pictures a second.
 */
object PoulpyCheer {
    const val FRAMES = 72
    const val FPS = 20
    const val COLUMNS = 6
    const val SHEET_FRAMES = 24
    const val SHEETS = FRAMES / SHEET_FRAMES

    /**
     * A frame is [FRAME_WIDTH] by [FRAME_HEIGHT] pixels, and his still picture is [PICTURE_WIDTH] by [PICTURE_HEIGHT] in the middle of it, so a frame is drawn
     * that much bigger than the still, centred on it, to leave room for the confetti.
     */
    const val FRAME_WIDTH = 497
    const val FRAME_HEIGHT = 472
    const val PICTURE_WIDTH = 277
    const val PICTURE_HEIGHT = 252

    /** Whether Poulpy as [drawing] celebrates when a screen opens: only the champion does. */
    fun celebrates(drawing: PoulpyDrawing): Boolean = drawing == PoulpyDrawing.CHAMPION

    /** How long the celebration lasts. */
    const val MILLIS = FRAMES * 1_000L / FPS

    /** The name of sheet [index], from 0: the champion's frames [index] * [SHEET_FRAMES] up to the next sheet's. */
    fun sheet(index: Int): String {
        require(index in 0 until SHEETS) { "no sheet $index" }
        return "pose_champion_cheer_${index + 1}"
    }

    /** Which of the [FRAMES] pictures to show [elapsedMillis] into the celebration, or null once it is over: he is back to his picture. */
    fun frame(elapsedMillis: Long): Int? = if (elapsedMillis < 0 || elapsedMillis >= MILLIS) null else (elapsedMillis * FPS / 1_000).toInt().coerceAtMost(FRAMES - 1)

    /** Which sheet [frame] is in. */
    fun sheetOf(frame: Int): Int = frame / SHEET_FRAMES

    /** Where [frame] is in its sheet: its column and its row. */
    fun cellOf(frame: Int): Pair<Int, Int> = (frame % SHEET_FRAMES).let { it % COLUMNS to it / COLUMNS }
}
