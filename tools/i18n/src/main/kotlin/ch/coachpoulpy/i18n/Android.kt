package ch.coachpoulpy.i18n

import java.io.File

/**
 * Android string resources: `values/strings.xml` for the source locale, `values-<locale>/strings.xml`
 * for the others. A message id becomes a resource name with dots as underscores (`home.seeWorkout` is
 * `R.string.home_seeWorkout`); a message with a plural becomes a `<plurals>`. Arguments are positional in
 * order of first appearance in the source locale: `%1$s` for text, `%1$d` for a number.
 */
object Android {
    fun write(translations: Translations, namespace: String, outputDir: File) {
        val messages = translations.messages(namespace)
        val order = messages.getValue(translations.baseLocale).associate { it.id to it.arguments }
        outputDir.deleteRecursively()
        messages.forEach { (locale, list) ->
            val folder = if (locale == translations.baseLocale) "values" else "values-${qualifier(locale)}"
            val file = File(outputDir, "$folder/strings.xml")
            file.parentFile.mkdirs()
            file.writeText(strings(list, order))
        }
    }

    fun strings(messages: List<Message>, order: Map<String, List<Argument>>): String = buildString {
        appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        appendLine("<!-- Generated from i18n/messages by tools/i18n. Do not edit: change the JSON instead. -->")
        appendLine("<resources>")
        messages.forEach { message ->
            val arguments = order.getValue(message.id)
            val name = resourceName(message.id)
            val plural = message.plural
            if (plural == null) {
                appendLine("""    <string name="$name">${text(message.parts, arguments)}</string>""")
            } else {
                appendLine("""    <plurals name="$name">""")
                expand(message).forEach { (quantity, parts) ->
                    appendLine("""        <item quantity="$quantity">${text(parts, arguments)}</item>""")
                }
                appendLine("    </plurals>")
            }
        }
        appendLine("</resources>")
    }

    fun resourceName(id: String) = id.replace('.', '_')

    private fun qualifier(locale: String): String {
        val (language, region) = locale.split('-').let { it[0] to it.getOrNull(1) }
        return if (region == null) language else "$language-r$region"
    }

    private fun text(parts: List<Part>, arguments: List<Argument>): String {
        val formatted = arguments.isNotEmpty()
        val body = parts.joinToString("") { part ->
            when (part) {
                is Part.Text -> escape(part.text, formatted)
                is Part.Argument -> placeholder(part.name, part.type, arguments)
                is Part.Pound -> placeholder(part.name, ArgumentType.NUMBER, arguments)
                is Part.Plural -> error("plurals are expanded before")
            }
        }
        // aapt trims and collapses unquoted whitespace, and reads a leading @ or ? as a reference.
        if (body.startsWith(' ') || body.endsWith(' ') || "  " in body) return "\"$body\""
        return if (body.startsWith('@') || body.startsWith('?')) "\\$body" else body
    }

    private fun placeholder(name: String, type: ArgumentType, arguments: List<Argument>): String {
        val position = arguments.indexOfFirst { it.name == name } + 1
        return "%$position$" + if (type == ArgumentType.NUMBER) "d" else "s"
    }

    private fun escape(text: String, formatted: Boolean): String = buildString {
        text.forEach { c ->
            when (c) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '%' -> append(if (formatted) "%%" else "%")
                else -> append(c)
            }
        }
    }
}

/**
 * A message with a plural, written out once per plural category with the text around the plural
 * repeated in each. Native plural files have no `=0`-style exact branches, so those are refused.
 */
internal fun expand(message: Message): Map<String, List<Part>> {
    val plural = message.plural ?: return mapOf("other" to message.parts)
    val index = message.parts.indexOf(plural)
    val before = message.parts.subList(0, index)
    val after = message.parts.subList(index + 1, message.parts.size)
    return plural.branches.mapValues { (selector, branch) ->
        if (selector.startsWith("=")) throw I18nException("${message.id} has an exact plural branch ($selector), which Android and iOS can't express")
        before + branch + after
    }
}
