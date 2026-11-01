package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.serialization.json.Json

/** Steps with a role, repeats and rests, as in an interval run or a circuit. */
class BuilderStepsTest {
    private val graph = AppGraph(DriverFactory())

    /**
     * Warm-up: run at 5:30 /km until button press. Work, repeat 4 times: run 400 m at 5:00 /km,
     * recover walking 1 min 30 s. Cool down: run at 5:45 /km until the end.
     */
    private fun intervalRun(builder: BuilderModel = graph.createWorkout("en")): BuilderModel {
        builder.rename("4 × 400 m")
        builder.chooseFormat(WorkoutFormat.CIRCUIT)
        builder.addPart(PartKind.WARM_UP)
        builder.addPart(PartKind.COOL_DOWN)

        builder.add("run", builder.part(PartKind.WARM_UP))
        val warmUp = builder.state.value.addedKey!!
        builder.setDurationKind(warmUp, DurationKind.UNTIL_PRESSED)
        builder.setIntensityKind(warmUp, IntensityKind.PACE)
        builder.setPace(warmUp, "5:30", PaceUnit.PER_KM)

        val repeat = builder.state.value.parts[1].steps.single().key
        builder.setRepeatTimes(repeat, 4)
        builder.add("run", builder.work, repeat)
        val fast = builder.state.value.addedKey!!
        builder.setDistance(fast, "400", DistanceUnit.M)
        builder.setIntensityKind(fast, IntensityKind.PACE)
        builder.setPace(fast, "5:00", PaceUnit.PER_KM)
        builder.add("walk", builder.work, repeat)
        val easy = builder.state.value.addedKey!!
        builder.setRole(easy, StepRole.RECOVER)
        builder.setDuration(easy, "1:30")

        builder.add("run", builder.part(PartKind.COOL_DOWN))
        val coolDown = builder.state.value.addedKey!!
        builder.setDurationKind(coolDown, DurationKind.UNTIL_PRESSED)
        builder.setIntensityKind(coolDown, IntensityKind.PACE)
        builder.setPace(coolDown, "5:45", PaceUnit.PER_KM)
        return builder
    }

    @Test
    fun anIntervalRunReadsLikeTheWorkoutItIs() {
        val state = intervalRun().state.value
        assertEquals(listOf("Warm-up", "Work", "Cool down"), state.parts.map { it.name })
        assertNull(state.rows[0].roleLabel)
        assertEquals("5:30 /km · until button press", state.rows[0].summary)
        assertEquals(DurationKind.UNTIL_PRESSED, state.rows[0].target?.duration)

        val repeat = assertNotNull(state.parts[1].steps.single().repeatGroup)
        assertEquals("4 times", repeat.timesLabel)
        assertEquals(listOf("400 m · 5:00 /km", "1 min 30"), repeat.rows.map { it.summary })
        assertEquals(listOf(null, "Recover"), repeat.rows.map { it.roleLabel })
        assertTrue(repeat.rows.all { it.repeatKey == repeat.key })
        assertTrue(state.parts[1].summary.startsWith("4 rounds · about "))

        assertEquals("5:45 /km · until the end", state.rows.last().summary)
        assertTrue(state.rows.last().isLast)
        assertEquals("1 step · until the end", state.parts.last().summary)
        assertTrue(state.canSave)
    }

    @Test
    fun anIntervalRunCompilesToRoundsThatEndOnAPress() {
        val id = intervalRun().save()!!
        val workout = graph.store.latestWorkout(WorkoutId(id))!!
        val work = graph.compiler.compile(workout).phases.filterIsInstance<Phase.Work>()

        assertEquals(1 + 4 * 2 + 1, work.size)
        assertEquals(Target.Open, work.first().target)
        assertEquals(CompletionRule.Manual, work.first().completion)
        assertNull(work.first().duration)
        assertEquals(PartKind.WARM_UP, work.first().part)
        assertEquals(StepRole.WORK, work.first().role)
        assertEquals(listOf(1, 1, 2, 2, 3, 3, 4, 4), work.drop(1).dropLast(1).map { it.round })
        assertTrue(work.drop(1).dropLast(1).all { it.rounds == 4 })
        assertEquals(Target.Time(90.seconds), work[2].target)
        assertEquals(StepRole.RECOVER, work[2].role)
        assertEquals(Target.Open, work.last().target)
        assertEquals(PartKind.COOL_DOWN, work.last().part)
        assertTrue(work.drop(1).dropLast(1).all { it.part == PartKind.WORK && it.workPart == null })
    }

