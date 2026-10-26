package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Intensity
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
    ) : Phase

    data class Rest(override val duration: Duration) : Phase
}

/** Unrolls the step tree (groups, rounds, sets and rests) into a [Timeline]. */
class TimelineCompiler(private val registry: WorkoutTypeRegistry) {

    fun compile(workout: Workout): Timeline {
        val module = registry[workout.type]
        val phases = mutableListOf<Phase>()
        workout.steps.forEach { phases.addStep(it, module, round = 1, rounds = 1) }
        return Timeline(phases.dropLastWhile { it is Phase.Rest })
    }

    private fun MutableList<Phase>.addStep(step: Step, module: WorkoutTypeModule, round: Int, rounds: Int) {
        when (step) {
            is Step.Rest -> addRest(step.duration)
            is Step.ExerciseStep -> step.prescription.sets.forEachIndexed { index, set ->
                val completion = module.completionRule(set)
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
                    ),
                )
                addRest(set.restAfter)
            }
            is Step.Group -> for (r in 1..step.rounds) {
                step.children.forEach { addStep(it, module, round = r, rounds = step.rounds) }
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
