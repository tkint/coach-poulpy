package ch.coachpoulpy.device.ftms

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TreadmillState {
    data object Connecting : TreadmillState
    data class Connected(val capabilities: TreadmillCapabilities) : TreadmillState

    /** The link dropped during use; it is being restored, [attempt] counting from 1. */
    data class Reconnecting(val attempt: Int) : TreadmillState

    /** The first connection failed [TreadmillConnection.initialAttempts] times in a row. */
    data class Failed(val reason: String) : TreadmillState
    data object Closed : TreadmillState
}

/**
 * Keeps one treadmill connected and turns its notifications into readings. On connect
 * it reads what the machine supports, then streams Treadmill Data. If the link drops
 * after a first success it reconnects with growing delays until [close], since a
 * runner mid-session would rather wait than lose the recording.
 */
class TreadmillConnection(
    private val link: BleLink,
    private val scope: CoroutineScope,
    private val initialAttempts: Int = 3,
    private val backoff: (attempt: Int) -> Duration = { attempt -> (1L shl (attempt - 1).coerceAtMost(5)).seconds },
) {
    private val _state = MutableStateFlow<TreadmillState>(TreadmillState.Connecting)
    val state: StateFlow<TreadmillState> = _state.asStateFlow()

    private val _readings = MutableSharedFlow<TreadmillData>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Complete readings, about one a second. Not replayed: a late subscriber starts with the next one. */
    val readings: SharedFlow<TreadmillData> = _readings.asSharedFlow()

    private val belt = BeltMonitor()
    private val _beltState = MutableStateFlow(BeltState.UNKNOWN)

    /** Moving, paused or stopped, from the machine's status events and its speed. */
    val beltState: StateFlow<BeltState> = _beltState.asStateFlow()

    /** Whether the machine has sent any status event, so the UI can tell a pause from a stop. */
    var sendsStatus = false
        private set

    /** Notifications that did not match their flags; worth showing when testing a new machine. */
    var malformedPackets = 0
        private set

    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch { run() }
    }

    private suspend fun run() {
        var everConnected = false
        var failures = 0
        while (true) {
            _state.value = if (everConnected) TreadmillState.Reconnecting(failures + 1) else TreadmillState.Connecting
            val error = try {
                link.connect()
                val capabilities = readCapabilities()
                everConnected = true
                failures = 0
                _state.value = TreadmillState.Connected(capabilities)
                streamUntilDisconnected()
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e
            }
            if (error != null) failures++
            if (!everConnected && failures >= initialAttempts) {
                _state.value = TreadmillState.Failed(error?.message ?: "Could not connect")
                return
            }
            if (failures > 0) delay(backoff(failures))
        }
    }

    private suspend fun readCapabilities(): TreadmillCapabilities {
        // A machine without the optional characteristics still streams data: show what arrives.
        val features = runCatching { CapabilitiesDecoder.features(link.read(FtmsUuids.feature)) }.getOrNull()
            ?: return TreadmillCapabilities.Unknown
        return TreadmillCapabilities(
            metrics = features.first,
            targets = features.second,
            speedRange = runCatching { CapabilitiesDecoder.speedRange(link.read(FtmsUuids.supportedSpeedRange)) }.getOrNull(),
            inclineRange = if (MachineMetric.INCLINATION in features.first) {
                runCatching { CapabilitiesDecoder.inclineRange(link.read(FtmsUuids.supportedInclinationRange)) }.getOrNull()
            } else {
                null
            },
        )
    }

    private suspend fun streamUntilDisconnected() = coroutineScope {
        val assembler = TreadmillDataAssembler()
        val stream = launch {
            link.observe(FtmsUuids.treadmillData).collect { bytes ->
                val packet = try {
                    TreadmillDataDecoder.decode(bytes)
                } catch (e: FtmsFormatException) {
                    malformedPackets++
                    return@collect
                }
                assembler.add(packet)?.let { reading ->
                    belt.onReading(reading.speedKmh)
                    _beltState.value = belt.state
                    _readings.emit(reading)
                }
            }
        }
        val status = launch {
            // Optional characteristic: a machine without it still streams data, so a failure here is not a disconnect.
            try {
                link.observe(FtmsUuids.machineStatus).collect { bytes ->
                    val event = try {
                        MachineStatusDecoder.decode(bytes)
                    } catch (e: FtmsFormatException) {
                        malformedPackets++
                        return@collect
                    }
                    sendsStatus = true
                    belt.onStatus(event)
                    _beltState.value = belt.state
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Not supported by this machine.
            }
        }
        link.awaitDisconnected()
        stream.cancel()
        status.cancel()
    }

    /** Stops reconnecting and drops the link. */
    suspend fun close() {
        job?.cancel()
        job = null
        runCatching { link.disconnect() }
        link.close()
        _state.value = TreadmillState.Closed
    }
}
