package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.device.ftms.BleCentral
import ch.coachpoulpy.device.ftms.BleLink
import ch.coachpoulpy.device.ftms.FoundDevice
import ch.coachpoulpy.device.ftms.FtmsUuids
import ch.coachpoulpy.types.treadmill.TreadmillModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

/** A treadmill that reports distance, heart rate and speed, and sends what the test pushes. */
private class FakeTreadmill : BleCentral, BleLink {
    val device = FoundDevice(id = "AA:BB", name = "Test treadmill", rssi = -50, advertisesFtms = true)
    val notifications = Channel<ByteArray>(Channel.UNLIMITED)

    override fun scan(): Flow<FoundDevice> = flowOf(FoundDevice("phone", null, -40, false), device)
    override fun open(device: FoundDevice): BleLink = this

    override suspend fun connect() = Unit
    override suspend fun awaitDisconnected() = CompletableDeferred<Unit>().await()
    override suspend fun disconnect() = Unit
    override suspend fun read(characteristic: Uuid): ByteArray = when (characteristic) {
        FtmsUuids.feature -> bytes(0x04, 0x04, 0, 0, 0, 0, 0, 0) // total distance (bit 2) and heart rate (bit 10)
        else -> error("Not readable")
    }
    override fun observe(characteristic: Uuid): Flow<ByteArray> =
        if (characteristic == FtmsUuids.treadmillData) notifications.receiveAsFlow() else flow { error("No machine status") }
    override suspend fun write(characteristic: Uuid, value: ByteArray) = Unit
    override fun close() = Unit

    /** Flags: total distance (bit 2) and heart rate (bit 8). */
    fun send(speedCentiKmh: Int, meters: Int, bpm: Int) {
        notifications.trySend(
            bytes(0x04, 0x01, speedCentiKmh and 0xFF, speedCentiKmh shr 8, meters and 0xFF, (meters shr 8) and 0xFF, meters shr 16, bpm),
        )
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}

private class MemoryStore : KeyValueStore {
    val values = mutableMapOf<String, String>()
    override fun getString(key: String) = values[key]
    override fun putString(key: String, value: String?) {
        if (value == null) values.remove(key) else values[key] = value
    }
}

class TreadmillModelTest {
    private fun TestScope.model(treadmill: FakeTreadmill, store: LocalStore, memory: KeyValueStore = MemoryStore()): TreadmillModel {
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(1_700_000_000_000 + testScheduler.currentTime)
        }
        val compiler = TimelineCompiler(WorkoutTypeRegistry(listOf(TreadmillModule)))
        return TreadmillModel(treadmill, store, compiler, memory, backgroundScope, clock)
    }

    @Test
    fun scansConnectsAndRecordsAFreeRun() = runTest {
        val treadmill = FakeTreadmill()
        val store = LocalStore(DriverFactory())
        val model = model(treadmill, store)

        model.startScan()
        runCurrent()
        assertEquals(listOf("Test treadmill"), model.state.value.devices.map { it.name }) // the unnamed phone is hidden

        model.connect("AA:BB")
        runCurrent()
        assertEquals(ConnectionStatus.CONNECTED, model.state.value.connection)

        treadmill.send(speedCentiKmh = 800, meters = 1_000, bpm = 120)
        runCurrent()
        assertEquals(
            listOf(LiveMetric.SPEED, LiveMetric.PACE, LiveMetric.DISTANCE_KM, LiveMetric.HEART_RATE),
            model.state.value.tiles.map { it.metric },
        )
        assertEquals(450.0, model.state.value.tiles[1].value) // 8 km/h is 7:30 per km
        assertEquals(BeltStatus.MOVING, model.state.value.belt)

        model.startRecording("free-run")
        for (i in 1..10) {
            advanceTimeBy(1.seconds)
            treadmill.send(speedCentiKmh = 1000, meters = 1_000 + 3 * i, bpm = 130)
            runCurrent()
        }
        assertNotNull(model.state.value.recording)
        model.stopRecording()

        val state = model.state.value
        assertNull(state.recording)
        val summary = assertNotNull(state.summary)
        assertEquals(10, summary.durationSeconds)
        assertEquals(27.0, summary.distanceMeters) // from the first reading in the recording to the last
        assertEquals(130, summary.maxHeartRateBpm)
    }

    @Test
    fun remembersTheTreadmillUntilForgotten() = runTest {
        val memory = MemoryStore()
        val store = LocalStore(DriverFactory())
        val first = model(FakeTreadmill(), store, memory)
        first.startScan()
        runCurrent()
        first.connect("AA:BB")
        runCurrent()
        assertEquals("Test treadmill", first.state.value.rememberedName)

        // Next launch: the remembered treadmill connects as soon as the scan sees it.
        val second = model(FakeTreadmill(), store, memory)
        assertEquals("Test treadmill", second.state.value.rememberedName)
        second.connectRemembered()
        runCurrent()
        assertEquals(ConnectionStatus.CONNECTED, second.state.value.connection)

        second.forget()
        runCurrent()
        assertEquals(ConnectionStatus.IDLE, second.state.value.connection)
        assertNull(second.state.value.rememberedName)
        assertEquals(emptyMap(), memory.values)
    }
}
