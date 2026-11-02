package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.BodyPosition
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseFocus
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.ExerciseTag
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern

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

/** One row of the Equipment sheet, with how many offered exercises use it ("12 exercises"). */
data class EquipmentOption(val equipment: Equipment, val label: String, val count: Int, val countLabel: String)

/** One row of the Focus, Position or Tags sheet: how many offered exercises it lists, as "9 exercises". */
data class FilterOption<T>(val value: T, val label: String, val count: Int, val countLabel: String)

/**
 * One variation in the exercise page's variations sheet; [current] marks the exercise
 * the page is about. [note] is a line on how it differs ("Knees down, a shorter lever").
 */
data class VariationRow(val slug: String, val name: String, val note: String, val current: Boolean)

data class ExerciseDetail(
    val slug: String,
    val name: String,
    val pattern: MovementPattern,
    val patternLabel: String,
    /** Pattern · measurement, and "Each side" when it applies: "Lunge · Reps · Each side". */
    val eyebrow: String,
    val muscles: String,
    /** The body map areas the exercise works, as area keys (`quads-left`); see [BodyMap.worked]. */
    val workedAreas: Set<String>,
    val equipment: String,
    val perSide: Boolean,
    val instructions: List<String>,
    /** Easiest to hardest, this exercise included; empty when it has no variations. */
    val variations: List<VariationRow>,
    /** This exercise's place in [variations], from 1; 0 without variations. */
    val variationIndex: Int,
    /** "3 of 5", or empty without variations. */
    val variationPosition: String,
)

/** The catalog as the exercise screens and the workout builder's picker show it. */
class ExerciseListModel internal constructor(
    private val catalog: Catalog,
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

    /**
     * What the Equipment sheet lists, each with how many exercises use it: "No equipment"
     * ([Equipment.BODYWEIGHT], the exercises needing nothing) first, then the catalog's
     * equipment. The mat is left out, as it is never needed.
     */
    val equipmentOptions: List<EquipmentOption> = Equipment.entries
        .filter { it != Equipment.MAT }
        .mapNotNull { item ->
            val count = catalog.offered.count { if (item == Equipment.BODYWEIGHT) it.equipment.isEmpty() else item in it.allEquipment }
            if (count == 0) null else EquipmentOption(item, labels.equipment(item), count, labels.exercises(count))
        }

    /** What the Focus sheet lists, in order; one with no exercise is left out. */
    val focusOptions: List<FilterOption<ExerciseFocus>> = ExerciseFocus.entries.options(labels::focus) { it.focus }

    /** What the Position sheet lists. */
    val positionOptions: List<FilterOption<BodyPosition>> = BodyPosition.entries.options(labels::bodyPosition) { it.positions }

    /** What the Tags sheet lists. */
    val tagOptions: List<FilterOption<ExerciseTag>> = ExerciseTag.entries.options(labels::tag) { it.tags }

    private fun <T> List<T>.options(label: (T) -> String, of: (Exercise) -> Collection<T>) = mapNotNull { item ->
        val count = catalog.offered.count { item in of(it) }
        if (count == 0) null else FilterOption(item, label(item), count, labels.exercises(count))
    }

    /**
     * Offered exercises matching [query] and every filter given. Inside one filter an exercise
     * needs any ticked item ([focus], [positions], [zones] by their area name without a side,
     * `pectoral`, and the equipment [ticked]); [tags] must all apply. An empty or null filter
     * is off. With a query, one section of the best matches first ([ExerciseSearch]); without,
     * a section per movement pattern, by name.
     * [withRecent], for the builder's picker, puts up to 5 recently used exercises
     * first while nothing is typed or filtered; they also stay in their pattern.
     */
    fun filter(
        query: String = "",
        ticked: Set<Equipment>? = null,
        withRecent: Boolean = false,
        focus: Set<ExerciseFocus> = emptySet(),
        positions: Set<BodyPosition> = emptySet(),
        tags: Set<ExerciseTag> = emptySet(),
        zones: Set<String> = emptySet(),
    ): ExerciseListing {
        val owned = ticked?.takeIf { it.isNotEmpty() }
        val candidates = catalog.offered
            .filter { owned == null || it.uses(owned) }
            .filter { focus.isEmpty() || it.focus.any { item -> item in focus } }
            .filter { positions.isEmpty() || it.positions.any { item -> item in positions } }
            .filter { it.tags.containsAll(tags) }
            .filter { zones.isEmpty() || it.worksAny(zones) }
        val narrowed = focus.isNotEmpty() || positions.isNotEmpty() || tags.isNotEmpty() || zones.isNotEmpty()
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
        val restricted = owned != null
        val recentRows = if (withRecent && !searching && !narrowed && !restricted) recentlyUsedRows() else emptyList()
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
        val variations = catalog.variationsOf(exercise.slug)
        val index = variations.indexOfFirst { it.slug == exercise.slug }
        ExerciseDetail(
            slug = exercise.slug.value,
            name = labels.text(exercise.name),
            pattern = exercise.movementPattern,
            patternLabel = labels.pattern(exercise.movementPattern),
            eyebrow = listOfNotNull(
                labels.pattern(exercise.movementPattern),
                labels.measurement(exercise.measurement),
                labels.perSide.capitalized().takeIf { exercise.perSide },
            ).joinToString(" · "),
            muscles = exercise.muscleGroups.map(labels::muscle).sentence(),
            workedAreas = BodyMap.worked(exercise.muscleGroups).toSet(),
            equipment = when {
                exercise.equipment.isNotEmpty() -> needs(exercise)
                exercise.optionalEquipment.isNotEmpty() -> exercise.optionalEquipment.map(labels::equipment).sentence()
                else -> labels.equipment(Equipment.BODYWEIGHT)
            },
            perSide = exercise.perSide,
            instructions = exercise.instructions.map { labels.text(it) },
            variations = variations.map {
                VariationRow(it.slug.value, labels.text(it.name), it.variationNote?.let(labels::text).orEmpty(), current = it.slug == exercise.slug)
            },
            variationIndex = index + 1,
            variationPosition = if (variations.isEmpty()) "" else labels.position(index + 1, variations.size),
        )
    }

    /**
     * What the Equipment filter lists: exercises using something ticked, needed or
     * optional, whose every need is ticked, and with "No equipment" ticked the exercises
     * needing nothing. Dumbbells alone list the goblet squat and lunges but not the
     * dumbbell bench press; a treadmill lists the walk and the run.
     */
    private fun Exercise.uses(ticked: Set<Equipment>) =
        (Equipment.BODYWEIGHT in ticked && equipment.isEmpty()) || (fits(ticked) && allEquipment.any { it in ticked })

    /** True when the exercise works one of the body map areas named in [zones], on either side. */
    private fun Exercise.worksAny(zones: Set<String>) =
        BodyMap.worked(muscleGroups).any { it.removeSuffix("-left").removeSuffix("-right") in zones }

    private fun recentlyUsedRows(): List<ExerciseRow> = recent.asSequence()
        .mapNotNull { catalog[it] }
        .filter { !it.retired }
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
