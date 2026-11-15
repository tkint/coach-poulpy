package ch.coachpoulpy.shared

import kotlin.math.abs

/**
 * How the loader moves: Poulpy, seen from above, turns [DEGREES] one way and then the same back the other way, over and over,
 * easing in and out of each end. One there-and-back takes [MILLIS], or [REDUCED_MILLIS] when the phone asks for less motion.
 * Android and iOS only ask where he is at a given time and draw the loader picture turned by that many degrees.
 */
object LoaderSwing {
    const val DEGREES = 45f
    const val MILLIS = 2_400L
    const val REDUCED_MILLIS = 10_000L

    /** The turn, from 0 to [DEGREES], [elapsedMillis] after the loader appeared; it starts upright and turns clockwise first. */
    fun degrees(elapsedMillis: Long, periodMillis: Long = MILLIS): Float {
        val phase = (elapsedMillis % periodMillis).toFloat() / periodMillis
        // 0 to 1 on the way there, 1 to 0 on the way back.
        val way = 1f - abs(2f * phase - 1f)
        // Smoothstep: slow at both ends so he seems to settle before he turns back.
        return DEGREES * way * way * (3f - 2f * way)
    }
}
