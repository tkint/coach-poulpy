package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.device.ftms.BleCentral
import ch.coachpoulpy.device.ftms.DeviceKind
import ch.coachpoulpy.device.ftms.FoundDevice
import ch.coachpoulpy.device.ftms.MachineMetric
import ch.coachpoulpy.device.ftms.SensorConnection
import ch.coachpoulpy.device.ftms.TreadmillConnection
import ch.coachpoulpy.device.ftms.TreadmillState
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConnectionStatus { IDLE, CONNECTING, CONNECTED, RECONNECTING, FAILED }

/** Whether the belt moves. PAUSED needs a treadmill that reports pauses; others show STOPPED. */
enum class BeltStatus { UNKNOWN, MOVING, PAUSED, STOPPED }

/** What a device can measure, and what a data source is picked for. SPEED also covers pace and distance. */
enum class Quantity { SPEED, HEART_RATE, CADENCE, INCLINE, ENERGY, POWER }

/** Small values kept between launches: SharedPreferences on Android, UserDefaults on iOS. */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
}

/** A device the user added. [key] is a MAC address on Android, a UUID on iOS. */
data class KnownDeviceUi(
    val key: String,
    val name: String,
    val kind: DeviceKind,
    val status: ConnectionStatus,
    val batteryPercent: Int?,
    /** Connects on its own when the app opens and the device is nearby. */
    val autoConnect: Boolean,
    /** What it measures, as far as the app knows: its kind's usual numbers and any it has sent. */
    val provides: List<Quantity>,
    /** Of [provides], what the live screen takes from this device right now. */
    val usedFor: List<Quantity>,
    /** One per quantity in [provides], for the device page. */
    val measures: List<MeasureUi>,
    /** Why connecting failed, in English: shown as a detail under a localized title. */
    val error: String?,
)

/** A device the scan found that is not added yet. [kind] null: it lists no known service, so it is tried as a treadmill. */
data class FoundDeviceUi(val key: String, val name: String, val kind: DeviceKind?, val rssi: Int)

/**
 * One quantity a device provides, on its page. [used]: this device is the metric source for it among the
 * added devices, by the user's choice or else automatically. [choosable]: another added device provides it
 * too, so the page offers a "Use this device" switch.
 */
data class MeasureUi(val quantity: Quantity, val used: Boolean, val choosable: Boolean)

data class SourceOption(val key: String, val name: String)

/** The device used for one [quantity]: [choiceKey] null is automatic. */
data class SourceUi(val quantity: Quantity, val choiceKey: String?, val options: List<SourceOption>)

data class DevicesUiState(
    val known: List<KnownDeviceUi> = emptyList(),
    val scanning: Boolean = false,
    val found: List<FoundDeviceUi> = emptyList(),
    val error: String? = null,
    /** Only quantities that more than one added device measures: the others have nothing to choose. */
    val sources: List<SourceUi> = emptyList(),
    val connectedCount: Int = 0,
)

/** The merged reading of every connected device, with the name of the device each number came from. */
internal data class MergedReading(val metrics: DeviceMetrics, val sources: Map<Quantity, String>)

/**
 * Settings › Devices, and the source of the Live tab's numbers: the devices the user
 * added, several connected at once, and one merged reading where each number comes
 * from the device picked for it (automatic: what is on the runner's body wins).
 */
