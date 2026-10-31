package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.StoredSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.TimeZone

/** A session left open when the app was closed: offered on Home, opened, discarded or closed with its totals. */
class UnfinishedSessionTest {
    private val graph = AppGraph(DriverFactory())
    private val start = Instant.parse("2023-11-15T12:00:00Z")

    private fun workout(name: String = "Quick"): WorkoutId {
        val builder = graph.createWorkout("en")
        builder.rename(name)
        builder.chooseFormat(WorkoutFormat.SETS)
        builder.add("push-up", builder.work)
        return WorkoutId(builder.save()!!)
    }

    /** A guided session of [workoutId] left open 12 min 40 s in, after its first set. */
    private fun leftInPlayer(workoutId: WorkoutId): String {
        val workout = graph.store.latestWorkout(workoutId)!!
        graph.store.startExecution(
            Execution(ExecutionId("left"), ExecutionSource.GUIDED, startedAt = start, workout = WorkoutRef(workout.id, workout.version)),
            title = workout.name,
            format = workout.format,
        )
        graph.store.appendEvent("left", SessionEvent.Started(start))
        graph.store.appendEvent("left", SessionEvent.PhaseCompleted(start + 12.minutes + 40.seconds, 0))
        return "left"
    }

    /** A free walk recorded in Live, killed 20 minutes in, after its last treadmill reading. */
    private fun leftInLive(): String {
        graph.store.startExecution(Execution(ExecutionId("walk"), ExecutionSource.DEVICE_ONLY, startedAt = start), title = "Walk")
        graph.store.appendEvent("walk", SessionEvent.Started(start))
        graph.store.appendEvent("walk", SessionEvent.Sample(start + 1.seconds, DeviceMetrics(speedKmh = 5.0, distanceMeters = 0.0)))
        graph.store.appendEvent("walk", SessionEvent.Sample(start + 20.minutes, DeviceMetrics(speedKmh = 5.0, distanceMeters = 1_600.0)))
        return "walk"
    }

    private fun finished(): List<StoredSession> = runBlocking { graph.store.finishedSessions().first() }

    private fun home() = HomeModel(graph.store, graph.compiler, graph.catalog, "en", zone = TimeZone.UTC, scope = CoroutineScope(Dispatchers.Default))

    private fun HomeModel.openCard(): TodayItem? =
        runBlocking { withTimeout(5.seconds) { state.first { it.loaded } } }.today.firstOrNull { it.status == TodayStatus.IN_PROGRESS }

    /** The state once its card for the session left open is gone. */
    private fun HomeModel.awaitNoOpenCard() = runBlocking {
        withTimeout(5.seconds) { state.first { it.loaded && it.today.none { item -> item.status == TodayStatus.IN_PROGRESS } } }
    }

    @Test
    fun nothingLeftOpenShowsNoCard() {
        assertNull(home().openCard())
    }

    @Test
    fun aSessionLeftInThePlayerShowsACardWithItsWorkout() {
        val workoutId = workout("Quick")
        val id = leftInPlayer(workoutId)

        val card = assertNotNull(home().openCard())
        assertEquals("open:$id", card.key)
        assertEquals("Quick", card.title)
        assertEquals(workoutId.value, card.workoutId)
        assertEquals(false, card.live)
    }

    @Test
    fun discardLeavesNoTrace() {
        val id = leftInPlayer(workout())
        val home = home()
        assertNotNull(home.openCard())

        home.discardUnfinished()

        home.awaitNoOpenCard()
        assertNull(graph.store.unfinishedExecution())
        assertTrue(graph.store.events(id).isEmpty())
        assertTrue(finished().isEmpty())
    }

    @Test
    fun aSessionOfADeletedWorkoutShowsNoCard() {
        val workoutId = workout()
        leftInPlayer(workoutId)
        graph.deleteWorkout(workoutId.value)

        assertNull(home().openCard())
    }

    @Test
    fun aLiveRecordingShowsACardThatOpensLive() {
        val id = leftInLive()

        val card = assertNotNull(home().openCard())
        assertEquals("open:$id", card.key)
        assertEquals("Walk", card.title)
        assertTrue(card.live)
        assertNull(card.workoutId)
    }

    @Test
    fun discardingTheCardOfALiveRecordingDeletesIt() {
        val id = leftInLive()
        val home = home()
        assertNotNull(home.openCard())

        home.discardUnfinished()

        assertNull(graph.store.unfinishedExecution())
        assertTrue(graph.store.events(id).isEmpty())
    }

    @Test
    fun resumingInThePlayerLeavesALiveRecordingOpen() {
        val id = leftInLive()

        assertNull(graph.resumeSession("en"), "Live brings it back, not the player")

        assertEquals(id, graph.store.unfinishedExecution()?.id?.value)
        assertTrue(finished().isEmpty())
    }

    @Test
    fun startingAnotherWorkoutClosesTheOneLeftOpenAtItsLastEventWithItsTotals() {
        val first = workout("First")
        val id = leftInPlayer(first)

        assertNotNull(graph.player(workout("Second").value, "en"))

        val closed = finished().single()
        assertEquals(id, closed.execution.id.value)
        assertEquals(start + 12.minutes + 40.seconds, closed.execution.endedAt)
        val totals = assertNotNull(closed.totals)
        assertEquals(12.minutes + 40.seconds, totals.active)
        assertEquals(1, totals.setsDone)
    }

    @Test
    fun startingAWorkoutClosesALiveRecordingLeftOpenWithItsDistance() {
        val id = leftInLive()

        assertNotNull(graph.player(workout().value, "en"))

        val closed = finished().single()
        assertEquals(id, closed.execution.id.value)
        assertEquals(start + 20.minutes, closed.execution.endedAt)
        assertEquals(1_600.0, closed.totals?.distanceMeters)
    }
}
