package ch.coachpoulpy.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class LocalStoreTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun sessionEventsComeBackInOrder() {
        val store = LocalStore(DriverFactory())
        store.startExecution(Execution(ExecutionId("e1"), ExecutionSource.GUIDED, startedAt = t0))

        val written = listOf(SessionEvent.Started(t0), SessionEvent.PhaseCompleted(t0, 0), SessionEvent.Paused(t0))
        written.forEach { store.appendEvent("e1", it) }

        assertEquals(written, store.events("e1"))
    }

    @Test
    fun finishedSessionIsNoLongerUnfinished() {
        val store = LocalStore(DriverFactory())
        store.startExecution(Execution(ExecutionId("e1"), ExecutionSource.GUIDED, startedAt = t0))
        assertEquals(ExecutionId("e1"), store.unfinishedExecution()?.id)

        store.finishExecution("e1", t0)
        assertNull(store.unfinishedExecution())
    }

    @Test
    fun onlySessionsPlayedToTheEndCountAsFinished() = runTest {
        val store = LocalStore(DriverFactory())
        val t1 = Instant.fromEpochSeconds(t0.epochSeconds + 600)
        store.startExecution(Execution(ExecutionId("done"), ExecutionSource.GUIDED, startedAt = t0))
        store.appendEvent("done", SessionEvent.Started(t0))
        store.appendEvent("done", SessionEvent.Finished(t1))
        store.finishExecution("done", t1)
        // Left behind, then closed when another session started.
        store.startExecution(Execution(ExecutionId("left"), ExecutionSource.GUIDED, startedAt = t0))
        store.appendEvent("left", SessionEvent.Started(t0))
        store.finishExecution("left", t0)
        store.startExecution(Execution(ExecutionId("open"), ExecutionSource.GUIDED, startedAt = t1))

        assertEquals(listOf(t1), store.finishedSessionEnds().first())
    }

    @Test
    fun exercisesLeftOutOfANewCatalogAreRetiredNotDeleted() {
        val store = LocalStore(DriverFactory())
        store.replaceCatalog(1, listOf(exercise("squat"), exercise("lunge")))
        store.replaceCatalog(2, listOf(exercise("squat")))

        assertEquals(2, store.catalogVersion())
        val bySlug = store.catalogExercises().associateBy { it.slug.value }
        assertEquals(false, bySlug.getValue("squat").retired)
        assertEquals(true, bySlug.getValue("lunge").retired)
    }

    @Test
    fun databaseFromTheFirstBuildGainsTheCatalogTables() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "CREATE TABLE workout_version (id TEXT NOT NULL, version INTEGER NOT NULL, type TEXT NOT NULL, name TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL, PRIMARY KEY (id, version))", 0)
        CoachPoulpyDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = CoachPoulpyDatabase.Schema.version)

        val db = CoachPoulpyDatabase(driver)
        db.catalogEntryQueries.setVersion(2)
        assertEquals(2L, db.catalogEntryQueries.version().executeAsOne())
    }

    private fun exercise(slug: String) = Exercise(
        slug = ExerciseSlug(slug),
        name = LocalizedText(slug, slug),
        movementPattern = MovementPattern.SQUAT,
        muscleGroups = listOf(MuscleGroup.QUADRICEPS),
        measurement = MeasurementMode.REPS,
    )
}
