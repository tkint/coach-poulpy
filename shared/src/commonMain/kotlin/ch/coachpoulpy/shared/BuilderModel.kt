package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.LocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One exercise as the builder screen shows it. */
data class BuilderRow(
    val key: String,
    val name: String,
    val pattern: MovementPattern,
    val sets: Int,
    val amount: Int,
    val amountLabel: String,
    /** True when [amount] is a duration in seconds, so the UI can show a timer icon. */
    val isTimed: Boolean,
    val perSideLabel: String?,
    val loadKg: Double?,
    val restSeconds: Int,
    val restLabel: String,
    /** False when the chosen workout type cannot run this exercise. */
    val fitsType: Boolean,
    /** The folded card's line, as on the workout page: "3 × 10 · 20 kg · rest 90 s". */
    val summary: String,
)

data class BuilderState(
    val name: String,
    val type: WorkoutType,
    val typeLabel: String,
    val rows: List<BuilderRow>,
    val canSave: Boolean,
)

data class WorkoutTypeOption(val type: WorkoutType, val label: String)

/** The workout builder: pick exercises, then set sets, reps or time, weight and rest for each. */
class BuilderModel internal constructor(
    initial: WorkoutDraft,
    private val catalog: Catalog,
    private val registry: WorkoutTypeRegistry,
    private val store: LocalStore,
    languageCode: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val labels = Labels(languageCode)
    private var draft = initial
    private val mutableState = MutableStateFlow(render(initial))

    val isEditing: Boolean = initial.id != null

    val typeOptions: List<WorkoutTypeOption> = BUILDER_TYPES.map { WorkoutTypeOption(it, labels.workoutType(it)) }

    val state: StateFlow<BuilderState> = mutableState.asStateFlow()

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: BuilderState get() = state.value

    fun watch(onChange: (BuilderState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun rename(name: String) = edit { copy(name = name) }

    fun setType(type: WorkoutType) = edit { copy(type = type) }

    fun add(slug: String) {
        val exercise = catalog[ExerciseSlug(slug)] ?: return
        edit { copy(entries = entries + DraftEntry.new(exercise, type)) }
    }

    fun remove(key: String) = edit { copy(entries = entries.filterNot { it.key == key }) }

    fun moveUp(key: String) = move(key, -1)
    fun moveDown(key: String) = move(key, +1)

    fun setSets(key: String, sets: Int) = editEntry(key) { copy(sets = sets.coerceIn(1, 20)) }

    fun changeAmount(key: String, steps: Int) = editEntry(key) {
        val step = DraftEntry.amountStep(measurement)
        copy(amount = (amount + steps * step).coerceIn(step, 1000 * step))
    }

    /** Sets the weight; zero or less means no weight. */
    fun setLoadKg(key: String, kg: Double) = editEntry(key) { copy(loadKg = kg.takeIf { it > 0.0 }) }

    fun changeRest(key: String, steps: Int) = editEntry(key) {
        copy(restSeconds = (restSeconds + steps * REST_STEP).coerceIn(0, 600))
    }

    /** Saves the workout and returns its id, or null when it is not ready ([BuilderState.canSave]). */
    fun save(): String? {
        if (!state.value.canSave) return null
        val workout = draft.toWorkout()
        store.saveWorkout(workout)
        draft = draft.copy(id = workout.id, version = workout.version + 1)
        return workout.id.value
    }

    fun close() = scope.cancel()

    private fun edit(change: WorkoutDraft.() -> WorkoutDraft) {
        draft = draft.change()
        mutableState.value = render(draft)
    }

    private fun editEntry(key: String, change: DraftEntry.() -> DraftEntry) = edit {
        copy(entries = entries.map { if (it.key == key) it.change() else it })
    }

    private fun move(key: String, by: Int) = edit {
        val from = entries.indexOfFirst { it.key == key }
        val to = from + by
        if (from < 0 || to !in entries.indices) return@edit this
        copy(entries = entries.toMutableList().apply { add(to, removeAt(from)) })
    }

    private fun render(draft: WorkoutDraft): BuilderState {
        val rows = draft.entries.map { entry ->
            val exercise = catalog[entry.exercise]
            BuilderRow(
                key = entry.key,
                name = exercise?.let { labels.text(it.name) } ?: entry.exercise.value,
                pattern = exercise?.movementPattern ?: MovementPattern.CORE,
                sets = entry.sets,
                amount = entry.amount,
                amountLabel = labels.amount(entry.measurement, entry.amount),
                isTimed = entry.measurement == MeasurementMode.TIME || entry.measurement == MeasurementMode.HOLD,
                perSideLabel = if (entry.perSide) labels.perSide else null,
                loadKg = entry.loadKg,
                restSeconds = entry.restSeconds,
                restLabel = labels.duration(entry.restSeconds),
                fitsType = registry.accepts(draft.type, entry.measurement),
                summary = labels.prescription(entry),
            )
        }
        return BuilderState(
            name = draft.name,
            type = draft.type,
            typeLabel = labels.workoutType(draft.type),
            rows = rows,
            canSave = draft.name.isNotBlank() && rows.isNotEmpty() && rows.all { it.fitsType },
        )
    }

    private companion object {
        const val REST_STEP = 15
        val BUILDER_TYPES = listOf(WorkoutType.STRENGTH, WorkoutType.CONDITIONING, WorkoutType.MOBILITY, WorkoutType.REHAB)
    }
}
