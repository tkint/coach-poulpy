package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.server.admin.AdminCatalog
import ch.coachpoulpy.server.admin.ExerciseQuery
import ch.coachpoulpy.server.admin.ExerciseSort
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requiresAdmin
import ch.coachpoulpy.server.catalog.DraftExercise
import ch.coachpoulpy.server.catalog.DraftResult
import ch.coachpoulpy.server.catalog.EditedExercise
import ch.coachpoulpy.server.catalog.MissingText
import ch.coachpoulpy.server.catalog.PublishResult
import ch.coachpoulpy.server.catalog.ServerCatalog
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Header
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int

/** One exercise of the draft: the shared Exercise JSON, whether a version holds it, and what changed since (added, edited, retired, offered or null). */
@Serializable
data class DraftExerciseResponse(val exercise: Exercise, val published: Boolean, val change: String?)

@Serializable
data class DraftExercisesResponse(val exercises: List<DraftExerciseResponse>, val total: Int)

@Serializable
data class ChangedExerciseResponse(val slug: String, val name: Map<String, String>)

/** One changed field of an edited exercise: its path in the exercise JSON, such as `name.fr`, and its value in the latest version and in the draft (null when absent). */
@Serializable
data class FieldChangeResponse(val field: String, val before: JsonElement?, val after: JsonElement?)

@Serializable
data class EditedExerciseResponse(val slug: String, val name: Map<String, String>, val fields: List<FieldChangeResponse>)

@Serializable
data class MissingTextResponse(val slug: String, val name: Map<String, String>, val languages: List<String>)

@Serializable
data class CatalogChangesResponse(
    /** The latest published version. */
    val version: Int,
    /** The number publishing now would give. */
    val nextVersion: Int,
    val added: List<ChangedExerciseResponse>,
    /** Each with the fields that changed, before (the latest version) and after (the draft). */
    val edited: List<EditedExerciseResponse>,
    val retired: List<ChangedExerciseResponse>,
    /** Retired in the latest version, offered again in the draft. */
    val offered: List<ChangedExerciseResponse>,
    /** The exercises whose text lacks a shipped language; publishing is refused while there is one. */
    val missing: List<MissingTextResponse>,
)

@Serializable
data class PublishedResponse(val version: Int)

@Serializable
data class PublishRefusedResponse(val error: String, val missing: List<MissingTextResponse>)

@Serializable
data class InvalidExerciseResponse(val error: String, val reason: String)

private val catalogAdminTag = Tag("admin")
private val slugPath = Path.map(::ExerciseSlug, ExerciseSlug::value).of("slug", "The exercise's slug")
private val textQuery = Query.optional("q", "Part of the slug, of a name in any language or of an alias, whatever its case")
private val retiredQuery = Query.optional("retired", "yes or no: only the retired exercises, or only the offered ones")
private val sortQuery = Query.optional("sort", "catalog, name, pattern, languages, change or status (default catalog, the catalog's order)")
private val orderQuery = Query.optional("order", "asc or desc (default asc)")
private val pageQuery = Query.int().defaulted("page", 1, "The page, from 1")
private val sizeQuery = Query.int().defaulted("size", ExerciseQuery.DEFAULT_PAGE_SIZE, "Exercises per page, up to ${ExerciseQuery.MAX_PAGE_SIZE}")

private val sorts = mapOf(
    "catalog" to ExerciseSort.Catalog,
    "name" to ExerciseSort.Name,
    "pattern" to ExerciseSort.Pattern,
    "languages" to ExerciseSort.Languages,
    "change" to ExerciseSort.Change,
    "status" to ExerciseSort.Status,
)

/** The table's question from the query string, or null when a value isn't one of the allowed words. */
private fun Request.exerciseQuery(): ExerciseQuery? {
    val retired = retiredQuery(this)
    val sort = sortQuery(this)
    val order = orderQuery(this)
    if (retired !in listOf(null, "yes", "no") || sort != null && sort !in sorts || order !in listOf(null, "asc", "desc")) return null
    return ExerciseQuery(
        text = textQuery(this),
        retired = retired?.let { it == "yes" },
        sort = sort?.let(sorts::getValue) ?: ExerciseSort.Catalog,
        ascending = order != "desc",
        page = pageQuery(this),
        size = sizeQuery(this),
    )
}

private const val EXERCISES = "$API_PREFIX/admin/catalog/exercises"

/** The admin's catalog routes (#337, ADR 0026): the draft's exercises, what changed since the last version, and publishing. */
fun adminCatalogRoutes(catalog: AdminCatalog): List<ContractRoute> = listOf(
    listRoute(catalog),
    getRoute(catalog),
    createRoute(catalog),
    editRoute(catalog),
    retireRoute(catalog),
    offerRoute(catalog),
    changesRoute(catalog),
    publishRoute(catalog),
)

