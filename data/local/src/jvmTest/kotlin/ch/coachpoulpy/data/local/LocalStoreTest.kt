package ch.coachpoulpy.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class LocalStoreTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun sessionEventsComeBackInOrder() {
        val store = LocalStore(DriverFactory())
        store.startSession(SessionRecord(SessionId("e1"), SessionSource.GUIDED, startedAt = t0))

        val written = listOf(SessionEvent.Started(t0), SessionEvent.PhaseCompleted(t0, 0), SessionEvent.Paused(t0))
        written.forEach { store.appendEvent("e1", it) }

        assertEquals(written, store.events("e1"))
    }

    @Test
    fun finishedSessionIsNoLongerUnfinished() {
        val store = LocalStore(DriverFactory())
        store.startSession(SessionRecord(SessionId("e1"), SessionSource.GUIDED, startedAt = t0))
        assertEquals(SessionId("e1"), store.unfinishedRecord()?.id)

        store.finishSession("e1", t0)
        assertNull(store.unfinishedRecord())
    }

    @Test
    fun onlySessionsPlayedToTheEndCountAsFinished() = runTest {
        val store = LocalStore(DriverFactory())
        val t1 = Instant.fromEpochSeconds(t0.epochSeconds + 600)
        store.startSession(SessionRecord(SessionId("done"), SessionSource.GUIDED, startedAt = t0))
        store.appendEvent("done", SessionEvent.Started(t0))
        store.appendEvent("done", SessionEvent.Finished(t1))
        store.finishSession("done", t1)
        // Left behind, then closed when another session started.
        store.startSession(SessionRecord(SessionId("left"), SessionSource.GUIDED, startedAt = t0))
        store.appendEvent("left", SessionEvent.Started(t0))
        store.finishSession("left", t0)
        store.startSession(SessionRecord(SessionId("open"), SessionSource.GUIDED, startedAt = t1))

        assertEquals(listOf(t1), store.finishedSessionEnds().first())
    }

    @Test
    fun anExerciseIsReplacedOnlyByAHigherRevision() {
        val store = LocalStore(DriverFactory())
        fun named(name: String) = exercise("squat").copy(name = LocalizedText(mapOf("en" to name)))
        assertEquals(1, store.applyCatalogRevisions(listOf(CatalogRevision(named("Squat"), 1))))
        assertEquals(0, store.applyCatalogRevisions(listOf(CatalogRevision(named("Older"), 1))), "the same revision changes nothing")
        assertEquals("Squat", store.catalogExercises().single().name.byLanguage["en"])
        assertEquals(1, store.applyCatalogRevisions(listOf(CatalogRevision(named("Better squat"), 2))))
        assertEquals(0, store.applyCatalogRevisions(listOf(CatalogRevision(named("Stale"), 1))), "a lower revision is ignored")
        assertEquals("Better squat", store.catalogExercises().single().name.byLanguage["en"])
        assertEquals(mapOf(ExerciseSlug("squat") to 2), store.catalogRevisions())
    }

    @Test
    fun exercisesAnUpdateLeavesOutAreKeptAsTheyAre() {
        val store = LocalStore(DriverFactory())
        store.applyCatalogRevisions(listOf(CatalogRevision(exercise("squat"), 1), CatalogRevision(exercise("lunge"), 1)))
        store.applyCatalogRevisions(listOf(CatalogRevision(exercise("squat").copy(retired = true), 2)), cursor = "7")

        val bySlug = store.catalogExercises().associateBy { it.slug.value }
        assertEquals(true, bySlug.getValue("squat").retired)
        assertEquals(false, bySlug.getValue("lunge").retired)
        assertEquals("7", store.catalogCursor())
    }

    @Test
    fun theCursorIsKeptWithTheExercisesOrNotAtAll() {
        val store = LocalStore(DriverFactory())
        assertEquals(null, store.catalogCursor())
        store.applyCatalogRevisions(listOf(CatalogRevision(exercise("squat"), 1)))
        assertEquals(null, store.catalogCursor(), "a store that never updated has no cursor")
        store.applyCatalogRevisions(listOf(CatalogRevision(exercise("lunge"), 1)), cursor = "3")
        store.applyCatalogRevisions(emptyList())
        assertEquals("3", store.catalogCursor(), "an update with no cursor leaves the last one")
    }

    @Test
    fun variationGroupsFollowTheExercisesThatNameThem() {
        val store = LocalStore(DriverFactory())
        store.applyCatalogRevisions(
            listOf(
                CatalogRevision(exercise("squat"), 1, variationGroup = 0, variationRank = 1),
                CatalogRevision(exercise("chair-squat"), 1, variationGroup = 0, variationRank = 0),
                CatalogRevision(exercise("lunge"), 1),
            ),
        )
        assertEquals(listOf(listOf(ExerciseSlug("chair-squat"), ExerciseSlug("squat"))), store.catalogVariationGroups())

        // The same revision refreshes the group; a phone migrated from before groups were columns gets them this way.
        store.applyCatalogRevisions(listOf(CatalogRevision(exercise("lunge"), 1, variationGroup = 1, variationRank = 0), CatalogRevision(exercise("squat"), 1, variationGroup = 1, variationRank = 1)))
        assertEquals(2, store.catalogVariationGroups().size)
    }

    @Test
    fun databaseFromTheFirstBuildGainsTheCatalogTables() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "CREATE TABLE workout_version (id TEXT NOT NULL, version INTEGER NOT NULL, type TEXT NOT NULL, name TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL, PRIMARY KEY (id, version))", 0)
        driver.execute(null, "CREATE TABLE execution (id TEXT NOT NULL PRIMARY KEY, source TEXT NOT NULL, workout_id TEXT, workout_version INTEGER, started_at INTEGER NOT NULL, ended_at INTEGER)", 0)
        driver.execute(null, "CREATE TABLE execution_event (execution_id TEXT NOT NULL REFERENCES execution(id) ON DELETE CASCADE, seq INTEGER NOT NULL, at INTEGER NOT NULL, payload TEXT NOT NULL, PRIMARY KEY (execution_id, seq))", 0)
        driver.execute(null, "INSERT INTO execution VALUES ('old', 'GUIDED', NULL, NULL, 1700000000000, 1700000600000)", 0)
        CoachPoulpyDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = CoachPoulpyDatabase.Schema.version)

        val db = CoachPoulpyDatabase(driver)
        db.catalogEntryQueries.upsert("squat", "{}", 1, null, null)
        assertEquals(1L, db.catalogEntryQueries.revisions().executeAsOne().revision)
        assertEquals(null, db.catalogEntryQueries.cursor().executeAsOneOrNull())
        // Sessions done before history keep their rows, with nothing summed up yet.
        val old = db.sessionQueries.finished().executeAsOne()
        assertEquals("old", old.id)
        assertNull(old.summary)
        // The migrations reach the planning and variation tables too.
        assertEquals(0, db.plannedWorkoutQueries.plans().executeAsList().size)
        assertEquals(0, db.catalogEntryQueries.variationMembers().executeAsList().size)
    }

    @Test
    fun workoutsWithATypeAreRemovedAndSessionsKeptWithoutIt() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "CREATE TABLE workout_version (id TEXT NOT NULL, version INTEGER NOT NULL, type TEXT NOT NULL, name TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL, PRIMARY KEY (id, version))", 0)
        driver.execute(null, "CREATE TABLE execution (id TEXT NOT NULL PRIMARY KEY, source TEXT NOT NULL, workout_id TEXT, workout_version INTEGER, started_at INTEGER NOT NULL, ended_at INTEGER)", 0)
        driver.execute(null, "CREATE TABLE execution_event (execution_id TEXT NOT NULL REFERENCES execution(id) ON DELETE CASCADE, seq INTEGER NOT NULL, at INTEGER NOT NULL, payload TEXT NOT NULL, PRIMARY KEY (execution_id, seq))", 0)
        CoachPoulpyDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 5)
        driver.execute(null, "INSERT INTO workout_version VALUES ('w1', 1, 'STRENGTH', 'Legs', '{}', 1700000000000)", 0)
        driver.execute(null, "INSERT INTO planned_workout VALUES ('p1', 'w1', '2026-10-27', 1700000000000)", 0)
        driver.execute(null, "INSERT INTO execution(id, source, workout_id, workout_version, started_at, ended_at, title, workout_type) VALUES ('old', 'GUIDED', 'w1', 1, 1700000000000, 1700000600000, 'Legs', 'STRENGTH')", 0)

        CoachPoulpyDatabase.Schema.migrate(driver, oldVersion = 5, newVersion = CoachPoulpyDatabase.Schema.version)

        val db = CoachPoulpyDatabase(driver)
        assertEquals(0, db.workoutQueries.latestVersions().executeAsList().size)
        assertEquals(0, db.plannedWorkoutQueries.plans().executeAsList().size)
        val old = db.sessionQueries.finished().executeAsOne()
        assertEquals("Legs", old.title)
        assertNull(old.workout_type)
        db.workoutQueries.insert("w2", 1, "CIRCUIT", "Burner", "{}", 1700000000000)
        assertEquals("CIRCUIT", db.workoutQueries.latestById("w2").executeAsOne().format)
    }

    @Test
    fun plansKeepTheirDayAndGoWithTheirWorkout() = runTest {
        val store = LocalStore(DriverFactory())
        val workout = Workout(WorkoutId("w1"), 1, "Legs", WorkoutFormat.SETS, emptyList())
        store.saveWorkout(workout)
        val later = store.planWorkout(workout.id, LocalDate(2026, 10, 29))
        val sooner = store.planWorkout(workout.id, LocalDate(2026, 10, 27))

        assertEquals(listOf(sooner, later), store.plannedWorkouts().first().map { it.id })

        store.movePlan(later, LocalDate(2026, 10, 26))
        assertEquals(LocalDate(2026, 10, 26), store.plannedWorkouts().first().first().day)

        store.removePlan(sooner)
        store.deleteWorkout(workout.id)
        assertEquals(emptyList(), store.plannedWorkouts().first())
    }

    @Test
    fun finishedSessionsComeNewestFirstWithTheirTotals() = runTest {
        val store = LocalStore(DriverFactory())
        store.startSession(SessionRecord(SessionId("a"), SessionSource.GUIDED, startedAt = t0), title = "Legs", format = WorkoutFormat.SETS)
        store.startSession(SessionRecord(SessionId("b"), SessionSource.DEVICE_ONLY, startedAt = t0 + 1.days), format = WorkoutFormat.CIRCUIT)
        store.startSession(SessionRecord(SessionId("open"), SessionSource.GUIDED, startedAt = t0 + 2.days))
        store.finishSession("a", t0 + 20.minutes)
        store.finishSession("b", t0 + 1.days + 30.minutes)
        store.saveTotals("b", SessionTotals(active = 30.minutes, distanceMeters = 3000.0))

        val rows = store.finishedSessions().first()
        assertEquals(listOf("b", "a"), rows.map { it.record.id.value })
        assertEquals(3000.0, rows[0].totals?.distanceMeters)
        assertEquals(WorkoutFormat.CIRCUIT, rows[0].format)
        assertEquals("Legs", rows[1].title)
        assertNull(rows[1].totals)
    }

    @Test
    fun deletingASessionRemovesItsEvents() {
        val store = LocalStore(DriverFactory())
        store.startSession(SessionRecord(SessionId("e1"), SessionSource.GUIDED, startedAt = t0))
        store.appendEvent("e1", SessionEvent.Started(t0))
        store.finishSession("e1", t0)

        store.deleteSession("e1")
        assertEquals(emptyList<SessionEvent>(), store.events("e1"))
        assertNull(store.unfinishedRecord())
    }

    private fun exercise(slug: String) = Exercise(
        slug = ExerciseSlug(slug),
        name = LocalizedText(mapOf("en" to slug)),
        movementPattern = MovementPattern.SQUAT,
        muscleGroups = listOf(MuscleGroup.QUADRICEPS),
        measurement = MeasurementMode.REPS,
    )

    @Test
    fun aSecondBodyMeasurementOnTheSameDayCorrectsTheFirst() = runTest {
        val store = LocalStore(DriverFactory())
        val day = LocalDate(2026, 11, 1)
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 72.6))
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 72.9))
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, LocalDate(2026, 10, 25), 73.1))
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.HEIGHT, day, 175.0))

        assertEquals(listOf(73.1, 72.9), store.bodyMeasurements(BodyMetric.WEIGHT).first().map { it.value })
        assertEquals(listOf(175.0), store.bodyMeasurements(BodyMetric.HEIGHT).first().map { it.value })

        store.deleteBodyMeasurement(BodyMetric.WEIGHT, day)
        assertEquals(listOf(73.1), store.bodyMeasurements(BodyMetric.WEIGHT).first().map { it.value })
    }
}
