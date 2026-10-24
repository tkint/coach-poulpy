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
}
