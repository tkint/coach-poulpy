package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.accounts.PasswordResets
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.admin.EmailAddress
import ch.coachpoulpy.server.admin.promoteFirstAdmin
import ch.coachpoulpy.server.app
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import ch.coachpoulpy.server.mail.NoMailServer
import ch.coachpoulpy.server.mail.SettingsMailer
import jakarta.mail.AuthenticationFailedException
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import org.http4k.core.Status.Companion.UNAUTHORIZED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The public status page and the admin's status view (#340). */
class StatusTest {
    private val test = TestServer()

    /** The same server with no SMTP settings: every email goes to [NoMailServer]. */
    private val noMail = SettingsMailer(NoMailServer, test.settings::mailFrom, test.clock)
    private val noMailAccounts = Accounts(test.database, noMail, "https://app.example.com", test.passwords, test.clock)
    private val noMailResets = PasswordResets(test.database, noMail, "https://app.example.com", test.signIns, test.passwords, test.clock)
    private val noMailHttp = app(
        "0.7.0", test.catalog, test.database, noMailAccounts, test.signIns, noMailResets, test.accountDeletion, test.sync, test.audit,
        settings = test.settings, mailer = noMail,
    )
    private val adminToken: String

    init {
        confirmed("ana@example.com")
        promoteFirstAdmin(test.database, test.audit.events, EmailAddress.of("ana@example.com"))
        adminToken = signIn("ana@example.com", Client.Website)
    }

