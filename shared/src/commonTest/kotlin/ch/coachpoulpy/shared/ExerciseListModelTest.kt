package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.types.mobility.MobilityModule
import ch.coachpoulpy.types.rehab.RehabModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExerciseListModelTest {
    private val registry = WorkoutTypeRegistry(listOf(MobilityModule, RehabModule))
    private fun model(language: String) = ExerciseListModel(Catalog.bundled, registry, language)

    @Test
    fun rowsAreLocalized() {
        assertEquals("Pompe", model("fr").rows.single { it.slug == "push-up" }.name)
        assertEquals("Push-up", model("en").rows.single { it.slug == "push-up" }.name)
    }

    @Test
    fun searchMatchesEitherLanguageAndFiltersByMuscle() {
        val list = model("en")
        assertEquals(listOf("push-up"), list.filter("pompe").map { it.slug }.filter { it == "push-up" })
        assertTrue(list.filter(muscle = MuscleGroup.NECK).all { it.slug == "neck-stretch" })
    }

    @Test
    fun pickerOnlyOffersExercisesTheWorkoutTypeCanRun() {
        val rehab = model("en").filter(type = WorkoutType.REHAB).map { Catalog.bundled[ch.coachpoulpy.core.model.ExerciseSlug(it.slug)]!!.measurement }
        assertTrue(rehab.isNotEmpty())
        assertTrue(MeasurementMode.TIME !in rehab)
    }

    @Test
    fun detailHasLocalizedInstructions() {
        val detail = model("fr").detail("plank")!!
        assertEquals("Gainage", detail.name)
        assertTrue(detail.instructions.first().startsWith("En appui"))
    }

    @Test
    fun detailListsVariationsEasiestToHardestWithTheCurrentOneMarked() {
        val variations = model("fr").detail("diamond-push-up")!!.variations
        assertEquals(
            listOf("knee-push-up", "incline-push-up", "push-up", "diamond-push-up", "one-arm-push-up"),
            variations.map { it.slug },
        )
        assertEquals(listOf("diamond-push-up"), variations.filter { it.current }.map { it.slug })
        assertEquals("Pompe diamant", variations.single { it.current }.name)
    }

    @Test
    fun detailHasNoVariationsOutsideAGroup() {
        assertTrue(model("en").detail("plank")!!.variations.isEmpty())
    }
}
