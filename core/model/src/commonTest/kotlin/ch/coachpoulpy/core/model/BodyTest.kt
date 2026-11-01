package ch.coachpoulpy.core.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BodyTest {
    private val today = LocalDate(2026, 11, 1)

    private fun weight(day: String, kg: Double) = BodyMeasurement(BodyMetric.WEIGHT, LocalDate.parse(day), kg)

    private val log = listOf(
        weight("2026-08-02", 74.8),
        weight("2026-10-15", 73.0),
        weight("2026-10-29", 72.6),
        weight("2026-09-20", 73.9),
        weight("2026-07-15", 76.0),
    )

    @Test
    fun ageCountsWholeYearsAndIsNullWithoutABirthdate() {
        val profile = Profile(birthdate = LocalDate(1991, 3, 14))
        assertEquals(35, profile.age(LocalDate(2026, 3, 14)))
        assertEquals(34, profile.age(LocalDate(2026, 3, 13)))
        assertNull(Profile().age(today))
        assertNull(Profile(birthdate = LocalDate(2030, 1, 1)).age(today))
    }

    @Test
    fun trendOfThreeMonthsLeavesOutOlderEntriesAndReadsChangeLowestHighest() {
        val trend = BodyTrend.of(log, BodyRange.THREE_MONTHS, today)

        assertEquals(listOf("2026-08-02", "2026-09-20", "2026-10-15", "2026-10-29"), trend.entries.map { it.day.toString() })
        assertEquals(72.6 - 74.8, trend.change!!, 1e-9)
        assertEquals(72.6, trend.lowest!!.value)
        assertEquals(74.8, trend.highest!!.value)
    }

    @Test
    fun allKeepsEveryEntryAndOneEntryHasNoChange() {
        assertEquals(5, BodyTrend.of(log, BodyRange.ALL, today).entries.size)
        assertNull(BodyTrend.of(log, BodyRange.MONTH, today).entries.let { BodyTrend.of(it.take(1), BodyRange.ALL, today).change })
        assertNull(BodyTrend.of(emptyList(), BodyRange.ALL, today).lowest)
    }
}
