package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.admin.EmailAddress
import ch.coachpoulpy.server.admin.promoteFirstAdmin
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebSessionRoutesTest {
    private val test = TestServer()
    private val website = "https://app.coach-poulpy.com"

    init {
        test.accounts.signUp(SignUp("ana@example.com", "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.single().text.substringAfter("#").substringBefore("\n")))
    }

    private fun signIn(password: String = "correct horse", origin: String? = website): Response = test.http(
        Request(POST, "/api/v1/web/session")
            .header("Content-Type", "application/json")
            .let { if (origin == null) it else it.header("Origin", origin) }
            .body("""{"email":"ana@example.com","password":"$password"}"""),
    )

    private fun Response.cookie(name: String): Cookie = assertNotNull(cookies().firstOrNull { it.name == name }, "no $name cookie")

    private fun Request.withCookies(response: Response): Request =
        response.cookies().filter { it.value.isNotEmpty() }.fold(this) { request, cookie -> request.cookie(cookie.name, cookie.value) }

    private fun me(response: Response) = test.http(Request(GET, "/api/v1/me").withCookies(response))

    @Test
    fun signingInSetsHttpOnlySecureStrictCookies() {
        val response = signIn()
        assertEquals(NO_CONTENT, response.status)
        assertEquals("no-store", response.header("Cache-Control"))
        val access = response.cookie("__Host-cp_at")
        assertTrue(access.value.startsWith("cp_at_"))
        assertEquals("/", access.path)
        assertEquals(900, access.maxAge)
        val refresh = response.cookie("__Secure-cp_rt")
        assertTrue(refresh.value.startsWith("cp_rt_"))
        assertEquals("/api/v1/web/session", refresh.path)
        for (cookie in listOf(access, refresh)) {
            assertTrue(cookie.secure && cookie.httpOnly)
            assertEquals(SameSite.Strict, cookie.sameSite)
            assertNull(cookie.domain)
        }
    }

    @Test
    fun theCookieSignsTheBrowserIn() {
        val response = me(signIn())
        assertEquals(OK, response.status)
        val body = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("ana@example.com", body.getValue("email").jsonPrimitive.content)
        assertEquals("website", body.getValue("client").jsonPrimitive.content)
    }

    @Test
    fun aWrongPasswordIs400AndSetsNoCookie() {
        val response = signIn(password = "wrong")
        assertEquals(BAD_REQUEST, response.status)
        assertEquals(emptyList(), response.cookies())
    }

    @Test
    fun signingInFromAnotherSiteOrWithNoOriginIs403() {
        assertEquals(FORBIDDEN, signIn(origin = "https://evil.example.com").status)
        assertEquals(FORBIDDEN, signIn(origin = null).status)
    }

    @Test
    fun aWriteWithTheCookieFromAnotherSiteIs403() {
        val signedIn = signIn()
        val evil = test.http(Request(DELETE, "/api/v1/me").header("Origin", "https://evil.example.com").withCookies(signedIn))
        assertEquals(FORBIDDEN, evil.status)
        val none = test.http(Request(DELETE, "/api/v1/me").withCookies(signedIn))
        assertEquals(FORBIDDEN, none.status)
    }

    @Test
    fun aMemberWithTheCookieIs403OnAdminRoutesAndAnAdminIs200() {
        val member = signIn()
        assertEquals(FORBIDDEN, test.http(Request(GET, "/api/v1/admin/audit").withCookies(member)).status)
        promoteFirstAdmin(test.database, test.audit.events, EmailAddress.of("ana@example.com"))
        val admin = signIn()
        assertEquals(OK, test.http(Request(GET, "/api/v1/admin/audit").withCookies(admin)).status)
    }

    @Test
    fun anAppTokenInTheCookieIsNotASignIn() {
        val app = test.signIns.signIn("ana@example.com", "correct horse", "Ana's phone") as ch.coachpoulpy.server.auth.SignInResult.Signed
        val response = test.http(Request(GET, "/api/v1/me").cookie("__Host-cp_at", app.tokens.accessToken))
        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun refreshingGivesNewCookiesAndTheOldRefreshCookieStopsWorking() {
        val first = signIn()
        val refresh = { response: Response ->
            test.http(Request(POST, "/api/v1/web/session/refresh").header("Origin", website).withCookies(response))
        }
        val second = refresh(first)
        assertEquals(NO_CONTENT, second.status)
        assertTrue(second.cookie("__Secure-cp_rt").value != first.cookie("__Secure-cp_rt").value)
        assertEquals(OK, me(second).status)
        val reused = refresh(first)
        assertEquals(UNAUTHORIZED, reused.status)
        assertEquals(0, reused.cookie("__Host-cp_at").maxAge)
        assertEquals(UNAUTHORIZED, me(second).status)
    }

    @Test
    fun refreshingWithNoCookieIs401() {
        val response = test.http(Request(POST, "/api/v1/web/session/refresh").header("Origin", website))
        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun signingOutRevokesTheTokensAndClearsTheCookies() {
        val signedIn = signIn()
        val out = test.http(Request(DELETE, "/api/v1/web/session").header("Origin", website).withCookies(signedIn))
        assertEquals(NO_CONTENT, out.status)
        assertEquals(0, out.cookie("__Host-cp_at").maxAge)
        assertEquals(0, out.cookie("__Secure-cp_rt").maxAge)
        assertEquals(UNAUTHORIZED, me(signedIn).status)
    }

    @Test
    fun theTokenEndpointRefusesTheWebsiteClient() {
        val response = test.http(
            Request(POST, "/oauth/token").header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=password&username=ana%40example.com&password=correct+horse&client_id=website"),
        )
        assertEquals(UNAUTHORIZED, response.status)
    }

    @Test
    fun corsAllowsTheWebsiteWithCredentials() {
        val response = test.http(
            Request(org.http4k.core.Method.OPTIONS, "/api/v1/web/session")
                .header("Origin", website).header("Access-Control-Request-Method", "DELETE"),
        )
        assertEquals(website, response.header("Access-Control-Allow-Origin"))
        assertEquals("true", response.header("Access-Control-Allow-Credentials"))
        assertTrue("DELETE" in response.header("Access-Control-Allow-Methods").orEmpty())
    }
}
