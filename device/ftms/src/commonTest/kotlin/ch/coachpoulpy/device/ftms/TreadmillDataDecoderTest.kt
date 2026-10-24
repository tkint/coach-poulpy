package ch.coachpoulpy.device.ftms

import ch.coachpoulpy.core.model.DeviceMetrics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class TreadmillDataDecoderTest {
    @Test
    fun speedAloneWhenNoFlagIsSet() {
        val data = TreadmillDataDecoder.decode(packet(0x0000 to 2, 1050 to 2))
        assertEquals(10.5, data.speedKmh)
        assertEquals(false, data.moreData)
        assertNull(data.totalDistanceMeters)
    }

    @Test
    fun typicalTreadmillPacket() {
        // Flags: total distance (2), inclination (3), expended energy (7), heart rate (8), elapsed time (10).
        val flags = (1 shl 2) or (1 shl 3) or (1 shl 7) or (1 shl 8) or (1 shl 10)
        val bytes = packet(
            flags to 2,
            1234 to 2, // 12.34 km/h
            70_000 to 3, // 70 km, needs the third byte
            -15 to 2, // -1.5 %
            0 to 2, // ramp angle
            321 to 2, 0 to 2, 0 to 1, // energy: total, per hour, per minute
            142 to 1,
            3_725 to 2,
        )
        val data = TreadmillDataDecoder.decode(bytes)
        assertEquals(12.34, data.speedKmh)
        assertEquals(70_000, data.totalDistanceMeters)
        assertEquals(-1.5, data.inclinePercent)
        assertEquals(321, data.totalEnergyKcal)
        assertEquals(142, data.heartRateBpm)
        assertEquals(3_725.seconds, data.elapsed)
    }

    @Test
    fun everyFieldInOrder() {
        val flags = 0x1FFE // all fields except "more data", so speed is present too
        val bytes = packet(
            flags to 2,
            800 to 2, 750 to 2, // speed, average speed
            1_500 to 3,
            20 to 2, 11 to 2, // incline 2.0 %, ramp 1.1 degrees
            123 to 2, 4 to 2, // elevation up 12.3 m, down 0.4 m
            75 to 1, 80 to 1, // paces
            100 to 2, 600 to 2, 10 to 1,
            130 to 1,
            85 to 1, // 8.5 MET
            900 to 2, 300 to 2,
            -20 to 2, 180 to 2, // force, power
        )
        val data = TreadmillDataDecoder.decode(bytes)
        assertEquals(8.0, data.speedKmh)
        assertEquals(7.5, data.averageSpeedKmh)
        assertEquals(1_500, data.totalDistanceMeters)
        assertEquals(2.0, data.inclinePercent)
        assertEquals(1.1, data.rampAngleDegrees)
        assertEquals(12.3, data.positiveElevationGainMeters)
        assertEquals(0.4, data.negativeElevationGainMeters)
        assertEquals(7.5, data.instantaneousPaceKmPerMin)
        assertEquals(8.0, data.averagePaceKmPerMin)
        assertEquals(600, data.energyPerHourKcal)
        assertEquals(130, data.heartRateBpm)
        assertEquals(8.5, data.metabolicEquivalent)
        assertEquals(900.seconds, data.elapsed)
        assertEquals(300.seconds, data.remaining)
        assertEquals(-20, data.forceOnBeltNewtons)
        assertEquals(180, data.powerWatts)
    }

    @Test
    fun moreDataMeansNoSpeedInThisPacket() {
        val data = TreadmillDataDecoder.decode(packet(((1 shl 0) or (1 shl 8)) to 2, 120 to 1))
        assertEquals(true, data.moreData)
        assertNull(data.speedKmh)
        assertEquals(120, data.heartRateBpm)
    }

    @Test
    fun truncatedPacketIsRejected() {
        assertFailsWith<FtmsFormatException> { TreadmillDataDecoder.decode(packet((1 shl 2) to 2, 500 to 2, 1 to 2)) }
    }

    @Test
    fun assemblerJoinsSplitReadings() {
        val assembler = TreadmillDataAssembler()
        assertNull(assembler.add(TreadmillData(moreData = true, heartRateBpm = 120, totalDistanceMeters = 400)))
        val reading = assembler.add(TreadmillData(speedKmh = 9.0))
        assertEquals(TreadmillData(speedKmh = 9.0, heartRateBpm = 120, totalDistanceMeters = 400), reading)
    }

    @Test
    fun placeholderValuesAreDroppedFromMetrics() {
        val metrics = TreadmillData(speedKmh = 5.0, heartRateBpm = 0, totalEnergyKcal = 0xFFFF).toMetrics()
        assertEquals(DeviceMetrics(speedKmh = 5.0), metrics)
    }
}
