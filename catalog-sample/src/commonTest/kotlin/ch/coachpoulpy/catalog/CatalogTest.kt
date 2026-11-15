package ch.coachpoulpy.catalog

import ch.coachpoulpy.catalog.sample.SampleCatalog
import ch.coachpoulpy.core.model.Aliases
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MeasurementMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CatalogTest {
    @Test
    fun sampleCatalogParsesWithUniqueSlugs() {
        val exercises = SampleCatalog.catalog.exercises
        assertTrue(exercises.isNotEmpty())
        assertEquals(exercises.size, exercises.map { it.slug }.toSet().size)
    }

    @Test
    fun everySampleExerciseHasInstructionsInBothLanguages() {
        SampleCatalog.catalog.exercises.forEach { exercise ->
            assertTrue(exercise.instructions.isNotEmpty(), "${exercise.slug.value} has no instructions")
            exercise.instructions.forEach { text -> LANGUAGES.forEach { assertTrue(text.byLanguage[it].orEmpty().isNotBlank()) } }
        }
    }

    @Test
    fun retiredEntriesAreHiddenButStillFound() {
        val push = SampleCatalog.catalog.exercises.first()
        val catalog = Catalog(listOf(push.copy(retired = true)))
        assertTrue(catalog.offered.isEmpty())
        assertEquals(MeasurementMode.REPS, catalog[push.slug]?.measurement)
    }

    @Test
    fun jsonRoundTrips() {
        val again = Catalog.fromJson(SampleCatalog.catalog.toJson())
        assertEquals(SampleCatalog.catalog.exercises, again.exercises)
        assertEquals(SampleCatalog.catalog.variationGroups, again.variationGroups)
    }

    private fun slugs(vararg values: String) = values.map { ExerciseSlug(it) }
    private fun names(exercises: List<Exercise>) = exercises.map { it.slug.value }

    @Test
    fun sampleVariationsAreOrderedEasiestToHardest() {
        val pushUps = listOf("knee-push-up", "incline-push-up", "push-up", "diamond-push-up", "one-arm-push-up")
        assertEquals(pushUps, names(SampleCatalog.catalog.variationsOf(ExerciseSlug("push-up"))))
        assertEquals(pushUps, names(SampleCatalog.catalog.variationsOf(ExerciseSlug("one-arm-push-up"))))
        assertEquals(
            listOf("chair-squat", "bodyweight-squat", "goblet-squat"),
            names(SampleCatalog.catalog.variationsOf(ExerciseSlug("goblet-squat"))),
        )
        assertTrue(SampleCatalog.catalog.variationsOf(ExerciseSlug("plank")).isEmpty())
    }

    @Test
    fun walksAndRunsAreOrderedEasiestToHardest() {
        assertEquals(
            listOf("walk-with-support", "gentle-walk", "walk", "brisk-walk"),
            names(SampleCatalog.catalog.variationsOf(ExerciseSlug("gentle-walk"))),
        )
        assertEquals(listOf("easy-run", "run"), names(SampleCatalog.catalog.variationsOf(ExerciseSlug("run"))))
        listOf("walk-with-support", "gentle-walk").forEach { slug ->
            val walk = SampleCatalog.catalog[ExerciseSlug(slug)]!!
            assertEquals(MeasurementMode.TIME, walk.measurement)
            assertTrue(walk.equipment.isEmpty(), "$slug needs nothing; a treadmill is only optional")
        }
    }

    @Test
    fun everyWalkAndRunCanBeDoneOnATreadmillButNotTheJumps() {
        val treadmill = SampleCatalog.catalog.offered.filter { Equipment.TREADMILL in it.optionalEquipment }.map { it.slug.value }.toSet()
        assertEquals(setOf("walk-with-support", "gentle-walk", "walk", "brisk-walk", "easy-run", "run"), treadmill)
    }

    @Test
    fun aVariationGroupNamingAnUnknownSlugIsRejected() {
        val exercises = SampleCatalog.catalog.exercises
        assertFailsWith<IllegalArgumentException> {
            Catalog(exercises, listOf(slugs("push-up", "pushup-typo")))
        }
    }

    @Test
    fun anExerciseInTwoVariationGroupsIsRejected() {
        val exercises = SampleCatalog.catalog.exercises
        assertFailsWith<IllegalArgumentException> {
            Catalog(exercises, listOf(slugs("knee-push-up", "push-up"), slugs("push-up", "diamond-push-up")))
        }
    }

    @Test
    fun retiredVariationsStayInTheirGroupButAreNotListed() {
        val exercises = SampleCatalog.catalog.exercises.map { if (it.slug.value == "incline-push-up") it.copy(retired = true) else it }
        val catalog = Catalog(exercises, SampleCatalog.catalog.variationGroups)
        assertEquals(
            listOf("knee-push-up", "push-up", "diamond-push-up", "one-arm-push-up"),
            names(catalog.variationsOf(ExerciseSlug("push-up"))),
        )
    }

    @Test
    fun aGroupWithOnlyOneOfferedExerciseHasNoVariations() {
        val exercises = SampleCatalog.catalog.exercises.map { if (it.slug.value == "knee-push-up") it.copy(retired = true) else it }
        val catalog = Catalog(exercises, listOf(slugs("knee-push-up", "push-up")))
        assertTrue(catalog.variationsOf(ExerciseSlug("push-up")).isEmpty())
    }

    @Test
    fun sampleAliasesLoadPerLanguage() {
        val rdl = SampleCatalog.catalog[ExerciseSlug("romanian-deadlift")]!!
        assertEquals(listOf("RDL"), rdl.aliases["en"])
        assertEquals(listOf("abdos"), SampleCatalog.catalog[ExerciseSlug("plank")]!!.aliases["fr"])
        assertTrue(SampleCatalog.catalog[ExerciseSlug("wall-sit")]!!.aliases.all.isEmpty())
    }

    @Test
    fun anEmptyAliasIsRejected() {
        val exercises = SampleCatalog.catalog.exercises.map {
            if (it.slug.value == "plank") it.copy(aliases = Aliases(mapOf("fr" to listOf("abdos", " ")))) else it
        }
        assertFailsWith<IllegalArgumentException> { Catalog(exercises) }
    }

    @Test
    fun equipmentLoadsAsNeedsOfAlternatives() {
        assertEquals(listOf(listOf(Equipment.DUMBBELL, Equipment.KETTLEBELL)), SampleCatalog.catalog[ExerciseSlug("goblet-squat")]!!.equipment)
        assertEquals(
            listOf(listOf(Equipment.DUMBBELL), listOf(Equipment.BENCH)),
            SampleCatalog.catalog[ExerciseSlug("dumbbell-bench-press")]!!.equipment,
        )
        val plank = SampleCatalog.catalog[ExerciseSlug("plank")]!!
        assertTrue(plank.equipment.isEmpty())
        assertEquals(listOf(Equipment.MAT), plank.optionalEquipment)
    }

    @Test
    fun anEmptyNeedIsRejected() {
        val exercises = SampleCatalog.catalog.exercises.map { if (it.slug.value == "plank") it.copy(equipment = listOf(emptyList())) else it }
        assertFailsWith<IllegalArgumentException> { Catalog(exercises) }
    }

    @Test
    fun aNeedHoldingBodyweightOrAMatIsRejected() {
        listOf(Equipment.BODYWEIGHT, Equipment.MAT).forEach { never ->
            val exercises = SampleCatalog.catalog.exercises.map {
                if (it.slug.value == "plank") it.copy(equipment = listOf(listOf(Equipment.DUMBBELL, never))) else it
            }
            assertFailsWith<IllegalArgumentException> { Catalog(exercises) }
        }
    }

    @Test
    fun aFlatEquipmentListFromAnOlderCatalogReadsAsOneNeedPerItem() {
        val old = """{"version": 4, "exercises": [{"slug": "dumbbell-bench-press", "name": {"en": "Dumbbell bench press", "fr": "Développé couché haltères"},
            "movementPattern": "PUSH", "muscleGroups": ["CHEST"], "equipment": ["DUMBBELL", "BENCH", "BODYWEIGHT", "MAT"], "measurement": "REPS"}]}"""
        val exercise = Catalog.fromJson(old).exercises.single()
        assertEquals(listOf(listOf(Equipment.DUMBBELL), listOf(Equipment.BENCH)), exercise.equipment)
    }

    @Test
    fun anEntryStoredBeforeTheTextMovedToTranslationFilesDecodesUnchanged() {
        // Catalog version 8, as phones stored it before the text moved to i18n/messages (ADR 0015).
        val stored = """{"version": 8, "exercises": [{"slug": "chair-squat", "name": {"en": "Sit to stand", "fr": "Assis-debout sur chaise"},
            "aliases": {"en": ["chair squat"], "fr": ["squat sur chaise"]}, "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS", "GLUTES"],
            "measurement": "REPS", "variationNote": {"en": "Sit down on a chair and stand back up", "fr": "On s’assoit sur une chaise et on se relève"},
            "instructions": [{"en": "Sit on the front half of a sturdy chair, feet flat.", "fr": "Asseyez-vous sur l'avant d'une chaise stable, pieds à plat."}]},
            {"slug": "wall-sit", "name": {"en": "Wall sit", "fr": "Chaise contre le mur"}, "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS"],
            "measurement": "HOLD"}]}"""
        val (chair, wall) = Catalog.fromJson(stored).exercises
        assertEquals("Assis-debout sur chaise", chair.name.forLanguage("fr-CH"))
        assertEquals("Sit to stand", chair.name.forLanguage("de"))
        assertEquals(listOf("chair squat"), chair.aliases["en"])
        assertEquals(listOf("squat sur chaise"), chair.aliases["fr"])
        assertEquals("On s’assoit sur une chaise et on se relève", chair.variationNote?.forLanguage("fr"))
        assertEquals("Sit on the front half of a sturdy chair, feet flat.", chair.instructions.single().forLanguage("en"))
        assertTrue(wall.aliases.all.isEmpty())
        assertEquals(null, wall.variationNote)
        val again = Catalog.fromJson(Catalog.fromJson(stored).toJson()).exercises
        assertEquals(listOf(chair, wall), again)
    }

    @Test
    fun everyExerciseInAVariationGroupHasANoteInBothLanguages() {
        SampleCatalog.catalog.variationGroups.flatten().forEach { slug ->
            val note = SampleCatalog.catalog[slug]?.variationNote
            assertTrue(note != null && LANGUAGES.all { note.byLanguage[it].orEmpty().isNotBlank() }, "${slug.value} has no variation note")
        }
    }

    private companion object {
        val LANGUAGES = listOf("en", "fr")
    }
}
