package ch.coachpoulpy.device.ftms

import ch.coachpoulpy.core.model.DeviceMetrics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * One Treadmill Data notification, decoded. Null means the field was not in this
 * notification. Units are the app's: km/h, %, metres, kcal, seconds.
 */
data class TreadmillData(
    /** Set when the machine split one reading across several notifications and more follow. */
    val moreData: Boolean = false,
    val speedKmh: Double? = null,
    val averageSpeedKmh: Double? = null,
    val totalDistanceMeters: Int? = null,
    val inclinePercent: Double? = null,
    val rampAngleDegrees: Double? = null,
    val positiveElevationGainMeters: Double? = null,
    val negativeElevationGainMeters: Double? = null,
    val instantaneousPaceKmPerMin: Double? = null,
    val averagePaceKmPerMin: Double? = null,
    val totalEnergyKcal: Int? = null,
    val energyPerHourKcal: Int? = null,
    val energyPerMinuteKcal: Int? = null,
    val heartRateBpm: Int? = null,
    val metabolicEquivalent: Double? = null,
    val elapsed: Duration? = null,
    val remaining: Duration? = null,
    val forceOnBeltNewtons: Int? = null,
    val powerWatts: Int? = null,
) {
    /** Fields of [newer] win; the rest are kept. Used to join a split reading. */
    fun mergedWith(newer: TreadmillData) = TreadmillData(
        moreData = newer.moreData,
        speedKmh = newer.speedKmh ?: speedKmh,
        averageSpeedKmh = newer.averageSpeedKmh ?: averageSpeedKmh,
        totalDistanceMeters = newer.totalDistanceMeters ?: totalDistanceMeters,
        inclinePercent = newer.inclinePercent ?: inclinePercent,
        rampAngleDegrees = newer.rampAngleDegrees ?: rampAngleDegrees,
        positiveElevationGainMeters = newer.positiveElevationGainMeters ?: positiveElevationGainMeters,
        negativeElevationGainMeters = newer.negativeElevationGainMeters ?: negativeElevationGainMeters,
        instantaneousPaceKmPerMin = newer.instantaneousPaceKmPerMin ?: instantaneousPaceKmPerMin,
        averagePaceKmPerMin = newer.averagePaceKmPerMin ?: averagePaceKmPerMin,
        totalEnergyKcal = newer.totalEnergyKcal ?: totalEnergyKcal,
        energyPerHourKcal = newer.energyPerHourKcal ?: energyPerHourKcal,
        energyPerMinuteKcal = newer.energyPerMinuteKcal ?: energyPerMinuteKcal,
        heartRateBpm = newer.heartRateBpm ?: heartRateBpm,
        metabolicEquivalent = newer.metabolicEquivalent ?: metabolicEquivalent,
        elapsed = newer.elapsed ?: elapsed,
        remaining = newer.remaining ?: remaining,
        forceOnBeltNewtons = newer.forceOnBeltNewtons ?: forceOnBeltNewtons,
        powerWatts = newer.powerWatts ?: powerWatts,
    )

    fun toMetrics() = DeviceMetrics(
        speedKmh = speedKmh,
        inclinePercent = inclinePercent,
        distanceMeters = totalDistanceMeters?.toDouble(),
        // 0 and 255 are what machines send when no chest strap or grip is in use.
        heartRateBpm = heartRateBpm?.takeIf { it in 1..254 },
        energyKcal = totalEnergyKcal?.takeIf { it != 0xFFFF },
        powerWatts = powerWatts,
        elevationGainMeters = positiveElevationGainMeters,
        machineElapsed = elapsed,
    )
}

/**
 * Decodes the Treadmill Data characteristic (0x2ACD). The 16-bit flags at the start
 * say which fields follow, in a fixed order. Bit 0 is inverted: when it is clear, the
 * instantaneous speed is present.
 */