private fun <T> json(status: Status, serializer: KSerializer<T>, value: T): Response =
    Response(status).with(Header.CONTENT_TYPE of APPLICATION_JSON).body(ServerCatalog.json.encodeToString(serializer, value))

private fun problem(status: Status, error: String) = json(status, Problem.serializer(), Problem(error))

private fun Request.admin(): Caller.Signed = caller as Caller.Signed

private fun DraftExercise.toResponse() = DraftExerciseResponse(exercise, published, change?.code)

private fun Exercise.changed() = ChangedExerciseResponse(slug.value, name.byLanguage)

private fun EditedExercise.toResponse() =
    EditedExerciseResponse(exercise.slug.value, exercise.name.byLanguage, fields.map { FieldChangeResponse(it.field, it.before, it.after) })

/**
 * The exercise in the body, or why it isn't one: bad JSON, a missing or unknown field, or a text
 * without English (a [kotlinx.serialization.SerializationException] is an [IllegalArgumentException]).
 */
private fun Request.exercise(): Result<Exercise> = try {
    Result.success(ServerCatalog.decode(bodyString()))
} catch (e: IllegalArgumentException) {
    Result.failure(e)
}

private fun invalid(reason: String) = json(BAD_REQUEST, InvalidExerciseResponse.serializer(), InvalidExerciseResponse("invalid-exercise", reason))

/** The answer for a write to the draft. */
private fun DraftResult.toResponse(saved: Status): Response = when (this) {
    is DraftResult.Saved -> json(saved, DraftExerciseResponse.serializer(), exercise.toResponse())
    DraftResult.Unknown -> problem(NOT_FOUND, "unknown-exercise")
    DraftResult.SlugTaken -> problem(CONFLICT, "slug-taken")
    DraftResult.SlugPublished -> problem(CONFLICT, "slug-published")
    is DraftResult.Invalid -> invalid(reason)
}

