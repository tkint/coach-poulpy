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
import kotlinx.datetime.atStartOfDayIn

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
    fun workoutPagePlansSeveralDaysAndKeepsThePast() {
        val legs = workout("Legs", "goblet-squat")
        val past = LocalDate(2023, 11, 10)
        graph.store.planWorkout(legs, past)
        val model = page(legs)
        val empty = model.state.await { it.loaded && it.found }
        assertTrue(empty.plans.isEmpty())
        assertEquals("Goblet squat", empty.steps.single().name)
        assertTrue(empty.steps.single().prescription.startsWith("3 × 10"))
        assertTrue(empty.editable)

        // A past day is ignored: only today on can be planned.
        model.setDays(listOf(PlanDate(2023, 11, 20), PlanDate(2023, 11, 17), PlanDate(2023, 11, 1)))
        val two = model.state.await { it.plans.size == 2 }
        assertEquals(listOf(PlanDate(2023, 11, 17), PlanDate(2023, 11, 20)), two.plans.map { it.date })
        assertEquals(listOf(2, 5), two.plans.map { it.daysAway })

        // Ticking 16, unticking 20: 17 keeps its plan, the past one stays untouched.
        val kept = two.plans.first().planId
        model.setDays(listOf(PlanDate(2023, 11, 16), PlanDate(2023, 11, 17)))
        val moved = model.state.await { it.plans.map { p -> p.date.day } == listOf(16, 17) }
        assertEquals(kept, moved.plans[1].planId)
        val stored = runBlocking { graph.store.plannedWorkouts().first() }
        assertEquals(listOf(past, LocalDate(2023, 11, 16), LocalDate(2023, 11, 17)), stored.map { it.day })

        val library = library()
        val row = library.state.await { it.loaded && it.rows.isNotEmpty() && it.rows.single().morePlanned == 1 }.rows.single()
        assertEquals(LibraryNote.PLANNED, row.note)
        assertEquals(LocalDate(2023, 11, 16).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(), row.noteMillis)

        model.setDays(emptyList())
        model.state.await { it.plans.isEmpty() }
        assertEquals(listOf(past), runBlocking { graph.store.plannedWorkouts().first() }.map { it.day })

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

    @Test
    fun aCopyIsAnUnsavedDraftWithNoDaysOrHistory() {
        val legs = workout("Legs", "goblet-squat", "push-up")
        graph.store.planWorkout(legs, LocalDate(2023, 11, 17))
        play(legs)
        val saved = runBlocking { graph.store.workouts().first() }.size

        val copy = graph.copyWorkout(legs.value, "en")!!
        assertEquals("Legs (copy)", copy.state.value.name)
        assertEquals(2, copy.state.value.rows.size)
        assertEquals(saved, runBlocking { graph.store.workouts().first() }.size)

        val id = copy.save()!!
        assertTrue(id != legs.value)
        val page = page(WorkoutId(id)).state.await { it.loaded && it.found }
        assertTrue(page.plans.isEmpty())
        assertEquals(0, page.timesDone)
        assertEquals("Haut du corps (copie)", Labels("fr").copyName("Haut du corps"))
    }

    @Test
    fun deletingSaysWhatGoesAndWhatStays() {
        val legs = workout("Legs", "goblet-squat")
        graph.store.planWorkout(legs, LocalDate(2023, 11, 17))
        graph.store.planWorkout(legs, LocalDate(2023, 11, 20))
        play(legs)

        val prompt = page(legs).state.await { it.found && it.timesDone == 1 && it.plans.size == 2 }.deletePrompt
        assertEquals("Delete Legs?", prompt.title)
        assertEquals("It comes off its 2 planned days. The time you did it stays in History.", prompt.message)

        val library = library()
        val row = library.state.await { it.loaded && it.rows.singleOrNull()?.deletePrompt?.title == "Delete Legs?" }.rows.single()
        assertEquals(prompt, row.deletePrompt)
        assertEquals("This can’t be undone.", Labels("en").deletePrompt("Arms", 0, 0).message)
        assertEquals("Les 3 fois où vous l’avez faite restent dans l’historique.", Labels("fr").deletePrompt("Bras", 0, 3).message)

        library.delete(row)
        library.state.await { it.loaded && it.rows.isEmpty() }
        assertTrue(runBlocking { graph.store.plannedWorkouts().first() }.isEmpty())
    }

}
