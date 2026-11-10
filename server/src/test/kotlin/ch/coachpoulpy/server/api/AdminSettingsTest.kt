package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.admin.AdminAction
import ch.coachpoulpy.server.admin.EmailAddress
import ch.coachpoulpy.server.admin.mailFromOrNull
import ch.coachpoulpy.server.admin.promoteFirstAdmin
import ch.coachpoulpy.server.app
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import jakarta.mail.AuthenticationFailedException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.events.and
import org.http4k.testing.RecordingEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdminSettingsTest {
    private val test = TestServer()
    private val events = RecordingEvents()
    private val http = app(
        "0.7.0", test.catalog, test.database, test.accounts, test.signIns, test.passwordResets, test.accountDeletion, test.sync, test.audit,
        events = events.and(test.audit.events), settings = test.settings, mailer = test.outgoing,
    )
    private val adminToken: String

    init {
        account("ana@example.com")
        promoteFirstAdmin(test.database, test.audit.events, EmailAddress.of("ana@example.com"))
        adminToken = signIn("ana@example.com", Client.Website).tokens.accessToken
    }

    private fun account(email: String) {
        test.accounts.signUp(SignUp(email, "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
    }

    private fun signIn(email: String, client: Client = Client.App) =
        test.signIns.signIn(email, "correct horse", "A device", client) as SignInResult.Signed

    private fun call(request: Request, token: String = adminToken): Response = http(request.header("Authorization", "Bearer $token"))

    private fun body(response: Response) = Json.parseToJsonElement(response.bodyString()).jsonObject

    private fun settings() = body(call(Request(GET, "/api/v1/admin/settings")))

    private fun saveFrom(from: String, token: String = adminToken) = call(
        Request(POST, "/api/v1/admin/settings/mail-from").header("content-type", "application/json").body("""{"mailFrom":"$from"}"""),
        token,
    )

    private fun testEmail() = call(Request(POST, "/api/v1/admin/settings/test-email"))

    @Test
    fun mailFromStaysTheDefaultUntilAValueIsSaved() {
        val shown = settings()
        assertEquals("Coach Poulpy <hello@coachpoulpy.example>", shown.getValue("mailFrom").jsonPrimitive.content)
        assertEquals("false", shown.getValue("mailFromSaved").jsonPrimitive.content)
        account("lea@example.com")
        assertEquals("Coach Poulpy <hello@coachpoulpy.example>", test.mailer.sent.last().from)
    }

    @Test
    fun aSavedFromAddressIsUsedByTheNextEmail() {
        assertEquals(NO_CONTENT, saveFrom("Poulpy <team@poulpy.example>").status)
        assertEquals("Poulpy <team@poulpy.example>", settings().getValue("mailFrom").jsonPrimitive.content)
        assertEquals("true", settings().getValue("mailFromSaved").jsonPrimitive.content)
        account("lea@example.com")
        assertEquals("Poulpy <team@poulpy.example>", test.mailer.sent.last().from)
        assertEquals(NO_CONTENT, saveFrom("hello@poulpy.example").status)
        test.passwordResets.request("lea@example.com")
        assertEquals("hello@poulpy.example", test.mailer.sent.last().from)
    }

    @Test
    fun savingIsWrittenToTheAuditLogWithAMaskedAddress() {
        // A minute after the promotion, so the audit log's newest-first order doesn't hang on two ids made in the same millisecond.
        test.clock.now = test.clock.now.plusSeconds(60)
        saveFrom("Poulpy <team@poulpy.example>")
        val action = events.filterIsInstance<AdminAction>().single()
        assertEquals("admin.settings.mail-from", action.action)
        assertEquals("t***@poulpy.example", action.target)
        assertFalse("team@poulpy.example" in action.toString())
        assertEquals("admin.settings.mail-from", test.audit.recent().first().action)
    }

    @Test
    fun anInvalidAddressIsRefusedAndNothingChanges() {
        listOf("hello@", "not an email", "", "a@b.example, c@d.example", "Poulpy <team@poulpy.example").forEach {
            val response = saveFrom(it)
            assertEquals(BAD_REQUEST, response.status, it)
            assertEquals("invalid-address", body(response).getValue("error").jsonPrimitive.content)
        }
        assertEquals("false", settings().getValue("mailFromSaved").jsonPrimitive.content)
        assertTrue(events.none { it is AdminAction })
    }

    @Test
    fun aFromAddressIsOneAddressWithOrWithoutAName() {
        assertEquals("hello@example.com", mailFromOrNull(" hello@example.com "))
        assertEquals("Coach Poulpy <hello@example.com>", mailFromOrNull("Coach Poulpy <hello@example.com>"))
        assertNull(mailFromOrNull("hello@"))
        assertNull(mailFromOrNull("@example.com"))
    }

    @Test
    fun theTestEmailGoesToTheAdminWithTheSavedSettings() {
        saveFrom("Poulpy <team@poulpy.example>")
        val response = testEmail()
        assertEquals(OK, response.status)
        assertEquals("true", body(response).getValue("sent").jsonPrimitive.content)
        assertEquals(JsonNull, body(response).getValue("error"))
        val sent = test.mailer.sent.last()
        assertEquals("ana@example.com", sent.to)
        assertEquals("Poulpy <team@poulpy.example>", sent.from)
        assertEquals("Coach Poulpy test email", sent.subject)
        val last = assertNotNull(test.outgoing.last)
        assertTrue(last.sent)
        assertEquals("a***@example.com", last.to.toString())
    }

    @Test
    fun theTestEmailReportsTheSmtpError() {
        test.mailer.failure = AuthenticationFailedException("535 Authentication failed")
        val response = testEmail()
        assertEquals(OK, response.status)
        assertEquals("false", body(response).getValue("sent").jsonPrimitive.content)
        assertEquals("535 Authentication failed", body(response).getValue("error").jsonPrimitive.content)
        assertEquals("535 Authentication failed", test.outgoing.last?.error)
    }

    @Test
    fun onlyAnAdminOnTheWebsiteReachesTheSettings() {
        account("lea@example.com")
        val member = signIn("lea@example.com", Client.Website).tokens.accessToken
        assertEquals(FORBIDDEN, call(Request(GET, "/api/v1/admin/settings"), member).status)
        assertEquals(FORBIDDEN, saveFrom("lea@example.com", member).status)
        assertEquals(FORBIDDEN, call(Request(POST, "/api/v1/admin/settings/test-email"), signIn("ana@example.com").tokens.accessToken).status)
    }
}