    @Test
    fun anIntervalRunComesBackTheSameWhenEdited() {
        val id = intervalRun().save()!!
        val saved = graph.store.latestWorkout(WorkoutId(id))!!
        assertEquals(listOf(PartKind.WARM_UP, PartKind.WORK, PartKind.COOL_DOWN), saved.parts.map { it.kind })
        val edit = assertNotNull(graph.editWorkout(id, "en"))
        val state = edit.state.value
        assertEquals(listOf(null, null, "Recover", null), state.rows.map { it.roleLabel })
        assertEquals(4, state.parts[1].steps.single().repeatGroup?.times)
        assertEquals("5:45 /km · until the end", state.rows.last().summary)

        val again = graph.store.latestWorkout(WorkoutId(edit.save()!!))!!
        assertEquals(saved.steps, again.steps)
    }

    @Test
    fun untilTheEndIsOnlyTheLastStep() {
        val builder = intervalRun()
        val coolDown = builder.state.value.rows.last().key
        builder.add("walk", builder.part(PartKind.COOL_DOWN))
        val row = builder.state.value.rows.single { it.key == coolDown }
        assertFalse(row.isLast)
        assertEquals("5:45 /km · until button press", row.summary)

        // Inside a repeat it would come back every round, so it never reads "until the end" there.
        builder.remove(builder.state.value.addedKey!!)
        val part = builder.part(PartKind.COOL_DOWN)
        builder.addRepeat(part)
        builder.add("walk", part, builder.state.value.parts.last().steps.last().key)
        assertFalse(builder.state.value.rows.last().isLast)
    }

    @Test
    fun setsKeepTheirRestAndASupersetIsARepeat() {
        val builder = graph.createWorkout("en")
        builder.rename("Legs and core")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        builder.addRepeat(builder.work)
        val repeat = builder.state.value.parts.single().steps.last().key
        builder.setRepeatTimes(repeat, 3)
        builder.add("push-up", builder.work, repeat)
        builder.addRest(builder.work, repeat)
        val rest = builder.state.value.addedKey!!
        builder.changeRest(rest, 1)

        val rows = builder.state.value.rows
        assertEquals("3 × 10 · 1 min between sets", rows[0].summary)
        assertEquals("10 reps", rows[1].summary)
        assertTrue(rows[2].isRest)
        assertEquals("Recover", rows[2].roleLabel)
        assertEquals("1 min 15", rows[2].summary)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val group = workout.steps[1] as Step.Group
        assertEquals(3, group.rounds)
        assertEquals(75.seconds, (group.children[1] as Step.Rest).duration)
        val phases = graph.compiler.compile(workout).phases
        // Squat: 3 sets a minute apart; then 3 rounds of push-up and rest, the very last rest dropped.
        assertEquals(listOf(true, false, true, false, true), phases.take(5).map { it is Phase.Work })
        assertEquals(3, phases.drop(5).count { it is Phase.Work })
        assertTrue(phases.last() is Phase.Work)
    }

    @Test
    fun stepsMoveIntoAndOutOfTheRepeatAbove() {
        val builder = graph.createWorkout("en")
        builder.rename("Circuit")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.addRepeat(builder.work)
        assertFalse(builder.state.value.canSave, "an empty repeat")
        builder.add("plank", builder.work)
        val plank = builder.state.value.addedKey!!
        assertTrue(builder.state.value.rows.single().canMoveIntoRepeat)

        builder.moveIntoRepeatAbove(plank)
        val steps = { builder.state.value.parts.single().steps }
        assertEquals(1, steps().size)
        assertEquals(plank, steps().single().repeatGroup!!.rows.single().key)
        assertTrue(builder.state.value.canSave)

        builder.moveOutOfRepeat(plank)
        assertEquals(listOf(null, plank), steps().map { it.row?.key })
        assertTrue(steps().first().repeatGroup!!.rows.isEmpty())
    }

    @Test
    fun dragAndMoveStayInsideTheListThatHoldsTheStep() {
        val builder = graph.createWorkout("en")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        builder.addRepeat(builder.work)
        val steps = { builder.state.value.parts.single().steps }
        val repeat = steps().last().key
        builder.add("push-up", builder.work, repeat)
        val push = builder.state.value.addedKey!!
        builder.add("plank", builder.work, repeat)
        val plank = builder.state.value.addedKey!!

        builder.move(plank, 0)
        assertEquals(listOf(plank, push), steps()[1].repeatGroup!!.rows.map { it.key })
        builder.moveUp(plank)
        assertEquals(listOf(plank, push), steps()[1].repeatGroup!!.rows.map { it.key })
        builder.move(repeat, 0)
        assertEquals(repeat, steps().first().key)

        builder.remove(repeat)
        assertEquals(listOf("Goblet squat"), builder.state.value.rows.map { it.name })
    }

