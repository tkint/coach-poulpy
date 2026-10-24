package ch.coachpoulpy.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase

/** Used by tests and desktop tools. [url] defaults to an in-memory database. */
actual class DriverFactory(private val url: String = JdbcSqliteDriver.IN_MEMORY) {
    actual fun create(): SqlDriver = JdbcSqliteDriver(url).also { CoachPoulpyDatabase.Schema.create(it) }
}
