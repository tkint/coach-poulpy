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
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import org.http4k.core.then
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class OAuthRoutesTest {
    private val test = TestServer()

    init {
        test.accounts.signUp(SignUp("Ana@Example.com", "correct horse", "fr", healthDataConsent = true))
        val token = test.mailer.sent.single().text.substringAfter("#").substringBefore("\n")
        check(test.accounts.confirm(token))
    }

    private fun json(response: Response): JsonObject = Json.parseToJsonElement(response.bodyString()).jsonObject

    private fun JsonObject.text(key: String) = getValue(key).jsonPrimitive.content

    private fun form(path: String, vararg fields: Pair<String, String>): Response = test.http(
        fields.fold(Request(POST, path).header("Content-Type", "application/x-www-form-urlencoded")) { request, (name, value) ->
            request.form(name, value)
        },
    )

    private fun signIn(email: String = "ana@example.com", password: String = "correct horse", device: String = "Ana's iPhone"): Response =
        form("/oauth/token", "grant_type" to "password", "username" to email, "password" to password, "device_name" to device)

    private fun refresh(token: String): Response = form("/oauth/token", "grant_type" to "refresh_token", "refresh_token" to token)

    private fun revoke(token: String): Response = form("/oauth/revoke", "token" to token)

    private fun assertInvalidGrant(response: Response) {
        assertEquals(BAD_REQUEST, response.status)
        assertEquals("invalid_grant", json(response).text("error"))
    }

    private fun me(accessToken: String): Response = test.http(Request(GET, "/api/v1/me").header("Authorization", "Bearer $accessToken"))

    private fun tokens(): Pair<String, String> = json(signIn()).let { it.text("access_token") to it.text("refresh_token") }

    @Test
    fun thePasswordGrantGivesAShortAccessTokenAndARefreshToken() {
        val response = signIn(email = "ANA@example.com")
        assertEquals(OK, response.status)
        assertEquals("no-store", response.header("Cache-Control"))
        val body = json(response)
        assertTrue(body.text("access_token").startsWith("cp_at_"))
        assertTrue(body.text("refresh_token").startsWith("cp_rt_"))
        assertEquals("Bearer", body.text("token_type"))
        assertEquals("900", body.text("expires_in"))
        assertTrue("sync" in body.text("scope").split(" "))
        assertEquals(6 + 43, body.text("access_token").length)
    }

    @Test
    fun aMissingFieldIsAnInvalidRequest() {
        val response = form("/oauth/token", "grant_type" to "password", "username" to "ana@example.com")
        assertEquals(BAD_REQUEST, response.status)
        assertEquals("invalid_request", json(response).text("error"))
        assertEquals("invalid_request", json(form("/oauth/token")).text("error"))
    }

    @Test
    fun anotherGrantTypeIsUnsupported() {
        assertEquals("unsupported_grant_type", json(form("/oauth/token", "grant_type" to "client_credentials")).text("error"))
    }

    @Test
    fun anUnknownClientIsRefused() {
        val response = form("/oauth/token", "grant_type" to "password", "username" to "ana@example.com", "password" to "correct horse", "client_id" to "other")
        assertEquals(UNAUTHORIZED, response.status)
        assertEquals("invalid_client", json(response).text("error"))
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
            assertInvalidGrant(response)
        }
    }

    @Test
    fun repeatedFailuresAreThrottledPerAddress() {
        repeat(5) { assertInvalidGrant(signIn(password = "wrong password")) }
        val throttled = signIn()
        assertEquals(TOO_MANY_REQUESTS, throttled.status)
        assertEquals("900", throttled.header("Retry-After"))
        // Another address is not slowed down.
        assertInvalidGrant(signIn(email = "other@example.com"))
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
        assertNotEquals(refresh, next.text("refresh_token"))
        assertEquals(OK, me(next.text("access_token")).status)
        assertEquals(OK, refresh(next.text("refresh_token")).status)
    }

    @Test
    fun reusingARefreshTokenSignsTheDeviceOut() {
        val (_, first) = tokens()
        val second = json(refresh(first))
        assertInvalidGrant(refresh(first))
        // Both holders are out: the new access token and the new refresh token stop working too.
        assertEquals(UNAUTHORIZED, me(second.text("access_token")).status)
        assertInvalidGrant(refresh(second.text("refresh_token")))
    }

    @Test
    fun anotherDeviceStaysSignedInWhenOneIsRevoked() {
        val (_, phone) = tokens()
        val (tablet, _) = json(signIn(device = "Ana's iPad")).let { it.text("access_token") to it.text("refresh_token") }
        refresh(phone)
        refresh(phone)
        assertEquals(OK, me(tablet).status)
    }

    @Test
    fun aRefreshTokenExpiresAfterNinetyDaysUnused() {
        val (_, refresh) = tokens()
        test.clock.now = test.clock.now.plus(Duration.ofDays(90))
        assertInvalidGrant(refresh(refresh))
    }

    @Test
    fun anUnknownRefreshTokenIsRefused() {
        assertInvalidGrant(refresh("cp_rt_unknown"))
    }

    @Test
    fun revokingTheAccessTokenSignsTheDeviceOut() {
        val (access, refresh) = tokens()
        assertEquals(OK, revoke(access).status)
        assertEquals(UNAUTHORIZED, me(access).status)
        assertInvalidGrant(refresh(refresh))
    }

    @Test
    fun revokingTheRefreshTokenSignsTheDeviceOut() {
        val (access, refresh) = tokens()
        assertEquals(OK, revoke(refresh).status)
        assertEquals(UNAUTHORIZED, me(access).status)
    }

    @Test
    fun revokingAnUnknownTokenStillAnswers200() {
        assertEquals(OK, revoke("cp_at_unknown").status)
        assertEquals("invalid_request", json(form("/oauth/revoke")).text("error"))
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
