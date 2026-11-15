package ch.coachpoulpy.shared

import kotlin.math.abs

/**
 * How the loader moves. Poulpy, seen from underneath, is the 3D model's outline with loose tentacles, drawn frame by frame
 * (design/build-poulpy-pictures.mjs): [FRAMES] pictures of one cycle, each showing how his tentacles hang at that moment relative
 * to his body. The apps show the picture of the moment, [frame], and turn it by [degrees] (or [clockwiseDegrees]), so his turning
 * stays smooth. One cycle takes [MILLIS], or [REDUCED_MILLIS] when the phone asks for less motion.
 *
 * There are two versions. He swings from -[DEGREES] to +[DEGREES] and back, easing in and out of each end (half a turn one way,
 * then half a turn the other way), or he only turns clockwise, one full turn per cycle. Each has its own frames.
 */
object LoaderSwing {
    const val DEGREES = 180f
    const val MILLIS = 3_600L
    const val REDUCED_MILLIS = 12_000L

    /** How many pictures one cycle is, in each version. */
    const val FRAMES = 48

    /** The swing, from -[DEGREES] to [DEGREES], [elapsedMillis] after the loader appeared; the turn is clockwise when it grows. */
    fun degrees(elapsedMillis: Long, periodMillis: Long = MILLIS): Float = DEGREES * (2f * ease(way(phase(elapsedMillis, periodMillis))) - 1f)

    /** The clockwise-only version: 0 to 360 degrees, steadily, once every [periodMillis]. */
    fun clockwiseDegrees(elapsedMillis: Long, periodMillis: Long = MILLIS): Float = 360f * phase(elapsedMillis, periodMillis)

    /** Which of the [FRAMES] pictures shows how his tentacles hang [elapsedMillis] after the loader appeared, from 0. */
    fun frame(elapsedMillis: Long, periodMillis: Long = MILLIS): Int = (phase(elapsedMillis, periodMillis) * FRAMES).toInt().coerceAtMost(FRAMES - 1)

    private fun phase(elapsedMillis: Long, periodMillis: Long) = (elapsedMillis % periodMillis).toFloat() / periodMillis

    /** 0 to 1 on the way there, 1 to 0 on the way back. */
    private fun way(phase: Float) = 1f - abs(2f * phase - 1f)

    /** Smoothstep: slow at both ends so he seems to settle before he turns back. */
    private fun ease(way: Float) = way * way * (3f - 2f * way)
}