private fun listRoute(catalog: AdminCatalog): ContractRoute = EXERCISES meta {
    summary = "List the draft's exercises"
    description = "One page of the catalog draft's exercises, filtered and sorted (the catalog's order by default), with how many match " +
        "in all. Each says whether a published version holds it and what changed since the latest one: added, edited, retired, " +
        "offered or null. A value that isn't an allowed word is a 400."
    operationId = "listAdminCatalogExercises"
    tags += catalogAdminTag
    queries += textQuery
    queries += retiredQuery
    queries += sortQuery
    queries += orderQuery
    queries += pageQuery
    queries += sizeQuery
    security = requiresAdmin()
    produces += APPLICATION_JSON
    returning(OK to "A page of exercises, as {exercises: [{exercise, published, change}], total}")
    returning(BAD_REQUEST to "A filter or sort value isn't allowed")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { request ->
    request.exerciseQuery()?.let { query ->
        val page = catalog.list(query)
        json(OK, DraftExercisesResponse.serializer(), DraftExercisesResponse(page.exercises.map { it.toResponse() }, page.total))
    } ?: problem(BAD_REQUEST, "bad-query")
}

private fun getRoute(catalog: AdminCatalog): ContractRoute = EXERCISES / slugPath meta {
    summary = "One exercise of the draft"
    description = "The exercise as the list shows it. 404 when no exercise has this slug."
    operationId = "getAdminCatalogExercise"
    tags += catalogAdminTag
    security = requiresAdmin()
    produces += APPLICATION_JSON
    returning(OK to "The exercise, as {exercise, published, change}")
    returning(NOT_FOUND to "No exercise with this slug")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { slug ->
    { _: Request ->
        catalog.get(slug)?.let { json(OK, DraftExerciseResponse.serializer(), it.toResponse()) } ?: problem(NOT_FOUND, "unknown-exercise")
    }
}

private fun createRoute(catalog: AdminCatalog): ContractRoute = EXERCISES meta {
    summary = "Add an exercise to the draft"
    description = "The body is an exercise in the catalog's JSON, texts as maps from language to string; it is added offered, " +
        "after the others. The English name is required; the other shipped languages can wait until publishing. " +
        "400 invalid-exercise, 409 slug-taken. Written to the audit log."
    operationId = "createAdminCatalogExercise"
    tags += catalogAdminTag
    security = requiresAdmin()
    consumes += APPLICATION_JSON
    produces += APPLICATION_JSON
    returning(CREATED to "The exercise, as {exercise, published, change}")
    returning(BAD_REQUEST to "Not a valid exercise")
    returning(CONFLICT to "Another exercise has this slug")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { request ->
    request.exercise().fold(
        { catalog.create(request.admin().accountId, it).toResponse(CREATED) },
        { invalid(it.message.orEmpty()) },
    )
}

private fun editRoute(catalog: AdminCatalog): ContractRoute = EXERCISES / slugPath meta {
    summary = "Edit an exercise of the draft"
    description = "Replaces the exercise with the body, an exercise in the catalog's JSON; its retired state is kept (retire changes it). " +
        "A different slug renames it, but only until a version publishes it: then 409 slug-published, since slugs never change " +
        "(ADR 0008). 400 invalid-exercise, 404 unknown-exercise, 409 slug-taken. Written to the audit log."
    operationId = "editAdminCatalogExercise"
    tags += catalogAdminTag
    security = requiresAdmin()
    consumes += APPLICATION_JSON
    produces += APPLICATION_JSON
    returning(OK to "The exercise, as {exercise, published, change}")
    returning(BAD_REQUEST to "Not a valid exercise")
    returning(NOT_FOUND to "No exercise with this slug")
    returning(CONFLICT to "The slug is published, or another exercise has the new one")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract PUT to { slug ->
    { request: Request ->
        request.exercise().fold(
            { catalog.edit(request.admin().accountId, slug, it).toResponse(OK) },
            { invalid(it.message.orEmpty()) },
        )
    }
}

private fun retireRoute(catalog: AdminCatalog): ContractRoute = EXERCISES / slugPath / "retire" meta {
    summary = "Retire an exercise in the draft"
    description = "The exercise stays in the catalog, hidden from browsing, so workouts that use it still open; it is never deleted " +
        "(ADR 0008). Written to the audit log."
    operationId = "retireAdminCatalogExercise"
    tags += catalogAdminTag
    security = requiresAdmin()
    produces += APPLICATION_JSON
    returning(OK to "The exercise, as {exercise, published, change}")
    returning(NOT_FOUND to "No exercise with this slug")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { slug, _ ->
    { request: Request -> catalog.retire(request.admin().accountId, slug).toResponse(OK) }
}

private fun offerRoute(catalog: AdminCatalog): ContractRoute = EXERCISES / slugPath / "offer" meta {
    summary = "Offer a retired exercise again"
    description = "Undoes a retire in the draft: the next version lists the exercise for browsing and building again. " +
        "Does nothing to an exercise that is offered. Written to the audit log."
    operationId = "offerAdminCatalogExercise"
    tags += catalogAdminTag
    security = requiresAdmin()
    produces += APPLICATION_JSON
    returning(OK to "The exercise, as {exercise, published, change}")
    returning(NOT_FOUND to "No exercise with this slug")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { slug, _ ->
    { request: Request -> catalog.offer(request.admin().accountId, slug).toResponse(OK) }
}

private fun MissingText.toResponse() = MissingTextResponse(exercise.slug.value, exercise.name.byLanguage, languages)

private fun changesRoute(catalog: AdminCatalog): ContractRoute = "$API_PREFIX/admin/catalog/changes" meta {
    summary = "What publishing would change"
    description = "The draft against the latest published version: the exercises added, edited (each with its changed fields, " +
        "before and after), retired and offered again since, the number the next " +
        "version would take, and the exercises whose text still lacks a shipped language (${ServerCatalog.LANGUAGES.joinToString()})."
    operationId = "getAdminCatalogChanges"
    tags += catalogAdminTag
    security = requiresAdmin()
    produces += APPLICATION_JSON
    returning(OK to "The changes, as {version, nextVersion, added, edited, retired, offered, missing}")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { _ ->
    val changes = catalog.changes()
    json(
        OK,
        CatalogChangesResponse.serializer(),
        CatalogChangesResponse(
            changes.version,
            changes.nextVersion,
            changes.added.map { it.changed() },
            changes.edited.map { it.toResponse() },
            changes.retired.map { it.changed() },
            changes.offered.map { it.changed() },
            changes.missing.map { it.toResponse() },
        ),
    )
}

private fun publishRoute(catalog: AdminCatalog): ContractRoute = "$API_PREFIX/admin/catalog/publish" meta {
    summary = "Publish the draft"
    description = "Makes the draft the next catalog version, in the format the app bundles; GET /api/v1/catalog serves it from then on. " +
        "409 missing-translations, with the exercises and languages, while a text lacks a shipped language " +
        "(${ServerCatalog.LANGUAGES.joinToString()}); 409 no-changes when the draft is the latest version. Written to the audit log."
    operationId = "publishAdminCatalog"
    tags += catalogAdminTag
    security = requiresAdmin()
    produces += APPLICATION_JSON
    returning(OK to "Published, as {version}")
    returning(CONFLICT to "A text lacks a language, or nothing changed")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { request ->
    when (val result = catalog.publish(request.admin().accountId)) {
        is PublishResult.Published -> json(OK, PublishedResponse.serializer(), PublishedResponse(result.version))
        PublishResult.NothingChanged -> json(CONFLICT, PublishRefusedResponse.serializer(), PublishRefusedResponse("no-changes", emptyList()))
        is PublishResult.MissingTranslations -> json(
            CONFLICT,
            PublishRefusedResponse.serializer(),
            PublishRefusedResponse("missing-translations", result.missing.map { it.toResponse() }),
        )
    }
}
