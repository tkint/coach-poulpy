package ch.coachpoulpy.types.treadmill

import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TreadmillRecorderTest {
    private val t0 = Instant.fromEpochSeconds(1_700_000_000)
    private val compiler = TimelineCompiler(WorkoutTypeRegistry(listOf(TreadmillModule)))

    private fun block(id: String, target: Target, kmh: Double) = Step.ExerciseStep(
        id = StepId(id),
        exercise = TreadmillModule.treadmillRun,
        prescription = Prescription(listOf(SetPrescription(target, intensity = Intensity.Speed(kmh)))),
    )

    /** 60 s warm-up at 6 km/h, then 100 m at 12 km/h. */
    private val workout = Workout(
        id = WorkoutId("w"),
        version = 1,
        name = "Intervals",
        type = WorkoutType.TREADMILL,
        steps = listOf(block("warm-up", Target.Time(60.seconds), 6.0), block("fast", Target.Distance(100.0), 12.0)),
    )

    @Test
    fun followsAPlannedWorkoutAndChecksIt() {
        val timeline = compiler.compile(workout)
        val session = Session(timeline)
        val recorder = TreadmillRecorder(session)
        session.start(t0)

        // Warm-up: readings every second for 60 s, the last 20 too slow. Treadmills count whole metres.
        var distance = 0.0
        for (s in 0 until 60) {
            val speed = if (s < 40) 6.0 else 5.0
            recorder.onReading(t0 + s.seconds, DeviceMetrics(speedKmh = speed, distanceMeters = distance))
            distance += 2.0
        }

        // Fast block: ends by itself once the counter is 100 m past the last warm-up reading (118 m).
        var s = 60
        while (session.state(t0 + s.seconds).status != SessionState.Status.FINISHED) {
            recorder.onReading(t0 + s.seconds, DeviceMetrics(speedKmh = 12.0, distanceMeters = distance))
            distance += 10.0
            s++
            check(s < 200) { "Distance block never completed" }
        }
        assertEquals(71, s) // the reading at 70 s shows 220 m

        val summary = TreadmillSummary.of(timeline, session.events)
        assertEquals(70.seconds, summary.duration)
        val (warmUp, fast) = summary.phases
        assertEquals(60.seconds, warmUp.duration)
        assertEquals(6.0, warmUp.targetSpeedKmh)
        assertEquals(40.0 / 60, warmUp.timeOnTargetSpeed)
        assertEquals(10.seconds, fast.duration)
        assertEquals(118.0, warmUp.distanceMeters)
        assertEquals(102.0, fast.distanceMeters)
        assertEquals(220.0, summary.distanceMeters)
        assertEquals(12.0, fast.averageSpeedKmh)
        assertEquals(1.0, fast.timeOnTargetSpeed)
    }

    @Test
    fun freeRunRecordsUntilStoppedAndIgnoresPauses() {
        val session = Session(TreadmillModule.freeRun)
        val recorder = TreadmillRecorder(session)
        session.start(t0)
        recorder.onReading(t0, DeviceMetrics(speedKmh = 8.0, distanceMeters = 1_000.0, heartRateBpm = 120, energyKcal = 50))
        session.pause(t0 + 5.seconds)
        assertEquals(emptyList(), recorder.onReading(t0 + 6.seconds, DeviceMetrics(speedKmh = 0.0)))
        session.resume(t0 + 10.seconds)
        recorder.onReading(t0 + 20.seconds, DeviceMetrics(speedKmh = 10.0, distanceMeters = 1_050.0, heartRateBpm = 140, energyKcal = 54))
        session.completeCurrent(t0 + 30.seconds)

        assertEquals(SessionState.Status.FINISHED, session.state(t0 + 30.seconds).status)
        val summary = TreadmillSummary.of(TreadmillModule.freeRun, session.events)
        assertEquals(30.seconds, summary.duration)
        assertEquals(50.0, summary.distanceMeters)
        assertEquals(9.0, summary.averageSpeedKmh)
        assertEquals(130, summary.averageHeartRateBpm)
        assertEquals(140, summary.maxHeartRateBpm)
        assertEquals(4, summary.energyKcal)
        assertEquals(2, session.events.count { it is SessionEvent.Sample })
        assertEquals(30.seconds, summary.phases.single().duration)
    }
}
