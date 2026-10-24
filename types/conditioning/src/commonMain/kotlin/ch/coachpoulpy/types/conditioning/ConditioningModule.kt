package ch.coachpoulpy.types.conditioning

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/** Conditioning formats (circuits, EMOM, Tabata): timed work moves on by itself, rep work waits for a tap. */
object ConditioningModule : WorkoutTypeModule {
    override val type = WorkoutType.CONDITIONING
    override val acceptedTargets = setOf(Target.Time::class, Target.Reps::class)

    override fun completionRule(set: SetPrescription): CompletionRule =
        if (set.target is Target.Time) CompletionRule.Timer else CompletionRule.Manual
}
