package ch.coachpoulpy.device.ftms

import ch.coachpoulpy.core.model.DeviceMetrics
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
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

/** The standard Bluetooth profiles of foot pods and heart rate bands, and the battery level most of them report. */
object SensorUuids {
    val runningSpeedCadence: Uuid = FtmsUuids.short(0x1814)

    /** RSC Measurement: speed, cadence and optionally stride and distance, about once a second. */
    val rscMeasurement: Uuid = FtmsUuids.short(0x2A53)

    val heartRate: Uuid = FtmsUuids.short(0x180D)

    /** Heart Rate Measurement, notified at each beat or once a second. */
    val heartRateMeasurement: Uuid = FtmsUuids.short(0x2A37)

    val battery: Uuid = FtmsUuids.short(0x180F)
    val batteryLevel: Uuid = FtmsUuids.short(0x2A19)
}

/** Decodes RSC Measurement (0x2A53). An 8-bit flags field says which optional fields follow. */
object RscDecoder {
    private const val STRIDE_LENGTH = 0
    private const val TOTAL_DISTANCE = 1

    fun decode(bytes: ByteArray): DeviceMetrics {
        val reader = ByteReader(bytes)
        val flags = reader.uint8()
        // Speed comes in 1/256 m/s.
        val speedKmh = reader.uint16() / 256.0 * 3.6
        val cadence = reader.uint8()
        if (flags.bit(STRIDE_LENGTH)) reader.uint16()
        val distance = if (flags.bit(TOTAL_DISTANCE)) reader.uint32() / 10.0 else null
        return DeviceMetrics(speedKmh = speedKmh, distanceMeters = distance, cadenceSpm = cadence)
    }
}

/** Decodes Heart Rate Measurement (0x2A37). */
object HeartRateDecoder {
    private const val UINT16_VALUE = 0
    private const val CONTACT_SUPPORTED = 2
    private const val CONTACT_DETECTED = 1

    /** Null when the band says it is not touching the skin, since its number is then meaningless. */
    fun decode(bytes: ByteArray): Int? {
        val reader = ByteReader(bytes)
        val flags = reader.uint8()
        val bpm = if (flags.bit(UINT16_VALUE)) reader.uint16() else reader.uint8()
        if (flags.bit(CONTACT_SUPPORTED) && !flags.bit(CONTACT_DETECTED)) return null
        return bpm.takeIf { it > 0 }
    }
}

private fun Int.bit(index: Int) = (this shr index) and 1 == 1

/**
 * Keeps a foot pod or a heart rate band connected and turns its notifications into
 * readings, reconnecting like [TreadmillConnection] when the link drops.
 */
class SensorConnection(
    private val link: BleLink,
    val kind: DeviceKind,
    private val scope: CoroutineScope,
    private val initialAttempts: Int = 3,
    private val backoff: (attempt: Int) -> Duration = { attempt -> (1L shl (attempt - 1).coerceAtMost(5)).seconds },
) {
    init {
        require(kind != DeviceKind.TREADMILL) { "Treadmills connect through TreadmillConnection" }
    }

    private val _state = MutableStateFlow<TreadmillState>(TreadmillState.Connecting)

    /** The same states as a treadmill's; [TreadmillState.Connected] then carries no capabilities. */
    val state: StateFlow<TreadmillState> = _state.asStateFlow()

    private val _readings = MutableSharedFlow<DeviceMetrics>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val readings: SharedFlow<DeviceMetrics> = _readings.asSharedFlow()

    private val _battery = MutableStateFlow<Int?>(null)

    /** 0 to 100, read on each connection; null when the device does not say. */
    val batteryPercent: StateFlow<Int?> = _battery.asStateFlow()

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
                everConnected = true
                failures = 0
                _battery.value = runCatching { link.read(SensorUuids.batteryLevel, SensorUuids.battery)[0].toInt() and 0xFF }
                    .getOrNull()?.takeIf { it in 0..100 }
                // No machine features: what a pod or a band measures comes from its profile and its readings.
                _state.value = TreadmillState.Connected(TreadmillCapabilities(emptySet(), emptySet()))
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

    private suspend fun streamUntilDisconnected() = coroutineScope {
        val (service, characteristic) = when (kind) {
            DeviceKind.FOOT_POD -> SensorUuids.runningSpeedCadence to SensorUuids.rscMeasurement
            else -> SensorUuids.heartRate to SensorUuids.heartRateMeasurement
        }
        val stream = launch {
            link.observe(characteristic, service).collect { bytes ->
                val reading = try {
                    when (kind) {
                        DeviceKind.FOOT_POD -> RscDecoder.decode(bytes)
                        else -> DeviceMetrics(heartRateBpm = HeartRateDecoder.decode(bytes))
                    }
                } catch (e: FtmsFormatException) {
                    malformedPackets++
                    return@collect
                }
                _readings.emit(reading)
            }
        }
        link.awaitDisconnected()
        stream.cancel()
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
