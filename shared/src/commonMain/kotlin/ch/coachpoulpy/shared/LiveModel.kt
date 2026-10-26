package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.covering
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
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
import kotlin.time.Duration
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

/**
 * Something the recorder can follow: a walk or a run from the catalog, recorded as one open
 * phase, or a treadmill workout. [note] says what it is in a few words, and may be empty.
 */
data class LiveChoice(val key: String, val name: String, val note: String)

data class RecordingUi(
    val paused: Boolean,
    /** Time recorded, without the pauses. */
    val elapsedSeconds: Long,
    /** Time in the current step, without the pauses. */
    val stepElapsedSeconds: Long,
    /** The steps already done in this session, oldest first. */
    val steps: List<PhaseResultUi>,
    /** Paused after ending a step: how long the rest has lasted so far. */
    val restSeconds: Long?,
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
    /** The pause between this step and the next, when the step was ended while paused. */
    val restAfterSeconds: Long? = null,
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

/** Why Poulpy asks whether the session is over. */
enum class FinishReason {
    /** The belt has not moved for a long time, or the safety key was pulled. */
    BELT_STILL,

    /** The treadmill disconnected and has not come back. */
    TREADMILL_LOST,

    /** Stop was pressed on the console: the user may start another program, or be done. */
    BELT_STOPPED,
}

/** "Finish the session?", asked when the treadmill went quiet or away for a long time. */
data class FinishPromptUi(
    val reason: FinishReason,
    /** How long ago the belt stopped or the treadmill went away. */
    val sinceMinutes: Int,
    /** With no answer, the session finishes on its own in this many minutes. */
    val autoFinishMinutes: Int,
    /** Where the session would end, in seconds since it started: when the belt last moved or the treadmill was last seen. */
    val endsAtSeconds: Long,
)

/** Where the Live tab stands with the added devices. */
enum class LiveConnection {
    /** No device added, or none of the added ones is connecting: the empty state shows. */
    EMPTY,

    /**
     * At least one added device is connecting or reconnecting, or about to just after launch, and none is
     * connected yet: Live shows read-only.
     */
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
    /** Whether a treadmill is connected, for the "Follow the treadmill" switch. */
    val hasTreadmill: Boolean = false,
    /**
     * When on, Start waits for the belt to move, the recording pauses and resumes with the belt,
     * and Stop on the console finishes it. When off, the recording is driven from the phone only.
     */
    val followTreadmill: Boolean = true,
    /** What Start will record, while it waits for the belt to move. */
    val waitingFor: LiveChoice? = null,
    /** Set when the treadmill went quiet or away for a long time during the recording. */
    val finishPrompt: FinishPromptUi? = null,
    /** For the summary, which the apps format with [Units]. */
    val units: UnitSystem = UnitSystem.METRIC,
    /** What Start records, shown in the "What are you doing?" field; null until the choices load. */
    val choice: LiveChoice? = null,
    /** Up to three choices last started, most recent first, at the top of the sheet. */
    val recent: List<LiveChoice> = emptyList(),
    /** Every treadmill-ready walk not in [recent], easiest first. */
    val walking: List<LiveChoice> = emptyList(),
    /** Every treadmill-ready run not in [recent], easiest first. */
    val running: List<LiveChoice> = emptyList(),
    /** The saved treadmill workouts not in [recent]. */
    val workouts: List<LiveChoice> = emptyList(),
    /** The [LiveChoice.key] that Start records. */
    val selected: String = "",
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
    private val catalog: () -> Catalog = { Catalog.bundled },
    languageCode: String = "en",
) {
    private val labels = Labels(languageCode)

    /** The keys last started, most recent first. */
    private var recents = memory.getString(RECENT)?.split(',')?.filter { it.isNotBlank() }.orEmpty()

    private val _state = MutableStateFlow(
        LiveUiState(
            // The old "pause and resume with the belt" switch seeds this one once.
            followTreadmill = (memory.getString(FOLLOW) ?: memory.getString(SYNC_PAUSE)) != "false",
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

    /** The key Start was tapped with, while the recording waits for the belt to move. */
    private var waiting: String? = null

    private class Recording(val executionId: String, val timeline: Timeline, val session: Session, val freeRun: Boolean) {
        val recorder = TreadmillRecorder(session)
        var ticker: Job? = null

        /** Whether a treadmill took part, so its going quiet or away can end the session. */
        var hadTreadmill = false

        /** Since when the belt has not moved. */
        var stillSince: Instant? = null

        /** Since when the treadmill is disconnected. */
        var lostSince: Instant? = null

        /** "Keep going" was answered to Stop on the console; asked again only once the belt has moved. */
        var stopAnswered = false

        /** Stop was pressed on the console since the last resume: restarting the belt starts a new step. */
        var stoppedOnConsole = false
        var prompt: Prompt? = null

        /** The finished steps, kept until another one ends or a rest closes. */
        var steps: List<PhaseResultUi> = emptyList()
        var stepsKey: Pair<Int, Int>? = null

        /** Paused right after ending a step, so the next one has not begun. */
        fun stepJustEnded(): Boolean =
            session.events.lastOrNull { it !is SessionEvent.Sample } is SessionEvent.PhaseCompleted
    }

    /** A finish question asked [at], which would end the session at [since]. */
    private class Prompt(val reason: FinishReason, val since: Instant, val at: Instant)

    init {
        scope.launch {
            store.workouts().collect { workouts ->
                savedWorkouts = workouts.filter { it.type == WorkoutType.TREADMILL }
                publishChoices()
            }
        }
        publishChoices()
        scope.launch { devices.merged.collect(::onReading) }
        scope.launch {
            devices.belt.collect { belt ->
                _state.update { it.copy(belt = belt) }
                onBelt(belt)
            }
        }
        // Read once now, not only when the collector first runs: the first frame would show the empty state.
        onDevices(devices.state.value)
        scope.launch { devices.state.collect(::onDevices) }
        scope.launch { devices.available.collect { _state.update { it.copy(tiles = tiles()) } } }
        scope.launch { settings.collect { s -> _state.update { it.copy(units = s.units, tiles = tiles()) } } }
    }

    /** Follows the added devices: how many are connected, and whether Live waits for some. */
    private fun onDevices(d: DevicesUiState) {
        if (d.connectedCount == 0) latest = null
        val connecting = d.known.filter {
            it.status == ConnectionStatus.CONNECTING || it.status == ConnectionStatus.RECONNECTING ||
                (d.startingUp && it.autoConnect && it.status == ConnectionStatus.IDLE)
        }
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
        recording?.let(::followConnection)
    }

    /**
     * Notes when the treadmill of the recording goes away and comes back. Following the treadmill, a treadmill
     * switched off pauses the recording like a halted belt, and it resumes once the belt moves again.
     */
    private fun followConnection(current: Recording) {
        if (devices.hasTreadmill) {
            current.hadTreadmill = true
            if (current.lostSince != null) {
                current.lostSince = null
                if (current.prompt?.reason == FinishReason.TREADMILL_LOST) current.prompt = null
            }
        } else if (current.hadTreadmill && current.lostSince == null) {
            current.lostSince = clock.now()
            val running = current.session.state(clock.now()).status == SessionState.Status.RUNNING
            if (_state.value.followTreadmill && running) {
                pausedByBelt = true
                persist(current, current.session.pause(clock.now()))
            }
        }
        refreshRecording(current)
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

    /** Every walk and run that can be done on a treadmill, as Walking then Running. */
    private fun exerciseGroups(): Pair<List<Exercise>, List<Exercise>> {
        val catalog = catalog()
        val ready = catalog.offered.filter { Equipment.TREADMILL in it.optionalEquipment }
        val walking = catalog.variationsOf(WALK).filter { it in ready }
        val running = catalog.variationsOf(RUN).filter { it in ready }
        // A treadmill exercise outside both chains still shows, after the runs.
        return walking to running + (ready - walking.toSet() - running.toSet())
    }

    private fun publishChoices() {
        val (walking, running) = exerciseGroups()
        fun exercise(e: Exercise) =
            LiveChoice(EXERCISE + e.slug.value, labels.text(e.name), e.variationNote?.let(labels::text).orEmpty())
        val walks = walking.map(::exercise)
        val runs = running.map(::exercise)
        val workouts = savedWorkouts.map { LiveChoice(it.id.value, it.name, "") }
        val all = (walks + runs + workouts).associateBy { it.key }
        val selected = _state.value.selected.takeIf { it in all }
            ?: recents.firstOrNull { it in all }
            ?: DEFAULT_CHOICE.takeIf { it in all }
            ?: all.keys.first()
        val recent = recents.mapNotNull(all::get).take(RECENT_COUNT)
        // A recent choice shows once, in Recent, not again in its group.
        fun others(choices: List<LiveChoice>) = choices.filterNot { it in recent }
        _state.update {
            it.copy(
                choice = all[selected],
                recent = recent,
                walking = others(walks),
                running = others(runs),
                workouts = others(workouts),
                selected = selected,
            )
        }
    }

    /** Picks what Start records, from the chips or the "More" sheet. */
    fun select(key: String) {
        _state.update { it.copy(selected = key) }
        publishChoices()
    }

    /**
     * Starts recording [key], an exercise or a workout from [LiveUiState]. Works without a device too, as a timer,
     * but not while the added devices are still connecting: Live is read-only until one is there.
     */
    fun startRecording(key: String) {
        if (recording != null || waiting != null || _state.value.connection == LiveConnection.CONNECTING) return
        val choice = with(_state.value) { recent + walking + running + workouts }.firstOrNull { it.key == key } ?: return
        if (_state.value.followTreadmill && devices.hasTreadmill && _state.value.belt != BeltStatus.MOVING) {
            waiting = key
            _state.update { it.copy(waitingFor = choice, summary = null) }
            return
        }
        begin(key)
    }

    /** While waiting for the belt: records right away, belt or not. */
    fun startNow() {
        val key = waiting ?: return
        cancelWaiting()
        begin(key)
    }

    /** While waiting for the belt: back to the choices, with nothing saved. */
    fun cancelWaiting() {
        waiting = null
        _state.update { it.copy(waitingFor = null) }
    }

    private fun begin(key: String) {
        val exercise = key.takeIf { it.startsWith(EXERCISE) }?.let { catalog()[ExerciseSlug(it.removePrefix(EXERCISE))] }
        val workout = if (exercise != null) null else savedWorkouts.firstOrNull { it.id.value == key } ?: return
        val freeRun = exercise != null
        val timeline = if (exercise != null) TreadmillModule.freeRecording(exercise.slug) else compiler.compile(workout!!)
        val now = clock.now()
        val executionId = Ids.newUuidV7(now.toEpochMilliseconds())
        store.startExecution(
            Execution(
                id = ExecutionId(executionId),
                source = if (freeRun) ExecutionSource.DEVICE_ONLY else ExecutionSource.GUIDED,
                startedAt = now,
                workout = if (workout != null && workout in savedWorkouts) WorkoutRef(workout.id, workout.version) else null,
            ),
            title = exercise?.let { labels.text(it.name) } ?: workout!!.name,
            type = WorkoutType.TREADMILL,
        )
        recents = (listOf(key) + (recents - key)).take(RECENT_KEPT)
        memory.putString(RECENT, recents.joinToString(","))
        publishChoices()
        val started = Recording(executionId, timeline, Session(timeline), freeRun)
        started.hadTreadmill = devices.hasTreadmill
        if (started.hadTreadmill && _state.value.belt != BeltStatus.MOVING) started.stillSince = now
        recording = started
        persist(started, started.session.start(now))
        // Timed phases end on the clock even when no reading arrives.
        started.ticker = scope.launch {
            while (isActive) {
                delay(500.milliseconds)
                persist(started, started.session.tick(clock.now()))
                checkInactivity(started)
                refreshRecording(started)
            }
        }
        steps.reset()
        _state.update { it.copy(summary = null) }
        refreshRecording(started)
    }

    fun pauseRecording() = act { it.pause(clock.now()) }

    fun resumeRecording() {
        pausedByBelt = false
        recording?.stoppedOnConsole = false
        act { it.resume(clock.now()) }
    }

    /**
     * While paused, in a free recording: closes the current step, and the next one starts on resume. The pause
     * between them is the rest. In a workout, moves on to the next phase like [skipPhase].
     */
    fun endStep() {
        val current = recording ?: return
        if (!current.freeRun) return skipPhase()
        if (current.stepJustEnded()) return
        act { it.completeCurrent(clock.now()) }
    }

    fun setFollowTreadmill(on: Boolean) {
        memory.putString(FOLLOW, on.toString())
        _state.update { it.copy(followTreadmill = on) }
        if (on) followBelt(_state.value.belt)
    }

    private fun onBelt(belt: BeltStatus) {
        if (belt == BeltStatus.MOVING && waiting != null && _state.value.followTreadmill) {
            startNow()
            return
        }
        val current = recording ?: return
        current.stillSince = when (belt) {
            BeltStatus.MOVING, BeltStatus.UNKNOWN -> null
            else -> current.stillSince ?: clock.now()
        }
        if (belt != BeltStatus.STOPPED) current.stopAnswered = false
        if (belt == BeltStatus.STOPPED) current.stoppedOnConsole = true
        if (belt == BeltStatus.MOVING && current.prompt?.reason in BELT_REASONS) current.prompt = null
        followBelt(belt)
        recording?.let {
            checkInactivity(it)
            refreshRecording(it)
        }
    }

    /**
     * With [LiveUiState.followTreadmill] on, the recording pauses and resumes with the belt. Stop on the console
     * pauses too, and asks whether the session is over. A pause the user made on the phone stays theirs.
     */
    private fun followBelt(belt: BeltStatus) {
        val current = recording ?: return
        if (!_state.value.followTreadmill || !devices.hasTreadmill) return
        val status = current.session.state(clock.now()).status
        when {
            belt in HALTED && status == SessionState.Status.RUNNING -> {
                pausedByBelt = true
                act { it.pause(clock.now()) }
            }
            belt == BeltStatus.MOVING && status == SessionState.Status.PAUSED && pausedByBelt -> {
                // After a Stop, the belt restarting is another run: a new step, the stop its rest.
                if (current.stoppedOnConsole && current.freeRun) endStep()
                resumeRecording()
            }
        }
    }

    /**
     * Asks whether the session is over once the belt has been still for [STILL_AFTER] (at once for the safety key,
     * and for Stop on the console while following the treadmill) or the treadmill gone for [LOST_AFTER], and
     * finishes it when nobody answers within [AUTO_FINISH_AFTER].
     */
    private fun checkInactivity(current: Recording) {
        if (recording !== current || !current.hadTreadmill) return
        val now = clock.now()
        val prompt = current.prompt
        if (prompt != null) {
            if (now - prompt.at >= AUTO_FINISH_AFTER) finishAt(current, prompt.since)
            return
        }
        val lost = current.lostSince
        val still = current.stillSince
        current.prompt = when {
            lost != null && now - lost >= LOST_AFTER -> Prompt(FinishReason.TREADMILL_LOST, lost, now)
            still != null && _state.value.belt == BeltStatus.STOPPED && _state.value.followTreadmill && !current.stopAnswered -> Prompt(FinishReason.BELT_STOPPED, still, now)
            still != null && (_state.value.belt == BeltStatus.SAFETY_KEY || now - still >= STILL_AFTER) -> Prompt(FinishReason.BELT_STILL, still, now)
            else -> null
        }
    }

    /** "Keep going" on the finish question: it comes back only after another full wait. */
    fun keepGoing() {
        val current = recording ?: return
        val now = clock.now()
        if (current.prompt?.reason == FinishReason.BELT_STOPPED) current.stopAnswered = true
        current.prompt = null
        if (current.stillSince != null) current.stillSince = now
        if (current.lostSince != null) current.lostSince = now
        refreshRecording(current)
    }

    /** Finishes at [since] rather than now, so the time nobody was there does not count. */
    private fun finishAt(current: Recording, since: Instant) {
        val lastControl = current.session.events.lastOrNull { it !is SessionEvent.Sample }?.at ?: since
        stopRecording(maxOf(since, lastControl))
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
    fun stopRecording() = stopRecording(clock.now())

    private fun stopRecording(now: Instant) {
        val current = recording ?: return
        if (current.freeRun) {
            // A step ended during the pause has nothing after it: the session ends with it.
            if (!current.stepJustEnded()) persist(current, current.session.completeCurrent(now))
            persist(current, current.session.finish(now))
            refreshRecording(current)
            return
        }
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
        _state.update { it.copy(recording = null, finishPrompt = null, summary = summary.toUi(finished.session.events)) }
    }

    private fun refreshRecording(current: Recording) {
        if (recording !== current) return
        val now = clock.now()
        val s = current.session.state(now)
        val work = s.phase as? Phase.Work
        val events = current.session.events
        val paused = s.status == SessionState.Status.PAUSED
        val key = s.phaseIndex to events.count { it is SessionEvent.Resumed }
        if (current.stepsKey != key) {
            current.steps = TreadmillSummary.of(current.timeline, events).toUi(events).phases.filter { it.phaseNumber <= s.phaseIndex }
            current.stepsKey = key
        }
        _state.update {
            it.copy(
                recording = RecordingUi(
                    paused = paused,
                    elapsedSeconds = activeTime(events, now).inWholeSeconds,
                    stepElapsedSeconds = s.phaseElapsed.inWholeSeconds,
                    steps = current.steps,
                    restSeconds = if (paused && current.stepJustEnded()) {
                        events.lastOrNull { it is SessionEvent.Paused }?.let { (now - it.at).inWholeSeconds }
                    } else {
                        null
                    },
                    phaseNumber = s.phaseIndex + 1,
                    phaseCount = current.timeline.covering(events).phases.size,
                    isRest = s.phase is Phase.Rest,
                    phaseRemainingSeconds = s.phaseRemaining?.inWholeSeconds,
                    targetSpeedKmh = (work?.intensity as? Intensity.Speed)?.kmh,
                    targetDistanceMeters = (work?.target as? Target.Distance)?.meters,
                    freeRun = current.freeRun,
                ),
                finishPrompt = current.prompt?.let { p ->
                    val left = AUTO_FINISH_AFTER - (now - p.at)
                    FinishPromptUi(
                        reason = p.reason,
                        sinceMinutes = (now - p.since).inWholeMinutes.toInt(),
                        autoFinishMinutes = ((left.inWholeSeconds + 59) / 60).toInt().coerceAtLeast(0),
                        endsAtSeconds = activeTime(current.session.events.filter { e -> e.at <= p.since }, p.since).inWholeSeconds,
                    )
                },
            )
        }
    }

    /** The time between each start or resume and the next pause or finish. */
    private fun activeTime(events: List<SessionEvent>, now: Instant): Duration {
        var total = Duration.ZERO
        var runningSince: Instant? = null
        for (event in events) {
            when (event) {
                is SessionEvent.Started, is SessionEvent.Resumed -> if (runningSince == null) runningSince = event.at
                is SessionEvent.Paused, is SessionEvent.Finished -> runningSince?.let {
                    total += event.at - it
                    runningSince = null
                }
                else -> Unit
            }
        }
        return total + (runningSince?.let { now - it } ?: Duration.ZERO)
    }

    /** [events] give the rest after each step: a pause that a step ended in, once it is over. */
    private fun TreadmillSummary.toUi(events: List<SessionEvent>): SummaryUi {
        val rests = TreadmillSummary.pauses(events, end = null)
        val ends = events.filter { it is SessionEvent.PhaseCompleted || it is SessionEvent.PhaseSkipped }.map { it.at }
        return toUi { index ->
            ends.getOrNull(index)?.let { end -> rests.firstOrNull { (from, to) -> end in from..to } }
                ?.let { (from, to) -> (to - from).inWholeSeconds }
        }
    }

    private fun TreadmillSummary.toUi(restAfter: (Int) -> Long?) = SummaryUi(
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
                restAfterSeconds = restAfter(p.phaseIndex),
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
        private const val FOLLOW = "treadmill.follow"

        private val HALTED = setOf(BeltStatus.PAUSED, BeltStatus.STOPPED, BeltStatus.SAFETY_KEY, BeltStatus.STILL)

        /** The questions that go away when the belt moves again. */
        private val BELT_REASONS = setOf(FinishReason.BELT_STILL, FinishReason.BELT_STOPPED)
        internal val STILL_AFTER = 10.minutes
        internal val LOST_AFTER = 2.minutes
        internal val AUTO_FINISH_AFTER = 10.minutes
        private const val RECENT = "live.recent"
        private const val RECENT_COUNT = 3

        /** More than [RECENT_COUNT], so a deleted workout leaves room for the next one. */
        private const val RECENT_KEPT = 6

        /** The key prefix of a walk or a run from the catalog. */
        const val EXERCISE = "exercise:"

        private val WALK = ExerciseSlug("walk")
        private val RUN = ExerciseSlug("run")
        /** What Start records before anything was ever started. */
        private const val DEFAULT_CHOICE = "${EXERCISE}walk"

        /** A short workout to try plan checking on a real treadmill before the workout builder existed; History still reads it. */
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

        private fun run(id: String, target: Target, kmh: Double) = Step.ExerciseStep(
            id = StepId(id),
            exercise = TreadmillModule.treadmillRun,
            prescription = Prescription(listOf(SetPrescription(target, intensity = Intensity.Speed(kmh)))),
        )
    }
}
