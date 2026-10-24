package ch.coachpoulpy.types.rehab

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/** Rehabilitation: reps are confirmed by hand, holds run on a timer, per side. */
object RehabModule : WorkoutTypeModule {
    override val type = WorkoutType.REHAB
    override val acceptedTargets = setOf(Target.Reps::class, Target.Hold::class)

    override fun completionRule(set: SetPrescription): CompletionRule =
        if (set.target is Target.Hold) CompletionRule.Timer else CompletionRule.Manual
}
