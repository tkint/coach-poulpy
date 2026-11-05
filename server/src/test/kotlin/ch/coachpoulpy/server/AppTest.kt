package ch.coachpoulpy.server

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {
    @Test
    fun healthAnswersOk() {
        assertEquals(OK, app()(Request(GET, "/health")).status)
    }

    @Test
    fun healthReportsTheVersion() {
        assertEquals("ok 0.6.0", app("0.6.0")(Request(GET, "/health")).bodyString())
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
