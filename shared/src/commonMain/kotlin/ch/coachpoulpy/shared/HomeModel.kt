package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.PlannedWorkout
import ch.coachpoulpy.data.local.StoredSession
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

enum class TodayStatus { TO_DO, IN_PROGRESS, DONE }

/** One thing of today: a workout planned for today, a session left halfway, or one done today. */
data class TodayItem(
    val key: String,
    val status: TodayStatus,
    val title: String,
    /** "Strength · 5 exercises · about 40 min"; empty for a session done. */
    val details: String,
    /** The exercises in order, for a workout still to do; empty otherwise. */
    val exercises: String,
    /** The saved workout, to open its page or start it. Null for a session whose workout is gone. */
    val workoutId: String?,
    /** The plan this item comes from, when it was planned. */
    val planId: String?,
    /** The finished session, for [TodayStatus.DONE]. */
    val session: SessionRow?,
)

/** The first workout planned after today. [dayMillis] is that day at midnight, local time. */
data class NextSession(
    val planId: String,
    val workoutId: String,
    val title: String,
    val details: String,
    val dayMillis: Long,
    /** 1 for tomorrow. */
    val daysAway: Int,
)

data class HomeState(
    /** False until everything was read once, so Home does not flash "nothing planned". */
    val loaded: Boolean = false,
    val poulpy: PoulpyStatus? = null,
    /** In progress first, then still to do, then done. Empty on a day with nothing planned or done. */
    val today: List<TodayItem> = emptyList(),
    val next: NextSession? = null,
    /** False before the first workout is saved: Home then points to the library to make one. */
    val hasWorkouts: Boolean = false,
)

/**
 * Home: Poulpy with his tentacles, what there is to do today or was done today, and the
 * next planned session. Workouts without a day live in the library.
 */
class HomeModel internal constructor(
    private val store: LocalStore,
    compiler: TimelineCompiler,
    catalog: Catalog,
    languageCode: String,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val labels = Labels(languageCode)
    private val coach = PoulpyCoach(languageCode, zone)
    private val facts = WorkoutFacts(compiler, catalog, labels)

    /** The session left open. It is not a flow in the store, so the screens call [refresh] when they show. */
    private val unfinished = MutableStateFlow(openInPlayer())

    val state: StateFlow<HomeState> = combine(
        store.workouts(),
        store.plannedWorkouts(),
        store.finishedSessions(),
        store.finishedSessionEnds(),
        unfinished,
    ) { workouts, plans, sessions, ends, open -> build(workouts, plans, sessions, ends, open) }
        .stateIn(scope, SharingStarted.Eagerly, HomeState())

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: HomeState get() = state.value

    fun watch(onChange: (HomeState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    /** Reads the session left open again, for example after coming back from the player. */
    fun refresh() {
        unfinished.value = openInPlayer()
    }

    /** A Live recording left open comes back in Live, not on Home's Resume card. */
    private fun openInPlayer() = store.unfinishedExecution()?.takeUnless { it.source.isLive }

    fun deleteSession(sessionId: String) = store.deleteSession(sessionId)

    fun close() = scope.cancel()

    private fun build(
        workouts: List<Workout>,
        plans: List<PlannedWorkout>,
        sessions: List<StoredSession>,
        ends: List<Instant>,
        open: Execution?,
    ): HomeState {
        val now = clock.now()
        val today = now.toLocalDateTime(zone).date
        val byId = workouts.associateBy { it.id }
        val livePlans = plans.filter { it.workoutId in byId }

        val doneToday = sessions
            .filter { it.execution.startedAt.toLocalDateTime(zone).date == today }
            .mapNotNull { session -> session.realTotals?.let { session to it } }
            .sortedBy { (session, _) -> session.execution.startedAt }

        // Each session done today ticks off one plan of today for the same workout.
        val todo = livePlans.filter { it.day == today }.toMutableList()
        doneToday.forEach { (session, _) ->
            val id = session.execution.workout?.workoutId ?: return@forEach
            todo.firstOrNull { it.workoutId == id }?.let(todo::remove)
        }

        val openWorkout = open?.workout?.let { ref -> store.workout(ref.workoutId, ref.version) }
        val openPlan = openWorkout?.let { workout -> todo.firstOrNull { it.workoutId == workout.id } }
        val items = buildList {
            if (open != null && openWorkout != null) add(item(TodayStatus.IN_PROGRESS, "open:${open.id.value}", byId[openWorkout.id] ?: openWorkout, openPlan))
            todo.filter { it != openPlan }.forEach { plan -> add(item(TodayStatus.TO_DO, "plan:${plan.id}", byId.getValue(plan.workoutId), plan)) }
            doneToday.forEach { (session, totals) ->
                val row = sessionRow(session, totals, labels)
                add(TodayItem("done:${row.id}", TodayStatus.DONE, row.title, "", "", session.execution.workout?.workoutId?.value?.takeIf { WorkoutId(it) in byId }, null, row))
            }
        }

        val next = livePlans.firstOrNull { it.day > today }?.let { plan ->
            val workout = byId.getValue(plan.workoutId)
            NextSession(plan.id, workout.id.value, workout.name, facts.details(workout), plan.day.startMillis(zone), today.daysUntil(plan.day))
        }

        return HomeState(
            loaded = true,
            poulpy = coach.status(ends, now),
            today = items,
            next = next,
            hasWorkouts = workouts.isNotEmpty(),
        )
    }

    private fun item(status: TodayStatus, key: String, workout: Workout, plan: PlannedWorkout?) = TodayItem(
        key = key,
        status = status,
        title = workout.name,
        details = facts.details(workout),
        exercises = facts.exerciseNames(workout),
        workoutId = workout.id.value,
        planId = plan?.id,
        session = null,
    )
}

