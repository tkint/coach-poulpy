package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.admin.AdminAction
import ch.coachpoulpy.server.admin.EmailAddress
import ch.coachpoulpy.server.admin.promoteFirstAdmin
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import dev.forkhandles.values.parseOrNull
import org.http4k.testing.RecordingEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdminRoutesTest {
    private val test = TestServer()

    private fun account(email: String, confirm: Boolean = true) {
        test.accounts.signUp(SignUp(email, "correct horse", "en", healthDataConsent = true))
        if (confirm) check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
    }

    private fun promote(email: String, events: org.http4k.events.Events = test.audit.events) =
        promoteFirstAdmin(test.database, events, EmailAddress.of(email))

    private fun accessToken(email: String, client: Client): String {
        val result = test.signIns.signIn(email, "correct horse", "A browser", client) as SignInResult.Signed
        return result.tokens.accessToken
    }

    private fun audit(token: String?) = test.http(
        Request(GET, "/api/v1/admin/audit").let { if (token == null) it else it.header("Authorization", "Bearer $token") },
    )

    @Test
    fun anonymousCallersGet401() {
        assertEquals(UNAUTHORIZED, audit(null).status)
    }

    @Test
    fun aMemberGets403EvenFromTheWebsite() {
        account("ana@example.com")
        assertEquals(FORBIDDEN, audit(accessToken("ana@example.com", Client.Website)).status)
    }

    @Test
    fun anAdminOutsideTheWebsiteGets403() {
        account("ana@example.com")
        assertTrue(promote("ana@example.com"))
        assertEquals(FORBIDDEN, audit(accessToken("ana@example.com", Client.App)).status)
    }

    @Test
    fun anAdminOnTheWebsiteReadsTheAuditLog() {
        account("ana@example.com")
        promote("ana@example.com")
        val response = audit(accessToken("ana@example.com", Client.Website))
        assertEquals(OK, response.status)
        val entry = Json.parseToJsonElement(response.bodyString()).jsonObject.getValue("entries").jsonArray.single().jsonObject
        assertEquals("admin.promote", entry.getValue("action").jsonPrimitive.content)
    }

    @Test
    fun promotingNeedsAConfirmedAccount() {
        account("ana@example.com", confirm = false)
        assertFalse(promote("ana@example.com"))
        assertFalse(promote("nobody@example.com"))
        assertEquals(emptyList(), test.audit.recent())
    }

    @Test
    fun promotingTwiceRecordsOnce() {
        account("ana@example.com")
        assertTrue(promote("Ana@Example.com"))
        assertFalse(promote("ana@example.com"))
        assertEquals(1, test.audit.recent().size)
    }

    @Test
    fun promotingEmitsAnAdminActionWithoutTheAddress() {
        account("ana@example.com")
        val events = RecordingEvents()
        promote("ana@example.com", events)
        val action = events.single() as AdminAction
        assertEquals("admin.promote", action.action)
        assertNull(action.actor)
        assertFalse("ana@example.com" in action.toString())
    }

    @Test
    fun anEmailAddressPrintsMasked() {
        val email = EmailAddress.of("ana@example.com")
        assertEquals("a***@example.com", email.toString())
        assertEquals("ana@example.com", email.value)
    }

    @Test
    fun anInvalidAddressIsRefused() {
        assertNull(EmailAddress.parseOrNull("not an email"))
    }

    @Test
    fun theAuditLogKeepsEntriesWhenTheActorIsDeleted() {
        account("ana@example.com")
        promote("ana@example.com")
        val id = test.database.queries.accountQueries.forSignIn("ana@example.com").executeAsOne().id
        test.clock.now = test.clock.now.plusSeconds(60)
        test.audit.events(AdminAction(AccountId.of(id), "catalog.publish", "7"))
        assertEquals("ana@example.com", test.audit.recent().first().actorEmail)
        test.database.queries.accountQueries.delete(id).executeAsOne()
        val entries = test.audit.recent()
        assertEquals(2, entries.size)
        assertNull(entries.first().actorEmail)
    }
}
