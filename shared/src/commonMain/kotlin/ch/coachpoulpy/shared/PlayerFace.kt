package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Session
import ch.coachpoulpy.core.engine.SessionState
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import kotlin.time.Instant

/**
 * What the session screen shows for a [workout] at a moment of a [Session]. The guided player and Live, when it
 * runs a workout that mixes walks and runs with other exercises (#82), draw the same step from it.
 */
internal class PlayerFace(
    private val workout: Workout,
    private val catalog: Catalog,
    languageCode: String,
    units: () -> UnitSystem = { UnitSystem.METRIC },
) {
    private val labels = Labels(languageCode, units)
    private val steps = workout.steps.flatMap { it.exerciseSteps() }.associateBy { it.id }

    fun of(session: Session, now: Instant): PlayerState {
        val state = session.state(now)
        val phases = session.timeline.phases
        val phase = state.phase
        val work = phase as? Phase.Work
        val step = work?.let { steps[it.step] }
        val exercise = work?.let { catalog[it.exercise] }
        val next = phases.drop(state.phaseIndex + 1).firstOrNull { it is Phase.Work } as? Phase.Work
        // Where the user is: a rest belongs to the step before it, or to the first one when it opens the workout.
        val here = work ?: phases.take(state.phaseIndex).lastOrNull { it is Phase.Work } as? Phase.Work ?: next
        // "Set 2 of 3" only in a Sets workout outside a repeat: a circuit or a repeat counts rounds.
        val sets = work?.takeIf { workout.format == WorkoutFormat.SETS && it.rounds == 1 }?.let { w -> step?.let { it.setNumber(w.setIndex) to it.setCount() } }

        return PlayerState(
            status = when (state.status) {
                SessionState.Status.NOT_STARTED -> PlayerStatus.READY
                SessionState.Status.RUNNING -> PlayerStatus.RUNNING
                SessionState.Status.PAUSED -> PlayerStatus.PAUSED
                SessionState.Status.FINISHED -> PlayerStatus.FINISHED
            },
            workoutName = workout.name,
            isRest = phase is Phase.Rest,
            title = when {
                phase is Phase.Rest -> labels.rest
                exercise != null -> labels.text(exercise.name)
                else -> work?.exercise?.value ?: ""
            },
            exerciseSlug = work?.exercise?.value,
            pattern = exercise?.movementPattern,
            partKind = here?.part,
            partLabel = here?.let { labels.partLine(labels.part(it.part, it.workPart), it.round, it.rounds) },
            roleLabel = if (phase is Phase.Rest) labels.role(StepRole.RECOVER) else work?.let { labels.role(it.role) },
            setNumber = sets?.first ?: 0,
            setCount = sets?.second ?: 0,
            sideLabel = work?.let { labels.side(it.side) },
            targetLabel = work?.let { labels.target(it.target) },
            targetDetail = (work ?: here)?.let { detailOf(it) },
            inRepeat = here?.let { it.rounds > 1 } ?: false,
            targetValue = work?.let { (it.target as? Target.Reps)?.count?.toString() },
            targetUnit = work?.let { w -> (w.target as? Target.Reps)?.let { labels.repsUnit(it.count, loadOf(w)) } },
            loadLabel = work?.let { loadOf(it) },
            isTimed = phase?.duration != null,
            remainingSeconds = state.phaseRemaining?.let { ceilSeconds(it.inWholeMilliseconds) },
            totalSeconds = state.phaseDuration?.inWholeSeconds?.toInt(),
            elapsedSeconds = state.phaseElapsed.inWholeSeconds.toInt(),
            exerciseCountLabel = labels.exercises(steps.size),
            sessionClock = clockOf(activeTime(session.events, now).inWholeSeconds.toInt()),
            nextTitle = next?.let { nameOf(it) },
            nextHeadline = next?.let { n -> setsOf(n)?.let { (number, count) -> labels.nameSet(nameOf(n), number, count) } ?: nameOf(n) },
            nextDetail = next?.let { detailOf(it) },
            nextLine = nextLine(phases, state.phaseIndex + 1, work, here),
            nextRound = next?.takeIf { it.rounds > 1 && (here?.rounds != it.rounds || here?.round != it.round) }?.let { labels.roundOf(it.round, it.rounds) },
            phaseNumber = (state.phaseIndex + 1).coerceAtMost(phases.size),
            phaseCount = phases.size,
        )
    }

    private fun nameOf(work: Phase.Work) = catalog[work.exercise]?.let { labels.text(it.name) } ?: work.exercise.value

    private fun loadOf(work: Phase.Work) = steps[work.step]?.prescription?.sets?.getOrNull(work.setIndex)?.load?.label()

    /** "8 reps at 16 kg", or "8 reps" for a bodyweight set. */
    private fun detailOf(work: Phase.Work) = labels.target(work.target).let { target -> loadOf(work)?.let { labels.withLoad(target, it) } ?: target }

    /** The set number and count of [work] in a Sets workout outside a repeat; null elsewhere, where rounds are counted. */
    private fun setsOf(work: Phase.Work): Pair<Int, Int>? =
        work.takeIf { workout.format == WorkoutFormat.SETS && it.rounds == 1 }?.let { w -> steps[w.step]?.let { it.setNumber(w.setIndex) to it.setCount() } }

    /**
     * The line under "Next" while an exercise runs: a rest says how long and what follows it; an exercise names its
     * part when that changes, and otherwise what it asks for.
     */
    private fun nextLine(phases: List<Phase>, index: Int, current: Phase.Work?, here: Phase.Work?): String? = when (val after = phases.getOrNull(index)) {
        null -> null
        is Phase.Rest -> {
            val then = phases.drop(index + 1).firstOrNull { it is Phase.Work } as? Phase.Work
            then?.let { labels.restThen(after.duration.inWholeSeconds.toInt(), thenLabel(it, current)) }
        }
        is Phase.Work ->
            if (here != null && (after.part != here.part || after.workPart != here.workPart)) {
                labels.part(after.part, after.workPart) + " · " + nameOf(after)
            } else {
                nameOf(after) + " · " + detailOf(after)
            }
    }

    /** What follows a rest: the next set of the same exercise, or the next exercise. */
    private fun thenLabel(then: Phase.Work, current: Phase.Work?): String {
        val sets = setsOf(then)
        return if (current != null && then.step == current.step && sets != null) labels.thenSet(sets.first) else nameOf(then)
    }

    private fun clockOf(seconds: Int) = "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"

    private fun Load.label(): String? = when (this) {
        is Load.Kilograms -> labels.weight(kg)
        is Load.Band -> label
        is Load.Bodyweight, is Load.PercentOfOneRepMax -> null
    }

    private companion object {
        fun ceilSeconds(millis: Long): Int = ((millis + 999) / 1000).toInt()

        /** Left and right halves of a set count as one set. */
        fun Step.ExerciseStep.setNumber(index: Int): Int =
            prescription.sets.take(index + 1).count { it.side != Side.RIGHT }

        fun Step.ExerciseStep.setCount(): Int = prescription.sets.count { it.side != Side.RIGHT }
    }
}
