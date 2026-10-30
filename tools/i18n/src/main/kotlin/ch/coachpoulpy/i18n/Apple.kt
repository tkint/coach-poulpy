package ch.coachpoulpy.i18n

import java.io.File

/**
 * iOS string tables: `<locale>.lproj/Localizable.strings`, and `Localizable.stringsdict` for the messages
 * with a plural. A message's key is its id followed by one format per argument, in order of first
 * appearance in the source locale (`plan.title %@`, `player.step %lld %lld`), which is the key SwiftUI
 * looks up for `Text("player.step \(index) \(count)")`. The text uses positional arguments: `%1$@` for
 * text, `%1$lld` for a number.
 */
object Apple {
    fun write(translations: Translations, namespace: String, outputDir: File) {
        val messages = translations.messages(namespace)
        val order = messages.getValue(translations.baseLocale).associate { it.id to it.arguments }
        outputDir.deleteRecursively()
        messages.forEach { (locale, list) ->
            val folder = File(outputDir, "$locale.lproj").apply { mkdirs() }
            File(folder, "Localizable.strings").writeText(strings(list.filter { it.plural == null }, order))
            File(folder, "Localizable.stringsdict").writeText(stringsdict(list.filter { it.plural != null }, order))
        }
    }

    fun key(id: String, arguments: List<Argument>) =
        (listOf(id) + arguments.map { if (it.type == ArgumentType.NUMBER) "%lld" else "%@" }).joinToString(" ")

    fun strings(messages: List<Message>, order: Map<String, List<Argument>>): String = buildString {
        appendLine("/* Generated from i18n/messages by tools/i18n. Do not edit: change the JSON instead. */")
        messages.forEach { message ->
            val arguments = order.getValue(message.id)
            appendLine("\"${quote(key(message.id, arguments))}\" = \"${quote(text(message.parts, arguments))}\";")
        }
    }

    fun stringsdict(messages: List<Message>, order: Map<String, List<Argument>>): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">""")
        appendLine("<!-- Generated from i18n/messages by tools/i18n. Do not edit: change the JSON instead. -->")
        appendLine("""<plist version="1.0">""")
        appendLine("<dict>")
        messages.forEach { message ->
            val arguments = order.getValue(message.id)
            val plural = message.plural!!
            val index = message.parts.indexOf(plural)
            val position = arguments.indexOfFirst { it.name == plural.name } + 1
            val format = text(message.parts.subList(0, index), arguments) + "%$position$#@${plural.name}@" +
                text(message.parts.subList(index + 1, message.parts.size), arguments)
            appendLine("    <key>${xml(key(message.id, arguments))}</key>")
            appendLine("    <dict>")
            appendLine("        <key>NSStringLocalizedFormatKey</key>")
            appendLine("        <string>${xml(format)}</string>")
            appendLine("        <key>${plural.name}</key>")
            appendLine("        <dict>")
            appendLine("            <key>NSStringFormatSpecTypeKey</key>")
            appendLine("            <string>NSStringPluralRuleType</string>")
            appendLine("            <key>NSStringFormatValueTypeKey</key>")
            appendLine("            <string>lld</string>")
            expand(message).keys.forEach { selector ->
                appendLine("            <key>$selector</key>")
                appendLine("            <string>${xml(text(plural.branches.getValue(selector), arguments))}</string>")
            }
            appendLine("        </dict>")
            appendLine("    </dict>")
        }
        appendLine("</dict>")
        appendLine("</plist>")
    }

    private fun text(parts: List<Part>, arguments: List<Argument>): String = parts.joinToString("") { part ->
        when (part) {
            is Part.Text -> if (arguments.isEmpty()) part.text else part.text.replace("%", "%%")
            is Part.Argument -> placeholder(part.name, part.type, arguments)
            is Part.Pound -> placeholder(part.name, ArgumentType.NUMBER, arguments)
            is Part.Plural -> error("plurals are written as stringsdict variables")
        }
    }

    private fun placeholder(name: String, type: ArgumentType, arguments: List<Argument>): String {
        val position = arguments.indexOfFirst { it.name == name } + 1
        return "%$position$" + if (type == ArgumentType.NUMBER) "lld" else "@"
    }

    private fun quote(text: String) = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    private fun xml(text: String) = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
