package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.data.local.LocalStore

/** What the import preview shows: the workout in the file, or why it can't be added. */
data class WorkoutImportState(
    /** True when the file can be added to the library. */
    val ready: Boolean,
    val name: String = "",
    /** "Circuit" or "Sets". */
    val formatLabel: String = "",
    /** "3 parts". */
    val partsLabel: String = "",
    /** "12 steps": exercises and rests, with a repeat's steps counted once. */
    val stepsLabel: String = "",
    /** Why the file was refused, in the person's language; empty when [ready]. */
    val errorMessage: String = "",
)

/** A workout file opened from another app: [state] for the preview, then [add] or nothing. */
class WorkoutImport internal constructor(
    fileText: String,
    private val store: LocalStore,
    catalog: Catalog,
    languageCode: String,
) {
    private val labels = Labels(languageCode)
    private val reading = WorkoutFile.read(
        text = fileText,
        catalog = catalog,
        existingNames = store.savedWorkouts().map { it.workout.name }.toSet(),
        copyName = labels::copyName,
    )

    val state: WorkoutImportState = reading.workout?.let { workout ->
        WorkoutImportState(
            ready = true,
            name = workout.name,
            formatLabel = labels.format(workout.format),
            partsLabel = labels.parts(workout.parts.size),
            stepsLabel = labels.steps(leafSteps(workout)),
        )
    } ?: WorkoutImportState(ready = false, errorMessage = labels.workoutFileProblem(reading.problem!!, reading.unknownExercises))

    /** Saves the workout as a new one in the library and returns its id; null when the file was refused. */
    fun add(): String? {
        val workout = reading.workout ?: return null
        store.saveWorkout(workout)
        return workout.id.value
    }

    private fun leafSteps(workout: Workout) = workout.steps.sumOf { if (it is Step.Group) it.children.size else 1 }
}
