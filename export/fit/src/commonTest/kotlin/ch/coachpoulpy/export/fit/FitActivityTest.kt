package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FitActivityTest {
    private val compiler = TimelineCompiler()
    private val t0 = Instant.parse("2026-10-24T16:05:00Z")
    private val fitT0 = t0.epochSeconds - 631_065_600L

    private fun exercise(id: String, vararg sets: SetPrescription) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(id), Prescription(sets.toList()))

    private fun workout(format: WorkoutFormat, vararg parts: Part) = Workout(WorkoutId("w"), 1, "Test", format, parts.toList())

    private fun part(kind: PartKind, vararg steps: Step) = Part(PartId(kind.name), kind, steps.toList())

    private fun List<DecodedMessage>.of(global: Int) = filter { it.global == global }

    private val strength = workout(
        WorkoutFormat.SETS,
        part(
            PartKind.WORK,
            exercise("goblet-squat", *Array(3) { SetPrescription(Target.Reps(10), load = Load.Kilograms(16.0), restAfter = 60.seconds) }),
            exercise("plank", SetPrescription(Target.Hold(30.seconds))),
        ),
    )

    private fun strengthEvents() = listOf(
        SessionEvent.Started(t0),
        SessionEvent.PhaseCompleted(t0 + 40.seconds, 0),
        SessionEvent.PhaseCompleted(t0 + 100.seconds, 1),
        SessionEvent.PhaseCompleted(t0 + 140.seconds, 2),
        SessionEvent.PhaseCompleted(t0 + 200.seconds, 3),
        SessionEvent.PhaseSkipped(t0 + 210.seconds, 4),
        SessionEvent.PhaseCompleted(t0 + 245.seconds, 5),
        SessionEvent.Finished(t0 + 245.seconds),
    )

    private fun encode(events: List<SessionEvent>, workout: Workout, sport: FitSport, utcOffsetSeconds: Int = 0) =
        decodeFit(assertNotNull(FitActivity.encode(events, compiler.compile(workout), workout, sport, serialNumber = 42, utcOffsetSeconds = utcOffsetSeconds)))

    @Test
    fun aSessionNeverStartedHasNoFile() {
        assertNull(FitActivity.encode(emptyList(), compiler.compile(strength), strength, FitSport.STRENGTH_TRAINING, serialNumber = 1))
    }

    @Test
    fun theFileOpensWithItsIdAndEndsWithTheSessionAndTheActivity() {
        val messages = encode(strengthEvents(), strength, FitSport.STRENGTH_TRAINING, utcOffsetSeconds = 3600)

        val id = messages.first()
        assertEquals(0, id.global)
        assertEquals(4L, id.fields[0])
        assertEquals(255L, id.fields[1])
        assertEquals(42L, id.fields[3])
        assertEquals(fitT0, id.fields[4])
        assertEquals(listOf(18, 34), messages.takeLast(2).map { it.global })
        // The creating device and the sport come right after the id.
        assertEquals(listOf(0, 23, 12), messages.take(3).map { it.global })
        assertEquals(listOf(255L, 42L), messages[1].fields.let { listOf(it[2], it[3]) })
        assertEquals(listOf(10L, 20L), messages[2].fields.let { listOf(it[0], it[1]) })
        val activity = messages.last().fields
        assertEquals(fitT0 + 245, activity[253])
        assertEquals(fitT0 + 245 + 3600, activity[5])
        assertEquals(245_000L, activity[0])
    }

    @Test
    fun strengthSetsCarryTheirRepsAndWeightAndTheRestsBetween() {
        val messages = encode(strengthEvents(), strength, FitSport.STRENGTH_TRAINING)

        val sets = messages.of(225).map { it.fields }
        // Squat, rest, squat, rest; the third squat was skipped; then the plank.
        assertEquals(listOf(1L, 0L, 1L, 0L, 1L), sets.map { it[5] })
        assertEquals(listOf(10L, null, 10L, null, null), sets.map { it[3] })
        assertEquals(listOf(256L, null, 256L, null, null), sets.map { it[4] })
        assertEquals(listOf(1L, null, 1L, null, null), sets.map { it[9] })
        // Garmin Connect and Strava name a set after its exercise category: squat, then plank.
        assertEquals(listOf(28L, null, 28L, null, 19L), sets.map { it[7] })
        assertEquals(listOf(40_000L, 60_000L, 40_000L, 60_000L, 35_000L), sets.map { it[0] })
        assertEquals(listOf(fitT0, fitT0 + 40, fitT0 + 100, fitT0 + 140, fitT0 + 210), sets.map { it[6] })
        assertEquals(listOf(0L, 1L, 2L, 3L, 4L), sets.map { it[10] })

        val session = messages.of(18).single().fields
        assertEquals(10L, session[5])
        assertEquals(20L, session[6])
        assertEquals(245_000L, session[8])
        assertEquals(1L, session[26])
        assertEquals(1, messages.of(19).size)
        // No machine sent readings: one record at the start and one at the end, so the file has a track.
        assertEquals(listOf(fitT0, fitT0 + 245), messages.of(20).map { it.fields[253] })
    }

    @Test
    fun otherSportsWriteNoSets() {
        val messages = encode(strengthEvents(), strength, FitSport.CARDIO_TRAINING)

        assertEquals(0, messages.of(225).size)
        assertEquals(listOf(10L, 26L), messages.of(18).single().fields.let { listOf(it[5], it[6]) })
    }

    private val intervals = workout(
        WorkoutFormat.CIRCUIT,
        part(PartKind.WARM_UP, exercise("walk", SetPrescription(Target.Time(2.minutes), intensity = Intensity.Speed(5.0)))),
        part(PartKind.WORK, exercise("run", SetPrescription(Target.Time(2.minutes), intensity = Intensity.Speed(9.0)))),
    )

    private fun reading(seconds: Int, meters: Double, kmh: Double, bpm: Int, kcal: Int) =
        SessionEvent.Sample(t0 + seconds.seconds, DeviceMetrics(speedKmh = kmh, distanceMeters = meters, heartRateBpm = bpm, energyKcal = kcal, inclinePercent = 1.5, cadenceSpm = 160))

    private fun treadmillEvents() = listOf(
        SessionEvent.Started(t0),
        // The machine's own counters started before the session.
        reading(0, 500.0, 5.0, 100, 40),
        reading(60, 580.0, 5.0, 110, 44),
        SessionEvent.Paused(t0 + 60.seconds),
        SessionEvent.Resumed(t0 + 90.seconds),
        reading(150, 666.0, 5.0, 120, 48),
        SessionEvent.PhaseCompleted(t0 + 150.seconds, 0),
        reading(210, 816.0, 9.0, 150, 56),
        reading(270, 966.0, 9.0, 160, 64),
        SessionEvent.PhaseCompleted(t0 + 270.seconds, 1),
        SessionEvent.Finished(t0 + 270.seconds),
    )

    @Test
    fun treadmillSessionsWriteARecordPerReadingAndALapPerPhase() {
        val messages = encode(treadmillEvents(), intervals, FitSport.TREADMILL_RUNNING)

        val records = messages.of(20).map { it.fields }
        assertEquals(listOf(0L, 60L, 150L, 210L, 270L), records.map { it.getValue(253) - fitT0 })
        assertEquals(listOf(0L, 8000L, 16600L, 31600L, 46600L), records.map { it[5] })
        assertEquals(listOf(1389L, 1389L, 1389L, 2500L, 2500L), records.map { it[6] })
        assertEquals(listOf(100L, 110L, 120L, 150L, 160L), records.map { it[3] })
        assertEquals(80L, records.first()[4])
        assertEquals(150L, records.first()[9])

        val laps = messages.of(19).map { it.fields }
        assertEquals(2, laps.size)
        assertEquals(listOf(fitT0, fitT0 + 150), laps.map { it[2] })
        assertEquals(listOf(150_000L, 120_000L), laps.map { it[7] })
        // The pause does not count in the first lap's time.
        assertEquals(listOf(120_000L, 120_000L), laps.map { it[8] })
        assertEquals(listOf(16600L, 30000L), laps.map { it[9] })
        assertEquals(listOf(8L, 16L), laps.map { it[11] })
        assertEquals(listOf(110L, 155L), laps.map { it[15] })
        assertEquals(listOf(120L, 160L), laps.map { it[16] })
        assertEquals(listOf(2L, 0L), laps.map { it[23] })
        assertEquals(listOf(0L, 7L), laps.map { it[24] })
        assertEquals(listOf(listOf(1L, 1L), listOf(1L, 1L)), laps.map { listOf(it[25], it[39]) })

        val session = messages.of(18).single().fields
        assertEquals(listOf(1L, 1L), listOf(session[5], session[6]))
        assertEquals(270_000L, session[7])
        assertEquals(240_000L, session[8])
        assertEquals(46600L, session[9])
        assertEquals(24L, session[11])
        assertEquals(1942L, session[14])
        assertEquals(2L, session[26])
        assertEquals(0, messages.of(225).size)

        // The timer starts, stops for the pause, starts again and stops at the end.
        val timer = messages.of(21).map { (it.fields.getValue(253) - fitT0) to it.fields[1] }
        assertEquals(listOf(0L to 0L, 60L to 4L, 90L to 0L, 270L to 4L), timer)
    }

    @Test
    fun aMachineRestartedMidSessionNeverTakesTheDistanceBack() {
        val events = listOf(
            SessionEvent.Started(t0),
            reading(0, 900.0, 6.0, 100, 10),
            reading(60, 1000.0, 6.0, 100, 12),
            reading(120, 0.0, 6.0, 100, 0),
            reading(180, 100.0, 6.0, 100, 2),
            SessionEvent.Finished(t0 + 180.seconds),
        )
        val workout = workout(WorkoutFormat.CIRCUIT, part(PartKind.WORK, exercise("walk", SetPrescription(Target.Open))))

        val messages = encode(events, workout, FitSport.TREADMILL_RUNNING)

        assertEquals(listOf(0L, 10000L, 10000L, 20000L), messages.of(20).map { it.fields[5] })
        assertEquals(20000L, messages.of(18).single().fields[9])
    }

}
