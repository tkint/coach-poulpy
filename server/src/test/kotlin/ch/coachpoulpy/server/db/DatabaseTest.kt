package ch.coachpoulpy.server.db

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs the migrations against a real Postgres 17, started from binaries, with no Docker needed. */
class DatabaseTest {
    private val postgres = EmbeddedPostgres.start()
    private val database = Database(postgres.postgresDatabase)

    @AfterTest
    fun stop() = postgres.close()

    @Test
    fun aNewDatabaseHasNoSchema() {
        assertNull(database.version())
    }

    @Test
    fun migratingANewDatabaseCreatesTheLatestSchema() {
        database.migrate()
        assertEquals(ServerDatabase.Schema.version, database.version())
    }

    @Test
    fun migratingTwiceChangesNothing() {
        database.migrate()
        database.migrate()
        assertEquals(ServerDatabase.Schema.version, database.version())
    }

    @Test
    fun aDatabaseNewerThanTheServerStopsIt() {
        database.migrate()
        postgres.postgresDatabase.connection.use { it.createStatement().execute("UPDATE schema_version SET version = version + 1") }
        assertFailsWith<IllegalStateException> { database.migrate() }
    }

    @Test
    fun theDatabaseAnswersWhileItRuns() {
        assertTrue(database.answers())
        postgres.close()
        assertFalse(database.answers())
    }

    @Test
    fun aCatalogPublishedByVersionKeepsEveryRevisionAndGetsNumbersInPublicationOrder() {
        postgres.postgresDatabase.connection.use { connection ->
            connection.createStatement().execute(File("src/main/sqldelight/migrations/12.sqm").readText())
            connection.createStatement().execute(
                """
                INSERT INTO catalog_version (version, published_at) VALUES (1, now()), (2, now());
                INSERT INTO catalog_exercise_revision (slug, revision, position, content, published_in, created_at) VALUES
                    ('a', 1, 0, '{}', 1, now()), ('b', 1, 1, '{}', 1, now()), ('a', 2, 0, '{}', 2, now()), ('c', 1, 2, '{}', NULL, now());
                """.trimIndent(),
            )
        }
        ServerDatabase.Schema.migrate(postgres.postgresDatabase.asJdbcDriver(), 13, 14)
        postgres.postgresDatabase.connection.use { connection ->
            val seqs = connection.createStatement().executeQuery("SELECT slug, revision, seq FROM catalog_exercise_revision ORDER BY slug, revision").use { rows ->
                buildList { while (rows.next()) add(Triple(rows.getString(1), rows.getInt(2), rows.getObject(3) as Long?)) }
            }
            assertEquals(listOf(Triple("a", 1, 1L), Triple("a", 2, 3L), Triple("b", 1, 2L), Triple("c", 1, null)), seqs, "published in order, the draft has no number")
            val last = connection.createStatement().executeQuery("SELECT last_seq FROM catalog_publication").use { rows -> rows.next(); rows.getLong(1) }
            assertEquals(3L, last)
        }
    }
}