    @Test
    fun theChevronsStopAtTheEndsOfThePartAndOfTheRepeat() {
        val builder = graph.createWorkout("en")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        builder.addRepeat(builder.work)
        val steps = { builder.state.value.parts.single().steps }
        val repeat = steps().last().key
        builder.add("push-up", builder.work, repeat)
        builder.add("plank", builder.work, repeat)

        val squat = steps().first().row!!
        assertEquals(false to true, squat.canMoveUp to squat.canMoveDown)
        val group = steps().last().repeatGroup!!
        assertEquals(true to false, group.canMoveUp to group.canMoveDown)
        assertEquals(listOf(false to true, true to false), group.rows.map { it.canMoveUp to it.canMoveDown })

        builder.moveDown(group.rows.first().key)
        assertEquals(listOf("Plank", "Push-up"), steps().last().repeatGroup!!.rows.map { it.name })
        builder.moveUp(repeat)
        assertEquals(repeat, steps().first().key)
        assertEquals(false to true, steps().first().repeatGroup!!.let { it.canMoveUp to it.canMoveDown })
    }

    @Test
    fun anExerciseAddedToARepeatStartsAtOneSet() {
        val builder = graph.createWorkout("en")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.addRepeat(builder.work)
        builder.add("push-up", builder.work, builder.state.value.parts.single().steps.single().key)
        assertEquals(1, builder.state.value.rows.single().sets)
    }

    @Test
    fun theGuidedSessionSaysThePartAndTheRound() {
        val workout = graph.store.latestWorkout(WorkoutId(intervalRun().save()!!))!!
        val player = PlayerModel(
            workout, graph.compiler.compile(workout), graph.catalog, graph.store, "en",
            executionId = "steps-1", events = graph.store.events("steps-1"),
            scope = CoroutineScope(StandardTestDispatcher()),
        )
        player.start()
        with(player.state.value) {
            assertEquals("Warm-up", partLabel)
            assertNull(roleLabel)
            assertEquals("Until you tap Next", targetLabel)
            assertFalse(isTimed)
        }
        player.done()
        assertEquals("Work · round 1 of 4", player.state.value.partLabel)
        assertNull(player.state.value.roleLabel)
    }

    /** Warm-up, two work parts of rounds and a cool down: built, saved, opened again and run. */
    @Test
    fun aCircuitInFourPartsComesBackAndRuns() {
        val builder = graph.createWorkout("en")
        builder.rename("Full body burner")
        builder.chooseFormat(WorkoutFormat.CIRCUIT)
        builder.addPart(PartKind.WARM_UP)
        builder.addPart(PartKind.WORK)
        builder.addPart(PartKind.COOL_DOWN)
        val (warmUp, first, second, coolDown) = builder.state.value.parts
        builder.add("jumping-jack", warmUp.key)
        builder.add("goblet-squat", first.key, first.steps.single().key)
        builder.add("push-up", first.key, first.steps.single().key)
        builder.addRest(first.key, first.steps.single().key)
        builder.add("plank", second.key, second.steps.single().key)
        builder.add("hamstring-stretch", coolDown.key)
        val id = assertNotNull(builder.save())

        val saved = graph.store.latestWorkout(WorkoutId(id))!!
        assertEquals(listOf(PartKind.WARM_UP, PartKind.WORK, PartKind.WORK, PartKind.COOL_DOWN), saved.parts.map { it.kind })
        val edit = assertNotNull(graph.editWorkout(id, "en"))
        assertEquals(listOf("Warm-up", "Work 1", "Work 2", "Cool down"), edit.state.value.parts.map { it.name })
        assertEquals("3 rounds · about 6 min", edit.state.value.parts[1].summary)
        assertEquals(saved.steps, graph.store.latestWorkout(WorkoutId(edit.save()!!))!!.steps)

        val work = graph.compiler.compile(saved).phases.filterIsInstance<Phase.Work>()
        assertEquals(listOf(1, 1, 2, 2, 3, 3), work.filter { it.workPart == 1 }.map { it.round })
        assertEquals(3, work.count { it.workPart == 2 })
        assertEquals(PartKind.COOL_DOWN, work.last().part)
    }

    /** Warm-up, a bench press with rest between sets, a superset and a cool down: built, saved, opened again and run. */
    @Test
    fun aSetsWorkoutWithASupersetComesBackAndRuns() {
        val builder = graph.createWorkout("en")
        builder.rename("Upper body")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.addPart(PartKind.WARM_UP)
        builder.add("walk", builder.part(PartKind.WARM_UP))
        builder.add("dumbbell-bench-press", builder.work)
        val press = builder.state.value.addedKey!!
        builder.changeRestBetweenSets(press, 4)
        builder.addRepeat(builder.work)
        val superset = builder.state.value.parts[1].steps.last().key
        builder.setRepeatTimes(superset, 3)
        builder.add("biceps-curl", builder.work, superset)
        builder.add("bench-dip", builder.work, superset)
        val id = assertNotNull(builder.save())

        val edit = assertNotNull(graph.editWorkout(id, "en"))
        val rows = edit.state.value.rows
        assertEquals("3 × 10 · 2 min between sets", rows[1].summary)
        assertEquals(listOf("10 reps", "10 reps"), rows.drop(2).map { it.summary })
        assertEquals("3 exercises · about 10 min", edit.state.value.parts[1].summary)

        val saved = graph.store.latestWorkout(WorkoutId(id))!!
        val phases = graph.compiler.compile(saved).phases
        assertEquals(2, phases.count { it is Phase.Rest && it.duration == 120.seconds })
        assertEquals(1 + 3 + 6, phases.count { it is Phase.Work })
    }

