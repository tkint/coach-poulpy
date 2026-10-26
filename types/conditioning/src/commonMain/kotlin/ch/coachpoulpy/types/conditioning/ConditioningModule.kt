package ch.coachpoulpy.types.conditioning

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/**
 * Conditioning formats (circuits, EMOM, Tabata): timed work and holds move on by themselves,
 * rep work waits for a tap, and so does a run to a distance, which the builder may add.
 */
object ConditioningModule : WorkoutTypeModule {
    override val type = WorkoutType.CONDITIONING
    override val acceptedTargets = setOf(Target.Time::class, Target.Hold::class, Target.Reps::class, Target.Open::class)

    override fun completionRule(set: SetPrescription): CompletionRule =
        if (set.target is Target.Reps || set.target is Target.Distance || set.target is Target.Open) CompletionRule.Manual else CompletionRule.Timer
}
