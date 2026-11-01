package ch.coachpoulpy.shared

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** How a walk or a run block shows its distance. */
enum class DistanceUnit(val meters: Double, val symbol: String) {
    M(1.0, "m"),
    KM(1000.0, "km"),
    MI(1609.344, "mi"),
    ;

    companion object {
        /**
         * The unit a distance was picked in, read from its value: a whole number of miles or of kilometres, which a
         * saved target is the only trace of ("1 mile" is stored as 1609 m). Null for anything else, such as 400 m,
         * which stays in metres whatever the unit system.
         */
        fun signature(meters: Double): DistanceUnit? = when {
            meters < 1000 -> null
            abs(meters - (meters / 1000).roundToInt() * 1000.0) <= 1.0 -> KM
            abs(meters - (meters / MI.meters).roundToInt() * MI.meters) <= 1.0 -> MI
            else -> null
        }
    }
}

/** How a walk or a run block shows its pace: time per kilometre or mile, or a speed as treadmills show it. */
enum class PaceUnit(val symbol: String, val isSpeed: Boolean, internal val meters: Double) {
    PER_KM("/km", false, 1000.0),
    PER_MI("/mi", false, 1609.344),
    KMH("km/h", true, 1000.0),
    MPH("mph", true, 1609.344),
}

/** A quick pick in a value sheet: [text] goes into the field as if typed. */
data class ValuePick(val label: String, val text: String)

/**
 * Reads and writes the values typed for a walk or a run: "1:05:00", "45", "5:30", "10.5" or "10,5".
 * Durations are seconds, distances metres and paces km/h, whatever the unit shown.
 */
object TargetInput {
    /** "20" is twenty minutes, "5:30" five and a half minutes, "1:05:00" an hour and five minutes. */
    fun parseDuration(text: String): Int? {
        val parts = text.trim().split(':')
        if (parts.any { it.isBlank() || it.any { c -> !c.isDigit() } }) return null
        val numbers = parts.map { it.toInt() }
        val seconds = when (numbers.size) {
            1 -> numbers[0] * 60
            2 -> if (numbers[1] < 60) numbers[0] * 60 + numbers[1] else return null
            3 -> if (numbers[1] < 60 && numbers[2] < 60) numbers[0] * 3600 + numbers[1] * 60 + numbers[2] else return null
            else -> return null
        }
        return seconds.takeIf { it > 0 }
    }

    /** "20:00", or "1:05:00" from an hour on. */
    fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = seconds % 3600 / 60
        val s = seconds % 60
        return if (h > 0) "$h:${two(m)}:${two(s)}" else "$m:${two(s)}"
    }

    fun parseDistance(text: String, unit: DistanceUnit): Double? =
        number(text)?.takeIf { it > 0 }?.let { it * unit.meters }

    /** One or two decimals, none for metres: "5.0", "21.1", "3.11", "400". */
    fun formatDistance(meters: Double, unit: DistanceUnit): String {
        if (unit == DistanceUnit.M) return meters.roundToInt().toString()
        val text = decimals(meters / unit.meters, 2)
        return if ('.' in text) text else "$text.0"
    }

    /** Time per unit as "5:30" (or "5" for five minutes), or a speed as "10.5"; returns km/h. */
    fun parsePace(text: String, unit: PaceUnit): Double? {
        if (unit.isSpeed) return number(text)?.takeIf { it > 0 }?.let { it * unit.meters / 1000.0 }
        val parts = text.trim().split(':')
        if (parts.size > 2 || parts.any { it.isBlank() || it.any { c -> !c.isDigit() } }) return null
        val seconds = parts[0].toInt() * 60 + (parts.getOrNull(1)?.toInt()?.takeIf { it < 60 } ?: if (parts.size == 2) return null else 0)
        if (seconds <= 0) return null
        return unit.meters / 1000.0 / (seconds / 3600.0)
    }

    /** "6:00" per km or mile, or "10.0" as a speed. */
    fun formatPace(kmh: Double, unit: PaceUnit): String {
        if (unit.isSpeed) return decimals(kmh * 1000.0 / unit.meters, 1, keepZero = true)
        val seconds = (unit.meters / 1000.0 / kmh * 3600).roundToInt()
        return "${seconds / 60}:${two(seconds % 60)}"
    }

    fun distanceLabel(meters: Double, unit: DistanceUnit) = "${formatDistance(meters, unit)} ${unit.symbol}"

    fun paceLabel(kmh: Double, unit: PaceUnit) = "${formatPace(kmh, unit)} ${unit.symbol}"

    /** The same unit system for the speed when the pace is a time, or for the time when it is a speed. */
    internal fun other(unit: PaceUnit) = when (unit) {
        PaceUnit.PER_KM -> PaceUnit.KMH
        PaceUnit.PER_MI -> PaceUnit.MPH
        PaceUnit.KMH -> PaceUnit.PER_KM
        PaceUnit.MPH -> PaceUnit.PER_MI
    }

    private fun number(text: String) = text.trim().replace(',', '.').toDoubleOrNull()

    private fun two(n: Int) = n.toString().padStart(2, '0')

    internal fun decimals(value: Double, places: Int, keepZero: Boolean = false): String {
        var factor = 1L
        repeat(places) { factor *= 10 }
        val scaled = (abs(value) * factor).roundToLong()
        val whole = scaled / factor
        var fraction = (scaled % factor).toString().padStart(places, '0')
        if (!keepZero) fraction = fraction.trimEnd('0')
        val sign = if (value < 0 && scaled != 0L) "-" else ""
        return if (fraction.isEmpty()) "$sign$whole" else "$sign$whole.$fraction"
    }
}
