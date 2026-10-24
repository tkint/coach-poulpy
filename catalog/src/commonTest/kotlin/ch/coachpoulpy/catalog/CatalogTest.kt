package ch.coachpoulpy.catalog

import ch.coachpoulpy.core.model.MeasurementMode
import kotlin.test.Test
import kotlin.test.assertEquals
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
    }
}
