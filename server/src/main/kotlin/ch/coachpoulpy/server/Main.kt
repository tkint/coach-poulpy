package ch.coachpoulpy.server

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.accounts.AccountDeletion
import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.accounts.PasswordResets
import ch.coachpoulpy.server.accounts.Passwords
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.mail.ConsoleMailer
import ch.coachpoulpy.server.mail.MailSettings
import ch.coachpoulpy.server.mail.SmtpMailer
import ch.coachpoulpy.server.api.api
import ch.coachpoulpy.server.api.confirmationEvents
import ch.coachpoulpy.server.auth.authenticate
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.db.DatabaseSettings
import ch.coachpoulpy.server.sync.Sync
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.PolyHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.Only
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

/**
 * The release this server was built from: `APP_VERSION`, set by the Release workflow in the
 * image, or `dev` anywhere else.
 */
fun appVersion(env: (String) -> String? = System::getenv): String =
    env("APP_VERSION")?.takeIf { it.isNotBlank() } ?: "dev"

/** Where the links in emails point: `WEBSITE_URL`, or the website (ADR 0021). */
fun websiteUrl(env: (String) -> String?): String =
    env("WEBSITE_URL")?.takeIf { it.isNotBlank() } ?: "https://app.coach-poulpy.com"

/** The website's origin, such as `https://app.coach-poulpy.com`: the scheme and host of [websiteUrl]. */
fun websiteOrigin(websiteUrl: String): String = java.net.URI(websiteUrl).let { uri ->
    "${uri.scheme}://${uri.authority}"
}

/**
 * Browsers may call the API from the website only (ADR 0021): its confirm-email and
 * reset-password pages post JSON. No cookie or credential is involved yet.
 */
fun websiteCors(origin: String) = ServerFilters.Cors(
    CorsPolicy(OriginPolicy.Only(origin), headers = listOf("content-type"), methods = listOf(GET, POST)),
)

/**
 * The server: `/health` for the Docker health check, and the API under `/api/v1`, where every
 * request first gets its caller (ADR 0020): anonymous, or the account of a `cp_at_` access token.
 * Any other token is refused.
 *
 * `/health` answers 503 while [databaseAnswers] says the database doesn't, so Docker reports the
 * server unhealthy rather than serving requests that would fail.
 */
fun app(
    version: String,
    catalog: Catalog,
    database: Database,
    accounts: Accounts,
    signIns: SignIns,
    passwordResets: PasswordResets,
    accountDeletion: AccountDeletion,
    sync: Sync = Sync(database),
    databaseAnswers: () -> Boolean = database::answers,
    websiteOrigin: String = websiteOrigin(websiteUrl { null }),
): HttpHandler = websiteCors(websiteOrigin).then(routes(
    "/health" bind GET to {
        if (databaseAnswers()) Response(OK).body("ok $version") else Response(SERVICE_UNAVAILABLE).body("database unavailable")
    },
    authenticate(mapOf(SignIns.ACCESS_PREFIX to signIns.authenticator)).then(api(version, catalog, accounts, signIns, passwordResets, accountDeletion, sync, database)),
))

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val version = appVersion()
    // Migrations run before the server listens; a failed one throws and stops it here.
    val database = Database.connect(DatabaseSettings.from(System::getenv)).apply { migrate() }
    val mailer = MailSettings.from(System::getenv)?.let(::SmtpMailer) ?: ConsoleMailer
    val passwords = Passwords()
    val accounts = Accounts(database, mailer, websiteUrl = websiteUrl(System::getenv), passwords = passwords)
    val signIns = SignIns(database, passwords)
    val passwordResets = PasswordResets(database, mailer, websiteUrl(System::getenv), signIns, passwords)
    val accountDeletion = AccountDeletion(database, passwords)
    val origin = websiteOrigin(websiteUrl(System::getenv))
    val http = app(version, Catalog.bundled, database, accounts, signIns, passwordResets, accountDeletion, websiteOrigin = origin)
    // Jetty, for the server-sent events SunHttp can't hold open (#319).
    PolyHandler(http, sse = confirmationEvents(accounts)).asServer(Jetty(port)).start()
    println("Coach Poulpy server $version listening on http://localhost:$port")
}
