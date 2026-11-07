package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.AccountProblem
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.mail.Email
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasswordResetRoutesTest {
    private val test = TestServer()

    init {
        test.accounts.signUp(SignUp("Ana@Example.com", "correct horse", "fr", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.single().text.substringAfter("#").substringBefore("\n")))
        test.mailer.sent.clear()
    }

    private fun json(path: String, body: String, method: org.http4k.core.Method = POST, token: String? = null): Response = test.http(
        Request(method, path).header("Content-Type", "application/json").body(body)
            .let { if (token == null) it else it.header("Authorization", "Bearer $token") },
    )

    private fun askReset(email: String = "ana@example.com") = json("/api/v1/password-resets", """{"email":"$email"}""")

    private fun newPassword(token: String, password: String = "a new password") =
        json("/api/v1/password-resets/confirm", """{"token":"$token","password":"$password"}""")

    private fun deleteMe(accessToken: String, password: String = "correct horse") =
        json("/api/v1/me", """{"password":"$password"}""", DELETE, accessToken)

    private fun Email.token(): String = Regex("https://app\\.example\\.com/reset-password#([A-Za-z0-9_-]+)").find(text)!!.groupValues[1]

    private fun Response.error(): String = Json.parseToJsonElement(bodyString()).jsonObject.getValue("error").jsonPrimitive.content

    private fun signIn(password: String = "correct horse"): Response = test.http(
        Request(POST, "/oauth/token").header("Content-Type", "application/x-www-form-urlencoded")
            .form("grant_type", "password").form("username", "ana@example.com").form("password", password),
    )

    private fun accessToken(password: String = "correct horse"): String =
        Json.parseToJsonElement(signIn(password).bodyString()).jsonObject.getValue("access_token").jsonPrimitive.content

    private fun me(accessToken: String) = test.http(Request(GET, "/api/v1/me").header("Authorization", "Bearer $accessToken"))

    @Test
    fun aConfirmedAccountGetsALinkInItsLanguage() {
        assertEquals(ACCEPTED, askReset("ANA@example.com").status)
        val email = test.mailer.sent.single()
        assertEquals("Ana@Example.com", email.to)
        assertTrue(email.subject.contains("mot de passe"), email.subject)
        assertEquals(43, email.token().length)
    }

    @Test
    fun anUnknownOrUnconfirmedAddressGetsTheSameAnswerAndNoEmail() {
        test.accounts.signUp(SignUp("new@example.com", "correct horse", "en", healthDataConsent = true))
        test.mailer.sent.clear()
        assertEquals(ACCEPTED, askReset("nobody@example.com").status)
        assertEquals(ACCEPTED, askReset("new@example.com").status)
        assertEquals(emptyList(), test.mailer.sent)
    }

    @Test
    fun atMostThreeLinksAnHour() {
        repeat(5) { assertEquals(ACCEPTED, askReset().status) }
        assertEquals(3, test.mailer.sent.size)
        test.clock.now = test.clock.now.plus(Duration.ofHours(1))
        askReset()
        assertEquals(4, test.mailer.sent.size)
    }

    @Test
    fun theLinkSetsTheNewPasswordOnceAndSignsEveryDeviceOut() {
        val phone = accessToken()
        askReset()
        askReset()
        val token = test.mailer.sent[0].token()
        assertEquals(NO_CONTENT, newPassword(token).status)
        assertEquals(UNAUTHORIZED, me(phone).status)
        assertEquals(BAD_REQUEST, signIn().status)
        assertEquals(OK, signIn("a new password").status)
        assertEquals("invalid-token", newPassword(token, "yet another one").error())
        // The other link sent before it no longer works either.
        assertEquals("invalid-token", newPassword(test.mailer.sent[1].token(), "yet another one").error())
    }

    @Test
    fun aLinkExpiresAfterAnHour() {
        askReset()
        test.clock.now = test.clock.now.plus(Duration.ofHours(1))
        assertEquals("invalid-token", newPassword(test.mailer.sent.single().token()).error())
        assertEquals(OK, signIn().status)
    }

    @Test
    fun aPasswordTooShortIsRefusedAndTheLinkStillWorks() {
        askReset()
        val token = test.mailer.sent.single().token()
        assertEquals("invalid-password", newPassword(token, "short").error())
        assertEquals(NO_CONTENT, newPassword(token).status)
    }

    @Test
    fun anUnknownTokenIsRefused() {
        assertEquals("invalid-token", newPassword("not-a-token").error())
    }

    @Test
    fun deletingTheAccountRemovesItAndSignsItsDevicesOut() {
        val phone = accessToken()
        val tablet = accessToken()
        askReset()
        assertEquals(NO_CONTENT, deleteMe(phone).status)
        assertNull(test.database.queries.accountQueries.byEmail("ana@example.com").executeAsOneOrNull())
        assertEquals(UNAUTHORIZED, me(tablet).status)
        assertEquals(BAD_REQUEST, signIn().status)
        assertEquals("invalid-token", newPassword(test.mailer.sent.single().token()).error())
        // The address is free again.
        test.accounts.signUp(SignUp("ana@example.com", "correct horse", "en", healthDataConsent = true))
        assertTrue(test.mailer.sent.last().subject.startsWith("Confirm your email"))
    }

    @Test
    fun deletingNeedsTheRightPassword() {
        val phone = accessToken()
        val response = deleteMe(phone, "wrong password")
        assertEquals(FORBIDDEN, response.status)
        assertEquals("wrong-password", response.error())
        assertEquals(OK, me(phone).status)
    }

    @Test
    fun deletingNeedsASignIn() {
        assertEquals(UNAUTHORIZED, test.http(Request(DELETE, "/api/v1/me").header("Content-Type", "application/json").body("""{"password":"x"}""")).status)
    }

    @Test
    fun onlyAFirstPartyClientMayDelete() {
        val id = test.database.queries.accountQueries.byEmail("ana@example.com").executeAsOne().id
        val website = Caller.Signed(AccountId(id.toString()), Client("partner"), emptySet())
        assertEquals(AccountProblem.NotFirstParty, test.accountDeletion.deleteAccount(website, "correct horse"))
    }
}
