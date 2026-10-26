package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.Target

/** How a work phase finishes during a session. */
sealed interface CompletionRule {
    /** The user taps "done". */
    data object Manual : CompletionRule

    /** The phase ends on its own when its duration has elapsed. */
    data object Timer : CompletionRule

    companion object {
        /** A time or a hold runs a timer; reps, a distance or an open target wait for a tap. */
        fun of(target: Target): CompletionRule = when (target) {
            is Target.Time, is Target.Hold -> Timer
            is Target.Reps, is Target.Distance, Target.Open -> Manual
        }
    }
}
