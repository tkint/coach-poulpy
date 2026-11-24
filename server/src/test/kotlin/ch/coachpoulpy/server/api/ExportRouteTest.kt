package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.SignInResult
import java.util.zip.ZipInputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Export my data: one .zip with the account as JSON and each finished session as a .FIT file (#553). */
class ExportRouteTest {
    private val test = TestServer()
    private val ana = signedIn("ana@example.com")

    private fun signedIn(email: String): String {
        test.accounts.signUp(SignUp(email, "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
        return (test.signIns.signIn(email, "correct horse", "Phone") as SignInResult.Signed).tokens.accessToken
    }

    private fun push(vararg changes: String, token: String = ana) = test.http(
        Request(POST, "/api/v1/sync/push").header("Authorization", "Bearer $token").header("Content-Type", "application/json")
            .body("""{"changes":[${changes.joinToString(",")}]}"""),
    ).also { assertEquals(OK, it.status, it.bodyString()) }

    private fun export(token: String? = ana): Response = test.http(
        Request(GET, "/api/v1/me/export").let { if (token == null) it else it.header("Authorization", "Bearer $token") },
    )

    /** A free run on a treadmill: started, two samples, finished. */
    private fun run(id: String, minute: Int) = arrayOf(
        """{"type":"session","id":"$id","source":"DEVICE_ONLY","startedAt":"2026-11-07T07:${minute}0:00Z","endedAt":"2026-11-07T07:${minute}5:00Z"}""",
        """{"type":"session-events","sessionId":"$id","events":[""" +
            """{"seq":1,"at":"2026-11-07T07:${minute}0:00Z","payload":{"type":"started","at":"2026-11-07T07:${minute}0:00Z"}},""" +
            """{"seq":2,"at":"2026-11-07T07:${minute}1:00Z","payload":{"type":"sample","at":"2026-11-07T07:${minute}1:00Z","metrics":{"speedKmh":8.0,"distanceMeters":130.0}}},""" +
            """{"seq":3,"at":"2026-11-07T07:${minute}2:00Z","payload":{"type":"sample","at":"2026-11-07T07:${minute}2:00Z","metrics":{"speedKmh":8.0,"distanceMeters":270.0}}},""" +
            """{"seq":4,"at":"2026-11-07T07:${minute}5:00Z","payload":{"type":"finished","at":"2026-11-07T07:${minute}5:00Z"}}]}""",
    )

    private fun Response.entries(): Map<String, ByteArray> = ZipInputStream(body.stream).use { zip ->
        generateSequence { zip.nextEntry }.associate { it.name to zip.readBytes() }
    }

    @Test
    fun theZipHoldsTheAccountAsJsonAndEachFinishedSessionAsAFitFile() {
        push(*run(FIRST, 1), *run(SECOND, 3))
        val response = export()
        assertEquals(OK, response.status)
        assertEquals("application/zip", response.header("Content-Type"))
        assertTrue(response.header("Content-Disposition")!!.contains(".zip"))
        val entries = response.entries()
        val json = Json.parseToJsonElement(entries.getValue("coach-poulpy.json").decodeToString()).jsonObject
        assertEquals("coach-poulpy-export", json.getValue("format").jsonPrimitive.content)
        assertEquals(2, json.getValue("changes").jsonArray.count { it.jsonObject.getValue("type").jsonPrimitive.content == "session" })
        val fits = entries.filterKeys { it.startsWith("sessions/") }
        assertEquals(setOf("sessions/coach-poulpy-2026-11-07-0710.fit", "sessions/coach-poulpy-2026-11-07-0730.fit"), fits.keys)
        fits.values.forEach { assertEquals(".FIT", it.copyOfRange(8, 12).decodeToString()) }
    }

    @Test
    fun aDeletedSessionIsNotExported() {
        push(*run(FIRST, 1), """{"type":"session-deleted","sessionId":"$FIRST","deletedAt":"2026-11-07T09:00:00Z"}""")
        assertEquals(emptySet(), export().entries().keys.filter { it.startsWith("sessions/") }.toSet())
    }

    @Test
    fun anotherAccountsSessionsStayOut() {
        push(*run(FIRST, 1))
        val ben = signedIn("ben@example.com")
        assertEquals(emptySet(), export(ben).entries().keys.filter { it.startsWith("sessions/") }.toSet())
    }

    @Test
    fun exportingNeedsASignIn() {
        assertEquals(UNAUTHORIZED, export(token = null).status)
    }

    private companion object {
        const val FIRST = "0190d4d2-0000-7000-8000-0000000000a1"
        const val SECOND = "0190d4d2-0000-7000-8000-0000000000a2"
    }
}
