package ch.coachpoulpy.i18n

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Puts the exercise text of the `catalog` namespace back into the catalog's structure (ADR 0015), so each
 * catalog version stays one document with every text as a map from language to string, the JSON phones
 * already store: `goblet-squat.name` becomes the exercise's `name`, `goblet-squat.instructions.0` its first
 * instruction, `goblet-squat.aliases` (comma-separated, optional per language) its `aliases`.
 */
object CatalogText {
    private const val NAMESPACE = "catalog"
    private val FIELDS = setOf("name", "variationNote", "instructions", "aliases")
    private val INSTRUCTION = Regex("instructions\\.(\\d+)")

    fun write(translations: Translations, structure: File, output: File) {
        output.parentFile.mkdirs()
        output.writeText(merge(translations, structure.readText()))
    }

    fun merge(translations: Translations, structure: String): String {
        translations.checkComplete(NAMESPACE, optional = { it.endsWith(".aliases") })
        val catalog = Json.parseToJsonElement(structure).jsonObject
        val exercises = catalog.getValue("exercises").jsonArray.map { it.jsonObject }
        val slugs = exercises.map { it.getValue("slug").jsonPrimitive.content }.toSet()
        exercises.forEach { exercise ->
            val text = FIELDS.filter { it in exercise }
            if (text.isNotEmpty()) throw I18nException("${exercise["slug"]} has ${text.joinToString()} in the catalog structure; text lives in i18n/messages/<locale>/catalog.json")
        }

        // locale → slug → field → text
        val bySlug = translations.locales.associateWith { locale ->
            translations.texts(NAMESPACE, locale).entries.groupBy({ it.key.substringBefore('.') }, { it.key.substringAfter('.') to it.value })
                .mapValues { (_, fields) -> fields.toMap() }
        }
        bySlug.forEach { (locale, entries) ->
            entries.forEach { (slug, fields) ->
                if (slug !in slugs) throw I18nException("$locale/catalog.json has text for $slug, which the catalog doesn't have")
                fields.keys.forEach { field ->
                    if (field !in setOf("name", "variationNote", "aliases") && !INSTRUCTION.matches(field)) {
                        throw I18nException("$locale/catalog.json: $slug.$field is not an exercise text")
                    }
                }
            }
        }

        val merged = exercises.map { exercise ->
            val slug = exercise.getValue("slug").jsonPrimitive.content
            val source = bySlug.getValue(translations.baseLocale)[slug] ?: throw I18nException("$slug has no name in ${translations.baseLocale}/catalog.json")
            fun text(field: String): JsonObject = JsonObject(translations.locales.associateWith { locale ->
                JsonPrimitive(bySlug.getValue(locale)[slug]?.get(field) ?: throw I18nException("$slug.$field is missing in $locale/catalog.json"))
            })
            val instructions = source.keys.mapNotNull { INSTRUCTION.matchEntire(it)?.groupValues?.get(1)?.toInt() }.sorted()
            if (instructions != instructions.indices.toList()) throw I18nException("$slug's instructions are not numbered 0 to ${instructions.size - 1}")
            val aliases = translations.locales.mapNotNull { locale ->
                val list = bySlug.getValue(locale)[slug]?.get("aliases") ?: return@mapNotNull null
                val names = list.split(',').map(String::trim)
                if (names.any(String::isEmpty)) throw I18nException("$slug.aliases in $locale/catalog.json has an empty alias")
                locale to JsonArray(names.map(::JsonPrimitive))
            }.toMap()

            buildMap<String, JsonElement> {
                put("slug", exercise.getValue("slug"))
                if ("name" !in source) throw I18nException("$slug has no name in ${translations.baseLocale}/catalog.json")
                put("name", text("name"))
                if (aliases.isNotEmpty()) put("aliases", JsonObject(aliases))
                exercise.forEach { (key, value) -> if (key != "slug") put(key, value) }
                if (instructions.isNotEmpty()) put("instructions", JsonArray(instructions.map { text("instructions.$it") }))
                if ("variationNote" in source) put("variationNote", text("variationNote"))
            }.let(::JsonObject)
        }
        return JsonObject(catalog + ("exercises" to JsonArray(merged))).toString()
    }
}
