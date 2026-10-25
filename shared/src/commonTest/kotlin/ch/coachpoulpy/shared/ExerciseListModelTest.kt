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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseListModelTest {
    private val registry = WorkoutTypeRegistry(listOf(MobilityModule, RehabModule))
    private fun model(language: String) = ExerciseListModel(Catalog.bundled, registry, language)

    @Test
    fun rowsAreLocalized() {
        assertEquals("Pompe", model("fr").filter().rows.single { it.slug == "push-up" }.name)
        assertEquals("Push-up", model("en").filter().rows.single { it.slug == "push-up" }.name)
    }

    @Test
    fun searchMatchesEitherLanguageAndFiltersByMuscle() {
        val list = model("en")
        assertEquals(listOf("push-up"), list.filter("pompe").rows.map { it.slug }.filter { it == "push-up" })
        assertTrue(list.filter(area = MuscleArea.SHOULDERS).rows.any { it.slug == "neck-stretch" })
    }

    @Test
    fun pickerOnlyOffersExercisesTheWorkoutTypeCanRun() {
        val rehab = model("en").filter(type = WorkoutType.REHAB).rows.map { Catalog.bundled[ch.coachpoulpy.core.model.ExerciseSlug(it.slug)]!!.measurement }
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
        val plank = model("en").detail("plank")!!
        assertTrue(plank.variations.isEmpty())
        assertEquals(0, plank.variationIndex)
        assertEquals("", plank.variationPosition)
        assertEquals("Core · Hold", plank.eyebrow)
    }

    @Test
    fun detailGivesTheEyebrowThePositionAndEachVariationsNote() {
        val detail = model("en").detail("diamond-push-up")!!
        assertEquals("Push · Reps", detail.eyebrow)
        assertEquals(4, detail.variationIndex)
        assertEquals("4 of 5", detail.variationPosition)
        assertEquals("Hands together, more triceps", detail.variations.single { it.current }.note)
        assertEquals("Knees down, a shorter lever", detail.variations.first().note)
        assertEquals("Triceps, chest, shoulders", detail.muscles)
        assertEquals("1 sur 3", model("fr").detail("chair-squat")!!.variationPosition)
    }

    @Test
    fun eachSideMovesIntoTheEyebrow() {
        assertEquals("Lunge · Reps · Each side", model("en").detail("reverse-lunge")!!.eyebrow)
        assertEquals("Fente · Répétitions · Par côté", model("fr").detail("reverse-lunge")!!.eyebrow)
    }

    private fun search(query: String, language: String = "en") = model(language).filter(query).rows.map { it.slug }

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
        val dumbbell = Catalog.bundled.offered.filter { Equipment.DUMBBELL in it.allEquipment }.map { it.slug.value }
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
        assertEquals("Romanian deadlift", model("en").filter("rdl").rows.single().name)
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
        val all = model("en").filter().rows.map { it.slug }
        val start = all.indexOf("knee-push-up")
        assertEquals(pushUps, all.subList(start, start + pushUps.size))
    }

    @Test
    fun filtersStillApplyOnTopOfTheQuery() {
        assertEquals(listOf("dumbbell-row", "romanian-deadlift"), model("en").filter("dumbbell", MuscleArea.BACK).rows.map { it.slug }.sorted())
    }

    @Test
    fun anEmptySearchIsGroupedByMovementPatternInOrder() {
        val listing = model("en").filter()
        assertEquals(listOf("Squat", "Hinge", "Lunge", "Push", "Pull", "Carry", "Core", "Rotation", "Cardio", "Mobility", "Stretch"), listing.sections.map { it.title })
        assertEquals(Catalog.bundled.offered.size, listing.count)
        assertEquals("${listing.count} exercises", listing.countLabel)
        assertFalse(listing.searching)
        val squat = listing.sections.first().rows.map { it.slug }
        assertEquals(listOf("chair-squat", "bodyweight-squat", "goblet-squat"), squat.filter { it in listOf("chair-squat", "bodyweight-squat", "goblet-squat") })
    }

    @Test
    fun aVariationGroupIsPlacedByItsEasiestMembersName() {
        // Sit to stand (S) opens the squat group, so Bodyweight squat and Goblet squat follow it, after Calf raise and before Wall sit.
        assertEquals(
            listOf("calf-raise", "chair-squat", "bodyweight-squat", "goblet-squat", "wall-sit"),
            model("en").filter().sections.first().rows.map { it.slug },
        )
        val push = model("en").filter().sections.single { it.title == "Push" }.rows.map { it.slug }
        assertEquals(listOf("bench-dip", "dumbbell-bench-press", "overhead-press", "knee-push-up", "incline-push-up", "push-up", "diamond-push-up", "one-arm-push-up"), push)
    }

    @Test
    fun searchingGivesOneUntitledSectionCountedAsMatches() {
        val listing = model("en").filter("rdl")
        assertEquals(listOf<String?>(null), listing.sections.map { it.title })
        assertEquals("1 match", listing.countLabel)
        assertTrue(listing.searching)
        assertEquals("5 résultats", model("fr").filter("pompes").countLabel)
        assertTrue(model("en").filter("zumba").sections.isEmpty())
        assertEquals("0 matches", model("en").filter("zumba").countLabel)
    }

    @Test
    fun everyMuscleGroupMapsToExactlyOneChip() {
        MuscleGroup.entries.forEach { group ->
            assertEquals(1, MuscleArea.entries.count { group in it.groups }, "${group.name} is not on exactly one chip")
        }
        assertEquals(listOf("Legs", "Glutes", "Core", "Back", "Chest", "Shoulders", "Arms", "Full body"), model("en").muscleFilters.map { it.label })
    }

    @Test
    fun aChipListsEveryExerciseWithOneOfItsMuscles() {
        val legs = model("en").filter(area = MuscleArea.LEGS).rows.map { it.slug }
        assertTrue("calf-raise" in legs && "hip-circles" in legs && "goblet-squat" in legs)
        assertTrue("push-up" !in legs)
    }

    @Test
    fun theDetailsLineGivesTwoMusclesThenOneFact() {
        val rows = model("en").filter().rows.associate { it.slug to it.details }
        assertEquals("Chest, triceps · No equipment", rows["push-up"])
        assertEquals("Quadriceps, glutes · Dumbbells or kettlebell", rows["goblet-squat"])
        assertEquals("Chest, triceps · Dumbbells and bench or step", rows["dumbbell-bench-press"])
        assertEquals("Quadriceps, glutes · Each side", rows["reverse-lunge"])
        assertEquals("Core · Hold", rows["plank"])
        assertEquals("Core, hips · Hold · each side", rows["side-plank"])
        assertEquals("Full body · Timed", rows["jumping-jack"])
        assertEquals("Glutes, hamstrings · Mat", rows["glute-bridge"])
        assertEquals("Back, biceps · Dumbbells · each side", rows["dumbbell-row"])
        assertEquals("Pectoraux, triceps · Sans matériel", model("fr").filter().rows.single { it.slug == "push-up" }.details)
    }

    private fun fitting(vararg ticked: Equipment) = model("en").filter(ticked = ticked.toSet()).rows.map { it.slug }

    @Test
    fun withDumbbellsTickedAGobletSquatShowsButNotAKettlebellSwing() {
        val shown = fitting(Equipment.DUMBBELL)
        assertTrue("goblet-squat" in shown)
        assertTrue("kettlebell-swing" !in shown)
    }

    @Test
    fun everyNeedMustHaveOneAlternativeTicked() {
        assertTrue("dumbbell-bench-press" !in fitting(Equipment.DUMBBELL))
        assertTrue("dumbbell-bench-press" in fitting(Equipment.DUMBBELL, Equipment.BENCH))
    }

    @Test
    fun aTreadmillListsOnlyTheExercisesThatUseIt() {
        assertEquals(setOf("walk-with-support", "gentle-walk", "walk", "brisk-walk", "easy-run", "run"), fitting(Equipment.TREADMILL).toSet())
    }

    @Test
    fun optionalEquipmentCountsAndBodyweightOnlyHides() {
        val shown = fitting(Equipment.DUMBBELL)
        assertTrue("reverse-lunge" in shown && "calf-raise" in shown)
        assertTrue("push-up" !in shown && "plank" !in shown)
    }

    @Test
    fun noEquipmentListsTheExercisesNeedingNothing() {
        val free = Catalog.bundled.offered.filter { it.equipment.isEmpty() }.map { it.slug.value }
        assertEquals(free.toSet(), fitting(Equipment.BODYWEIGHT).toSet())
        assertTrue("push-up" in free && "plank" in free && "reverse-lunge" in free)
        val withKettlebell = fitting(Equipment.BODYWEIGHT, Equipment.KETTLEBELL)
        assertTrue("push-up" in withKettlebell && "kettlebell-swing" in withKettlebell)
        assertTrue("dumbbell-row" !in withKettlebell)
    }

    @Test
    fun tickingNothingIsNoFilter() {
        val all = model("en").filter(ticked = emptySet())
        assertEquals(Catalog.bundled.offered.size, all.count)
        assertFalse(all.restricted)
        assertEquals(Catalog.bundled.offered.size, model("en").filter(ticked = null).count)
    }

    @Test
    fun theCountSaysWhatYouCanDoWhileEquipmentIsOn() {
        val listing = model("en").filter(ticked = setOf(Equipment.DUMBBELL))
        assertTrue(listing.restricted)
        assertEquals("${listing.count} exercises you can do", listing.countLabel)
        assertEquals("1 match you can do", model("en").filter("rdl", ticked = setOf(Equipment.DUMBBELL)).countLabel)
        assertFalse(model("en").filter().restricted)
    }

    @Test
    fun theSheetListsNoEquipmentThenEquipmentButNotTheMat() {
        val options = model("en").equipmentOptions
        assertEquals(
            listOf(Equipment.BODYWEIGHT, Equipment.DUMBBELL, Equipment.KETTLEBELL, Equipment.BARBELL, Equipment.BAND, Equipment.BENCH, Equipment.PULL_UP_BAR, Equipment.TREADMILL),
            options.map { it.equipment },
        )
        val kettlebell = options.single { it.equipment == Equipment.KETTLEBELL }
        assertEquals(Catalog.bundled.offered.count { Equipment.KETTLEBELL in it.allEquipment }, kettlebell.count)
        assertEquals("${kettlebell.count} exercises", kettlebell.countLabel)
        val none = options.first()
        assertEquals("No equipment", none.label)
        assertEquals(Catalog.bundled.offered.count { it.equipment.isEmpty() }, none.count)
    }
}
