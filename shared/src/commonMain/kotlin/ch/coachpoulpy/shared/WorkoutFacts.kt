package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.StoredSession
import kotlin.time.Duration
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

    /** Timed phases count for their length; a set of reps is guessed at 40 seconds. */
    fun minutes(workout: Workout): Int = minutes(phases(workout))

    /** "about 25 min", or "1 min" when every phase runs on a timer and the length is known. */
    fun length(workout: Workout): String {
        val phases = phases(workout)
        val minutes = minutes(phases)
        return if (phases.all { it.duration != null }) labels.duration(minutes * 60) else labels.aboutMinutes(minutes)
    }

    private fun phases(workout: Workout) = runCatching { compiler.compile(workout).phases }.getOrDefault(emptyList())

    private fun minutes(phases: List<Phase>): Int {
        val length = phases.fold(Duration.ZERO) { total, phase -> total + (phase.duration ?: MANUAL_SET) }
        return ((length.inWholeSeconds + 59) / 60).toInt()
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
 * A session that really happened: summed up with something done, or from before the
 * history (not summed up yet). Sessions closed because they were left behind with
 * nothing done do not count.
 */
internal val StoredSession.counts: Boolean get() = totals?.isEmpty != true

internal val StoredSession.realTotals: SessionTotals? get() = totals?.takeUnless { it.isEmpty }

internal fun LocalDate.startMillis(zone: TimeZone): Long = atStartOfDayIn(zone).toEpochMilliseconds()
