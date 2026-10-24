package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.device.ftms.BleCentral
import ch.coachpoulpy.device.ftms.FoundDevice
import ch.coachpoulpy.device.ftms.MachineMetric
import ch.coachpoulpy.device.ftms.TreadmillCapabilities
import ch.coachpoulpy.device.ftms.TreadmillConnection
import ch.coachpoulpy.device.ftms.TreadmillState
import ch.coachpoulpy.types.treadmill.TreadmillModule
import ch.coachpoulpy.types.treadmill.TreadmillRecorder
import ch.coachpoulpy.types.treadmill.TreadmillSummary
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ConnectionStatus { IDLE, CONNECTING, CONNECTED, RECONNECTING, FAILED }

/** Metrics the live screen can show; each platform names them in its own language. */
enum class LiveMetric { SPEED, INCLINE, DISTANCE_KM, HEART_RATE, ENERGY, MACHINE_TIME, POWER }

/** A value for one metric. The platform formats it with [decimals] in the user's locale; MACHINE_TIME is seconds. */
data class LiveTile(val metric: LiveMetric, val value: Double, val decimals: Int)

/** [key] identifies the device for [TreadmillModel.connect]: a MAC address on Android, a UUID on iOS. */
data class DeviceRow(val key: String, val name: String, val rssi: Int, val advertisesFtms: Boolean)

/** Something to record against: a free run, the demo intervals or a saved treadmill workout. */
data class PlanOption(val key: String, val name: String)

data class RecordingUi(
    val paused: Boolean,
    val elapsedSeconds: Long,
    /** 1-based, out of [phaseCount]. */
    val phaseNumber: Int,
    val phaseCount: Int,
    val isRest: Boolean,
    val phaseRemainingSeconds: Long?,
    val targetSpeedKmh: Double?,
    val targetDistanceMeters: Double?,
    val freeRun: Boolean,
)

data class PhaseResultUi(
    val phaseNumber: Int,
    val isRest: Boolean,
    val durationSeconds: Long,
    val distanceMeters: Double?,
    val averageSpeedKmh: Double?,
    val targetSpeedKmh: Double?,
    /** 0 to 100, or null without a target speed or readings. */
    val onTargetPercent: Int?,
)

data class SummaryUi(
    val durationSeconds: Long,
    val distanceMeters: Double?,
    val averageSpeedKmh: Double?,
    val averageHeartRateBpm: Int?,
    val maxHeartRateBpm: Int?,
    val energyKcal: Int?,
    val phases: List<PhaseResultUi>,
)

data class TreadmillUiState(
    val scanning: Boolean = false,
    val devices: List<DeviceRow> = emptyList(),
    val connection: ConnectionStatus = ConnectionStatus.IDLE,
    val deviceName: String? = null,
    /** Why scanning or connecting failed, in English: shown as a detail under a localized title. */
    val error: String? = null,
    val tiles: List<LiveTile> = emptyList(),
    /** What the machine says it supports, for testing a new treadmill. */
    val supportedMetrics: List<String> = emptyList(),
    val malformedPackets: Int = 0,
    val plans: List<PlanOption> = emptyList(),
    val recording: RecordingUi? = null,
    val summary: SummaryUi? = null,
)

/**
 * The treadmill screen: find the treadmill, connect, watch live metrics, and record
 * a session, free or following a treadmill workout. Each recorded event is written
 * to the phone's database as it happens, like any guided session.
 */
