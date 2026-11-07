package ch.coachpoulpy.server

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.api.api
import ch.coachpoulpy.server.auth.authenticate
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.db.DatabaseSettings
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
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
 *
 * `/health` answers 503 while [databaseAnswers] says the database doesn't, so Docker reports the
 * server unhealthy rather than serving requests that would fail.
 */
fun app(version: String, catalog: Catalog, databaseAnswers: () -> Boolean): HttpHandler = routes(
    "/health" bind GET to {
        if (databaseAnswers()) Response(OK).body("ok $version") else Response(SERVICE_UNAVAILABLE).body("database unavailable")
    },
    authenticate(authenticators = emptyMap()).then(api(version, catalog)),
)

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val version = appVersion()
    // Migrations run before the server listens; a failed one throws and stops it here.
    val database = Database.connect(DatabaseSettings.from(System::getenv)).apply { migrate() }
    app(version, Catalog.bundled, database::answers).asServer(SunHttp(port)).start()
    println("Coach Poulpy server $version listening on http://localhost:$port")
}
