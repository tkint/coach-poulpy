package ch.coachpoulpy.device.ftms

import ch.coachpoulpy.core.model.DeviceMetrics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

/** A heart rate band with a battery: tests push measurements. */
private class FakeBand : BleLink {
    val notifications = Channel<ByteArray>(Channel.UNLIMITED)
    val observed = mutableListOf<Pair<Uuid, Uuid>>()

    override suspend fun connect() = Unit
    override suspend fun awaitDisconnected() = CompletableDeferred<Unit>().await()
    override suspend fun disconnect() = Unit
    override suspend fun read(characteristic: Uuid, service: Uuid) =
        if (service == SensorUuids.battery && characteristic == SensorUuids.batteryLevel) byteArrayOf(64) else error("Not readable")
    override fun observe(characteristic: Uuid, service: Uuid): Flow<ByteArray> {
        observed += service to characteristic
        return notifications.receiveAsFlow()
    }
    override suspend fun write(characteristic: Uuid, value: ByteArray, service: Uuid) = Unit
    override fun close() = Unit
}

class SensorsTest {
    @Test
    fun decodesAFootPodWithDistance() {
        // Flags: stride (bit 0), distance (bit 1), running (bit 2). 2.5 m/s is 9 km/h.
        val metrics = RscDecoder.decode(packet(0x07 to 1, 640 to 2, 172 to 1, 140 to 2, 12_345 to 4))
        assertEquals(9.0, metrics.speedKmh!!, 0.001)
        assertEquals(172, metrics.cadenceSpm)
        assertEquals(1234.5, metrics.distanceMeters)
    }

    @Test
    fun aFootPodWithoutDistanceLeavesItOut() {
        val metrics = RscDecoder.decode(packet(0x00 to 1, 512 to 2, 160 to 1))
        assertNull(metrics.distanceMeters)
        assertEquals(160, metrics.cadenceSpm)
    }

    @Test
    fun decodesHeartRateInBothSizes() {
        assertEquals(72, HeartRateDecoder.decode(packet(0x00 to 1, 72 to 1)))
        assertEquals(301, HeartRateDecoder.decode(packet(0x01 to 1, 301 to 2)))
        // Contact supported (bit 2) but not detected (bit 1 clear): the band is off the skin.
        assertNull(HeartRateDecoder.decode(packet(0x04 to 1, 80 to 1)))
        assertEquals(80, HeartRateDecoder.decode(packet(0x06 to 1, 80 to 1)))
    }

    @Test
    fun tellsKindsApartFromTheAdvertisement() {
        fun found(vararg services: Uuid) = FoundDevice("id", "name", -50, advertisesFtms = false, services = services.toSet())
        assertEquals(DeviceKind.FOOT_POD, found(SensorUuids.runningSpeedCadence).kind)
        assertEquals(DeviceKind.HEART_RATE, found(SensorUuids.heartRate, SensorUuids.battery).kind)
        assertEquals(DeviceKind.TREADMILL, found(FtmsUuids.service, SensorUuids.heartRate).kind)
        assertNull(found().kind)
    }

    @Test
    fun aBandStreamsHeartRateAndReadsItsBattery() = runTest {
        val band = FakeBand()
        val connection = SensorConnection(band, DeviceKind.HEART_RATE, backgroundScope)
        val readings = mutableListOf<DeviceMetrics>()
        backgroundScope.launch { connection.readings.collect { readings += it } }
        connection.start()
        runCurrent()

        assertIs<TreadmillState.Connected>(connection.state.value)
        assertEquals(64, connection.batteryPercent.value)
        assertEquals(listOf(SensorUuids.heartRate to SensorUuids.heartRateMeasurement), band.observed)

        band.notifications.trySend(packet(0x00 to 1, 141 to 1))
        band.notifications.trySend(byteArrayOf()) // malformed: skipped
        runCurrent()
        assertEquals(listOf(141), readings.map { it.heartRateBpm })
        assertEquals(1, connection.malformedPackets)
    }
}
