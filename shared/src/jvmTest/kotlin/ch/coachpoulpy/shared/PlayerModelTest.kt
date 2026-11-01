package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
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

    /** Twice 10 push-ups and a 45 s rest, then a 30 s plank. */
    private fun workout(): WorkoutId {
        val builder = graph.createWorkout("en")
        builder.rename("Quick")
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.addRepeat(builder.work)
        val repeat = builder.state.value.parts.single().steps.single().key
        builder.add("push-up", builder.work, repeat)
        builder.addRest(builder.work, repeat)
        builder.changeRest(builder.state.value.addedKey!!, -1)
        builder.add("plank", builder.work)
        builder.setSets(builder.state.value.addedKey!!, 1)
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
            assertEquals("Work · round 1 of 2", partLabel)
            assertEquals(0, setCount, "a repeat counts rounds, not sets")
            assertEquals("10 reps", targetLabel)
            assertEquals(false, isTimed)
        }

        player.done()
        with(player.state.value) {
            assertEquals(true, isRest)
            assertEquals(45, remainingSeconds)
            assertEquals("Push-up", nextTitle)
            assertEquals("round 2 of 2", nextRound)
            assertEquals("Recover", roleLabel)
        }

        clock.advance(45.seconds)
        player.tick()
        assertEquals("Work · round 2 of 2", player.state.value.partLabel)

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
        }
        assertNull(graph.store.unfinishedExecution())
        val finished = player.finished(UnitSystem.METRIC)!!
        assertEquals("Well done!", finished.heading)
        assertEquals("3 of 3 sets done in 2 min", finished.summary)
        assertEquals("Quick", finished.workoutName)
        assertEquals(clock.now().toEpochMilliseconds() - 2.minutes.inWholeMilliseconds, finished.startedAtMillis)
        assertEquals(listOf("Time" to "2:00", "Sets" to "3"), finished.stats.map { it.label to it.value })
        assertEquals(1, finished.poulpy.tentaclesLit)
    }

    @Test
    fun noFinishedScreenBeforeTheSessionEnds() {
        val player = player(workout())
        player.start()
        assertNull(player.finished(UnitSystem.METRIC))
    }

    @Test
    fun sessionComesBackPausedAtItsLastEventAfterTheAppIsKilled() {
        val id = workout()
        val first = player(id)
        first.start()
        first.done()
        clock.advance(10.seconds)

        // The app is killed 10 s into the rest and reopened an hour later.
        clock.advance(1.hours)
        val resumed = player(id)
        with(resumed.state.value) {
            assertEquals(PlayerStatus.PAUSED, status)
            assertEquals(true, isRest)
            assertEquals(45, remainingSeconds) // nothing was written after the set: the rest picks up from its start
        }
        assertNotNull(graph.store.unfinishedExecution())

        resumed.resume()
        clock.advance(5.seconds)
        resumed.tick()
        assertEquals(40, resumed.state.value.remainingSeconds)
    }

    @Test
    fun stoppingEarlyKeepsWhatWasDone() {
        val player = player(workout())
        player.start()
        player.done()
        player.stop()
        assertEquals(true, player.finished(UnitSystem.METRIC)?.summary?.startsWith("1 of 3 sets done"))
        assertNull(graph.store.unfinishedExecution())
    }

    @Test
    fun theSharedImageIsTheSameStraightAfterTheSessionAndFromTheHistory() {
        val player = player(workout())
        player.start()
        player.done()
        clock.advance(45.seconds)
        player.tick()
        player.done()
        clock.advance(45.seconds)
        player.tick()
        clock.advance(30.seconds)
        player.tick()
        val fresh = assertNotNull(player.shareCard(UnitSystem.METRIC))
        assertEquals(PoulpyDrawing.EXHAUSTED, fresh.drawing)
        assertEquals(fresh, graph.sessionShareCard("run-1", UnitSystem.METRIC, "en"))
    }
}
