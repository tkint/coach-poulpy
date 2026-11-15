package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.server.catalog.ServerCatalog
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Header
import org.http4k.lens.Query

private val after = Query.optional("after", "The cursor of the last answer the phone received; without it, every exercise")

/**
 * One published revision of an exercise: the shared Exercise JSON with its [revision], and its place in a
 * variation group (the group's number and the exercise's rank, easiest first), both null when it has none.
 */
@Serializable
data class CatalogExerciseResponse(val revision: Int, val exercise: Exercise, val variationGroup: Int?, val variationRank: Int?)

/** The exercises published since a cursor, and the [cursor] to ask with next time. */
@Serializable
data class CatalogUpdateResponse(val cursor: String, val exercises: List<CatalogExerciseResponse>)

/**
 * The exercises published since the phone's last update (#437, ADR 0030). A guest's phone asks on launch (#27),
 * the only call a guest makes (ADR 0001, ADR 0008). The cursor is opaque: the phone keeps the last one it
 * received and sends it back.
 */
fun catalogRoute(catalog: ServerCatalog): ContractRoute = "$API_PREFIX/catalog/exercises" meta {
    summary = "The exercises published since a cursor"
    description = "Each exercise whose latest published revision came after the cursor given in after, with its revision, and the " +
        "cursor to send next time. Without after, every exercise: the first download and the full refresh. Answers 204 when " +
        "nothing is newer, 400 when the cursor isn't one this server gave."
    operationId = "getCatalogExercises"
    tags += Tag("catalog")
    security = Public
    queries += after
    produces += APPLICATION_JSON
    returning(OK to "The cursor and the exercises: {cursor, exercises: [{revision, exercise, variationGroup, variationRank}]}")
    returning(NO_CONTENT to "Nothing was published after the cursor")
    returning(BAD_REQUEST to "The cursor isn't one this server gave")
} bindContract GET to { request ->
    val given = after(request)
    val cursor = given?.let { it.toLongOrNull()?.takeIf { value -> value >= 0 } ?: return@to Response(BAD_REQUEST) }
    when (val update = catalog.since(cursor)) {
        null -> Response(NO_CONTENT)
        else -> Response(OK).with(Header.CONTENT_TYPE of APPLICATION_JSON).body(
            ServerCatalog.json.encodeToString(
                CatalogUpdateResponse.serializer(),
                CatalogUpdateResponse(update.cursor, update.exercises.map { CatalogExerciseResponse(it.revision, it.exercise, it.group, it.rank) }),
            ),
        )
    }
}
