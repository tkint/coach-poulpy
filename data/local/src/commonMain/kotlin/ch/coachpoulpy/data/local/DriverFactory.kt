package ch.coachpoulpy.data.local

import app.cash.sqldelight.db.SqlDriver

/** Each platform opens the app's private SQLite file its own way. */
expect class DriverFactory {
    fun create(): SqlDriver
}

internal const val DATABASE_FILE = "coachpoulpy.db"
