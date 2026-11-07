package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.app
import ch.coachpoulpy.server.mail.Email
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NO_CONTENT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountRoutesTest {
    private val test = TestServer()

    private fun signUp(
        email: String = "Guest@Example.com",
        password: String = "correct horse",
        language: String = "fr-CH",
        consent: Boolean = true,
    ): Response = test.http(
        Request(POST, "/api/v1/accounts").header("Content-Type", "application/json")
            .body("""{"email":"$email","password":"$password","language":"$language","healthDataConsent":$consent}"""),
    )

    private fun confirm(token: String): Response = test.http(
        Request(POST, "/api/v1/accounts/confirm").header("Content-Type", "application/json").body("""{"token":"$token"}"""),
    )

    private fun Email.token(): String = Regex("https://app\\.example\\.com/confirm-email#([A-Za-z0-9_-]+)").find(text)!!.groupValues[1]

    private fun Response.error(): String = Json.parseToJsonElement(bodyString()).jsonObject.getValue("error").jsonPrimitive.content

    private fun account(email: String) = test.database.queries.accountQueries.byEmail(email).executeAsOneOrNull()

    @Test
    fun signingUpMailsAConfirmationLinkInTheAccountsLanguage() {
        assertEquals(ACCEPTED, signUp().status)
        val email = test.mailer.sent.single()
        assertEquals("Guest@Example.com", email.to)
        assertTrue(email.subject.startsWith("Confirmez"), email.subject)
        assertEquals(43, email.token().length)
        val account = assertNotNull(account("guest@example.com"))
        assertEquals("fr", account.language)
        assertNull(account.confirmed_at)
    }

    @Test
    fun aLanguageWithNoTranslationGetsEnglish() {
        signUp(language = "de")
        assertTrue(test.mailer.sent.single().subject.startsWith("Confirm your email"))
    }

    @Test
    fun onlyTheHashOfThePasswordIsStored() {
        signUp()
        val stored = test.database.queries.accountQueries.passwordHash("guest@example.com").executeAsOne()
        assertTrue(stored.startsWith("\$argon2id\$"))
        assertTrue(test.passwords.verify("correct horse", stored))
    }

    @Test
    fun theLinkConfirmsTheEmailOnce() {
        signUp()
        val token = test.mailer.sent.single().token()
        assertEquals(NO_CONTENT, confirm(token).status)
        assertNotNull(account("guest@example.com")?.confirmed_at)
        val again = confirm(token)
        assertEquals(BAD_REQUEST, again.status)
        assertEquals("invalid-token", again.error())
    }

    @Test
    fun aLinkExpiresAfterADay() {
        signUp()
        val token = test.mailer.sent.single().token()
        test.clock.now = test.clock.now.plus(Duration.ofHours(24))
        assertEquals(BAD_REQUEST, confirm(token).status)
        assertNull(account("guest@example.com")?.confirmed_at)
    }

    @Test
    fun anUnknownTokenIsRefused() {
        assertEquals("invalid-token", confirm("not-a-token").error())
    }

    @Test
    fun aTakenAddressGetsTheSameAnswerAndANewLinkUntilConfirmed() {
        signUp()
        assertEquals(ACCEPTED, signUp(email = "guest@EXAMPLE.com", password = "another password", language = "en").status)
        assertEquals(2, test.mailer.sent.size)
        // The account keeps its first language and password, and either link confirms it.
        assertTrue(test.mailer.sent[1].subject.startsWith("Confirmez"))
        assertEquals("Guest@Example.com", test.mailer.sent[1].to)
        assertEquals(NO_CONTENT, confirm(test.mailer.sent[0].token()).status)
        // The other link no longer serves.
        assertEquals(BAD_REQUEST, confirm(test.mailer.sent[1].token()).status)
    }

    @Test
    fun aConfirmedAddressGetsTheSameAnswerAndNoEmail() {
        signUp()
        confirm(test.mailer.sent.single().token())
        assertEquals(ACCEPTED, signUp().status)
        assertEquals(1, test.mailer.sent.size)
    }

    @Test
    fun aFailingMailerStillAnswersTheSame() {
        val accounts = Accounts(test.database, { error("SMTP down") }, "https://app.example.com", test.passwords, test.clock)
        val http = app("0.7.0", test.catalog, test.database, accounts, test.signIns, test.passwordResets, test.accountDeletion)
        val response = http(
            Request(POST, "/api/v1/accounts").header("Content-Type", "application/json")
                .body("""{"email":"guest@example.com","password":"correct horse","healthDataConsent":true}"""),
        )
        assertEquals(ACCEPTED, response.status)
    }

    @Test
    fun aSignUpBreakingARuleIsRefusedWithItsCode() {
        assertEquals("invalid-email", signUp(email = "guest").error())
        assertEquals("invalid-email", signUp(email = "guest@example").error())
        assertEquals("invalid-password", signUp(password = "short").error())
        assertEquals("consent-required", signUp(consent = false).error())
        assertEquals(emptyList(), test.mailer.sent)
    }

    @Test
    fun aMalformedBodyIsABadRequest() {
        val response = test.http(Request(POST, "/api/v1/accounts").header("Content-Type", "application/json").body("""{"email":"guest@example.com"}"""))
        assertEquals(BAD_REQUEST, response.status)
    }
}
