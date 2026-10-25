package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.TimelineCompiler
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
import ch.coachpoulpy.types.treadmill.StepCounter
import ch.coachpoulpy.types.treadmill.TreadmillModule
import ch.coachpoulpy.types.treadmill.TreadmillRecorder
import ch.coachpoulpy.types.treadmill.TreadmillSummary
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

/** Numbers the Live tab can show; each platform names them in its own language. */
enum class LiveMetric {
    SPEED, PACE, DISTANCE, HEART_RATE, CADENCE, INCLINE, ENERGY, POWER,

    /** Estimated from the speed when no pod counts steps: shown with "≈". */
    STEPS_ESTIMATED;

    /** Shown as minutes and seconds; the tile's value is then a number of seconds. */
    val isTime: Boolean get() = this == PACE
}

/**
 * One number of the Live tab, already in the user's units. [value] null keeps the tile
 * in place with a dash, for example the pace while the belt is paused, so tiles never jump.
 * [source] is the name of the device it comes from.
 */
data class LiveTile(val metric: LiveMetric, val value: Double?, val decimals: Int, val unit: String, val source: String?)

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
    /** Estimated from the speed, shown with "≈". */
    val estimatedSteps: Int?,
    val phases: List<PhaseResultUi>,
)

/** Where the Live tab stands with the added devices. */
enum class LiveConnection {
    /** No device added, or none of the added ones is connecting: the empty state shows. */
    EMPTY,

    /** At least one added device is connecting or reconnecting and none is connected yet: Live shows read-only. */
    CONNECTING,

    /** At least one device is connected. */
    CONNECTED,
}

data class LiveUiState(
    val connectedCount: Int = 0,
    val connection: LiveConnection = LiveConnection.EMPTY,
    /** The devices being connected while [connection] is CONNECTING, for "Connecting to …". */
    val connecting: List<String> = emptyList(),
    /**
     * SPEED and PACE first when a device measures speed: the apps show those two large.
     * While connecting, the tiles of what the connecting devices usually measure, all without a value.
     */
    val tiles: List<LiveTile> = emptyList(),
    val belt: BeltStatus = BeltStatus.UNKNOWN,
    /** Whether a treadmill is connected, for the belt switch. */
    val hasTreadmill: Boolean = false,
    /** When on, pausing or stopping the belt pauses the recording, and restarting it resumes. */
    val syncPause: Boolean = true,
    /** For the summary, which the apps format with [Units]. */
    val units: UnitSystem = UnitSystem.METRIC,
    val plans: List<PlanOption> = emptyList(),
    val recording: RecordingUi? = null,
    val summary: SummaryUi? = null,
)

/**
 * The Live tab: the merged numbers of every connected device, and the recorder, free
 * or following a treadmill workout. Each recorded event is written to the phone's
 * database as it happens, like any guided session.
 */
