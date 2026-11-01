package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMetric
import kotlin.math.abs
import kotlin.math.round

/**
 * How weights and heights read and step in the person's unit system. Values are kg and cm
 * (ADR 0017); the apps only render these strings and call [step] from their stepper.
 */
object BodyDisplay {
    private const val NON_BREAKING_SPACE = " "

    /** "72.6 kg" or "160.1 lb"; the decimal sign follows [languageCode]. */
    fun weight(kg: Double, system: UnitSystem, languageCode: String): String =
        ShareCard.measure(Units.weight(kg, system), languageCode)

    /** "175 cm" or "5 ft 9 in" (half inches show as "5 ft 9.5 in"). */
    fun height(cm: Double, system: UnitSystem, languageCode: String): String = when (system) {
        UnitSystem.METRIC -> ShareCard.measure(Units.height(cm, system), languageCode)
        UnitSystem.IMPERIAL -> {
            val (feet, inches) = Units.feetAndInches(cm)
            val shown = if (inches % 1.0 == 0.0) inches.toInt().toString() else inches.toString().replace('.', if (languageCode == "fr") ',' else '.')
            "$feet${NON_BREAKING_SPACE}ft $shown${NON_BREAKING_SPACE}in"
        }
    }

    fun value(metric: BodyMetric, stored: Double, system: UnitSystem, languageCode: String) = when (metric) {
        BodyMetric.WEIGHT -> weight(stored, system, languageCode)
        BodyMetric.HEIGHT -> height(stored, system, languageCode)
    }

    /** A difference with its sign: "−2.2 kg", "+0.4 kg" or "0 kg". Never coloured: going up is not bad, going down is not good. */
    fun weightChange(deltaKg: Double, system: UnitSystem, languageCode: String): String {
        val measure = Units.weight(abs(deltaKg), system)
        val rounded = round(measure.value * 10) / 10
        val body = ShareCard.measure(measure, languageCode)
        return when {
            rounded == 0.0 -> "0${NON_BREAKING_SPACE}${measure.unit}"
            deltaKg < 0 -> "−$body"
            else -> "+$body"
        }
    }

    /**
     * The stored value after one press of the stepper: 0.1 kg or 0.2 lb for a weight (in the
     * unit shown, then back to kg), one centimetre or half an inch for a height. [direction]
     * is +1 or −1. Results stay inside the range people can plausibly be.
     */
    fun step(metric: BodyMetric, stored: Double, system: UnitSystem, direction: Int): Double = when (metric) {
        BodyMetric.WEIGHT -> when (system) {
            UnitSystem.METRIC -> (round(stored * 10) + direction) / 10
            UnitSystem.IMPERIAL -> Units.kilogramsOf((round(Units.poundsOf(stored) * 5) + direction) / 5)
        }.coerceIn(MIN_KG, MAX_KG)
        BodyMetric.HEIGHT -> when (system) {
            UnitSystem.METRIC -> round(stored) + direction
            UnitSystem.IMPERIAL -> {
                val (feet, inches) = Units.feetAndInches(stored)
                Units.centimetresOf(feet, inches + 0.5 * direction)
            }
        }.coerceIn(MIN_CM, MAX_CM)
    }

    /** The text of a weight field: kg or lb to one decimal, with the decimal sign of [languageCode]. */
    fun weightText(kg: Double, system: UnitSystem, languageCode: String): String =
        (round(Units.weight(kg, system).value * 10) / 10).toString().withDecimalSign(languageCode)

    /** The kilograms typed in a weight field in [system], or null when it is not a number or not a plausible weight. */
    fun weightFrom(text: String, system: UnitSystem): Double? {
        val typed = text.asNumber() ?: return null
        val kg = if (system == UnitSystem.IMPERIAL) Units.kilogramsOf(typed) else typed
        return kg.takeIf { it in MIN_KG..MAX_KG }
    }

    /** The text of a centimetre field, whole centimetres. */
    fun centimetresText(cm: Double): String = round(cm).toInt().toString()

    /** The centimetres typed in a height field, or null when it is not a number or not a plausible height. */
    fun centimetresFrom(text: String): Double? = text.asNumber()?.takeIf { it in MIN_CM..MAX_CM }

    /** The feet of a height, for the ft field. */
    fun feetText(cm: Double): String = Units.feetAndInches(cm).feet.toString()

    /** The inches left over after the feet of a height, for the in field (half inches show with a decimal sign). */
    fun inchesText(cm: Double, languageCode: String): String {
        val inches = Units.feetAndInches(cm).inches
        return if (inches % 1.0 == 0.0) inches.toInt().toString() else inches.toString().withDecimalSign(languageCode)
    }

    /** The centimetres of a height typed as feet and inches; empty inches count as 0. Null when it is not a plausible height. */
    fun centimetresFrom(feet: String, inches: String): Double? {
        val ft = feet.trim().toIntOrNull() ?: return null
        val inch = if (inches.isBlank()) 0.0 else inches.asNumber() ?: return null
        return Units.centimetresOf(ft, inch).takeIf { it in MIN_CM..MAX_CM }
    }

    /** The value a chart plots, in the unit shown: kg or lb, cm or total inches. */
    fun chartValue(metric: BodyMetric, stored: Double, system: UnitSystem): Double = when (metric) {
        BodyMetric.WEIGHT -> Units.weight(stored, system).value
        BodyMetric.HEIGHT -> if (system == UnitSystem.IMPERIAL) stored / CM_PER_INCH else stored
    }

    /** The smallest span a chart axis covers, in the unit shown, so a flat series still has a few lines. */
    fun chartMinSpan(metric: BodyMetric, system: UnitSystem): Double = when (metric) {
        BodyMetric.WEIGHT -> if (system == UnitSystem.IMPERIAL) 4.0 else 2.0
        BodyMetric.HEIGHT -> if (system == UnitSystem.IMPERIAL) 2.0 else 4.0
    }

    /** A grid line's label: "73", "72.5", or "5′9″" for an imperial height. */
    fun axisLabel(metric: BodyMetric, shown: Double, system: UnitSystem, languageCode: String): String =
        if (metric == BodyMetric.HEIGHT && system == UnitSystem.IMPERIAL) {
            val inches = round(shown).toInt()
            "${inches / 12}′${inches % 12}″"
        } else if (shown % 1.0 == 0.0) shown.toInt().toString() else shown.toString().withDecimalSign(languageCode)

    /** The unit written once at the top of the axis; an imperial height carries its units in its labels. */
    fun axisUnit(metric: BodyMetric, system: UnitSystem): String = when (metric) {
        BodyMetric.WEIGHT -> Units.weight(0.0, system).unit
        BodyMetric.HEIGHT -> if (system == UnitSystem.IMPERIAL) "ft in" else "cm"
    }

    private const val CM_PER_INCH = 2.54

    private fun String.asNumber(): Double? = trim().replace(',', '.').toDoubleOrNull()

    private fun String.withDecimalSign(languageCode: String) = if (languageCode == "fr") replace('.', ',') else this

    /** What the stepper starts on when nothing was logged yet. */
    fun starting(metric: BodyMetric) = if (metric == BodyMetric.WEIGHT) 70.0 else 170.0

    const val MIN_KG = 20.0
    const val MAX_KG = 400.0
    const val MIN_CM = 50.0
    const val MAX_CM = 250.0
}
