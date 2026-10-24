package ch.coachpoulpy.catalog

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The exercise catalog shipped with the app. Its version lets the server send updates later. */
class Catalog(val version: Int, val exercises: List<Exercise>) {
    private val bySlug = exercises.associateBy { it.slug }

    operator fun get(slug: ExerciseSlug): Exercise? = bySlug[slug]

    companion object {
        val bundled: Catalog by lazy {
            val file = Json.decodeFromString<CatalogFile>(BUNDLED_EXERCISES_JSON)
            Catalog(file.version, file.exercises)
        }
    }
}

@Serializable
private class CatalogFile(val version: Int, val exercises: List<Exercise>)
