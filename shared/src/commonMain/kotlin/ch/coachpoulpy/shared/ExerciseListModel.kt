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
    /** Exercises listed, each once: the "Recently used" section repeats some and isn't counted. */
    val count: Int,
    /** "34 exercises", or "3 matches" while searching; "… you can do" while [restricted]. */
    val countLabel: String,
    val searching: Boolean,
    /** True while the equipment filter is on. */
    val restricted: Boolean,
) {
    /** Every row once, in list order. */
    val rows: List<ExerciseRow> get() = sections.flatMap { it.rows }.distinctBy { it.slug }
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

/** One row of the Equipment sheet, with how many offered exercises use it ("12 exercises"). */
data class EquipmentOption(val equipment: Equipment, val label: String, val count: Int, val countLabel: String)

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
    /** Exercises used last, most recent first, read once when the picker first needs them. */
    recentlyUsed: () -> List<ExerciseSlug> = { emptyList() },
) {
    private val labels = Labels(languageCode)
    private val recent by lazy(recentlyUsed)
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
     * What the Equipment sheet lists: the catalog's equipment, bodyweight and mat left
     * out since those exercises always show, each with how many exercises use it.
     */
    val equipmentOptions: List<EquipmentOption> = Equipment.entries
        .filter { it != Equipment.BODYWEIGHT && it != Equipment.MAT }
        .mapNotNull { item ->
            val count = catalog.offered.count { item in it.allEquipment }
            if (count == 0) null else EquipmentOption(item, labels.equipment(item), count, labels.exercises(count))
        }

    /**
     * Offered exercises matching [query] and [area]. With [type], only exercises that
     * workout type can run are listed; with [ticked], only those that equipment fits
     * (null is no equipment filter). With a query, one section of the best matches
     * first ([ExerciseSearch]); without, a section per movement pattern, by name.
     * [withRecent], for the builder's picker, puts up to 5 recently used exercises
     * first while nothing is typed or filtered; they also stay in their pattern.
     */
    fun filter(
        query: String = "",
        area: MuscleArea? = null,
        type: WorkoutType? = null,
        ticked: Set<Equipment>? = null,
        withRecent: Boolean = false,
    ): ExerciseListing {
        val candidates = catalog.offered
            .filter { area == null || it.muscleGroups.any { group -> group in area.groups } }
            .filter { type == null || registry.accepts(type, it.measurement) }
            .filter { ticked == null || it.fits(ticked) }
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
        val restricted = ticked != null
        val recentRows = if (withRecent && !searching && area == null && !restricted) recentlyUsedRows(type) else emptyList()
        val shown = if (recentRows.isEmpty()) sections else listOf(ExerciseSection(labels.recentlyUsed, recentRows)) + sections
        val countLabel = when {
            searching && restricted -> labels.matchesYouCanDo(count)
            searching -> labels.matches(count)
            restricted -> labels.exercisesYouCanDo(count)
            else -> labels.exercises(count)
        }
        return ExerciseListing(shown, count, countLabel, searching, restricted)
    }

    fun detail(slug: String): ExerciseDetail? = catalog[ExerciseSlug(slug)]?.let { exercise ->
        ExerciseDetail(
            slug = exercise.slug.value,
            name = labels.text(exercise.name),
            pattern = exercise.movementPattern,
            patternLabel = labels.pattern(exercise.movementPattern),
            muscles = exercise.muscleGroups.joinToString(", ") { labels.muscle(it) },
            equipment = when {
                exercise.equipment.isNotEmpty() -> needs(exercise)
                exercise.optionalEquipment.isNotEmpty() -> exercise.optionalEquipment.map(labels::equipment).sentence()
                else -> labels.equipment(Equipment.BODYWEIGHT)
            },
            perSide = exercise.perSide,
            instructions = exercise.instructions.map { labels.text(it) },
            variations = catalog.variationsOf(exercise.slug).map {
                VariationRow(it.slug.value, labels.text(it.name), current = it.slug == exercise.slug)
            },
        )
    }

    private fun recentlyUsedRows(type: WorkoutType?): List<ExerciseRow> = recent.asSequence()
        .mapNotNull { catalog[it] }
        .filter { !it.retired && (type == null || registry.accepts(type, it.measurement)) }
        .take(RECENT_COUNT)
        .map(::row)
        .toList()

    private fun row(exercise: Exercise) =
        ExerciseRow(exercise.slug.value, labels.text(exercise.name), details(exercise), exercise.movementPattern)

    private fun details(exercise: Exercise): String {
        val muscles = exercise.muscleGroups.take(2).map(labels::muscle).sentence()
        return listOf(muscles, fact(exercise)).filter { it.isNotEmpty() }.joinToString(" · ")
    }

    /** What the exercise needs, else how it is done: "Dumbbells or kettlebell", "Hold", "Each side", "Timed". */
    private fun fact(exercise: Exercise): String {
        val main = when {
            exercise.equipment.isNotEmpty() -> needs(exercise)
            exercise.measurement == MeasurementMode.HOLD -> labels.hold
            exercise.perSide -> return labels.perSide.capitalized()
            exercise.measurement == MeasurementMode.TIME -> labels.timed
            Equipment.MAT in exercise.optionalEquipment -> labels.equipment(Equipment.MAT)
            else -> labels.equipment(Equipment.BODYWEIGHT)
        }
        return if (exercise.perSide) "$main · ${labels.perSide}" else main
    }

    /** "Dumbbells or kettlebell", "Dumbbells and bench or step": alternatives with "or", needs with "and". */
    private fun needs(exercise: Exercise): String {
        val words = exercise.equipment.joinToString(" ${labels.andWord} ") { need ->
            need.joinToString(" ${labels.orWord} ") { labels.equipment(it).lowercase() }
        }
        return words.capitalized()
    }

    /** "Chest, triceps": the first word as it is, the others in lower case. */
    private fun List<String>.sentence() = mapIndexed { index, word -> if (index == 0) word else word.lowercase() }.joinToString(", ")

    private fun String.capitalized() = replaceFirstChar { it.uppercaseChar() }

    private companion object {
        const val RECENT_COUNT = 5
    }
}
