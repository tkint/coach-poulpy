package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import kotlin.test.Test
import kotlin.test.assertEquals

/** Changing the password while signed in, and the list of signed-in devices (#553). */
class AccountSecurityRoutesTest {
    private val test = TestServer()

    init {
        test.accounts.signUp(SignUp("ana@example.com", "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.single().text.substringAfter("#").substringBefore("\n")))
        test.mailer.sent.clear()
    }

    private fun signIn(device: String, password: String = "correct horse"): Response = test.http(
        Request(POST, "/oauth/token").header("Content-Type", "application/x-www-form-urlencoded")
            .form("grant_type", "password").form("username", "ana@example.com").form("password", password).form("device_name", device),
    )

    private fun token(device: String, password: String = "correct horse"): String =
        Json.parseToJsonElement(signIn(device, password).bodyString()).jsonObject.getValue("access_token").jsonPrimitive.content

    private fun call(method: Method, path: String, token: String, body: String? = null): Response = test.http(
        Request(method, path).header("Authorization", "Bearer $token")
            .let { if (body == null) it else it.header("Content-Type", "application/json").body(body) },
    )

    private fun me(token: String) = call(GET, "/api/v1/me", token)

    private fun changePassword(token: String, current: String = "correct horse", new: String = "purple octopus") =
        call(POST, "/api/v1/me/password", token, """{"currentPassword":"$current","newPassword":"$new"}""")

    private fun devices(token: String): List<JsonObject> =
        Json.parseToJsonElement(call(GET, "/api/v1/me/devices", token).bodyString()).jsonObject.getValue("devices").jsonArray.map { it.jsonObject }

    private fun Response.error(): String = Json.parseToJsonElement(bodyString()).jsonObject.getValue("error").jsonPrimitive.content

    @Test
    fun changingThePasswordKeepsThisDeviceAndSignsTheOthersOut() {
        val phone = token("Pixel 8")
        val other = token("Ana's iPhone")
        assertEquals(NO_CONTENT, changePassword(phone).status)
        assertEquals(OK, me(phone).status)
        assertEquals(UNAUTHORIZED, me(other).status)
        assertEquals(BAD_REQUEST, signIn("Ana's iPhone").status)
        assertEquals(OK, signIn("Ana's iPhone", "purple octopus").status)
    }

    @Test
    fun aWrongCurrentPasswordChangesNothing() {
        val phone = token("Pixel 8")
        val other = token("Ana's iPhone")
        val refused = changePassword(phone, current = "wrong guess")
        assertEquals(FORBIDDEN, refused.status)
        assertEquals("wrong-password", refused.error())
        assertEquals(OK, me(other).status)
        assertEquals(OK, signIn("Laptop").status)
    }

    @Test
    fun aNewPasswordTooShortIsRefused() {
        val phone = token("Pixel 8")
        val refused = changePassword(phone, new = "short")
        assertEquals(BAD_REQUEST, refused.status)
        assertEquals("invalid-password", refused.error())
        assertEquals(OK, signIn("Laptop").status)
    }

    @Test
    fun theDevicesListMarksTheCallerAndLeavesSignedOutOnesOut() {
        val phone = token("Pixel 8")
        val other = token("Ana's iPhone")
        val listed = devices(phone)
        assertEquals(setOf("Pixel 8", "Ana's iPhone"), listed.map { it.getValue("name").jsonPrimitive.content }.toSet())
        assertEquals("Pixel 8", listed.single { it.getValue("current").jsonPrimitive.boolean }.getValue("name").jsonPrimitive.content)
        call(DELETE, "/api/v1/me/devices", other)
        assertEquals(listOf("Ana's iPhone"), devices(other).map { it.getValue("name").jsonPrimitive.content })
    }

    @Test
    fun signingOneDeviceOutStopsItsTokensOnly() {
        val phone = token("Pixel 8")
        val other = token("Ana's iPhone")
        val id = devices(phone).single { it.getValue("name").jsonPrimitive.content == "Ana's iPhone" }.getValue("id").jsonPrimitive.content
        assertEquals(NO_CONTENT, call(DELETE, "/api/v1/me/devices/$id", phone).status)
        assertEquals(UNAUTHORIZED, me(other).status)
        assertEquals(OK, me(phone).status)
        assertEquals(NOT_FOUND, call(DELETE, "/api/v1/me/devices/$id", phone).status)
    }

    @Test
    fun anotherAccountsDeviceCannotBeSignedOut() {
        test.accounts.signUp(SignUp("ben@example.com", "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.single().text.substringAfter("#").substringBefore("\n")))
        val ben = Json.parseToJsonElement(
            test.http(
                Request(POST, "/oauth/token").header("Content-Type", "application/x-www-form-urlencoded")
                    .form("grant_type", "password").form("username", "ben@example.com").form("password", "correct horse").form("device_name", "Ben's phone"),
            ).bodyString(),
        ).jsonObject.getValue("access_token").jsonPrimitive.content
        val anaPhone = token("Pixel 8")
        val anaId = devices(anaPhone).single().getValue("id").jsonPrimitive.content
        assertEquals(NOT_FOUND, call(DELETE, "/api/v1/me/devices/$anaId", ben).status)
        assertEquals(OK, me(anaPhone).status)
    }

    @Test
    fun signingEveryOtherDeviceOutKeepsTheCaller() {
        val phone = token("Pixel 8")
        val other = token("Ana's iPhone")
        val third = token("Laptop")
        assertEquals(NO_CONTENT, call(DELETE, "/api/v1/me/devices", phone).status)
        assertEquals(OK, me(phone).status)
        assertEquals(UNAUTHORIZED, me(other).status)
        assertEquals(UNAUTHORIZED, me(third).status)
    }
}
