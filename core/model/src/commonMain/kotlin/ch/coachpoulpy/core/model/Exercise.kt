package ch.coachpoulpy.core.model

import kotlinx.serialization.Serializable

/**
 * A catalog entry. The slug is the stable key: workouts point at it, so an entry is
 * never deleted. One that is no longer offered is [retired], which hides it from the
 * catalog while workouts that already use it keep working. [aliases] are other names
 * people search with; they are never shown.
 */
@Serializable
data class Exercise(
    val slug: ExerciseSlug,
    val name: LocalizedText,
    val aliases: Aliases = Aliases(),
    val movementPattern: MovementPattern,
    val muscleGroups: List<MuscleGroup>,
    val equipment: List<Equipment> = emptyList(),
    val measurement: MeasurementMode,
    val perSide: Boolean = false,
    val images: List<String> = emptyList(),
    val instructions: List<LocalizedText> = emptyList(),
    val retired: Boolean = false,
)

/** Search-only names for an exercise, per language, like "RDL" or "abdos". */
@Serializable
data class Aliases(val en: List<String> = emptyList(), val fr: List<String> = emptyList()) {
    val all: List<String> get() = en + fr
}

@Serializable
enum class MovementPattern { SQUAT, HINGE, LUNGE, PUSH, PULL, CARRY, CORE, ROTATION, LOCOMOTION, MOBILITY, STRETCH }

@Serializable
enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, FOREARMS, CORE, GLUTES, QUADRICEPS, HAMSTRINGS, CALVES, HIPS, NECK, FULL_BODY
}

@Serializable
enum class Equipment { BODYWEIGHT, DUMBBELL, KETTLEBELL, BARBELL, BAND, MAT, BENCH, PULL_UP_BAR, TREADMILL, MACHINE }

@Serializable
enum class MeasurementMode { REPS, TIME, DISTANCE, HOLD }