    @Test
    fun aStepSavedBeforeRolesIsAnExerciseStep() {
        val json = """{"type":"exercise","id":"a","exercise":"squat","prescription":{"sets":[]}}"""
        val step = Json.decodeFromString<Step>(json) as Step.ExerciseStep
        assertEquals(StepRole.WORK, step.role)
        assertEquals(Target.Open, Json.decodeFromString<Target>("""{"type":"open"}"""))
    }

    @Test
    fun aStepIsAnExerciseOrRecovers() {
        val builder = graph.createWorkout("en")
        assertEquals(listOf("Exercise", "Recover"), builder.roleOptions.map { it.label })
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("walk", builder.work, role = StepRole.RECOVER)
        assertEquals("Recover", builder.state.value.rows.single().roleLabel)
    }

    @Test
    fun duplicatingPutsACopyRightAfter() {
        val builder = intervalRun()
        val warmUp = builder.state.value.rows.first()
        builder.duplicate(warmUp.key)
        val rows = builder.state.value.rows
        assertEquals(warmUp.summary, rows[1].summary)
        assertEquals(warmUp.partKey, rows[1].partKey)
        assertTrue(rows[1].key != warmUp.key)

        val repeat = builder.state.value.parts[1].steps.single().repeatGroup!!
        builder.duplicate(repeat.key)
        val copy = builder.state.value.parts[1].steps[1].repeatGroup!!
        assertEquals(repeat.timesLabel, copy.timesLabel)
        assertEquals(repeat.rows.map { it.summary }, copy.rows.map { it.summary })
        assertTrue(copy.rows.none { row -> repeat.rows.any { it.key == row.key } })
    }

    @Test
    fun theWorkoutPageShowsTheStructure() {
        val id = intervalRun().save()!!
        val page = WorkoutPageModel(WorkoutId(id), graph.store, graph.compiler, graph.catalog, "en", scope = CoroutineScope(Dispatchers.Default))
        val state = runBlocking { withTimeout(5.seconds) { page.state.first { it.loaded } } }
        assertEquals(listOf("Warm-up", "Work", "Cool down"), state.parts.map { it.name })
        val repeat = assertNotNull(state.parts[1].steps.single().repeatGroup)
        assertEquals("4 times", repeat.timesLabel)
        // Units are not saved with the workout (#72), so the page shows the distance in km.
        assertEquals(listOf("0.4 km · 5:00 /km", "1 min 30"), repeat.rows.map { it.prescription })
        assertEquals("5:45 /km · until the end", state.parts[2].steps.single().row?.prescription)
        assertEquals("1 step · until the end", state.parts[2].summary)
        page.close()
    }

    @Test
    fun aWalkOrRunStartsWithADistance() {
        val builder = graph.createWorkout("en")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("run", builder.work)
        val target = builder.state.value.rows.single().target!!
        assertEquals(DurationKind.DISTANCE, target.duration)
        assertEquals("5.0 km", builder.state.value.rows.single().summary)
    }


    /** The number fields type reps, a rest and the rest between sets; the − and + keep the same limits. */
    @Test
    fun typedNumbersAreKeptWithinTheLimits() {
        val builder = graph.createWorkout("en")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("push-up", builder.work)
        val press = builder.state.value.addedKey!!
        builder.addRest(builder.work)
        val rest = builder.state.value.addedKey!!

        builder.setAmount(press, 12)
        builder.setRestBetweenSets(press, 90)
        builder.setRest(rest, 45)
        val rows = builder.state.value.rows
        assertEquals(12, rows[0].amount)
        assertEquals(AmountUnit.REPS, rows[0].amountUnit)
        assertEquals(90, rows[0].restBetweenSets)
        assertEquals(45, rows[1].restSeconds)
        assertEquals(AmountUnit.SECONDS, rows[1].amountUnit)

        builder.setAmount(press, 0)
        builder.setRestBetweenSets(press, 5000)
        builder.setRest(rest, 0)
        val clamped = builder.state.value.rows
        assertEquals(1, clamped[0].amount)
        assertEquals(BuilderModel.MAX_REST, clamped[0].restBetweenSets)
        assertEquals(1, clamped[1].restSeconds)
    }
}
