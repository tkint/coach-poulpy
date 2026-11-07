package ch.coachpoulpy.server

import ch.coachpoulpy.catalog.Catalog
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {
    private val test = TestServer()

    @Test
    fun healthReportsTheVersion() {
        val response = app("0.6.0", Catalog.bundled, test.database, test.accounts, test.signIns, databaseAnswers = { true })(Request(GET, "/health"))
        assertEquals(OK, response.status)
        assertEquals("ok 0.6.0", response.bodyString())
    }

    @Test
    fun healthIsUnavailableWhileTheDatabaseIsDown() {
        val response = app("0.6.0", Catalog.bundled, test.database, test.accounts, test.signIns, databaseAnswers = { false })(Request(GET, "/health"))
        assertEquals(SERVICE_UNAVAILABLE, response.status)
    }

    @Test
    fun versionComesFromAppVersion() {
        assertEquals("0.6.0", appVersion { if (it == "APP_VERSION") "0.6.0" else null })
    }

    @Test
    fun versionIsDevWhenUnset() {
        assertEquals("dev", appVersion { null })
        assertEquals("dev", appVersion { " " })
    }
}
