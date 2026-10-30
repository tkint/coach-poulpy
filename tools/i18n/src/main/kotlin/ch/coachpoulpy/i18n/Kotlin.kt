package ch.coachpoulpy.i18n

import java.io.File

/**
 * The `domain` text as one Kotlin object with a function per message: `muscle.chest` is
 * `DomainText.muscleChest(language)`, `count.rounds` is `DomainText.countRounds(language, count)`. A
 * `{x, number}` or plural argument is an `Int` parameter and `{x}` a `String`, in order of first appearance
 * in the source locale, so a mistyped id or argument fails the build. Plurals choose their branch with
 * `PluralRule`, written by hand next to the generated file; an unknown language gets the source locale.
 */
object Kotlin {
    fun write(translations: Translations, namespace: String, packageName: String, objectName: String, outputDir: File) {
        val source = source(translations, namespace, packageName, objectName)
        outputDir.deleteRecursively()
        val file = File(outputDir, packageName.replace('.', '/') + "/$objectName.kt")
        file.parentFile.mkdirs()
        file.writeText(source)
    }

    fun source(translations: Translations, namespace: String, packageName: String, objectName: String): String = buildString {
        val messages = translations.messages(namespace)
        val base = translations.baseLocale
        val others = translations.locales.filter { it != base }
        val byLocale = messages.mapValues { (_, list) -> list.associateBy { it.id } }
        appendLine("// Generated from i18n/messages/*/$namespace.json by tools/i18n. Do not edit: change the JSON instead.")
        appendLine("package $packageName")
        appendLine()
        appendLine("internal object $objectName {")
        appendLine("    /** The shipped locale for a language code such as \"fr\" or \"fr-CH\", or \"$base\". */")
        appendLine("    fun locale(language: String): String = when (language.substringBefore('-').substringBefore('_').lowercase()) {")
        others.forEach { appendLine("        \"$it\" -> \"$it\"") }
        appendLine("        else -> \"$base\"")
        appendLine("    }")
        val names = mutableMapOf<String, String>()
        messages.getValue(base).forEach { message ->
            val name = functionName(message.id)
            names.put(name, message.id)?.let { throw I18nException("${message.id} and $it both become $name") }
            val parameters = listOf("language: String") + message.arguments.map { "${it.name}: ${if (it.type == ArgumentType.NUMBER) "Int" else "String"}" }
            appendLine()
            val usesRule = translations.locales.any { locale -> byLocale.getValue(locale).getValue(message.id).plural?.branches?.keys.orEmpty().any(::isCategory) }
            val subject = if (usesRule) "val locale = locale(language)" else "locale(language)"
            appendLine("    /** `${message.id}`: ${comment(translations.texts(namespace, base).getValue(message.id))} */")
            appendLine("    fun $name(${parameters.joinToString()}): String = when ($subject) {")
            others.forEach { locale -> appendLine("        \"$locale\" -> ${expression(byLocale.getValue(locale).getValue(message.id), "locale", "        ")}") }
            appendLine("        else -> ${expression(message, "locale", "        ")}")
            appendLine("    }")
        }
        appendLine("}")
    }

    fun functionName(id: String): String = id.split('.').mapIndexed { index, segment ->
        val words = segment.split('-')
        words.mapIndexed { i, word -> if (index == 0 && i == 0) word else word.replaceFirstChar(Char::uppercaseChar) }.joinToString("")
    }.joinToString("")

    private fun expression(message: Message, locale: String, indent: String): String {
        val plural = message.plural ?: return template(message.parts)
        val index = message.parts.indexOf(plural)
        val before = message.parts.subList(0, index)
        val after = message.parts.subList(index + 1, message.parts.size)
        val categories = plural.branches.keys.filter(::isCategory)
        return buildString {
            appendLine("when {")
            plural.branches.forEach { (selector, branch) ->
                if (selector.startsWith("=")) appendLine("$indent    ${plural.name} == ${selector.drop(1)} -> ${template(before + branch + after)}")
            }
            categories.forEach { category ->
                val branch = plural.branches.getValue(category)
                appendLine("$indent    PluralRule.category($locale, ${plural.name}) == PluralCategory.${category.uppercase()} -> ${template(before + branch + after)}")
            }
            appendLine("$indent    else -> ${template(before + plural.branches.getValue("other") + after)}")
            append("$indent}")
        }
    }

    private fun template(parts: List<Part>): String = parts.joinToString("", prefix = "\"", postfix = "\"") { part ->
        when (part) {
            is Part.Text -> part.text.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$").replace("\n", "\\n")
            is Part.Argument -> "\${${part.name}}"
            is Part.Pound -> "\${${part.name}}"
            is Part.Plural -> error("one plural per message")
        }
    }

    /** A plural branch chosen by the language's rule: not `other`, which is the fallback, nor an exact `=N`. */
    private fun isCategory(selector: String) = selector != "other" && !selector.startsWith("=")

    private fun comment(text: String) = text.replace("*/", "* /").replace("\n", " ")
}
