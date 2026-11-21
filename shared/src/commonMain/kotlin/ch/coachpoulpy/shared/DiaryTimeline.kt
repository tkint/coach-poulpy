package ch.coachpoulpy.shared

import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/** One row of the diary's past timeline on Home, newest first. Dates are epoch milliseconds at local midnight. */
sealed interface DiaryEntry {
    /** A week of sessions. [isCurrent] weeks read "This week"; the platform names the others from [startMillis] (a Monday). */
    data class WeekHeading(val startMillis: Long, val isCurrent: Boolean) : DiaryEntry

    /** The week in numbers, under its heading. */
    data class WeekRecap(val sessions: Int, val activeMinutes: Int) : DiaryEntry

    /** A day that has sessions. */
    data class DayHeading(val dateMillis: Long, val isToday: Boolean) : DiaryEntry

    data class Session(val row: SessionRow) : DiaryEntry
}

/** The diary's past: sessions by week, then by day, newest first, each week opening with its recap. */
internal fun diaryTimeline(sessions: List<SessionRow>, now: Instant, zone: TimeZone): List<DiaryEntry> {
    val today = now.toLocalDateTime(zone).date
    val byDay: List<Pair<LocalDate, List<SessionRow>>> = sessions
        .groupBy { Instant.fromEpochMilliseconds(it.startedAtMillis).toLocalDateTime(zone).date }
        .toList()
        .sortedByDescending { it.first }
    val thisMonday = today.monday()
    return buildList {
        var week: LocalDate? = null
        byDay.forEach { (day, rows) ->
            val monday = day.monday()
            if (monday != week) {
                week = monday
                val inWeek = byDay.filter { it.first.monday() == monday }.flatMap { it.second }
                add(DiaryEntry.WeekHeading(monday.atStartOfDayIn(zone).toEpochMilliseconds(), isCurrent = monday == thisMonday))
                add(DiaryEntry.WeekRecap(inWeek.size, (inWeek.sumOf { it.activeSeconds } / 60).toInt()))
            }
            add(DiaryEntry.DayHeading(day.atStartOfDayIn(zone).toEpochMilliseconds(), isToday = day == today))
            rows.sortedByDescending { it.startedAtMillis }.forEach { add(DiaryEntry.Session(it)) }
        }
    }
}

/**
 * Where the timeline scrolls to for a day tapped in the calendar sheet: that day's heading, or the nearest earlier day
 * with sessions when it had none, or the oldest day when it is older than all of them. -1 when the timeline is empty.
 */
fun List<DiaryEntry>.indexOfDay(dayMillis: Long): Int {
    val headings = withIndex().filter { it.value is DiaryEntry.DayHeading }
    return (headings.firstOrNull { (it.value as DiaryEntry.DayHeading).dateMillis <= dayMillis } ?: headings.lastOrNull())?.index ?: -1
}

private fun LocalDate.monday(): LocalDate = minus(DatePeriod(days = dayOfWeek.isoDayNumber - 1))
