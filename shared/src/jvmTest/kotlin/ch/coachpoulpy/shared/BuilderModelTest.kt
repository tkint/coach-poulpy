package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class BuilderModelTest {
    private val graph = AppGraph(DriverFactory())

    @Test
    fun savedWorkoutCompilesToTheSetsAndRestsChosen() {
        val builder = graph.createWorkout("en")
        assertNull(builder.save(), "nothing to save yet")

        builder.rename("Monday")
        builder.add("goblet-squat")
        val key = builder.state.value.rows.single().key
        builder.setSets(key, 2)
        builder.changeAmount(key, 2)
        builder.setLoadKg(key, 12.0)
        assertEquals("2 × 12 · 12 kg", builder.state.value.rows.single().summary)
        builder.addRest()
        builder.changeRest(builder.state.value.addedKey!!, -2)
        builder.add("push-up")

        val id = assertNotNull(builder.save())
        val workout = assertNotNull(graph.store.latestWorkout(WorkoutId(id)))
        val phases = graph.compiler.compile(workout).phases

        // 2 sets of 12 reps one after the other, the 30 s rest step, then the push-ups.
        assertEquals(listOf(true, true, false), phases.take(3).map { it is Phase.Work })
        assertEquals(Target.Reps(12), (phases[0] as Phase.Work).target)
        assertEquals(30.seconds, phases[2].duration)
    }

    @Test
    fun perSideTimedExerciseGetsLeftAndRightWithTimeToSwitch() {
        val builder = graph.createWorkout("en")
        builder.rename("Stretch")
        builder.add("hamstring-stretch")
        assertEquals(1, builder.state.value.rows.single().sets, "a stretch starts at one set")
        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!

        val phases = graph.compiler.compile(workout).phases
        assertEquals(listOf(Side.LEFT, Side.RIGHT), phases.filterIsInstance<Phase.Work>().map { it.side })
        assertEquals(listOf(30.seconds, 5.seconds, 30.seconds), phases.map { it.duration })
    }

    @Test
    fun severalSetsInACircuitBlockSaving() {
        val builder = graph.createWorkout("en")
        builder.rename("Mixed")
        builder.add("jumping-jack")
        assertEquals(3, builder.state.value.rows.single().sets)
        builder.setFormat(WorkoutFormat.CIRCUIT)
        assertFalse(builder.state.value.rows.single().fitsFormat)
        assertFalse(builder.state.value.canSave)

        builder.setSets(builder.state.value.rows.single().key, 1)
        assertTrue(builder.state.value.canSave)
        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        assertEquals(WorkoutFormat.CIRCUIT, workout.format)
    }

    @Test
    fun aCircuitStartsExercisesAtOneSetAndSetsAtThree() {
        val circuit = graph.createWorkout("en")
        circuit.setFormat(WorkoutFormat.CIRCUIT)
        circuit.add("push-up")
        assertEquals(1, circuit.state.value.rows.single().sets)

        val sets = graph.createWorkout("en")
        sets.add("push-up")
        assertEquals(3, sets.state.value.rows.single().sets)
    }

    @Test
    fun editingSavesANewVersionWithTheSameExercises() {
        val builder = graph.createWorkout("en")
        builder.rename("Legs")
        builder.add("bodyweight-squat")
        builder.add("reverse-lunge")
        val id = builder.save()!!

        val edit = assertNotNull(graph.editWorkout(id, "en"))
        assertTrue(edit.isEditing)
        assertEquals(listOf("Bodyweight squat", "Reverse lunge"), edit.state.value.rows.map { it.name })
        edit.moveUp(edit.state.value.rows[1].key)
        edit.save()

        val latest = graph.store.latestWorkout(WorkoutId(id))!!
        assertEquals(2, latest.version)
        assertEquals(listOf("Reverse lunge", "Bodyweight squat"), graph.editWorkout(id, "en")!!.state.value.rows.map { it.name })
        assertEquals(3, edit.state.value.rows.single { it.name == "Reverse lunge" }.sets)
    }

    @Test
    fun addToAWorkoutStartsANewOneHoldingTheExercise() {
        val builder = graph.createWorkoutWith("diamond-push-up", "en")
        assertFalse(builder.isEditing)
        assertEquals(listOf("Diamond push-up"), builder.state.value.rows.map { it.name })
    }

    @Test
    fun aWalkIsOneTwoKilometreBlockNotSetsOfShortReps() {
        val builder = graph.createWorkout("en")
        builder.rename("Walk")
        builder.setFormat(WorkoutFormat.CIRCUIT)
        builder.add("walk")
        val row = builder.state.value.rows.single()
        assertEquals(DurationKind.DISTANCE, row.target?.duration)
        assertEquals(IntensityKind.FREE, row.target?.intensity)
        assertEquals("2.0 km", row.summary)
        assertTrue(row.fitsFormat)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val phases = graph.compiler.compile(workout).phases
        assertEquals(1, phases.size)
        assertEquals(Target.Distance(2000.0), (phases.single() as Phase.Work).target)
    }

    @Test
    fun distanceAndPaceTargetsSaveAndComeBack() {
        val builder = graph.createWorkout("en")
        builder.rename("Long run")
        builder.setFormat(WorkoutFormat.CIRCUIT)
        builder.add("run")
        val key = builder.state.value.rows.single().key

        builder.setDurationKind(key, DurationKind.DISTANCE)
        builder.setIntensityKind(key, IntensityKind.PACE)
        var target = builder.state.value.rows.single().target!!
        assertEquals("5.0 km", target.distanceLabel)
        assertEquals("6:00 /km", target.paceLabel)
        assertEquals("10.0 km/h · about 30 min", target.hint)

        assertTrue(builder.setDistance(key, "10", DistanceUnit.KM))
        assertTrue(builder.setPace(key, "5:30", PaceUnit.PER_KM))
        assertFalse(builder.setPace(key, "fast", PaceUnit.PER_KM))
        builder.changeDistance(key, 2)
        builder.changePace(key, -6)
        assertEquals("10.2 km · 5:00 /km", builder.state.value.rows.single().summary)

        val id = builder.save()!!
        val step = graph.store.latestWorkout(WorkoutId(id))!!.steps.single() as ch.coachpoulpy.core.model.Step.ExerciseStep
        val set = step.prescription.sets.single()
        assertEquals(Target.Distance(10_200.0), set.target)
        assertEquals(12.0, (set.intensity as ch.coachpoulpy.core.model.Intensity.Speed).kmh, 0.001)

        target = graph.editWorkout(id, "en")!!.state.value.rows.single().target!!
        assertEquals(DurationKind.DISTANCE, target.duration)
        assertEquals(IntensityKind.PACE, target.intensity)
        assertEquals("10.2", target.distanceText)
        assertEquals("5:00", target.paceText)
    }

    @Test
    fun switchingUnitsRewritesTheValue() {
        val builder = graph.createWorkout("en")
        builder.add("brisk-walk")
        val key = builder.state.value.rows.single().key
        builder.setIntensityKind(key, IntensityKind.PACE)
        assertEquals("10:00", builder.state.value.rows.single().target!!.paceText)
        assertEquals("6.0", builder.setPaceUnit(key, PaceUnit.KMH, "10:00"))
        assertEquals("6.0 km/h", builder.state.value.rows.single().target!!.paceLabel)

        builder.setDurationKind(key, DurationKind.DISTANCE)
        builder.setIntensityKind(key, IntensityKind.FREE)
        assertNull(builder.state.value.rows.single().target!!.speedKmh)
        assertEquals("2000", builder.setDistanceUnit(key, DistanceUnit.M, "2"))
        assertEquals("1.0", builder.setDistanceUnit(key, DistanceUnit.MI, "1609"))
    }

    @Test
    fun valuePicksGoUpToTheMarathon() {
        val builder = graph.createWorkout("fr")
        assertEquals(listOf("10 min", "20 min", "30 min", "45 min", "1 h"), builder.durationPicks().map { it.label })
        assertEquals("Semi-marathon", builder.distancePicks(DistanceUnit.KM)[3].label)
        assertEquals(42_195.0, TargetInput.parseDistance(builder.distancePicks(DistanceUnit.MI).last().text, DistanceUnit.MI)!!, 1.0)
    }

    @Test
    fun aDraggedExerciseLandsWhereItIsDropped() {
        val builder = graph.createWorkout("en")
        listOf("goblet-squat", "push-up", "plank").forEach(builder::add)
        fun order() = builder.state.value.rows.map { it.key }
        val (squat, pushUp, plank) = builder.state.value.rows.map { it.key }

        builder.move(squat, 2)
        assertEquals(listOf(pushUp, plank, squat), order())
        builder.move(squat, -5)
        assertEquals(listOf(squat, pushUp, plank), order())
        builder.move(plank, 99)
        assertEquals(listOf(squat, pushUp, plank), order())
        builder.moveUp(squat)
        builder.moveDown(plank)
        assertEquals(listOf(squat, pushUp, plank), order())
        builder.moveDown(squat)
        assertEquals(listOf(pushUp, squat, plank), order())
    }
}
