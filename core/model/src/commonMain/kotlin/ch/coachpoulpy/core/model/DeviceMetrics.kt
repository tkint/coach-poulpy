package ch.coachpoulpy.core.model

import kotlin.time.Duration
import kotlinx.serialization.Serializable

/**
 * One reading from a connected machine, in the units the app shows. Every field is
 * optional because machines send different subsets; a missing value means "not sent",
 * never zero. Counters (distance, energy, elevation) are totals since the machine's
 * own start, so a recording keeps the first reading to compute what was done in it.
 */
@Serializable
data class DeviceMetrics(
    val speedKmh: Double? = null,
    val inclinePercent: Double? = null,
    val distanceMeters: Double? = null,
    val heartRateBpm: Int? = null,
    val energyKcal: Int? = null,
    val powerWatts: Int? = null,
    val elevationGainMeters: Double? = null,
    val machineElapsed: Duration? = null,
)
