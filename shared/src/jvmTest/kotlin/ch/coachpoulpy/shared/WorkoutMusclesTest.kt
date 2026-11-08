package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.MuscleGroup
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
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/** The muscles the workout page draws on the body map and lists under the exercises. */
class WorkoutMusclesTest {
    private val graph = AppGraph(DriverFactory())

    private fun exercise(id: String, slug: String, target: Target, sets: Int = 1) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(slug), Prescription(List(sets) { SetPrescription(target) }))

    /** A walk to warm up, two rounds of a push-up and a rest, a plank, then a walk. Only the work counts. */
    private fun circuit() = Workout(
        WorkoutId("muscles"), 1, "Circuit", WorkoutFormat.CIRCUIT,
        listOf(
            Part(PartId("warm-up"), PartKind.WARM_UP, listOf(exercise("walk-in", "walk", Target.Time(5.minutes)))),
            Part(
                PartId("work"), PartKind.WORK,
                listOf(
                    Step.Group(
                        StepId("repeat"), GroupKind.CIRCUIT, rounds = 2,
                        children = listOf(exercise("push-up", "push-up", Target.Reps(10)), Step.Rest(StepId("rest"), 30.seconds)),
                    ),
                    exercise("plank", "plank", Target.Hold(30.seconds)),
                ),
            ),
            Part(PartId("cool-down"), PartKind.COOL_DOWN, listOf(exercise("walk-out", "walk", Target.Time(1.minutes)))),
        ),
    )

    @Test
    fun heatStepsUpWithTheShareOfTheBusiestGroup() {
        assertEquals(listOf(1, 2, 3), listOf(2, 3, 6).map { WorkoutMuscles.heatOf(it, 6) })
        // The edges: 39 % is still 1, 40 % is 2, 74 % is 2, 75 % is 3.
        assertEquals(listOf(1, 2, 2, 3), listOf(39, 40, 74, 75).map { WorkoutMuscles.heatOf(it, 100) })
        assertEquals(0, WorkoutMuscles.heatOf(0, 4))
    }

    @Test
    fun theSetsOfTheWorkPartsDecideEachGroupsLoadBusiestFirst() {
        graph.store.saveWorkout(circuit())
        val page = WorkoutPageModel(WorkoutId("muscles"), graph.store, graph.compiler, graph.catalog, "en", scope = CoroutineScope(Dispatchers.Default))
        val state = runBlocking { withTimeout(5.seconds) { page.state.first { it.loaded } } }

        // Push-up works chest, triceps and shoulders over two rounds; the plank adds one set of core.
        assertEquals(
            listOf("Chest" to 2, "Shoulders" to 2, "Triceps" to 2, "Core" to 1),
            state.muscles.map { it.label to it.sets },
        )
        assertEquals(listOf(3, 3, 3, 2), state.muscles.map { it.heat })
        assertEquals(listOf(100, 100, 100, 50), state.muscles.map { it.percent })
        assertEquals("2 sets", state.muscles.first().setsLabel)
        assertEquals("1 set", state.muscles.last().setsLabel)

        // Both sides of a muscle are lit together, at its heat; the legs the walks use stay plain.
        assertEquals(3, state.heat["pectoral-left"])
        assertEquals(3, state.heat["pectoral-right"])
        assertEquals(2, state.heat["abs-left"])
        assertEquals(null, state.heat["quads-left"])
        page.close()
    }

    @Test
    fun aFullBodyExerciseLightsEveryMuscleButNotTheJoints() {
        val workout = Workout(
            WorkoutId("burpees"), 1, "Burpees", WorkoutFormat.SETS,
            listOf(Part(PartId("work"), PartKind.WORK, listOf(exercise("burpee", "burpee", Target.Reps(10), sets = 3)))),
        )
        val muscles = WorkoutMuscles.of(workout, graph.catalog, Labels("en"))

        assertEquals(listOf(MuscleGroup.FULL_BODY), muscles.loads.map { it.group })
        assertEquals(3, muscles.heat["quads-right"])
        assertEquals(null, muscles.heat["knee-right"])
    }

    @Test
    fun painInAWorkedMuscleIsNamedAndOneElsewhereIsNot() {
        graph.store.saveWorkout(circuit())
        fun page(pain: List<String>) = WorkoutPageModel(
            WorkoutId("muscles"), graph.store, graph.compiler, graph.catalog, "en",
            scope = CoroutineScope(Dispatchers.Default), painAreas = { pain },
        )
        fun WorkoutPageModel.loaded() = runBlocking { withTimeout(5.seconds) { state.first { it.loaded } } }

        val shoulder = page(listOf("deltoid-left")).loaded()
        assertEquals(listOf("deltoid-left"), shoulder.painAreas)
        assertEquals("Your left shoulder still hurts. Go gently, I'll keep an eye on it with you.", shoulder.painNote)

        // A hurt knee is not worked by this circuit, so the page stays quiet about it.
        val knee = page(listOf("knee-left")).loaded()
        assertTrue(knee.painAreas.isEmpty())
        assertEquals("", knee.painNote)
    }

    @Test
    fun theRowsOfTheWorkoutPageLeaveTheRestBetweenSetsOut() {
        val workout = Workout(
            WorkoutId("rests"), 1, "Rests", WorkoutFormat.SETS,
            listOf(
                Part(
                    PartId("work"), PartKind.WORK,
                    listOf(
                        Step.ExerciseStep(
                            StepId("push-up"), ExerciseSlug("push-up"),
                            Prescription(List(3) { SetPrescription(Target.Reps(12), restAfter = 90.seconds) }),
                        ),
                    ),
                ),
            ),
        )
        graph.store.saveWorkout(workout)
        val page = WorkoutPageModel(workout.id, graph.store, graph.compiler, graph.catalog, "en", scope = CoroutineScope(Dispatchers.Default))
        val state = runBlocking { withTimeout(5.seconds) { page.state.first { it.loaded } } }

        assertEquals("3 × 12", state.parts.single().steps.single().row!!.prescription)
        page.close()
    }
}
