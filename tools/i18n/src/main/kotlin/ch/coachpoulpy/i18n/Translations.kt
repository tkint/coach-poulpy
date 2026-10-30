package ch.coachpoulpy.i18n

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Every message of every shipped locale, read from `i18n/` (ADR 0014): `messages/<locale>/<namespace>.json`,
 * nested JSON whose path is the message id. The shipped locales and the source one come from the inlang
 * settings Fink reads.
 */
class Translations(
    val baseLocale: String,
    val locales: List<String>,
    /** namespace → locale → id → text, each locale's ids in file order. */
    private val texts: Map<String, Map<String, Map<String, String>>>,
) {
    fun texts(namespace: String, locale: String): Map<String, String> =
        texts[namespace]?.get(locale) ?: throw I18nException("No $namespace messages for $locale")

    /**
     * The ICU messages of [namespace], per locale, after checking that every locale has exactly the
     * source's messages, with the same arguments.
     */
    fun messages(namespace: String): Map<String, List<Message>> {
        checkComplete(namespace)
        val parsed = locales.associateWith { locale -> texts(namespace, locale).map { (id, text) -> Message.parse("$locale/$namespace $id", text) } }
        val source = parsed.getValue(baseLocale).associateBy { it.id.substringAfter(' ') }
        parsed.forEach { (locale, messages) ->
            messages.forEach { message ->
                val id = message.id.substringAfter(' ')
                val expected = source.getValue(id).arguments.toSet()
                if (message.arguments.toSet() != expected) {
                    throw I18nException("$locale/$namespace $id has arguments ${message.arguments.describe()}, but $baseLocale has ${expected.toList().describe()}")
                }
            }
        }
        return parsed.mapValues { (_, messages) -> messages.map { Message(it.id.substringAfter(' '), it.parts) } }
    }

    /** Fails on a message missing from a shipped locale, or one the source doesn't have. */
    fun checkComplete(namespace: String, optional: (String) -> Boolean = { false }) {
        val source = texts(namespace, baseLocale).keys
        locales.filter { it != baseLocale }.forEach { locale ->
            val ids = texts(namespace, locale).keys
            val missing = (source - ids).filterNot(optional)
            if (missing.isNotEmpty()) throw I18nException("$locale/$namespace.json is missing ${missing.joinToString()}")
            val unknown = ids - source
            if (unknown.isNotEmpty()) throw I18nException("$locale/$namespace.json has ${unknown.joinToString()}, which $baseLocale/$namespace.json doesn't")
        }
    }

    companion object {
        private val ID_SEGMENT = Regex("[a-z0-9][a-zA-Z0-9-]*")

        fun read(root: File): Translations {
            val settings = Json.parseToJsonElement(File(root, "project.inlang/settings.json").readText()).jsonObject
            val base = settings.getValue("baseLocale").jsonPrimitive.content
            val locales = settings.getValue("locales").jsonArray.map { it.jsonPrimitive.content }
            val namespaces = File(root, "messages/$base").listFiles { f -> f.extension == "json" }.orEmpty().map { it.nameWithoutExtension }.sorted()
            val texts = namespaces.associateWith { namespace ->
                locales.associateWith { locale ->
                    val file = File(root, "messages/$locale/$namespace.json")
                    if (!file.exists()) throw I18nException("${file.path} is missing")
                    flatten(Json.parseToJsonElement(file.readText()).jsonObject, prefix = "", file = file)
                }
            }
            return Translations(base, locales, texts)
        }

        private fun flatten(json: JsonObject, prefix: String, file: File): Map<String, String> = buildMap {
            json.forEach { (key, value) ->
                if (!ID_SEGMENT.matches(key)) throw I18nException("${file.path}: \"$prefix$key\" is not a message id segment")
                val id = prefix + key
                when {
                    value is JsonObject -> putAll(flatten(value, "$id.", file))
                    value is JsonPrimitive && value.isString -> put(id, value.content)
                    else -> throw I18nException("${file.path}: $id is neither text nor a group")
                }
            }
        }
    }
}

private fun List<Argument>.describe() = if (isEmpty()) "none" else joinToString { "{${it.name}${if (it.type == ArgumentType.NUMBER) ", number" else ""}}" }
