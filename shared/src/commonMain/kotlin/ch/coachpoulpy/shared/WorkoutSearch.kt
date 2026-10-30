package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Workout

/**
 * The forgiving workout search, with the exercise search's rules ([SearchText]): every
 * word of the query must match, in any order and either language. A word's tier is 1
 * when the name starts with it, 2 when a word of the name does, 3 when it is inside the
 * name, 4 when the workout's format starts with it and 5 when an exercise inside does (its
 * name or an alias).
 */
class WorkoutSearch private constructor(private val entries: Map<String, Entry>) {
    /** Rank of each matching workout by id, or null when [query] has no words, so nothing is filtered. */
    internal fun rank(query: String): Map<String, SearchRank>? {
        val words = SearchText.words(query)
        if (words.isEmpty()) return null
        return entries.mapNotNull { (id, entry) -> entry.rank(words)?.let { id to it } }.toMap()
    }

    private class Entry(workout: Workout, catalog: Catalog) {
        private val name = SearchText.fold(workout.name)
        private val nameWords = SearchText.words(workout.name)
        private val formatWords = languages.flatMap { SearchText.words(it.format(workout.format)) }
        private val exerciseWords = exerciseSteps(workout).mapNotNull { catalog[it.exercise] }.distinct()
            .flatMap { it.name.all + it.aliases.all }
            .flatMap(SearchText::words)

        fun rank(words: List<String>): SearchRank? {
            val tiers = words.map { tier(it) ?: return null }
            return SearchRank(tiers.max(), tiers.sum())
        }

        private fun tier(word: String): Int? = when {
            name.startsWith(word) -> 1
            nameWords.any { it.startsWith(word) } -> 2
            word in name -> 3
            formatWords.any { it.startsWith(word) } -> 4
            exerciseWords.any { it.startsWith(word) } -> 5
            else -> null
        }
    }

    companion object {
        private val languages = listOf(Labels("en"), Labels("fr"))

        internal val EMPTY = WorkoutSearch(emptyMap())

        internal fun of(workouts: List<Workout>, catalog: Catalog) = WorkoutSearch(workouts.associate { it.id.value to Entry(it, catalog) })
    }
}
