package ch.coachpoulpy.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod

/** What is measured. Weight is stored in kilograms and height in centimetres (ADR 0017). */
enum class BodyMetric { WEIGHT, HEIGHT }

/** One dated entry of the log. [value] is in kg or cm; a second entry on the same day replaces the first. */
data class BodyMeasurement(val metric: BodyMetric, val day: LocalDate, val value: Double)

/** The ranges a chart can show. [days] is null for all of the log. */
enum class BodyRange(val days: Int?) { MONTH(30), THREE_MONTHS(91), YEAR(365), ALL(null) }

/** What the tiles under a chart say, read straight from the log. */
data class BodyTrend(
    val entries: List<BodyMeasurement>,
    /** Last minus first of [entries]; null with fewer than two. */
    val change: Double?,
    val lowest: BodyMeasurement?,
    val highest: BodyMeasurement?,
) {
    companion object {
        /** The entries of [range] counted back from [today], oldest first. The change is judged by nobody: it is just a difference. */
        fun of(log: List<BodyMeasurement>, range: BodyRange, today: LocalDate): BodyTrend {
            val from = range.days?.let { today.minus(DatePeriod(days = it)) }
            val entries = log.filter { from == null || it.day >= from }.sortedBy { it.day }
            return BodyTrend(
                entries = entries,
                change = if (entries.size >= 2) entries.last().value - entries.first().value else null,
                lowest = entries.minByOrNull { it.value },
                highest = entries.maxByOrNull { it.value },
            )
        }
    }
}