object TreadmillDataDecoder {
    private const val MORE_DATA = 0
    private const val AVERAGE_SPEED = 1
    private const val TOTAL_DISTANCE = 2
    private const val INCLINATION_AND_RAMP = 3
    private const val ELEVATION_GAIN = 4
    private const val INSTANTANEOUS_PACE = 5
    private const val AVERAGE_PACE = 6
    private const val EXPENDED_ENERGY = 7
    private const val HEART_RATE = 8
    private const val METABOLIC_EQUIVALENT = 9
    private const val ELAPSED_TIME = 10
    private const val REMAINING_TIME = 11
    private const val FORCE_AND_POWER = 12

    fun decode(bytes: ByteArray): TreadmillData {
        val reader = ByteReader(bytes)
        val flags = reader.uint16()
        fun has(bit: Int) = (flags shr bit) and 1 == 1

        val moreData = has(MORE_DATA)
        val speed = if (!moreData) reader.uint16() / 100.0 else null
        val averageSpeed = if (has(AVERAGE_SPEED)) reader.uint16() / 100.0 else null
        val distance = if (has(TOTAL_DISTANCE)) reader.uint24() else null
        val (incline, ramp) = if (has(INCLINATION_AND_RAMP)) reader.sint16() / 10.0 to reader.sint16() / 10.0 else null to null
        val (elevationUp, elevationDown) = if (has(ELEVATION_GAIN)) reader.uint16() / 10.0 to reader.uint16() / 10.0 else null to null
        // FTMS 1.0 defines both paces as one byte in 0.1 km/min. Treadmills rarely send them;
        // if one does with a different size, the fields after it would be off, so check here first.
        val pace = if (has(INSTANTANEOUS_PACE)) reader.uint8() / 10.0 else null
        val averagePace = if (has(AVERAGE_PACE)) reader.uint8() / 10.0 else null
        var totalEnergy: Int? = null
        var energyPerHour: Int? = null
        var energyPerMinute: Int? = null
        if (has(EXPENDED_ENERGY)) {
            totalEnergy = reader.uint16()
            energyPerHour = reader.uint16()
            energyPerMinute = reader.uint8()
        }
        val heartRate = if (has(HEART_RATE)) reader.uint8() else null
        val met = if (has(METABOLIC_EQUIVALENT)) reader.uint8() / 10.0 else null
        val elapsed = if (has(ELAPSED_TIME)) reader.uint16().seconds else null
        val remaining = if (has(REMAINING_TIME)) reader.uint16().seconds else null
        val (force, power) = if (has(FORCE_AND_POWER)) reader.sint16() to reader.sint16() else null to null

        return TreadmillData(
            moreData = moreData,
            speedKmh = speed,
            averageSpeedKmh = averageSpeed,
            totalDistanceMeters = distance,
            inclinePercent = incline,
            rampAngleDegrees = ramp,
            positiveElevationGainMeters = elevationUp,
            negativeElevationGainMeters = elevationDown,
            instantaneousPaceKmPerMin = pace,
            averagePaceKmPerMin = averagePace,
            totalEnergyKcal = totalEnergy,
            energyPerHourKcal = energyPerHour,
            energyPerMinuteKcal = energyPerMinute,
            heartRateBpm = heartRate,
            metabolicEquivalent = met,
            elapsed = elapsed,
            remaining = remaining,
            forceOnBeltNewtons = force,
            powerWatts = power,
        )
    }
}

/**
 * Joins notifications into complete readings. A machine with more fields than fit in
 * one packet sends several with the "more data" bit set, then a last one without it.
 * Each complete reading also keeps fields from earlier readings, so a value a machine
 * sends only every few seconds does not flicker on screen.
 */
class TreadmillDataAssembler {
    private var latest = TreadmillData()

    /** Returns the complete reading when [packet] ends one, else null. */
    fun add(packet: TreadmillData): TreadmillData? {
        latest = latest.mergedWith(packet)
        return if (packet.moreData) null else latest
    }
}
