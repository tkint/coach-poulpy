package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.data.local.LocalStore

/** Whether a workout file can be added, and if not, why, for the refused screen. */
data class WorkoutImportState(
    /** True when the file can be added to the library. */
    val ready: Boolean,
    /** Why the file was refused, in the person's language; empty when [ready]. */
    val errorMessage: String = "",
)

/**
 * A workout file opened from another app. A good file opens in the builder as an unsaved copy, so saving there adds
 * it to the library and leaving adds nothing. A workout the builder can't hold (a repeat with a rest between rounds,
 * for one) has no [builder]: [add] saves it and the workout page opens. A refused file only has [state] to show.
 */
class WorkoutImport internal constructor(
    fileText: String,
    private val store: LocalStore,
    private val catalog: Catalog,
    private val compiler: TimelineCompiler,
    private val languageCode: String,
    private val units: () -> UnitSystem,
) {
    private val labels = Labels(languageCode)
    private val reading = WorkoutFile.read(
        text = fileText,
        catalog = catalog,
        existingNames = store.savedWorkouts().map { it.workout.name }.toSet(),
        autoName = { it.autoName(catalog, labels) },
        copyName = labels::copyName,
    )
    private val draft = reading.workout?.let { WorkoutDraft.from(it, catalog) }

    val state: WorkoutImportState = if (reading.workout != null) {
        WorkoutImportState(ready = true)
    } else {
        WorkoutImportState(
            ready = false,
            errorMessage = labels.workoutFileProblem(reading.problem!!, reading.unknownExercises),
        )
    }

    /** True when [builder] holds the workout; otherwise [add] is the way in. */
    val opensInBuilder: Boolean = draft != null

    /**
     * The builder holding the file's workout as a new, unsaved one: a new id, version 1 and the name the import gave
     * it. Null when the file was refused or the builder can't hold the workout.
     */
    fun builder(): BuilderModel? =
        draft?.let { BuilderModel(it.copy(id = null, version = 1), catalog, store, compiler, languageCode, units) }

    /** Saves the workout as a new one in the library and returns its id; null when the file was refused. */
    fun add(): String? {
        val workout = reading.workout ?: return null
        store.saveWorkout(workout)
        return workout.id.value
    }
}
