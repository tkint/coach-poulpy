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

/** One variation on an exercise's page; [current] marks the exercise the page is about. */
data class VariationRow(val slug: String, val name: String, val current: Boolean)

data class ExerciseDetail(
    val slug: String,
    val name: String,
    val pattern: MovementPattern,
    val patternLabel: String,
    val muscles: String,
    val equipment: String,
    val perSide: Boolean,
    val instructions: List<String>,
    /** Easiest to hardest, this exercise included; empty when it has no variations. */
    val variations: List<VariationRow>,
)

/** The catalog as the exercise screens and the workout builder's picker show it. */
class ExerciseListModel internal constructor(
    private val catalog: Catalog,
    private val registry: WorkoutTypeRegistry,
    languageCode: String,
) {
    private val labels = Labels(languageCode)
    private val search = ExerciseSearch(catalog.offered)

    /**
     * By name, with a variation group kept together easiest to hardest and placed by
     * its easiest member's name.
     */
    private val byName: Comparator<Exercise> = compareBy<Exercise>(
        { SearchText.fold(labels.text((catalog.variationsOf(it.slug).firstOrNull() ?: it).name)) },
        { catalog.variationsOf(it.slug).indexOf(it) },
    )

    val rows: List<ExerciseRow> get() = filter()

    val muscleFilters: List<MuscleFilter> = catalog.offered.flatMap { it.muscleGroups }.distinct()
        .sortedBy { it.ordinal }
        .map { MuscleFilter(it, labels.muscle(it)) }

    /**
     * Offered exercises matching [query] and [muscle]. With [type], only exercises that
     * workout type can run are listed. With a query, the best matches come first
     * ([ExerciseSearch]); otherwise, and inside a rank, they go by name.
     */
    fun filter(query: String = "", muscle: MuscleGroup? = null, type: WorkoutType? = null): List<ExerciseRow> {
        val candidates = catalog.offered
            .filter { muscle == null || muscle in it.muscleGroups }
            .filter { type == null || registry.accepts(type, it.measurement) }
        val ranks = search.rank(query)
        val listed = if (ranks == null) {
            candidates.sortedWith(byName)
        } else {
            candidates.filter { it.slug in ranks }.sortedWith(compareBy<Exercise> { ranks.getValue(it.slug) }.then(byName))
        }
        return listed.map { ExerciseRow(it.slug.value, labels.text(it.name), details(it), it.movementPattern) }
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
            variations = catalog.variationsOf(exercise.slug).map {
                VariationRow(it.slug.value, labels.text(it.name), current = it.slug == exercise.slug)
            },
        )
    }

    private fun details(exercise: Exercise) = exercise.muscleGroups.joinToString(" · ") { labels.muscle(it) }
}
