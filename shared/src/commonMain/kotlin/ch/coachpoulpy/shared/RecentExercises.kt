package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.data.local.LocalStore
import kotlin.time.Instant

/**
 * The exercises used last, most recent first: an exercise is used when it is in a
 * session you did (from when it started) or in a workout you saved (from when it was
 * saved), and the later of those dates counts. Read from what the phone already keeps.
 */
internal fun recentlyUsedExercises(store: LocalStore): List<ExerciseSlug> {
    val lastUsed = mutableMapOf<ExerciseSlug, Instant>()
    fun used(slugs: List<ExerciseSlug>, at: Instant) = slugs.forEach { slug ->
        if (lastUsed[slug]?.let { it < at } != false) lastUsed[slug] = at
    }
    store.savedWorkouts().forEach { saved -> used(exerciseSteps(saved.workout).map { it.exercise }, saved.savedAt) }
    store.finishedWorkoutRefs().forEach { (ref, startedAt) ->
        store.workout(ref.workoutId, ref.version)?.let { used(exerciseSteps(it).map { step -> step.exercise }, startedAt) }
    }
    return lastUsed.entries.sortedByDescending { it.value }.map { it.key }
}
