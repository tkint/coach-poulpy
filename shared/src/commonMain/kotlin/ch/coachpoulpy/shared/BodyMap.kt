package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.MuscleGroup

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

    /**
     * The areas an exercise works, from its coarse [MuscleGroup]s: both sides of each group's areas, in reading
     * order. Full body lights every muscle area but not the joints, the spine or the head.
     */
    fun worked(groups: Collection<MuscleGroup>): List<String> {
        val areas = groups.flatMap { if (it == MuscleGroup.FULL_BODY) MUSCLES else AREAS_OF.getValue(it) }.toSet()
        return AREAS.filter { area -> area.removeSuffix("-left").removeSuffix("-right") in areas }
    }

    private val MUSCLES = listOf(
        "trapezius", "deltoid", "pectoral", "serratus", "biceps", "triceps", "forearm", "abs", "obliques", "back",
        "scapula", "lower-back", "glutes", "quads", "adductors", "hamstrings", "calves",
    )

    private val AREAS_OF: Map<MuscleGroup, List<String>> = mapOf(
        MuscleGroup.CHEST to listOf("pectoral"),
        MuscleGroup.BACK to listOf("back", "scapula", "lower-back"),
        MuscleGroup.SHOULDERS to listOf("deltoid"),
        MuscleGroup.BICEPS to listOf("biceps"),
        MuscleGroup.TRICEPS to listOf("triceps"),
        MuscleGroup.FOREARMS to listOf("forearm"),
        MuscleGroup.CORE to listOf("abs", "obliques"),
        MuscleGroup.GLUTES to listOf("glutes"),
        MuscleGroup.QUADRICEPS to listOf("quads"),
        MuscleGroup.HAMSTRINGS to listOf("hamstrings"),
        MuscleGroup.CALVES to listOf("calves"),
        MuscleGroup.HIPS to listOf("hip", "adductors"),
        MuscleGroup.NECK to listOf("neck", "trapezius"),
    )

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

/**
 * The body zones of the exercise filter: the map's muscle areas, with left and right together. A zone is
 * picked by its area name without a side (`pectoral`), which is what [ExerciseListModel.filter] takes; the
 * figure still shows both sides lit, so what is picked is kept as the figure's keys (`pectoral-left`, `pectoral-right`).
 * Joints, the spine and the head are not zones: no exercise is filed under them.
 */
object Zones {
    private val MUSCLES = listOf(
        "neck", "trapezius", "deltoid", "pectoral", "serratus", "biceps", "triceps", "forearm", "abs", "obliques",
        "back", "scapula", "lower-back", "hip", "glutes", "quads", "adductors", "hamstrings", "calves",
    )

    /** Taps [key] on the figure: both sides of its zone are picked, or both are cleared; a key that is no zone changes nothing. */
    fun toggle(picked: Set<String>, key: String): Set<String> {
        val zone = zoneOf(key)
        if (zone !in MUSCLES) return picked
        val both = setOf("$zone-left", "$zone-right")
        return if (key in picked) picked - both else picked + both
    }

    /** The zones picked, by area name without a side, for [ExerciseListModel.filter]. */
    fun zones(picked: Set<String>): Set<String> = picked.map(::zoneOf).filter { it in MUSCLES }.toSet()

    /** "Chest, shoulders": the zones picked in reading order, or empty. */
    fun summary(picked: Set<String>, languageCode: String): String {
        val zones = zones(picked)
        return MUSCLES.filter { it in zones }.mapIndexed { index, zone ->
            label(zone, languageCode).let { if (index == 0) it else it.lowercase() }
        }.joinToString(", ")
    }

    private fun zoneOf(key: String) = key.removeSuffix("-left").removeSuffix("-right")

    private fun label(zone: String, languageCode: String): String = when (zone) {
        "neck" -> DomainText.zoneNeck(languageCode)
        "trapezius" -> DomainText.zoneTrapezius(languageCode)
        "deltoid" -> DomainText.zoneDeltoid(languageCode)
        "pectoral" -> DomainText.zonePectoral(languageCode)
        "serratus" -> DomainText.zoneSerratus(languageCode)
        "biceps" -> DomainText.zoneBiceps(languageCode)
        "triceps" -> DomainText.zoneTriceps(languageCode)
        "forearm" -> DomainText.zoneForearm(languageCode)
        "abs" -> DomainText.zoneAbs(languageCode)
        "obliques" -> DomainText.zoneObliques(languageCode)
        "back" -> DomainText.zoneBack(languageCode)
        "scapula" -> DomainText.zoneScapula(languageCode)
        "lower-back" -> DomainText.zoneLowerBack(languageCode)
        "hip" -> DomainText.zoneHip(languageCode)
        "glutes" -> DomainText.zoneGlutes(languageCode)
        "quads" -> DomainText.zoneQuads(languageCode)
        "adductors" -> DomainText.zoneAdductors(languageCode)
        "hamstrings" -> DomainText.zoneHamstrings(languageCode)
        else -> DomainText.zoneCalves(languageCode)
    }
}
