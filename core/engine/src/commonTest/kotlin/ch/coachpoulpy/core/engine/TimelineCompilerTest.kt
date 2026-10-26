package ch.coachpoulpy.core.engine

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class TimelineCompilerTest {
    private val compiler = TimelineCompiler()

    private fun exercise(id: String, vararg sets: SetPrescription) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(id), Prescription(sets.toList()))

    private fun workout(format: WorkoutFormat, vararg parts: Part) =
        Workout(WorkoutId("w"), 1, "Test", format, parts.toList())

    private fun part(kind: PartKind, vararg steps: Step) = Part(PartId(kind.name), kind, steps.toList())

    @Test
    fun intervalGroupUnrollsRoundsAndDropsTrailingRest() {
        val workout = workout(
            WorkoutFormat.CIRCUIT,
            part(
                PartKind.WORK,
                Step.Group(
                    id = StepId("g"),
                    kind = GroupKind.INTERVALS,
                    rounds = 3,
                    children = listOf(exercise("jumping-jack", SetPrescription(Target.Time(40.seconds))), Step.Rest(StepId("r"), 20.seconds)),
                ),
            ),
        )

        val phases = compiler.compile(workout).phases

        assertEquals(5, phases.size)
        assertEquals(listOf(40.seconds, 20.seconds, 40.seconds, 20.seconds, 40.seconds), phases.map { it.duration })
        assertEquals(listOf(1, 2, 3), phases.filterIsInstance<Phase.Work>().map { it.round })
    }

    @Test
    fun setsRestBetweenThemAndNotAfterTheLast() {
        val set = SetPrescription(Target.Reps(8), restAfter = 90.seconds)
        val workout = workout(WorkoutFormat.SETS, part(PartKind.WORK, exercise("bench-press", set, set, set), exercise("row", SetPrescription(Target.Reps(10)))))

        val phases = compiler.compile(workout).phases

        assertEquals(listOf(null, 90.seconds, null, 90.seconds, null, null), phases.map { it.duration })
        assertEquals(listOf(0, 1, 2), phases.filterIsInstance<Phase.Work>().filter { it.exercise.value == "bench-press" }.map { it.setIndex })
    }

    @Test
    fun theTargetDecidesHowAPhaseFinishes() {
        assertEquals(CompletionRule.Timer, CompletionRule.of(Target.Time(30.seconds)))
        assertEquals(CompletionRule.Timer, CompletionRule.of(Target.Hold(30.seconds)))
        assertEquals(CompletionRule.Manual, CompletionRule.of(Target.Reps(10)))
        assertEquals(CompletionRule.Manual, CompletionRule.of(Target.Distance(400.0)))
        assertEquals(CompletionRule.Manual, CompletionRule.of(Target.Open))
    }

    @Test
    fun phasesCarryTheirPartAndNumberWorkPartsOnlyWhenThereAreSeveral() {
        val one = SetPrescription(Target.Time(30.seconds))
        val several = workout(
            WorkoutFormat.CIRCUIT,
            part(PartKind.WARM_UP, exercise("jumping-jack", one)),
            part(PartKind.WORK, exercise("squat", one)),
            part(PartKind.WORK, exercise("plank", one)),
            part(PartKind.COOL_DOWN, exercise("stretch", one)),
        )
        val single = workout(WorkoutFormat.CIRCUIT, part(PartKind.WORK, exercise("squat", one)))

        val phases = compiler.compile(several).phases.filterIsInstance<Phase.Work>()

        assertEquals(listOf(PartKind.WARM_UP, PartKind.WORK, PartKind.WORK, PartKind.COOL_DOWN), phases.map { it.part })
        assertEquals(listOf(null, 1, 2, null), phases.map { it.workPart })
        assertEquals(null, (compiler.compile(single).phases.single() as Phase.Work).workPart)
    }
}
