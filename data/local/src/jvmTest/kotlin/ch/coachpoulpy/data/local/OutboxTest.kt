package ch.coachpoulpy.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMeasurementChange
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.PlannedWorkoutChange
import ch.coachpoulpy.core.model.PlannedWorkoutDeletedChange
import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionDeletedChange
import ch.coachpoulpy.core.model.SessionEventsChange
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutDeletedChange
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.jsonPrimitive

class OutboxTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)
    private var now = t0
    private val store = LocalStore(DriverFactory(), clock = object : Clock {
        override fun now() = now
    })
    private val legs = Workout(WorkoutId("w1"), 1, "Legs", WorkoutFormat.SETS, emptyList())
    private val day = LocalDate(2026, 11, 1)

    private fun changes(): List<SyncChange> = store.outbox(1000).map { it.change }

    @Test
    fun aGuestsWritesAddNothing() {
        store.saveWorkout(legs)
        store.planWorkout(legs.id, day)
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 72.5))
        store.startSession(SessionRecord(SessionId("s1"), SessionSource.GUIDED, startedAt = t0))
        store.appendEvent("s1", SessionEvent.Started(t0))
        store.recordSetting("profile.name", "Sam")
        store.deleteWorkout(legs.id)

        assertFalse(store.recordingChanges())
        assertEquals(emptyList(), changes())
    }

    @Test
    fun workoutVersionsAndTheirDeletionAreRecorded() {
        store.startRecordingChanges(emptyMap())
        store.saveWorkout(legs)
        store.saveWorkout(legs.copy(version = 2, name = "Legs day"))
        val plan = store.planWorkout(legs.id, day)
        now = t0 + 5.minutes
        store.deleteWorkout(legs.id)

        assertEquals(
            listOf(
                WorkoutVersionChange(legs, null, t0),
                WorkoutVersionChange(legs.copy(version = 2, name = "Legs day"), 1, t0),
                PlannedWorkoutChange(plan, legs.id, day, t0, t0),
                PlannedWorkoutDeletedChange(plan, now),
                WorkoutDeletedChange(legs.id, 2, now),
            ),
            changes(),
        )
    }

    @Test
    fun plansAreRecordedWhenPutMovedAndRemoved() {
        store.saveWorkout(legs)
        store.startRecordingChanges(emptyMap())
        val plan = store.planWorkout(legs.id, day)
        now = t0 + 1.minutes
        store.movePlan(plan, LocalDate(2026, 11, 3))
        now = t0 + 2.minutes
        store.replacePlans(legs.id, add = emptyList(), remove = listOf(plan))
        store.removePlan("never-planned")

        assertEquals(
            listOf(
                PlannedWorkoutChange(plan, legs.id, day, t0, t0),
                PlannedWorkoutChange(plan, legs.id, LocalDate(2026, 11, 3), t0, t0 + 1.minutes),
                PlannedWorkoutDeletedChange(plan, t0 + 2.minutes),
            ),
            changes().drop(1), // The first is the workout, from filling the outbox.
        )
    }

    @Test
    fun bodyLogWritesAreRecordedWithNoValueForADelete() {
        store.startRecordingChanges(emptyMap())
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 72.5))
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 72.9))
        store.deleteBodyMeasurement(BodyMetric.WEIGHT, day)
        store.deleteBodyMeasurement(BodyMetric.HEIGHT, day)

        assertEquals(
            listOf(
                BodyMeasurementChange(BodyMetric.WEIGHT, day, 72.5, t0),
                BodyMeasurementChange(BodyMetric.WEIGHT, day, 72.9, t0),
                BodyMeasurementChange(BodyMetric.WEIGHT, day, null, t0),
            ),
            changes(),
        )
    }

    @Test
    fun sessionHeadersEventsAndDeletionAreRecorded() {
        store.startRecordingChanges(emptyMap())
        val end = t0 + 20.minutes
        store.startSession(SessionRecord(SessionId("s1"), SessionSource.GUIDED, startedAt = t0), title = "Legs", format = WorkoutFormat.SETS)
        store.appendEvent("s1", SessionEvent.Started(t0))
        store.appendEvent("s1", SessionEvent.Finished(end))
        store.finishSession("s1", end)
        store.saveTotals("s1", SessionTotals(active = 20.minutes))
        store.deleteSession("s1")

        val changes = changes()
        assertEquals(SessionChange(SessionId("s1"), SessionSource.GUIDED, t0, title = "Legs", format = WorkoutFormat.SETS), changes[0])
        val events = changes.subList(1, 3).map { (it as SessionEventsChange).events.single() }
        assertEquals(listOf(0L, 1L), events.map { it.seq })
        assertEquals(listOf("started", "finished"), events.map { it.payload.getValue("type").jsonPrimitive.content })
        assertEquals(end, (changes[3] as SessionChange).endedAt)
        assertTrue((changes[4] as SessionChange).totals != null)
        assertEquals(SessionDeletedChange(SessionId("s1"), t0), changes[5])
        assertEquals(6, changes.size)
    }

    @Test
    fun onlySettingsThatFollowTheAccountAreRecorded() {
        store.startRecordingChanges(emptyMap())
        store.recordSetting("profile.name", "Sam")
        store.recordSetting("settings.theme", "DARK")
        store.recordSetting("settings.units", null)

        assertEquals(listOf(SettingChange("profile.name", "Sam", t0), SettingChange("settings.units", null, t0)), changes())
    }

    @Test
    fun startingFillsTheOutboxWithTheWholePhoneKeepingEveryId() {
        store.saveWorkout(legs)
        store.saveWorkout(legs.copy(version = 2))
        val plan = store.planWorkout(legs.id, day)
        store.logBodyMeasurement(BodyMeasurement(BodyMetric.HEIGHT, day, 175.0))
        store.startSession(SessionRecord(SessionId("s1"), SessionSource.GUIDED, startedAt = t0))
        repeat(250) { store.appendEvent("s1", SessionEvent.Paused(t0)) }

        store.startRecordingChanges(mapOf("profile.name" to "Sam", "settings.theme" to "DARK"))

        val changes = changes()
        assertEquals(listOf(null, 1), changes.filterIsInstance<WorkoutVersionChange>().map { it.baseVersion })
        assertEquals(listOf(plan), changes.filterIsInstance<PlannedWorkoutChange>().map { it.id })
        assertEquals(listOf(175.0), changes.filterIsInstance<BodyMeasurementChange>().map { it.value })
        assertEquals(listOf(SessionId("s1")), changes.filterIsInstance<SessionChange>().map { it.id })
        val events = changes.filterIsInstance<SessionEventsChange>()
        assertEquals(listOf(200, 50), events.map { it.events.size })
        assertEquals((0L until 250L).toList(), events.flatMap { it.events }.map { it.seq })
        assertEquals(listOf(SettingChange("profile.name", "Sam", t0)), changes.filterIsInstance<SettingChange>())
    }

    @Test
    fun startingAgainDoesNotRecordTwice() {
        store.saveWorkout(legs)
        store.startRecordingChanges(emptyMap())
        store.startRecordingChanges(emptyMap())

        assertEquals(1, changes().size)
    }

    @Test
    fun pushedChangesLeaveAndStoppingEmptiesTheOutbox() {
        store.startRecordingChanges(emptyMap())
        store.recordSetting("profile.name", "Sam")
        store.recordSetting("profile.name", "Alex")
        store.removeFromOutbox(store.outbox(1).single().seq)
        assertEquals(listOf(SettingChange("profile.name", "Alex", t0)), changes())

        store.stopRecordingChanges()
        store.saveWorkout(legs)
        assertFalse(store.recordingChanges())
        assertEquals(emptyList(), changes())
    }

    @Test
    fun aPhoneFromBeforeSyncGainsAnEmptyOutboxAndRecordsNothing() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "CREATE TABLE workout_version (id TEXT NOT NULL, version INTEGER NOT NULL, type TEXT NOT NULL, name TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL, PRIMARY KEY (id, version))", 0)
        driver.execute(null, "CREATE TABLE execution (id TEXT NOT NULL PRIMARY KEY, source TEXT NOT NULL, workout_id TEXT, workout_version INTEGER, started_at INTEGER NOT NULL, ended_at INTEGER)", 0)
        driver.execute(null, "CREATE TABLE execution_event (execution_id TEXT NOT NULL REFERENCES execution(id) ON DELETE CASCADE, seq INTEGER NOT NULL, at INTEGER NOT NULL, payload TEXT NOT NULL, PRIMARY KEY (execution_id, seq))", 0)
        CoachPoulpyDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 8)
        driver.execute(null, "INSERT INTO body_measurement VALUES ('m1', 'WEIGHT', '2026-11-01', 72.5, 1700000000000)", 0)

        CoachPoulpyDatabase.Schema.migrate(driver, oldVersion = 8, newVersion = CoachPoulpyDatabase.Schema.version)

        val db = CoachPoulpyDatabase(driver)
        assertEquals(0, db.syncQueries.outbox(10).executeAsList().size)
        assertEquals(null, db.syncQueries.recording().executeAsOneOrNull())
        assertEquals(1, db.bodyMeasurementQueries.allMeasurements().executeAsList().size)
    }
}
