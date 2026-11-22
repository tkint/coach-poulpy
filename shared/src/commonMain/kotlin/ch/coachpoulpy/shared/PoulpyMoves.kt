package ch.coachpoulpy.shared

/**
 * What happens when Home's Poulpy is tapped: he plays a short move for his picture, drawn frame by frame from the 3D model
 * (design/build-poulpy-pictures.mjs, into design/poulpy-3d/moves). Happy and proud jump, leaning to one side or the other at random, and happy sometimes
 * spins in a high jump; the other moods have no move yet. A sheet holds the [FRAMES] pictures of a move, [COLUMNS] across, shown at [FPS] pictures a second (a mood move at [MOOD_FPS]).
 */
object PoulpyMoves {
    const val FRAMES = 60
    const val FPS = 48

    /** The mood moves have fewer pictures a second: their sheets are taller than a tap's would be at [FPS]. */
    const val MOOD_FPS = 24
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

    /** A move: his little jump leaning to one side (as the viewer sees him), or the high jump with a full turn. */
    enum class Move(val assetName: String) { LEFT("left"), RIGHT("right"), SPIN("spin") }

    /** The moves a tap on Poulpy as [drawing] picks from at random: empty when that picture has none. Happy also has the spin. */
    fun movesOf(drawing: PoulpyDrawing): List<Move> = when (drawing) {
        PoulpyDrawing.MOOD_HAPPY -> listOf(Move.LEFT, Move.RIGHT, Move.SPIN)
        PoulpyDrawing.MOOD_PROUD -> listOf(Move.LEFT, Move.RIGHT)
        else -> emptyList()
    }

    /** The name of the sheet of [drawing]'s [move], or null when that picture does not have it. */
    fun sheet(drawing: PoulpyDrawing, move: Move): String? = if (move in movesOf(drawing)) "${drawing.assetName}_${move.assetName}" else null

    /** Whether a tap on Poulpy as [drawing] plays a move. */
    fun moves(drawing: PoulpyDrawing): Boolean = movesOf(drawing).isNotEmpty()

    /** Which of the [FRAMES] pictures to show [elapsedMillis] into a move, or null once it is over: he is back to his picture. */
    fun frame(elapsedMillis: Long): Int? = if (elapsedMillis < 0 || elapsedMillis >= MILLIS) null else (elapsedMillis * FPS / 1_000).toInt().coerceAtMost(FRAMES - 1)
}
