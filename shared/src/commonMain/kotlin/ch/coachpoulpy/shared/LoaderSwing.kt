package ch.coachpoulpy.shared

import kotlin.math.abs

/**
 * How the loader moves: Poulpy, seen from above, swings from -[DEGREES] to +[DEGREES] and back, over and over, easing in and out
 * of each end (so half a turn one way, then half a turn the other way, either side of upright). One there-and-back takes
 * [MILLIS], or [REDUCED_MILLIS] when the phone asks for less motion. Android and iOS only ask where he is at a given time and
 * draw the loader picture turned by that many degrees.
 */
object LoaderSwing {
    const val DEGREES = 180f
    const val MILLIS = 3_600L
    const val REDUCED_MILLIS = 12_000L

    /** The turn, from -[DEGREES] to [DEGREES], [elapsedMillis] after the loader appeared; the turn is clockwise when it grows. */
    fun degrees(elapsedMillis: Long, periodMillis: Long = MILLIS): Float {
        val phase = (elapsedMillis % periodMillis).toFloat() / periodMillis
        // 0 to 1 on the way there, 1 to 0 on the way back.
        val way = 1f - abs(2f * phase - 1f)
        // Smoothstep: slow at both ends so he seems to settle before he turns back.
        return DEGREES * (2f * way * way * (3f - 2f * way) - 1f)
    }
}
