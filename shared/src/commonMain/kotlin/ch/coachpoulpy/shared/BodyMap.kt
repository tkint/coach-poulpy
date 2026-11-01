package ch.coachpoulpy.shared

/** The two sides of the body map. */
enum class BodyView { FRONT, BACK }

/**
 * The areas of the body map and their names. An area key is `<area>-<side>` (`knee-left`), the same as in
 * `design/body-map.json`; left and right are the person's own.
 */
object BodyMap {
    /** Every area key a figure can show, in reading order. */
    val AREAS: List<String> = listOf(
        "neck", "trapezius", "deltoid", "pectoral", "biceps", "triceps", "forearm", "elbow", "hand",
        "abs", "obliques", "lats", "lower-back", "hip", "glutes", "quads", "hamstrings", "knee", "shin", "calves", "foot",
    ).flatMap { listOf("$it-left", "$it-right") }

    /** For pain that no area of the figure names; shown as a chip beside the figure. */
    const val OTHER = "other"

    /** Keeps the keys that are real areas, in reading order, so what is saved never depends on the order they were picked. */
    fun valid(keys: Collection<String>): List<String> = (AREAS + OTHER).filter { it in keys }

    /** "Left knee", "Genou gauche". */
    fun label(key: String, languageCode: String): String = when (key) {
        "neck-left" -> DomainText.bodyAreaNeckLeft(languageCode)
        "neck-right" -> DomainText.bodyAreaNeckRight(languageCode)
        "trapezius-left" -> DomainText.bodyAreaTrapeziusLeft(languageCode)
        "trapezius-right" -> DomainText.bodyAreaTrapeziusRight(languageCode)
        "deltoid-left" -> DomainText.bodyAreaDeltoidLeft(languageCode)
        "deltoid-right" -> DomainText.bodyAreaDeltoidRight(languageCode)
        "pectoral-left" -> DomainText.bodyAreaPectoralLeft(languageCode)
        "pectoral-right" -> DomainText.bodyAreaPectoralRight(languageCode)
        "biceps-left" -> DomainText.bodyAreaBicepsLeft(languageCode)
        "biceps-right" -> DomainText.bodyAreaBicepsRight(languageCode)
        "triceps-left" -> DomainText.bodyAreaTricepsLeft(languageCode)
        "triceps-right" -> DomainText.bodyAreaTricepsRight(languageCode)
        "forearm-left" -> DomainText.bodyAreaForearmLeft(languageCode)
        "forearm-right" -> DomainText.bodyAreaForearmRight(languageCode)
        "elbow-left" -> DomainText.bodyAreaElbowLeft(languageCode)
        "elbow-right" -> DomainText.bodyAreaElbowRight(languageCode)
        "hand-left" -> DomainText.bodyAreaHandLeft(languageCode)
        "hand-right" -> DomainText.bodyAreaHandRight(languageCode)
        "abs-left" -> DomainText.bodyAreaAbsLeft(languageCode)
        "abs-right" -> DomainText.bodyAreaAbsRight(languageCode)
        "obliques-left" -> DomainText.bodyAreaObliquesLeft(languageCode)
        "obliques-right" -> DomainText.bodyAreaObliquesRight(languageCode)
        "lats-left" -> DomainText.bodyAreaLatsLeft(languageCode)
        "lats-right" -> DomainText.bodyAreaLatsRight(languageCode)
        "lower-back-left" -> DomainText.bodyAreaLowerBackLeft(languageCode)
        "lower-back-right" -> DomainText.bodyAreaLowerBackRight(languageCode)
        "hip-left" -> DomainText.bodyAreaHipLeft(languageCode)
        "hip-right" -> DomainText.bodyAreaHipRight(languageCode)
        "glutes-left" -> DomainText.bodyAreaGlutesLeft(languageCode)
        "glutes-right" -> DomainText.bodyAreaGlutesRight(languageCode)
        "quads-left" -> DomainText.bodyAreaQuadsLeft(languageCode)
        "quads-right" -> DomainText.bodyAreaQuadsRight(languageCode)
        "hamstrings-left" -> DomainText.bodyAreaHamstringsLeft(languageCode)
        "hamstrings-right" -> DomainText.bodyAreaHamstringsRight(languageCode)
        "knee-left" -> DomainText.bodyAreaKneeLeft(languageCode)
        "knee-right" -> DomainText.bodyAreaKneeRight(languageCode)
        "shin-left" -> DomainText.bodyAreaShinLeft(languageCode)
        "shin-right" -> DomainText.bodyAreaShinRight(languageCode)
        "calves-left" -> DomainText.bodyAreaCalvesLeft(languageCode)
        "calves-right" -> DomainText.bodyAreaCalvesRight(languageCode)
        "foot-left" -> DomainText.bodyAreaFootLeft(languageCode)
        "foot-right" -> DomainText.bodyAreaFootRight(languageCode)
        OTHER -> DomainText.bodyAreaOther(languageCode)
        else -> key
    }
}
