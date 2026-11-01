package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMetric
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BodyDisplayTest {
    private val nbsp = " "

    @Test
    fun weightsAndHeightsReadInTheChosenUnits() {
        assertEquals("72.6${nbsp}kg", BodyDisplay.weight(72.6, UnitSystem.METRIC, "en"))
        assertEquals("72,6${nbsp}kg", BodyDisplay.weight(72.6, UnitSystem.METRIC, "fr"))
        assertEquals("160.1${nbsp}lb", BodyDisplay.weight(72.6, UnitSystem.IMPERIAL, "en"))
        assertEquals("175${nbsp}cm", BodyDisplay.height(175.0, UnitSystem.METRIC, "en"))
        assertEquals("5${nbsp}ft 9${nbsp}in", BodyDisplay.height(175.0, UnitSystem.IMPERIAL, "en"))
        assertEquals("5${nbsp}ft 9,5${nbsp}in", BodyDisplay.height(176.5, UnitSystem.IMPERIAL, "fr"))
    }

    @Test
    fun aChangeCarriesItsSignAndAZeroHasNone() {
        assertEquals("−2.2${nbsp}kg", BodyDisplay.weightChange(-2.2, UnitSystem.METRIC, "en"))
        assertEquals("+0.4${nbsp}kg", BodyDisplay.weightChange(0.4, UnitSystem.METRIC, "en"))
        assertEquals("0${nbsp}kg", BodyDisplay.weightChange(0.01, UnitSystem.METRIC, "en"))
    }

    @Test
    fun theStepperMovesInSmallStepsOfTheUnitShown() {
        assertEquals(72.7, BodyDisplay.step(BodyMetric.WEIGHT, 72.6, UnitSystem.METRIC, +1), 1e-9)
        assertEquals(72.5, BodyDisplay.step(BodyMetric.WEIGHT, 72.6, UnitSystem.METRIC, -1), 1e-9)
        assertEquals(Units.kilogramsOf(160.2), BodyDisplay.step(BodyMetric.WEIGHT, Units.kilogramsOf(160.0), UnitSystem.IMPERIAL, +1), 1e-6)
        assertEquals(176.0, BodyDisplay.step(BodyMetric.HEIGHT, 175.0, UnitSystem.METRIC, +1), 1e-9)
        assertEquals(Units.centimetresOf(5, 9.5), BodyDisplay.step(BodyMetric.HEIGHT, Units.centimetresOf(5, 9.0), UnitSystem.IMPERIAL, +1), 1e-9)
        assertEquals(BodyDisplay.MIN_KG, BodyDisplay.step(BodyMetric.WEIGHT, BodyDisplay.MIN_KG, UnitSystem.METRIC, -1), 1e-9)
    }

    @Test
    fun typedValuesAreReadBackInKilogramsAndCentimetres() {
        assertEquals("72.6", BodyDisplay.weightText(72.6, UnitSystem.METRIC, "en"))
        assertEquals("72,6", BodyDisplay.weightText(72.6, UnitSystem.METRIC, "fr"))
        assertEquals(72.6, BodyDisplay.weightFrom("72,6", UnitSystem.METRIC))
        assertEquals(72.6, BodyDisplay.weightFrom("160.1", UnitSystem.IMPERIAL)!!, 0.05)
        assertNull(BodyDisplay.weightFrom("", UnitSystem.METRIC))
        assertNull(BodyDisplay.weightFrom("5", UnitSystem.METRIC))
        assertNull(BodyDisplay.weightFrom("abc", UnitSystem.METRIC))
        assertEquals(175.0, BodyDisplay.centimetresFrom("175"))
        assertNull(BodyDisplay.centimetresFrom("17"))
        assertEquals("175", BodyDisplay.centimetresText(174.6))
        assertEquals("5", BodyDisplay.feetText(175.0))
        assertEquals("9", BodyDisplay.inchesText(175.0, "en"))
        assertEquals("9,5", BodyDisplay.inchesText(176.5, "fr"))
        assertEquals(175.26, BodyDisplay.centimetresFrom("5", "9")!!, 0.01)
        assertEquals(152.4, BodyDisplay.centimetresFrom("5", "")!!, 0.01)
        assertNull(BodyDisplay.centimetresFrom("", "9"))
    }

    @Test
    fun chartGridLinesAreRoundAndLabelledInTheShownUnit() {
        assertEquals(listOf(72.0, 73.0, 74.0, 75.0), ChartAxis.ticks(72.6, 74.8, minSpan = 2.0))
        assertEquals(listOf(71.0, 72.0, 73.0), ChartAxis.ticks(72.0, 72.0, minSpan = 2.0))
        assertEquals(listOf(60.0, 70.0, 80.0, 90.0), ChartAxis.ticks(62.0, 88.0, minSpan = 2.0))
        assertEquals("73", BodyDisplay.axisLabel(BodyMetric.WEIGHT, 73.0, UnitSystem.METRIC, "en"))
        assertEquals("72,5", BodyDisplay.axisLabel(BodyMetric.WEIGHT, 72.5, UnitSystem.METRIC, "fr"))
        assertEquals("5′9″", BodyDisplay.axisLabel(BodyMetric.HEIGHT, 69.0, UnitSystem.IMPERIAL, "en"))
        assertEquals("kg", BodyDisplay.axisUnit(BodyMetric.WEIGHT, UnitSystem.METRIC))
        assertEquals("lb", BodyDisplay.axisUnit(BodyMetric.WEIGHT, UnitSystem.IMPERIAL))
        assertEquals(69.0, BodyDisplay.chartValue(BodyMetric.HEIGHT, 175.26, UnitSystem.IMPERIAL), 0.01)
    }
}