class LiveModel(
    private val devices: DevicesModel,
    private val settings: StateFlow<SettingsUiState>,
    private val store: LocalStore,
    private val compiler: TimelineCompiler,
    private val memory: KeyValueStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    private val clock: Clock = Clock.System,
) {
    private val _state = MutableStateFlow(
        LiveUiState(
            plans = builtInPlans.map { PlanOption(it.id.value, it.name) },
            syncPause = memory.getString(SYNC_PAUSE) != "false",
            units = settings.value.units,
        ),
    )
    val state: StateFlow<LiveUiState> = _state.asStateFlow()

    private var savedWorkouts = emptyList<Workout>()
    private var latest: MergedReading? = null

    /** What the devices being connected usually measure, for placeholder tiles before any is connected. */
    private var expected = emptySet<Quantity>()

    /** Steps since the recording started, or since connecting when not recording. */
    private val steps = StepCounter()

    /** True while the recording is paused because the belt stopped, so restarting the belt resumes it. */
    private var pausedByBelt = false
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
        scope.launch { devices.merged.collect(::onReading) }
        scope.launch {
            devices.belt.collect { belt ->
                _state.update { it.copy(belt = belt) }
                followBelt(belt)
            }
        }
        scope.launch {
            devices.state.collect { d ->
                if (d.connectedCount == 0) latest = null
                val connecting = d.known.filter { it.status == ConnectionStatus.CONNECTING || it.status == ConnectionStatus.RECONNECTING }
                val connection = when {
                    d.connectedCount > 0 -> LiveConnection.CONNECTED
                    connecting.isNotEmpty() -> LiveConnection.CONNECTING
                    else -> LiveConnection.EMPTY
                }
                expected = if (connection == LiveConnection.CONNECTING) connecting.flatMap { it.provides }.toSet() else emptySet()
                _state.update {
                    it.copy(
                        connectedCount = d.connectedCount,
                        connection = connection,
                        connecting = if (connection == LiveConnection.CONNECTING) connecting.map { device -> device.name } else emptyList(),
                        hasTreadmill = devices.hasTreadmill,
                        tiles = tiles(),
                    )
                }
            }
        }
        scope.launch { devices.available.collect { _state.update { it.copy(tiles = tiles()) } } }
        scope.launch { settings.collect { s -> _state.update { it.copy(units = s.units, tiles = tiles()) } } }
    }

    private fun onReading(reading: MergedReading) {
        latest = reading
        reading.metrics.speedKmh?.let { steps.add(clock.now(), it) }
        _state.update { it.copy(tiles = tiles()) }
        val current = recording ?: return
        persist(current, current.recorder.onReading(clock.now(), reading.metrics))
        refreshRecording(current)
    }

    /**
     * The tiles follow what the connected devices measure, not what the last reading
     * held: a number that goes missing for a moment keeps its tile, with a dash.
     */
    private fun tiles(): List<LiveTile> {
        val available = devices.available.value.ifEmpty { expected }
        val units = settings.value.units
        val m = latest?.metrics
        val from = latest?.sources.orEmpty()
        val speedFrom = from[Quantity.SPEED]
        val tiles = mutableListOf<LiveTile>()
        if (Quantity.SPEED in available) {
            val speed = m?.speedKmh?.let { Units.speed(it, units) }
            tiles += LiveTile(LiveMetric.SPEED, speed?.value, 1, Units.speed(0.0, units).unit, speedFrom)
            tiles += LiveTile(LiveMetric.PACE, m?.speedKmh?.let { Units.paceSeconds(it, units) }, 0, Units.paceUnit(units), speedFrom)
            val distance = m?.distanceMeters?.let { Units.distance(it, units) }
            tiles += LiveTile(LiveMetric.DISTANCE, distance?.value, 2, Units.distance(0.0, units).unit, speedFrom)
        }
        if (Quantity.HEART_RATE in available) {
            tiles += LiveTile(LiveMetric.HEART_RATE, m?.heartRateBpm?.toDouble(), 0, "bpm", from[Quantity.HEART_RATE])
        }
        if (Quantity.CADENCE in available) {
            tiles += LiveTile(LiveMetric.CADENCE, m?.cadenceSpm?.toDouble(), 0, "spm", from[Quantity.CADENCE])
        }
        if (Quantity.INCLINE in available) {
            tiles += LiveTile(LiveMetric.INCLINE, m?.inclinePercent, 1, "%", from[Quantity.INCLINE])
        }
        if (Quantity.ENERGY in available) {
            tiles += LiveTile(LiveMetric.ENERGY, m?.energyKcal?.toDouble(), 0, "kcal", from[Quantity.ENERGY])
        }
        if (Quantity.POWER in available) {
            tiles += LiveTile(LiveMetric.POWER, m?.powerWatts?.toDouble(), 0, "W", from[Quantity.POWER])
        }
        if (Quantity.SPEED in available && Quantity.CADENCE !in available) {
            tiles += LiveTile(LiveMetric.STEPS_ESTIMATED, steps.steps.toDouble().takeIf { m?.speedKmh != null }, 0, "", speedFrom)
        }
        return tiles
    }

    /**
     * Starts recording against the plan with this [PlanOption.key]. Works without a device too, as a timer,
     * but not while the added devices are still connecting: Live is read-only until one is there.
     */
    fun startRecording(planKey: String) {
        if (recording != null || _state.value.connection == LiveConnection.CONNECTING) return
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
            // Built-in plans get their name in the user's language when the history shows them.
            title = if (workout in savedWorkouts) workout.name else null,
            type = WorkoutType.TREADMILL,
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
        steps.reset()
        _state.update { it.copy(summary = null) }
        refreshRecording(started)
        // Started with the belt still: the timer waits for it to move.
        followBelt(_state.value.belt)
    }

    fun pauseRecording() = act { it.pause(clock.now()) }

    fun resumeRecording() {
        pausedByBelt = false
        act { it.resume(clock.now()) }
    }

    fun setSyncPause(on: Boolean) {
        memory.putString(SYNC_PAUSE, on.toString())
        _state.update { it.copy(syncPause = on) }
        if (on) followBelt(_state.value.belt)
    }

    /** With [LiveUiState.syncPause] on, the recording follows the belt. A pause the user made stays theirs. */
    private fun followBelt(belt: BeltStatus) {
        val current = recording ?: return
        if (!_state.value.syncPause || !devices.hasTreadmill) return
        val status = current.session.state(clock.now()).status
        when {
            (belt == BeltStatus.PAUSED || belt == BeltStatus.STOPPED) && status == SessionState.Status.RUNNING -> {
                pausedByBelt = true
                act { it.pause(clock.now()) }
            }
            belt == BeltStatus.MOVING && status == SessionState.Status.PAUSED && pausedByBelt -> resumeRecording()
        }
    }

    fun skipPhase() = act { it.skipCurrent(clock.now()) }

    /** Closes the summary of the last recording. */
    fun clearSummary() = _state.update { it.copy(summary = null) }

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
        store.saveTotals(finished.executionId, sessionTotals(finished.session.events, finished.timeline))
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
        estimatedSteps = estimatedSteps,
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
    fun watch(onChange: (LiveUiState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun close() {
        recording?.let { stopRecording() }
        scope.cancel()
    }

    companion object {
        private const val SYNC_PAUSE = "treadmill.syncPause"

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
