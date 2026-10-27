package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RecentlyUsedTest {
    private var now = Instant.fromEpochSeconds(1_700_000_000)
    private val store = LocalStore(DriverFactory(), clock = object : Clock {
        override fun now() = this@RecentlyUsedTest.now
    })
    private fun model(catalog: Catalog = Catalog.bundled) = ExerciseListModel(catalog, "en") { recentlyUsedExercises(store) }

    private fun picker(
        catalog: Catalog = Catalog.bundled,
        query: String = "",
        area: MuscleArea? = null,
        ticked: Set<Equipment>? = null,
    ) = model(catalog).filter(query, area, ticked, withRecent = true)

    private fun recent(listing: ExerciseListing) =
        listing.sections.firstOrNull { it.title == "Recently used" }?.rows?.map { it.slug }

    private fun workout(name: String, vararg slugs: String) = workoutOf(
        id = name,
        name = name,
        steps = slugs.mapIndexed { index, slug ->
            Step.ExerciseStep(StepId("$name-$index"), ExerciseSlug(slug), Prescription(listOf(SetPrescription(Target.Reps(10)))))
        },
    )

    private fun save(workout: Workout) {
        now += 1.minutes
        store.saveWorkout(workout)
    }

    private fun session(workout: Workout) {
        now += 1.minutes
        val id = "session-${now.epochSeconds}"
        store.startExecution(Execution(ExecutionId(id), ExecutionSource.GUIDED, startedAt = now, workout = WorkoutRef(workout.id, workout.version)))
        store.finishExecution(id, now + 20.minutes)
    }

    @Test
    fun sessionsAndSavedWorkoutsCountMostRecentFirst() {
        val legs = workout("Legs", "goblet-squat")
        save(legs)
        save(workout("Push", "push-up"))
        assertEquals(listOf("push-up", "goblet-squat"), recent(picker()))
        session(legs)
        assertEquals(listOf("goblet-squat", "push-up"), recent(picker()))
    }

    @Test
    fun theSectionIsCappedAtFiveAndItsExercisesStayInTheirGroup() {
        val slugs = listOf("goblet-squat", "push-up", "plank", "pull-up", "burpee", "crunch", "bench-dip")
        slugs.forEach { save(workout(it, it)) }
        val listing = picker()
        assertEquals(slugs.reversed().take(5), recent(listing))
        assertEquals("Recently used", listing.sections.first().title)
        assertTrue("bench-dip" in listing.sections.single { it.title == "Push" }.rows.map { it.slug })
        assertEquals(Catalog.bundled.offered.size, listing.count)
    }

    @Test
    fun retiredExercisesAreLeftOut() {
        save(workout("Old", "push-up", "plank"))
        val catalog = Catalog(5, Catalog.bundled.exercises.map { if (it.slug.value == "plank") it.copy(retired = true) else it }, Catalog.bundled.variationGroups)
        assertEquals(listOf("push-up"), recent(picker(catalog)))
    }

    @Test
    fun theSectionIsAbsentWithNoHistoryWhileSearchingOrFilteringAndInTheLibrary() {
        assertEquals(null, recent(picker()))
        save(workout("Legs", "goblet-squat"))
        assertEquals(null, recent(picker(query = "squat")))
        assertEquals(null, recent(picker(area = MuscleArea.LEGS)))
        assertEquals(null, recent(picker(ticked = setOf(Equipment.DUMBBELL))))
        assertEquals(null, recent(model().filter()))
    }
}
