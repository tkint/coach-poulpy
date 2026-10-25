package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.PlannedWorkout
import ch.coachpoulpy.data.local.StoredSession
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/** One exercise of the workout page: "3 × 10 each side · 14 kg · rest 60 s". */
data class WorkoutStepRow(
    val key: String,
    val slug: String,
    val name: String,
    val pattern: MovementPattern?,
    val prescription: String,
)

/** A calendar day, [month] 1 to 12, as the plan sheet picks it. */
data class PlanDate(val year: Int, val month: Int, val day: Int) {
    internal val date: LocalDate get() = LocalDate(year, month, day)

    internal companion object {
        fun of(date: LocalDate) = PlanDate(date.year, date.month.number, date.day)
    }
}

/** An upcoming day a workout is planned for. [dayMillis] is that day at midnight, local time; [daysAway] 0 for today. */
data class PlannedDay(val planId: String, val dayMillis: Long, val daysAway: Int, val date: PlanDate)

data class WorkoutPageState(
    val loaded: Boolean = false,
    /** False once the workout is gone, for example deleted from another screen. */
    val found: Boolean = false,
    val id: String = "",
    val name: String = "",
    val type: WorkoutType = WorkoutType.STRENGTH,
    val typeLabel: String = "",
    /** "5 exercises · about 40 min · dumbbells, mat". */
    val details: String = "",
    val steps: List<WorkoutStepRow> = emptyList(),
    val timesDone: Int = 0,
    val lastDoneMillis: Long? = null,
    /** The middle length of the sessions done, in minutes; null before the first. */
    val usualMinutes: Int? = null,
    /** Every upcoming planned day, soonest first; empty when the workout waits in the library. */
    val plans: List<PlannedDay> = emptyList(),
    /** Only workouts made with the builder can be edited or copied. */
    val editable: Boolean = false,
    val deletePrompt: DeletePrompt = DeletePrompt("", ""),
)

/** What deleting a workout asks: "Delete Upper body?", then what goes and what stays. */
data class DeletePrompt(val title: String, val message: String)

/** The upcoming days [workoutId] is planned for, soonest first; a plan for today already done is not shown again. */
internal fun upcomingDays(plans: List<PlannedWorkout>, workoutId: WorkoutId, today: LocalDate, doneToday: Boolean, zone: TimeZone) =
    plans.filter { it.workoutId == workoutId && (it.day > today || (it.day == today && !doneToday)) }
        .sortedBy { it.day }
        .map { PlannedDay(it.id, it.day.startMillis(zone), today.daysUntil(it.day), PlanDate.of(it.day)) }

/**
 * Plans [workoutId] on exactly [days] from [today] on, as ticked on the plan sheet:
 * [upcoming] plans not in [days] are removed, new days are added, and past days are
 * left as they are. An empty list keeps the workout unplanned.
 */
internal fun LocalStore.setDays(workoutId: WorkoutId, upcoming: List<PlannedDay>, days: List<PlanDate>, today: LocalDate) {
    val wanted = days.map { it.date }.filter { it >= today }.toSet()
    replacePlans(
        workoutId,
        add = (wanted - upcoming.map { it.date.date }.toSet()).sorted(),
        remove = upcoming.filter { it.date.date !in wanted }.map { it.planId },
    )
}

/** One saved workout: what is in it, how it went before, and the days it is planned for. */
class WorkoutPageModel internal constructor(
    private val workoutId: WorkoutId,
    private val store: LocalStore,
    compiler: TimelineCompiler,
    private val catalog: Catalog,
    languageCode: String,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val labels = Labels(languageCode)
    private val facts = WorkoutFacts(compiler, catalog, labels)

    val state: StateFlow<WorkoutPageState> =
        combine(store.workouts(), store.plannedWorkouts(), store.finishedSessions(), ::build)
            .stateIn(scope, SharingStarted.Eagerly, WorkoutPageState())

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: WorkoutPageState get() = state.value

    fun watch(onChange: (WorkoutPageState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    /** Plans the workout on exactly [days] from today on, as ticked on the plan sheet ([LocalStore.setDays]). */
    fun setDays(days: List<PlanDate>) = store.setDays(workoutId, state.value.plans, days, clock.now().toLocalDateTime(zone).date)

    fun delete() = store.deleteWorkout(workoutId)

    fun close() = scope.cancel()

    private fun build(workouts: List<Workout>, plans: List<PlannedWorkout>, sessions: List<StoredSession>): WorkoutPageState {
        val workout = workouts.firstOrNull { it.id == workoutId } ?: return WorkoutPageState(loaded = true, found = false)
        val today = clock.now().toLocalDateTime(zone).date
        val done = sessions.filter { it.counts && it.execution.workout?.workoutId == workoutId }
        val doneToday = done.any { it.execution.startedAt.toLocalDateTime(zone).date == today }
        val upcoming = upcomingDays(plans, workoutId, today, doneToday, zone)
        val minutes = done.mapNotNull { it.realTotals?.active?.inWholeMinutes?.toInt() }.sorted()
        val equipment = exerciseSteps(workout).mapNotNull { catalog[it.exercise] }
            .flatMap { it.equipment.flatten() }
            .distinct()
            .map { labels.equipment(it).lowercase() }
        return WorkoutPageState(
            loaded = true,
            found = true,
            id = workout.id.value,
            name = workout.name,
            type = workout.type,
            typeLabel = labels.workoutType(workout.type),
            details = (listOf(labels.exercises(exerciseSteps(workout).size), labels.aboutMinutes(facts.minutes(workout))) +
                listOfNotNull(equipment.takeIf { it.isNotEmpty() }?.joinToString(", "))).joinToString(" · "),
            steps = exerciseSteps(workout).map(::stepRow),
            timesDone = done.size,
            lastDoneMillis = done.maxOfOrNull { it.execution.startedAt }?.toEpochMilliseconds(),
            usualMinutes = minutes.getOrNull(minutes.size / 2)?.coerceAtLeast(1),
            plans = upcoming,
            deletePrompt = labels.deletePrompt(workout.name, upcoming.size, done.size),
            editable = WorkoutDraft.from(workout, catalog) != null,
        )
    }

    private fun stepRow(step: Step.ExerciseStep): WorkoutStepRow {
        val exercise = catalog[step.exercise]
        val entry = DraftEntry.from(step, exercise)
        val prescription = entry?.let(labels::prescription) ?: "${step.prescription.sets.size} ×"
        return WorkoutStepRow(
            key = step.id.value,
            slug = step.exercise.value,
            name = exercise?.let { labels.text(it.name) } ?: step.exercise.value,
            pattern = exercise?.movementPattern,
            prescription = prescription,
        )
    }
}
