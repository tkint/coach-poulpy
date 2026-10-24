package ch.coachpoulpy.core.stats

import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** One finished session, as the stats see it. */
data class DoneSession(val startedAt: Instant, val totals: SessionTotals)

data class PeriodTotals(val sessions: Int, val active: Duration, val distanceMeters: Double) {
    companion object {
        val None = PeriodTotals(0, Duration.ZERO, 0.0)
    }
}

/** A calendar week, Monday to Sunday in the phone's time zone. */
data class WeekTotals(val monday: LocalDate, val totals: PeriodTotals)

data class HistoryStats(val thisWeek: PeriodTotals, val thisMonth: PeriodTotals, val weeks: List<WeekTotals>) {
    companion object {
        /**
         * Totals for the current week and month, and for each of the last [weekCount]
         * weeks, oldest first, the current one included. A session counts on the day it started.
         */
        fun of(sessions: List<DoneSession>, now: Instant, zone: TimeZone, weekCount: Int = 8): HistoryStats {
            val today = now.toLocalDateTime(zone).date
            val thisMonday = today.monday()
            val byDay = sessions.groupBy { it.startedAt.toLocalDateTime(zone).date }

            fun total(days: (LocalDate) -> Boolean): PeriodTotals {
                val picked = byDay.filterKeys(days).values.flatten().map { it.totals }
                return PeriodTotals(
                    sessions = picked.size,
                    active = picked.fold(Duration.ZERO) { sum, it -> sum + it.active },
                    distanceMeters = picked.sumOf { it.distanceMeters ?: 0.0 },
                )
            }

            val weeks = (weekCount - 1 downTo 0).map { back ->
                val monday = thisMonday.minus(DatePeriod(days = 7 * back))
                val nextMonday = monday.plus(DatePeriod(days = 7))
                WeekTotals(monday, total { it >= monday && it < nextMonday })
            }
            return HistoryStats(
                thisWeek = weeks.last().totals,
                thisMonth = total { it.year == today.year && it.month == today.month },
                weeks = weeks,
            )
        }

        private fun LocalDate.monday(): LocalDate =
            minus(DatePeriod(days = dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber))
    }
}
