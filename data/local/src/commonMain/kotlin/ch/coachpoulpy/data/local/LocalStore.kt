package ch.coachpoulpy.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMeasurementChange
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.PlannedWorkoutChange
import ch.coachpoulpy.core.model.PlannedWorkoutDeletedChange
import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionDeletedChange
import ch.coachpoulpy.core.model.SessionEventsChange
import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncedEvent
import ch.coachpoulpy.core.model.WorkoutDeletedChange
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.db.CoachPoulpyDatabase
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/** The phone's database: the source of truth for a guest. */
class LocalStore(driverFactory: DriverFactory, private val clock: Clock = Clock.System) {
    private val db = CoachPoulpyDatabase(driverFactory.create())
    private val json = Json { ignoreUnknownKeys = true }
    private val slugList = ListSerializer(ExerciseSlug.serializer())

    fun saveWorkout(workout: Workout) = db.transaction {
        val base = db.workoutQueries.latestById(workout.id.value).executeAsOneOrNull()?.version?.toInt()
        val now = clock.now()
        db.workoutQueries.insert(
            id = workout.id.value,
            version = workout.version.toLong(),
            format = workout.format.name,
            name = workout.name,
            content = json.encodeToString(Workout.serializer(), workout),
            created_at = now.toEpochMilliseconds(),
        )
        record { WorkoutVersionChange(workout, base, now) }
    }

    fun workouts(): Flow<List<Workout>> =
        db.workoutQueries.latestVersions { _, _, _, _, content, _ -> json.decodeFromString(Workout.serializer(), content) }
            .asFlow()
            .mapToList(Dispatchers.IO)

    /** Every saved workout's latest version with when it was saved, read once. */
    fun savedWorkouts(): List<SavedWorkout> =
        db.workoutQueries.latestVersions().executeAsList().map {
            SavedWorkout(json.decodeFromString(Workout.serializer(), it.content), Instant.fromEpochMilliseconds(it.created_at))
        }

    fun latestWorkout(id: WorkoutId): Workout? =
        db.workoutQueries.latestById(id.value).executeAsOneOrNull()
            ?.let { json.decodeFromString(Workout.serializer(), it.content) }

    fun workout(id: WorkoutId, version: Int): Workout? =
        db.workoutQueries.byIdAndVersion(id.value, version.toLong()).executeAsOneOrNull()
            ?.let { json.decodeFromString(Workout.serializer(), it.content) }

    /** Its plans go with it: a day never points at a workout that is gone. */
    fun deleteWorkout(id: WorkoutId) = db.transaction {
        val latest = db.workoutQueries.latestById(id.value).executeAsOneOrNull()
        val now = clock.now()
        db.plannedWorkoutQueries.planIdsOfWorkout(id.value).executeAsList().forEach { planId ->
            record { PlannedWorkoutDeletedChange(planId, now) }
        }
        db.plannedWorkoutQueries.deletePlansOfWorkout(id.value)
        db.workoutQueries.deleteWorkout(id.value)
        if (latest != null) record { WorkoutDeletedChange(id, latest.version.toInt(), now) }
    }

    /** Every planned workout, by day then in the order they were planned. */
    fun plannedWorkouts(): Flow<List<PlannedWorkout>> =
        db.plannedWorkoutQueries.plans { id, workoutId, day, _ -> PlannedWorkout(id, WorkoutId(workoutId), LocalDate.parse(day)) }
            .asFlow()
            .mapToList(Dispatchers.IO)

    /** Puts a workout on [day] and returns the plan's id. */
    fun planWorkout(workoutId: WorkoutId, day: LocalDate): String = db.transactionWithResult {
        val id = Ids.newUuidV7()
        val now = clock.now()
        db.plannedWorkoutQueries.insertPlan(id, workoutId.value, day.toString(), now.toEpochMilliseconds())
        record { PlannedWorkoutChange(id, workoutId, day, createdAt = now, dayChangedAt = now) }
        id
    }

    fun movePlan(planId: String, day: LocalDate) = db.transaction {
        db.plannedWorkoutQueries.movePlan(day.toString(), planId)
        val plan = db.plannedWorkoutQueries.planById(planId).executeAsOneOrNull() ?: return@transaction
        record {
            PlannedWorkoutChange(planId, WorkoutId(plan.workout_id), day, Instant.fromEpochMilliseconds(plan.created_at), clock.now())
        }
    }

    fun removePlan(planId: String) = db.transaction {
        if (db.plannedWorkoutQueries.planById(planId).executeAsOneOrNull() == null) return@transaction
        db.plannedWorkoutQueries.deletePlan(planId)
        record { PlannedWorkoutDeletedChange(planId, clock.now()) }
    }

