package ch.coachpoulpy.device.ftms

/**
 * What a machine says it can do, from the Fitness Machine Feature characteristic
 * and the supported ranges. Read once on connect: the UI hides metrics a machine
 * does not report, and control (later) stays within the ranges.
 */
data class TreadmillCapabilities(
    val metrics: Set<MachineMetric>,
    val targets: Set<TargetSetting>,
    val speedRange: SupportedRange? = null,
    val inclineRange: SupportedRange? = null,
) {
    /** Speed and incline control both need the control point; the targets say which ones the machine accepts. */
    val canSetSpeed: Boolean get() = TargetSetting.SPEED in targets
    val canSetIncline: Boolean get() = TargetSetting.INCLINATION in targets

    companion object {
        /** Used when the feature characteristic cannot be read: show whatever arrives. */
        val Unknown = TreadmillCapabilities(MachineMetric.entries.toSet(), emptySet())
    }
}

/** Fitness Machine Features, bits 0 to 16 of the first 32-bit field. */
enum class MachineMetric(val bit: Int) {
    AVERAGE_SPEED(0),
    CADENCE(1),
    TOTAL_DISTANCE(2),
    INCLINATION(3),
    ELEVATION_GAIN(4),
    PACE(5),
    STEP_COUNT(6),
    RESISTANCE_LEVEL(7),
    STRIDE_COUNT(8),
    EXPENDED_ENERGY(9),
    HEART_RATE(10),
    METABOLIC_EQUIVALENT(11),
    ELAPSED_TIME(12),
    REMAINING_TIME(13),
    POWER(14),
    FORCE_ON_BELT_AND_POWER(15),
    USER_DATA_RETENTION(16),
}

/** Target Setting Features, the second 32-bit field: what the control point accepts. */
enum class TargetSetting(val bit: Int) {
    SPEED(0),
    INCLINATION(1),
    RESISTANCE(2),
    POWER(3),
    HEART_RATE(4),
    EXPENDED_ENERGY(5),
    STEP_COUNT(6),
    STRIDE_COUNT(7),
    DISTANCE(8),
    TRAINING_TIME(9),
}

/** A supported speed (km/h) or incline (%) range, with the smallest step the machine takes. */
data class SupportedRange(val min: Double, val max: Double, val increment: Double)

object CapabilitiesDecoder {
    fun features(bytes: ByteArray): Pair<Set<MachineMetric>, Set<TargetSetting>> {
        val reader = ByteReader(bytes)
        val machine = reader.uint32()
        val targets = reader.uint32()
        return MachineMetric.entries.filter { machine.hasBit(it.bit) }.toSet() to
            TargetSetting.entries.filter { targets.hasBit(it.bit) }.toSet()
    }

    /** Supported Speed Range: three uint16 in 0.01 km/h. */
    fun speedRange(bytes: ByteArray): SupportedRange {
        val reader = ByteReader(bytes)
        return SupportedRange(reader.uint16() / 100.0, reader.uint16() / 100.0, reader.uint16() / 100.0)
    }

    /** Supported Inclination Range: min and max as sint16, increment as uint16, all in 0.1 %. */
    fun inclineRange(bytes: ByteArray): SupportedRange {
        val reader = ByteReader(bytes)
        return SupportedRange(reader.sint16() / 10.0, reader.sint16() / 10.0, reader.uint16() / 10.0)
    }

    private fun Long.hasBit(bit: Int) = (this shr bit) and 1L == 1L
}
