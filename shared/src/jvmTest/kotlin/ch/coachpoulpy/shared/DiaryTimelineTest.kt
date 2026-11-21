package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class DiaryTimelineTest {
    private val zone = TimeZone.UTC
    // Saturday 24 October 2026, noon.
    private val now = Instant.parse("2026-10-24T12:00:00Z")

    private fun day(date: String) = LocalDate.parse(date).atStartOfDayIn(zone).toEpochMilliseconds()

    private fun row(id: String, date: String, hour: Int = 9, minutes: Int = 30) = SessionRow(
        id = id, title = id, kind = SessionKind.WORKOUT, formatLabel = null,
        startedAtMillis = day(date) + hour * 3_600_000L, activeSeconds = minutes * 60L,
        setsDone = null, setsSkipped = null, setsPlanned = null, distanceMeters = null, averageSpeedKmh = null,
        averageHeartRateBpm = null, maxHeartRateBpm = null, energyKcal = null, estimatedSteps = null,
    )

    @Test
    fun anEmptyPastHasNoEntries() {
        assertEquals(emptyList(), diaryTimeline(emptyList(), now, zone))
    }

    @Test
    fun sessionsAreGroupedByWeekThenDayNewestFirstWithAWeeklyRecap() {
        val timeline = diaryTimeline(
            listOf(row("old", "2026-10-14"), row("a", "2026-10-22", hour = 8), row("b", "2026-10-22", hour = 18, minutes = 40), row("c", "2026-10-20")),
            now, zone,
        )
        assertEquals(
            listOf(
                DiaryEntry.WeekHeading(day("2026-10-19"), isCurrent = true),
                DiaryEntry.WeekRecap(sessions = 3, activeMinutes = 100),
                DiaryEntry.DayHeading(day("2026-10-22"), isToday = false),
                DiaryEntry.Session(row("b", "2026-10-22", hour = 18, minutes = 40)),
                DiaryEntry.Session(row("a", "2026-10-22", hour = 8)),
                DiaryEntry.DayHeading(day("2026-10-20"), isToday = false),
                DiaryEntry.Session(row("c", "2026-10-20")),
                DiaryEntry.WeekHeading(day("2026-10-12"), isCurrent = false),
                DiaryEntry.WeekRecap(sessions = 1, activeMinutes = 30),
                DiaryEntry.DayHeading(day("2026-10-14"), isToday = false),
                DiaryEntry.Session(row("old", "2026-10-14")),
            ),
            timeline,
        )
    }

    @Test
    fun todayIsMarked() {
        val heading = diaryTimeline(listOf(row("a", "2026-10-24")), now, zone).filterIsInstance<DiaryEntry.DayHeading>().single()
        assertEquals(true, heading.isToday)
    }

    @Test
    fun aTappedDayScrollsToItsHeadingOrTheNearestEarlierDay() {
        val timeline = diaryTimeline(listOf(row("a", "2026-10-22"), row("c", "2026-10-20"), row("old", "2026-10-14")), now, zone)
        fun headingAt(index: Int) = (timeline[index] as DiaryEntry.DayHeading).dateMillis
        assertEquals(day("2026-10-22"), headingAt(timeline.indexOfDay(day("2026-10-22"))))
        // A rest day goes to the last day with sessions before it.
        assertEquals(day("2026-10-20"), headingAt(timeline.indexOfDay(day("2026-10-21"))))
        // A day after the newest session goes to the newest.
        assertEquals(day("2026-10-22"), headingAt(timeline.indexOfDay(day("2026-10-24"))))
        // A day before every session goes to the oldest.
        assertEquals(day("2026-10-14"), headingAt(timeline.indexOfDay(day("2026-09-01"))))
    }

    @Test
    fun anEmptyTimelineHasNowhereToScroll() {
        assertEquals(-1, emptyList<DiaryEntry>().indexOfDay(day("2026-10-22")))
    }
}
