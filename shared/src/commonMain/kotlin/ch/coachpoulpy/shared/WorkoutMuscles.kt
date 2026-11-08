package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Workout

/**
 * One muscle group a workout works: how many [sets] it gets, its [heat] from 1 (a little) to 3 (the most)
 * and [percent] of the busiest group's sets, for the bar of the "Muscles worked" list.
 */
data class MuscleLoad(val group: MuscleGroup, val label: String, val sets: Int, val setsLabel: String, val heat: Int, val percent: Int)

/** What a workout works: the list under the body map, and the heat of each area drawn on it. */
data class WorkoutMuscles(
    /** Busiest first; groups with the same sets keep the body map's reading order. */
    val loads: List<MuscleLoad> = emptyList(),
    /** Heat 1 to 3 of every area key (`deltoid-left`) the figure lights, both sides of a muscle together. */
    val heat: Map<String, Int> = emptyMap(),
) {
    companion object {
        val NONE = WorkoutMuscles()

        /** Heat 1 to 3 by the share of the busiest group's sets: below 40 % is 1, from 40 % is 2, from 75 % is 3. */
        fun heatOf(sets: Int, busiest: Int): Int = when {
            busiest <= 0 || sets <= 0 -> 0
            sets * 100 >= busiest * 75 -> 3
            sets * 100 >= busiest * 40 -> 2
            else -> 1
        }

        /**
         * The sets each muscle group gets from the work of [workout]: the exercises of its work parts, not the
         * warm-up or cool down and not recovery steps, each counted for all its sets and every round of the repeats around it.
         * An exercise that is not in the [catalog] adds nothing.
         */
        internal fun of(workout: Workout, catalog: Catalog, labels: Labels): WorkoutMuscles {
            val sets = linkedMapOf<MuscleGroup, Int>()
            fun open(steps: List<Step>, rounds: Int) {
                steps.forEach { step ->
                    when (step) {
                        is Step.ExerciseStep -> if (step.role == StepRole.WORK) {
                            val groups = catalog[step.exercise]?.muscleGroups.orEmpty()
                            groups.forEach { sets[it] = (sets[it] ?: 0) + step.prescription.sets.size * rounds }
                        }
                        is Step.Group -> open(step.children, rounds * step.rounds.coerceAtLeast(1))
                        is Step.Rest -> Unit
                    }
                }
            }
            workout.parts.filter { it.kind == PartKind.WORK }.forEach { open(it.steps, 1) }
            if (sets.isEmpty()) return NONE

            val busiest = sets.values.max()
            val order = MuscleGroup.entries
            val loads = sets.entries
                .sortedWith(compareByDescending<Map.Entry<MuscleGroup, Int>> { it.value }.thenBy { order.indexOf(it.key) })
                .map { (group, count) ->
                    MuscleLoad(group, labels.muscle(group), count, labels.sets(count), heatOf(count, busiest), (count * 100 + busiest / 2) / busiest)
                }
            // Full body lights every muscle, but any group named too lights its own areas at its own heat, whichever is more.
            val heat = linkedMapOf<String, Int>()
            loads.forEach { load ->
                BodyMap.worked(listOf(load.group)).forEach { area -> heat[area] = maxOf(heat[area] ?: 0, load.heat) }
            }
            return WorkoutMuscles(loads, BodyMap.AREAS.filter { it in heat }.associateWith { heat.getValue(it) })
        }
    }
}
