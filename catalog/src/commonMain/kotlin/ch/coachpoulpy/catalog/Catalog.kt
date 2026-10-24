package ch.coachpoulpy.catalog

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * One published version of the exercise catalog. The app ships [bundled] as the
 * starting copy and keeps the newest version it has in its database; a later
 * server publishes newer versions in the same JSON format.
 */
class Catalog(val version: Int, val exercises: List<Exercise>) {
    private val bySlug = exercises.associateBy { it.slug }

    /** What the catalog offers when browsing or building a workout: retired entries are left out. */
    val offered: List<Exercise> get() = exercises.filterNot { it.retired }

    /** Any entry, retired or not, so an existing workout can always show its exercises. */
    operator fun get(slug: ExerciseSlug): Exercise? = bySlug[slug]

    fun toJson(): String = json.encodeToString(CatalogFile.serializer(), CatalogFile(version, exercises))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(text: String): Catalog {
            val file = json.decodeFromString(CatalogFile.serializer(), text)
            return Catalog(file.version, file.exercises)
        }

        val bundled: Catalog by lazy { fromJson(BUNDLED_EXERCISES_JSON) }
    }
}

@Serializable
private class CatalogFile(val version: Int, val exercises: List<Exercise>)
