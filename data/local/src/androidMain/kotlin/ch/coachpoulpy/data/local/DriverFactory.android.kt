package ch.coachpoulpy.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase

actual class DriverFactory(private val context: Context) {
    actual fun create(): SqlDriver = AndroidSqliteDriver(CoachPoulpyDatabase.Schema, context, DATABASE_FILE)
}
