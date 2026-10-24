package ch.coachpoulpy.types.mobility

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/** Stretching and mobility: every hold runs on a timer. */
object MobilityModule : WorkoutTypeModule {
    override val type = WorkoutType.MOBILITY
    override val acceptedTargets = setOf(Target.Hold::class, Target.Time::class)

    override fun completionRule(set: SetPrescription): CompletionRule = CompletionRule.Timer
}
