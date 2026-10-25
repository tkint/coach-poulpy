package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.Equipment
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

    private fun search(query: String, language: String = "en") = model(language).filter(query).map { it.slug }

    private val pushUps = listOf("knee-push-up", "incline-push-up", "push-up", "diamond-push-up", "one-arm-push-up")

    @Test
    fun searchIgnoresAccentsAndCase() {
        assertEquals(setOf("dumbbell-bench-press", "overhead-press"), search("developpe").toSet())
        assertEquals(search("Développé"), search("developpe"))
    }

    @Test
    fun searchIgnoresPluralsHyphensAndSpaces() {
        assertEquals(pushUps.toSet(), search("pompes").toSet())
        assertEquals(pushUps.toSet(), search("pushup").toSet())
        assertEquals(pushUps.toSet(), search("push-ups").toSet())
    }

    @Test
    fun everyWordMustMatchInAnyOrder() {
        assertEquals("goblet-squat", search("squat dumbbell").first())
        assertEquals(search("squat dumbbell"), search("dumbbell squat"))
        assertTrue(search("squat dumbbell").none { it == "bodyweight-squat" })
    }

    @Test
    fun equipmentFindsEveryExerciseUsingItWithNamesFirst() {
        val dumbbell = Catalog.bundled.offered.filter { Equipment.DUMBBELL in it.equipment }.map { it.slug.value }
        assertEquals(11, dumbbell.size)
        val found = search("dumbbell")
        assertEquals(dumbbell.toSet(), found.toSet())
        val named = listOf("dumbbell-bench-press", "overhead-press", "dumbbell-row", "biceps-curl")
        assertEquals(named.toSet(), found.take(named.size).toSet())
        assertEquals(found.toSet(), search("haltères", "fr").toSet())
    }

    @Test
    fun aliasesAreSearchedButNeverShown() {
        assertEquals(listOf("romanian-deadlift"), search("rdl"))
        assertEquals("Romanian deadlift", model("en").filter("rdl").single().name)
        val core = listOf("plank", "side-plank", "dead-bug", "bird-dog", "crunch", "hollow-hold", "russian-twist")
        assertEquals(core.toSet(), search("abdos").take(core.size).toSet())
    }

    @Test
    fun aNameThatStartsWithTheWordComesFirst() {
        val found = search("push")
        assertEquals("push-up", found.first())
        assertTrue(found.indexOf("push-up") < found.indexOf("bench-dip"))
    }

    @Test
    fun aVariationGroupStaysTogetherEasiestToHardest() {
        assertEquals(pushUps, search("pompes"))
        val all = model("en").rows.map { it.slug }
        val start = all.indexOf("knee-push-up")
        assertEquals(pushUps, all.subList(start, start + pushUps.size))
    }

    @Test
    fun filtersStillApplyOnTopOfTheQuery() {
        assertEquals(listOf("biceps-curl", "dumbbell-row"), model("en").filter("dumbbell", MuscleGroup.BICEPS).map { it.slug }.sorted())
    }
}
