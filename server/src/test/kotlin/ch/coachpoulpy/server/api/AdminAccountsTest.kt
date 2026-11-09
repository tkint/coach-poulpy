package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.admin.EmailAddress
import ch.coachpoulpy.server.admin.promoteFirstAdmin
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdminAccountsTest {
    private val test = TestServer()
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

    private fun idOf(email: String) = test.database.queries.accountQueries.forSignIn(email).executeAsOne().id

    private fun call(request: Request, token: String? = adminToken): Response =
        test.http(if (token == null) request else request.header("Authorization", "Bearer $token"))

    private fun list(vararg params: Pair<String, String>, token: String? = adminToken): Response =
        call(params.fold(Request(GET, "/api/v1/admin/accounts")) { request, (name, value) -> request.query(name, value) }, token)

    private fun find(email: String, token: String? = adminToken) = list("q" to email, token = token)

    private fun body(response: Response) = Json.parseToJsonElement(response.bodyString()).jsonObject

    private fun emails(response: Response) = body(response).getValue("accounts").jsonArray.map { it.jsonObject.getValue("email").jsonPrimitive.content }

    private fun total(response: Response) = body(response).getValue("total").jsonPrimitive.int

    /** The account with exactly [email], as the list shows it. */
    private fun shown(email: String) = body(find(email)).getValue("accounts").jsonArray.single().jsonObject

    private fun disable(email: String) = call(Request(POST, "/api/v1/admin/accounts/${idOf(email)}/disable"))

    private fun enable(email: String) = call(Request(POST, "/api/v1/admin/accounts/${idOf(email)}/enable"))

    private fun delete(email: String, confirm: String) =
        call(Request(DELETE, "/api/v1/admin/accounts/${idOf(email)}").header("content-type", "application/json").body("""{"confirmEmail":"$confirm"}"""))

    @Test
    fun theListShowsEachAccountsStateAndNothingElse() {
        account("lea@example.com")
        val response = find("LEA@example")
        assertEquals(OK, response.status)
        assertEquals(1, total(response))
        val body = shown("lea@example.com")
        assertEquals(setOf("id", "email", "role", "confirmed", "createdAt", "disabledAt"), body.keys)
        assertEquals("lea@example.com", body.getValue("email").jsonPrimitive.content)
        assertEquals("member", body.getValue("role").jsonPrimitive.content)
        assertEquals("true", body.getValue("confirmed").jsonPrimitive.content)
        assertEquals(JsonNull, body.getValue("disabledAt"))
    }

    @Test
    fun oneAccountIsOpenedByItsId() {
        account("lea@example.com")
        val response = call(Request(GET, "/api/v1/admin/accounts/${idOf("lea@example.com")}"))
        assertEquals(OK, response.status)
        assertEquals("lea@example.com", body(response).getValue("email").jsonPrimitive.content)
        assertEquals(NOT_FOUND, call(Request(GET, "/api/v1/admin/accounts/00000000-0000-7000-8000-000000000000")).status)
        assertEquals(FORBIDDEN, call(Request(GET, "/api/v1/admin/accounts/${idOf("lea@example.com")}"), signIn("lea@example.com", Client.Website).tokens.accessToken).status)
    }

    @Test
    fun aSearchWithNoMatchIsAnEmptyPage() {
        val response = find("nobody@example.com")
        assertEquals(OK, response.status)
        assertEquals(0, total(response))
        assertEquals(emptyList(), emails(response))
    }

    @Test
    fun filtersNarrowTheListAndTheTotalFollows() {
        account("lea@example.com")
        account("tom@example.com")
        disable("tom@example.com")
        assertEquals(listOf("ana@example.com"), emails(list("role" to "admin")))
        assertEquals(listOf("tom@example.com"), emails(list("signIn" to "disabled")))
        assertEquals(setOf("ana@example.com", "lea@example.com"), emails(list("signIn" to "active")).toSet())
        assertEquals(3, total(list("confirmed" to "yes")))
        assertEquals(0, total(list("confirmed" to "no")))
        assertEquals(listOf("lea@example.com"), emails(list("q" to "EA@", "signIn" to "active", "role" to "member")))
    }

    @Test
    fun theListSortsOnAColumnInEitherDirection() {
        account("zoe@example.com")
        account("lea@example.com")
        assertEquals(listOf("ana@example.com", "lea@example.com", "zoe@example.com"), emails(list("sort" to "email", "order" to "asc")))
        assertEquals(listOf("zoe@example.com", "lea@example.com", "ana@example.com"), emails(list("sort" to "email", "order" to "desc")))
        assertEquals("ana@example.com", emails(list("sort" to "role", "order" to "asc")).first())
    }

    @Test
    fun theListIsPaged() {
        listOf("a", "b", "c", "d", "e").forEach { account("$it@example.com") }
        val first = list("sort" to "email", "order" to "asc", "size" to "4", "page" to "1")
        val second = list("sort" to "email", "order" to "asc", "size" to "4", "page" to "2")
        assertEquals(6, total(first))
        assertEquals(listOf("a@example.com", "ana@example.com", "b@example.com", "c@example.com"), emails(first))
        assertEquals(listOf("d@example.com", "e@example.com"), emails(second))
        assertEquals(emptyList(), emails(list("size" to "4", "page" to "3")))
    }

    @Test
    fun anUnknownFilterOrSortWordIs400() {
        assertEquals(BAD_REQUEST, list("role" to "boss").status)
        assertEquals(BAD_REQUEST, list("sort" to "password").status)
        assertEquals(BAD_REQUEST, list("order" to "sideways").status)
    }

    @Test
    fun onlyAnAdminOnTheWebsiteMayUseIt() {
        account("lea@example.com")
        assertEquals(UNAUTHORIZED, find("lea@example.com", token = null).status)
        assertEquals(FORBIDDEN, find("ana@example.com", token = signIn("lea@example.com", Client.Website).tokens.accessToken).status)
        assertEquals(FORBIDDEN, find("lea@example.com", token = signIn("ana@example.com", Client.App).tokens.accessToken).status)
    }

    @Test
    fun aDisabledAccountCannotSignInOrRefresh() {
        account("lea@example.com")
        val before = signIn("lea@example.com")
        assertEquals(NO_CONTENT, disable("lea@example.com").status)

        assertEquals(SignInResult.Refused, test.signIns.signIn("lea@example.com", "correct horse", "A device"))
        assertNull(test.signIns.refresh(before.tokens.refreshToken))
        assertEquals(UNAUTHORIZED, call(Request(GET, "/api/v1/me"), before.tokens.accessToken).status)
        assertNotNull(shown("lea@example.com").getValue("disabledAt").takeIf { it != JsonNull })
    }

    @Test
    fun enablingLetsTheAccountSignInAgain() {
        account("lea@example.com")
        disable("lea@example.com")
        assertEquals(NO_CONTENT, enable("lea@example.com").status)
        assertTrue(test.signIns.signIn("lea@example.com", "correct horse", "A device") is SignInResult.Signed)
        assertEquals(JsonNull, shown("lea@example.com").getValue("disabledAt"))
    }

    @Test
    fun anAdminCannotDisableOrDeleteTheirOwnAccount() {
        assertEquals(CONFLICT, disable("ana@example.com").status)
        assertEquals(CONFLICT, delete("ana@example.com", "ana@example.com").status)
        assertTrue(test.signIns.signIn("ana@example.com", "correct horse", "A browser", Client.Website) is SignInResult.Signed)
    }

    @Test
    fun anUnknownAccountIs404() {
        val unknown = "00000000-0000-7000-8000-000000000000"
        assertEquals(NOT_FOUND, call(Request(POST, "/api/v1/admin/accounts/$unknown/disable")).status)
        assertEquals(NOT_FOUND, call(Request(POST, "/api/v1/admin/accounts/$unknown/enable")).status)
    }

    @Test
    fun deletingNeedsTheEmailTypedAgain() {
        account("lea@example.com")
        assertEquals(BAD_REQUEST, delete("lea@example.com", "lea@exam").status)
        assertEquals(1, total(find("lea@example.com")))
    }

    @Test
    fun deletingRemovesTheAccountAndItsRows() {
        account("lea@example.com")
        val id = idOf("lea@example.com")
        val signedIn = signIn("lea@example.com")
        assertEquals(NO_CONTENT, delete("lea@example.com", " Lea@Example.com ").status)

        assertEquals(0, total(find("lea@example.com")))
        assertNull(test.database.queries.accountQueries.byId(id).executeAsOneOrNull())
        assertNull(test.database.queries.signInQueries.liveCaller(ch.coachpoulpy.server.auth.Secrets.hashOf(signedIn.tokens.accessToken), java.time.OffsetDateTime.now(test.clock)).executeAsOneOrNull())
        assertFalse(test.signIns.signIn("lea@example.com", "correct horse", "A device") is SignInResult.Signed)
    }

    @Test
    fun everyChangeIsWrittenToTheAuditLogWithoutTheAddress() {
        account("lea@example.com")
        val id = idOf("lea@example.com")
        disable("lea@example.com")
        test.clock.now = test.clock.now.plusSeconds(1)
        enable("lea@example.com")
        test.clock.now = test.clock.now.plusSeconds(1)
        delete("lea@example.com", "lea@example.com")

        val entries = test.audit.recent().filter { it.action.startsWith("admin.account.") }
        assertEquals(listOf("admin.account.delete", "admin.account.enable", "admin.account.disable"), entries.map { it.action })
        assertTrue(entries.all { it.target == id.toString() && it.actorEmail == "ana@example.com" })
    }
}
