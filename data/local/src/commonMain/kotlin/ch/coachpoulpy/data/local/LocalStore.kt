package ch.coachpoulpy.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun latestWorkout(id: WorkoutId): Workout? =
        db.workoutQueries.latestById(id.value).executeAsOneOrNull()
            ?.let { json.decodeFromString(Workout.serializer(), it.content) }

    fun workout(id: WorkoutId, version: Int): Workout? =
        db.workoutQueries.byIdAndVersion(id.value, version.toLong()).executeAsOneOrNull()
            ?.let { json.decodeFromString(Workout.serializer(), it.content) }

    fun deleteWorkout(id: WorkoutId) = db.workoutQueries.deleteWorkout(id.value)

    /** The catalog version stored on the phone, or null before the first seed. */
    fun catalogVersion(): Int? = db.catalogEntryQueries.version().executeAsOneOrNull()?.toInt()

    fun catalogExercises(): List<Exercise> =
        db.catalogEntryQueries.all().executeAsList().map { json.decodeFromString(Exercise.serializer(), it) }

    /**
     * Stores a whole catalog version. An entry the new version leaves out is kept as
     * retired rather than deleted, so workouts that use it still open.
     */
    fun replaceCatalog(version: Int, exercises: List<Exercise>) = db.transaction {
        val incoming = exercises.map { it.slug }.toSet()
        val dropped = catalogExercises().filter { it.slug !in incoming }.map { it.copy(retired = true) }
        (exercises + dropped).forEach {
            db.catalogEntryQueries.upsert(it.slug.value, json.encodeToString(Exercise.serializer(), it))
        }
        db.catalogEntryQueries.setVersion(version.toLong())
    }

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

    fun finishExecution(executionId: String, endedAt: Instant) =
        db.executionQueries.finishExecution(endedAt.toEpochMilliseconds(), executionId)

    /** The session left open when the app was closed or killed, if any. */
    fun unfinishedExecution(): Execution? = db.executionQueries.unfinished().executeAsOneOrNull()?.let { row ->
        Execution(
            id = ExecutionId(row.id),
            source = ExecutionSource.valueOf(row.source),
            startedAt = Instant.fromEpochMilliseconds(row.started_at),
            workout = row.workout_id?.let { WorkoutRef(WorkoutId(it), row.workout_version?.toInt() ?: 1) },
        )
    }

    /** When each finished session ended, oldest first, updated as sessions finish. */
    fun finishedSessionEnds(): Flow<List<Instant>> =
        db.executionQueries.finishedSessionEnds()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { ends -> ends.map(Instant::fromEpochMilliseconds) }

    fun events(executionId: String): List<SessionEvent> =
        db.executionQueries.events(executionId).executeAsList()
            .map { json.decodeFromString(SessionEvent.serializer(), it) }
}
