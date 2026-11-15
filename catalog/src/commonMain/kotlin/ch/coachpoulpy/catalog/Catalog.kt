package ch.coachpoulpy.catalog

import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The exercise catalog. The app ships [bundled] as the starting copy and keeps each
 * exercise's newest revision in its database; the server publishes newer revisions
 * of single exercises (ADR 0030). The catalog as a whole has no version.
 *
 * [revisions] holds the revision of an exercise the bundled file numbers above 1; any
 * other exercise is [revisionOf] 1.
 *
 * [variationGroups] are the catalog's variations: each group lists slugs of the same
 * movement from easiest to hardest, and an exercise belongs to at most one group.
 */
class Catalog(
    val exercises: List<Exercise>,
    val variationGroups: List<List<ExerciseSlug>> = emptyList(),
    private val revisions: Map<ExerciseSlug, Int> = emptyMap(),
) {
    private val bySlug = exercises.associateBy { it.slug }
    private val groupOf: Map<ExerciseSlug, List<ExerciseSlug>>

    init {
        exercises.forEach { exercise ->
            require(exercise.aliases.all.none { it.isBlank() }) { "Exercise ${exercise.slug.value} has an empty alias" }
            exercise.equipment.forEach { need ->
                require(need.isNotEmpty()) { "Exercise ${exercise.slug.value} has an empty equipment need" }
                require(Equipment.BODYWEIGHT !in need && Equipment.MAT !in need) {
                    "Exercise ${exercise.slug.value} needs bodyweight or a mat, which are never needs"
                }
            }
        }
        val seen = mutableMapOf<ExerciseSlug, List<ExerciseSlug>>()
        variationGroups.forEach { group ->
            group.forEach { slug ->
                require(slug in bySlug) { "Variation group names unknown exercise ${slug.value}" }
                require(seen.put(slug, group) == null) { "Exercise ${slug.value} is in two variation groups" }
            }
        }
        groupOf = seen
    }

    /** What the catalog offers when browsing or building a workout: retired entries are left out. */
    val offered: List<Exercise> get() = exercises.filterNot { it.retired }

    /** Any entry, retired or not, so an existing workout can always show its exercises. */
    operator fun get(slug: ExerciseSlug): Exercise? = bySlug[slug]

    /**
     * The group [slug] belongs to, easiest to hardest, with [slug] itself in place and
     * retired entries left out. Empty when the exercise has no other variation offered.
     */
    fun variationsOf(slug: ExerciseSlug): List<Exercise> {
        val group = groupOf[slug] ?: return emptyList()
        val listed = group.mapNotNull { bySlug[it] }.filterNot { it.retired }
        return if (listed.any { it.slug != slug }) listed else emptyList()
    }

    /** The revision of the exercise [slug] this catalog holds: 1 unless the bundled file numbers it higher. */
    fun revisionOf(slug: ExerciseSlug): Int = revisions[slug] ?: 1

    fun toJson(): String = json.encodeToString(CatalogFile.serializer(), CatalogFile(exercises, variationGroups))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(text: String): Catalog {
            val file = json.decodeFromString(CatalogFile.serializer(), text)
            val revisions = json.decodeFromString(CatalogRevisions.serializer(), text).exercises.associate { it.slug to it.revision }
            return Catalog(file.exercises, file.variations, revisions)
        }

        val bundled: Catalog by lazy { fromJson(BUNDLED_EXERCISES_JSON) }
    }
}

@Serializable
private class CatalogFile(
    val exercises: List<Exercise>,
    val variations: List<List<ExerciseSlug>> = emptyList(),
)

/** Only the revision each exercise of the file carries, which [Exercise] doesn't hold: 1 when absent. */
@Serializable
private class CatalogRevisions(val exercises: List<ExerciseRevision>)

@Serializable
private class ExerciseRevision(val slug: ExerciseSlug, val revision: Int = 1)
