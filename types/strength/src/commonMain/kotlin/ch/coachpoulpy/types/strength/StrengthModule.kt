package ch.coachpoulpy.types.strength

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType

/** Strength and muscle building: the user taps done after each set, then the rest timer starts. */
object StrengthModule : WorkoutTypeModule {
    override val type = WorkoutType.STRENGTH
    override val acceptedTargets = setOf(Target.Reps::class)

    override fun completionRule(set: SetPrescription): CompletionRule = CompletionRule.Manual
}
