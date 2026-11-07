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
import ch.coachpoulpy.server.auth.authenticate
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.db.DatabaseSettings
import ch.coachpoulpy.server.sync.Sync
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

/** Where the links in emails point: `WEBSITE_URL`, or the website (ADR 0021). */
fun websiteUrl(env: (String) -> String?): String =
    env("WEBSITE_URL")?.takeIf { it.isNotBlank() } ?: "https://app.coach-poulpy.com"

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
): HttpHandler = routes(
    "/health" bind GET to {
        if (databaseAnswers()) Response(OK).body("ok $version") else Response(SERVICE_UNAVAILABLE).body("database unavailable")
    },
    authenticate(mapOf(SignIns.ACCESS_PREFIX to signIns.authenticator)).then(api(version, catalog, accounts, signIns, passwordResets, accountDeletion, sync, database)),
)

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
    app(version, Catalog.bundled, database, accounts, signIns, passwordResets, accountDeletion).asServer(SunHttp(port)).start()
    println("Coach Poulpy server $version listening on http://localhost:$port")
}
