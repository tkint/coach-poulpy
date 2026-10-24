package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.data.local.LocalStore
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlayerStatus { READY, RUNNING, PAUSED, FINISHED }

/** What the session screen shows. Everything is decided here; the screen only renders it. */
data class PlayerState(
    val status: PlayerStatus,
    val workoutName: String,
    val isRest: Boolean,
    /** The exercise name, or "Rest". */
    val title: String,
    val exerciseSlug: String?,
    val pattern: MovementPattern?,
    val setNumber: Int,
    val setCount: Int,
    val sideLabel: String?,
    val targetLabel: String?,
    val loadLabel: String?,
    /** True when the phase ends by itself; false when it waits for a tap on "done". */
    val isTimed: Boolean,
    val remainingSeconds: Int?,
    val totalSeconds: Int?,
    val elapsedSeconds: Int,
    /** The next exercise, so the user can get ready during a rest. */
    val nextTitle: String?,
    val nextDetail: String?,
    val phaseNumber: Int,
    val phaseCount: Int,
    val summary: SessionSummary?,
)

data class SessionSummary(val durationLabel: String, val setsDone: Int, val setsSkipped: Int, val setsPlanned: Int)

/**
 * The guided session. Every action is written to the phone's database the moment it
 * happens, so a session interrupted by a call or by the system killing the app picks
 * up where it was (see [AppGraph.resumeSession]).
 */
