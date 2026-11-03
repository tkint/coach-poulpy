package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.PartKind
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
    /** The part this step is in, for its icon; a rest takes the part of the step before it. */
    val partKind: PartKind?,
    /** "Warm-up", "Work 2", or "Work 1 · round 2 of 4" inside a repeat. */
    val partLabel: String?,
    /** "Recover" on rests and recover steps; null for work steps. */
    val roleLabel: String?,
    /** True inside a repeat, whose rounds a rest shows beside its part. */
    val inRepeat: Boolean,
    val setNumber: Int,
    /** How many sets "Set 2 of 3" counts; 0 in a circuit or a repeat, which count rounds instead. */
    val setCount: Int,
    val sideLabel: String?,
    val targetLabel: String?,
    /** The big number of a rep target ("8"); null for a time, a distance or an open target, which [targetLabel] says whole. */
    val targetValue: String?,
    /** What goes under [targetValue]: "reps", or "reps at 40 kg" when the set has a load. */
    val targetUnit: String?,
    /** What the step asks for, with its load: "8 reps at 16 kg". On the first exercise it is the "first up" line. */
    val targetDetail: String?,
    val loadLabel: String?,
    /** True when the phase ends by itself; false when it waits for a tap on "done". */
    val isTimed: Boolean,
    val remainingSeconds: Int?,
    val totalSeconds: Int?,
    val elapsedSeconds: Int,
    /** "5 exercises", on the screen before the first one. */
    val exerciseCountLabel: String,
    /** Time since the session started, without the pauses: "09:12". */
    val sessionClock: String,
    /** The next exercise, so the user can get ready during a rest. */
    val nextTitle: String?,
    /** [nextTitle] with its set in a Sets workout: "Goblet squat · set 3 of 4". */
    val nextHeadline: String?,
    /** What the next exercise asks for, with its load: "8 reps at 16 kg". */
    val nextDetail: String?,
    /** The one line under "Next" while an exercise runs: "Rest 2 min, then set 3", "Push-up · 10 reps". Null on the last step. */
    val nextLine: String?,
    /** "round 3 of 4" when the next exercise starts another round; null otherwise. */
    val nextRound: String?,
    val phaseNumber: Int,
    val phaseCount: Int,
)

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
    private val languageCode: String,
    private val units: () -> UnitSystem = { UnitSystem.METRIC },
    private val sessionId: String,
    events: List<SessionEvent>,
    private val clock: Clock = Clock.System,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val face = PlayerFace(workout, catalog, languageCode, units)
    private val session = Session(timeline, events)
    private val mutableState = MutableStateFlow(render())
    private var ticker: Job? = null

    val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    init {
        // A session reopened after the app was closed comes back paused at its last event: the time the app was
        // gone does not count, and Resume carries on whenever the user is ready.
        val last = session.events.lastOrNull()
        if (last != null && session.state(last.at).status == SessionState.Status.RUNNING) act { session.pause(last.at) }
    }

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: PlayerState get() = state.value

    fun watch(onChange: (PlayerState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun start() {
        if (session.events.isNotEmpty()) return
        val now = clock.now()
        store.startSession(
            SessionRecord(
                id = SessionId(sessionId),
                source = SessionSource.GUIDED,
                startedAt = now,
                workout = WorkoutRef(workout.id, workout.version),
            ),
            title = workout.name,
            format = workout.format,
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

    /** Adds [seconds] to the running rest, or removes them when negative. */
    fun adjustRest(seconds: Int) = whileRunning { act { session.adjustRest(clock.now(), seconds) } }

    /** Ends the session now; what was done so far is kept. */
    fun stop() {
        val status = current().status
        if (status == SessionState.Status.RUNNING || status == SessionState.Status.PAUSED) act { session.finish(clock.now()) }
    }

    /** Drops the session with everything it recorded, as if it never happened. */
    fun discard() {
        ticker?.cancel()
        store.deleteSession(sessionId)
    }

    /** The picture to share once the session is finished; null before. */
    fun shareCard(units: UnitSystem): ShareCard? = finished(units)?.let { ShareCard.of(it, languageCode) }

    /**
     * The finished screen's heading, name, date, summary, stats and Poulpy; null before the session is finished.
     * Poulpy's line is the one for the tentacle this session lit.
     */
    fun finished(units: UnitSystem): FinishedScreen? {
        if (current().status != SessionState.Status.FINISHED) return null
        val poulpy = PoulpyCoach(languageCode).status(store.finishedSessionEndsNow(), clock.now())
        return FinishedScreens.of(workout.name, workout, session.timeline, session.events, poulpy, units, languageCode)
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
            store.appendEvent(sessionId, event)
            if (event is SessionEvent.Finished) {
                store.finishSession(sessionId, event.at)
                store.saveTotals(sessionId, sessionTotals(session.events, session.timeline))
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

    private fun render(): PlayerState = face.of(session, clock.now())

    private companion object {
        val TICK = 200.milliseconds
    }
}
