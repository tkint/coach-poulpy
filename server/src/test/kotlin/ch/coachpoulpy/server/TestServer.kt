package ch.coachpoulpy.server

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.accounts.AccountDeletion
import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.accounts.PasswordResets
import ch.coachpoulpy.server.accounts.Passwords
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.server.mail.Email
import ch.coachpoulpy.server.mail.Mailer
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

/** Collects what would have been sent. */
class FakeMailer : Mailer {
    val sent = mutableListOf<Email>()

    override fun send(email: Email) {
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
    val passwords = Passwords(memoryKib = 64, iterations = 1)
    val accounts = Accounts(database, mailer, "https://app.example.com", passwords, clock)
    val catalog: Catalog = Catalog.bundled
    val signIns = SignIns(database, passwords, clock)
    val passwordResets = PasswordResets(database, mailer, "https://app.example.com", signIns, passwords, clock)
    val accountDeletion = AccountDeletion(database, passwords)
    val http = app("0.7.0", catalog, database, accounts, signIns, passwordResets, accountDeletion)
}
