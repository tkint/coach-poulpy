package ch.coachpoulpy.i18n

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GeneratorTest {
    private fun translations(en: Map<String, String>, fr: Map<String, String>, namespace: String = "app") =
        Translations("en", listOf("en", "fr"), mapOf(namespace to mapOf("en" to en, "fr" to fr)))

    @Test
    fun argumentsFollowTheirFirstAppearance() {
        val message = Message.parse("home.inDays", "{days, plural, one {In # day · {date}} other {In # days · {date}}}")
        assertEquals(listOf(Argument("days", ArgumentType.NUMBER), Argument("date", ArgumentType.TEXT)), message.arguments)
    }

    @Test
    fun brokenMessagesAreRefused() {
        assertFailsWith<I18nException> { Message.parse("a", "{count, select, one {x} other {y}}") }
        assertFailsWith<I18nException> { Message.parse("a", "{count, plural, one {x}}") }
        assertFailsWith<I18nException> { Message.parse("a", "Nothing for {query") }
        assertFailsWith<I18nException> { Message.parse("a", "{x} and {x, number}").arguments }
    }

    @Test
    fun androidUsesPositionalArgumentsInTheSourceOrder() {
        val xml = Android.strings(
            translations(mapOf("player.next" to "Next: {name} · {detail}"), mapOf("player.next" to "Ensuite : {detail}, {name}"))
                .messages("app").getValue("fr"),
            mapOf("player.next" to listOf(Argument("name", ArgumentType.TEXT), Argument("detail", ArgumentType.TEXT))),
        )
        assertContains(xml, """<string name="player_next">Ensuite : %2${'$'}s, %1${'$'}s</string>""")
    }

    @Test
    fun androidPluralsRepeatTheTextAroundThePlural() {
        val translations = translations(
            mapOf("plan.save" to "Save {days, plural, one {# day} other {# days}} for {name}"),
            mapOf("plan.save" to "Enregistrer {days, plural, one {# jour} many {# jours} other {# jours}} pour {name}"),
        )
        val order = translations.messages("app").getValue("en").associate { it.id to it.arguments }
        val fr = Android.strings(translations.messages("app").getValue("fr"), order)
        assertContains(fr, """<item quantity="one">Enregistrer %1${'$'}d jour pour %2${'$'}s</item>""")
        assertContains(fr, """<item quantity="many">Enregistrer %1${'$'}d jours pour %2${'$'}s</item>""")
    }

    @Test
    fun androidEscapesQuotesAndPercentOnlyWhenFormatting() {
        val translations = translations(
            mapOf("a.quote" to "It's \"on\" & <off>", "a.percent" to "100%", "a.battery" to "battery {percent, number}%"),
            mapOf("a.quote" to "C’est « ça »", "a.percent" to "100 %", "a.battery" to "batterie {percent, number} %"),
        )
        val order = translations.messages("app").getValue("en").associate { it.id to it.arguments }
        val en = Android.strings(translations.messages("app").getValue("en"), order)
        assertContains(en, """<string name="a_quote">It\'s \"on\" &amp; &lt;off&gt;</string>""")
        assertContains(en, """<string name="a_percent">100%</string>""")
        assertContains(en, """<string name="a_battery">battery %1${'$'}d%%</string>""")
    }

    @Test
    fun exactPluralBranchesAreRefusedOnThePhones() {
        val translations = translations(
            mapOf("part.addToWork" to "{number, plural, =0 {Add to Work} other {Add to Work #}}"),
            mapOf("part.addToWork" to "{number, plural, =0 {Ajouter} other {Ajouter #}}"),
        )
        assertFailsWith<I18nException> { Android.strings(translations.messages("app").getValue("en"), mapOf("part.addToWork" to emptyList())) }
    }

    @Test
    fun appleKeysCarryOneFormatPerArgument() {
        val translations = translations(
            mapOf("player.summary" to "{done, number} of {planned, number} sets done in {duration}", "workout.times" to "{count, plural, one {# time} other {# times}}"),
            mapOf("player.summary" to "{done, number} séries sur {planned, number} faites en {duration}", "workout.times" to "{count, plural, one {# fois} many {# fois} other {# fois}}"),
        )
        val messages = translations.messages("app")
        val order = messages.getValue("en").associate { it.id to it.arguments }
        val strings = Apple.strings(messages.getValue("fr").filter { it.plural == null }, order)
        assertContains(strings, "\"player.summary %lld %lld %@\" = \"%1\$lld séries sur %2\$lld faites en %3\$@\";")
        val dict = Apple.stringsdict(messages.getValue("en").filter { it.plural != null }, order)
        assertContains(dict, "<key>workout.times %lld</key>")
        assertContains(dict, "<string>%1\$#@count@</string>")
        assertContains(dict, "<string>%1\$lld times</string>")
    }

    @Test
    fun domainTextIsOneTypedFunctionPerMessage() {
        val source = Kotlin.source(
            translations(
                mapOf("muscle.fullBody" to "Full body", "count.rounds" to "{count, plural, one {# round} other {# rounds}}", "part.addedAfter" to "Added after {after} as {part}"),
                mapOf("muscle.fullBody" to "Corps entier", "count.rounds" to "{count, plural, one {# tour} many {# tours} other {# tours}}", "part.addedAfter" to "Ajouté après {after} : {part}"),
                namespace = "domain",
            ),
            "domain", "ch.coachpoulpy.shared", "DomainText",
        )
        assertContains(source, "fun muscleFullBody(language: String): String = when (locale(language)) {")
        assertContains(source, "\"fr\" -> \"Corps entier\"")
        assertContains(source, "fun countRounds(language: String, count: Int): String")
        assertContains(source, "PluralRule.category(locale, count) == PluralCategory.MANY -> \"\${count} tours\"")
        assertContains(source, "fun partAddedAfter(language: String, after: String, part: String)")
        assertContains(source, "\"fr\" -> \"Ajouté après \${after} : \${part}\"")
    }

    @Test
    fun aMissingOrExtraMessageFails() {
        assertFailsWith<I18nException> { translations(mapOf("a.b" to "x", "a.c" to "y"), mapOf("a.b" to "x")).messages("app") }
        assertFailsWith<I18nException> { translations(mapOf("a.b" to "x"), mapOf("a.b" to "x", "a.c" to "y")).messages("app") }
    }

    @Test
    fun aTranslationWithOtherArgumentsFails() {
        assertFailsWith<I18nException> { translations(mapOf("a.b" to "Hi {name}"), mapOf("a.b" to "Salut {nom}")).messages("app") }
        assertFailsWith<I18nException> { translations(mapOf("a.b" to "{n, number} s"), mapOf("a.b" to "{n} s")).messages("app") }
    }

    @Test
    fun functionNamesJoinTheIdInCamelCase() {
        assertEquals("muscleAreaFullBody", Kotlin.functionName("muscleArea.fullBody"))
        assertEquals("gobletSquatName", Kotlin.functionName("goblet-squat.name"))
        assertTrue(Android.resourceName("home.seeWorkout") == "home_seeWorkout")
    }
}
