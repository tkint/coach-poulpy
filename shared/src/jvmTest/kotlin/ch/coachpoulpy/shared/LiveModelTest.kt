package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.device.ftms.BleCentral
import ch.coachpoulpy.device.ftms.BleLink
import ch.coachpoulpy.device.ftms.FoundDevice
import ch.coachpoulpy.device.ftms.FtmsUuids
import ch.coachpoulpy.device.ftms.SensorUuids
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
private class FakeTreadmill : BleLink {
    val device = FoundDevice(id = "AA:BB", name = "Test treadmill", rssi = -50, advertisesFtms = true)
    val notifications = Channel<ByteArray>(Channel.UNLIMITED)

    override suspend fun connect() = Unit
    override suspend fun awaitDisconnected() = CompletableDeferred<Unit>().await()
    override suspend fun disconnect() = Unit
    override suspend fun read(characteristic: Uuid, service: Uuid): ByteArray = when (characteristic) {
        FtmsUuids.feature -> bytes(0x04, 0x04, 0, 0, 0, 0, 0, 0) // total distance (bit 2) and heart rate (bit 10)
        else -> error("Not readable")
    }
    override fun observe(characteristic: Uuid, service: Uuid): Flow<ByteArray> =
        if (characteristic == FtmsUuids.treadmillData) notifications.receiveAsFlow() else flow { error("No machine status") }
    override suspend fun write(characteristic: Uuid, value: ByteArray, service: Uuid) = Unit
    override fun close() = Unit

    /** Flags: total distance (bit 2) and heart rate (bit 8). */
    fun send(speedCentiKmh: Int, meters: Int, bpm: Int) {
        notifications.trySend(
            bytes(0x04, 0x01, speedCentiKmh and 0xFF, speedCentiKmh shr 8, meters and 0xFF, (meters shr 8) and 0xFF, meters shr 16, bpm),
        )
    }
}

/** A foot pod or a heart rate band: no battery service, measurements pushed by the test. */
private class FakeSensor(val device: FoundDevice) : BleLink {
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
private class FakeCentral : BleCentral {
    val treadmill = FakeTreadmill()
    val pod = FakeSensor(FoundDevice("POD", "Test pod", -55, false, services = setOf(SensorUuids.runningSpeedCadence)))
    val band = FakeSensor(FoundDevice("BAND", "Test band", -60, false, services = setOf(SensorUuids.heartRate)))

    override fun scan(): Flow<FoundDevice> = flowOf(FoundDevice("phone", null, -40, false), treadmill.device, pod.device, band.device)
    override fun open(device: FoundDevice): BleLink = when (device.id) {
        treadmill.device.id -> treadmill
        pod.device.id -> pod
        else -> band
    }
}

private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

private class MemoryStore : KeyValueStore {
    val values = mutableMapOf<String, String>()
    override fun getString(key: String) = values[key]
    override fun putString(key: String, value: String?) {
        if (value == null) values.remove(key) else values[key] = value
    }
}

class LiveModelTest {
    private class Models(val devices: DevicesModel, val live: LiveModel, val settings: SettingsModel)

    private fun TestScope.models(central: FakeCentral, store: LocalStore, memory: KeyValueStore = MemoryStore()): Models {
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(1_700_000_000_000 + testScheduler.currentTime)
        }
        val compiler = TimelineCompiler(WorkoutTypeRegistry(listOf(TreadmillModule)))
        val settings = SettingsModel(memory, backgroundScope)
        val devices = DevicesModel(central, memory, backgroundScope, clock)
        return Models(devices, LiveModel(devices, settings.state, store, compiler, memory, backgroundScope, clock), settings)
    }

    private fun TestScope.addTreadmill(models: Models) {
        models.devices.startScan()
        runCurrent()
        models.devices.add("AA:BB")
        models.devices.stopScan()
        runCurrent()
    }

