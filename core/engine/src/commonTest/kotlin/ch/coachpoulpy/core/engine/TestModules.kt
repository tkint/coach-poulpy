package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/** Minimal module for engine tests: reps are manual, everything else is timed. */
object TestModule : WorkoutTypeModule {
    override val type = WorkoutType.CONDITIONING
    override val acceptedTargets = setOf(Target.Reps::class, Target.Time::class)
    override fun completionRule(set: SetPrescription) =
        if (set.target is Target.Reps) CompletionRule.Manual else CompletionRule.Timer
}