class DevicesModel(
    private val central: BleCentral,
    private val memory: KeyValueStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val clock: Clock = Clock.System,
) {
    private data class Known(val key: String, val name: String, val kind: DeviceKind, val autoConnect: Boolean)

    /** A connected (or connecting) device and what it last sent. */
    private inner class Link(val known: Known) {
        var treadmill: TreadmillConnection? = null
        var sensor: SensorConnection? = null
        var status = ConnectionStatus.CONNECTING
        var battery: Int? = null
        var latest: DeviceMetrics? = null
        var belt = BeltStatus.UNKNOWN
        val seen = mutableSetOf<Quantity>()

        /** Metres from the speed, for a pod that sends no distance of its own. */
        var odometer = 0.0
        var lastAt: Instant? = null
        var jobs = emptyList<Job>()
    }

    private var known: List<Known> = loadKnown()
    private val links = mutableMapOf<String, Link>()
    private val failures = mutableMapOf<String, String>()

    /** In the order first seen, so a row never moves under the user's finger while the scan runs. */
    private val found = linkedMapOf<String, FoundDevice>()

    /** Added devices to connect as soon as the scan sees them. */
    private val pending = mutableSetOf<String>()
    private var scanJob: Job? = null
    private var pendingTimeout: Job? = null

    /** True while the Add screen is open: the scan then runs until it is left. */
    private var userScan = false
    private var autoConnectedOnce = false

    private val _state = MutableStateFlow(DevicesUiState())
    val state: StateFlow<DevicesUiState> = _state.asStateFlow()

    private val _merged = MutableSharedFlow<MergedReading>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    internal val merged: SharedFlow<MergedReading> = _merged.asSharedFlow()

    private val _belt = MutableStateFlow(BeltStatus.UNKNOWN)

    /** The connected treadmill's belt; UNKNOWN without one. */
    val belt: StateFlow<BeltStatus> = _belt.asStateFlow()

    /** What the connected devices measure together, for the Live tab's tiles. */
    private val _available = MutableStateFlow(emptySet<Quantity>())
    internal val available: StateFlow<Set<Quantity>> = _available.asStateFlow()

    val hasTreadmill: Boolean get() = links.values.any { it.known.kind == DeviceKind.TREADMILL && it.status == ConnectionStatus.CONNECTED }

    init {
        publish()
    }

    /**
     * Connects every added device marked to connect on its own, once per launch, without
     * asking for anything: the apps call it when Bluetooth is already allowed and on.
     */
    fun connectAutomatically() {
        if (autoConnectedOnce) return
        autoConnectedOnce = true
        known.filter { it.autoConnect }.forEach { connect(it.key) }
    }

    /** Scans for devices to add, until [stopScan]. Added devices are left out of [DevicesUiState.found]. */
    fun startScan() {
        userScan = true
        // A new scan may order the list afresh: known kinds first, then the strongest signal.
        val sorted = found.values.sortedWith(compareByDescending<FoundDevice> { it.kind != null }.thenByDescending { it.rssi })
        found.clear()
        sorted.forEach { found[it.id] = it }
        publish()
        scan()
    }

    fun stopScan() {
        userScan = false
        if (pending.isEmpty()) stopScanning()
    }

    /**
     * Phones cannot connect to a Bluetooth address without having seen it in a scan
     * since launch, so connecting to an added device goes through the scan on both platforms.
     */
    fun connect(key: String) {
        val device = known.firstOrNull { it.key == key } ?: return
        if (links.containsKey(key)) return
        failures.remove(key)
        val seen = found[key]
        if (seen != null) {
            open(device, seen)
            return
        }
        pending += key
        scan()
        // A device that is off would otherwise read as connecting forever.
        pendingTimeout?.cancel()
        pendingTimeout = scope.launch {
            delay(PENDING_TIMEOUT)
            pending.clear()
            if (!userScan) stopScanning()
            publish()
        }
        publish()
    }

    /** Adds a device the scan found and connects to it. */
    fun add(key: String) {
        val device = found[key] ?: return
        if (known.none { it.key == key }) {
            known = known + Known(key, device.name ?: key, device.kind ?: DeviceKind.TREADMILL, autoConnect = true)
            saveKnown()
        }
        connect(key)
    }

    fun disconnect(key: String) {
        pending -= key
        val link = links.remove(key) ?: run { publish(); return }
        link.jobs.forEach { it.cancel() }
        scope.launch {
            link.treadmill?.close()
            link.sensor?.close()
        }
        publish()
    }

    fun forget(key: String) {
        disconnect(key)
        failures.remove(key)
        known = known.filterNot { it.key == key }
        saveKnown()
        Quantity.entries.forEach { if (memory.getString(sourceKey(it)) == key) memory.putString(sourceKey(it), null) }
        publish()
    }

    fun setAutoConnect(key: String, on: Boolean) {
        known = known.map { if (it.key == key) it.copy(autoConnect = on) else it }
        saveKnown()
        publish()
    }

    /** Null goes back to automatic. */
    fun setSource(quantity: Quantity, key: String?) {
        memory.putString(sourceKey(quantity), key)
        publish()
    }

    /**
     * The device page's "Use this device" switch: the same setting as [setSource]. Off hands [quantity] back to
     * automatic; when automatic would pick this device anyway, the next added device that provides it is picked,
     * so the switch never stays on after being turned off.
     */
    fun useForMeasure(key: String, quantity: Quantity, on: Boolean) {
        if (on) return setSource(quantity, key)
        if (chosen(quantity) != key) return
        val automatic = automatic(quantity, known)
        setSource(quantity, if (automatic?.key == key) automatic(quantity, known.filter { it.key != key })?.key else null)
    }

    private fun scan() {
        if (scanJob != null) return
        _state.update { it.copy(scanning = true, error = null) }
        scanJob = scope.launch {
            val self = coroutineContext[Job]
            try {
                central.scan().collect { device ->
                    // Unnamed devices with none of the known services are phones, earbuds and the like.
                    if (device.name == null && device.kind == null) return@collect
                    found[device.id] = device
                    if (device.id in pending) {
                        pending -= device.id
                        known.firstOrNull { it.key == device.id }?.let { open(it, device) }
                        if (pending.isEmpty() && !userScan) stopScanning()
                    }
                    publish()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                if (scanJob === self) {
                    scanJob = null
                    _state.update { it.copy(scanning = false) }
                }
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        pendingTimeout?.cancel()
        _state.update { it.copy(scanning = false) }
    }

    private fun open(device: Known, seen: FoundDevice) {
        if (links.containsKey(device.key)) return
        val bleLink = try {
            central.open(seen)
        } catch (e: Exception) {
            failures[device.key] = e.message ?: e.toString()
            publish()
            return
        }
        val link = Link(device)
        links[device.key] = link
        link.seen += defaults(device.kind)
        if (device.kind == DeviceKind.TREADMILL) {
            val connection = TreadmillConnection(bleLink, scope)
            link.treadmill = connection
            link.jobs = listOf(
                scope.launch { connection.state.collect { onState(link, it) } },
                scope.launch {
                    connection.beltState.collect { belt ->
                        link.belt = BeltStatus.valueOf(belt.name)
                        _belt.value = treadmillBelt()
                    }
                },
                scope.launch { connection.readings.collect { onReading(link, it.toMetrics()) } },
            )
            connection.start()
        } else {
            val connection = SensorConnection(bleLink, device.kind, scope)
            link.sensor = connection
            link.jobs = listOf(
                scope.launch { connection.state.collect { onState(link, it) } },
                scope.launch { connection.batteryPercent.collect { link.battery = it; publish() } },
                scope.launch { connection.readings.collect { onReading(link, it) } },
            )
            connection.start()
        }
        publish()
    }

    private fun onState(link: Link, state: TreadmillState) {
        if (links[link.known.key] !== link) return
        link.status = when (state) {
            TreadmillState.Connecting -> ConnectionStatus.CONNECTING
            is TreadmillState.Connected -> ConnectionStatus.CONNECTED.also {
                state.capabilities.metrics.forEach { metric -> capability(metric)?.let { link.seen += it } }
            }
            is TreadmillState.Reconnecting -> ConnectionStatus.RECONNECTING
            is TreadmillState.Failed -> ConnectionStatus.FAILED
            TreadmillState.Closed -> ConnectionStatus.IDLE
        }
        if (state is TreadmillState.Failed) {
            failures[link.known.key] = state.reason
            links.remove(link.known.key)
            link.jobs.forEach { it.cancel() }
        }
        _belt.value = treadmillBelt()
        publish()
    }

    private fun onReading(link: Link, metrics: DeviceMetrics) {
        if (links[link.known.key] !== link) return
        val now = clock.now()
        val speed = metrics.speedKmh
        val previous = link.latest?.speedKmh
        val lastAt = link.lastAt
        if (previous != null && lastAt != null) {
            val seconds = (now - lastAt).coerceIn(0.seconds, MAX_GAP).inWholeMilliseconds / 1000.0
            link.odometer += previous / 3.6 * seconds
        }
        link.lastAt = now
        link.latest = metrics
        val before = link.seen.size
        if (speed != null) link.seen += Quantity.SPEED
        if (metrics.heartRateBpm != null) link.seen += Quantity.HEART_RATE
        if (metrics.cadenceSpm != null) link.seen += Quantity.CADENCE
        if (metrics.inclinePercent != null) link.seen += Quantity.INCLINE
        if (metrics.energyKcal != null) link.seen += Quantity.ENERGY
        if (metrics.powerWatts != null) link.seen += Quantity.POWER
        if (link.seen.size != before) publish()

        // One merged reading per reading of the device that sets the pace of the recording.
        val primary = source(Quantity.SPEED) ?: source(Quantity.HEART_RATE) ?: link
        if (primary === link) _merged.tryEmit(merge())
    }

    private fun merge(): MergedReading {
        val names = mutableMapOf<Quantity, String>()
        fun <T> take(quantity: Quantity, value: (Link, DeviceMetrics) -> T?): T? {
            val link = source(quantity) ?: return null
            val latest = link.latest ?: return null
            return value(link, latest)?.also { names[quantity] = link.known.name }
        }
        val speedLink = source(Quantity.SPEED)
        val treadmill = speedLink?.takeIf { it.known.kind == DeviceKind.TREADMILL }?.latest
        return MergedReading(
            metrics = DeviceMetrics(
                speedKmh = take(Quantity.SPEED) { _, m -> m.speedKmh },
                distanceMeters = speedLink?.let { link -> link.latest?.distanceMeters ?: link.odometer.takeIf { link.lastAt != null } },
                heartRateBpm = take(Quantity.HEART_RATE) { _, m -> m.heartRateBpm },
                cadenceSpm = take(Quantity.CADENCE) { _, m -> m.cadenceSpm },
                inclinePercent = take(Quantity.INCLINE) { _, m -> m.inclinePercent },
                energyKcal = take(Quantity.ENERGY) { _, m -> m.energyKcal },
                powerWatts = take(Quantity.POWER) { _, m -> m.powerWatts },
                elevationGainMeters = treadmill?.elevationGainMeters,
                machineElapsed = treadmill?.machineElapsed,
            ),
            sources = names,
        )
    }

    /** The connected device picked for [quantity]: the user's choice, else the one on the body. */
    private fun source(quantity: Quantity): Link? {
        val candidates = links.values.filter { it.status == ConnectionStatus.CONNECTED && quantity in it.seen }
        val chosen = memory.getString(sourceKey(quantity))
        return candidates.firstOrNull { it.known.key == chosen }
            ?: candidates.minByOrNull { priority(quantity, it.known.kind) }
    }

    private fun priority(quantity: Quantity, kind: DeviceKind) = when (quantity) {
        Quantity.SPEED, Quantity.CADENCE -> listOf(DeviceKind.FOOT_POD, DeviceKind.TREADMILL, DeviceKind.HEART_RATE)
        Quantity.HEART_RATE -> listOf(DeviceKind.HEART_RATE, DeviceKind.TREADMILL, DeviceKind.FOOT_POD)
        else -> listOf(DeviceKind.TREADMILL, DeviceKind.FOOT_POD, DeviceKind.HEART_RATE)
    }.indexOf(kind)

    private fun treadmillBelt(): BeltStatus =
        links.values.firstOrNull { it.known.kind == DeviceKind.TREADMILL && it.status == ConnectionStatus.CONNECTED }?.belt
            ?: BeltStatus.UNKNOWN

    private fun provides(device: Known): Set<Quantity> = links[device.key]?.seen ?: defaults(device.kind)

    /** The added device that is the metric source for [quantity], connected or not: the user's choice, else automatic. */
    private fun chosen(quantity: Quantity): String? {
        val options = known.filter { quantity in provides(it) }
        val choice = memory.getString(sourceKey(quantity))
        return options.firstOrNull { it.key == choice }?.key ?: automatic(quantity, options)?.key
    }

    /** Automatic among [devices]: the one on the body wins, like [source] does for connected ones. */
    private fun automatic(quantity: Quantity, devices: List<Known>): Known? =
        devices.filter { quantity in provides(it) }.minByOrNull { priority(quantity, it.kind) }

    private fun publish() {
        val used = Quantity.entries.associateWith { source(it)?.known?.key }
        val rows = known.map { device ->
            val link = links[device.key]
            val provides = Quantity.entries.filter { it in provides(device) }
            KnownDeviceUi(
                key = device.key,
                name = device.name,
                kind = device.kind,
                status = when {
                    link != null -> link.status
                    device.key in pending -> ConnectionStatus.CONNECTING
                    device.key in failures -> ConnectionStatus.FAILED
                    else -> ConnectionStatus.IDLE
                },
                batteryPercent = link?.battery,
                autoConnect = device.autoConnect,
                provides = provides,
                usedFor = provides.filter { used[it] == device.key },
                measures = provides.map { quantity ->
                    MeasureUi(
                        quantity = quantity,
                        used = chosen(quantity) == device.key,
                        choosable = known.any { it.key != device.key && quantity in provides(it) },
                    )
                },
                error = failures[device.key],
            )
        }
        val sources = Quantity.entries.mapNotNull { quantity ->
            val options = known.filter { quantity in provides(it) }.map { SourceOption(it.key, it.name) }
            if (options.size < 2) return@mapNotNull null
            SourceUi(quantity, memory.getString(sourceKey(quantity))?.takeIf { key -> options.any { it.key == key } }, options)
        }
        val knownKeys = known.map { it.key }.toSet()
        _available.value = links.values.filter { it.status == ConnectionStatus.CONNECTED }.flatMap { it.seen }.toSet()
        _state.update {
            it.copy(
                known = rows,
                found = found.values
                    .filter { device -> device.id !in knownKeys }
                    .map { d -> FoundDeviceUi(d.id, d.name ?: d.id, d.kind, d.rssi) },
                sources = sources,
                connectedCount = links.values.count { link -> link.status == ConnectionStatus.CONNECTED },
            )
        }
    }

    private fun loadKnown(): List<Known> {
        // The treadmill remembered before Devices existed becomes the first added device.
        val oldKey = memory.getString(OLD_KEY)
        if (oldKey != null && memory.getString(KNOWN) == null) {
            memory.putString(KNOWN, encode(listOf(Known(oldKey, memory.getString(OLD_NAME) ?: oldKey, DeviceKind.TREADMILL, autoConnect = true))))
            memory.putString(OLD_KEY, null)
            memory.putString(OLD_NAME, null)
        }
        return memory.getString(KNOWN).orEmpty().lines().mapNotNull { line ->
            val parts = line.split('\t', limit = 4)
            if (parts.size < 4) return@mapNotNull null
            val kind = DeviceKind.entries.firstOrNull { it.name == parts[0] } ?: return@mapNotNull null
            Known(key = parts[2], name = parts[3], kind = kind, autoConnect = parts[1] == "auto")
        }
    }

    private fun saveKnown() = memory.putString(KNOWN, encode(known))

    /** One line per device, tab-separated, the name last since it is the only free text. */
    private fun encode(devices: List<Known>) =
        devices.joinToString("\n") { "${it.kind.name}\t${if (it.autoConnect) "auto" else "manual"}\t${it.key}\t${it.name.replace('\n', ' ')}" }

    /** Callback form of [state] for Swift. Returns a handle to stop watching. */
    fun watch(onChange: (DevicesUiState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun close() {
        links.keys.toList().forEach(::disconnect)
        stopScanning()
    }

    private companion object {
        const val KNOWN = "devices.known"
        const val OLD_KEY = "treadmill.key"
        const val OLD_NAME = "treadmill.name"
        val PENDING_TIMEOUT = 30.seconds
        val MAX_GAP = 5.seconds

        fun sourceKey(quantity: Quantity) = "devices.source.${quantity.name}"

        fun defaults(kind: DeviceKind) = when (kind) {
            DeviceKind.TREADMILL -> setOf(Quantity.SPEED)
            DeviceKind.FOOT_POD -> setOf(Quantity.SPEED, Quantity.CADENCE)
            DeviceKind.HEART_RATE -> setOf(Quantity.HEART_RATE)
        }

        fun capability(metric: MachineMetric) = when (metric) {
            MachineMetric.INCLINATION -> Quantity.INCLINE
            MachineMetric.HEART_RATE -> Quantity.HEART_RATE
            MachineMetric.EXPENDED_ENERGY -> Quantity.ENERGY
            MachineMetric.POWER -> Quantity.POWER
            MachineMetric.CADENCE -> Quantity.CADENCE
            else -> null
        }
    }
}
