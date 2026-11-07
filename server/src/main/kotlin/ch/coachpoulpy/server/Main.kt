package ch.coachpoulpy.server

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.api.api
import ch.coachpoulpy.server.auth.authenticate
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
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
 * The server: `/health` for the Docker health check, and the API under `/api/v1`, where every
 * request first gets its caller (ADR 0020). No authenticator is registered until accounts
 * arrive (#25), so for now every caller is anonymous and any token is refused.
 */
fun app(version: String = appVersion(), catalog: Catalog = Catalog.bundled): HttpHandler = routes(
    "/health" bind GET to { Response(OK).body("ok $version") },
    authenticate(authenticators = emptyMap()).then(api(version, catalog)),
)

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val version = appVersion()
    app(version).asServer(SunHttp(port)).start()
    println("Coach Poulpy server $version listening on http://localhost:$port")
}
