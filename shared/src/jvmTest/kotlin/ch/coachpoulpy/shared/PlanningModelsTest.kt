package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class PlanningModelsTest {
    private val graph = AppGraph(DriverFactory())

    // Wednesday 2023-11-15 in UTC.
    private val now = Instant.parse("2023-11-15T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }
    private val today = LocalDate(2023, 11, 15)

    private fun scope() = CoroutineScope(Dispatchers.Default)
    private fun home() = HomeModel(graph.store, graph.compiler, graph.catalog, "en", clock, TimeZone.UTC, scope())
    private fun library() = LibraryModel(graph.store, graph.compiler, graph.catalog, "en", clock, TimeZone.UTC, scope())
    private fun page(id: WorkoutId) = WorkoutPageModel(id, graph.store, graph.compiler, graph.catalog, "en", clock, TimeZone.UTC, scope())

    private fun <T> StateFlow<T>.await(predicate: (T) -> Boolean): T = runBlocking {
        withTimeout(5.seconds) { first(predicate) }
    }

    private fun workout(name: String, vararg exercises: String): WorkoutId {
        val builder = graph.createWorkout("en")
        builder.rename(name)
        builder.setType(WorkoutType.STRENGTH)
        exercises.forEach { builder.add(it) }
        return WorkoutId(builder.save()!!)
    }

    /** A session of [id] started half an hour ago; played to the end unless [finish] is false. */
    private fun play(id: WorkoutId, finish: Boolean = true) {
        val workout = graph.store.latestWorkout(id)!!
        val player = PlayerModel(
            workout, graph.compiler.compile(workout), graph.catalog, graph.store, "en",
            executionId = "run-${id.value}", events = emptyList(),
            clock = object : Clock {
                var calls = 0
                override fun now() = now - 30.minutes + (calls++).minutes
            },
            scope = CoroutineScope(StandardTestDispatcher()),
        )
        player.start()
        if (finish) {
            player.done()
            player.stop()
        }
    }

    @Test
    fun homeShowsOnlyTodayAndTheNextPlannedSession() {
        val legs = workout("Legs", "goblet-squat", "push-up")
        val arms = workout("Arms", "push-up")
        workout("Never planned", "push-up")
        graph.store.planWorkout(legs, today)
        graph.store.planWorkout(arms, LocalDate(2023, 11, 17))
        graph.store.planWorkout(legs, LocalDate(2023, 11, 20))

        val state = home().state.await { it.loaded && it.today.isNotEmpty() && it.next != null }

        with(state.today.single()) {
            assertEquals(TodayStatus.TO_DO, status)
            assertEquals("Legs", title)
            assertEquals("Strength · 2 exercises · about 6 min", details.replace(Regex("about \\d+ min"), "about 6 min"))
            assertEquals("Goblet squat, Push-up", exercises)
        }
        with(state.next!!) {
            assertEquals("Arms", title)
            assertEquals(2, daysAway)
        }
    }

    @Test
    fun aSessionDoneTodayTicksOffItsPlan() {
        val legs = workout("Legs", "push-up")
        graph.store.planWorkout(legs, today)
        play(legs)

        val state = home().state.await { it.loaded && it.today.isNotEmpty() }

        with(state.today.single()) {
            assertEquals(TodayStatus.DONE, status)
            assertEquals("Legs", title)
            assertEquals(1, session!!.setsDone)
            assertEquals(legs.value, workoutId)
        }
    }

    @Test
    fun aSessionLeftHalfwayIsInProgressInPlaceOfItsPlan() {
        val legs = workout("Legs", "push-up")
        val planId = graph.store.planWorkout(legs, today)
        play(legs, finish = false)

        val model = home()
        model.refresh()
        val state = model.state.await { it.loaded && it.today.isNotEmpty() }

        with(state.today.single()) {
            assertEquals(TodayStatus.IN_PROGRESS, status)
            assertEquals(planId, this.planId)
        }
    }

    @Test
    fun nothingPlannedTodayLeavesTodayEmpty() {
        workout("Legs", "push-up")
        val state = home().state.await { it.loaded && it.hasWorkouts }
        assertTrue(state.today.isEmpty())
        assertNull(state.next)
    }

    @Test
    fun libraryPutsPlansFirstAndSortsByLastDone() {
        val legs = workout("Legs", "push-up")
        val arms = workout("Arms", "push-up")
        workout("Zebra", "push-up")
        play(legs)
        graph.store.planWorkout(arms, LocalDate(2023, 11, 17))

        val model = library()
        val state = model.state.await { it.loaded && it.rows.size == 3 && it.rows.any { r -> r.note == LibraryNote.DONE_TODAY } }
        val rows = model.filter(state).rows

        assertEquals(listOf("Legs", "Arms", "Zebra"), rows.map { it.name })
        assertEquals(listOf(LibraryNote.DONE_TODAY, LibraryNote.PLANNED, LibraryNote.NOT_DONE_YET), rows.map { it.note })
        assertEquals(listOf("Zebra"), model.filter(state, query = "zeb").rows.map { it.name })
        assertEquals(listOf("Arms", "Legs", "Zebra"), model.filter(state, sort = LibrarySort.ALPHABETICAL).rows.map { it.name })
        assertEquals(listOf("Arms", "Legs", "Zebra"), model.filter(state, sort = LibrarySort.NEXT_PLANNED).rows.map { it.name })
        assertEquals(listOf(WorkoutType.STRENGTH), state.kinds.map { it.type })
    }

    @Test
    fun librarySearchForgivesAndLooksInsideWorkouts() {
        workout("Lower body strength", "goblet-squat")
        workout("Upper body", "push-up")
        workout("Équilibre et gainage", "push-up")

        val model = library()
        val state = model.state.await { it.loaded && it.rows.size == 3 }
        fun names(query: String) = model.filter(state, query = query).rows.map { it.name }

        // An exercise inside, in either language and through an alias.
        assertEquals(listOf("Lower body strength"), names("squats"))
        assertEquals(listOf("Équilibre et gainage", "Upper body"), names("pompe"))
        // "up" starts Upper body's name, so it comes before a workout matching only by its exercise.
        assertEquals(listOf("Upper body", "Équilibre et gainage"), names("press-up"))
        // Accents and case ignored, words in any order.
        assertEquals(listOf("Équilibre et gainage"), names("GAINAGE equilibre"))
        // A word of the name before the type ("Strength", "Renforcement"), ties by name.
        assertEquals(listOf("Lower body strength", "Équilibre et gainage", "Upper body"), names("str"))
        assertEquals(3, names("renfo").size)
        assertEquals(emptyList(), names("zumba"))

        val listing = model.filter(state, query = "pompe")
        assertTrue(listing.searching)
        assertEquals("2 matches", listing.countLabel)
        assertEquals("3 workouts", model.filter(state).countLabel)
    }

    @Test
    fun workoutPagePlansMovesAndUnplans() {
        val legs = workout("Legs", "goblet-squat")
        val model = page(legs)
        val empty = model.state.await { it.loaded && it.found }
        assertNull(empty.plan)
        assertEquals("Goblet squat", empty.steps.single().name)
        assertTrue(empty.steps.single().prescription.startsWith("3 × 10"))
        assertTrue(empty.editable)

        model.plan(2023, 11, 17)
        assertEquals(2, model.state.await { it.plan != null }.plan!!.daysAway)

        model.plan(2023, 11, 16)
        assertEquals(1, model.state.await { it.plan?.daysAway == 1 }.plan!!.daysAway)
        assertEquals(1, runBlocking { graph.store.plannedWorkouts().first() }.size)

        model.unplan()
        model.state.await { it.plan == null }

        model.delete()
        assertFalse(model.state.await { !it.found }.found)
    }

    @Test
    fun workoutPageCountsSessionsDone() {
        val legs = workout("Legs", "push-up")
        play(legs)
        val state = page(legs).state.await { it.loaded && it.timesDone == 1 }
        // Started half an hour ago, give or take the player's own clock reads.
        val ago = now.toEpochMilliseconds() - state.lastDoneMillis!!
        assertTrue(ago in 0..30.minutes.inWholeMilliseconds, "started $ago ms ago")
    }
}
