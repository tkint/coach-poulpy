package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class FinishedStatsTest {
    private fun stats(facts: FinishedFacts, units: UnitSystem = UnitSystem.METRIC, language: String = "en") =
        FinishedStats.of(facts, units, language).map { it.label to it.value }

    @Test
    fun aSetsSessionShowsTimeSetsAndWeightLifted() {
        assertEquals(
            listOf("Time" to "38:24", "Sets" to "17", "Lifted" to "2.1 t".replace(" ", " ")),
            stats(FinishedFacts(activeSeconds = 2304, setsDone = 17, liftedKg = 2100.0, distanceMeters = 0.0)),
        )
    }

    @Test
    fun lessThanATonneIsShownInKilograms() {
        assertEquals("820 kg", stats(FinishedFacts(600, 3, 820.0, 0.0))[2].second)
    }

    @Test
    fun aBodyweightSessionLeavesLiftedOut() {
        assertEquals(listOf("Time" to "10:00", "Sets" to "3"), stats(FinishedFacts(600, 3, 0.0, 0.0)))
    }

    @Test
    fun aRunShowsTimeDistanceAndPace() {
        assertEquals(
            listOf("Time" to "38:24", "Distance" to "5.2 km", "Pace" to "7:23 /km"),
            stats(FinishedFacts(2304, 6, 0.0, 5200.0)),
        )
    }

    @Test
    fun aRunInMilesAndFrenchUsesTheirUnitsAndDecimalSign() {
        assertEquals(
            listOf("Durée" to "38:24", "Distance" to "3,2 mi", "Allure" to "11:53 /mi"),
            stats(FinishedFacts(2304, 6, 0.0, 5200.0), UnitSystem.IMPERIAL, "fr"),
        )
    }

    @Test
    fun heartRateIsNeverAStat() {
        val labels = stats(FinishedFacts(600, 3, 100.0, 0.0)).map { it.first } + stats(FinishedFacts(600, 3, 0.0, 1000.0)).map { it.first }
        assertEquals(emptyList(), labels.filter { "heart" in it.lowercase() })
    }
}
