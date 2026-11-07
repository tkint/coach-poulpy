package ch.coachpoulpy.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import ch.coachpoulpy.core.model.BodyMeasurementChange
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.PlannedWorkoutChange
import ch.coachpoulpy.core.model.PlannedWorkoutDeletedChange
import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionDeletedChange
import ch.coachpoulpy.core.model.SessionEventsChange
import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutDeletedChange
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutVersionChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

// Sync, the phone's side (docs/accounts.md, Sync): what a pull brings is applied here, and a workout
// edited on two phones is forked here (ADR 0025). Nothing applied is added to the outbox.

/** Where the last pull stopped, or null before the first. */
fun LocalStore.syncCursor(): String? = db.syncQueries.cursor().executeAsOneOrNull()?.cursor

/** How many changes wait in the outbox, updated as writes add them and pushes remove them. */
fun LocalStore.outboxSize(): Flow<Long> = db.syncQueries.outboxSize().asFlow().mapToOne(Dispatchers.IO)

/** Removes one change the server refused for good, so the ones after it can go. */
fun LocalStore.dropFromOutbox(seq: Long) = db.syncQueries.removeChange(seq)

/**
 * Applies one pulled [page] and keeps its cursor, all at once; applying a page twice changes nothing.
 * A small field with a change of its own still in the outbox keeps the phone's value: the push that
 * follows sends it, and the server keeps the later write. [setting] receives the pulled settings,
 * kept outside this database. A workout version that clashes with one made here forks the phone's
 * edits into a workout named by [forkName].
 */
fun LocalStore.applyPulled(page: SyncPage, forkName: (Workout) -> String, setting: (SettingChange) -> Unit) = db.transaction {
    val pending = pendingKeys()
    page.changes.forEach { change ->
        if (change.key() in pending) return@forEach
        apply(change, forkName, setting)
    }
    db.syncQueries.ensureState()
    db.syncQueries.setCursor(page.cursor)
}

/**
 * Forks each of [workouts] that still has versions waiting in the outbox, as after a push refused
 * with stale-base once the pull that followed did not settle it.
 */
fun LocalStore.forkUnsent(workouts: List<WorkoutId>, forkName: (Workout) -> String) = db.transaction {
    val unsent = wholeOutbox().map { it.second }.filterIsInstance<WorkoutVersionChange>()
    workouts.forEach { id ->
        val from = unsent.filter { it.workout.id == id }.minOfOrNull { it.workout.version } ?: return@forEach
        fork(id, from, forkName)
    }
}

/**
 * Clears everything that follows the account from the phone: workouts, plans, the body log and
 * sessions, with the outbox and cursor. The settings kept outside this database are the caller's.
 */
fun LocalStore.clearAccountData() = db.transaction {
    db.workoutQueries.deleteAllWorkouts()
    db.plannedWorkoutQueries.deleteAllPlans()
    db.bodyMeasurementQueries.deleteAllMeasurements()
    db.sessionQueries.deleteAllEvents()
    db.sessionQueries.deleteAllSessions()
    stopRecordingChanges()
}

private fun LocalStore.apply(change: SyncChange, forkName: (Workout) -> String, setting: (SettingChange) -> Unit) {
    when (change) {
        is SessionChange -> {
            val source = change.source.name
            val workoutId = change.workout?.workoutId?.value
            val version = change.workout?.version?.toLong()
            val startedAt = change.startedAt.toEpochMilliseconds()
            val endedAt = change.endedAt?.toEpochMilliseconds()
            val format = change.format?.name
            val totals = change.totals?.toString()
            db.sessionQueries.updateHeader(source, workoutId, version, startedAt, endedAt, change.title, format, totals, change.id.value)
            db.sessionQueries.insertHeader(change.id.value, source, workoutId, version, startedAt, endedAt, change.title, format, totals)
        }
        is SessionEventsChange -> change.events.forEach {
            db.sessionQueries.insertEventIfMissing(change.sessionId.value, it.seq, it.at.toEpochMilliseconds(), it.payload.toString())
        }
        is SessionDeletedChange -> {
            db.sessionQueries.deleteEvents(change.sessionId.value)
            db.sessionQueries.deleteSession(change.sessionId.value)
        }
        is WorkoutVersionChange -> applyVersion(change, forkName)
        is WorkoutDeletedChange -> {
            // The edit wins: a version made after the one deleted keeps the workout.
            val latest = db.workoutQueries.latestById(change.workoutId.value).executeAsOneOrNull() ?: return
            if (latest.version > change.version) return
            db.plannedWorkoutQueries.deletePlansOfWorkout(change.workoutId.value)
            db.workoutQueries.deleteWorkout(change.workoutId.value)
        }
        is PlannedWorkoutChange -> db.plannedWorkoutQueries.upsertPlan(
            change.id, change.workoutId.value, change.day.toString(), change.createdAt.toEpochMilliseconds(),
        )
        is PlannedWorkoutDeletedChange -> db.plannedWorkoutQueries.deletePlan(change.id)
        is BodyMeasurementChange -> {
            val metric = change.metric.name
            val day = change.day.toString()
            val queries = db.bodyMeasurementQueries
            when {
                change.value == null -> queries.deleteMeasurement(metric, day)
                queries.measurementOnDay(metric, day).executeAsOneOrNull() != null -> queries.correctMeasurement(change.value!!, metric, day)
                else -> queries.insertMeasurement(Ids.newUuidV7(), metric, day, change.value!!, change.changedAt.toEpochMilliseconds())
            }
        }
        is SettingChange -> setting(change)
    }
}

