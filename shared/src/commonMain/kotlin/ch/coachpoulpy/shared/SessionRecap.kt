package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.covering
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.types.treadmill.TreadmillSummary
import kotlin.time.Duration

/** One figure of the recap, for example "Duration" and "31:40". */
data class RecapTile(val label: String, val value: String)

/**
 * One exercise, or one walk or run step, as done: "3 × 10 · 14 kg · 1 set skipped", or "5:00 · 0.5 km · 6.0 km/h · 102 bpm".
 * [detail] is empty only when nothing could be said about it.
 */
data class RecapRow(
    val key: String,
    val slug: String,
    val name: String,
    val pattern: MovementPattern?,
    val detail: String,
    /** Time spent on it, rests included ("3:20"), or empty when the events do not say. */
    val time: String,
)

/** A part of the workout with the exercises done in it, and the time spent in it ("8:00", empty when the events do not say). */
data class RecapPart(val key: String, val kind: PartKind, val name: String, val summary: String, val time: String, val rows: List<RecapRow>)

/**
 * What opening a finished session shows (`session-recap-*` mockups): the figures, then the exercises it went through
 * grouped by part. [parts] is empty when there is nothing to list (a free run, a session whose workout is gone), so
 * the screen keeps the figures alone. Decided here, from the session's event log, so Android and iOS draw the same recap.
 */
data class SessionRecap(
    val id: String,
    val title: String,
    /** "Sets", "Circuit", or null for a free run and sessions from before formats. */
    val formatLabel: String?,
    val startedAtMillis: Long,
    val kind: SessionKind,
    val tiles: List<RecapTile>,
    /** "Exercises", or "Steps" for a walk or a run. */
    val listTitle: String,
    val parts: List<RecapPart>,
)

