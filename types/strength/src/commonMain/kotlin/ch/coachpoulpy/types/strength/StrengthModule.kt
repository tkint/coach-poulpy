package ch.coachpoulpy.types.strength

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/**
 * Strength and muscle building: the user taps done after each set of reps, then the rest
 * timer starts. Holds and timed sets (a plank, a carry) run on a timer.
 */
object StrengthModule : WorkoutTypeModule {
    override val type = WorkoutType.STRENGTH
    override val acceptedTargets = setOf(Target.Reps::class, Target.Hold::class, Target.Time::class, Target.Open::class)

    override fun completionRule(set: SetPrescription): CompletionRule =
        if (set.target is Target.Reps || set.target is Target.Open) CompletionRule.Manual else CompletionRule.Timer
}
