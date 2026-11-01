package ch.coachpoulpy.shared

/** One tile of the finished screen, for example "Time" and "38:24". */
data class FinishedStat(val label: String, val value: String)

/**
 * What the finished screen shows besides the summary line: the workout's name and date, up to three stats, and Poulpy
 * with the tentacle this session lit. Decided here so Android and iOS draw the same screen (`player-finished` and
 * `player-finished-run` mockups).
 */
data class FinishedScreen(
    val workoutName: String,
    val startedAtMillis: Long,
    val stats: List<FinishedStat>,
    val poulpy: PoulpyStatus,
)

/** What a guided session logged, from which [FinishedStats] picks the tiles. */
data class FinishedFacts(
    val activeSeconds: Long,
    val setsDone: Int,
    /** Weight times reps over the sets done, in kilograms; 0 when nothing was lifted. */
    val liftedKg: Double,
    /** Distance of the sets done, in metres; 0 when the workout has no distance target. */
    val distanceMeters: Double,
    /** The unit system the run's distance was picked in, when it was a whole number of miles or kilometres; null follows Settings. */
    val distanceSystem: UnitSystem? = null,
)

/**
 * Time always, then distance and pace for a run or a walk, or sets and weight lifted otherwise. Never heart rate. A
 * stat with nothing logged is left out, so a bodyweight session shows Time and Sets.
 */
object FinishedStats {
    fun of(facts: FinishedFacts, units: UnitSystem, languageCode: String): List<FinishedStat> = buildList {
        add(FinishedStat(DomainText.shareTime(languageCode), ShareCard.clock(facts.activeSeconds)))
        if (facts.distanceMeters > 0) {
            val shown = facts.distanceSystem ?: units
            add(FinishedStat(DomainText.shareDistance(languageCode), ShareCard.measure(Units.distance(facts.distanceMeters, shown, decimals = 1), languageCode)))
            pace(facts, shown)?.let { add(FinishedStat(DomainText.sharePace(languageCode), it)) }
        } else {
            add(FinishedStat(DomainText.shareSets(languageCode), "${facts.setsDone}"))
            if (facts.liftedKg > 0) add(FinishedStat(DomainText.shareLifted(languageCode), lifted(facts.liftedKg, units, languageCode)))
        }
    }

    /** "7:23 /km"; null when the session was too short or too slow for a pace to mean anything. */
    private fun pace(facts: FinishedFacts, units: UnitSystem): String? {
        if (facts.activeSeconds <= 0) return null
        val kmh = facts.distanceMeters / 1000 / (facts.activeSeconds / 3600.0)
        val seconds = Units.paceSeconds(kmh, units)?.let { kotlin.math.round(it).toLong() } ?: return null
        return "${ShareCard.clock(seconds)} ${Units.paceUnit(units)}"
    }

    /** Metric: "820 kg" below a tonne, "2.1 t" above. Imperial: whole pounds, "1,807 lb" in English. */
    private fun lifted(kg: Double, units: UnitSystem, languageCode: String): String = when {
        units == UnitSystem.IMPERIAL -> DomainText.unitPounds(languageCode, Units.poundsOf(kg).let { kotlin.math.round(it).toLong().toString() })
        kg < 1000 -> Labels(languageCode).kilograms(kotlin.math.round(kg))
        else -> DomainText.unitTonnes(languageCode, ShareCard.measure(Measure(kg / 1000, 1, ""), languageCode).removeSuffix("\u00a0"))
    }
}
