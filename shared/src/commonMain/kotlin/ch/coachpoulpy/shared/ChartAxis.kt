package ch.coachpoulpy.shared

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

/** The grid lines behind a line chart: round values, so a reader can say "about 73" at a glance. */
object ChartAxis {
    /**
     * Round tick values covering [low]..[high], a step of 1, 2 or 5 times a power of ten with about [target] lines.
     * A flat series (one entry, or every entry equal) is widened to [minSpan] around its middle so the line is not squashed.
     */
    fun ticks(low: Double, high: Double, minSpan: Double, target: Int = 4): List<Double> {
        var from = low
        var to = high
        if (to - from < minSpan) {
            val middle = (from + to) / 2
            from = middle - minSpan / 2
            to = middle + minSpan / 2
        }
        val step = niceStep((to - from) / (target - 1))
        val first = floor(from / step + 1e-9)
        val last = ceil(to / step - 1e-9)
        return (0..(last - first).toInt()).map { clean((first + it) * step) }
    }

    private fun niceStep(raw: Double): Double {
        val power = 10.0.pow(floor(log10(raw)))
        val fraction = raw / power
        return power * when {
            fraction <= 1.0 -> 1.0
            fraction <= 2.0 -> 2.0
            fraction <= 5.0 -> 5.0
            else -> 10.0
        }
    }

    private fun clean(value: Double) = round(value * 1_000_000) / 1_000_000
}
