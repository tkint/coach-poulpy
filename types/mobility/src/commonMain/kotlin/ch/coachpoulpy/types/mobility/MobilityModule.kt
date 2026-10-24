package ch.coachpoulpy.types.mobility

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/** Stretching and mobility: holds and timed drills run on a timer, slow reps (cat-cow) wait for a tap. */
object MobilityModule : WorkoutTypeModule {
    override val type = WorkoutType.MOBILITY
    override val acceptedTargets = setOf(Target.Hold::class, Target.Time::class, Target.Reps::class)

    override fun completionRule(set: SetPrescription): CompletionRule =
        if (set.target is Target.Reps) CompletionRule.Manual else CompletionRule.Timer
}
