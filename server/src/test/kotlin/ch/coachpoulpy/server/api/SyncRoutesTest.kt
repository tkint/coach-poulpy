package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.SignInResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.REQUEST_ENTITY_TOO_LARGE
import org.http4k.core.Status.Companion.UNAUTHORIZED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncRoutesTest {
    private val test = TestServer()
    private val ana = signedIn("ana@example.com")

    private fun signedIn(email: String): String {
        test.accounts.signUp(SignUp(email, "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
        return (test.signIns.signIn(email, "correct horse", "Phone") as SignInResult.Signed).tokens.accessToken
    }

    private fun push(vararg changes: String, token: String = ana): Response = test.http(
        Request(POST, "/api/v1/sync/push").header("Authorization", "Bearer $token").header("Content-Type", "application/json")
            .body("""{"changes":[${changes.joinToString(",")}]}"""),
    )

    private fun pull(cursor: String? = null, limit: Int? = null, token: String = ana): Response = test.http(
        Request(GET, "/api/v1/sync/pull").header("Authorization", "Bearer $token")
            .let { if (cursor != null) it.query("cursor", cursor) else it }
            .let { if (limit != null) it.query("limit", limit.toString()) else it },
    )

    private fun Response.page(): JsonObject {
        assertEquals(OK, status, bodyString())
        return Json.parseToJsonElement(bodyString()).jsonObject
    }

    private val JsonObject.changes: JsonArray get() = getValue("changes").jsonArray
    private val JsonObject.cursor: String get() = getValue("cursor").jsonPrimitive.content
    private val JsonObject.more: Boolean get() = getValue("more").jsonPrimitive.boolean
    private fun JsonArray.types() = map { it.jsonObject.getValue("type").jsonPrimitive.content }

    private fun session(id: String = SESSION, title: String = "Leg day", ended: Boolean = true) =
        """{"type":"session","id":"$id","source":"GUIDED","startedAt":"2026-11-07T07:30:00Z",""" +
            (if (ended) """"endedAt":"2026-11-07T08:05:00Z",""" else "") +
            """"workout":{"workoutId":"$WORKOUT","version":2},"title":"$title","format":"SETS","totals":{"sets":12}}"""

    private fun events(vararg seqs: Int, id: String = SESSION) =
        """{"type":"session-events","sessionId":"$id","events":[""" +
            seqs.joinToString(",") { """{"seq":$it,"at":"2026-11-07T07:3$it:00Z","payload":{"type":"set","n":$it}}""" } + "]}"

    private fun deleted(id: String = SESSION) = """{"type":"session-deleted","sessionId":"$id","deletedAt":"2026-11-07T09:00:00Z"}"""

    @Test
    fun aPushedSessionComesBackOnAPullWithItsEvents() {
        assertEquals(OK, push(session(), events(1, 2, 3)).status)
        val page = pull().page()
        assertEquals(listOf("session", "session-events"), page.changes.types())
        val header = page.changes[0].jsonObject
        assertEquals("Leg day", header.getValue("title").jsonPrimitive.content)
        assertEquals("2026-11-07T08:05:00Z", header.getValue("endedAt").jsonPrimitive.content)
        assertEquals("12", header.getValue("totals").jsonObject.getValue("sets").jsonPrimitive.content)
        assertEquals(3, page.changes[1].jsonObject.getValue("events").jsonArray.size)
        assertEquals("4", page.cursor)
        assertFalse(page.more)
    }

    @Test
    fun pushingTheSameChangesAgainChangesNothing() {
        push(session(), events(1, 2))
        val cursor = pull().page().cursor
        assertEquals(OK, push(session(), events(1, 2)).status)
        val after = pull(cursor).page()
        assertEquals(0, after.changes.size)
        assertEquals(cursor, after.cursor)
    }

    @Test
    fun aPullFromTheCursorGivesOnlyWhatChanged() {
        push(session(ended = false), events(1))
        val cursor = pull().page().cursor
        push(session(), events(1, 2))
        val page = pull(cursor).page()
        assertEquals(listOf("session", "session-events"), page.changes.types())
        assertEquals(listOf(2), page.changes[1].jsonObject.getValue("events").jsonArray.map { it.jsonObject.getValue("seq").jsonPrimitive.content.toInt() })
    }

    @Test
    fun eventsNeverArriveBeforeTheirSession() {
        push(session(ended = false), events(1, 2))
        // The header changes after its events: a phone pulling everything still gets it first.
        push(session())
        val page = pull().page()
        assertEquals(listOf("session", "session-events", "session"), page.changes.types())
    }

    @Test
    fun pagesFollowEachOther() {
        push(session(), events(1, 2, 3, 4, 5))
        val first = pull(limit = 2).page()
        assertTrue(first.more)
        assertEquals("2", first.cursor)
        val second = pull(first.cursor, limit = 2).page()
        assertTrue(second.more)
        val third = pull(second.cursor, limit = 2).page()
        assertFalse(third.more)
        assertEquals("6", third.cursor)
    }

    @Test
    fun aDeletedSessionIsGoneEverywhereAndCantComeBack() {
        push(session(), events(1, 2))
        val cursor = pull().page().cursor
        assertEquals(OK, push(deleted()).status)
        assertEquals(listOf("session-deleted"), pull(cursor).page().changes.types())
        // An older push sent again doesn't bring it back.
        push(session(), events(3))
        assertEquals(listOf("session-deleted"), pull().page().changes.types())
    }

    @Test
    fun eventsBeforeTheirSessionAreRefusedAndNothingIsStored() {
        val response = push(session(id = OTHER), events(1))
        assertEquals(BAD_REQUEST, response.status)
        val problem = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("invalid-change", problem.getValue("error").jsonPrimitive.content)
        assertEquals(1, problem.getValue("index").jsonPrimitive.content.toInt())
        assertEquals(0, pull().page().changes.size)
    }

    @Test
    fun aChangeThatDoesntParseIsRefusedWithItsIndex() {
        val response = push(session(), """{"type":"session","id":"not-a-uuid"}""")
        assertEquals(BAD_REQUEST, response.status)
        assertEquals("1", Json.parseToJsonElement(response.bodyString()).jsonObject.getValue("index").jsonPrimitive.content)
        assertEquals(BAD_REQUEST, push(session(id = "not-a-uuid")).status)
        assertEquals(BAD_REQUEST, push("""{"type":"workout-teleported"}""").status)
    }

    @Test
    fun tooManyChangesAreRefused() {
        assertEquals(REQUEST_ENTITY_TOO_LARGE, push(*Array(PAGE_MAX + 1) { deleted() }).status)
    }

    @Test
    fun aBadCursorOrLimitIsRefused() {
        assertEquals(BAD_REQUEST, pull("yesterday").status)
        assertEquals(BAD_REQUEST, pull(limit = 0).status)
        assertEquals(BAD_REQUEST, pull(limit = PAGE_MAX + 1).status)
        assertEquals("invalid-limit", Json.parseToJsonElement(test.http(Request(GET, "/api/v1/sync/pull?limit=many").header("Authorization", "Bearer $ana")).bodyString()).jsonObject.getValue("error").jsonPrimitive.content)
    }

    @Test
    fun anAccountNeverSeesAnothersChanges() {
        val ben = signedIn("ben@example.com")
        push(session(), events(1))
        assertEquals(0, pull(token = ben).page().changes.size)
        // The same id pushed by another account is its own session.
        push(session(title = "Ben's legs"), token = ben)
        assertEquals("Leg day", pull().page().changes[0].jsonObject.getValue("title").jsonPrimitive.content)
    }

    @Test
    fun syncNeedsASignIn() {
        assertEquals(UNAUTHORIZED, test.http(Request(GET, "/api/v1/sync/pull")).status)
        assertEquals(UNAUTHORIZED, test.http(Request(POST, "/api/v1/sync/push").body("""{"changes":[]}""")).status)
    }

    @Test
    fun deletingTheAccountDeletesItsSessions() {
        push(session(), events(1))
        val id = test.database.queries.accountQueries.byEmail("ana@example.com").executeAsOne().id
        test.database.queries.accountQueries.delete(id).executeAsOneOrNull()
        assertEquals(0L, test.database.queries.syncQueries.eventsAfter(id, 0, 10).executeAsList().size.toLong())
    }

    companion object {
        const val SESSION = "0190d4d2-0000-7000-8000-000000000002"
        const val OTHER = "0190d4d2-0000-7000-8000-000000000003"
        const val WORKOUT = "0190d4d2-0000-7000-8000-000000000009"
    }
}
