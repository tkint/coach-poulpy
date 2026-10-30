package ch.coachpoulpy.i18n

/**
 * The part of ICU MessageFormat the translation files use: text, `{name}` (text),
 * `{name, number}` (a whole number) and `{name, plural, =0 {…} one {…} other {…}}`,
 * where `#` inside a branch stands for the plural argument. No message uses ICU's
 * apostrophe quoting, so an apostrophe is plain text.
 */
sealed interface Part {
    data class Text(val text: String) : Part
    data class Argument(val name: String, val type: ArgumentType) : Part
    data class Plural(val name: String, val branches: Map<String, List<Part>>) : Part
    /** `#` in a plural branch: the plural argument, as a number. */
    data class Pound(val name: String) : Part
}

enum class ArgumentType { TEXT, NUMBER }

data class Argument(val name: String, val type: ArgumentType)

class Message(val id: String, val parts: List<Part>) {
    /** Each argument once, in order of first appearance: the order positional arguments follow. */
    val arguments: List<Argument> = buildList {
        val seen = mutableMapOf<String, ArgumentType>()
        fun visit(parts: List<Part>) {
            parts.forEach { part ->
                val (name, type) = when (part) {
                    is Part.Text -> return@forEach
                    is Part.Argument -> part.name to part.type
                    is Part.Pound -> part.name to ArgumentType.NUMBER
                    is Part.Plural -> part.name to ArgumentType.NUMBER
                }
                val before = seen.put(name, type)
                if (before == null) add(Argument(name, type))
                else if (before != type) throw I18nException("$id uses {$name} both as text and as a number")
                if (part is Part.Plural) part.branches.values.forEach(::visit)
            }
        }
        visit(parts)
    }

    /** The message's only plural, if it has one. Two plurals, or one inside another, are refused. */
    val plural: Part.Plural? = parts.filterIsInstance<Part.Plural>().let { plurals ->
        if (plurals.size > 1) throw I18nException("$id has more than one plural")
        plurals.firstOrNull()?.also { plural ->
            if (plural.branches.values.any { branch -> branch.any { it is Part.Plural } }) throw I18nException("$id has a plural inside a plural")
        }
    }

    companion object {
        fun parse(id: String, pattern: String): Message = Message(id, IcuParser(id, pattern).parseMessage(pluralName = null, nested = false))
    }
}

private val NAME = Regex("[a-z][a-zA-Z0-9]*")
private val SELECTOR = Regex("=\\d+|zero|one|two|few|many|other")

private class IcuParser(private val id: String, private val text: String) {
    private var at = 0

    private fun fail(why: String): Nothing = throw I18nException("$id: $why, at character $at of \"$text\"")

    fun parseMessage(pluralName: String?, nested: Boolean): List<Part> {
        val parts = mutableListOf<Part>()
        val literal = StringBuilder()
        fun flush() {
            if (literal.isNotEmpty()) parts += Part.Text(literal.toString())
            literal.clear()
        }
        while (at < text.length) {
            when (val c = text[at]) {
                '{' -> {
                    flush()
                    at++
                    parts += parseArgument()
                }
                '}' -> {
                    if (!nested) fail("unmatched }")
                    break
                }
                '#' -> {
                    at++
                    if (pluralName != null) {
                        flush()
                        parts += Part.Pound(pluralName)
                    } else {
                        literal.append(c)
                    }
                }
                else -> {
                    literal.append(c)
                    at++
                }
            }
        }
        flush()
        return parts
    }

    private fun parseArgument(): Part {
        val name = word()
        if (!NAME.matches(name)) fail("argument name \"$name\" is not camelCase")
        skipSpaces()
        if (peek() == '}') {
            at++
            return Part.Argument(name, ArgumentType.TEXT)
        }
        expect(',')
        val type = word()
        skipSpaces()
        return when (type) {
            "number" -> {
                expect('}')
                Part.Argument(name, ArgumentType.NUMBER)
            }
            "plural" -> {
                expect(',')
                val branches = linkedMapOf<String, List<Part>>()
                while (true) {
                    skipSpaces()
                    if (peek() == '}') {
                        at++
                        break
                    }
                    val selector = word()
                    if (!SELECTOR.matches(selector)) fail("unknown plural selector \"$selector\"")
                    skipSpaces()
                    expect('{')
                    val branch = parseMessage(pluralName = name, nested = true)
                    expect('}')
                    if (branches.put(selector, branch) != null) fail("plural selector \"$selector\" twice")
                }
                if ("other" !in branches) fail("plural without an \"other\" branch")
                Part.Plural(name, branches)
            }
            else -> fail("argument type \"$type\" is not supported (only number and plural)")
        }
    }

    private fun peek(): Char = if (at < text.length) text[at] else fail("message ends inside an argument")

    private fun expect(c: Char) {
        skipSpaces()
        if (peek() != c) fail("expected '$c'")
        at++
    }

    private fun skipSpaces() {
        while (at < text.length && text[at].isWhitespace()) at++
    }

    private fun word(): String {
        skipSpaces()
        val start = at
        while (at < text.length && (text[at].isLetterOrDigit() || text[at] == '=' || text[at] == '_')) at++
        if (start == at) fail("expected a name")
        return text.substring(start, at)
    }
}

class I18nException(message: String) : RuntimeException(message)