    /** Removes the plans [remove] and puts the workout on each of [add], all at once. */
    fun replacePlans(workoutId: WorkoutId, add: List<LocalDate>, remove: List<String>) = db.transaction {
        remove.forEach { removePlan(it) }
        add.forEach { planWorkout(workoutId, it) }
    }

    /** The catalog version stored on the phone, or null before the first seed. */
    /** Adds a body measurement, or corrects the one already logged for that day (ADR 0017). */
    fun logBodyMeasurement(measurement: BodyMeasurement) = db.transaction {
        val queries = db.bodyMeasurementQueries
        val metric = measurement.metric.name
        val day = measurement.day.toString()
        if (queries.measurementOnDay(metric, day).executeAsOneOrNull() != null) {
            queries.correctMeasurement(measurement.value, metric, day)
        } else {
            queries.insertMeasurement(Ids.newUuidV7(), metric, day, measurement.value, clock.now().toEpochMilliseconds())
        }
        record { BodyMeasurementChange(measurement.metric, measurement.day, measurement.value, clock.now()) }
    }

    fun bodyMeasurements(metric: BodyMetric): Flow<List<BodyMeasurement>> =
        db.bodyMeasurementQueries.measurements(metric.name) { _, metricName, day, value, _ ->
            BodyMeasurement(BodyMetric.valueOf(metricName), LocalDate.parse(day), value)
        }.asFlow().mapToList(Dispatchers.IO)

    fun deleteBodyMeasurement(metric: BodyMetric, day: LocalDate) = db.transaction {
        if (db.bodyMeasurementQueries.measurementOnDay(metric.name, day.toString()).executeAsOneOrNull() == null) return@transaction
        db.bodyMeasurementQueries.deleteMeasurement(metric.name, day.toString())
        record { BodyMeasurementChange(metric, day, null, clock.now()) }
    }

    fun catalogVersion(): Int? = db.catalogEntryQueries.version().executeAsOneOrNull()?.toInt()

    fun catalogExercises(): List<Exercise> =
        db.catalogEntryQueries.all().executeAsList().map { json.decodeFromString(Exercise.serializer(), it) }

    /** The catalog's variation groups, each easiest to hardest, in catalog order. */
    fun catalogVariationGroups(): List<List<ExerciseSlug>> =
        db.catalogEntryQueries.variationGroups().executeAsList().map { json.decodeFromString(slugList, it) }

    /**
     * Stores a whole catalog version. An entry the new version leaves out is kept as
     * retired rather than deleted, so workouts that use it still open. Variation groups
     * are replaced by the new version's.
     */
    fun replaceCatalog(
        version: Int,
        exercises: List<Exercise>,
        variationGroups: List<List<ExerciseSlug>> = emptyList(),
    ) = db.transaction {
        val incoming = exercises.map { it.slug }.toSet()
        val dropped = catalogExercises().filter { it.slug !in incoming }.map { it.copy(retired = true) }
        (exercises + dropped).forEach {
            db.catalogEntryQueries.upsert(it.slug.value, json.encodeToString(Exercise.serializer(), it))
        }
        db.catalogEntryQueries.clearVariationGroups()
        variationGroups.forEachIndexed { position, group ->
            db.catalogEntryQueries.insertVariationGroup(position.toLong(), json.encodeToString(slugList, group))
        }
        db.catalogEntryQueries.setVersion(version.toLong())
    }

    /** [title] and [format] are kept as they were at the start, for the history. */
    fun startSession(record: SessionRecord, title: String? = null, format: WorkoutFormat? = null) = db.transaction {
        db.sessionQueries.insertSession(
            id = record.id.value,
            source = record.source.name,
            workout_id = record.workout?.workoutId?.value,
            workout_version = record.workout?.version?.toLong(),
            started_at = record.startedAt.toEpochMilliseconds(),
            ended_at = record.endedAt?.toEpochMilliseconds(),
            title = title,
            workout_type = format?.name,
        )
        recordHeader(record.id.value)
    }

    /** Written at once for every session action, so a killed app can replay it. */
    fun appendEvent(sessionId: String, event: SessionEvent) = db.transaction {
        val payload = json.encodeToString(SessionEvent.serializer(), event)
        db.sessionQueries.appendEvent(sessionId = sessionId, at = event.at.toEpochMilliseconds(), payload = payload)
        record {
            val seq = db.sessionQueries.lastSeq(sessionId).executeAsOne()
            SessionEventsChange(SessionId(sessionId), listOf(SyncedEvent(seq, event.at, json.parseToJsonElement(payload).jsonObject)))
        }
    }

    fun finishSession(sessionId: String, endedAt: Instant) = db.transaction {
        db.sessionQueries.finishSession(endedAt.toEpochMilliseconds(), sessionId)
        recordHeader(sessionId)
    }

