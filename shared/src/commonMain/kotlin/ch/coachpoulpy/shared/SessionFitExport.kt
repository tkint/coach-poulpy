package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.StoredSession
import ch.coachpoulpy.export.fit.FitActivity
import ch.coachpoulpy.export.fit.FitSport
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime

/** A session written as a .FIT file to send to Garmin Connect or Strava: [fileName] ends in `.fit`. */
class SessionFitFile(val fileName: String, val bytes: ByteArray)

/**
 * The .FIT file of a session of the history (ADR 0011), rebuilt from what the phone kept. Its FIT sport
 * follows what the session holds, not a workout type.
 */
internal class SessionFitExport(private val store: LocalStore, private val compiler: TimelineCompiler, private val catalog: Catalog) {
    fun file(sessionId: String, zone: TimeZone): SessionFitFile? {
        val stored = store.finishedSession(sessionId) ?: return null
        val events = store.events(sessionId)
        val workout = stored.record.workout?.let { store.workout(it.workoutId, it.version) }
        val timeline = sessionTimeline(stored, workout, compiler)
        val start = stored.record.startedAt
        val bytes = FitActivity.encode(
            events = events,
            timeline = timeline,
            workout = workout,
            sport = sport(stored, events, timeline, workout),
            serialNumber = sessionId.hashCode().toLong() and 0xFFFFFFFFL,
            utcOffsetSeconds = zone.offsetAt(start).totalSeconds,
        ) ?: return null
        val local = start.toLocalDateTime(zone)
        val time = "${local.hour.toString().padStart(2, '0')}${local.minute.toString().padStart(2, '0')}"
        return SessionFitFile("coach-poulpy-${local.date}-$time.fit", bytes)
    }

    /**
     * Only walks and runs are running, on a treadmill when a machine sent its speed or distance; any other
     * session follows its workout's format. A session whose plan is gone is running when a treadmill sent readings.
     */
    fun sport(stored: StoredSession, events: List<SessionEvent>, timeline: Timeline?, workout: Workout?): FitSport {
        val treadmill = events.any { it is SessionEvent.Sample && (it.metrics.speedKmh != null || it.metrics.distanceMeters != null) }
        val work = timeline?.phases.orEmpty().filterIsInstance<Phase.Work>()
        val walksAndRuns = if (timeline == null) treadmill else work.isNotEmpty() && work.all { it.exercise.isWalkOrRun(catalog) }
        return FitSport.of(stored.format ?: workout?.format ?: WorkoutFormat.CIRCUIT, walksAndRuns, treadmill)
    }
}
