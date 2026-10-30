package ch.coachpoulpy.i18n

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CatalogTextTest {
    private val structure = """{"version": 9, "exercises": [
        {"slug": "chair-squat", "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS", "GLUTES"], "measurement": "REPS"},
        {"slug": "wall-sit", "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS", "GLUTES"], "measurement": "HOLD"}
    ], "variations": [["wall-sit", "chair-squat"]]}"""

    private val en = mapOf(
        "chair-squat.name" to "Sit to stand",
        "chair-squat.variationNote" to "Sit down on a chair and stand back up",
        "chair-squat.instructions.0" to "Sit on the front half of a sturdy chair, feet flat.",
        "chair-squat.instructions.1" to "Lean forward slightly and stand up without using your hands.",
        "chair-squat.aliases" to "chair squat",
        "wall-sit.name" to "Wall sit",
        "wall-sit.instructions.0" to "Keep your knees above your ankles and hold.",
    )
    private val fr = mapOf(
        "chair-squat.name" to "Assis-debout sur chaise",
        "chair-squat.variationNote" to "On s’assoit sur une chaise et on se relève",
        "chair-squat.instructions.0" to "Asseyez-vous sur l'avant d'une chaise stable, pieds à plat.",
        "chair-squat.instructions.1" to "Penchez-vous légèrement en avant et levez-vous sans les mains.",
        "chair-squat.aliases" to "squat sur chaise, squat assis",
        "wall-sit.name" to "Chaise contre le mur",
        "wall-sit.instructions.0" to "Gardez les genoux au-dessus des chevilles et tenez la position.",
    )

    private fun translations(en: Map<String, String> = this.en, fr: Map<String, String> = this.fr) =
        Translations("en", listOf("en", "fr"), mapOf("catalog" to mapOf("en" to en, "fr" to fr)))

    @Test
    fun theMergedCatalogIsTheDocumentPhonesAlreadyStore() {
        // Catalog version 8 stored these two entries with their text inline; the merge gives them back.
        val stored = """{"version": 9, "exercises": [
            {"slug": "chair-squat", "name": {"en": "Sit to stand", "fr": "Assis-debout sur chaise"},
             "aliases": {"en": ["chair squat"], "fr": ["squat sur chaise", "squat assis"]},
             "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS", "GLUTES"], "measurement": "REPS",
             "variationNote": {"en": "Sit down on a chair and stand back up", "fr": "On s’assoit sur une chaise et on se relève"},
             "instructions": [
                {"en": "Sit on the front half of a sturdy chair, feet flat.", "fr": "Asseyez-vous sur l'avant d'une chaise stable, pieds à plat."},
                {"en": "Lean forward slightly and stand up without using your hands.", "fr": "Penchez-vous légèrement en avant et levez-vous sans les mains."}
             ]},
            {"slug": "wall-sit", "name": {"en": "Wall sit", "fr": "Chaise contre le mur"},
             "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS", "GLUTES"], "measurement": "HOLD",
             "instructions": [{"en": "Keep your knees above your ankles and hold.", "fr": "Gardez les genoux au-dessus des chevilles et tenez la position."}]}
        ], "variations": [["wall-sit", "chair-squat"]]}"""
        assertEquals(Json.parseToJsonElement(stored), Json.parseToJsonElement(CatalogText.merge(translations(), structure)))
    }

    @Test
    fun aliasesAreOptionalPerLanguage() {
        val merged = Json.parseToJsonElement(CatalogText.merge(translations(fr = fr - "chair-squat.aliases"), structure)).jsonObject
        val aliases = merged.getValue("exercises").jsonArray[0].jsonObject.getValue("aliases").jsonObject
        assertEquals(setOf("en"), aliases.keys)
    }

    @Test
    fun aMissingNameOrInstructionFails() {
        assertFailsWith<I18nException> { CatalogText.merge(translations(fr = fr - "wall-sit.name"), structure) }
        assertFailsWith<I18nException> { CatalogText.merge(translations(en = en - "wall-sit.name", fr = fr - "wall-sit.name"), structure) }
        assertFailsWith<I18nException> { CatalogText.merge(translations(fr = fr - "chair-squat.instructions.1"), structure) }
    }

    @Test
    fun textForAnUnknownSlugFails() {
        assertFailsWith<I18nException> {
            CatalogText.merge(translations(en = en + ("wall-squat.name" to "Wall squat"), fr = fr + ("wall-squat.name" to "Squat mural")), structure)
        }
    }

    @Test
    fun textLeftInTheStructureFails() {
        val withText = structure.replace("\"slug\": \"wall-sit\",", "\"slug\": \"wall-sit\", \"name\": {\"en\": \"Wall sit\"},")
        assertFailsWith<I18nException> { CatalogText.merge(translations(), withText) }
    }

    @Test
    fun theRepositoryTranslationsGenerate() {
        // Gradle runs the tests from tools/i18n.
        val root = File("../..").canonicalFile
        val translations = Translations.read(File(root, "i18n"))
        Android.strings(translations.messages("app").getValue("fr"), translations.messages("app").getValue("en").associate { it.id to it.arguments })
        Kotlin.source(translations, "domain", "ch.coachpoulpy.shared", "DomainText")
        val merged = Json.parseToJsonElement(CatalogText.merge(translations, File(root, "catalog/data/exercises.json").readText())).jsonObject
        merged.getValue("exercises").jsonArray.forEach { exercise ->
            val name = exercise.jsonObject.getValue("name").jsonObject
            assertTrue(name.getValue("en").jsonPrimitive.content.isNotBlank())
            assertTrue(name.getValue("fr").jsonPrimitive.content.isNotBlank())
        }
    }
}
