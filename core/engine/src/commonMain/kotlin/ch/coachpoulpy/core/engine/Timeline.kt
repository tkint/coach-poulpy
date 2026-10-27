package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import kotlin.time.Duration

/**
 * A workout compiled into the flat list of phases the player runs one after another. An [openEnded]
 * timeline, a free recording, repeats its last phase for as many steps as the user ends.
 */
data class Timeline(val phases: List<Phase>, val openEnded: Boolean = false) {
    fun phaseAt(index: Int): Phase? = phases.getOrNull(index) ?: phases.lastOrNull()?.takeIf { openEnded && index >= 0 }

    /** The first [count] phases, an open-ended timeline grown to reach them. */
    fun grownTo(count: Int): Timeline =
        if (!openEnded || count <= phases.size) this else Timeline(List(count) { phaseAt(it)!! }, openEnded = true)
}

sealed interface Phase {
    /** Planned length, or null when the phase ends manually. */
    val duration: Duration?

    data class Work(
        val step: StepId,
        val exercise: ExerciseSlug,
        val setIndex: Int,
        val round: Int,
        val target: Target,
        val side: Side,
        val completion: CompletionRule,
        override val duration: Duration?,
        /** The prescribed effort, such as a treadmill speed, checked against what a connected machine reports. */
        val intensity: Intensity? = null,
        val role: StepRole = StepRole.WORK,
        /** How many rounds the repeat holding this step runs; 1 outside a repeat. */
        val rounds: Int = 1,
        /** The kind of part holding this step. */
        val part: PartKind = PartKind.WORK,
        /** Which work part holds this step ("Work 2"), when the workout has several; null otherwise. */
        val workPart: Int? = null,
    ) : Phase

    data class Rest(override val duration: Duration) : Phase
}

/** Unrolls the parts and their step trees (groups, rounds, sets and rests) into a [Timeline]. */
class TimelineCompiler {

    fun compile(workout: Workout): Timeline {
        val phases = mutableListOf<Phase>()
        val severalWorkParts = workout.parts.count { it.kind == PartKind.WORK } > 1
        var workParts = 0
        workout.parts.forEach { part ->
            if (part.kind == PartKind.WORK) workParts++
            val place = Place(part.kind, workParts.takeIf { severalWorkParts && part.kind == PartKind.WORK })
            part.steps.forEach { phases.addStep(it, place, round = 1, rounds = 1) }
        }
        return Timeline(phases.dropLastWhile { it is Phase.Rest })
    }

    /** The part a step belongs to, as its phases carry it. */
    private data class Place(val kind: PartKind, val workPart: Int?)

    private fun MutableList<Phase>.addStep(step: Step, place: Place, round: Int, rounds: Int) {
        when (step) {
            is Step.Rest -> addRest(step.duration)
            is Step.ExerciseStep -> step.prescription.sets.forEachIndexed { index, set ->
                val completion = CompletionRule.of(set.target)
                add(
                    Phase.Work(
                        step = step.id,
                        exercise = step.exercise,
                        setIndex = index,
                        round = round,
                        target = set.target,
                        side = set.side,
                        completion = completion,
                        duration = if (completion == CompletionRule.Timer) set.target.duration() else null,
                        intensity = set.intensity,
                        role = step.role,
                        rounds = rounds,
                        part = place.kind,
                        workPart = place.workPart,
                    ),
                )
                if (index < step.prescription.sets.lastIndex) addRest(set.restAfter)
            }
            is Step.Group -> for (r in 1..step.rounds) {
                step.children.forEach { addStep(it, place, round = r, rounds = step.rounds) }
                if (r < step.rounds) addRest(step.restBetweenRounds)
            }
        }
    }

    private fun MutableList<Phase>.addRest(duration: Duration) {
        if (duration > Duration.ZERO) add(Phase.Rest(duration))
    }

    private fun Target.duration(): Duration? = when (this) {
        is Target.Time -> duration
        is Target.Hold -> duration
        is Target.Reps, is Target.Distance, Target.Open -> null
    }
}
