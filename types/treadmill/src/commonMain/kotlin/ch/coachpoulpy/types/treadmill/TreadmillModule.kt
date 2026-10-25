package ch.coachpoulpy.types.treadmill

import ch.coachpoulpy.core.engine.CompletionRule
import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.WorkoutTypeModule
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutType
import kotlin.time.Duration

/**
 * Treadmill runs and walks. Timed blocks end on the clock; distance blocks end when
 * the connected treadmill reports the distance done (see [TreadmillRecorder]), or
 * with a tap when no treadmill is connected.
 */
object TreadmillModule : WorkoutTypeModule {
    override val type = WorkoutType.TREADMILL
    override val acceptedTargets = setOf(Target.Time::class, Target.Distance::class)

    override fun completionRule(set: SetPrescription): CompletionRule =
        if (set.target is Target.Time) CompletionRule.Timer else CompletionRule.Manual

    /** The exercise a treadmill block refers to. */
    val treadmillRun = ExerciseSlug("treadmill-run")

    /** A recording without a planned workout: one open phase on [exercise] that ends when the user stops. */
    fun freeRecording(exercise: ExerciseSlug) = Timeline(
        listOf(
            Phase.Work(
                step = StepId("free-run"),
                exercise = exercise,
                setIndex = 0,
                round = 1,
                target = Target.Time(Duration.INFINITE),
                side = Side.BOTH,
                completion = CompletionRule.Manual,
                duration = null,
            ),
        ),
    )

    /** The free recording from before the exercise could be picked, for sessions recorded then. */
    val freeRun = freeRecording(treadmillRun)
}
