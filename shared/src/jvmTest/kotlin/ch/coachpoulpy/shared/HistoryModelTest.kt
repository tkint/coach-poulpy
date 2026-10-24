package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.TimeZone

class HistoryModelTest {
    private val graph = AppGraph(DriverFactory())

    // Wednesday 2023-11-15 in UTC.
    private val now = Instant.parse("2023-11-15T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }

    private fun history() = HistoryModel(
        graph.store, graph.compiler, "en", clock, TimeZone.UTC,
        scope = CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
    )

    /** The state once it shows [count] sessions, each summed up. */
    private fun HistoryModel.awaitSessions(count: Int): HistoryState = runBlocking {
        withTimeout(5.seconds) { state.first { it.loaded && it.sessions.size == count } }
    }

    private fun strengthWorkout(): WorkoutId {
        val builder = graph.createWorkout("en")
        builder.rename("Quick")
        builder.setType(WorkoutType.STRENGTH)
        builder.add("push-up")
        return WorkoutId(builder.save()!!)
    }

    @Test
    fun guidedSessionShowsItsSetsAndCountsThisWeek() {
        val id = strengthWorkout()
        val workout = graph.store.latestWorkout(id)!!
        val player = PlayerModel(
            workout, graph.compiler.compile(workout), graph.catalog, graph.store, "en",
            executionId = "run-1", events = emptyList(),
            clock = object : Clock {
                var calls = 0
                // Each action happens a minute after the last one.
                override fun now() = now - 1.days + (calls++).minutes
            },
            scope = CoroutineScope(StandardTestDispatcher()),
        )
        player.start()
        player.done()
        player.stop()

        val state = history().awaitSessions(1)
        with(state.sessions.single()) {
            assertEquals("Quick", title)
            assertEquals(SessionKind.WORKOUT, kind)
            assertEquals("Strength", typeLabel)
            assertEquals(1, setsDone)
            assertNull(distanceMeters)
        }
        assertEquals(1, state.thisWeek.sessions)
        assertEquals(1, state.thisMonth.sessions)
        assertEquals(8, state.weeks.size)
        assertEquals(true, state.weeks.last().isCurrent)
    }

    @Test
    fun sessionsFromBeforeTheHistoryAreSummedUpOnce() {
        val start = now - 2.days
        graph.store.startExecution(Execution(ExecutionId("old-run"), ExecutionSource.DEVICE_ONLY, startedAt = start))
        listOf(
            SessionEvent.Started(start),
            SessionEvent.Sample(start + 1.seconds, DeviceMetrics(speedKmh = 6.0, distanceMeters = 100.0, heartRateBpm = 110)),
            SessionEvent.Sample(start + 10.minutes, DeviceMetrics(speedKmh = 6.0, distanceMeters = 1100.0, heartRateBpm = 130)),
            SessionEvent.PhaseCompleted(start + 10.minutes, 0),
            SessionEvent.Finished(start + 10.minutes),
        ).forEach { graph.store.appendEvent("old-run", it) }
        graph.store.finishExecution("old-run", start + 10.minutes)

        val row = history().awaitSessions(1).sessions.single()
        assertEquals("Free run", row.title)
        assertEquals(SessionKind.TREADMILL, row.kind)
        assertEquals(1000.0, row.distanceMeters)
        assertEquals(130, row.maxHeartRateBpm)
        assertEquals(600, row.activeSeconds)
        assertNotNull(row.estimatedSteps)
        // Saved, so the next opening reads it without replaying the events.
        val saved = runBlocking { graph.store.finishedSessions().first() }.single()
        assertEquals(1000.0, saved.totals?.distanceMeters)
        assertEquals(WorkoutType.TREADMILL, saved.type)
    }

    @Test
    fun keepsTheNameOfAWorkoutDeletedAfterwards() {
        val id = strengthWorkout()
        val start = now - 1.days
        graph.store.startExecution(Execution(ExecutionId("e1"), ExecutionSource.GUIDED, startedAt = start, workout = WorkoutRef(id, 1)), "Quick", WorkoutType.STRENGTH)
        graph.store.appendEvent("e1", SessionEvent.Started(start))
        graph.store.appendEvent("e1", SessionEvent.Finished(start + 5.minutes))
        graph.store.finishExecution("e1", start + 5.minutes)
        graph.deleteWorkout(id.value)

        assertEquals("Quick", history().awaitSessions(1).sessions.single().title)
    }

    @Test
    fun hidesSessionsWhereNothingWasDoneAndDeletesOthers() {
        graph.store.startExecution(Execution(ExecutionId("nothing"), ExecutionSource.DEVICE_ONLY, startedAt = now))
        graph.store.finishExecution("nothing", now)
        val start = now - 1.days
        graph.store.startExecution(Execution(ExecutionId("walk"), ExecutionSource.DEVICE_ONLY, startedAt = start))
        graph.store.appendEvent("walk", SessionEvent.Started(start))
        graph.store.appendEvent("walk", SessionEvent.Finished(start + 20.minutes))
        graph.store.finishExecution("walk", start + 20.minutes)

        val model = history()
        assertEquals(listOf("walk"), model.awaitSessions(1).sessions.map { it.id })
        assertEquals(20, model.current.thisWeek.activeMinutes)

        model.delete("walk")
        assertEquals(0, model.awaitSessions(0).thisWeek.sessions)
    }

    private fun walk(id: String, start: Instant, minutes: Int = 20, meters: Double? = null) {
        graph.store.startExecution(Execution(ExecutionId(id), ExecutionSource.DEVICE_ONLY, startedAt = start))
        graph.store.appendEvent(id, SessionEvent.Started(start))
        if (meters != null) {
            graph.store.appendEvent(id, SessionEvent.Sample(start + 1.seconds, DeviceMetrics(speedKmh = 5.0, distanceMeters = 0.0)))
            graph.store.appendEvent(id, SessionEvent.Sample(start + minutes.minutes, DeviceMetrics(speedKmh = 5.0, distanceMeters = meters)))
        }
        graph.store.appendEvent(id, SessionEvent.Finished(start + minutes.minutes))
        graph.store.finishExecution(id, start + minutes.minutes)
    }

    @Test
    fun calendarShowsTheMonthMondayFirstWithActiveDaysAndTheSelectedRestDay() {
        // November 2023 starts on a Wednesday; "now" is Wednesday the 15th.
        walk("a", Instant.parse("2023-11-13T08:00:00Z"))
        walk("b", Instant.parse("2023-11-13T18:00:00Z"))
        walk("c", Instant.parse("2023-11-02T08:00:00Z"))
        walk("old", Instant.parse("2023-09-20T08:00:00Z"))
        val model = history()
        val state = model.awaitSessions(4)
        assertEquals(-2, state.firstMonthOffset)

        val month = model.month(state, offset = 0, selectedDay = 15)
        assertEquals(2023 to 11, month.year to month.month)
        assertEquals(2, month.leadingBlanks)
        assertEquals(30, month.days.size)
        assertEquals(2, month.activeDays)
        assertEquals(2, month.days[12].sessionCount)
        assertEquals(true, month.days[14].isToday)
        assertEquals(true, month.days[15].isFuture)
        assertEquals(false, month.canGoForward)
        assertEquals(true, month.canGoBack)
        // Newest first, with today kept as a rest day.
        assertEquals(listOf(15, 13, 2), month.sections.map { it.day })
        assertEquals(0, month.sections[0].sessions.size)
        assertEquals(listOf("b", "a"), month.sections[1].sessions.map { it.id })

        val september = model.month(state, offset = -2, selectedDay = 0)
        assertEquals(listOf(20), september.sections.map { it.day })
        assertEquals(false, september.canGoBack)
        assertEquals(true, september.canGoForward)
        // A future day cannot be selected.
        assertEquals(listOf(13, 2), model.month(state, offset = 0, selectedDay = 20).sections.map { it.day })
    }

    @Test
    fun statsKeepTheLongestRunAndCountPoulpysTentacles() {
        (1..9).forEach { walk("w$it", now - it.days, meters = if (it == 4) 5200.0 else 1000.0 * it / 2) }
        val state = history().awaitSessions(9)
        assertEquals(9, state.totalSessions)
        assertEquals(5200.0, state.longestRunMeters)
        assertEquals((now - 4.days).toEpochMilliseconds(), state.longestRunMillis)
        assertEquals(1, state.fullOctopuses)
        assertEquals(1, state.tentaclesLit)
    }
}
