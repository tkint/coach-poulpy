package ch.coachpoulpy.shared

/**
 * What happens when Home's Poulpy is tapped: he plays a short move for his picture, drawn frame by frame from the 3D model
 * (design/build-poulpy-pictures.mjs, into design/poulpy-3d/moves). Happy and proud jump, leaning to one side or the other at random;
 * the other moods have no move yet. A sheet holds the [FRAMES] pictures of a move, [COLUMNS] across, shown at [FPS] pictures a second.
 */
object PoulpyMoves {
    const val FRAMES = 30
    const val FPS = 24
    const val COLUMNS = 6

    /**
     * How big a picture of a move is next to his still picture, in pixels of the sheets: a frame is [FRAME_WIDTH] by [FRAME_HEIGHT], and his
     * still picture is [PICTURE_WIDTH] by [PICTURE_HEIGHT] in it, in the same place, so a frame is drawn that much bigger than the still, centred on it.
     */
    const val FRAME_WIDTH = 355
    const val FRAME_HEIGHT = 336
    const val PICTURE_WIDTH = 211
    const val PICTURE_HEIGHT = 192

    /** How long a move lasts. */
    const val MILLIS = FRAMES * 1_000L / FPS

    /** Which way he leans, as the viewer sees him. */
    enum class Side(val assetName: String) { LEFT("left"), RIGHT("right") }

    /** The name of the sheet of [drawing]'s move leaning to [side], or null when that picture has no move. */
    fun sheet(drawing: PoulpyDrawing, side: Side): String? = if (moves(drawing)) "${drawing.assetName}_${side.assetName}" else null

    /** Whether a tap on Poulpy as [drawing] plays a move. */
    fun moves(drawing: PoulpyDrawing): Boolean = drawing in MOVING

    /** Which of the [FRAMES] pictures to show [elapsedMillis] into a move, or null once it is over: he is back to his picture. */
    fun frame(elapsedMillis: Long): Int? = if (elapsedMillis < 0 || elapsedMillis >= MILLIS) null else (elapsedMillis * FPS / 1_000).toInt().coerceAtMost(FRAMES - 1)

    private val MOVING = setOf(PoulpyDrawing.MOOD_HAPPY, PoulpyDrawing.MOOD_PROUD)
}
