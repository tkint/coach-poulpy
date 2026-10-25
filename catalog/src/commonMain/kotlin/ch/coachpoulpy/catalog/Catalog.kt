package ch.coachpoulpy.catalog

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * One published version of the exercise catalog. The app ships [bundled] as the
 * starting copy and keeps the newest version it has in its database; a later
 * server publishes newer versions in the same JSON format.
 *
 * [variationGroups] are the catalog's variations: each group lists slugs of the same
 * movement from easiest to hardest, and an exercise belongs to at most one group.
 */
class Catalog(
    val version: Int,
    val exercises: List<Exercise>,
    val variationGroups: List<List<ExerciseSlug>> = emptyList(),
) {
    private val bySlug = exercises.associateBy { it.slug }
    private val groupOf: Map<ExerciseSlug, List<ExerciseSlug>>

    init {
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

    fun toJson(): String = json.encodeToString(CatalogFile.serializer(), CatalogFile(version, exercises, variationGroups))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(text: String): Catalog {
            val file = json.decodeFromString(CatalogFile.serializer(), text)
            return Catalog(file.version, file.exercises, file.variations)
        }

        val bundled: Catalog by lazy { fromJson(BUNDLED_EXERCISES_JSON) }
    }
}

@Serializable
private class CatalogFile(
    val version: Int,
    val exercises: List<Exercise>,
    val variations: List<List<ExerciseSlug>> = emptyList(),
)
