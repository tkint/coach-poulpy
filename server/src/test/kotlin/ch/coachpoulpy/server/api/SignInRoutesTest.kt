package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.auth.Authenticator
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.Scope
import ch.coachpoulpy.server.auth.authenticate
import ch.coachpoulpy.server.auth.requires
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SignInRoutesTest {
    private val test = TestServer()

    init {
        test.accounts.signUp(SignUp("Ana@Example.com", "correct horse", "fr", healthDataConsent = true))
        val token = test.mailer.sent.single().text.substringAfter("#").substringBefore("\n")
        check(test.accounts.confirm(token))
    }

    private fun json(response: Response): JsonObject = Json.parseToJsonElement(response.bodyString()).jsonObject

    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.content

    private fun signIn(email: String = "ana@example.com", password: String = "correct horse", device: String = "Ana's iPhone"): Response =
        test.http(
            Request(POST, "/api/v1/sign-ins").header("Content-Type", "application/json")
                .body("""{"email":"$email","password":"$password","deviceName":"$device"}"""),
        )

    private fun refresh(token: String): Response = test.http(
        Request(POST, "/api/v1/sign-ins/refresh").header("Content-Type", "application/json").body("""{"refreshToken":"$token"}"""),
    )

    private fun me(accessToken: String): Response = test.http(Request(GET, "/api/v1/me").header("Authorization", "Bearer $accessToken"))

    private fun tokens(): Pair<String, String> = json(signIn()).let { it.text("accessToken") to it.text("refreshToken") }

    @Test
    fun signingInGivesAShortAccessTokenAndARefreshToken() {
        val response = signIn(email = "ANA@example.com")
        assertEquals(OK, response.status)
        val body = json(response)
        assertTrue(body.text("accessToken").startsWith("cp_at_"))
        assertTrue(body.text("refreshToken").startsWith("cp_rt_"))
        assertEquals("Bearer", body.text("tokenType"))
        assertEquals("900", body.text("expiresIn"))
        assertEquals(6 + 43, body.text("accessToken").length)
    }

    @Test
    fun meSaysWhoTheCallerIs() {
        val response = me(tokens().first)
        assertEquals(OK, response.status)
        val body = json(response)
        assertEquals("Ana@Example.com", body.text("email"))
        assertEquals("member", body.text("role"))
        assertEquals("app", body.text("client"))
        assertTrue("sync" in body.getValue("scopes").jsonArray.map { it.jsonPrimitive.content })
        assertEquals(36, body.text("accountId").length)
    }

    @Test
    fun meNeedsASignedInCaller() {
        val response = test.http(Request(GET, "/api/v1/me"))
        assertEquals(UNAUTHORIZED, response.status)
        assertEquals("Bearer", response.header("WWW-Authenticate"))
    }

    @Test
    fun aWrongPasswordAnUnknownAddressAndAnUnconfirmedEmailGetTheSameAnswer() {
        test.accounts.signUp(SignUp("new@example.com", "correct horse", "en", healthDataConsent = true))
        for (response in listOf(signIn(password = "wrong password"), signIn(email = "nobody@example.com"), signIn(email = "new@example.com"))) {
            assertEquals(UNAUTHORIZED, response.status)
            assertEquals("invalid-credentials", json(response).text("error"))
        }
    }

    @Test
    fun repeatedFailuresAreThrottledPerAddress() {
        repeat(5) { assertEquals(UNAUTHORIZED, signIn(password = "wrong password").status) }
        val throttled = signIn()
        assertEquals(TOO_MANY_REQUESTS, throttled.status)
        assertEquals("900", throttled.header("Retry-After"))
        // Another address is not slowed down.
        assertEquals(UNAUTHORIZED, signIn(email = "other@example.com").status)
        test.clock.now = test.clock.now.plus(Duration.ofMinutes(16))
        assertEquals(OK, signIn().status)
    }

    @Test
    fun anAccessTokenExpiresAfterFifteenMinutes() {
        val (access, _) = tokens()
        test.clock.now = test.clock.now.plus(Duration.ofMinutes(15))
        assertEquals(UNAUTHORIZED, me(access).status)
    }

    @Test
    fun aRefreshTokenGivesNewTokensAndWorksOnce() {
        val (_, refresh) = tokens()
        test.clock.now = test.clock.now.plus(Duration.ofMinutes(20))
        val response = refresh(refresh)
        assertEquals(OK, response.status)
        val next = json(response)
        assertNotEquals(refresh, next.text("refreshToken"))
        assertEquals(OK, me(next.text("accessToken")).status)
        assertEquals(OK, refresh(next.text("refreshToken")).status)
    }

    @Test
    fun reusingARefreshTokenSignsTheDeviceOut() {
        val (_, first) = tokens()
        val second = json(refresh(first))
        val reused = refresh(first)
        assertEquals(UNAUTHORIZED, reused.status)
        assertEquals("invalid-token", json(reused).text("error"))
        // Both holders are out: the new access token and the new refresh token stop working too.
        assertEquals(UNAUTHORIZED, me(second.text("accessToken")).status)
        assertEquals(UNAUTHORIZED, refresh(second.text("refreshToken")).status)
    }

    @Test
    fun anotherDeviceStaysSignedInWhenOneIsRevoked() {
        val (_, phone) = tokens()
        val (tablet, _) = json(signIn(device = "Ana's iPad")).let { it.text("accessToken") to it.text("refreshToken") }
        refresh(phone)
        refresh(phone)
        assertEquals(OK, me(tablet).status)
    }

    @Test
    fun aRefreshTokenExpiresAfterNinetyDaysUnused() {
        val (_, refresh) = tokens()
        test.clock.now = test.clock.now.plus(Duration.ofDays(90))
        assertEquals(UNAUTHORIZED, refresh(refresh).status)
    }

    @Test
    fun anUnknownRefreshTokenIsRefused() {
        assertEquals(UNAUTHORIZED, refresh("cp_rt_unknown").status)
    }

    @Test
    fun signingOutRevokesTheDevicesTokens() {
        val (access, refresh) = tokens()
        val response = test.http(Request(DELETE, "/api/v1/sign-ins/current").header("Authorization", "Bearer $access"))
        assertEquals(NO_CONTENT, response.status)
        assertEquals(UNAUTHORIZED, me(access).status)
        assertEquals(UNAUTHORIZED, refresh(refresh).status)
    }

    @Test
    fun signingOutNeedsASignedInCaller() {
        assertEquals(UNAUTHORIZED, test.http(Request(DELETE, "/api/v1/sign-ins/current")).status)
    }

    @Test
    fun aRefreshTokenIsNotAnAccessToken() {
        assertEquals(UNAUTHORIZED, me(tokens().second).status)
    }

    @Test
    fun aRouteNeedingAScopeTheCallerLacksIs403() {
        val limited = Caller.Signed(AccountId("0190d4d2-0000-7000-8000-000000000001"), Client("pat"), setOf(Scope.WorkoutsRead))
        val handler = authenticate(mapOf("cp_pat_" to Authenticator { limited })).then(
            contract {
                routes += "/workouts" meta { security = requires(Scope.WorkoutsWrite) } bindContract POST to { _ -> Response(OK) }
                routes += "/workouts" meta { security = requires(Scope.WorkoutsRead) } bindContract GET to { _ -> Response(OK) }
            },
        )
        val write = handler(Request(POST, "/workouts").header("Authorization", "Bearer cp_pat_x"))
        assertEquals(FORBIDDEN, write.status)
        assertEquals("Bearer error=\"insufficient_scope\", scope=\"workouts:write\"", write.header("WWW-Authenticate"))
        assertEquals(OK, handler(Request(GET, "/workouts").header("Authorization", "Bearer cp_pat_x")).status)
        assertEquals(UNAUTHORIZED, handler(Request(GET, "/workouts")).status)
    }
}
