package ch.coachpoulpy.data.remote

import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.WorkoutId
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest

class AccountClientTest {
    private var now = Instant.fromEpochSeconds(1_700_000_000)
    private val clock = object : Clock { override fun now() = now }
    private val store = MemoryTokens()
    private val requests = mutableListOf<String>()
    private var answer: suspend MockRequestHandleScope.(HttpRequestData, String) -> HttpResponseData = { _, _ -> respond("", HttpStatusCode.NotFound) }

    private val client = AccountClient(
        "https://api.example.com/",
        store,
        MockEngine { request ->
            val body = String(request.body.toByteArray())
            requests += "${request.method.value} ${request.url.encodedPath} $body".trim()
            answer(request, body)
        },
        clock,
    )

    private fun MockRequestHandleScope.json(text: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(text, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun tokens(n: Int) = """{"access_token":"cp_at_$n","token_type":"Bearer","expires_in":900,"refresh_token":"cp_rt_$n","scope":"sync"}"""

    @Test
    fun signingInKeepsTheTokens() = runTest {
        answer = { _, _ -> json(tokens(1)) }

        assertEquals(SignInAnswer.SignedIn, client.signIn("ana@example.com", "secret-pass", "Ana's iPhone"))

        assertEquals(Tokens("cp_at_1", "cp_rt_1", now + 15.minutes), store.tokens)
        val request = requests.single()
        assertTrue(request.startsWith("POST /oauth/token grant_type=password&username=ana%40example.com"), request)
        assertTrue("device_name=Ana%27s+iPhone" in request, request)
    }

    @Test
    fun aRefusedOrThrottledSignInKeepsNoTokens() = runTest {
        answer = { _, _ -> json("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest) }
        assertEquals(SignInAnswer.Refused, client.signIn("ana@example.com", "wrong", "Ana's iPhone"))

        answer = { _, _ -> respond("""{"error":"invalid_grant"}""", HttpStatusCode.TooManyRequests, headersOf("Retry-After", "600")) }
        assertEquals(SignInAnswer.TooManyTries(600), client.signIn("ana@example.com", "wrong", "Ana's iPhone"))
        assertNull(store.tokens)
    }

    @Test
    fun signUpAnswersBecomeTheirReason() = runTest {
        answer = { _, _ -> respond("", HttpStatusCode.Accepted) }
        assertEquals(SignUpAnswer.ACCEPTED, client.signUp("ana@example.com", "secret-pass", "fr", true))
        assertEquals(
            """POST /api/v1/accounts {"email":"ana@example.com","password":"secret-pass","language":"fr","healthDataConsent":true}""",
            requests.single(),
        )

        answer = { _, _ -> json("""{"error":"invalid-password"}""", HttpStatusCode.BadRequest) }
        assertEquals(SignUpAnswer.INVALID_PASSWORD, client.signUp("ana@example.com", "short", "fr", true))
    }

    @Test
    fun anExpiredAccessTokenIsRefreshedFirst() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now + 10.minutes)
        now += 10.minutes
        answer = { request, _ ->
            if (request.url.encodedPath == "/oauth/token") json(tokens(2))
            else json("""{"changes":[],"cursor":"0","more":false}""")
        }

        client.pull(null, 500)

        assertEquals(listOf("POST /oauth/token grant_type=refresh_token&refresh_token=cp_rt_1&client_id=app", "GET /api/v1/sync/pull"), requests)
        assertEquals("cp_rt_2", store.tokens?.refreshToken)
    }

    @Test
    fun aRefusedAccessTokenIsRefreshedOnceAndTheCallSentAgain() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now + 10.minutes)
        answer = { request, _ ->
            when {
                request.url.encodedPath == "/oauth/token" -> json(tokens(2))
                request.headers[HttpHeaders.Authorization] == "Bearer cp_at_2" -> json("""{"accountId":"a","email":"ana@example.com","role":"member","client":"app","scopes":["sync"]}""")
                else -> respond("", HttpStatusCode.Unauthorized)
            }
        }

        assertEquals("ana@example.com", client.me().email)
        assertEquals(3, requests.size)
    }

    @Test
    fun callsWaitingOnARefreshSpendTheRefreshTokenOnce() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now)
        answer = { request, _ ->
            if (request.url.encodedPath == "/oauth/token") json(tokens(2))
            else json("""{"changes":[],"cursor":"0","more":false}""")
        }

        (1..3).map { async { client.pull(null, 500) } }.awaitAll()

        assertEquals(1, requests.count { it.startsWith("POST /oauth/token") })
    }

    @Test
    fun aRefusedRefreshTokenSignsThePhoneOut() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now)
        answer = { _, _ -> json("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest) }

        assertFailsWith<SignInRefusedByServer> { client.pull(null, 500) }
        assertNull(store.tokens)
    }

    @Test
    fun pushAnswersAreRead() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now + 10.minutes)
        val push = SyncPush(listOf(SettingChange("profile.name", "Ana", now)))

        answer = { _, _ -> json("""{"error":"stale-base","workouts":["w1"]}""", HttpStatusCode.Conflict) }
        assertEquals(PushResult.StaleBase(listOf(WorkoutId("w1"))), client.push(push))
        assertTrue(requests.single().contains(""""type":"setting""""), requests.single())

        answer = { _, _ -> json("""{"error":"invalid-change","index":0}""", HttpStatusCode.BadRequest) }
        assertEquals(PushResult.InvalidChange(0), client.push(push))

        answer = { _, _ -> respond("", HttpStatusCode.OK) }
        assertEquals(PushResult.Accepted, client.push(push))
    }

    @Test
    fun signingOutRevokesTheRefreshTokenAndForgetsIt() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now + 10.minutes)
        answer = { _, _ -> respond("", HttpStatusCode.OK) }

        client.signOut()

        assertEquals("POST /oauth/revoke token=cp_rt_1", requests.single())
        assertNull(store.tokens)
    }

    @Test
    fun deletingTheAccountForgetsTheTokens() = runTest {
        store.tokens = Tokens("cp_at_1", "cp_rt_1", now + 10.minutes)
        answer = { _, body -> if ("right" in body) respond("", HttpStatusCode.NoContent) else json("""{"error":"wrong-password"}""", HttpStatusCode.Forbidden) }

        assertEquals(DeleteAnswer.WRONG_PASSWORD, client.deleteAccount("wrong"))
        assertEquals(DeleteAnswer.DELETED, client.deleteAccount("right"))
        assertNull(store.tokens)
    }

    private class MemoryTokens : TokenStore {
        var tokens: Tokens? = null
        override fun read() = tokens
        override fun write(tokens: Tokens?) { this.tokens = tokens }
    }
}
