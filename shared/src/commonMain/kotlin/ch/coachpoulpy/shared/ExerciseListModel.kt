package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.WorkoutType

/**
 * One row of the exercise list, already localized so the UI only renders it.
 * [details] is two muscles then one fact: "Chest, triceps · No equipment".
 */
data class ExerciseRow(val slug: String, val name: String, val details: String, val pattern: MovementPattern)

/** Rows under a movement pattern's [title], or untitled for search results. */
data class ExerciseSection(val title: String?, val rows: List<ExerciseRow>)

/**
 * The exercise list as shown: grouped by movement pattern while nothing is typed, one
 * untitled section of ranked results while searching, and the count line over it.
 */
data class ExerciseListing(
    val sections: List<ExerciseSection>,
    val count: Int,
    /** "34 exercises", or "3 matches" while searching. */
    val countLabel: String,
    val searching: Boolean,
) {
    val rows: List<ExerciseRow> get() = sections.flatMap { it.rows }
}

/** A broad muscle chip of the exercise list; every [MuscleGroup] belongs to exactly one. */
enum class MuscleArea(val groups: Set<MuscleGroup>) {
    LEGS(setOf(MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.HIPS)),
    GLUTES(setOf(MuscleGroup.GLUTES)),
    CORE(setOf(MuscleGroup.CORE)),
    BACK(setOf(MuscleGroup.BACK)),
    CHEST(setOf(MuscleGroup.CHEST)),
    SHOULDERS(setOf(MuscleGroup.SHOULDERS, MuscleGroup.NECK)),
    ARMS(setOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS, MuscleGroup.FOREARMS)),
    FULL_BODY(setOf(MuscleGroup.FULL_BODY)),
}

/** A chip the list can be narrowed to; "All" is no filter. */
data class MuscleFilter(val area: MuscleArea, val label: String)

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

    val muscleFilters: List<MuscleFilter> = MuscleArea.entries.map { MuscleFilter(it, labels.muscleArea(it)) }

    /**
     * Offered exercises matching [query] and [area]. With [type], only exercises that
     * workout type can run are listed. With a query, one section of the best matches
     * first ([ExerciseSearch]); without, a section per movement pattern, by name.
     */
    fun filter(query: String = "", area: MuscleArea? = null, type: WorkoutType? = null): ExerciseListing {
        val candidates = catalog.offered
            .filter { area == null || it.muscleGroups.any { group -> group in area.groups } }
            .filter { type == null || registry.accepts(type, it.measurement) }
        val ranks = search.rank(query)
        val sections = if (ranks == null) {
            MovementPattern.entries.mapNotNull { pattern ->
                candidates.filter { it.movementPattern == pattern }.sortedWith(byName).takeIf { it.isNotEmpty() }
                    ?.let { ExerciseSection(labels.pattern(pattern), it.map(::row)) }
            }
        } else {
            candidates.filter { it.slug in ranks }
                .sortedWith(compareBy<Exercise> { ranks.getValue(it.slug) }.then(byName))
                .takeIf { it.isNotEmpty() }
                ?.let { listOf(ExerciseSection(null, it.map(::row))) }
                ?: emptyList()
        }
        val count = sections.sumOf { it.rows.size }
        val searching = ranks != null
        return ExerciseListing(sections, count, if (searching) labels.matches(count) else labels.exercises(count), searching)
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

    private fun row(exercise: Exercise) =
        ExerciseRow(exercise.slug.value, labels.text(exercise.name), details(exercise), exercise.movementPattern)

    private fun details(exercise: Exercise): String {
        val muscles = exercise.muscleGroups.take(2).map(labels::muscle).sentence()
        return listOf(muscles, fact(exercise)).filter { it.isNotEmpty() }.joinToString(" · ")
    }

    /** What the exercise needs, else how it is done: "Dumbbells, bench", "Hold", "Each side", "Timed". */
    private fun fact(exercise: Exercise): String {
        val gear = if (Equipment.BODYWEIGHT in exercise.equipment) emptyList() else exercise.equipment - Equipment.MAT
        val main = when {
            gear.isNotEmpty() -> gear.map(labels::equipment).sentence()
            exercise.measurement == MeasurementMode.HOLD -> labels.hold
            exercise.perSide -> return labels.perSide.capitalized()
            exercise.measurement == MeasurementMode.TIME -> labels.timed
            Equipment.MAT in exercise.equipment -> labels.equipment(Equipment.MAT)
            else -> labels.equipment(Equipment.BODYWEIGHT)
        }
        return if (exercise.perSide) "$main · ${labels.perSide}" else main
    }

    /** "Chest, triceps": the first word as it is, the others in lower case. */
    private fun List<String>.sentence() = mapIndexed { index, word -> if (index == 0) word else word.lowercase() }.joinToString(", ")

    private fun String.capitalized() = replaceFirstChar { it.uppercaseChar() }
}
