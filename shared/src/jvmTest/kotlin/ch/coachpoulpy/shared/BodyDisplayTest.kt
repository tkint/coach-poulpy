package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMetric
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