class PlayerModel internal constructor(
    private val workout: Workout,
    timeline: Timeline,
    private val catalog: Catalog,
    private val store: LocalStore,
    languageCode: String,
    private val executionId: String,
    events: List<SessionEvent>,
    private val clock: Clock = Clock.System,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val labels = Labels(languageCode)
    private val session = Session(timeline, events)
    private val steps = workout.steps.flatMap { it.exerciseSteps() }.associateBy { it.id }
    private val mutableState = MutableStateFlow(render())
    private var ticker: Job? = null

    val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    init {
        // A session reopened after the app was killed: catch up on the timers that ran out meanwhile.
        if (session.state(clock.now()).status == SessionState.Status.RUNNING) startTicker()
    }

    fun watch(onChange: (PlayerState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun start() {
        if (session.events.isNotEmpty()) return
        val now = clock.now()
        store.startExecution(
            Execution(
                id = ExecutionId(executionId),
                source = ExecutionSource.GUIDED,
                startedAt = now,
                workout = WorkoutRef(workout.id, workout.version),
            ),
        )
        act { session.start(now) }
        startTicker()
    }

    /** Marks the current set done (or ends a rest early). */
    fun done() = whileRunning { act { session.completeCurrent(clock.now()) } }

    fun skip() = whileRunning { act { session.skipCurrent(clock.now()) } }

    fun pause() = whileRunning { act { session.pause(clock.now()) } }

    fun resume() {
        if (current().status != SessionState.Status.PAUSED) return
        act { session.resume(clock.now()) }
        startTicker()
    }

    /** Ends the session now; what was done so far is kept. */
    fun stop() {
        val status = current().status
        if (status == SessionState.Status.RUNNING || status == SessionState.Status.PAUSED) act { session.finish(clock.now()) }
    }

    /** Moves timed phases on. The ticker calls it; tests call it with a fake clock. */
    fun tick() = act { session.tick(clock.now()) }

    fun close() = scope.cancel()

    private fun current() = session.state(clock.now())

    private fun whileRunning(action: () -> Unit) {
        if (current().status == SessionState.Status.RUNNING) action()
    }

    private fun act(action: () -> List<SessionEvent>) {
        action().forEach { event ->
            store.appendEvent(executionId, event)
            if (event is SessionEvent.Finished) {
                store.finishExecution(executionId, event.at)
                ticker?.cancel()
            }
        }
        mutableState.value = render()
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                tick()
                delay(TICK)
            }
        }
    }

    private fun render(): PlayerState {
        val now = clock.now()
        val state = session.state(now)
        val phases = session.timeline.phases
        val phase = state.phase
        val work = phase as? Phase.Work
        val step = work?.let { steps[it.step] }
        val exercise = work?.let { catalog[it.exercise] }
        val next = phases.drop(state.phaseIndex + 1).firstOrNull { it is Phase.Work } as? Phase.Work

        return PlayerState(
            status = when (state.status) {
                SessionState.Status.NOT_STARTED -> PlayerStatus.READY
                SessionState.Status.RUNNING -> PlayerStatus.RUNNING
                SessionState.Status.PAUSED -> PlayerStatus.PAUSED
                SessionState.Status.FINISHED -> PlayerStatus.FINISHED
            },
            workoutName = workout.name,
            isRest = phase is Phase.Rest,
            title = when {
                phase is Phase.Rest -> labels.rest
                exercise != null -> labels.text(exercise.name)
                else -> work?.exercise?.value ?: ""
            },
            exerciseSlug = work?.exercise?.value,
            pattern = exercise?.movementPattern,
            setNumber = if (work != null && step != null) step.setNumber(work.setIndex) else 0,
            setCount = step?.setCount() ?: 0,
            sideLabel = work?.let { labels.side(it.side) },
            targetLabel = work?.let { labels.amount(it.target.measurement, it.target.amount) },
            loadLabel = work?.let { step?.prescription?.sets?.getOrNull(it.setIndex)?.load?.label() },
            isTimed = phase?.duration != null,
            remainingSeconds = state.phaseRemaining?.let { ceilSeconds(it.inWholeMilliseconds) },
            totalSeconds = phase?.duration?.inWholeSeconds?.toInt(),
            elapsedSeconds = state.phaseElapsed.inWholeSeconds.toInt(),
            nextTitle = next?.let { n -> catalog[n.exercise]?.let { labels.text(it.name) } ?: n.exercise.value },
            nextDetail = next?.let { labels.amount(it.target.measurement, it.target.amount) },
            phaseNumber = (state.phaseIndex + 1).coerceAtMost(phases.size),
            phaseCount = phases.size,
            summary = if (state.status == SessionState.Status.FINISHED) summary() else null,
        )
    }

    private fun summary(): SessionSummary {
        val phases = session.timeline.phases
        val events = session.events
        fun workIndices(pick: (SessionEvent) -> Int?) = events.mapNotNull(pick).filter { phases.getOrNull(it) is Phase.Work }
        val done = workIndices { (it as? SessionEvent.PhaseCompleted)?.phaseIndex }.size
        val skipped = workIndices { (it as? SessionEvent.PhaseSkipped)?.phaseIndex }.size
        val start = events.firstOrNull { it is SessionEvent.Started }?.at
        val end = events.lastOrNull { it is SessionEvent.Finished }?.at
        val seconds = if (start != null && end != null) (end - start).inWholeSeconds.toInt() else 0
        return SessionSummary(labels.duration(seconds), done, skipped, phases.count { it is Phase.Work })
    }

    private fun Load.label(): String? = when (this) {
        is Load.Kilograms -> labels.kilograms(kg)
        is Load.Band -> label
        is Load.Bodyweight, is Load.PercentOfOneRepMax -> null
    }

    private companion object {
        val TICK = 200.milliseconds

        fun ceilSeconds(millis: Long): Int = ((millis + 999) / 1000).toInt()

        fun Step.exerciseSteps(): List<Step.ExerciseStep> = when (this) {
            is Step.ExerciseStep -> listOf(this)
            is Step.Group -> children.flatMap { it.exerciseSteps() }
            is Step.Rest -> emptyList()
        }

        /** Left and right halves of a set count as one set. */
        fun Step.ExerciseStep.setNumber(index: Int): Int =
            prescription.sets.take(index + 1).count { it.side != Side.RIGHT }

        fun Step.ExerciseStep.setCount(): Int = prescription.sets.count { it.side != Side.RIGHT }
    }
}
