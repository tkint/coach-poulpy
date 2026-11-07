package ch.coachpoulpy.server.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

/** Where the server's Postgres is, from `DATABASE_URL`, `DATABASE_USER` and `DATABASE_PASSWORD`. */
data class DatabaseSettings(val url: String, val user: String, val password: String) {
    companion object {
        /** The defaults reach the local Postgres of `tools/compose.yaml`. */
        fun from(env: (String) -> String?) = DatabaseSettings(
            url = env("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/coachpoulpy",
            user = env("DATABASE_USER") ?: "coachpoulpy",
            password = env("DATABASE_PASSWORD") ?: "coachpoulpy",
        )
    }
}

/**
 * The server's Postgres: a small connection pool, the typed queries SQLDelight generates from
 * `src/main/sqldelight`, and [migrate], which brings the schema up to date (ADR 0022).
 */
class Database(dataSource: DataSource) {
    private val driver: SqlDriver = dataSource.asJdbcDriver()
    private val queries = ServerDatabase(driver)

    /** The schema version this database is at, or null before the first [migrate]. */
    fun version(): Long? = if (hasSchema()) queries.schemaVersionQueries.current().executeAsOneOrNull() else null

    /**
     * Applies the migrations this database lacks, in one transaction, so a failed migration leaves
     * the database as it was and stops the server before it answers. An advisory lock keeps two
     * servers starting at once from migrating together.
     */
    fun migrate() {
        queries.transaction {
            driver.execute(null, "SELECT pg_advisory_xact_lock($MIGRATION_LOCK)", 0)
            val target = ServerDatabase.Schema.version
            if (!hasSchema()) {
                ServerDatabase.Schema.create(driver)
                queries.schemaVersionQueries.record(target)
            } else {
                val current = queries.schemaVersionQueries.current().executeAsOne()
                check(current <= target) { "The database is at schema version $current, newer than this server's $target" }
                if (current < target) {
                    ServerDatabase.Schema.migrate(driver, current, target)
                    queries.schemaVersionQueries.advance(target)
                }
            }
        }
    }

    /** Whether the database answers a trivial query, for `/health`. */
    fun answers(): Boolean = runCatching {
        driver.executeQuery(null, "SELECT 1", { cursor -> QueryResult.Value(cursor.next().value) }, 0).value
    }.getOrDefault(false)

    private fun hasSchema(): Boolean = driver.executeQuery(
        null,
        "SELECT to_regclass('schema_version') IS NOT NULL",
        { cursor -> QueryResult.Value(cursor.next().value && cursor.getBoolean(0) == true) },
        0,
    ).value

    companion object {
        /** An arbitrary number only this server's migrations lock on. */
        private const val MIGRATION_LOCK = 4_242_001L

        fun connect(settings: DatabaseSettings): Database = Database(
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = settings.url
                    username = settings.user
                    password = settings.password
                    // The server is capped at 512 MB beside Postgres (ADR 0019); a few connections do.
                    maximumPoolSize = 5
                },
            ),
        )
    }
}
