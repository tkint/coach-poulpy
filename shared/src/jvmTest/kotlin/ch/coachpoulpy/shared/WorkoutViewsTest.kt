package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.ExerciseSlug
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/** What the website is told about saved workouts: the list's filters, order and pages, and one workout's page, in either language. */
class WorkoutViewsTest {
    private val views = WorkoutViews(Catalog.bundled)

    private fun workout(id: String, name: String, format: WorkoutFormat, slug: String = "push-up", reps: Int = 10) = Workout(
        WorkoutId(id), 1, name, format,
        listOf(
            Part(
                PartId("$id-work"), PartKind.WORK,
                listOf(Step.ExerciseStep(StepId("$id-step"), ExerciseSlug(slug), Prescription(listOf(SetPrescription(Target.Reps(reps)))))),
            ),
        ),
    )

    private val upper = workout("upper", "Upper body", WorkoutFormat.SETS)
    private val core = workout("core", "Core and mobility", WorkoutFormat.CIRCUIT, slug = "plank")
    private val legs = workout("legs", "Legs", WorkoutFormat.SETS, slug = "bodyweight-squat")
    private val all = listOf(upper, core, legs)
    private val history = mapOf(
        upper.id to WorkoutHistory(listOf(Instant.fromEpochSeconds(1_700_000_000), Instant.fromEpochSeconds(1_700_500_000)), listOf(30, 34)),
        core.id to WorkoutHistory(listOf(Instant.fromEpochSeconds(1_700_900_000)), listOf(15)),
    )

    @Test
    fun theListPutsTheRecentlyDoneFirstAndTheNeverDoneLast() {
        val page = views.list(all, history, "en")
        assertEquals(listOf("core", "upper", "legs"), page.items.map { it.id })
        assertEquals("3 workouts", page.countLabel)
        assertEquals(3, page.total)
        assertEquals(2, page.items[1].timesDone)
        assertEquals(1_700_500_000_000, page.items[1].lastDoneMillis)
        assertNull(page.items[2].lastDoneMillis)
    }

    @Test
    fun theListCanBeOrderedByName() {
        assertEquals(listOf("core", "legs", "upper"), views.list(all, history, "en", order = WorkoutOrder.NAME).items.map { it.id })
    }

    @Test
    fun theListNarrowsToAFormat() {
        val page = views.list(all, history, "en", format = WorkoutFormat.SETS)
        assertEquals(listOf("upper", "legs"), page.items.map { it.id })
        assertEquals("2 workouts", page.countLabel)
    }

    @Test
    fun theListSearchesTheNameAndTheExercisesInsideInEitherLanguage() {
        assertEquals(listOf("core"), views.list(all, history, "en", query = "mobil").items.map { it.id })
        assertEquals(listOf("legs"), views.list(all, history, "fr", query = "squat").items.map { it.id })
        val page = views.list(all, history, "en", query = "nothing like this")
        assertTrue(page.items.isEmpty())
        assertEquals("0 matches", page.countLabel)
    }

    @Test
    fun theListIsCutIntoPagesThatKeepTheTotal() {
        val second = views.list(all, history, "en", page = 2, pageSize = 2)
        assertEquals(listOf("legs"), second.items.map { it.id })
        assertEquals(3, second.total)
        assertEquals(2, second.page)
        assertTrue(views.list(all, history, "en", page = 9, pageSize = 2).items.isEmpty())
    }

    @Test
    fun aRowSaysItsFormatAndLengthInTheLanguageAsked() {
        assertEquals("Sets · 1 exercise · about 1 min", views.list(listOf(upper), emptyMap(), "en").items.single().details)
        assertEquals("Séries", views.list(listOf(upper), emptyMap(), "fr").items.single().formatLabel)
    }

    @Test
    fun aWorkoutPageHasItsPartsHowItWentBeforeAndTheMusclesItWorks() {
        val page = views.detail(upper, history.getValue(upper.id), "en")
        assertEquals("Upper body", page.name)
        assertEquals("Sets", page.formatLabel)
        assertEquals(2, page.timesDone)
        assertEquals(34, page.usualMinutes)
        assertEquals(1, page.parts.size)
        assertEquals("Work", page.parts.single().name)
        assertEquals("Push-up", page.parts.single().steps.single().row?.name)
        assertEquals("10 reps", page.parts.single().steps.single().row?.prescription)
        assertTrue(page.heat.isNotEmpty())
    }

    @Test
    fun aWorkoutNeverDoneHasNoHistoryToShow() {
        val page = views.detail(legs, WorkoutHistory(), "fr")
        assertEquals(0, page.timesDone)
        assertNull(page.lastDoneMillis)
        assertNull(page.usualMinutes)
    }
}
