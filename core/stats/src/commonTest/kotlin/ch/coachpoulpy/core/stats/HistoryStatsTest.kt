package ch.coachpoulpy.core.stats

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class HistoryStatsTest {
    private val zone = TimeZone.of("Europe/Zurich")

    private fun at(date: String, time: String = "18:00") = LocalDateTime.parse("${date}T$time").toInstant(zone)

    private fun session(date: String, minutes: Int, meters: Double? = null, time: String = "18:00") =
        DoneSession(at(date, time), SessionTotals(active = minutes.minutes, distanceMeters = meters))

    // Wednesday 2026-10-21.
    private val now = at("2026-10-21", "12:00")

    @Test
    fun weekRunsMondayToSundayInTheLocalZone() {
        val stats = HistoryStats.of(
            listOf(
                session("2026-10-19", 30, time = "00:30"), // Monday just after midnight: this week
                session("2026-10-18", 20, time = "23:30"), // Sunday night: last week
                session("2026-10-21", 15, meters = 2500.0),
            ),
            now, zone,
        )
        assertEquals(2, stats.thisWeek.sessions)
        assertEquals(45.minutes, stats.thisWeek.active)
        assertEquals(2500.0, stats.thisWeek.distanceMeters)
        assertEquals(LocalDate(2026, 10, 19), stats.weeks.last().monday)
        assertEquals(20.minutes, stats.weeks[stats.weeks.size - 2].totals.active)
    }

    @Test
    fun monthIsTheCalendarMonth() {
        val stats = HistoryStats.of(
            listOf(session("2026-10-01", 10), session("2026-09-30", 40), session("2026-10-20", 25)),
            now, zone,
        )
        assertEquals(2, stats.thisMonth.sessions)
        assertEquals(35.minutes, stats.thisMonth.active)
    }

    @Test
    fun givesEightWeeksOldestFirstEvenWhenEmpty() {
        val stats = HistoryStats.of(listOf(session("2026-09-01", 50)), now, zone)
        assertEquals(8, stats.weeks.size)
        assertEquals(LocalDate(2026, 8, 31), stats.weeks.first().monday)
        assertEquals(50.minutes, stats.weeks.first().totals.active)
        assertEquals(Duration.ZERO, stats.thisWeek.active)
    }
}
