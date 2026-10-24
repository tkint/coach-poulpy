package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher

class PlayerModelTest {
    private val graph = AppGraph(DriverFactory())
    private val clock = object : Clock {
        var now = Instant.fromEpochSeconds(1_700_000_000)
        override fun now() = now
        fun advance(by: Duration) { now += by }
    }

    /** Push-ups 2 x 10 with 45 s rest, then a 30 s plank. */
    private fun workout(): WorkoutId {
        val builder = graph.newWorkout("en")
        builder.rename("Quick")
        builder.setType(WorkoutType.STRENGTH)
        builder.add("push-up")
        builder.add("plank")
        val (push, plank) = builder.state.value.rows.map { it.key }
        builder.setSets(push, 2)
        builder.changeRest(push, -1)
        builder.setSets(plank, 1)
        return WorkoutId(builder.save()!!)
    }

    private fun player(id: WorkoutId): PlayerModel {
        val workout = graph.store.latestWorkout(id)!!
        return PlayerModel(
            workout, graph.compiler.compile(workout), graph.catalog, graph.store, "en",
            executionId = "run-1", events = graph.store.events("run-1"),
            clock = clock, scope = CoroutineScope(StandardTestDispatcher()),
        )
    }

    @Test
    fun guidesThroughSetsRestsAndTimers() {
        val player = player(workout())
        assertEquals(PlayerStatus.READY, player.state.value.status)

        player.start()
        with(player.state.value) {
            assertEquals("Push-up", title)
            assertEquals(1, setNumber)
            assertEquals(2, setCount)
            assertEquals("10 reps", targetLabel)
            assertEquals(false, isTimed)
        }

        player.done()
        with(player.state.value) {
            assertEquals(true, isRest)
            assertEquals(45, remainingSeconds)
            assertEquals("Push-up", nextTitle)
        }

        clock.advance(45.seconds)
        player.tick()
        assertEquals(2, player.state.value.setNumber)

        player.done()
        clock.advance(45.seconds)
        player.tick()
        with(player.state.value) {
            assertEquals("Plank", title)
            assertEquals(30, remainingSeconds)
        }

        clock.advance(30.seconds)
        player.tick()
        with(player.state.value) {
            assertEquals(PlayerStatus.FINISHED, status)
            assertEquals(SessionSummary("2 min", setsDone = 3, setsSkipped = 0, setsPlanned = 3), summary)
        }
        assertNull(graph.store.unfinishedExecution())
    }

    @Test
    fun sessionResumesWhereItWasAfterTheAppIsKilled() {
        val id = workout()
        val first = player(id)
        first.start()
        first.done()
        clock.advance(10.seconds)

        // The app is killed and reopened 10 s into the rest.
        val resumed = player(id)
        with(resumed.state.value) {
            assertEquals(PlayerStatus.RUNNING, status)
            assertEquals(true, isRest)
            assertEquals(35, remainingSeconds)
        }
        assertNotNull(graph.store.unfinishedExecution())
    }

    @Test
    fun stoppingEarlyKeepsWhatWasDone() {
        val player = player(workout())
        player.start()
        player.done()
        player.stop()
        assertEquals(1, player.state.value.summary?.setsDone)
        assertNull(graph.store.unfinishedExecution())
    }
}