private fun LocalStore.applyVersion(change: WorkoutVersionChange, forkName: (Workout) -> String) {
    val workout = change.workout
    val local = db.workoutQueries.byIdAndVersion(workout.id.value, workout.version.toLong()).executeAsOneOrNull()
    if (local != null) {
        // The phone's own version coming back changes nothing; another phone's, made from the same one, forks ours.
        if (json.decodeFromString(Workout.serializer(), local.content) == workout) return
        fork(workout.id, workout.version, forkName)
    }
    db.workoutQueries.insertIfMissing(
        workout.id.value, workout.version.toLong(), workout.format.name, workout.name,
        json.encodeToString(Workout.serializer(), workout), change.createdAt.toEpochMilliseconds(),
    )
}

/**
 * Turns the phone's versions of [id] from [from] on into a new workout at version 1, with the
 * latest of them, named by [forkName]. The phone's sessions that followed them and its plans still
 * to push now point at the copy, and the outbox pushes the copy instead (docs/accounts.md, Sync).
 */
private fun LocalStore.fork(id: WorkoutId, from: Int, forkName: (Workout) -> String) {
    val mine = db.workoutQueries.versionsFrom(id.value, from.toLong()).executeAsList()
    if (mine.isEmpty()) return
    val latest = json.decodeFromString(Workout.serializer(), mine.last().content)
    val now = clock.now()
    val copy = latest.copy(id = WorkoutId(Ids.newUuidV7()), version = 1, name = forkName(latest))
    db.workoutQueries.insert(
        copy.id.value, 1, copy.format.name, copy.name, json.encodeToString(Workout.serializer(), copy), now.toEpochMilliseconds(),
    )
    db.sessionQueries.repointSessions(copyId = copy.id.value, workoutId = id.value, fromVersion = from.toLong())
    db.workoutQueries.deleteVersionsFrom(id.value, from.toLong())

    val copyVersion = WorkoutVersionChange(copy, null, now)
    var placed = false
    wholeOutbox().forEach { (seq, change) ->
        val rewritten: SyncChange? = when {
            change is WorkoutVersionChange && change.workout.id == id && change.workout.version >= from ->
                if (placed) null else copyVersion.also { placed = true }
            change is SessionChange && change.workout?.workoutId == id && change.workout!!.version >= from ->
                change.copy(workout = WorkoutRef(copy.id, 1))
            change is PlannedWorkoutChange && change.workoutId == id -> {
                db.plannedWorkoutQueries.setPlanWorkout(copy.id.value, change.id)
                change.copy(workoutId = copy.id)
            }
            else -> change
        }
        when {
            rewritten == null -> db.syncQueries.removeChange(seq)
            rewritten != change -> db.syncQueries.rewriteChange(json.encodeToString(SyncChange.serializer(), rewritten), seq)
        }
    }
    if (!placed) add(copyVersion)
}

private fun LocalStore.wholeOutbox(): List<Pair<Long, SyncChange>> =
    db.syncQueries.wholeOutbox().executeAsList().map { it.seq to json.decodeFromString(SyncChange.serializer(), it.change_json) }

private fun LocalStore.pendingKeys(): Set<String> = wholeOutbox().mapNotNull { it.second.key() }.toSet()

/** The small field or session header a change writes, which the latest write decides. */
private fun SyncChange.key(): String? = when (this) {
    is SessionChange -> "session:${id.value}"
    is PlannedWorkoutChange -> "plan:$id"
    is PlannedWorkoutDeletedChange -> "plan:$id"
    is BodyMeasurementChange -> "body:${metric.name}:$day"
    is SettingChange -> "setting:$key"
    else -> null
}
