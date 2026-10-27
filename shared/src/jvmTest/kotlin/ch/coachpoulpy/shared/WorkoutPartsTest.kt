package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
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
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.withTimeout

/** The workout page and the player say which part the user is in, in both formats. */
class WorkoutPartsTest {
    private val graph = AppGraph(DriverFactory())
    private val clock = object : Clock {
        var now = Instant.fromEpochSeconds(1_700_000_000)
        override fun now() = now
        fun advance(by: Duration) { now += by }
    }

    private fun exercise(id: String, slug: String, target: Target) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(slug), Prescription(listOf(SetPrescription(target))))

    /** A 5 min walk, twice 10 push-ups and 30 s rest, a 30 s plank, then a 1 min walk. */
    private fun circuit(): Workout {
        val workout = Workout(
            WorkoutId("circuit"), 1, "Circuit", WorkoutFormat.CIRCUIT,
            listOf(
                Part(PartId("warm-up"), PartKind.WARM_UP, listOf(exercise("walk-in", "walk", Target.Time(5.minutes)))),
                Part(
                    PartId("work-1"), PartKind.WORK,
                    listOf(
                        Step.Group(
                            StepId("repeat"), GroupKind.CIRCUIT, rounds = 2,
                            children = listOf(exercise("push-up", "push-up", Target.Reps(10)), Step.Rest(StepId("rest"), 30.seconds)),
                        ),
                    ),
                ),
                Part(PartId("work-2"), PartKind.WORK, listOf(exercise("plank", "plank", Target.Hold(30.seconds)))),
                Part(PartId("cool-down"), PartKind.COOL_DOWN, listOf(exercise("walk-out", "walk", Target.Time(1.minutes)))),
            ),
        )
        graph.store.saveWorkout(workout)
        return workout
    }

    private fun player(workout: Workout) = PlayerModel(
        workout, graph.compiler.compile(workout), graph.catalog, graph.store, "en",
        executionId = "parts-${workout.id.value}", events = emptyList(),
        clock = clock, scope = CoroutineScope(StandardTestDispatcher()),
    )

    @Test
    fun theWorkoutPageGroupsStepsUnderTheirParts() {
        val page = WorkoutPageModel(circuit().id, graph.store, graph.compiler, graph.catalog, "en", scope = CoroutineScope(Dispatchers.Default))
        val state = runBlocking { withTimeout(5.seconds) { page.state.first { it.loaded } } }

        assertEquals(listOf("Warm-up", "Work 1", "Work 2", "Cool down"), state.parts.map { it.name })
        assertEquals(listOf(PartKind.WARM_UP, PartKind.WORK, PartKind.WORK, PartKind.COOL_DOWN), state.parts.map { it.kind })
        assertEquals("1 step · 5 min", state.parts[0].summary)
        assertEquals("2 rounds · about 2 min", state.parts[1].summary)
        assertEquals("1 exercise · 1 min", state.parts[2].summary)
        assertEquals("1 step · 1 min", state.parts[3].summary)
        assertEquals("Circuit", state.formatLabel)

        // No Warm-up or Cool down tag on the rows: the header says it. Rests keep Recover.
        assertNull(state.parts[0].steps.single().row!!.roleLabel)
        val repeat = state.parts[1].steps.single().repeatGroup!!
        assertEquals(listOf(null, "Recover"), repeat.rows.map { it.roleLabel })
        page.close()
    }

    @Test
    fun aCircuitShowsThePartAndTheRoundNeverASetCount() {
        val player = player(circuit())
        player.start()
        with(player.state.value) {
            assertEquals("Warm-up", partLabel)
            assertEquals(PartKind.WARM_UP, partKind)
            assertNull(roleLabel)
        }

        clock.advance(5.minutes)
        player.tick()
        with(player.state.value) {
            assertEquals("Push-up", title)
            assertEquals("Work 1 · round 1 of 2", partLabel)
            assertEquals(0, setCount)
            assertEquals("round 2 of 2", nextRound)
        }

        player.done()
        with(player.state.value) {
            assertEquals("Work 1 · round 1 of 2", partLabel)
            assertEquals("Recover", roleLabel)
            assertEquals("round 2 of 2", nextRound)
        }

        clock.advance(30.seconds)
        player.tick()
        with(player.state.value) {
            assertEquals("Work 1 · round 2 of 2", partLabel)
            assertNull(nextRound, "the plank is outside the repeat")
        }

        player.done()
        clock.advance(30.seconds)
        player.tick()
        assertEquals("Work 2", player.state.value.partLabel)
        clock.advance(30.seconds)
        player.tick()
        with(player.state.value) {
            assertEquals("Cool down", partLabel)
            assertEquals(PartKind.COOL_DOWN, partKind)
        }
    }

    @Test
    fun aSetsWorkoutCountsSetsOutsideARepeat() {
        val builder = graph.createWorkout("fr")
        builder.rename("Jambes")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val player = PlayerModel(
            workout, graph.compiler.compile(workout), graph.catalog, graph.store, "fr",
            executionId = "parts-sets", events = emptyList(),
            clock = clock, scope = CoroutineScope(StandardTestDispatcher()),
        )

        player.start()
        with(player.state.value) {
            assertEquals("Corps de séance", partLabel)
            assertEquals(1, setNumber)
            assertEquals(3, setCount)
        }
        player.done()
        clock.advance(1.minutes)
        player.tick()
        assertEquals(2, player.state.value.setNumber)
    }
}
