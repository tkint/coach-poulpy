package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import java.time.Duration
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import org.http4k.core.Method.GET
import org.http4k.core.PolyHandler
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.sse.SseMessage
import org.http4k.testing.testSseClient
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfirmationEventsTest {
    private val test = TestServer()
    private val key = "k".repeat(43)
    private val events = confirmationEvents(test.accounts, pingEvery = Duration.ofMillis(20), lifetime = Duration.ofMillis(200))

    private fun listen(key: String = this.key) =
        events.testSseClient(Request(GET, "/api/v1/accounts/confirmation-events").header(PENDING_KEY_HEADER, key))

    private fun signUp(pendingKey: String? = key) =
        test.accounts.signUp(SignUp("ana@example.com", "correct horse", "en", healthDataConsent = true, pendingKey = pendingKey))

    private fun openLink(n: Int = 0) {
        val token = Regex("confirm-email#([A-Za-z0-9_-]+)").find(test.mailer.sent[n].text)!!.groupValues[1]
        test.http(Request(POST, "/api/v1/accounts/confirm").header("Content-Type", "application/json").body("""{"token":"$token"}"""))
    }

    private val confirmed = SseMessage.Event("confirmed", "{}")

    @Test
    fun thePhoneHearsTheMomentTheLinkIsOpened() {
        signUp()
        val stream = listen()
        assertEquals(OK, stream.status)

        openLink()

        assertEquals(listOf<SseMessage>(confirmed), stream.received().filterNot { it == SseMessage.Ping }.toList())
    }

    @Test
    fun aLinkOpenedBeforeTheStreamIsHeardAtOnce() {
        signUp()
        openLink()

        assertEquals(listOf<SseMessage>(confirmed), listen().received().toList())
    }

    @Test
    fun aKeyOnlyHearsTheLinkOfItsOwnSignUp() {
        signUp(pendingKey = null)
        signUp()
        val stream = listen()

        openLink(0)

        // Only pings, until the stream ends on its own.
        assertEquals(emptyList(), stream.received().filterNot { it == SseMessage.Ping }.toList())
    }

    @Test
    fun anIdleStreamIsPingedThenClosed() {
        val received = listen("u".repeat(43)).received().toList()

        assert(received.isNotEmpty() && received.all { it == SseMessage.Ping }) { received.toString() }
    }

    @Test
    fun aMissingOrShortKeyIsRefused() {
        assertEquals(BAD_REQUEST, events.testSseClient(Request(GET, "/api/v1/accounts/confirmation-events")).status)
        assertEquals(BAD_REQUEST, listen("short").status)
    }

    @Test
    fun overJettyTheStreamCarriesTheEventOnTheWire() {
        signUp()
        val server = PolyHandler(test.http, sse = events).asServer(Jetty(0)).start()
        try {
            val client = HttpClient.newHttpClient()
            val health = client.send(HttpRequest.newBuilder(URI("http://localhost:${server.port()}/health")).build(), BodyHandlers.ofString())
            assertEquals(200, health.statusCode())
            val stream = client.send(
                HttpRequest.newBuilder(URI("http://localhost:${server.port()}/api/v1/accounts/confirmation-events"))
                    .header("Accept", "text/event-stream").header(PENDING_KEY_HEADER, key).build(),
                BodyHandlers.ofLines(),
            )
            assertEquals("text/event-stream", stream.headers().firstValue("content-type").orElse("").substringBefore(';'))
            openLink()
            // The stream ends after the event.
            val lines = stream.body().toList().filter { it.isNotEmpty() && !it.startsWith(":") }
            assertEquals(listOf("event:confirmed", "data:{}"), lines)
        } finally {
            server.stop()
        }
    }
}
