package ch.coachpoulpy.shared

/**
 * Poulpy the champion on the finished screen: he jumps for joy and never rests, round after round with his medal and confetti. It is a loop of
 * [FRAMES] pictures that ends exactly where it starts, so the last picture runs into the first without a seam. Frame by frame from the 3D model,
 * like [PoulpyCheer] (design/build-poulpy-pictures.mjs, into design/poulpy-3d/moves), on the same canvas, so the sizes are [PoulpyCheer]'s;
 * the sheets are laid out the same way, [PoulpyCheer.COLUMNS] across and [PoulpyCheer.SHEET_FRAMES] to a sheet, shown at [PoulpyCheer.FPS] a second.
 */
object PoulpyParty {
    const val FRAMES = 96
    const val SHEETS = FRAMES / PoulpyCheer.SHEET_FRAMES

    /** How long one round of the loop lasts. */
    const val MILLIS = FRAMES * 1_000L / PoulpyCheer.FPS

    /** The name of sheet [index], from 0. */
    fun sheet(index: Int): String {
        require(index in 0 until SHEETS) { "no sheet $index" }
        return "pose_champion_party_${index + 1}"
    }

    /** Which of the [FRAMES] pictures to show [elapsedMillis] into the party. There is always one: when the last has been shown, the first comes again. */
    fun frame(elapsedMillis: Long): Int = ((elapsedMillis.coerceAtLeast(0) * PoulpyCheer.FPS / 1_000) % FRAMES).toInt()

    /** Which sheet [frame] is in. */
    fun sheetOf(frame: Int): Int = frame / PoulpyCheer.SHEET_FRAMES
}
