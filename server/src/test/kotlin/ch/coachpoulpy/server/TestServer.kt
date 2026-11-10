package ch.coachpoulpy.server

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.accounts.AccountDeletion
import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.accounts.PasswordResets
import ch.coachpoulpy.server.accounts.Passwords
import ch.coachpoulpy.server.admin.AdminAudit
import ch.coachpoulpy.server.admin.ServerSettings
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.catalog.ServerCatalog
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.mail.Email
import ch.coachpoulpy.server.mail.Mailer
import ch.coachpoulpy.server.mail.SettingsMailer
import ch.coachpoulpy.server.sync.Sync
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

/** One Postgres 17 for the whole test run, started from binaries; each test gets its own empty database in it. */
object TestPostgres {
    private val postgres by lazy { EmbeddedPostgres.start() }
    private val count = AtomicInteger()

    /** A new, migrated database. */
    fun database(): Database {
        val name = "test_${count.incrementAndGet()}"
        postgres.postgresDatabase.connection.use { it.createStatement().execute("CREATE DATABASE $name") }
        return Database(postgres.getDatabase("postgres", name)).apply { migrate() }
    }
}

/** Collects what would have been sent, or refuses it as [failure] says, as a mail server would. */
class FakeMailer : Mailer {
    val sent = mutableListOf<Email>()
    var failure: Exception? = null

    override fun send(email: Email) {
        failure?.let { throw it }
        sent += email
    }
}

/** A clock the test moves by hand. */
class TestClock(var now: Instant = Instant.parse("2026-11-07T10:00:00Z")) : Clock() {
    override fun instant(): Instant = now
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = this
}

/** The whole server over a fresh database, with a fake mailer and a cheap password cost to keep tests fast. */
class TestServer {
    val database = TestPostgres.database()
    val mailer = FakeMailer()
    val clock = TestClock()
    val settings = ServerSettings(database, defaultMailFrom = "Coach Poulpy <hello@coachpoulpy.example>", clock = clock)
    /** What the server sends through: the from-address from [settings], then [mailer]. */
    val outgoing = SettingsMailer(mailer, settings::mailFrom, clock)
    val passwords = Passwords(memoryKib = 64, iterations = 1)
    val accounts = Accounts(database, outgoing, "https://app.example.com", passwords, clock)
    val catalog: Catalog = Catalog.bundled
    /** The catalog in the database, seeded from [catalog] as the server's first start does. */
    val serverCatalog = ServerCatalog(database, catalog, clock).apply { seed() }
    val signIns = SignIns(database, passwords, clock)
    val passwordResets = PasswordResets(database, outgoing, "https://app.example.com", signIns, passwords, clock)
    val accountDeletion = AccountDeletion(database, passwords)
    val sync = Sync(database)
    val audit = AdminAudit(database, clock)
    val http = app("0.7.0", catalog, database, accounts, signIns, passwordResets, accountDeletion, sync, audit, settings = settings, mailer = outgoing)
}
