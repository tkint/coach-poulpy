package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionEventsChange
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.SyncedEvent
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.Scope
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import ch.coachpoulpy.server.sync.InvalidChange
import ch.coachpoulpy.server.sync.Sync
import java.util.UUID
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.http4k.contract.ContractRoute
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.REQUEST_ENTITY_TOO_LARGE
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Query
import org.http4k.lens.int

/** A refused change: its place in the push, and why, for the developer reading the logs. */
@Serializable
data class ChangeProblem(val error: String, val index: Int? = null, val description: String? = null)

private val pushBody = Body.auto<SyncPush>().toLens()
private val pageBody = Body.auto<SyncPage>().toLens()
private val changeProblemBody = Body.auto<ChangeProblem>().toLens()
private val cursorQuery = Query.optional("cursor", "The cursor of the last page pulled; none to pull everything")
private val limitQuery = Query.int().optional("limit", "At most this many changes, 1 to $PAGE_MAX; $PAGE_MAX by default")

private val syncTag = Tag("sync")
private val json = Json

/** The most changes in one push or one page of a pull (docs/accounts.md, Sync). */
const val PAGE_MAX = 500

/** The largest push body, in bytes. */
const val PUSH_MAX_BYTES = 1_000_000

private val exampleSession = SessionChange(
    id = SessionId("0190d4d2-0000-7000-8000-000000000002"),
    source = SessionSource.GUIDED,
    startedAt = Instant.parse("2026-11-07T07:30:00Z"),
    endedAt = Instant.parse("2026-11-07T08:05:00Z"),
    title = "Leg day",
)

private val exampleEvents = SessionEventsChange(
    exampleSession.id,
    listOf(SyncedEvent(1, Instant.parse("2026-11-07T07:30:00Z"), JsonObject(mapOf("type" to JsonPrimitive("started"))))),
)

private val Request.account: UUID get() = UUID.fromString((caller as Caller.Signed).accountId.value)

/** Takes a phone's outbox: stored whole, or refused whole. */
fun pushRoute(sync: Sync): ContractRoute = "$API_PREFIX/sync/push" meta {
    summary = "Push the phone's changes"
    description = "Takes the outbox, oldest first: at most $PAGE_MAX changes and ${PUSH_MAX_BYTES / 1_000_000} MB. " +
        "All of it is stored or none of it. Sending the same changes again changes nothing, so a push whose answer was lost " +
        "is simply sent again. A change that doesn't parse or breaks a rule gets 400 invalid-change with its index."
    operationId = "syncPush"
    // The handler reads the changes one by one, so a bad one gets invalid-change with its index.
    preFlightExtraction = PreFlightExtraction.IgnoreBody
    tags += syncTag
    security = requires(Scope.Sync)
    receiving(pushBody to SyncPush(listOf(exampleSession, exampleEvents)), definitionId = "SyncPush")
    returning(OK to "Stored")
    returning(BAD_REQUEST, changeProblemBody to ChangeProblem("invalid-change", 0, "…"), "invalid-change", definitionId = "ChangeProblem")
    returning(REQUEST_ENTITY_TOO_LARGE, changeProblemBody to ChangeProblem("too-large"), "More than $PAGE_MAX changes or ${PUSH_MAX_BYTES / 1_000_000} MB")
} bindContract POST to { request ->
    val bytes = request.body.payload.remaining()
    if (bytes > PUSH_MAX_BYTES) {
        Response(REQUEST_ENTITY_TOO_LARGE).with(changeProblemBody of ChangeProblem("too-large"))
    } else {
        when (val parsed = parseChanges(request.bodyString())) {
            is Parsed.Refused -> Response(BAD_REQUEST).with(changeProblemBody of parsed.problem)
            is Parsed.Changes ->
                if (parsed.changes.size > PAGE_MAX) {
                    Response(REQUEST_ENTITY_TOO_LARGE).with(changeProblemBody of ChangeProblem("too-large"))
                } else {
                    try {
                        sync.push(request.account, parsed.changes)
                        Response(OK)
                    } catch (e: InvalidChange) {
                        Response(BAD_REQUEST).with(changeProblemBody of ChangeProblem("invalid-change", e.index, e.message))
                    }
                }
        }
    }
}

/** Gives the account's changes after a cursor, one page at a time. */
fun pullRoute(sync: Sync): ContractRoute = "$API_PREFIX/sync/pull" meta {
    summary = "Pull the account's changes"
    description = "The changes stored after the cursor, oldest first, the phone's own included. Keep calling with the " +
        "cursor answered while more is true. With no cursor, everything. The cursor counts changes; it is not a time."
    operationId = "syncPull"
    // The handler checks the cursor and the limit, to answer with its own codes.
    preFlightExtraction = PreFlightExtraction.None
    tags += syncTag
    security = requires(Scope.Sync)
    queries += cursorQuery
    queries += limitQuery
    returning(OK, pageBody to SyncPage(listOf(exampleSession, exampleEvents), "57", more = false), "One page", definitionId = "SyncPage")
    returning(BAD_REQUEST, changeProblemBody to ChangeProblem("invalid-cursor"), "invalid-cursor or invalid-limit", definitionId = "ChangeProblem")
} bindContract GET to { request ->
    val cursor = request.query("cursor")
    val after = if (cursor.isNullOrEmpty()) 0L else cursor.toLongOrNull()?.takeIf { it >= 0 }
    val limit = request.query("limit")?.let { it.toIntOrNull()?.takeIf { n -> n in 1..PAGE_MAX } ?: -1 } ?: PAGE_MAX
    when {
        after == null -> Response(BAD_REQUEST).with(changeProblemBody of ChangeProblem("invalid-cursor"))
        limit < 0 -> Response(BAD_REQUEST).with(changeProblemBody of ChangeProblem("invalid-limit"))
        else -> Response(OK).with(pageBody of sync.pull(request.account, after, limit))
    }
}

private sealed interface Parsed {
    data class Changes(val changes: List<SyncChange>) : Parsed
    data class Refused(val problem: ChangeProblem) : Parsed
}

// Each change is read on its own, so the answer can say which one is wrong.
private fun parseChanges(body: String): Parsed {
    val array: JsonArray = runCatching { json.parseToJsonElement(body).jsonObject.getValue("changes").jsonArray }.getOrNull()
        ?: return Parsed.Refused(ChangeProblem("invalid-change", description = "The body is {\"changes\": [...]}"))
    return Parsed.Changes(
        array.mapIndexed { index, element ->
            runCatching { json.decodeFromJsonElement(SyncChange.serializer(), element) }.getOrElse {
                return Parsed.Refused(ChangeProblem("invalid-change", index, it.message?.lineSequence()?.firstOrNull()))
            }
        },
    )
}