    @Test
    fun addsATreadmillAndRecordsAFreeRun() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))

        m.devices.startScan()
        runCurrent()
        assertEquals(listOf("Test treadmill", "Test pod", "Test band"), m.devices.state.value.found.map { it.name }) // the unnamed phone is hidden
        m.devices.add("AA:BB")
        runCurrent()
        assertEquals(listOf("Test pod", "Test band"), m.devices.state.value.found.map { it.name }) // added ones leave the list
        assertEquals(ConnectionStatus.CONNECTED, m.devices.state.value.known.single().status)
        assertEquals(1, m.live.state.value.connectedCount)

        central.treadmill.send(speedCentiKmh = 800, meters = 1_000, bpm = 120)
        runCurrent()
        assertEquals(
            listOf(LiveMetric.SPEED, LiveMetric.PACE, LiveMetric.DISTANCE, LiveMetric.HEART_RATE, LiveMetric.STEPS_ESTIMATED),
            m.live.state.value.tiles.map { it.metric },
        )
        assertEquals(450.0, m.live.state.value.tiles[1].value) // 8 km/h is 7:30 per km
        assertEquals("Test treadmill", m.live.state.value.tiles[0].source)
        assertEquals(BeltStatus.MOVING, m.live.state.value.belt)

        m.live.startRecording("free-run")
        for (i in 1..10) {
            advanceTimeBy(1.seconds)
            central.treadmill.send(speedCentiKmh = 1000, meters = 1_000 + 3 * i, bpm = 130)
            runCurrent()
        }
        assertNotNull(m.live.state.value.recording)
        m.live.stopRecording()

        val state = m.live.state.value
        assertNull(state.recording)
        val summary = assertNotNull(state.summary)
        assertEquals(10, summary.durationSeconds)
        assertEquals(27.0, summary.distanceMeters) // from the first reading in the recording to the last
        assertEquals(130, summary.maxHeartRateBpm)
    }

    @Test
    fun thePaceKeepsItsTileWhenTheBeltStops() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 800, meters = 0, bpm = 120)
        runCurrent()
        central.treadmill.send(speedCentiKmh = 0, meters = 10, bpm = 120)
        runCurrent()
        val pace = m.live.state.value.tiles.single { it.metric == LiveMetric.PACE }
        assertNull(pace.value)
        assertEquals("/km", pace.unit)
    }

    @Test
    fun imperialUnitsConvertTheTiles() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        m.settings.setUnits(UnitSystem.IMPERIAL)
        central.treadmill.send(speedCentiKmh = 1609, meters = 1609, bpm = 120)
        runCurrent()
        val tiles = m.live.state.value.tiles.associateBy { it.metric }
        assertEquals("mph", tiles.getValue(LiveMetric.SPEED).unit)
        assertEquals(10.0, tiles.getValue(LiveMetric.SPEED).value!!, 0.01)
        assertEquals(1.0, tiles.getValue(LiveMetric.DISTANCE).value!!, 0.001)
        assertEquals(360.0, tiles.getValue(LiveMetric.PACE).value!!, 0.5) // 6:00 per mile
    }

    @Test
    fun thePodAndTheBandWinOverTheTreadmillUnlessTheUserPicks() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        m.devices.startScan()
        runCurrent()
        listOf("AA:BB", "POD", "BAND").forEach { m.devices.add(it) }
        m.devices.stopScan()
        runCurrent()
        assertEquals(3, m.devices.state.value.connectedCount, "connected: ${m.devices.state.value.known}")

        central.treadmill.send(speedCentiKmh = 800, meters = 100, bpm = 110)
        central.band.beat(150)
        central.pod.pod(metersPerSecond256 = 640, cadence = 170) // 9 km/h
        runCurrent()
        var tiles = m.live.state.value.tiles.associateBy { it.metric }
        val seen = "tiles: ${m.live.state.value.tiles}, devices: ${m.devices.state.value.known}"
        assertEquals(9.0, tiles.getValue(LiveMetric.SPEED).value!!, 0.01, seen)
        assertEquals("Test pod", tiles.getValue(LiveMetric.SPEED).source, seen)
        assertEquals(150.0, tiles.getValue(LiveMetric.HEART_RATE).value, seen)
        assertEquals("Test band", tiles.getValue(LiveMetric.HEART_RATE).source, seen)
        assertEquals(170.0, tiles.getValue(LiveMetric.CADENCE).value, seen)
        assertNull(tiles[LiveMetric.STEPS_ESTIMATED]) // the pod counts real steps
        val treadmill = m.devices.state.value.known.first { it.key == "AA:BB" }
        assertEquals(listOf(Quantity.SPEED, Quantity.HEART_RATE), treadmill.provides, seen)
        assertEquals(emptyList(), treadmill.usedFor, seen)
        assertEquals(setOf(Quantity.SPEED, Quantity.HEART_RATE), m.devices.state.value.sources.map { it.quantity }.toSet(), seen)

        m.devices.setSource(Quantity.SPEED, "AA:BB")
        central.treadmill.send(speedCentiKmh = 800, meters = 110, bpm = 110)
        runCurrent()
        tiles = m.live.state.value.tiles.associateBy { it.metric }
        assertEquals(8.0, tiles.getValue(LiveMetric.SPEED).value, "tiles: ${m.live.state.value.tiles}")
        assertEquals("Test treadmill", tiles.getValue(LiveMetric.SPEED).source, "tiles: ${m.live.state.value.tiles}")
        assertEquals(0.11, tiles.getValue(LiveMetric.DISTANCE).value, "tiles: ${m.live.state.value.tiles}")
    }

    @Test
    fun aPodWithoutDistanceGetsOneFromItsSpeed() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        m.devices.startScan()
        runCurrent()
        m.devices.add("POD")
        runCurrent()
        repeat(11) {
            central.pod.pod(metersPerSecond256 = 768, cadence = 170) // 3 m/s
            runCurrent()
            advanceTimeBy(1.seconds)
        }
        assertEquals(0.03, m.live.state.value.tiles.single { it.metric == LiveMetric.DISTANCE }.value!!, 0.0001)
    }

    @Test
    fun remembersAddedDevicesUntilForgotten() = runTest {
        val memory = MemoryStore()
        val store = LocalStore(DriverFactory())
        addTreadmill(models(FakeCentral(), store, memory))

        // Next launch: the added treadmill connects as soon as the scan sees it.
        val second = models(FakeCentral(), store, memory)
        assertEquals(listOf("Test treadmill"), second.devices.state.value.known.map { it.name })
        assertEquals(ConnectionStatus.IDLE, second.devices.state.value.known.single().status)
        second.devices.connectAutomatically()
        runCurrent()
        assertEquals(ConnectionStatus.CONNECTED, second.devices.state.value.known.single().status)

        second.devices.forget("AA:BB")
        runCurrent()
        assertEquals(emptyList(), second.devices.state.value.known)
        assertEquals(0, second.live.state.value.connectedCount)
        assertEquals("", memory.values["devices.known"])
    }

    @Test
    fun theTreadmillRememberedBeforeDevicesBecomesAnAddedDevice() = runTest {
        val memory = MemoryStore().apply {
            values["treadmill.key"] = "AA:BB"
            values["treadmill.name"] = "Old treadmill"
        }
        val m = models(FakeCentral(), LocalStore(DriverFactory()), memory)
        assertEquals(listOf("Old treadmill"), m.devices.state.value.known.map { it.name })
        assertNull(memory.values["treadmill.key"])
    }

    @Test
    fun anAddedDeviceThatStaysOffStopsConnecting() = runTest {
        val memory = MemoryStore()
        val store = LocalStore(DriverFactory())
        addTreadmill(models(FakeCentral(), store, memory))
        val nobodyNearby = object : BleCentral by FakeCentral() {
            override fun scan(): Flow<FoundDevice> = flow { CompletableDeferred<Unit>().await() }
        }
        val devices = DevicesModel(nobodyNearby, memory, backgroundScope)
        devices.connectAutomatically()
        runCurrent()
        assertEquals(ConnectionStatus.CONNECTING, devices.state.value.known.single().status)
        assertEquals(true, devices.state.value.scanning)
        advanceTimeBy(31.seconds)
        assertEquals(ConnectionStatus.IDLE, devices.state.value.known.single().status)
        assertEquals(false, devices.state.value.scanning)
    }

    @Test
    fun recordingFollowsTheBeltWhenSyncIsOn() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 800, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("free-run")
        runCurrent()

        central.treadmill.send(speedCentiKmh = 0, meters = 10, bpm = 120)
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)

        central.treadmill.send(speedCentiKmh = 500, meters = 10, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)

        // Switched off: the belt stopping no longer pauses the recording.
        m.live.setSyncPause(false)
        central.treadmill.send(speedCentiKmh = 0, meters = 12, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)
    }

    @Test
    fun aPauseTheUserMadeIsNotUndoneByTheBelt() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 0, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("free-run") // the belt is still: the timer waits
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)

        central.treadmill.send(speedCentiKmh = 600, meters = 0, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)

        m.live.pauseRecording()
        central.treadmill.send(speedCentiKmh = 700, meters = 2, bpm = 120)
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)
    }
}
