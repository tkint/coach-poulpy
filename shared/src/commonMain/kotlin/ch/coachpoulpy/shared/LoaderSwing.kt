package ch.coachpoulpy.shared

import kotlin.math.abs

/**
 * How the loader moves. Poulpy, seen from above, swings from -[DEGREES] to +[DEGREES] and back, over and over, easing in and out
 * of each end (half a turn one way, then half a turn the other way, either side of upright). One there-and-back takes [MILLIS],
 * or [REDUCED_MILLIS] when the phone asks for less motion.
 *
 * His tentacles are loose: the loader is one tentacle picture drawn [TENTACLES] times, a turn of 360 / [TENTACLES] degrees apart,
 * and each bends about its base ([BASE], the share of the picture's height from its top where it meets his head) by
 * [tentacleDegrees], lagging behind the turn like something hanging free, each a little later than the one before.
 * Android and iOS only ask where things are at a given time and draw the picture turned by that many degrees.
 */
object LoaderSwing {
    const val DEGREES = 180f
    const val MILLIS = 3_600L
    const val REDUCED_MILLIS = 12_000L

    const val TENTACLES = 8

    /** How far from the top of the tentacle picture, as a share of its height, a tentacle bends (see design/build-poulpy-pictures.mjs). */
    const val BASE = 0.1828f

    /** The most a tentacle bends, in degrees, when he turns at his fastest. */
    const val BEND_DEGREES = 32f

    /** How much later each tentacle follows than the one before, as a share of a cycle. */
    private const val STAGGER = 0.012f

    /** The turn, from -[DEGREES] to [DEGREES], [elapsedMillis] after the loader appeared; the turn is clockwise when it grows. */
    fun degrees(elapsedMillis: Long, periodMillis: Long = MILLIS): Float = DEGREES * (2f * ease(way(phase(elapsedMillis, periodMillis))) - 1f)

    /**
     * How far tentacle [index] bends about its base: against the turn, by most when he turns fastest, and not at all when he stops
     * at either end. Positive is clockwise.
     */
    fun tentacleDegrees(elapsedMillis: Long, index: Int, periodMillis: Long = MILLIS): Float {
        val phase = phase(elapsedMillis, periodMillis) - index * STAGGER
        val wrapped = phase - kotlin.math.floor(phase)
        val way = way(wrapped)
        // Clockwise on the way there, which the tentacle trails; the other way on the way back.
        val direction = if (wrapped < 0.5f) -1f else 1f
        return direction * BEND_DEGREES * 4f * way * (1f - way)
    }

    private fun phase(elapsedMillis: Long, periodMillis: Long) = (elapsedMillis % periodMillis).toFloat() / periodMillis

    /** 0 to 1 on the way there, 1 to 0 on the way back. */
    private fun way(phase: Float) = 1f - abs(2f * phase - 1f)

    /** Smoothstep: slow at both ends so he seems to settle before he turns back. */
    private fun ease(way: Float) = way * way * (3f - 2f * way)
}
