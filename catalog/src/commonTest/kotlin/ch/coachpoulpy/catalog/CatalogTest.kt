package ch.coachpoulpy.catalog

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
}
