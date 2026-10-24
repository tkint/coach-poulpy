package ch.coachpoulpy.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

/** The phone's database: the source of truth for a guest. */
class LocalStore(driverFactory: DriverFactory, private val clock: Clock = Clock.System) {
    private val db = CoachPoulpyDatabase(driverFactory.create())
    private val json = Json { ignoreUnknownKeys = true }

    fun saveWorkout(workout: Workout) {
        db.workoutQueries.insert(
            id = workout.id.value,
            version = workout.version.toLong(),
            type = workout.type.name,
            name = workout.name,
            content = json.encodeToString(Workout.serializer(), workout),
            created_at = clock.now().toEpochMilliseconds(),
        )
    }

    fun workouts(): Flow<List<Workout>> =
        db.workoutQueries.latestVersions { _, _, _, _, content, _ -> json.decodeFromString(Workout.serializer(), content) }
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun startExecution(execution: Execution) {
        db.executionQueries.insertExecution(
            id = execution.id.value,
            source = execution.source.name,
            workout_id = execution.workout?.workoutId?.value,
            workout_version = execution.workout?.version?.toLong(),
            started_at = execution.startedAt.toEpochMilliseconds(),
            ended_at = execution.endedAt?.toEpochMilliseconds(),
        )
    }

    /** Written at once for every session action, so a killed app can replay it. */
    fun appendEvent(executionId: String, event: SessionEvent) {
        db.executionQueries.appendEvent(
            executionId = executionId,
            at = event.at.toEpochMilliseconds(),
            payload = json.encodeToString(SessionEvent.serializer(), event),
        )
    }

    fun events(executionId: String): List<SessionEvent> =
        db.executionQueries.events(executionId).executeAsList()
            .map { json.decodeFromString(SessionEvent.serializer(), it) }
}