    /** Sessions that ended, newest first. Emits again when one is added, summed up or deleted. */
    fun finishedSessions(): Flow<List<StoredSession>> =
        db.sessionQueries.finished().asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map { storedSession(it.id, it.source, it.started_at, it.ended_at, it.workout_id, it.workout_version, it.title, it.workout_type, it.summary) }
        }

    /** One finished session, read once; null when there is none with this id. */
    fun finishedSession(sessionId: String): StoredSession? =
        db.sessionQueries.finished().executeAsList().firstOrNull { it.id == sessionId }
            ?.let { storedSession(it.id, it.source, it.started_at, it.ended_at, it.workout_id, it.workout_version, it.title, it.workout_type, it.summary) }

    private fun storedSession(
        id: String, source: String, startedAt: Long, endedAt: Long?, workoutId: String?, workoutVersion: Long?,
        title: String?, workoutType: String?, summary: String?,
    ) = StoredSession(
        record = SessionRecord(
            id = SessionId(id),
            source = SessionSource.valueOf(source),
            startedAt = Instant.fromEpochMilliseconds(startedAt),
            endedAt = endedAt?.let(Instant::fromEpochMilliseconds),
            workout = workoutId?.let { WorkoutRef(WorkoutId(it), workoutVersion?.toInt() ?: 1) },
        ),
        title = title,
        format = workoutType?.let { name -> WorkoutFormat.entries.firstOrNull { it.name == name } },
        totals = summary?.let { runCatching { json.decodeFromString(SessionTotals.serializer(), it) }.getOrNull() },
    )

    /** The workout version each finished session followed, with when it started, newest first, read once. */
    fun finishedWorkoutRefs(): List<Pair<WorkoutRef, Instant>> =
        db.sessionQueries.finished().executeAsList().mapNotNull { row ->
            val id = row.workout_id ?: return@mapNotNull null
            WorkoutRef(WorkoutId(id), row.workout_version?.toInt() ?: 1) to Instant.fromEpochMilliseconds(row.started_at)
        }

    fun saveTotals(sessionId: String, totals: SessionTotals) = db.transaction {
        db.sessionQueries.setSummary(json.encodeToString(SessionTotals.serializer(), totals), sessionId)
        recordHeader(sessionId)
    }

    /** Fills in what sessions from before the history did not keep. */
    fun saveDetails(sessionId: String, title: String?, format: WorkoutFormat?, totals: SessionTotals) = db.transaction {
        db.sessionQueries.setDetails(title, format?.name, json.encodeToString(SessionTotals.serializer(), totals), sessionId)
        recordHeader(sessionId)
    }

    fun deleteSession(sessionId: String) = db.transaction {
        val existed = db.sessionQueries.byId(sessionId).executeAsOneOrNull() != null
        db.sessionQueries.deleteEvents(sessionId)
        db.sessionQueries.deleteSession(sessionId)
        if (existed) record { SessionDeletedChange(SessionId(sessionId), clock.now()) }
    }

    /** The session left open when the app was closed or killed, if any. */
    fun unfinishedRecord(): SessionRecord? = unfinishedSession()?.record

    /** [unfinishedRecord] with the title it started with. */
    fun unfinishedSession(): StoredSession? = db.sessionQueries.unfinished().executeAsOneOrNull()?.let { row ->
        StoredSession(
            record = SessionRecord(
                id = SessionId(row.id),
                source = SessionSource.valueOf(row.source),
                startedAt = Instant.fromEpochMilliseconds(row.started_at),
                workout = row.workout_id?.let { WorkoutRef(WorkoutId(it), row.workout_version?.toInt() ?: 1) },
            ),
            title = row.title,
            format = row.workout_type?.let { name -> WorkoutFormat.entries.firstOrNull { it.name == name } },
            totals = null,
        )
    }

    /** When each finished session ended, oldest first, updated as sessions finish. */
    fun finishedSessionEnds(): Flow<List<Instant>> =
        db.sessionQueries.finishedSessionEnds()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { ends -> ends.map(Instant::fromEpochMilliseconds) }

    /** [finishedSessionEnds] read once. */
    fun finishedSessionEndsNow(): List<Instant> =
        db.sessionQueries.finishedSessionEnds().executeAsList().map(Instant::fromEpochMilliseconds)

    fun events(sessionId: String): List<SessionEvent> =
        db.sessionQueries.events(sessionId).executeAsList()
            .map { json.decodeFromString(SessionEvent.serializer(), it) }

    // Sync (docs/accounts.md): while the phone is signed in with a confirmed email, every write
    // adds its change to the outbox in the same transaction. A guest's writes add nothing.

    /** Whether writes add their change to the outbox. */
    fun recordingChanges(): Boolean = db.syncQueries.recording().executeAsOneOrNull() == 1L

    /**
     * Starts adding each write to the outbox, which is first filled with the whole database,
     * every id kept: every workout version, plan, body measurement and session with its events,
     * then [settings], the profile fields, units and language that follow the account.
     */
    fun startRecordingChanges(settings: Map<String, String>) = db.transaction {
        db.syncQueries.clearOutbox()
        db.syncQueries.setRecording(1)
        val now = clock.now()
        var previous: Pair<String, Int>? = null
        db.workoutQueries.allVersions().executeAsList().forEach { row ->
            val base = previous?.takeIf { it.first == row.id }?.second
            add(WorkoutVersionChange(json.decodeFromString(Workout.serializer(), row.content), base, Instant.fromEpochMilliseconds(row.created_at)))
            previous = row.id to row.version.toInt()
        }
        db.plannedWorkoutQueries.plans().executeAsList().forEach { plan ->
            val createdAt = Instant.fromEpochMilliseconds(plan.created_at)
            add(PlannedWorkoutChange(plan.id, WorkoutId(plan.workout_id), LocalDate.parse(plan.day), createdAt, createdAt))
        }
        db.bodyMeasurementQueries.allMeasurements().executeAsList().forEach { row ->
            add(BodyMeasurementChange(BodyMetric.valueOf(row.metric), LocalDate.parse(row.day), row.value_, Instant.fromEpochMilliseconds(row.created_at)))
        }
        db.sessionQueries.allSessions().executeAsList().forEach { session ->
            add(headerChange(session))
            db.sessionQueries.storedEvents(session.id).executeAsList()
                .map { SyncedEvent(it.seq, Instant.fromEpochMilliseconds(it.at), json.parseToJsonElement(it.payload).jsonObject) }
                .chunked(EVENTS_PER_CHANGE)
                .forEach { add(SessionEventsChange(SessionId(session.id), it)) }
        }
        settings.filterKeys(SettingChange::follows).forEach { (key, value) -> add(SettingChange(key, value, now)) }
    }

    /** Stops adding writes to the outbox and empties it, as on sign-out. */
    fun stopRecordingChanges() = db.transaction {
        db.syncQueries.setRecording(0)
        db.syncQueries.clearOutbox()
    }

    /** A setting kept outside this database changed; added when it follows the account ([SettingChange.follows]). */
    fun recordSetting(key: String, value: String?) {
        if (SettingChange.follows(key)) record { SettingChange(key, value, clock.now()) }
    }

    /** The oldest [limit] changes waiting to be pushed, with their place in the outbox. */
    fun outbox(limit: Int): List<OutboxEntry> =
        db.syncQueries.outbox(limit.toLong()).executeAsList()
            .map { OutboxEntry(it.seq, json.decodeFromString(SyncChange.serializer(), it.change_json)) }

    /** Removes the changes pushed, up to and including [seq]. */
    fun removeFromOutbox(upToSeq: Long) = db.syncQueries.removeUpTo(upToSeq)

    private fun record(change: () -> SyncChange) {
        if (recordingChanges()) add(change())
    }

    private fun add(change: SyncChange) = db.syncQueries.addChange(json.encodeToString(SyncChange.serializer(), change))

    private fun recordHeader(sessionId: String) {
        if (!recordingChanges()) return
        db.sessionQueries.byId(sessionId).executeAsOneOrNull()?.let { add(headerChange(it)) }
    }

    private fun headerChange(row: Session) = SessionChange(
        id = SessionId(row.id),
        source = SessionSource.valueOf(row.source),
        startedAt = Instant.fromEpochMilliseconds(row.started_at),
        endedAt = row.ended_at?.let(Instant::fromEpochMilliseconds),
        workout = row.workout_id?.let { WorkoutRef(WorkoutId(it), row.workout_version?.toInt() ?: 1) },
        title = row.title,
        format = row.workout_type?.let { name -> WorkoutFormat.entries.firstOrNull { it.name == name } },
        totals = row.summary?.let { runCatching { json.parseToJsonElement(it).jsonObject }.getOrNull() },
    )

    private companion object {
        /** A long Live session holds thousands of samples: its events are sent in several changes. */
        const val EVENTS_PER_CHANGE = 200
    }
}

/** A change waiting in the outbox, at [seq]. */
data class OutboxEntry(val seq: Long, val change: SyncChange)

/** A finished session as stored: [totals] is null until it has been summed up once. */
data class StoredSession(val record: SessionRecord, val title: String?, val format: WorkoutFormat?, val totals: SessionTotals?)

/** A workout's latest version and when it was saved. */
data class SavedWorkout(val workout: Workout, val savedAt: Instant)

/** A workout put on a [day]. */
data class PlannedWorkout(val id: String, val workoutId: WorkoutId, val day: LocalDate)
