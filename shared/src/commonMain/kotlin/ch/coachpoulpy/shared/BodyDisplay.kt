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

    /** What the stepper starts on when nothing was logged yet. */
    fun starting(metric: BodyMetric) = if (metric == BodyMetric.WEIGHT) 70.0 else 170.0

    const val MIN_KG = 20.0
    const val MAX_KG = 400.0
    const val MIN_CM = 50.0
    const val MAX_CM = 250.0
}
