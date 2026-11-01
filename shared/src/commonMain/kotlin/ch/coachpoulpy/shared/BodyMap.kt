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
        "neck", "trapezius", "deltoid", "pectoral", "serratus", "biceps", "triceps", "forearm", "elbow",
        "wrist", "hand", "abs", "obliques", "back", "scapula", "lower-back", "hip", "glutes",
        "quads", "adductors", "hamstrings", "knee", "shin", "calves", "ankle", "foot",
    ).flatMap { listOf("$it-left", "$it-right") } + SPINE

    /** The one area that has no side: the spine, drawn down the middle of the back. */
    const val SPINE = "spine"

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
        "serratus-left" -> DomainText.bodyAreaSerratusLeft(languageCode)
        "serratus-right" -> DomainText.bodyAreaSerratusRight(languageCode)
        "biceps-left" -> DomainText.bodyAreaBicepsLeft(languageCode)
        "biceps-right" -> DomainText.bodyAreaBicepsRight(languageCode)
        "triceps-left" -> DomainText.bodyAreaTricepsLeft(languageCode)
        "triceps-right" -> DomainText.bodyAreaTricepsRight(languageCode)
        "forearm-left" -> DomainText.bodyAreaForearmLeft(languageCode)
        "forearm-right" -> DomainText.bodyAreaForearmRight(languageCode)
        "elbow-left" -> DomainText.bodyAreaElbowLeft(languageCode)
        "elbow-right" -> DomainText.bodyAreaElbowRight(languageCode)
        "wrist-left" -> DomainText.bodyAreaWristLeft(languageCode)
        "wrist-right" -> DomainText.bodyAreaWristRight(languageCode)
        "hand-left" -> DomainText.bodyAreaHandLeft(languageCode)
        "hand-right" -> DomainText.bodyAreaHandRight(languageCode)
        "abs-left" -> DomainText.bodyAreaAbsLeft(languageCode)
        "abs-right" -> DomainText.bodyAreaAbsRight(languageCode)
        "obliques-left" -> DomainText.bodyAreaObliquesLeft(languageCode)
        "obliques-right" -> DomainText.bodyAreaObliquesRight(languageCode)
        "back-left" -> DomainText.bodyAreaBackLeft(languageCode)
        "back-right" -> DomainText.bodyAreaBackRight(languageCode)
        "scapula-left" -> DomainText.bodyAreaScapulaLeft(languageCode)
        "scapula-right" -> DomainText.bodyAreaScapulaRight(languageCode)
        "lower-back-left" -> DomainText.bodyAreaLowerBackLeft(languageCode)
        "lower-back-right" -> DomainText.bodyAreaLowerBackRight(languageCode)
        "hip-left" -> DomainText.bodyAreaHipLeft(languageCode)
        "hip-right" -> DomainText.bodyAreaHipRight(languageCode)
        "glutes-left" -> DomainText.bodyAreaGlutesLeft(languageCode)
        "glutes-right" -> DomainText.bodyAreaGlutesRight(languageCode)
        "quads-left" -> DomainText.bodyAreaQuadsLeft(languageCode)
        "quads-right" -> DomainText.bodyAreaQuadsRight(languageCode)
        "adductors-left" -> DomainText.bodyAreaAdductorsLeft(languageCode)
        "adductors-right" -> DomainText.bodyAreaAdductorsRight(languageCode)
        "hamstrings-left" -> DomainText.bodyAreaHamstringsLeft(languageCode)
        "hamstrings-right" -> DomainText.bodyAreaHamstringsRight(languageCode)
        "knee-left" -> DomainText.bodyAreaKneeLeft(languageCode)
        "knee-right" -> DomainText.bodyAreaKneeRight(languageCode)
        "shin-left" -> DomainText.bodyAreaShinLeft(languageCode)
        "shin-right" -> DomainText.bodyAreaShinRight(languageCode)
        "calves-left" -> DomainText.bodyAreaCalvesLeft(languageCode)
        "calves-right" -> DomainText.bodyAreaCalvesRight(languageCode)
        "ankle-left" -> DomainText.bodyAreaAnkleLeft(languageCode)
        "ankle-right" -> DomainText.bodyAreaAnkleRight(languageCode)
        "foot-left" -> DomainText.bodyAreaFootLeft(languageCode)
        "foot-right" -> DomainText.bodyAreaFootRight(languageCode)
        SPINE -> DomainText.bodyAreaSpine(languageCode)
        OTHER -> DomainText.bodyAreaOther(languageCode)
        else -> key
    }
}
