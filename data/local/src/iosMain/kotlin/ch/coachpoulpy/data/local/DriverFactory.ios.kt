package ch.coachpoulpy.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase

actual class DriverFactory {
    actual fun create(): SqlDriver = NativeSqliteDriver(CoachPoulpyDatabase.Schema, DATABASE_FILE)
}
