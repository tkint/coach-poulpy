package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShareCardTest {
    private val workout = SessionRow(
        id = "s1", title = "Lower body strength", kind = SessionKind.WORKOUT, formatLabel = "Sets",
        startedAtMillis = 1_000L, activeSeconds = 38 * 60 + 24, setsDone = 17, setsSkipped = 1, setsPlanned = 18,
        distanceMeters = null, averageSpeedKmh = null, averageHeartRateBpm = null, maxHeartRateBpm = null,
        energyKcal = null, estimatedSteps = null,
    )

    private fun ShareCard.labels() = lines.map { it.label }

    @Test
    fun aWorkoutWithoutAMachineShowsOnlyTimeAndSets() {
        val card = ShareCard.of(workout, UnitSystem.METRIC, "en")
        assertEquals(listOf(ShareLine("Time", "38:24"), ShareLine("Sets", "17 of 18")), card.lines)
        assertEquals("Lower body strength", card.title)
        assertEquals(1_000L, card.startedAtMillis)
        assertEquals(PoulpyDrawing.MOOD_HAPPY, card.drawing)
        assertEquals("Coach Poulpy", card.appName)
    }

    @Test
    fun machineValuesComeAfterTheSetsInAFixedOrder() {
        val row = workout.copy(distanceMeters = 5200.0, averageSpeedKmh = 8.1, energyKcal = 412, averageHeartRateBpm = 140, maxHeartRateBpm = 170, estimatedSteps = 6000)
        val card = ShareCard.of(row, UnitSystem.METRIC, "en")
        assertEquals(listOf("Time", "Sets", "Distance", "Avg speed", "Energy"), card.labels())
        assertEquals(listOf("38:24", "17 of 18", "5.2\u00a0km", "8.1\u00a0km/h", "412 kcal"), card.lines.map { it.value })
    }

    @Test
    fun heartRateAndStepsAreNeverOnTheCard() {
        val row = workout.copy(averageHeartRateBpm = 140, maxHeartRateBpm = 170, estimatedSteps = 6000)
        assertEquals(listOf("Time", "Sets"), ShareCard.of(row, UnitSystem.METRIC, "en").labels())
    }

    @Test
    fun aMissingValueLeavesItsLineOut() {
        val row = workout.copy(distanceMeters = null, averageSpeedKmh = 7.0, energyKcal = null)
        assertEquals(listOf("Time", "Sets", "Avg speed"), ShareCard.of(row, UnitSystem.METRIC, "en").labels())
    }

    @Test
    fun aSessionWhoseWorkoutIsGoneHasNoSetsLine() {
        val row = workout.copy(setsDone = null, setsSkipped = null, setsPlanned = null)
        assertEquals(listOf("Time"), ShareCard.of(row, UnitSystem.METRIC, "en").labels())
    }

    @Test
    fun setsWithoutAPlanShowOnlyTheCountDone() {
        val row = workout.copy(setsPlanned = null)
        assertEquals("17", ShareCard.of(row, UnitSystem.METRIC, "en").lines[1].value)
    }

    @Test
    fun aTreadmillSessionShowsNoSets() {
        val row = workout.copy(kind = SessionKind.TREADMILL, setsDone = 3, setsPlanned = 4, distanceMeters = 3000.0)
        assertEquals(listOf("Time", "Distance"), ShareCard.of(row, UnitSystem.METRIC, "en").labels())
    }

    @Test
    fun anEmptySessionStillHasItsTime() {
        val row = workout.copy(activeSeconds = 0, setsDone = 0)
        assertEquals(ShareLine("Time", "0:00"), ShareCard.of(row, UnitSystem.METRIC, "en").lines.first())
    }

    @Test
    fun longSessionsShowHours() {
        val row = workout.copy(activeSeconds = 3600 + 5 * 60 + 9)
        assertEquals("1:05:09", ShareCard.of(row, UnitSystem.METRIC, "en").lines.first().value)
    }

    @Test
    fun imperialUnitsConvertDistanceAndSpeed() {
        val row = workout.copy(distanceMeters = 1609.344, averageSpeedKmh = 16.09344)
        val values = ShareCard.of(row, UnitSystem.IMPERIAL, "en").lines.map { it.value }
        assertTrue("1.0\u00a0mi" in values, values.toString())
        assertTrue("10.0\u00a0mph" in values, values.toString())
    }

    @Test
    fun theEighthTentacleShowsChampion() {
        assertEquals(PoulpyDrawing.CHAMPION, ShareCard.of(workout, UnitSystem.METRIC, "en", fullOctopus = true).drawing)
    }

    @Test
    fun frenchUsesACommaForDecimals() {
        val row = workout.copy(distanceMeters = 5200.0)
        assertEquals("5,2\u00a0km", ShareCard.of(row, UnitSystem.METRIC, "fr").lines.last().value)
    }

    @Test
    fun theCardSpeaksFrench() {
        val card = ShareCard.of(workout, UnitSystem.METRIC, "fr")
        assertEquals(listOf(ShareLine("Durée", "38:24"), ShareLine("Séries", "17 sur 18")), card.lines)
    }
}
