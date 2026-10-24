package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class TimelineCompilerTest {
    private val compiler = TimelineCompiler(WorkoutTypeRegistry(listOf(TestModule)))

    @Test
    fun intervalGroupUnrollsRoundsAndDropsTrailingRest() {
        val workout = Workout(
            id = WorkoutId("w"),
            version = 1,
            name = "3 x (40 s on, 20 s off)",
            type = WorkoutType.CONDITIONING,
            steps = listOf(
                Step.Group(
                    id = StepId("g"),
                    kind = GroupKind.INTERVALS,
                    rounds = 3,
                    children = listOf(
                        Step.ExerciseStep(
                            id = StepId("jumping-jacks"),
                            exercise = ExerciseSlug("jumping-jack"),
                            prescription = Prescription(listOf(SetPrescription(Target.Time(40.seconds)))),
                        ),
                        Step.Rest(StepId("r"), 20.seconds),
                    ),
                ),
            ),
        )

        val phases = compiler.compile(workout).phases

        assertEquals(5, phases.size)
        assertEquals(listOf(40.seconds, 20.seconds, 40.seconds, 20.seconds, 40.seconds), phases.map { it.duration })
        assertEquals(listOf(1, 2, 3), phases.filterIsInstance<Phase.Work>().map { it.round })
    }
}
