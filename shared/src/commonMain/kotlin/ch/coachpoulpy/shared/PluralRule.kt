package ch.coachpoulpy.shared

/** The CLDR plural categories a translation can branch on; `other` is every number no rule claims. */
internal enum class PluralCategory { ZERO, ONE, TWO, FEW, MANY, OTHER }

/**
 * Which plural branch a whole number takes in each shipped language, from the CLDR rules Android and iOS
 * also follow, so the generated `DomainText` picks the branch the apps would. A new language adds its rule
 * here; until then it gets `other`.
 */
internal object PluralRule {
    fun category(locale: String, n: Int): PluralCategory = when (locale) {
        "en" -> if (n == 1) PluralCategory.ONE else PluralCategory.OTHER
        // French: 0 and 1 are singular; a million and its multiples take "de" (un million de pas).
        "fr" -> when {
            n == 0 || n == 1 -> PluralCategory.ONE
            n % 1_000_000 == 0 -> PluralCategory.MANY
            else -> PluralCategory.OTHER
        }
        else -> PluralCategory.OTHER
    }
}
