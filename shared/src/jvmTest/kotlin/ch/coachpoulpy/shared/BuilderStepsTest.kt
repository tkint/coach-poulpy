package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.model.PartId
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
     * Warm-up: run at 5:30 /km until button press. Repeat 4 times: run 400 m at 5:00 /km,
     * recover walking 1 min 30 s. Cool down: run at 5:45 /km until the end.
     */
    private fun intervalRun(builder: BuilderModel = graph.createWorkout("en")): BuilderModel {
        builder.rename("4 × 400 m")
        builder.setFormat(WorkoutFormat.CIRCUIT)

        builder.add("run")
        val warmUp = builder.state.value.addedKey!!
        builder.setRole(warmUp, BuilderRole.WARM_UP)
        builder.setDurationKind(warmUp, DurationKind.UNTIL_PRESSED)
        builder.setIntensityKind(warmUp, IntensityKind.PACE)
        builder.setPace(warmUp, "5:30", PaceUnit.PER_KM)

        builder.addRepeat()
        val repeat = builder.state.value.steps.last().key
        builder.setRepeatTimes(repeat, 4)
        builder.add("run", repeat)
        val fast = builder.state.value.addedKey!!
        builder.setDistance(fast, "400", DistanceUnit.M)
        builder.setIntensityKind(fast, IntensityKind.PACE)
        builder.setPace(fast, "5:00", PaceUnit.PER_KM)
        builder.add("walk", repeat)
        val easy = builder.state.value.addedKey!!
        builder.setRole(easy, BuilderRole.RECOVER)
        builder.setDuration(easy, "1:30")

        builder.add("run")
        val coolDown = builder.state.value.addedKey!!
        builder.setRole(coolDown, BuilderRole.COOL_DOWN)
        builder.setDurationKind(coolDown, DurationKind.UNTIL_PRESSED)
        builder.setIntensityKind(coolDown, IntensityKind.PACE)
        builder.setPace(coolDown, "5:45", PaceUnit.PER_KM)
        return builder
    }

    @Test
    fun anIntervalRunReadsLikeTheWorkoutItIs() {
        val state = intervalRun().state.value
        assertEquals(3, state.steps.size)
        assertEquals("Warm-up", state.rows[0].roleLabel)
        assertEquals("5:30 /km · until button press", state.rows[0].summary)
        assertEquals(DurationKind.UNTIL_PRESSED, state.rows[0].target?.duration)

        val repeat = assertNotNull(state.steps[1].repeatGroup)
        assertEquals("4 times", repeat.timesLabel)
        assertEquals(listOf("400 m · 5:00 /km", "1 min 30"), repeat.rows.map { it.summary })
        assertEquals(listOf(null, "Recover"), repeat.rows.map { it.roleLabel })
        assertTrue(repeat.rows.all { it.repeatKey == repeat.key })

        assertEquals("Cool down", state.rows.last().roleLabel)
        assertEquals("5:45 /km · until the end", state.rows.last().summary)
        assertTrue(state.rows.last().isLast)
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
        assertEquals(listOf("Warm-up", null, "Recover", "Cool down"), state.rows.map { it.roleLabel })
        assertEquals(4, state.steps[1].repeatGroup?.times)
        assertEquals("5:45 /km · until the end", state.rows.last().summary)

        val again = graph.store.latestWorkout(WorkoutId(edit.save()!!))!!
        assertEquals(saved.steps, again.steps)
    }

    @Test
    fun untilTheEndIsOnlyTheLastStep() {
        val builder = intervalRun()
        val coolDown = builder.state.value.rows.last().key
        builder.add("walk")
        val row = builder.state.value.rows.single { it.key == coolDown }
        assertFalse(row.isLast)
        assertEquals("5:45 /km · until button press", row.summary)

        // Inside a repeat it would come back every round, so it never reads "until the end" there.
        builder.remove(builder.state.value.addedKey!!)
        builder.addRepeat()
        builder.add("walk", builder.state.value.steps.last().key)
        assertFalse(builder.state.value.rows.last().isLast)
    }

    @Test
    fun strengthKeepsItsSetsAndACircuitIsARepeat() {
        val builder = graph.createWorkout("en")
        builder.rename("Legs and core")
        builder.add("goblet-squat")
        builder.addRepeat()
        val repeat = builder.state.value.steps.last().key
        builder.setRepeatTimes(repeat, 3)
        builder.add("push-up", repeat)
        builder.addRest(repeat)
        val rest = builder.state.value.addedKey!!
        builder.changeRest(rest, 1)

        val rows = builder.state.value.rows
        assertEquals("3 × 10", rows[0].summary)
        assertEquals("1 × 10", rows[1].summary)
        assertTrue(rows[2].isRest)
        assertEquals("Recover", rows[2].roleLabel)
        assertEquals("1 min 15", rows[2].summary)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val group = workout.steps[1] as Step.Group
        assertEquals(3, group.rounds)
        assertEquals(75.seconds, (group.children[1] as Step.Rest).duration)
        val phases = graph.compiler.compile(workout).phases
        // Squat: 3 sets back to back; then 3 rounds of push-up and rest, the very last rest dropped.
        assertEquals(3, phases.take(3).count { it is Phase.Work })
        assertEquals(3, phases.drop(3).count { it is Phase.Work })
        assertTrue(phases.last() is Phase.Work)
    }

    @Test
    fun stepsMoveIntoAndOutOfTheRepeatAbove() {
        val builder = graph.createWorkout("en")
        builder.rename("Circuit")
        builder.addRepeat()
        assertFalse(builder.state.value.canSave, "an empty repeat")
        builder.add("plank")
        val plank = builder.state.value.addedKey!!
        assertTrue(builder.state.value.rows.single().canMoveIntoRepeat)

        builder.moveIntoRepeatAbove(plank)
        assertEquals(1, builder.state.value.steps.size)
        assertEquals(plank, builder.state.value.steps.single().repeatGroup!!.rows.single().key)
        assertTrue(builder.state.value.canSave)

        builder.moveOutOfRepeat(plank)
        assertEquals(listOf(null, plank), builder.state.value.steps.map { it.row?.key })
        assertTrue(builder.state.value.steps.first().repeatGroup!!.rows.isEmpty())
    }

    @Test
    fun dragAndMoveStayInsideTheListThatHoldsTheStep() {
        val builder = graph.createWorkout("en")
        builder.add("goblet-squat")
        builder.addRepeat()
        val repeat = builder.state.value.steps.last().key
        builder.add("push-up", repeat)
        val push = builder.state.value.addedKey!!
        builder.add("plank", repeat)
        val plank = builder.state.value.addedKey!!

        builder.move(plank, 0)
        assertEquals(listOf(plank, push), builder.state.value.steps[1].repeatGroup!!.rows.map { it.key })
        builder.moveUp(plank)
        assertEquals(listOf(plank, push), builder.state.value.steps[1].repeatGroup!!.rows.map { it.key })
        builder.move(repeat, 0)
        assertEquals(repeat, builder.state.value.steps.first().key)

        builder.remove(repeat)
        assertEquals(listOf("Goblet squat"), builder.state.value.rows.map { it.name })
    }

    @Test
    fun anExerciseAddedToARepeatStartsAtOneSet() {
        val builder = graph.createWorkout("en")
        builder.addRepeat()
        builder.add("push-up", builder.state.value.steps.single().key)
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

    @Test
    fun aWorkoutWithSeveralWorkPartsCannotBeEditedYet() {
        val saved = graph.store.latestWorkout(WorkoutId(intervalRun().save()!!))!!
        val split = saved.copy(parts = saved.parts.flatMap { part ->
            if (part.kind == PartKind.WORK) listOf(part, part.copy(id = PartId("second"))) else listOf(part)
        })

        assertNull(WorkoutDraft.from(split, graph.catalog))
    }

    @Test
    fun aStepSavedBeforeRolesIsAnExerciseStep() {
        val json = """{"type":"exercise","id":"a","exercise":"squat","prescription":{"sets":[]}}"""
        val step = Json.decodeFromString<Step>(json) as Step.ExerciseStep
        assertEquals(StepRole.WORK, step.role)
        assertEquals(Target.Open, Json.decodeFromString<Target>("""{"type":"open"}"""))
    }

    @Test
    fun aStepAddedAsAWarmUpOrCoolDownKeepsThatRole() {
        val builder = graph.createWorkout("en")
        builder.add("walk", role = BuilderRole.WARM_UP)
        builder.add("walk", role = BuilderRole.COOL_DOWN)
        assertEquals(listOf("Warm-up", "Cool down"), builder.state.value.rows.map { it.roleLabel })
    }

    @Test
    fun duplicatingPutsACopyRightAfter() {
        val builder = intervalRun()
        val warmUp = builder.state.value.rows.first()
        builder.duplicate(warmUp.key)
        val rows = builder.state.value.rows
        assertEquals(warmUp.summary, rows[1].summary)
        assertEquals("Warm-up", rows[1].roleLabel)
        assertTrue(rows[1].key != warmUp.key)

        val repeat = builder.state.value.steps[2].repeatGroup!!
        builder.duplicate(repeat.key)
        val copy = builder.state.value.steps[3].repeatGroup!!
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
        page.close()
    }

    @Test
    fun aWarmUpGoesBeforeEveryOtherStep() {
        val builder = intervalRun()
        builder.add("walk", role = BuilderRole.WARM_UP)
        val walk = builder.state.value.addedKey!!
        assertEquals(listOf("Run", "Walk"), builder.state.value.steps.take(2).map { it.row?.name })
        assertEquals(walk, builder.state.value.steps[1].key)

        // Even from a repeat's menu, a warm-up goes to the top of the workout.
        builder.add("walk", builder.state.value.steps[2].key, BuilderRole.WARM_UP)
        assertEquals(3, builder.state.value.steps.takeWhile { it.row?.role == BuilderRole.WARM_UP }.size)
    }

    @Test
    fun aWalkOrRunStartsWithADistance() {
        val builder = graph.createWorkout("en")
        builder.add("run")
        val target = builder.state.value.rows.single().target!!
        assertEquals(DurationKind.DISTANCE, target.duration)
        assertEquals("5.0 km", builder.state.value.rows.single().summary)
    }

}
