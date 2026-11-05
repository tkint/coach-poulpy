package ch.coachpoulpy.server

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer

/**
 * The release this server was built from: `APP_VERSION`, set by the Release workflow in the
 * image, or `dev` anywhere else.
 */
fun appVersion(env: (String) -> String? = System::getenv): String =
    env("APP_VERSION")?.takeIf { it.isNotBlank() } ?: "dev"

/**
 * Placeholder API. The guest app needs no server; this only proves the stack
 * runs on the dev computer. Auth, sync and the database arrive with accounts.
 */
fun app(version: String = appVersion()): HttpHandler = routes(
    "/health" bind GET to { Response(OK).body("ok $version") },
)

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val version = appVersion()
    app(version).asServer(SunHttp(port)).start()
    println("Coach Poulpy server $version listening on http://localhost:$port")
}
