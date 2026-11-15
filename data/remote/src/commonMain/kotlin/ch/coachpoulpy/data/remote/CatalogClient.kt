package ch.coachpoulpy.data.remote

import ch.coachpoulpy.core.model.Exercise
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One published revision of an exercise, with its place in a variation group (the group's number and its rank, easiest first), null when it has none. */
@Serializable
data class PublishedExercise(val revision: Int, val exercise: Exercise, val variationGroup: Int? = null, val variationRank: Int? = null)

/** What the server answers to a catalog update: the exercises published since the cursor, and the [cursor] to ask with next time. */
@Serializable
data class CatalogAnswer(val cursor: String, val exercises: List<PublishedExercise>)

/**
 * The phone's catalog update (ADR 0030, docs/the-api.md): `GET /api/v1/catalog/exercises`, which needs no
 * account, so a guest's phone asks too. [baseUrl] is the API's origin, such as `https://api.coach-poulpy.com`.
 */
class CatalogClient(baseUrl: String, engine: HttpClientEngine? = null) {
    private val base = baseUrl.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true }
    private val http = if (engine != null) HttpClient(engine) { setUp() } else HttpClient { setUp() }

    private fun io.ktor.client.HttpClientConfig<*>.setUp() {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
    }

    /**
     * The exercises published after [cursor] (every exercise without it), or null when nothing is newer.
     * Throws when the server can't be reached or answers anything else.
     */
    suspend fun since(cursor: String?): CatalogAnswer? {
        val response = http.get("$base/api/v1/catalog/exercises") {
            if (cursor != null) parameter("after", cursor)
        }
        return when (response.status) {
            HttpStatusCode.NoContent -> null
            HttpStatusCode.OK -> response.body<CatalogAnswer>()
            else -> throw UnexpectedAnswer(response.status.value)
        }
    }
}
