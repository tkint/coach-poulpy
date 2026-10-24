package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.MeasurementMode
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
import kotlinx.datetime.toLocalDateTime

/** One exercise of the workout page: "3 × 10 each side · 14 kg · rest 60 s". */
data class WorkoutStepRow(
    val key: String,
    val slug: String,
    val name: String,
    val pattern: MovementPattern?,
    val prescription: String,
)

/** The next day a workout is planned for. [dayMillis] is that day at midnight, local time; [daysAway] 0 for today. */
data class PlannedDay(val planId: String, val dayMillis: Long, val daysAway: Int)

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
    val plan: PlannedDay? = null,
    /** Only workouts made with the builder can be edited. */
    val editable: Boolean = false,
)

/** One saved workout: what is in it, how it went before, and the day it is planned for. */
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

    /** Plans the workout on a day picked on a calendar ([month] 1 to 12), or moves its next plan there. */
    fun plan(year: Int, month: Int, day: Int) {
        val date = LocalDate(year, month, day)
        val current = state.value.plan
        if (current != null) store.movePlan(current.planId, date) else store.planWorkout(workoutId, date)
    }

    /** Takes the workout off the day it is planned for. */
    fun unplan() {
        state.value.plan?.let { store.removePlan(it.planId) }
    }

    fun delete() = store.deleteWorkout(workoutId)

    fun close() = scope.cancel()

    private fun build(workouts: List<Workout>, plans: List<PlannedWorkout>, sessions: List<StoredSession>): WorkoutPageState {
        val workout = workouts.firstOrNull { it.id == workoutId } ?: return WorkoutPageState(loaded = true, found = false)
        val today = clock.now().toLocalDateTime(zone).date
        val done = sessions.filter { it.counts && it.execution.workout?.workoutId == workoutId }
        val doneToday = done.any { it.execution.startedAt.toLocalDateTime(zone).date == today }
        val plan = plans.firstOrNull { it.workoutId == workoutId && (it.day > today || (it.day == today && !doneToday)) }
        val minutes = done.mapNotNull { it.realTotals?.active?.inWholeMinutes?.toInt() }.sorted()
        val equipment = exerciseSteps(workout).mapNotNull { catalog[it.exercise] }
            .flatMap { it.equipment }
            .filter { it != Equipment.BODYWEIGHT }
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
            plan = plan?.let { PlannedDay(it.id, it.day.startMillis(zone), today.daysUntil(it.day)) },
            editable = WorkoutDraft.from(workout, catalog) != null,
        )
    }

    private fun stepRow(step: Step.ExerciseStep): WorkoutStepRow {
        val exercise = catalog[step.exercise]
        val entry = DraftEntry.from(step, exercise)
        val prescription = if (entry == null) {
            "${step.prescription.sets.size} ×"
        } else {
            val amount = when (entry.measurement) {
                MeasurementMode.REPS -> entry.amount.toString()
                else -> labels.amount(entry.measurement, entry.amount)
            }
            listOfNotNull(
                "${entry.sets} × $amount" + if (entry.perSide) " ${labels.perSide}" else "",
                entry.loadKg?.let(labels::kilograms),
                entry.restSeconds.takeIf { it > 0 }?.let(labels::restAfter),
            ).joinToString(" · ")
        }
        return WorkoutStepRow(
            key = step.id.value,
            slug = step.exercise.value,
            name = exercise?.let { labels.text(it.name) } ?: step.exercise.value,
            pattern = exercise?.movementPattern,
            prescription = prescription,
        )
    }
}
