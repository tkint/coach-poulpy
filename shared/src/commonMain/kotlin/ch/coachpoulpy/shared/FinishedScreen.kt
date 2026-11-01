package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout

/** One tile of the finished screen, for example "Time" and "38:24". */
data class FinishedStat(val label: String, val value: String)

/**
 * What the finished screen shows: the heading and Poulpy's quote, the workout's name and date, how many sets were done, up to three stats,
 * and Poulpy with the tentacle this session lit. Decided here so Android and iOS draw the same screen (`player-finished`
 * and `player-finished-run` mockups), and the same content is what the shared image shows, straight after the session or
 * from the history ([FinishedScreens.of]).
 */
data class FinishedScreen(
    val heading: String,
    /** Poulpy's line under the heading, in quotes. */
    val quote: String,
    /** Exhausted after a session; Champion when it lit the eighth tentacle. */
    val drawing: PoulpyDrawing,
    val workoutName: String,
    val startedAtMillis: Long,
    /** "17 of 18 sets done in 38:24 · 1 skipped"; null when there was no plan to count sets in. */
    val summary: String?,
    val stats: List<FinishedStat>,
    val poulpy: PoulpyStatus,
)

object FinishedScreens {
    /**
     * The finished screen of a session. [workout] and [timeline] are what it followed, null when they are no longer on
     * the phone (the sets and the weight lifted are then left out); [poulpy] is what Poulpy says about the tentacle it lit.
     */
    internal fun of(
        workoutName: String,
        workout: Workout?,
        timeline: Timeline?,
        events: List<SessionEvent>,
        poulpy: PoulpyStatus,
        units: UnitSystem,
        languageCode: String,
    ): FinishedScreen {
        val labels = Labels(languageCode)
        val phases = timeline?.phases.orEmpty()
        val steps = workout?.steps?.flatMap { it.exerciseSteps() }?.associateBy { it.id }.orEmpty()
        fun work(pick: (SessionEvent) -> Int?) = events.mapNotNull(pick).mapNotNull { phases.getOrNull(it) as? Phase.Work }
        val doneWork = work { (it as? SessionEvent.PhaseCompleted)?.phaseIndex }
        val skipped = work { (it as? SessionEvent.PhaseSkipped)?.phaseIndex }.size
        var liftedKg = 0.0
        var targetMeters = 0.0
        val pickedUnits = mutableSetOf<DistanceUnit?>()
        for (set in doneWork) {
            val load = steps[set.step]?.prescription?.sets?.getOrNull(set.setIndex)?.load as? Load.Kilograms
            val reps = (set.target as? Target.Reps)?.count
            if (load != null && reps != null) liftedKg += load.kg * reps
            (set.target as? Target.Distance)?.let { targetMeters += it.meters; pickedUnits += DistanceUnit.signature(it.meters) }
        }
        val totals = sessionTotals(events, timeline)
        val facts = FinishedFacts(
            activeSeconds = totals.active.inWholeSeconds,
            setsDone = doneWork.size,
            liftedKg = liftedKg,
            // The distance of the sets done, or what a machine reported when the plan has none.
            distanceMeters = if (targetMeters > 0) targetMeters else totals.distanceMeters ?: 0.0,
            // A run set in miles (or kilometres) reads in them, whatever the unit system.
            distanceSystem = when (pickedUnits.singleOrNull()) {
                DistanceUnit.MI -> UnitSystem.IMPERIAL
                DistanceUnit.KM -> UnitSystem.METRIC
                else -> null
            },
        )
        val champion = poulpy.tentaclesLit == PoulpyStatus.TENTACLES
        val start = events.firstOrNull { it is SessionEvent.Started }?.at
        val end = events.lastOrNull { it is SessionEvent.Finished }?.at
        val planned = phases.count { it is Phase.Work }
        val seconds = if (start != null && end != null) (end - start).inWholeSeconds.toInt() else 0
        val summary = if (planned == 0) null else
            DomainText.shareSummary(languageCode, doneWork.size, planned, labels.duration(seconds)) +
                if (skipped > 0) " \u00b7 " + DomainText.shareSummarySkipped(languageCode, skipped) else ""
        return FinishedScreen(
            heading = if (champion) DomainText.shareChampionHeading(languageCode) else DomainText.shareWellDone(languageCode),
            quote = if (champion) DomainText.shareChampionQuote(languageCode) else DomainText.shareQuote(languageCode),
            drawing = if (champion) PoulpyDrawing.CHAMPION else PoulpyDrawing.EXHAUSTED,
            workoutName = workoutName,
            startedAtMillis = start?.toEpochMilliseconds() ?: 0L,
            summary = summary,
            stats = FinishedStats.of(facts, units, languageCode),
            poulpy = poulpy,
        )
    }
}

internal fun Step.exerciseSteps(): List<Step.ExerciseStep> = when (this) {
    is Step.ExerciseStep -> listOf(this)
    is Step.Group -> children.flatMap { it.exerciseSteps() }
    is Step.Rest -> emptyList()
}

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
        kg < 1000 -> Labels(languageCode).weight(kotlin.math.round(kg))
        else -> DomainText.unitTonnes(languageCode, ShareCard.measure(Measure(kg / 1000, 1, ""), languageCode).removeSuffix("\u00a0"))
    }
}
