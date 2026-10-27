package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.StoredSession
import ch.coachpoulpy.types.treadmill.TreadmillModule
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** What Home, the library and the workout page say about a saved workout. */
internal class WorkoutFacts(private val compiler: TimelineCompiler, private val catalog: Catalog, private val labels: Labels) {

    /** "Circuit · 5 exercises · about 40 min". */
    fun details(workout: Workout): String =
        listOf(labels.format(workout.format), labels.exercises(exerciseSteps(workout).size), labels.aboutMinutes(minutes(workout)))
            .joinToString(" · ")

    /** The exercise names in order, for a workout still to do today. */
    fun exerciseNames(workout: Workout): String =
        exerciseSteps(workout).map { step -> catalog[step.exercise]?.let { labels.text(it.name) } ?: step.exercise.value }
            .distinct()
            .joinToString(", ")

    /** Timed phases count for their length, a distance at a pace for the time it takes; a set of reps is guessed at 40 seconds. */
    fun minutes(workout: Workout): Int = minutes(phases(workout))

    /** "about 25 min", or "1 min" when every phase runs on a timer and the length is known. */
    fun length(workout: Workout): String {
        val phases = phases(workout)
        val minutes = minutes(phases)
        return if (phases.all { it.duration != null }) labels.duration(minutes * 60) else labels.aboutMinutes(minutes)
    }

    /**
     * A part's header line, the same on the workout page and in the builder: "4 rounds · about
     * 14 min" for a work part that is one repeat, "4 exercises · about 25 min" for any other
     * work part, and "2 steps · about 6 min" for a warm-up or cool down. A part that ends the
     * workout ([endsWorkout]) with a step done until the button press says "until the end", and
     * an empty part only its count.
     */
    fun partSummary(part: Part, endsWorkout: Boolean): String {
        val alone = Workout(WorkoutId(""), 1, "", WorkoutFormat.SETS, listOf(part))
        val exercises = exerciseSteps(alone)
        val repeat = part.steps.singleOrNull() as? Step.Group
        val count = when {
            part.kind != PartKind.WORK -> labels.steps(exercises.size)
            repeat != null -> labels.rounds(repeat.rounds)
            else -> labels.exercises(exercises.size)
        }
        val last = part.steps.lastOrNull() as? Step.ExerciseStep
        val open = endsWorkout && last?.prescription?.sets?.lastOrNull()?.target == Target.Open
        return when {
            exercises.isEmpty() -> count
            open -> "$count · ${labels.until(last = true).lowercase()}"
            else -> "$count · ${length(alone)}"
        }
    }

    private fun phases(workout: Workout) = runCatching { compiler.compile(workout).phases }.getOrDefault(emptyList())

    private fun minutes(phases: List<Phase>): Int {
        val length = phases.fold(Duration.ZERO) { total, phase -> total + (phase.duration ?: guess(phase)) }
        return ((length.inWholeSeconds + 59) / 60).toInt()
    }

    /** A distance at a pace takes what the pace says; any other phase ended by a press, a set of reps. */
    private fun guess(phase: Phase): Duration {
        val meters = ((phase as? Phase.Work)?.target as? Target.Distance)?.meters
        val kmh = ((phase as? Phase.Work)?.intensity as? Intensity.Speed)?.kmh
        return if (meters != null && kmh != null && kmh > 0) (meters / 1000 / kmh).hours else MANUAL_SET
    }

    private companion object {
        val MANUAL_SET = 40.seconds
    }
}

/** Every exercise of a workout, groups opened up, in the order they are done. */
internal fun exerciseSteps(workout: Workout): List<Step.ExerciseStep> {
    fun open(steps: List<Step>): List<Step.ExerciseStep> = steps.flatMap { step ->
        when (step) {
            is Step.ExerciseStep -> listOf(step)
            is Step.Group -> open(step.children)
            is Step.Rest -> emptyList()
        }
    }
    return open(workout.steps)
}

/**
 * True when every exercise is a walk or a run, which the treadmill can follow from start
 * to end and a FIT export calls running.
 */
internal fun Workout.isWalksAndRuns(catalog: Catalog): Boolean {
    val steps = exerciseSteps(this)
    return steps.isNotEmpty() && steps.all { it.exercise == TreadmillModule.treadmillRun || catalog[it.exercise]?.isWalkOrRun == true }
}

/**
 * A session that really happened: summed up with something done, or from before the
 * history (not summed up yet). Sessions closed because they were left behind with
 * nothing done do not count.
 */
internal val StoredSession.counts: Boolean get() = totals?.isEmpty != true

internal val StoredSession.realTotals: SessionTotals? get() = totals?.takeUnless { it.isEmpty }

internal fun LocalDate.startMillis(zone: TimeZone): Long = atStartOfDayIn(zone).toEpochMilliseconds()
