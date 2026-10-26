package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
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
        builder.setType(WorkoutType.TREADMILL)

        builder.add("run")
        val warmUp = builder.state.value.addedKey!!
        builder.setRole(warmUp, StepRole.WARM_UP)
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
        builder.setRole(easy, StepRole.RECOVER)
        builder.setDuration(easy, "1:30")

        builder.add("run")
        val coolDown = builder.state.value.addedKey!!
        builder.setRole(coolDown, StepRole.COOL_DOWN)
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

        val repeat = assertNotNull(state.steps[1].repeat)
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
        assertEquals(StepRole.WARM_UP, work.first().role)
        assertEquals(listOf(1, 1, 2, 2, 3, 3, 4, 4), work.drop(1).dropLast(1).map { it.round })
        assertTrue(work.drop(1).dropLast(1).all { it.rounds == 4 })
        assertEquals(Target.Time(90.seconds), work[2].target)
        assertEquals(StepRole.RECOVER, work[2].role)
        assertEquals(Target.Open, work.last().target)
        assertEquals(StepRole.COOL_DOWN, work.last().role)
    }

    @Test
    fun anIntervalRunComesBackTheSameWhenEdited() {
        val id = intervalRun().save()!!
        val saved = graph.store.latestWorkout(WorkoutId(id))!!
        val edit = assertNotNull(graph.editWorkout(id, "en"))
        val state = edit.state.value
        assertEquals(listOf("Warm-up", null, "Recover", "Cool down"), state.rows.map { it.roleLabel })
        assertEquals(4, state.steps[1].repeat?.times)
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
        assertEquals("3 × 10 · rest 1 min", rows[0].summary)
        assertEquals("1 × 10 · rest 1 min", rows[1].summary)
        assertTrue(rows[2].isRest)
        assertEquals("Recover", rows[2].roleLabel)
        assertEquals("1 min 15", rows[2].summary)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val group = workout.steps[1] as Step.Group
        assertEquals(3, group.rounds)
        assertEquals(75.seconds, (group.children[1] as Step.Rest).duration)
        val phases = graph.compiler.compile(workout).phases
        // Squat: 3 sets with rests between them and after the last; then 3 rounds of push-up and rest, the very last rest dropped.
        assertEquals(3, phases.take(6).count { it is Phase.Work })
        assertEquals(3, phases.drop(6).count { it is Phase.Work })
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
        assertEquals(plank, builder.state.value.steps.single().repeat!!.rows.single().key)
        assertTrue(builder.state.value.canSave)

        builder.moveOutOfRepeat(plank)
        assertEquals(listOf(null, plank), builder.state.value.steps.map { it.row?.key })
        assertTrue(builder.state.value.steps.first().repeat!!.rows.isEmpty())
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
        assertEquals(listOf(plank, push), builder.state.value.steps[1].repeat!!.rows.map { it.key })
        builder.moveUp(plank)
        assertEquals(listOf(plank, push), builder.state.value.steps[1].repeat!!.rows.map { it.key })
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
    fun theGuidedSessionSaysTheRoleAndTheRound() {
        val workout = graph.store.latestWorkout(WorkoutId(intervalRun().save()!!))!!
        val player = PlayerModel(
            workout, graph.compiler.compile(workout), graph.catalog, graph.store, "en",
            executionId = "steps-1", events = graph.store.events("steps-1"),
            scope = CoroutineScope(StandardTestDispatcher()),
        )
        player.start()
        with(player.state.value) {
            assertEquals("Warm-up", roleLabel)
            assertNull(roundLabel)
            assertEquals("Until you tap Next", targetLabel)
            assertFalse(isTimed)
        }
        player.done()
        assertEquals("Round 1 of 4", player.state.value.roundLabel)
        assertNull(player.state.value.roleLabel)
    }

    @Test
    fun aStepSavedBeforeRolesIsAnExerciseStep() {
        val json = """{"type":"exercise","id":"a","exercise":"squat","prescription":{"sets":[]}}"""
        val step = Json.decodeFromString<Step>(json) as Step.ExerciseStep
        assertEquals(StepRole.WORK, step.role)
        assertEquals(Target.Open, Json.decodeFromString<Target>("""{"type":"open"}"""))
    }
}
