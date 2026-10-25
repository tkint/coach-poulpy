package ch.coachpoulpy.device.ftms

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

/** A treadmill in memory: tests push notifications and drop the link at will. */
private class FakeLink(var failConnects: Int = 0) : BleLink {
    var connects = 0
    var closed = false
    private var dropped = CompletableDeferred<Unit>()
    private var notifications = Channel<ByteArray>(Channel.UNLIMITED)
    private var statuses = Channel<ByteArray>(Channel.UNLIMITED)

    val reads = mapOf(
        FtmsUuids.feature to packet(((1 shl 2) or (1 shl 10)) to 4, 0 to 4),
        FtmsUuids.supportedSpeedRange to packet(50 to 2, 2000 to 2, 10 to 2),
    )

    override suspend fun connect() {
        connects++
        if (failConnects-- > 0) error("Out of range")
        dropped = CompletableDeferred()
        notifications = Channel(Channel.UNLIMITED)
        statuses = Channel(Channel.UNLIMITED)
    }

    override suspend fun awaitDisconnected() = dropped.await()
    override suspend fun disconnect() = drop()
    override suspend fun read(characteristic: Uuid, service: Uuid) = reads[characteristic] ?: error("Not readable")
    override fun observe(characteristic: Uuid, service: Uuid): Flow<ByteArray> = when (characteristic) {
        FtmsUuids.treadmillData -> notifications.receiveAsFlow()
        FtmsUuids.machineStatus -> statuses.receiveAsFlow()
        else -> error("Cannot observe $characteristic")
    }
    override suspend fun write(characteristic: Uuid, value: ByteArray, service: Uuid) = Unit
    override fun close() { closed = true }

    fun notify(bytes: ByteArray) = notifications.trySend(bytes)
    fun status(bytes: ByteArray) = statuses.trySend(bytes)
    fun drop() { dropped.complete(Unit) }
}

class TreadmillConnectionTest {
    @Test
    fun connectsReadsCapabilitiesAndStreams() = runTest {
        val link = FakeLink()
        val connection = TreadmillConnection(link, backgroundScope)
        val readings = mutableListOf<TreadmillData>()
        backgroundScope.launch { connection.readings.collect { readings += it } }
        connection.start()
        runCurrent()

        val state = assertIs<TreadmillState.Connected>(connection.state.value)
        assertEquals(setOf(MachineMetric.TOTAL_DISTANCE, MachineMetric.HEART_RATE), state.capabilities.metrics)
        assertEquals(SupportedRange(0.5, 20.0, 0.1), state.capabilities.speedRange)

        link.notify(packet(0 to 2, 900 to 2))
        link.notify(byteArrayOf(0x04)) // malformed: skipped, the stream goes on
        link.notify(packet(0 to 2, 950 to 2))
        runCurrent()
        assertEquals(listOf(9.0, 9.5), readings.map { it.speedKmh })
        assertEquals(1, connection.malformedPackets)
    }

    @Test
    fun reconnectsAfterADropWithBackoff() = runTest {
        val link = FakeLink()
        val connection = TreadmillConnection(link, backgroundScope)
        connection.start()
        runCurrent()

        link.failConnects = 2
        link.drop()
        runCurrent()
        assertEquals(TreadmillState.Reconnecting(1), connection.state.value) // first retry failed, waiting 1 s

        advanceTimeBy(1.minutes) // advanceUntilIdle ignores work left only in backgroundScope
        runCurrent()
        assertIs<TreadmillState.Connected>(connection.state.value)
        assertEquals(4, link.connects)
    }

    @Test
    fun givesUpWhenTheFirstConnectionKeepsFailing() = runTest {
        val link = FakeLink(failConnects = 10)
        val connection = TreadmillConnection(link, backgroundScope, initialAttempts = 3)
        connection.start()
        advanceTimeBy(1.minutes)
        runCurrent()
        assertEquals(TreadmillState.Failed("Out of range"), connection.state.value)
        assertEquals(3, link.connects)
    }

    @Test
    fun closeStopsReconnecting() = runTest {
        val link = FakeLink()
        val connection = TreadmillConnection(link, backgroundScope)
        connection.start()
        runCurrent()
        connection.close()
        advanceTimeBy(1.minutes)
        runCurrent()
        assertEquals(TreadmillState.Closed, connection.state.value)
        assertEquals(1, link.connects)
        assertEquals(true, link.closed)
    }

    @Test
    fun pauseAndResumeFromTheConsole() = runTest {
        val link = FakeLink()
        val connection = TreadmillConnection(link, backgroundScope)
        connection.start()
        runCurrent()

        link.notify(packet(0 to 2, 1000 to 2))
        runCurrent()
        assertEquals(BeltState.MOVING, connection.beltState.value)

        link.status(packet(0x02 to 1, 0x02 to 1)) // paused by the user
        link.notify(packet(0 to 2, 600 to 2)) // the belt slowing down is still a pause
        link.notify(packet(0 to 2, 0 to 2))
        runCurrent()
        assertEquals(BeltState.PAUSED, connection.beltState.value)
        assertEquals(true, connection.sendsStatus)

        link.status(packet(0x04 to 1))
        runCurrent()
        assertEquals(BeltState.MOVING, connection.beltState.value)
    }

    @Test
    fun stopAndTheSafetyKeyFromTheConsole() = runTest {
        val link = FakeLink()
        val connection = TreadmillConnection(link, backgroundScope)
        connection.start()
        runCurrent()
        link.notify(packet(0 to 2, 1000 to 2))
        link.status(packet(0x02 to 1, 0x01 to 1)) // stopped by the user
        link.notify(packet(0 to 2, 400 to 2))
        runCurrent()
        assertEquals(BeltState.STOPPED, connection.beltState.value)

        link.status(packet(0x04 to 1))
        link.status(packet(0x03 to 1))
        runCurrent()
        assertEquals(BeltState.SAFETY_KEY, connection.beltState.value)

        link.status(packet(0x04 to 1))
        link.status(packet(0x02 to 1)) // no parameter: a pause or a stop, so a pause
        runCurrent()
        assertEquals(BeltState.PAUSED, connection.beltState.value)
    }

    @Test
    fun withoutStatusEventsAStoppedBeltReadsAsStill() = runTest {
        val link = FakeLink()
        val connection = TreadmillConnection(link, backgroundScope)
        connection.start()
        runCurrent()
        link.notify(packet(0 to 2, 0 to 2))
        runCurrent()
        assertEquals(BeltState.STILL, connection.beltState.value)
        link.notify(packet(0 to 2, 300 to 2))
        runCurrent()
        assertEquals(BeltState.MOVING, connection.beltState.value)
    }
}