    /** An account confirmed through the link the fake mailer caught. */
    private fun confirmed(email: String) {
        test.accounts.signUp(SignUp(email, "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
    }

    private fun signIn(email: String, client: Client = Client.App) =
        (test.signIns.signIn(email, "correct horse", "A device", client) as SignInResult.Signed).tokens.accessToken

    private fun call(http: HttpHandler, path: String, token: String? = adminToken): Response =
        http(Request(GET, path).let { if (token == null) it else it.header("Authorization", "Bearer $token") })

    private fun json(response: Response): JsonObject = Json.parseToJsonElement(response.bodyString()).jsonObject

    private fun mail(http: HttpHandler = test.http) = json(call(http, "/api/v1/admin/status")).getValue("mail").jsonObject

    /** Signs in again first: the tests move the clock past the access token's 15 minutes. */
    private fun pending() =
        json(call(test.http, "/api/v1/admin/pending-registrations", signIn("ana@example.com", Client.Website))).getValue("registrations").jsonArray.map { it.jsonObject }

    private fun at(text: String) = Instant.parse(text)

    @Test
    fun thePublicStatusSaysUpAndNothingElse() {
        listOf(test.http, noMailHttp).forEach { http ->
            val response = call(http, "/api/v1/status", token = null)
            assertEquals(OK, response.status)
            assertEquals("""{"status":"up"}""", response.bodyString())
        }
    }

    @Test
    fun thePublicStatusExposesNoConfiguration() {
        val body = call(noMailHttp, "/api/v1/status", token = null).bodyString().lowercase()
        listOf("mail", "smtp", "configured", "0.7.0", "version", "database", "hello@coachpoulpy.example").forEach {
            assertFalse(it in body, "the public status says $it")
        }
        assertEquals(setOf("status"), json(call(test.http, "/api/v1/status", token = null)).keys)
    }

    @Test
    fun thePublicStatusSaysDownWhileTheDatabaseDoesNotAnswer() {
        val http = app(
            "0.7.0", test.catalog, test.database, test.accounts, test.signIns, test.passwordResets, test.accountDeletion, test.sync, test.audit,
            databaseAnswers = { false },
        )
        val response = call(http, "/api/v1/status", token = null)
        assertEquals(SERVICE_UNAVAILABLE, response.status)
        assertEquals("""{"status":"down"}""", response.bodyString())
    }

    @Test
    fun theAdminStatusSaysMailIsNotConfigured() {
        assertEquals("false", mail(noMailHttp).getValue("configured").jsonPrimitive.content)
        noMailAccounts.signUp(SignUp("lea@example.com", "correct horse", "en", healthDataConsent = true))
        val last = mail(noMailHttp).getValue("lastSend").jsonObject
        assertEquals("confirmation", last.getValue("kind").jsonPrimitive.content)
        assertEquals("false", last.getValue("sent").jsonPrimitive.content)
        assertTrue("SMTP_HOST" in last.getValue("error").jsonPrimitive.content)
    }

    @Test
    fun theAdminStatusSaysMailIsConfiguredAndHowTheLastSendWent() {
        val fresh = TestServer()
        fresh.accounts.signUp(SignUp("ana@example.com", "correct horse", "en", healthDataConsent = true))
        fresh.accounts.confirm(fresh.mailer.sent.last().text.substringAfter("#").substringBefore("\n"))
        promoteFirstAdmin(fresh.database, fresh.audit.events, EmailAddress.of("ana@example.com"))
        val token = (fresh.signIns.signIn("ana@example.com", "correct horse", "A device", Client.Website) as SignInResult.Signed).tokens.accessToken
        val status = { json(call(fresh.http, "/api/v1/admin/status", token)).getValue("mail").jsonObject }

        assertEquals("true", status().getValue("configured").jsonPrimitive.content)
        val sent = status().getValue("lastSend").jsonObject
        assertEquals("confirmation", sent.getValue("kind").jsonPrimitive.content)
        assertEquals("true", sent.getValue("sent").jsonPrimitive.content)
        assertEquals(JsonNull, sent.getValue("error"))
        assertEquals(fresh.clock.now, at(sent.getValue("at").jsonPrimitive.content))

        fresh.mailer.failure = AuthenticationFailedException("535 Authentication failed")
        fresh.http(Request(POST, "/api/v1/admin/settings/test-email").header("Authorization", "Bearer $token"))
        val failed = status().getValue("lastSend").jsonObject
        assertEquals("test", failed.getValue("kind").jsonPrimitive.content)
        assertEquals("false", failed.getValue("sent").jsonPrimitive.content)
        assertEquals("535 Authentication failed", failed.getValue("error").jsonPrimitive.content)
    }

    @Test
    fun theAdminStatusNeverShowsTheMailServer() {
        val body = call(test.http, "/api/v1/admin/status").bodyString().lowercase()
        listOf("host", "port", "password", "user", "hello@coachpoulpy.example").forEach { assertFalse(it in body, "the status says $it") }
    }

    @Test
    fun theAdminListsPendingRegistrationsNewestFirst() {
        val start = test.clock.now
        test.accounts.signUp(SignUp("tom@example.com", "correct horse", "en", healthDataConsent = true))
        test.clock.now = start.plus(Duration.ofHours(2))
        test.accounts.signUp(SignUp("lea@example.com", "correct horse", "fr", healthDataConsent = true))
        // Signing up again sends a newer link: the list shows when that one expires.
        test.clock.now = start.plus(Duration.ofHours(3))
        test.accounts.signUp(SignUp("tom@example.com", "correct horse", "en", healthDataConsent = true))

        val rows = pending()
        assertEquals(listOf("lea@example.com", "tom@example.com"), rows.map { it.getValue("email").jsonPrimitive.content })
        val tom = rows[1]
        assertEquals(start, at(tom.getValue("signedUpAt").jsonPrimitive.content))
        assertEquals(start.plus(Duration.ofHours(27)), at(tom.getValue("linkExpiresAt").jsonPrimitive.content))
        assertEquals(start.plus(Duration.ofHours(26)), at(rows[0].getValue("linkExpiresAt").jsonPrimitive.content))
    }

    @Test
    fun aConfirmedAccountLeavesThePendingRegistrations() {
        test.accounts.signUp(SignUp("lea@example.com", "correct horse", "en", healthDataConsent = true))
        assertEquals(listOf("lea@example.com"), pending().map { it.getValue("email").jsonPrimitive.content })
        test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n"))
        assertEquals(emptyList(), pending())
    }

    @Test
    fun aPendingRegistrationWithNoLinkLeftHasNoExpiry() {
        test.accounts.signUp(SignUp("lea@example.com", "correct horse", "en", healthDataConsent = true))
        val id = test.database.queries.accountQueries.byEmail("lea@example.com").executeAsOne().id
        test.database.queries.confirmationTokenQueries.deleteForAccount(id)
        assertEquals(JsonNull, pending().single().getValue("linkExpiresAt"))
    }

    @Test
    fun onlyAnAdminOnTheWebsiteSeesTheStatus() {
        confirmed("lea@example.com")
        val member = signIn("lea@example.com", Client.Website)
        listOf("/api/v1/admin/status", "/api/v1/admin/pending-registrations").forEach {
            assertEquals(FORBIDDEN, call(test.http, it, member).status, it)
            assertEquals(FORBIDDEN, call(test.http, it, signIn("ana@example.com")).status, it)
            assertEquals(UNAUTHORIZED, call(test.http, it, token = null).status, it)
        }
    }

    @Test
    fun noEmailIsPrintedToTheLogsWithoutAMailServer() {
        val output = ByteArrayOutputStream()
        val (out, err) = System.out to System.err
        System.setOut(PrintStream(output, true, Charsets.UTF_8))
        System.setErr(PrintStream(output, true, Charsets.UTF_8))
        try {
            noMailAccounts.signUp(SignUp("lea@example.com", "correct horse", "fr", healthDataConsent = true))
            val lea = test.database.queries.accountQueries.byEmail("lea@example.com").executeAsOne().id
            test.database.queries.accountQueries.confirm(OffsetDateTime.ofInstant(test.clock.now, ZoneOffset.UTC), lea)
            noMailResets.request("lea@example.com")
            noMailHttp(Request(POST, "/api/v1/admin/settings/test-email").header("Authorization", "Bearer $adminToken"))
        } finally {
            System.setOut(out)
            System.setErr(err)
        }
        val printed = output.toString(Charsets.UTF_8)
        listOf("lea@example.com", "ana@example.com", "confirm-email", "reset-password", "https://app.example.com", "Coach Poulpy", "Bonjour", "Hello").forEach {
            assertFalse(it in printed, "the log holds $it:\n$printed")
        }
        assertEquals("test", mail(noMailHttp).getValue("lastSend").jsonObject.getValue("kind").jsonPrimitive.content)
    }
}
