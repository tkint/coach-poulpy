package ch.coachpoulpy.shared

import ch.coachpoulpy.device.ftms.BleCentral
import ch.coachpoulpy.device.ftms.BleLink
import ch.coachpoulpy.device.ftms.FoundDevice
import ch.coachpoulpy.device.ftms.FtmsUuids
import ch.coachpoulpy.device.ftms.SensorUuids
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow

/** A treadmill that reports distance, heart rate and speed, and sends what the test pushes. */
internal class FakeTreadmill : BleLink {
    val device = FoundDevice(id = "AA:BB", name = "Test treadmill", rssi = -50, advertisesFtms = true)
    val notifications = Channel<ByteArray>(Channel.UNLIMITED)
    val statuses = Channel<ByteArray>(Channel.UNLIMITED)

    override suspend fun connect() = Unit
    override suspend fun awaitDisconnected() = CompletableDeferred<Unit>().await()
    override suspend fun disconnect() = Unit
    override suspend fun read(characteristic: Uuid, service: Uuid): ByteArray = when (characteristic) {
        FtmsUuids.feature -> bytes(0x04, 0x04, 0, 0, 0, 0, 0, 0) // total distance (bit 2) and heart rate (bit 10)
        else -> error("Not readable")
    }
    override fun observe(characteristic: Uuid, service: Uuid): Flow<ByteArray> =
        when (characteristic) {
            FtmsUuids.treadmillData -> notifications.receiveAsFlow()
            FtmsUuids.machineStatus -> statuses.receiveAsFlow()
            else -> flow { error("Not observed") }
        }
    override suspend fun write(characteristic: Uuid, value: ByteArray, service: Uuid) = Unit
    override fun close() = Unit

    /** Flags: total distance (bit 2) and heart rate (bit 8). */
    fun send(speedCentiKmh: Int, meters: Int, bpm: Int) {
        notifications.trySend(
            bytes(0x04, 0x01, speedCentiKmh and 0xFF, speedCentiKmh shr 8, meters and 0xFF, (meters shr 8) and 0xFF, meters shr 16, bpm),
        )
    }

    /** A Fitness Machine Status event, as pressed on the console: 0x02 0x01 stop, 0x02 0x02 pause, 0x03 safety key, 0x04 start. */
    fun status(vararg values: Int) {
        statuses.trySend(bytes(*values))
    }
}

/** A foot pod or a heart rate band: no battery service, measurements pushed by the test. */
internal class FakeSensor(val device: FoundDevice) : BleLink {
    val notifications = Channel<ByteArray>(Channel.UNLIMITED)

    override suspend fun connect() = Unit
    override suspend fun awaitDisconnected() = CompletableDeferred<Unit>().await()
    override suspend fun disconnect() = Unit
    override suspend fun read(characteristic: Uuid, service: Uuid): ByteArray = error("Not readable")
    override fun observe(characteristic: Uuid, service: Uuid): Flow<ByteArray> = notifications.receiveAsFlow()
    override suspend fun write(characteristic: Uuid, value: ByteArray, service: Uuid) = Unit
    override fun close() = Unit

    /** A pod reading without distance: speed in 1/256 m/s, then cadence. */
    fun pod(metersPerSecond256: Int, cadence: Int) = notifications.trySend(bytes(0x00, metersPerSecond256 and 0xFF, metersPerSecond256 shr 8, cadence))

    fun beat(bpm: Int) = notifications.trySend(bytes(0x00, bpm))
}

/** The phone's Bluetooth with a treadmill, a pod and a band nearby, and an unnamed phone that is hidden. */
internal class FakeCentral : BleCentral {
    val treadmill = FakeTreadmill()
    val pod = FakeSensor(FoundDevice("POD", "Test pod", -55, false, services = setOf(SensorUuids.runningSpeedCadence)))
    val band = FakeSensor(FoundDevice("BAND", "Test band", -60, false, services = setOf(SensorUuids.heartRate)))

    /** What the scan reports; a test can swap it to push advertisements one by one. */
    var advertisements: Flow<FoundDevice> = flowOf(FoundDevice("phone", null, -40, false), treadmill.device, pod.device, band.device)

    override fun scan(): Flow<FoundDevice> = advertisements
    override fun open(device: FoundDevice): BleLink = when (device.id) {
        treadmill.device.id -> treadmill
        pod.device.id -> pod
        else -> band
    }
}

internal fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

internal class MemoryStore : KeyValueStore {
    val values = mutableMapOf<String, String>()
    override fun getString(key: String) = values[key]
    override fun putString(key: String, value: String?) {
        if (value == null) values.remove(key) else values[key] = value
    }
}
