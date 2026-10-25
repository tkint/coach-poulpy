package ch.coachpoulpy.catalog

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MeasurementMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CatalogTest {
    @Test
    fun bundledCatalogParsesWithUniqueSlugs() {
        val exercises = Catalog.bundled.exercises
        assertTrue(exercises.isNotEmpty())
        assertEquals(exercises.size, exercises.map { it.slug }.toSet().size)
    }

    @Test
    fun everyBundledExerciseHasInstructionsInBothLanguages() {
        Catalog.bundled.exercises.forEach { exercise ->
            assertTrue(exercise.instructions.isNotEmpty(), "${exercise.slug.value} has no instructions")
            exercise.instructions.forEach { assertTrue(it.en.isNotBlank() && it.fr.isNotBlank()) }
        }
    }

    @Test
    fun retiredEntriesAreHiddenButStillFound() {
        val push = Catalog.bundled.exercises.first()
        val catalog = Catalog(3, listOf(push.copy(retired = true)))
        assertTrue(catalog.offered.isEmpty())
        assertEquals(MeasurementMode.REPS, catalog[push.slug]?.measurement)
    }

    @Test
    fun jsonRoundTrips() {
        val again = Catalog.fromJson(Catalog.bundled.toJson())
        assertEquals(Catalog.bundled.version, again.version)
        assertEquals(Catalog.bundled.exercises, again.exercises)
        assertEquals(Catalog.bundled.variationGroups, again.variationGroups)
    }

    private fun slugs(vararg values: String) = values.map { ExerciseSlug(it) }
    private fun names(exercises: List<Exercise>) = exercises.map { it.slug.value }

    @Test
    fun bundledVariationsAreOrderedEasiestToHardest() {
        val pushUps = listOf("knee-push-up", "incline-push-up", "push-up", "diamond-push-up", "one-arm-push-up")
        assertEquals(pushUps, names(Catalog.bundled.variationsOf(ExerciseSlug("push-up"))))
        assertEquals(pushUps, names(Catalog.bundled.variationsOf(ExerciseSlug("one-arm-push-up"))))
        assertEquals(
            listOf("chair-squat", "bodyweight-squat", "goblet-squat"),
            names(Catalog.bundled.variationsOf(ExerciseSlug("goblet-squat"))),
        )
        assertTrue(Catalog.bundled.variationsOf(ExerciseSlug("plank")).isEmpty())
    }

    @Test
    fun aVariationGroupNamingAnUnknownSlugIsRejected() {
        val exercises = Catalog.bundled.exercises
        assertFailsWith<IllegalArgumentException> {
            Catalog(3, exercises, listOf(slugs("push-up", "pushup-typo")))
        }
    }

    @Test
    fun anExerciseInTwoVariationGroupsIsRejected() {
        val exercises = Catalog.bundled.exercises
        assertFailsWith<IllegalArgumentException> {
            Catalog(3, exercises, listOf(slugs("knee-push-up", "push-up"), slugs("push-up", "diamond-push-up")))
        }
    }

    @Test
    fun retiredVariationsStayInTheirGroupButAreNotListed() {
        val exercises = Catalog.bundled.exercises.map { if (it.slug.value == "incline-push-up") it.copy(retired = true) else it }
        val catalog = Catalog(3, exercises, Catalog.bundled.variationGroups)
        assertEquals(
            listOf("knee-push-up", "push-up", "diamond-push-up", "one-arm-push-up"),
            names(catalog.variationsOf(ExerciseSlug("push-up"))),
        )
    }

    @Test
    fun aGroupWithOnlyOneOfferedExerciseHasNoVariations() {
        val exercises = Catalog.bundled.exercises.map { if (it.slug.value == "knee-push-up") it.copy(retired = true) else it }
        val catalog = Catalog(3, exercises, listOf(slugs("knee-push-up", "push-up")))
        assertTrue(catalog.variationsOf(ExerciseSlug("push-up")).isEmpty())
    }
}
