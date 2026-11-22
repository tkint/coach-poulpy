package ch.coachpoulpy.shared

/** The mood he is in because of the taps: his own happy one, annoyed or fuming. */
enum class TemperLevel(val drawing: PoulpyDrawing) {
    CALM(PoulpyDrawing.MOOD_HAPPY),
    ANNOYED(PoulpyDrawing.MOOD_ANNOYED),

    /** The fuming pose, with his fumes behind him, as the moves are drawn. */
    FUMING(PoulpyDrawing.FUMING),
}

/** A move of his, baked as the sheet [sheet] of [frames] pictures, after which he is in mood [after]. */
enum class TemperReaction(val sheet: String, val frames: Int, val after: TemperLevel) {
    TO_ANNOYED("mood_move_to_annoyed", 36, TemperLevel.ANNOYED),
    TO_FUMING("mood_move_to_fuming", 36, TemperLevel.FUMING),
    SHAKE("mood_move_shake", 34, TemperLevel.ANNOYED),
    SHUDDER("mood_move_shudder", 46, TemperLevel.FUMING),
    CALM_DOWN("mood_move_calm_down", 36, TemperLevel.ANNOYED),
    BACK_TO_NORMAL("mood_move_back_to_normal", 41, TemperLevel.CALM),
    ;

    /** How long the move lasts. */
    val millis: Long get() = frames * 1_000L / PoulpyMoves.MOOD_FPS

    /** Which of the [frames] pictures to show [elapsedMillis] into the move, or null once it is over: he is back to his picture. */
    fun frame(elapsedMillis: Long): Int? = if (elapsedMillis < 0 || elapsedMillis >= millis) null else (elapsedMillis * PoulpyMoves.MOOD_FPS / 1_000).toInt().coerceAtMost(frames - 1)

    /** The column and the row of [frame] in its sheet. */
    fun cellOf(frame: Int): Pair<Int, Int> = frame % PoulpyMoves.COLUMNS to frame / PoulpyMoves.COLUMNS

    /** How many rows the sheet has. */
    val rows: Int get() = (frames + PoulpyMoves.COLUMNS - 1) / PoulpyMoves.COLUMNS
}

/**
 * Poulpy losing patience on Home, and calming down (the moves validated on the workshop's "Poulpy moves" pages and the Home mockup "Planned, tap Poulpy
 * until he is angry"). Only the happy Poulpy gets angry: tapped again and again he turns annoyed, then fuming, and left alone he improves his mood a step
 * at a time. Pain or rehab never comes in here, because Poulpy is then patched up and not happy.
 *
 * [PoulpyTemper] decides what a tap does and which mood he is in; [TemperReaction] says which baked sheet plays and for how long
 * (design/build-poulpy-pictures.mjs, into design/poulpy-3d/moves, on the same canvas as [PoulpyMoves], so the sizes are [PoulpyMoves]'s).
 */
class PoulpyTemper(
    private val tapsToAnnoyed: Int,
    private val tapsToFuming: Int,
) {
    /** The usual temper: [TAPS_TO_ANNOYED] taps, then [TAPS_TO_FUMING] more. */
    constructor() : this(TAPS_TO_ANNOYED, TAPS_TO_FUMING)

    var level: TemperLevel = TemperLevel.CALM
        private set
    private var taps = 0

    /**
     * Someone tapped him: what he plays for it, or null when he plays his usual move for his picture (the happy one's jumps). The tap that makes
     * him change mood plays the turn; the taps before it, the move of his mood. Call [finished] when it is over.
     */
    fun tap(): TemperReaction? {
        taps++
        return when (level) {
            TemperLevel.CALM -> if (taps >= tapsToAnnoyed) TemperReaction.TO_ANNOYED else null
            TemperLevel.ANNOYED -> if (taps >= tapsToFuming) TemperReaction.TO_FUMING else TemperReaction.SHAKE
            TemperLevel.FUMING -> TemperReaction.SHUDDER
        }
    }

    /** The move [reaction] is over, and so is the count of taps if it changed his mood. */
    fun finished(reaction: TemperReaction) {
        if (reaction.after != level) taps = 0
        level = reaction.after
    }

    /**
     * He was left alone for [CALM_MILLIS]: the move that improves his mood by a step, or null when he is calm already (the count of taps starts over).
     */
    fun calm(): TemperReaction? = when (level) {
        TemperLevel.FUMING -> TemperReaction.CALM_DOWN
        TemperLevel.ANNOYED -> TemperReaction.BACK_TO_NORMAL
        TemperLevel.CALM -> { taps = 0; null }
    }

    companion object {
        /** Taps in a row that make the happy Poulpy turn annoyed. */
        const val TAPS_TO_ANNOYED = 5

        /** Taps in a row that make the annoyed Poulpy turn fuming. */
        const val TAPS_TO_FUMING = 3

        /** How long he is left alone before he improves his mood by a step. */
        const val CALM_MILLIS = 5_000L

        /** Whether tapping Poulpy as [drawing] can make him angry: only his happy picture can. */
        fun canGetAngry(drawing: PoulpyDrawing): Boolean = drawing == PoulpyDrawing.MOOD_HAPPY
    }
}
