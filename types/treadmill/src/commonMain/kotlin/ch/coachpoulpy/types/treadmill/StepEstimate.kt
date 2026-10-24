package ch.coachpoulpy.types.treadmill

import ch.coachpoulpy.core.engine.SessionEvent
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Steps estimated from the belt speed, since treadmills do not report them. Walking
 * divides the distance by a step length that grows with speed, from about 35 cm at
 * 1 km/h to 63 cm at 4 km/h and 90 cm at 7 km/h: about 48 steps a minute at 1 km/h,
 * 106 at 4 km/h and 127 at 7 km/h. Running uses typical cadences, from 170 at 8 km/h
 * to 190 at 16 km/h. Leg length and stride vary from person to person, so the app
 * always shows the result as an estimate.
 */
object StepEstimate {
    /** Steps per minute at this speed. */
    fun cadence(kmh: Double): Double {
        val walking = kmh * 1000 / 60 / stepLength(kmh)
        val running = (150 + 2.5 * kmh).coerceAtMost(200.0)
        return when {
            kmh < 0.5 -> 0.0
            kmh <= 7.0 -> walking
            kmh >= 8.0 -> running
            // Between 7 and 8 km/h people switch from walking to running: blend the two.
            else -> walking + (running - walking) * (kmh - 7.0)
        }
    }

    /** Walking step length in metres: short shuffling steps when slow, longer strides when brisk. */
    private fun stepLength(kmh: Double) = 0.25 + 0.095 * kmh

    /** Steps over a recorded session, from its speed samples. */
    fun steps(events: List<SessionEvent>): Int? {
        val counter = StepCounter()
        var sawSpeed = false
        events.filterIsInstance<SessionEvent.Sample>().forEach { sample ->
            sample.metrics.speedKmh?.let { sawSpeed = true; counter.add(sample.at, it) }
        }
        return if (sawSpeed) counter.steps else null
    }
}

/** Adds up estimated steps as readings arrive, about once a second. */
class StepCounter {
    private var total = 0.0
    private var last: Pair<Instant, Double>? = null

    val steps: Int get() = total.roundToInt()

    fun add(at: Instant, kmh: Double) {
        last?.let { (previousAt, previousKmh) ->
            // Readings come about once a second; a longer gap is a pause or a lost connection, not running.
            val elapsed = (at - previousAt).coerceIn(0.seconds, MAX_GAP)
            total += StepEstimate.cadence(previousKmh) * elapsed.inWholeMilliseconds / 60_000.0
        }
        last = at to kmh
    }

    fun reset() {
        total = 0.0
        last = null
    }

    private companion object {
        val MAX_GAP = 3.seconds
    }
}
