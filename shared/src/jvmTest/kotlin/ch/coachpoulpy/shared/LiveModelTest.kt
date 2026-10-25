package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.device.ftms.BleCentral
import ch.coachpoulpy.device.ftms.FoundDevice
import ch.coachpoulpy.types.treadmill.TreadmillModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

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

        m.live.startRecording("exercise:walk")
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
        // A pod or a band only claims what its profile measures, not every treadmill metric.
        assertEquals(listOf(Quantity.SPEED, Quantity.CADENCE), m.devices.state.value.known.first { it.key == "POD" }.provides, seen)
        assertEquals(listOf(Quantity.HEART_RATE), m.devices.state.value.known.first { it.key == "BAND" }.provides, seen)
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
    fun liveIsReadOnlyWhileAnAddedDeviceConnects() = runTest {
        val memory = MemoryStore()
        val store = LocalStore(DriverFactory())
        val nothingAdded = models(FakeCentral(), store, MemoryStore())
        assertEquals(LiveConnection.EMPTY, nothingAdded.live.state.value.connection)

        addTreadmill(models(FakeCentral(), store, memory))
        // Next launch, the treadmill is still off: the scan has not seen it yet.
        val treadmillOff = object : BleCentral by FakeCentral() {
            override fun scan(): Flow<FoundDevice> = flow { CompletableDeferred<Unit>().await() }
        }
        val compiler = TimelineCompiler(WorkoutTypeRegistry(listOf(TreadmillModule)))
        val settings = SettingsModel(memory, backgroundScope)
        val devices = DevicesModel(treadmillOff, memory, backgroundScope)
        val live = LiveModel(devices, settings.state, store, compiler, memory, backgroundScope)
        devices.connectAutomatically()
        runCurrent()
        val connecting = live.state.value
        assertEquals(LiveConnection.CONNECTING, connecting.connection)
        assertEquals(listOf("Test treadmill"), connecting.connecting)
        // The tiles a treadmill usually fills, waiting with a dash.
        assertEquals(
            listOf(LiveMetric.SPEED, LiveMetric.PACE, LiveMetric.DISTANCE, LiveMetric.STEPS_ESTIMATED),
            connecting.tiles.map { it.metric },
        )
        assertEquals(listOf(null, null, null, null), connecting.tiles.map { it.value })
        live.startRecording("exercise:walk")
        assertNull(live.state.value.recording)

        // It never showed up: back to the empty state.
        advanceTimeBy(31.seconds)
        assertEquals(LiveConnection.EMPTY, live.state.value.connection)
        assertEquals(emptyList(), live.state.value.connecting)
        assertEquals(emptyList(), live.state.value.tiles)
    }

    @Test
    fun justLaunchedLiveWaitsForTheDevicesThatConnectOnTheirOwn() = runTest {
        val memory = MemoryStore()
        val store = LocalStore(DriverFactory())
        addTreadmill(models(FakeCentral(), store, memory))

        // Next launch: before the app gets to connect, Live already waits instead of flashing its empty state.
        val launched = models(FakeCentral(), store, memory)
        assertEquals(LiveConnection.CONNECTING, launched.live.state.value.connection)
        assertEquals(listOf("Test treadmill"), launched.live.state.value.connecting)
        launched.devices.connectAutomatically()
        runCurrent()
        assertEquals(LiveConnection.CONNECTED, launched.live.state.value.connection)

        // Bluetooth off: the app never connects, and Live gives up waiting after a moment.
        val bluetoothOff = models(FakeCentral(), store, memory)
        runCurrent()
        assertEquals(LiveConnection.CONNECTING, bluetoothOff.live.state.value.connection)
        advanceTimeBy(4.seconds)
        assertEquals(LiveConnection.EMPTY, bluetoothOff.live.state.value.connection)
    }

    @Test
    fun liveTurnsLiveAsSoonAsADeviceConnects() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        assertEquals(LiveConnection.CONNECTED, m.live.state.value.connection)
        assertEquals(emptyList(), m.live.state.value.connecting)
        m.live.startRecording("exercise:walk") // no reading yet: the belt has not started
        assertNotNull(m.live.state.value.waitingFor)
    }

    @Test
    fun recordingFollowsTheBeltWhenFollowIsOn() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 800, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk") // the belt already moves: no waiting
        runCurrent()
        assertNull(m.live.state.value.waitingFor)

        central.treadmill.send(speedCentiKmh = 0, meters = 10, bpm = 120)
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)

        central.treadmill.send(speedCentiKmh = 500, meters = 10, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)

        // Switched off: the belt stopping no longer pauses the recording.
        m.live.setFollowTreadmill(false)
        central.treadmill.send(speedCentiKmh = 0, meters = 12, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)
    }

    @Test
    fun startWaitsForTheBeltToMove() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 0, meters = 0, bpm = 120)
        runCurrent()

        m.live.startRecording("exercise:walk")
        assertEquals("Walk", m.live.state.value.waitingFor?.name)
        assertNull(m.live.state.value.recording)
        m.live.cancelWaiting()
        assertNull(m.live.state.value.waitingFor)

        m.live.startRecording("exercise:walk")
        advanceTimeBy(30.seconds)
        central.treadmill.send(speedCentiKmh = 400, meters = 0, bpm = 120)
        runCurrent()
        assertNull(m.live.state.value.waitingFor)
        assertEquals(false, m.live.state.value.recording?.paused)
        m.live.stopRecording()
        runCurrent()
        // The session starts with the belt, not with the tap.
        assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000 + 30_000), store.finishedSessions().first().single().execution.startedAt)
    }

    @Test
    fun startNowRecordsWithoutTheBelt() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 0, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk")
        m.live.startNow()
        assertNull(m.live.state.value.waitingFor)
        assertEquals(false, m.live.state.value.recording?.paused)
    }

    @Test
    fun aPauseTheUserMadeIsNotUndoneByTheBelt() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 0, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk") // the belt is still: Start waits
        central.treadmill.send(speedCentiKmh = 600, meters = 0, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)

        m.live.pauseRecording()
        central.treadmill.send(speedCentiKmh = 700, meters = 2, bpm = 120)
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)
    }

    @Test
    fun pauseResumeAndStopOnTheConsole() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 800, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:run")
        advanceTimeBy(60.seconds)

        central.treadmill.status(0x02, 0x02)
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)
        central.treadmill.status(0x04)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)

        central.treadmill.status(0x02, 0x01)
        runCurrent()
        assertNull(m.live.state.value.recording)
        assertNotNull(m.live.state.value.summary)
    }

    @Test
    fun aStopOnTheConsoleSkipsTheRestOfAWorkout() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 600, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("demo-intervals")
        advanceTimeBy(60.seconds)
        central.treadmill.status(0x02, 0x01)
        runCurrent()
        assertNull(m.live.state.value.recording)
        assertNotNull(m.live.state.value.summary)
    }

    @Test
    fun aBeltStillForLongAsksThenFinishesWhenItLastMoved() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 500, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk")
        advanceTimeBy(5.minutes)
        // A treadmill without status events: 0 km/h is a pause, never a finish.
        central.treadmill.send(speedCentiKmh = 0, meters = 400, bpm = 100)
        runCurrent()
        val stoppedAt = Instant.fromEpochMilliseconds(1_700_000_000_000 + testScheduler.currentTime)
        assertEquals(true, m.live.state.value.recording?.paused)

        advanceTimeBy(9.minutes)
        assertNull(m.live.state.value.finishPrompt)
        advanceTimeBy(1.minutes + 1.seconds)
        val prompt = assertNotNull(m.live.state.value.finishPrompt)
        assertEquals(FinishReason.BELT_STILL, prompt.reason)
        assertEquals(10, prompt.sinceMinutes)
        assertEquals(10, prompt.autoFinishMinutes)
        assertEquals(300, prompt.endsAtSeconds)

        advanceTimeBy(10.minutes)
        assertNull(m.live.state.value.recording)
        runCurrent()
        val saved = store.finishedSessions().first().single()
        assertEquals(stoppedAt, saved.execution.endedAt)
        assertEquals(5.minutes, saved.totals?.active)
    }

    @Test
    fun keepGoingWaitsAnotherFullTenMinutes() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 500, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk")
        central.treadmill.send(speedCentiKmh = 0, meters = 10, bpm = 100)
        runCurrent()
        advanceTimeBy(10.minutes + 1.seconds)
        assertNotNull(m.live.state.value.finishPrompt)

        m.live.keepGoing()
        assertNull(m.live.state.value.finishPrompt)
        advanceTimeBy(9.minutes)
        assertNull(m.live.state.value.finishPrompt)
        assertNotNull(m.live.state.value.recording)

        // The belt moving again also puts the question away.
        advanceTimeBy(1.minutes + 1.seconds)
        assertNotNull(m.live.state.value.finishPrompt)
        central.treadmill.send(speedCentiKmh = 400, meters = 10, bpm = 110)
        runCurrent()
        assertNull(m.live.state.value.finishPrompt)
    }

    @Test
    fun theSafetyKeyAsksAtOnce() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        central.treadmill.send(speedCentiKmh = 500, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk")
        central.treadmill.status(0x03)
        runCurrent()
        assertEquals(true, m.live.state.value.recording?.paused)
        assertEquals(FinishReason.BELT_STILL, m.live.state.value.finishPrompt?.reason)
    }

    @Test
    fun aTreadmillGoneForTwoMinutesAsksEvenWithFollowOff() = runTest {
        val central = FakeCentral()
        val m = models(central, LocalStore(DriverFactory()))
        addTreadmill(m)
        m.live.setFollowTreadmill(false)
        central.treadmill.send(speedCentiKmh = 500, meters = 0, bpm = 120)
        runCurrent()
        m.live.startRecording("exercise:walk")
        advanceTimeBy(3.minutes)
        m.devices.disconnect("AA:BB")
        runCurrent()
        advanceTimeBy(2.minutes + 1.seconds)
        val prompt = assertNotNull(m.live.state.value.finishPrompt)
        assertEquals(FinishReason.TREADMILL_LOST, prompt.reason)
        assertEquals(180, prompt.endsAtSeconds)

        m.live.stopRecording()
        assertNull(m.live.state.value.finishPrompt)
    }

    @Test
    fun theOldPauseSwitchSeedsFollowTheTreadmill() = runTest {
        val memory = MemoryStore()
        memory.putString("treadmill.syncPause", "false")
        val m = models(FakeCentral(), LocalStore(DriverFactory()), memory)
        assertEquals(false, m.live.state.value.followTreadmill)
        m.live.setFollowTreadmill(true)
        assertEquals("true", memory.getString("treadmill.follow"))
    }

    @Test
    fun offersEveryTreadmillWalkAndRunThenTheWorkouts() = runTest {
        val m = models(FakeCentral(), LocalStore(DriverFactory()))
        runCurrent()
        val state = m.live.state.value
        assertEquals(
            listOf("exercise:walk-with-support", "exercise:gentle-walk", "exercise:walk", "exercise:brisk-walk"),
            state.walking.map { it.key },
        )
        assertEquals(listOf("exercise:easy-run", "exercise:run"), state.running.map { it.key })
        assertEquals("demo-intervals", state.workouts.first().key)
        assertEquals(listOf("Walk", "Brisk walk", "Easy run"), state.quickPicks.map { it.name })
        assertEquals("exercise:walk", state.selected)
        assertEquals("Your everyday pace", state.walking[2].note)
    }

    @Test
    fun theChoicesAreInTheUsersLanguage() = runTest {
        val central = FakeCentral()
        val memory = MemoryStore()
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(1_700_000_000_000 + testScheduler.currentTime)
        }
        val settings = SettingsModel(memory, backgroundScope)
        val devices = DevicesModel(central, memory, backgroundScope, clock)
        val compiler = TimelineCompiler(WorkoutTypeRegistry(listOf(TreadmillModule)))
        val live = LiveModel(devices, settings.state, LocalStore(DriverFactory()), compiler, memory, backgroundScope, clock, languageCode = "fr")
        runCurrent()
        assertEquals(listOf("Marche", "Marche rapide", "Course facile"), live.state.value.quickPicks.map { it.name })
        assertEquals("Fractionné de démo", live.state.value.workouts.first().name)
    }

    @Test
    fun aPickFromMoreTakesTheLastChip() {
        val available = listOf("exercise:walk", "exercise:brisk-walk", "exercise:easy-run", "exercise:run", "demo-intervals")
        val recents = listOf("exercise:walk", "exercise:brisk-walk", "exercise:easy-run")
        assertEquals(recents, LiveModel.quickPicks(recents, "exercise:brisk-walk", available))
        assertEquals(listOf("exercise:walk", "exercise:brisk-walk", "exercise:run"), LiveModel.quickPicks(recents, "exercise:run", available))
        // A recent that is gone, such as a deleted workout, gives its place to a default.
        assertEquals(recents, LiveModel.quickPicks(listOf("gone", "exercise:walk"), "exercise:walk", available))
    }

    @Test
    fun aFreeRecordingKeepsTheExerciseAndBecomesARecentPick() = runTest {
        val central = FakeCentral()
        val memory = MemoryStore()
        val store = LocalStore(DriverFactory())
        val m = models(central, store, memory)
        addTreadmill(m)
        m.live.select("exercise:run")
        assertEquals(listOf("Walk", "Brisk walk", "Run"), m.live.state.value.quickPicks.map { it.name })

        m.live.startRecording("exercise:run")
        advanceTimeBy(5.seconds)
        m.live.stopRecording()
        runCurrent()

        val saved = store.finishedSessions().first().single()
        assertEquals("Run", saved.title)
        assertEquals(listOf("Run", "Walk", "Brisk walk"), m.live.state.value.quickPicks.map { it.name })
        assertEquals("exercise:run,exercise:walk,exercise:brisk-walk", memory.getString("live.recent"))
    }
}
