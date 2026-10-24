package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.WorkoutType

/** One row of the exercise list, already localized so the UI only renders it. */
data class ExerciseRow(val slug: String, val name: String, val details: String, val pattern: MovementPattern)

/** A muscle group the list can be narrowed to. */
data class MuscleFilter(val group: MuscleGroup, val label: String)

data class ExerciseDetail(
    val slug: String,
    val name: String,
    val pattern: MovementPattern,
    val patternLabel: String,
    val muscles: String,
    val equipment: String,
    val perSide: Boolean,
    val instructions: List<String>,
)

/** The catalog as the exercise screens and the workout builder's picker show it. */
class ExerciseListModel internal constructor(
    private val catalog: Catalog,
    private val registry: WorkoutTypeRegistry,
    languageCode: String,
) {
    private val labels = Labels(languageCode)

    val rows: List<ExerciseRow> get() = filter()

    val muscleFilters: List<MuscleFilter> = catalog.offered.flatMap { it.muscleGroups }.distinct()
        .sortedBy { it.ordinal }
        .map { MuscleFilter(it, labels.muscle(it)) }

    /**
     * Offered exercises matching [query] (name in either language) and [muscle], sorted
     * by name. With [type], only exercises that workout type can run are listed.
     */
    fun filter(query: String = "", muscle: MuscleGroup? = null, type: WorkoutType? = null): List<ExerciseRow> {
        val needle = query.trim().lowercase()
        return catalog.offered
            .filter { needle.isEmpty() || needle in it.name.en.lowercase() || needle in it.name.fr.lowercase() }
            .filter { muscle == null || muscle in it.muscleGroups }
            .filter { type == null || registry.accepts(type, it.measurement) }
            .map { ExerciseRow(it.slug.value, labels.text(it.name), details(it), it.movementPattern) }
            .sortedBy { it.name.lowercase() }
    }

    fun detail(slug: String): ExerciseDetail? = catalog[ExerciseSlug(slug)]?.let { exercise ->
        ExerciseDetail(
            slug = exercise.slug.value,
            name = labels.text(exercise.name),
            pattern = exercise.movementPattern,
            patternLabel = labels.pattern(exercise.movementPattern),
            muscles = exercise.muscleGroups.joinToString(", ") { labels.muscle(it) },
            equipment = exercise.equipment.joinToString(", ") { labels.equipment(it) },
            perSide = exercise.perSide,
            instructions = exercise.instructions.map { labels.text(it) },
        )
    }

    private fun details(exercise: Exercise) = exercise.muscleGroups.joinToString(" · ") { labels.muscle(it) }
}
