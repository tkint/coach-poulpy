package ch.coachpoulpy.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class WorkoutTest {
    private fun exercise(id: String, sets: Int = 1) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(id), Prescription(List(sets) { SetPrescription(Target.Reps(10), restAfter = 60.seconds) }))

    private fun part(kind: PartKind, vararg steps: Step) = Part(PartId(kind.name), kind, steps.toList())

    private fun workout(format: WorkoutFormat, vararg parts: Part) = Workout(WorkoutId("w"), 1, "Test", format, parts.toList())

    @Test
    fun aWarmUpFirstWorkPartsAndACoolDownLastAreValid() {
        val workout = workout(
            WorkoutFormat.CIRCUIT,
            part(PartKind.WARM_UP, exercise("jumping-jack")),
            part(PartKind.WORK, exercise("squat")),
            part(PartKind.WORK, exercise("plank")),
            part(PartKind.COOL_DOWN, exercise("stretch")),
        )

        assertEquals(emptySet(), workout.problems())
        assertEquals(listOf("jumping-jack", "squat", "plank", "stretch"), workout.steps.map { (it as Step.ExerciseStep).exercise.value })
    }

    @Test
    fun partsOutOfOrderOrNoWorkPartAreProblems() {
        assertEquals(setOf(WorkoutProblem.NO_WORK_PART), workout(WorkoutFormat.SETS, part(PartKind.WARM_UP, exercise("walk"))).problems())
        assertEquals(
            setOf(WorkoutProblem.WARM_UP_NOT_FIRST, WorkoutProblem.COOL_DOWN_NOT_LAST),
            workout(WorkoutFormat.SETS, part(PartKind.WORK), part(PartKind.WARM_UP), part(PartKind.COOL_DOWN), part(PartKind.WORK)).problems(),
        )
        assertEquals(
            setOf(WorkoutProblem.WARM_UP_NOT_FIRST),
            workout(WorkoutFormat.SETS, part(PartKind.WARM_UP), part(PartKind.WARM_UP), part(PartKind.WORK)).problems(),
        )
    }

    @Test
    fun aCircuitRefusesAnExerciseWithSeveralSetsAndSetsAcceptsIt() {
        assertEquals(setOf(WorkoutProblem.SETS_IN_CIRCUIT), workout(WorkoutFormat.CIRCUIT, part(PartKind.WORK, exercise("squat", sets = 3))).problems())
        assertEquals(emptySet(), workout(WorkoutFormat.SETS, part(PartKind.WORK, exercise("squat", sets = 3))).problems())
    }

    @Test
    fun aCircuitTakesAnExerciseDoneOneSideAtATime() {
        val lunge = Step.ExerciseStep(
            StepId("lunge"),
            ExerciseSlug("lunge"),
            Prescription(listOf(SetPrescription(Target.Reps(10), side = Side.LEFT), SetPrescription(Target.Reps(10), side = Side.RIGHT))),
        )

        assertEquals(emptySet(), workout(WorkoutFormat.CIRCUIT, part(PartKind.WORK, lunge)).problems())
    }

    @Test
    fun repeatsStayOneLevelDeep() {
        val inner = Step.Group(StepId("inner"), GroupKind.CIRCUIT, 2, listOf(exercise("squat")))
        val outer = Step.Group(StepId("outer"), GroupKind.CIRCUIT, 3, listOf(inner))

        assertEquals(setOf(WorkoutProblem.NESTED_REPEAT), workout(WorkoutFormat.CIRCUIT, part(PartKind.WORK, outer)).problems())
    }

    @Test
    fun aWorkoutWithPartsRoundTripsThroughJson() {
        val workout = workout(
            WorkoutFormat.SETS,
            part(PartKind.WARM_UP, exercise("walk")),
            part(PartKind.WORK, exercise("bench-press", sets = 3), Step.Rest(StepId("rest"), 90.seconds)),
            part(PartKind.COOL_DOWN, exercise("stretch")),
        )

        val json = Json.encodeToString(Workout.serializer(), workout)

        assertEquals(workout, Json.decodeFromString(Workout.serializer(), json))
    }
}
