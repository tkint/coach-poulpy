package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
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
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration
import kotlin.time.Instant

/** Opening a finished session lists what was done, part by part, from its events (`session-recap-*` mockups). */
class SessionRecapTest {
    private val graph = AppGraph(DriverFactory())
    private val start = Instant.parse("2026-10-24T18:05:00Z")

    private fun reps(count: Int, load: Load? = null) = SetPrescription(Target.Reps(count), load = load)

    private fun exercise(id: String, slug: String, vararg sets: SetPrescription) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(slug), Prescription(sets.toList()))

    private fun save(workout: Workout): Workout = workout.also(graph.store::saveWorkout)

    /**
     * Runs [workout] to the end as one session: each phase is done, except those at [skipped], which are skipped.
     * [extra] is logged at the end, before the session finishes.
     */
    private fun run(id: String, workout: Workout, skipped: Set<Int> = emptySet(), extra: List<SessionEvent> = emptyList(), phaseLength: Duration = 1.minutes): SessionRecap {
        val phases = graph.compiler.compile(workout).phases
        graph.store.startSession(
            SessionRecord(SessionId(id), SessionSource.GUIDED, start, workout = WorkoutRef(workout.id, workout.version)),
            title = workout.name,
            format = workout.format,
        )
        graph.store.appendEvent(id, SessionEvent.Started(start))
        phases.indices.forEach { index ->
            // A reading goes in the log while its phase runs, so just before that phase ends.
            extra.filterIsInstance<SessionEvent.Sample>().filter { (it.at - start).inWholeMinutes.toInt() == index }.forEach { graph.store.appendEvent(id, it) }
            val at = start + phaseLength * (index + 1)
            graph.store.appendEvent(id, if (index in skipped) SessionEvent.PhaseSkipped(at, index) else SessionEvent.PhaseCompleted(at, index))
        }
        val end = start + phaseLength * phases.size
        graph.store.appendEvent(id, SessionEvent.Finished(end))
        graph.store.finishSession(id, end)
        return assertNotNull(graph.sessionRecap(id, "en"))
    }

    private fun sets() = save(
        Workout(
            WorkoutId("upper"), 1, "Upper body", WorkoutFormat.SETS,
            listOf(
                Part(PartId("w"), PartKind.WARM_UP, listOf(exercise("jack", "jumping-jack", SetPrescription(Target.Time(30.seconds))))),
                Part(
                    PartId("a"), PartKind.WORK,
                    listOf(
                        exercise("push", "push-up", reps(12), reps(12), reps(12)),
                        exercise("press", "dumbbell-overhead-press", reps(8, Load.Kilograms(10.0)), reps(8, Load.Kilograms(10.0)), reps(8, Load.Kilograms(10.0))),
                        exercise("plank", "plank", SetPrescription(Target.Hold(40.seconds)), SetPrescription(Target.Hold(40.seconds))),
                    ),
                ),
                Part(PartId("c"), PartKind.COOL_DOWN, listOf(exercise("pose", "not-in-the-catalog", SetPrescription(Target.Time(90.seconds))))),
            ),
        ),
    )

    @Test
    fun aSetsWorkoutListsEachExerciseUnderItsPartWithWhatWasDone() {
        // Phases: 0 jack, 1-3 push-up, 4-6 press, 7-8 plank, 9 pose. The press's third set is skipped.
        val recap = run("sets-1", sets(), skipped = setOf(6))

        assertEquals("Upper body", recap.title)
        assertEquals("Sets", recap.formatLabel)
        assertEquals(SessionKind.WORKOUT, recap.kind)
        assertEquals("Exercises", recap.listTitle)
        assertEquals(listOf("Warm-up", "Work", "Cool down"), recap.parts.map { it.name })
        val work = recap.parts[1]
        assertEquals("3 exercises · 7 of 8 sets", work.summary)
        assertEquals(listOf("3 × 12", "2 × 8 · 10 kg · 1 set skipped", "2 × 40 s"), work.rows.map { it.detail })
        assertEquals("Push-up", work.rows[0].name)
        assertEquals("1 exercise", recap.parts[0].summary)
        // An exercise the catalog no longer names is listed by its slug.
        assertEquals("not-in-the-catalog", recap.parts[2].rows.single().name)
        assertEquals("1 min 30", recap.parts[2].rows.single().detail)
        // Each phase took a minute: the time is on every exercise and every part, skipped sets included.
        assertEquals(listOf("3:00", "3:00", "2:00"), work.rows.map { it.time })
        assertEquals(listOf("1:00", "8:00", "1:00"), recap.parts.map { it.time })
        assertEquals(listOf("Duration" to "10:00", "Sets done" to "9 / 10", "Skipped" to "1"), recap.tiles.map { it.label to it.value })
    }

    @Test
    fun theMusclesWorkedFollowTheSetsDoneNotThePlan() {
        // Phases: 0 jack, 1-3 push-up, 4-6 press, 7-8 plank, 9 pose. Skip the press's third set and both plank sets.
        val recap = run("muscles-1", sets(), skipped = setOf(6, 7, 8))

        val loads = recap.muscles.loads.associate { it.label to it.sets }
        // The warm-up and cool down add nothing, and the skipped plank sets leave Core out.
        assertEquals(false, "Core" in loads)
        assertEquals(3, loads["Chest"])
        assertEquals(recap.muscles.loads.maxOf { it.heat }, recap.muscles.heat["deltoid-left"])
        assertEquals(null, recap.muscles.heat["abs-left"])
    }

    @Test
    fun aCircuitCountsRoundsAndWordsSkipsAsRounds() {
        val workout = save(
            Workout(
                WorkoutId("burner"), 1, "Full body burner", WorkoutFormat.CIRCUIT,
                listOf(
                    Part(
                        PartId("w"), PartKind.WORK,
                        listOf(
                            Step.Group(
                                StepId("repeat"), GroupKind.CIRCUIT, rounds = 4,
                                children = listOf(exercise("burpee", "burpee", reps(8)), exercise("thruster", "dumbbell-thruster", reps(10, Load.Kilograms(8.0)))),
                            ),
                        ),
                    ),
                ),
            ),
        )
        // Round 3's thruster (phase 5) is skipped.
        val recap = run("circuit-1", workout, skipped = setOf(5))

        assertEquals(listOf("4 × 8", "3 × 10 · 8 kg · 1 round skipped"), recap.parts.single().rows.map { it.detail })
        assertEquals("Rounds" to "4 / 4", recap.tiles[1].let { it.label to it.value })
        assertEquals("Skipped" to "1", recap.tiles[2].let { it.label to it.value })
    }

    @Test
    fun stepsNeverReachedAreLeftOut() {
        val workout = sets()
        val phases = graph.compiler.compile(workout).phases
        graph.store.startSession(SessionRecord(SessionId("early"), SessionSource.GUIDED, start, workout = WorkoutRef(workout.id, workout.version)), "Upper body", workout.format)
        // Only the warm-up and the first push-up set were done.
        listOf(
            SessionEvent.Started(start),
            SessionEvent.PhaseCompleted(start + 1.minutes, 0),
            SessionEvent.PhaseCompleted(start + 2.minutes, 1),
            SessionEvent.Finished(start + 3.minutes),
        ).forEach { graph.store.appendEvent("early", it) }
        graph.store.finishSession("early", start + 3.minutes)

        val recap = assertNotNull(graph.sessionRecap("early", "en"))
        assertEquals(listOf("Warm-up", "Work"), recap.parts.map { it.name })
        assertEquals(listOf("push-up"), recap.parts[1].rows.map { it.slug })
        assertEquals("12 reps", recap.parts[1].rows.single().detail)
        assertEquals(true, phases.any { it is Phase.Work })
    }

    @Test
    fun aRestCountsForTheExerciseItFollows() {
        val workout = save(
            Workout(
                WorkoutId("rests"), 1, "Rests", WorkoutFormat.SETS,
                listOf(
                    Part(
                        PartId("w"), PartKind.WORK,
                        listOf(
                            exercise("push", "push-up", SetPrescription(Target.Reps(10), restAfter = 30.seconds), reps(10)),
                            exercise("plank", "plank", SetPrescription(Target.Hold(30.seconds))),
                        ),
                    ),
                ),
            ),
        )
        // Phases: push-up, rest, push-up, plank, a minute each.
        val recap = run("rests-1", workout)

        assertEquals(listOf("3:00", "1:00"), recap.parts.single().rows.map { it.time })
        assertEquals("4:00", recap.parts.single().time)
    }

    @Test
    fun aWalkAndARunAreListedBlockByBlockWithWhatTheTreadmillSaid() {
        val workout = save(
            Workout(
                WorkoutId("easy-run"), 1, "Easy run", WorkoutFormat.SETS,
                listOf(
                    Part(
                        PartId("w"), PartKind.WORK,
                        listOf(
                            exercise("walk", "walk", SetPrescription(Target.Time(1.minutes), intensity = Intensity.Speed(6.0))),
                            exercise("run", "run", SetPrescription(Target.Time(1.minutes), intensity = Intensity.Speed(9.0))),
                        ),
                    ),
                ),
            ),
        )
        // The walk is read twice, at 6 km/h (110 then 114 bpm), and the belt goes on to 160 m at 9 km/h, 150 bpm for the run.
        val readings = listOf(
            SessionEvent.Sample(start, DeviceMetrics(speedKmh = 6.0, distanceMeters = 0.0, heartRateBpm = 110)),
            SessionEvent.Sample(start + 30.seconds, DeviceMetrics(speedKmh = 6.0, distanceMeters = 60.0, heartRateBpm = 114)),
            SessionEvent.Sample(start + 1.minutes, DeviceMetrics(speedKmh = 9.0, distanceMeters = 160.0, heartRateBpm = 150)),
        )
        val recap = run("run-1", workout, extra = readings)

        assertEquals("Steps", recap.listTitle)
        val block = recap.parts.single()
        assertEquals("2 steps", block.summary)
        assertEquals(listOf("Walk", "Run"), block.rows.map { it.name })
        assertEquals("0.1\u00a0km · 6.0\u00a0km/h · 112 bpm", block.rows[0].detail)
        assertEquals(listOf("1:00", "1:00"), block.rows.map { it.time })
        assertEquals("0.1\u00a0km · 9.0\u00a0km/h · 150 bpm", block.rows[1].detail)
        assertEquals(SessionKind.TREADMILL, recap.kind)
        assertEquals("Time", recap.tiles.first().label)
    }

    @Test
    fun aPhaseTappedThroughStillShowsItsTime() {
        // Every phase ends 400 ms after the last: the times round to the second and never vanish.
        val recap = run("quick", sets(), phaseLength = 400.milliseconds)
        assertTrue(recap.parts.flatMap { it.rows }.all { it.time.isNotEmpty() })
        assertTrue(recap.parts.all { it.time.isNotEmpty() })
    }

    @Test
    fun aFreeRunKeepsTheFiguresAlone() {
        graph.store.startSession(SessionRecord(SessionId("free"), SessionSource.DEVICE_ONLY, start))
        listOf(
            SessionEvent.Started(start),
            SessionEvent.Sample(start + 1.seconds, DeviceMetrics(speedKmh = 8.0, distanceMeters = 0.0, heartRateBpm = 120)),
            SessionEvent.Sample(start + 10.minutes, DeviceMetrics(speedKmh = 8.0, distanceMeters = 1300.0, heartRateBpm = 140)),
            SessionEvent.PhaseCompleted(start + 10.minutes, 0),
            SessionEvent.Finished(start + 10.minutes),
        ).forEach { graph.store.appendEvent("free", it) }
        graph.store.finishSession("free", start + 10.minutes)

        val recap = assertNotNull(graph.sessionRecap("free", "en"))
        assertEquals("Free run", recap.title)
        assertNull(recap.formatLabel)
        assertEquals(emptyList(), recap.parts)
        val tiles = recap.tiles.associate { it.label to it.value }
        assertEquals("10:00", tiles["Time"])
        assertEquals("1.3 km", tiles["Distance"])
        assertEquals("130 bpm", tiles["Avg heart rate"])
        assertEquals("140 bpm", tiles["Max heart rate"])
    }

    @Test
    fun aSessionThatIsGoneHasNoRecap() {
        assertNull(graph.sessionRecap("nothing", "en"))
    }

    @Test
    fun theFrenchRecapSaysTheSameInFrench() {
        run("fr-1", sets(), skipped = setOf(6))
        val recap = assertNotNull(graph.sessionRecap("fr-1", "fr"))
        assertEquals("Exercices", recap.listTitle)
        assertEquals("2 × 8 · 10 kg · 1 série passée", recap.parts[1].rows[1].detail)
        assertEquals("Durée", recap.tiles.first().label)
    }
}
