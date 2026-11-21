package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class YouModelTest {
    private val zone = TimeZone.UTC
    // Saturday 24 October 2026, noon.
    private val now = Instant.parse("2026-10-24T12:00:00Z")

    private fun row(id: String, date: String, minutes: Int = 30, meters: Double? = null) = SessionRow(
        id = id, title = id, kind = SessionKind.WORKOUT, formatLabel = null,
        startedAtMillis = LocalDate.parse(date).atStartOfDayIn(zone).toEpochMilliseconds() + 9 * 3_600_000L,
        activeSeconds = minutes * 60L, setsDone = null, setsSkipped = null, setsPlanned = null, distanceMeters = meters,
        averageSpeedKmh = null, averageHeartRateBpm = null, maxHeartRateBpm = null, energyKcal = null, estimatedSteps = null,
    )

    private val rows = listOf(
        row("today", "2026-10-24", 20, 3000.0),
        row("sixDaysAgo", "2026-10-18", 40),
        row("sevenDaysAgo", "2026-10-17", 50, 5200.0),
        row("twentyNineDaysAgo", "2026-09-25", 60),
        row("thirtyDaysAgo", "2026-09-24", 70, 8000.0),
        row("lastYear", "2025-10-25", 10),
        row("overAYear", "2025-10-24", 15),
    )

    @Test
    fun sevenDaysCountTodayAndTheSixBefore() {
        val stats = PeriodStats.of(rows, StatsPeriod.DAYS_7, now, zone)
        assertEquals(2, stats.sessions)
        assertEquals(60, stats.activeMinutes)
        assertEquals(3000.0, stats.distanceMeters)
    }

    @Test
    fun thirtyDaysStopTheDayBeforeTheThirtiethDayAgo() {
        val stats = PeriodStats.of(rows, StatsPeriod.DAYS_30, now, zone)
        assertEquals(4, stats.sessions)
        assertEquals(170, stats.activeMinutes)
        assertEquals(5200.0, stats.longestRunMeters)
    }

    @Test
    fun aYearKeepsTheSameDateLastYearPlusOne() {
        assertEquals(6, PeriodStats.of(rows, StatsPeriod.YEAR, now, zone).sessions)
    }

    @Test
    fun allTimeCountsEverySessionAndPicksTheLongestRun() {
        val stats = PeriodStats.of(rows, StatsPeriod.ALL_TIME, now, zone)
        assertEquals(7, stats.sessions)
        assertEquals(8000.0, stats.longestRunMeters)
        assertEquals(rows.first { it.id == "thirtyDaysAgo" }.startedAtMillis, stats.longestRunMillis)
    }

    @Test
    fun noDistanceMeansNoLongestRun() {
        val stats = PeriodStats.of(listOf(row("a", "2026-10-24")), StatsPeriod.ALL_TIME, now, zone)
        assertNull(stats.longestRunMeters)
        assertNull(stats.longestRunMillis)
    }

    @Test
    fun aSessionInTheFutureIsNotCounted() {
        assertEquals(0, PeriodStats.of(listOf(row("later", "2026-10-25")), StatsPeriod.ALL_TIME, now, zone).sessions)
    }

    @Test
    fun blocksComeBackInTheSavedOrderWithWhatWasSwitchedOff() {
        val saved = StatBlocks.write(listOf(StatBlockChoice(StatBlock.RECORDS_AND_WEIGHT, false), StatBlockChoice(StatBlock.SESSIONS_AND_TIME, true)))
        assertEquals("records:0,sessions:1", saved)
        assertEquals(
            listOf(StatBlockChoice(StatBlock.RECORDS_AND_WEIGHT, false), StatBlockChoice(StatBlock.SESSIONS_AND_TIME, true)),
            StatBlocks.parse(saved),
        )
    }

    @Test
    fun nothingSavedShowsEveryBlockInTheDefaultOrder() {
        assertEquals(StatBlocks.DEFAULT, StatBlocks.parse(null))
        assertEquals(StatBlocks.DEFAULT, StatBlocks.parse("nonsense"))
    }

    @Test
    fun aBlockMissingFromTheSavedTextIsAddedLastAndShown() {
        assertEquals(
            listOf(StatBlockChoice(StatBlock.RECORDS_AND_WEIGHT, false), StatBlockChoice(StatBlock.SESSIONS_AND_TIME, true)),
            StatBlocks.parse("records:0,gone:1"),
        )
    }

    @Test
    fun movingABlockPutsItAtThatPlace() {
        val moved = StatBlocks.move(StatBlocks.DEFAULT, StatBlock.RECORDS_AND_WEIGHT, 0)
        assertEquals(listOf(StatBlock.RECORDS_AND_WEIGHT, StatBlock.SESSIONS_AND_TIME), moved.map { it.block })
        assertEquals(StatBlocks.DEFAULT, StatBlocks.move(moved, StatBlock.RECORDS_AND_WEIGHT, 5))
    }

    @Test
    fun switchingABlockOffKeepsItsPlace() {
        val off = StatBlocks.show(StatBlocks.DEFAULT, StatBlock.SESSIONS_AND_TIME, false)
        assertEquals(listOf(false, true), off.map { it.shown })
    }
}
