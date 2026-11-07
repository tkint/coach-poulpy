package ch.coachpoulpy.server.db

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
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
}
