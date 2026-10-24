package ch.coachpoulpy.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
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

    /** Its plans go with it: a day never points at a workout that is gone. */
    fun deleteWorkout(id: WorkoutId) = db.transaction {
        db.plannedWorkoutQueries.deletePlansOfWorkout(id.value)
        db.workoutQueries.deleteWorkout(id.value)
    }

    /** Every planned workout, by day then in the order they were planned. */
    fun plannedWorkouts(): Flow<List<PlannedWorkout>> =
        db.plannedWorkoutQueries.plans { id, workoutId, day, _ -> PlannedWorkout(id, WorkoutId(workoutId), LocalDate.parse(day)) }
            .asFlow()
            .mapToList(Dispatchers.IO)

    /** Puts a workout on [day] and returns the plan's id. */
    fun planWorkout(workoutId: WorkoutId, day: LocalDate): String {
        val id = Ids.newUuidV7()
        db.plannedWorkoutQueries.insertPlan(id, workoutId.value, day.toString(), clock.now().toEpochMilliseconds())
        return id
    }

    fun movePlan(planId: String, day: LocalDate) = db.plannedWorkoutQueries.movePlan(day.toString(), planId)

    fun removePlan(planId: String) = db.plannedWorkoutQueries.deletePlan(planId)

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

    /** [title] and [type] are kept as they were at the start, for the history. */
    fun startExecution(execution: Execution, title: String? = null, type: WorkoutType? = null) {
        db.executionQueries.insertExecution(
            id = execution.id.value,
            source = execution.source.name,
            workout_id = execution.workout?.workoutId?.value,
            workout_version = execution.workout?.version?.toLong(),
            started_at = execution.startedAt.toEpochMilliseconds(),
            ended_at = execution.endedAt?.toEpochMilliseconds(),
            title = title,
            workout_type = type?.name,
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

    /** Sessions that ended, newest first. Emits again when one is added, summed up or deleted. */
    fun finishedSessions(): Flow<List<StoredSession>> =
        db.executionQueries.finished().asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map { row ->
                StoredSession(
                    execution = Execution(
                        id = ExecutionId(row.id),
                        source = ExecutionSource.valueOf(row.source),
                        startedAt = Instant.fromEpochMilliseconds(row.started_at),
                        endedAt = row.ended_at?.let(Instant::fromEpochMilliseconds),
                        workout = row.workout_id?.let { WorkoutRef(WorkoutId(it), row.workout_version?.toInt() ?: 1) },
                    ),
                    title = row.title,
                    type = row.workout_type?.let { name -> WorkoutType.entries.firstOrNull { it.name == name } },
                    totals = row.summary?.let { runCatching { json.decodeFromString(SessionTotals.serializer(), it) }.getOrNull() },
                )
            }
        }

    fun saveTotals(executionId: String, totals: SessionTotals) =
        db.executionQueries.setSummary(json.encodeToString(SessionTotals.serializer(), totals), executionId)

    /** Fills in what sessions from before the history did not keep. */
    fun saveDetails(executionId: String, title: String?, type: WorkoutType?, totals: SessionTotals) =
        db.executionQueries.setDetails(title, type?.name, json.encodeToString(SessionTotals.serializer(), totals), executionId)

    fun deleteSession(executionId: String) = db.transaction {
        db.executionQueries.deleteEvents(executionId)
        db.executionQueries.deleteExecution(executionId)
    }

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

/** A finished session as stored: [totals] is null until it has been summed up once. */
data class StoredSession(val execution: Execution, val title: String?, val type: WorkoutType?, val totals: SessionTotals?)

/** A workout put on a [day]. */
data class PlannedWorkout(val id: String, val workoutId: WorkoutId, val day: LocalDate)
