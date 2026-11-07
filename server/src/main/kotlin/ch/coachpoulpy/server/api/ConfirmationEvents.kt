package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.accounts.Accounts
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.routing.sse.bind
import org.http4k.routing.sse
import org.http4k.sse.SseHandler
import org.http4k.sse.SseMessage
import org.http4k.sse.SseResponse

/** The header the phone sends its pending key in, so the key stays out of URLs and access logs. */
const val PENDING_KEY_HEADER = "Pending-Key"

/** How long a stream stays open before the server closes it and the phone reconnects. */
val STREAM_LIFETIME: Duration = Duration.ofMinutes(10)

/** How often an idle stream gets a comment, so proxies and phones don't drop it as dead. */
val PING_EVERY: Duration = Duration.ofSeconds(20)

private val timers = Executors.newSingleThreadScheduledExecutor { Thread(it, "confirmation-events").apply { isDaemon = true } }

/**
 * `GET /api/v1/accounts/confirmation-events` with the [PENDING_KEY_HEADER] sent at sign-up: a
 * server-sent event stream that sends one `confirmed` event as soon as the link mailed in that
 * sign-up is opened, on any device, then closes (#319). A link already opened gets the event at once.
 * Otherwise the stream only carries pings and closes after [STREAM_LIFETIME]. A key never tells who has
 * an account: one that is unknown or was sent for an address already confirmed just never hears.
 */
fun confirmationEvents(accounts: Accounts, pingEvery: Duration = PING_EVERY, lifetime: Duration = STREAM_LIFETIME): SseHandler =
    sse("$API_PREFIX/accounts/confirmation-events" bind sse(GET to { request ->
        val key = request.header(PENDING_KEY_HEADER)
        if (key == null || key.length !in PENDING_KEY_LENGTH) {
            SseResponse(BAD_REQUEST, emptyList()) { it.close() }
        } else {
            SseResponse { sse ->
                val done = AtomicBoolean()
                val ping = timers.scheduleAtFixedRate(
                    { runCatching { sse.send(SseMessage.Ping) } }, pingEvery.toMillis(), pingEvery.toMillis(), MILLISECONDS,
                )
                val end = timers.schedule({ sse.close() }, lifetime.toMillis(), MILLISECONDS)
                var listening: AutoCloseable? = null
                sse.onClose {
                    ping.cancel(false)
                    end.cancel(false)
                    listening?.close()
                }
                listening = accounts.listen(key) {
                    if (done.compareAndSet(false, true)) {
                        runCatching { sse.send(SseMessage.Event("confirmed", "{}")) }
                        sse.close()
                    }
                }
            }
        }
    }))