/** Builds the recap of a finished session from what the phone kept: its events, its workout and its plan. */
internal class SessionRecaps(
    private val store: LocalStore,
    private val compiler: TimelineCompiler,
    private val catalog: Catalog,
    private val languageCode: String,
    private val units: () -> UnitSystem,
) {
    private val labels = Labels(languageCode, units)

    /** Null when the session is gone or has not finished. */
    fun recap(sessionId: String): SessionRecap? {
        val stored = store.finishedSession(sessionId) ?: return null
        val events = store.events(sessionId)
        val workout = stored.record.workout?.let { store.workout(it.workoutId, it.version) }
        val timeline = sessionTimeline(stored, workout, compiler)
        val totals = stored.totals ?: sessionTotals(events, timeline)
        val row = sessionRow(stored, totals, labels)
        val phases = timeline?.covering(events)?.phases.orEmpty()
        val done = events.mapNotNull { (it as? SessionEvent.PhaseCompleted)?.phaseIndex }.toSet()
        val skipped = events.mapNotNull { (it as? SessionEvent.PhaseSkipped)?.phaseIndex }.toSet()
        val machine = if (timeline != null && events.any { it is SessionEvent.Sample }) TreadmillSummary.of(timeline, events) else null
        val format = stored.format ?: workout?.format
        // A walk or a run is made of steps, not exercises.
        val walksOnly = workout?.steps?.flatMap { it.exerciseSteps() }?.let { steps -> steps.isNotEmpty() && steps.all { it.exercise.isWalkOrRun(catalog) } } == true
        val parts = workout?.let { listing(it, phases, done, skipped, machine, phaseMillis(events, phases), walksOnly) }.orEmpty()
        return SessionRecap(
            id = row.id,
            title = row.title,
            formatLabel = row.formatLabel,
            startedAtMillis = row.startedAtMillis,
            kind = row.kind,
            tiles = tiles(row, format, phases, done, skipped),
            listTitle = if (walksOnly) DomainText.recapListSteps(languageCode) else DomainText.recapListExercises(languageCode),
            parts = parts,
        )
    }

    private fun tiles(row: SessionRow, format: WorkoutFormat?, phases: List<Phase>, done: Set<Int>, skipped: Set<Int>) = buildList {
        val treadmill = row.kind == SessionKind.TREADMILL
        add(RecapTile(if (treadmill) DomainText.recapTileTime(languageCode) else DomainText.recapTileDuration(languageCode), ShareCard.clock(row.activeSeconds)))
        if (!treadmill && row.setsDone != null) {
            val rounds = if (format == WorkoutFormat.CIRCUIT) rounds(phases, done + skipped) else null
            if (rounds != null) {
                add(RecapTile(DomainText.recapTileRounds(languageCode), "${rounds.first} / ${rounds.second}"))
            } else {
                add(RecapTile(DomainText.recapTileSets(languageCode), row.setsPlanned?.let { "${row.setsDone} / $it" } ?: "${row.setsDone}"))
            }
            row.setsSkipped?.takeIf { it > 0 }?.let { add(RecapTile(DomainText.recapTileSkipped(languageCode), "$it")) }
        }
        row.distanceMeters?.let { add(RecapTile(DomainText.recapTileDistance(languageCode), ShareCard.measure(Units.distance(it, units(), 1), languageCode))) }
        row.averageSpeedKmh?.let { add(RecapTile(DomainText.recapTileSpeed(languageCode), ShareCard.measure(Units.speed(it, units()), languageCode))) }
        row.averageHeartRateBpm?.let { add(RecapTile(DomainText.recapTileHeartRate(languageCode), DomainText.recapBpm(languageCode, it))) }
        row.maxHeartRateBpm?.let { add(RecapTile(DomainText.recapTileMaxHeartRate(languageCode), DomainText.recapBpm(languageCode, it))) }
        row.energyKcal?.let { add(RecapTile(DomainText.recapTileEnergy(languageCode), DomainText.recapKcal(languageCode, it))) }
        row.estimatedSteps?.let { add(RecapTile(DomainText.recapTileSteps(languageCode), "≈ $it")) }
    }

    /** Rounds all of whose sets ended, out of the rounds the repeat runs; null when the plan repeats nothing. */
    private fun rounds(phases: List<Phase>, ended: Set<Int>): Pair<Int, Int>? {
        val work = phases.withIndex().mapNotNull { (index, phase) -> (phase as? Phase.Work)?.let { index to it } }
        val planned = work.maxOfOrNull { it.second.rounds } ?: return null
        if (planned < 2) return null
        val repeated = work.filter { it.second.rounds == planned }
        val finished = (1..planned).count { round -> repeated.filter { it.second.round == round }.let { set -> set.isNotEmpty() && set.all { it.first in ended } } }
        return finished to planned
    }

    private fun listing(
        workout: Workout,
        phases: List<Phase>,
        done: Set<Int>,
        skipped: Set<Int>,
        machine: TreadmillSummary?,
        millis: Map<Int, Long>,
        walksOnly: Boolean,
    ): List<RecapPart> {
        val severalWorkParts = workout.parts.count { it.kind == PartKind.WORK } > 1
        var workParts = 0
        return workout.parts.mapNotNull { part ->
            if (part.kind == PartKind.WORK) workParts++
            val steps = part.steps.flatMap { it.exerciseSteps() }.mapNotNull { step ->
                val own = phases.indices.filter { (phases[it] as? Phase.Work)?.step == step.id }
                val ended = own.filter { it in done || it in skipped }
                if (ended.isEmpty()) null else Triple(step, own, ended)
            }
            if (steps.isEmpty()) return@mapNotNull null
            val rows = steps.flatMap { (step, own, ended) ->
                if (step.exercise.isWalkOrRun(catalog)) walkRows(step, ended, phases, done, machine, millis) else listOf(row(step, own, phases, done, skipped, millis))
            }
            val strength = steps.filterNot { it.first.exercise.isWalkOrRun(catalog) }
            val summary = if (part.kind == PartKind.WORK && strength.isNotEmpty()) {
                DomainText.recapSetsDone(languageCode, strength.sumOf { (_, own, _) -> own.count { it in done } }, strength.sumOf { it.second.size })
            } else {
                null
            }
            RecapPart(
                key = part.id.value,
                kind = part.kind,
                name = labels.part(part.kind, workParts.takeIf { severalWorkParts && part.kind == PartKind.WORK }),
                summary = listOfNotNull(if (walksOnly) DomainText.countSteps(languageCode, steps.size) else labels.exercises(steps.size), summary).joinToString(" · "),
                time = clock(steps.flatMap { it.third }.mapNotNull { millis[it] }.takeIf { it.isNotEmpty() }?.sum()),
                rows = rows,
            )
        }
    }

    /** "1:30" for [millis] rounded to the second, so a phase tapped through shows "0:00"; empty when the events say nothing. */
    private fun clock(millis: Long?) = if (millis == null) "" else ShareCard.clock((millis + 500) / 1000)

    /**
     * Milliseconds spent per phase, without pauses: a phase runs from the end of the one before it to its own end. A rest
     * counts for the work it follows, so an exercise's time includes the rests between its sets.
     */
    private fun phaseMillis(events: List<SessionEvent>, phases: List<Phase>): Map<Int, Long> {
        val start = events.firstOrNull { it is SessionEvent.Started }?.at ?: return emptyMap()
        val end = events.lastOrNull { it is SessionEvent.Finished }?.at ?: events.maxOf { it.at }
        val pauses = TreadmillSummary.pauses(events, end)
        val ends = events.mapNotNull {
            when (it) {
                is SessionEvent.PhaseCompleted -> it.phaseIndex to it.at
                is SessionEvent.PhaseSkipped -> it.phaseIndex to it.at
                else -> null
            }
        }.toMap()
        val result = mutableMapOf<Int, Long>()
        var owner = -1
        for (index in phases.indices) {
            if (phases[index] is Phase.Work) owner = index
            val to = ends[index] ?: continue
            val from = if (index == 0) start else ends[index - 1] ?: continue
            if (owner < 0 || from > to) continue
            val paused = pauses.sumOf { (pausedAt, resumedAt) ->
                (minOf(to, resumedAt) - maxOf(from, pausedAt)).coerceAtLeast(Duration.ZERO).inWholeMilliseconds
            }
            result[owner] = (result[owner] ?: 0L) + ((to - from).inWholeMilliseconds - paused).coerceAtLeast(0L)
        }
        return result
    }

    private fun name(step: Step.ExerciseStep) = catalog[step.exercise]?.let { labels.text(it.name) } ?: step.exercise.value

    /** An exercise: the sets done as "3 × 10", its load, and what was skipped. */
    private fun row(step: Step.ExerciseStep, own: List<Int>, phases: List<Phase>, done: Set<Int>, skipped: Set<Int>, millis: Map<Int, Long>): RecapRow {
        val works = own.filter { it in done }.map { phases[it] as Phase.Work }
        val skips = own.count { it in skipped }
        val loads = works.mapNotNull { (step.prescription.sets.getOrNull(it.setIndex)?.load as? Load.Kilograms)?.kg }.distinct()
        val inRepeat = (phases[own.first()] as Phase.Work).rounds > 1
        return RecapRow(
            key = step.id.value,
            slug = step.exercise.value,
            name = name(step),
            pattern = catalog[step.exercise]?.movementPattern,
            detail = listOfNotNull(
                amount(works.map { it.target }),
                loads.singleOrNull()?.let(labels::weight),
                skips.takeIf { it > 0 }?.let { if (inRepeat) DomainText.recapSkippedRounds(languageCode, it) else DomainText.recapSkippedSets(languageCode, it) },
            ).joinToString(" · "),
            time = clock(own.filter { it in done || it in skipped }.mapNotNull { millis[it] }.takeIf { it.isNotEmpty() }?.sum()),
        )
    }

    /** "12 reps", "3 × 12", "2 × 40 s"; sets with different targets are listed one after the other. */
    private fun amount(targets: List<Target>): String? {
        val shown = targets.filter { it !is Target.Open }
        if (shown.isEmpty()) return null
        fun one(target: Target) = if (target is Target.Reps) "${target.count}" else labels.target(target)
        if (shown.size == 1) return labels.target(shown.single())
        return if (shown.distinct().size == 1) "${shown.size} × ${one(shown.first())}" else shown.joinToString(" · ", transform = ::one)
    }

    /** A walk or a run is listed block by block, with what the machine reported for each. */
    private fun walkRows(
        step: Step.ExerciseStep,
        ended: List<Int>,
        phases: List<Phase>,
        done: Set<Int>,
        machine: TreadmillSummary?,
        millis: Map<Int, Long>,
    ): List<RecapRow> =
        ended.mapIndexed { turn, index ->
            val result = machine?.phases?.firstOrNull { it.phaseIndex == index }
            val detail = if (index !in done) {
                DomainText.recapSkippedStep(languageCode)
            } else {
                listOfNotNull(
                    result?.distanceMeters?.let { ShareCard.measure(Units.distance(it, units(), 1), languageCode) },
                    result?.averageSpeedKmh?.let { ShareCard.measure(Units.speed(it, units()), languageCode) },
                    result?.averageHeartRateBpm?.let { DomainText.recapBpm(languageCode, it) },
                ).joinToString(" · ")
            }
            RecapRow("${step.id.value}-$turn", step.exercise.value, name(step), catalog[step.exercise]?.movementPattern, detail, clock(millis[index]))
        }
}
