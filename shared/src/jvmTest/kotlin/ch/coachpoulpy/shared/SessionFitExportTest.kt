package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.export.fit.FitSport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/** A session of the history as a .FIT file: its sport comes from what it holds, and the file is named by its start. */
class SessionFitExportTest {
    private val graph = AppGraph(DriverFactory())
    private val export = SessionFitExport(graph.store, graph.compiler, graph.catalog)
    private val start = Instant.parse("2023-11-13T08:05:00Z")

    private fun step(slug: String) =
        Step.ExerciseStep(StepId(slug), ExerciseSlug(slug), Prescription(listOf(SetPrescription(Target.Reps(10)))))

    /** A finished session of [workout] (or a free recording when null) with [samples] taken along the way. */
    private fun session(id: String, workout: Workout?, source: SessionSource = SessionSource.GUIDED, vararg samples: DeviceMetrics) {
        workout?.let(graph.store::saveWorkout)
        graph.store.startSession(SessionRecord(SessionId(id), source, startedAt = start, workout = workout?.let { WorkoutRef(it.id, it.version) }), workout?.name, workout?.format)
        graph.store.appendEvent(id, SessionEvent.Started(start))
        samples.forEachIndexed { i, metrics -> graph.store.appendEvent(id, SessionEvent.Sample(start + (i * 10).seconds, metrics)) }
        graph.store.appendEvent(id, SessionEvent.Finished(start + 5.minutes))
        graph.store.finishSession(id, start + 5.minutes)
    }

    private fun sportOf(id: String): FitSport {
        val stored = assertNotNull(graph.store.finishedSession(id))
        val workout = stored.record.workout?.let { graph.store.workout(it.workoutId, it.version) }
        return export.sport(stored, graph.store.events(id), sessionTimeline(stored, workout, graph.compiler), workout)
    }

    @Test
    fun walksAndRunsAreRunningOnATreadmillWhenItSentItsSpeed() {
        val walk = workoutOf("walk", "Walk", listOf(step("walk"), step("easy-run")), WorkoutFormat.SETS)
        session("free", null, SessionSource.DEVICE_ONLY, DeviceMetrics(speedKmh = 5.0, distanceMeters = 0.0), DeviceMetrics(speedKmh = 5.0, distanceMeters = 40.0))
        session("outdoor", walk)
        session("heart-rate-only", walk, SessionSource.GUIDED, DeviceMetrics(heartRateBpm = 120))

        assertEquals(FitSport.TREADMILL_RUNNING, sportOf("free"))
        assertEquals(FitSport.RUNNING, sportOf("outdoor"))
        assertEquals(FitSport.RUNNING, sportOf("heart-rate-only"))
    }

    @Test
    fun anyOtherSessionFollowsItsFormat() {
        session("sets", workoutOf("sets", "Legs", listOf(step("walk"), step("push-up")), WorkoutFormat.SETS))
        session("circuit", workoutOf("circuit", "Circuit", listOf(step("push-up")), WorkoutFormat.CIRCUIT))

        assertEquals(FitSport.STRENGTH_TRAINING, sportOf("sets"))
        assertEquals(FitSport.CARDIO_TRAINING, sportOf("circuit"))
    }

    @Test
    fun theFileIsNamedByWhenTheSessionStarted() {
        session("sets", workoutOf("sets", "Legs", listOf(step("push-up")), WorkoutFormat.SETS))

        val file = assertNotNull(export.file("sets", TimeZone.of("Europe/Zurich")))

        assertEquals("coach-poulpy-2023-11-13-0905.fit", file.fileName)
        assertEquals(".FIT", file.bytes.copyOfRange(8, 12).decodeToString())
        assertNull(export.file("unknown", TimeZone.UTC))
    }
}
