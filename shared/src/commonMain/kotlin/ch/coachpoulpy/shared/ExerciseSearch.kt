package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug

/**
 * How well an exercise matches a search, best first: [tier] is the worst-matching
 * word's tier (1 a name starts with it, 2 a word of a name or alias does, 3 it is
 * inside a name, 4 a muscle, equipment, pattern, focus or position label starts with it, 5 a word of a
 * name or alias is one wrong, missing, extra or swapped letter away), and [total] sums every word's tier
 * so a closer match wins inside a tier.
 */
internal data class SearchRank(val tier: Int, val total: Int) : Comparable<SearchRank> {
    override fun compareTo(other: SearchRank) = compareValuesBy(this, other, { it.tier }, { it.total })
}

/**
 * The forgiving exercise search: every word of the query must match (in any order,
 * either language), with case, accents, hyphens, apostrophes and plurals ignored. A word of 5 letters or
 * more also forgives one typo against names and aliases, ranked after every exact tier.
 */
internal class ExerciseSearch(exercises: List<Exercise>) {
    private val labels = listOf(Labels("en"), Labels("fr"))
    private val entries = exercises.associate { it.slug to Entry(it) }

    /** Rank of each matching exercise, or null when [query] has no words, so nothing is filtered. */
    fun rank(query: String): Map<ExerciseSlug, SearchRank>? {
        val words = SearchText.words(query)
        if (words.isEmpty()) return null
        val compact = SearchText.stem(SearchText.fold(query).replace(" ", ""))
        return entries.mapNotNull { (slug, entry) -> entry.rank(words, compact)?.let { slug to it } }.toMap()
    }

    private companion object {
        const val TYPO_TIER = 5
    }

    private inner class Entry(exercise: Exercise) {
        private val names = exercise.name.all.toList().map(SearchText::fold)
        private val compactNames = names.map { it.replace(" ", "") }
        private val compactTypoNames = (exercise.name.all.toList() + exercise.aliases.all).map { SearchText.fold(it).replace(" ", "") }
        private val nameWords = (exercise.name.all.toList() + exercise.aliases.all).flatMap(SearchText::words)
        private val labelWords = labels.flatMap { language ->
            exercise.muscleGroups.map(language::muscle) + exercise.allEquipment.map(language::equipment) +
                language.pattern(exercise.movementPattern) + exercise.focus.map(language::focus) +
                exercise.positions.map(language::bodyPosition)
        }.flatMap(SearchText::words)

        fun rank(words: List<String>, compact: String): SearchRank? {
            val tiers = words.map(::tier)
            if (tiers.all { it != null }) return SearchRank(tiers.maxOf { it!! }, tiers.sumOf { it!! })
            // "pushup" for "push-up": the query without spaces inside a name without spaces.
            if (compact.length >= 4 && compactNames.any { compact in it }) return SearchRank(3, 3)
            // "pusj up" for "push-up": the same, one typo away.
            if (compact.length >= SearchText.TYPO_MIN && compactTypoNames.any { SearchText.typoInside(compact, it) }) return SearchRank(TYPO_TIER, TYPO_TIER)
            return null
        }

        private fun tier(word: String): Int? = when {
            names.any { it.startsWith(word) } -> 1
            nameWords.any { it.startsWith(word) } -> 2
            names.any { word in it } -> 3
            labelWords.any { it.startsWith(word) } -> 4
            word.length >= SearchText.TYPO_MIN && nameWords.any { SearchText.oneTypoAway(word, it) } -> TYPO_TIER
            else -> null
        }
    }
}

/** Text as the search compares it. */
internal object SearchText {
    private val spaces = Regex("\\s+")

    /** Lower case, accents dropped, hyphens and apostrophes as spaces, spaces collapsed. */
    fun fold(text: String): String {
        val out = StringBuilder(text.length)
        for (c in text.lowercase()) {
            when (c) {
                in "àáâãäå" -> out.append('a')
                'ç' -> out.append('c')
                in "èéêë" -> out.append('e')
                in "ìíîï" -> out.append('i')
                'ñ' -> out.append('n')
                in "òóôõö" -> out.append('o')
                in "ùúûü" -> out.append('u')
                in "ýÿ" -> out.append('y')
                'œ' -> out.append("oe")
                'æ' -> out.append("ae")
                in "-‐‑–—'’‘" -> out.append(' ')
                else -> out.append(c)
            }
        }
        return out.toString().replace(spaces, " ").trim()
    }

    /** Drops a plural's trailing s or x on a word of 4 letters or more. */
    fun stem(word: String): String = if (word.length >= 4 && (word.last() == 's' || word.last() == 'x')) word.dropLast(1) else word

    /** The shortest word that may carry a typo; shorter ones are too likely to hit another word. */
    const val TYPO_MIN = 5

    /** True when [a] and [b] differ by exactly one wrong, missing, extra or swapped (adjacent) letter. */
    fun oneTypoAway(a: String, b: String): Boolean {
        if (a == b || kotlin.math.abs(a.length - b.length) > 1) return false
        val n = minOf(a.length, b.length)
        var i = 0
        while (i < n && a[i] == b[i]) i++
        return when {
            a.length == b.length -> a.substring(i + 1) == b.substring(i + 1) ||
                (i + 1 < a.length && a[i] == b[i + 1] && a[i + 1] == b[i] && a.substring(i + 2) == b.substring(i + 2))
            a.length > b.length -> a.substring(i + 1) == b.substring(i)
            else -> a.substring(i) == b.substring(i + 1)
        }
    }

    /** True when some stretch of [text] is one typo away from [word]. */
    fun typoInside(word: String, text: String): Boolean =
        listOf(word.length - 1, word.length, word.length + 1).any { size ->
            size > 0 && (0..text.length - size).any { oneTypoAway(word, text.substring(it, it + size)) }
        }

    fun words(text: String): List<String> = fold(text).split(' ').filter { it.isNotEmpty() }.map(::stem)
}
