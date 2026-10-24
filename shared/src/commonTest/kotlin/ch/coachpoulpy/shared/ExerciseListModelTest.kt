package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseListModelTest {
    @Test
    fun rowsAreLocalized() {
        val french = ExerciseListModel(Catalog.bundled, "fr").rows.single { it.slug == "push-up" }
        val english = ExerciseListModel(Catalog.bundled, "en").rows.single { it.slug == "push-up" }
        assertEquals("Pompe", french.name)
        assertEquals("Push-up", english.name)
    }
}