class TreadmillModel(
    private val central: BleCentral,
    private val store: LocalStore,
    private val compiler: TimelineCompiler,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val clock: Clock = Clock.System,
) {
    private val _state = MutableStateFlow(TreadmillUiState(plans = builtInPlans.map { PlanOption(it.id.value, it.name) }))
    val state: StateFlow<TreadmillUiState> = _state.asStateFlow()

    private val found = mutableMapOf<String, FoundDevice>()
    private var savedWorkouts = emptyList<Workout>()
    private var scanJob: Job? = null
    private var connection: TreadmillConnection? = null
    private var connectionJobs = emptyList<Job>()
    private var capabilities = TreadmillCapabilities.Unknown
    private var recording: Recording? = null

    private class Recording(val executionId: String, val timeline: Timeline, val session: Session, val freeRun: Boolean) {
        val recorder = TreadmillRecorder(session)
        var ticker: Job? = null
    }

    init {
        scope.launch {
            store.workouts().collect { workouts ->
                savedWorkouts = workouts.filter { it.type == WorkoutType.TREADMILL }
                _state.update { it.copy(plans = (builtInPlans + savedWorkouts).map { w -> PlanOption(w.id.value, w.name) }) }
            }
        }
    }

    fun startScan() {
        if (scanJob != null) return
        _state.update { it.copy(scanning = true, error = null) }
        scanJob = scope.launch {
            try {
                central.scan().collect { device ->
                    // Unnamed devices that do not advertise FTMS are phones, earbuds and the like.
                    if (device.name == null && !device.advertisesFtms) return@collect
                    found[device.id] = device
                    _state.update { it.copy(devices = deviceRows()) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: e.toString()) }
            } finally {
                _state.update { it.copy(scanning = false) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
    }

    private fun deviceRows() = found.values
        .sortedWith(compareByDescending<FoundDevice> { it.advertisesFtms }.thenByDescending { it.rssi })
        .map { DeviceRow(it.id, it.name ?: it.id, it.rssi, it.advertisesFtms) }

    fun connect(deviceId: String) {
        val device = found[deviceId] ?: return
        stopScan()
        disconnect()
        val link = try {
            central.open(device)
        } catch (e: Exception) {
            _state.update { it.copy(connection = ConnectionStatus.FAILED, error = e.message) }
            return
        }
        val newConnection = TreadmillConnection(link, scope)
        connection = newConnection
        _state.update { it.copy(deviceName = device.name ?: device.id, connection = ConnectionStatus.CONNECTING, error = null) }
        connectionJobs = listOf(
            scope.launch { newConnection.state.collect(::onConnectionState) },
            scope.launch {
                newConnection.readings.collect { data ->
                    onReading(data.toMetrics())
                    _state.update { it.copy(malformedPackets = newConnection.malformedPackets) }
                }
            },
        )
        newConnection.start()
    }

    private fun onConnectionState(state: TreadmillState) {
        val status = when (state) {
            TreadmillState.Connecting -> ConnectionStatus.CONNECTING
            is TreadmillState.Connected -> ConnectionStatus.CONNECTED.also { capabilities = state.capabilities }
            is TreadmillState.Reconnecting -> ConnectionStatus.RECONNECTING
            is TreadmillState.Failed -> ConnectionStatus.FAILED
            TreadmillState.Closed -> ConnectionStatus.IDLE
        }
        _state.update {
            it.copy(
                connection = status,
                error = (state as? TreadmillState.Failed)?.reason,
                supportedMetrics = capabilities.metrics.map { m -> m.name.lowercase().replace('_', ' ') } +
                    listOfNotNull(
                        capabilities.speedRange?.let { r -> "speed ${r.min}–${r.max} km/h" },
                        capabilities.inclineRange?.let { r -> "incline ${r.min}–${r.max} %" },
                    ),
            )
        }
    }

    fun disconnect() {
        val current = connection ?: return
        connection = null
        connectionJobs.forEach { it.cancel() }
        scope.launch { current.close() }
        _state.update { it.copy(connection = ConnectionStatus.IDLE, deviceName = null, tiles = emptyList()) }
    }

    private fun onReading(metrics: DeviceMetrics) {
        _state.update { it.copy(tiles = tiles(metrics)) }
        val current = recording ?: return
        persist(current, current.recorder.onReading(clock.now(), metrics))
        refreshRecording(current)
    }

    private fun tiles(m: DeviceMetrics): List<LiveTile> {
        val supported = capabilities.metrics
        return listOfNotNull(
            m.speedKmh?.let { LiveTile(LiveMetric.SPEED, it, 1) },
            m.inclinePercent?.takeIf { MachineMetric.INCLINATION in supported }?.let { LiveTile(LiveMetric.INCLINE, it, 1) },
            m.distanceMeters?.takeIf { MachineMetric.TOTAL_DISTANCE in supported }?.let { LiveTile(LiveMetric.DISTANCE_KM, it / 1000, 2) },
            m.heartRateBpm?.let { LiveTile(LiveMetric.HEART_RATE, it.toDouble(), 0) },
            m.energyKcal?.takeIf { MachineMetric.EXPENDED_ENERGY in supported }?.let { LiveTile(LiveMetric.ENERGY, it.toDouble(), 0) },
            m.machineElapsed?.takeIf { MachineMetric.ELAPSED_TIME in supported }?.let {
                LiveTile(LiveMetric.MACHINE_TIME, it.inWholeSeconds.toDouble(), 0)
            },
            m.powerWatts?.let { LiveTile(LiveMetric.POWER, it.toDouble(), 0) },
        )
    }

    /** Starts recording against the plan with this [PlanOption.key]. Works without a treadmill too, as a timer. */
    fun startRecording(planKey: String) {
        if (recording != null) return
        val workout = (builtInPlans + savedWorkouts).firstOrNull { it.id.value == planKey } ?: return
        val freeRun = workout === freeRunPlan
        val timeline = if (freeRun) TreadmillModule.freeRun else compiler.compile(workout)
        val now = clock.now()
        val executionId = Ids.newUuidV7(now.toEpochMilliseconds())
        store.startExecution(
            Execution(
                id = ExecutionId(executionId),
                source = if (freeRun) ExecutionSource.DEVICE_ONLY else ExecutionSource.GUIDED,
                startedAt = now,
                workout = if (workout in savedWorkouts) WorkoutRef(workout.id, workout.version) else null,
                notes = if (workout === demoIntervals) "Demo intervals" else null,
            ),
        )
        val started = Recording(executionId, timeline, Session(timeline), freeRun)
        recording = started
        persist(started, started.session.start(now))
        // Timed phases end on the clock even when no reading arrives.
        started.ticker = scope.launch {
            while (isActive) {
                delay(500.milliseconds)
                persist(started, started.session.tick(clock.now()))
                refreshRecording(started)
            }
        }
        _state.update { it.copy(summary = null) }
        refreshRecording(started)
    }

    fun pauseRecording() = act { it.pause(clock.now()) }

    fun resumeRecording() = act { it.resume(clock.now()) }

    fun skipPhase() = act { it.skipCurrent(clock.now()) }

    private fun act(action: (Session) -> List<SessionEvent>) {
        val current = recording ?: return
        persist(current, action(current.session))
        refreshRecording(current)
    }

    /** Ends the recording; phases not reached are logged as skipped. */
    fun stopRecording() {
        val current = recording ?: return
        val now = clock.now()
        if (current.session.state(now).status == SessionState.Status.PAUSED) persist(current, current.session.resume(now))
        while (current.session.state(now).status != SessionState.Status.FINISHED) {
            persist(current, if (current.freeRun) current.session.completeCurrent(now) else current.session.skipCurrent(now))
        }
        refreshRecording(current)
    }

    private fun persist(recording: Recording, events: List<SessionEvent>) {
        events.forEach { store.appendEvent(recording.executionId, it) }
        events.lastOrNull { it is SessionEvent.Finished }?.let { finish(recording, it.at) }
    }

    private fun finish(finished: Recording, at: Instant) {
        if (recording !== finished) return
        recording = null
        finished.ticker?.cancel()
        store.finishExecution(finished.executionId, at)
        val summary = TreadmillSummary.of(finished.timeline, finished.session.events)
        _state.update { it.copy(recording = null, summary = summary.toUi()) }
    }

    private fun refreshRecording(current: Recording) {
        if (recording !== current) return
        val now = clock.now()
        val s = current.session.state(now)
        val work = s.phase as? Phase.Work
        val started = current.session.events.firstOrNull()?.at ?: now
        _state.update {
            it.copy(
                recording = RecordingUi(
                    paused = s.status == SessionState.Status.PAUSED,
                    elapsedSeconds = (now - started).inWholeSeconds,
                    phaseNumber = s.phaseIndex + 1,
                    phaseCount = current.timeline.phases.size,
                    isRest = s.phase is Phase.Rest,
                    phaseRemainingSeconds = s.phaseRemaining?.inWholeSeconds,
                    targetSpeedKmh = (work?.intensity as? Intensity.Speed)?.kmh,
                    targetDistanceMeters = (work?.target as? Target.Distance)?.meters,
                    freeRun = current.freeRun,
                ),
            )
        }
    }

    private fun TreadmillSummary.toUi() = SummaryUi(
        durationSeconds = duration.inWholeSeconds,
        distanceMeters = distanceMeters,
        averageSpeedKmh = averageSpeedKmh,
        averageHeartRateBpm = averageHeartRateBpm,
        maxHeartRateBpm = maxHeartRateBpm,
        energyKcal = energyKcal,
        phases = phases.map { p ->
            PhaseResultUi(
                phaseNumber = p.phaseIndex + 1,
                isRest = p.phase is Phase.Rest,
                durationSeconds = p.duration.inWholeSeconds,
                distanceMeters = p.distanceMeters,
                averageSpeedKmh = p.averageSpeedKmh,
                targetSpeedKmh = p.targetSpeedKmh,
                onTargetPercent = p.timeOnTargetSpeed?.let { f -> (f * 100).toInt() },
            )
        },
    )

    /** Callback form of [state] for Swift. Returns a handle to stop watching. */
    fun watch(onChange: (TreadmillUiState) -> Unit): Cancellable {
        val job = scope.launch { state.collect { onChange(it) } }
        return Cancellable { job.cancel() }
    }

    fun close() {
        recording?.let { stopRecording() }
        disconnect()
        scope.cancel()
    }

    companion object {
        private val freeRunPlan = Workout(WorkoutId("free-run"), 1, "Free run", WorkoutType.TREADMILL, emptyList())

        /** A short workout to try plan checking on a real treadmill before the workout builder exists. */
        val demoIntervals = Workout(
            id = WorkoutId("demo-intervals"),
            version = 1,
            name = "Demo intervals",
            type = WorkoutType.TREADMILL,
            steps = listOf(
                run("warm-up", Target.Time(3.minutes), 6.0),
                Step.Group(
                    id = StepId("intervals"),
                    kind = GroupKind.INTERVALS,
                    rounds = 3,
                    children = listOf(run("fast", Target.Time(1.minutes), 9.0), run("easy", Target.Time(1.minutes), 6.0)),
                ),
                run("finish", Target.Distance(200.0), 7.0),
            ),
        )

        private val builtInPlans = listOf(freeRunPlan, demoIntervals)

        private fun run(id: String, target: Target, kmh: Double) = Step.ExerciseStep(
            id = StepId(id),
            exercise = TreadmillModule.treadmillRun,
            prescription = Prescription(listOf(SetPrescription(target, intensity = Intensity.Speed(kmh)))),
        )
    }
}
