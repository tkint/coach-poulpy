package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
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
        builder.changeRest(key, -2)
        assertEquals("2 × 12 · 12 kg · rest 30 s", builder.state.value.rows.single().summary)

        val id = assertNotNull(builder.save())
        val workout = assertNotNull(graph.store.latestWorkout(WorkoutId(id)))
        val phases = graph.compiler.compile(workout).phases

        // 2 sets of 12 reps with 30 s rest between them; the trailing rest is dropped.
        assertEquals(3, phases.size)
        assertEquals(Target.Reps(12), (phases[0] as Phase.Work).target)
        assertEquals(30.seconds, phases[1].duration)
    }

    @Test
    fun perSideTimedExerciseGetsLeftAndRightWithTimeToSwitch() {
        val builder = graph.createWorkout("en")
        builder.rename("Stretch")
        builder.setType(WorkoutType.MOBILITY)
        builder.add("hamstring-stretch")
        val key = builder.state.value.rows.single().key
        builder.setSets(key, 1)
        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!

        val phases = graph.compiler.compile(workout).phases
        assertEquals(listOf(Side.LEFT, Side.RIGHT), phases.filterIsInstance<Phase.Work>().map { it.side })
        assertEquals(listOf(30.seconds, 5.seconds, 30.seconds), phases.map { it.duration })
    }

    @Test
    fun exerciseTheTypeCannotRunBlocksSaving() {
        val builder = graph.createWorkout("en")
        builder.rename("Mixed")
        builder.add("jumping-jack")
        builder.setType(WorkoutType.REHAB)
        assertFalse(builder.state.value.rows.single().fitsType)
        assertFalse(builder.state.value.canSave)
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
    fun aWalkIsOneTwentyMinuteBlockNotSetsOfShortReps() {
        val builder = graph.createWorkout("en")
        builder.rename("Walk")
        builder.setType(WorkoutType.TREADMILL)
        builder.add("walk")
        val row = builder.state.value.rows.single()
        assertEquals(TargetKind.DURATION, row.target?.kind)
        assertEquals("20 min", row.summary)
        assertTrue(row.fitsType)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val phases = graph.compiler.compile(workout).phases
        assertEquals(1, phases.size)
        assertEquals(Target.Time(1200.seconds), (phases.single() as Phase.Work).target)
    }

    @Test
    fun distanceAndPaceTargetsSaveAndComeBack() {
        val builder = graph.createWorkout("en")
        builder.rename("Long run")
        builder.setType(WorkoutType.TREADMILL)
        builder.add("run")
        val key = builder.state.value.rows.single().key

        builder.setTarget(key, TargetKind.DISTANCE_PACE)
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
        assertEquals(TargetKind.DISTANCE_PACE, target.kind)
        assertEquals("10.2", target.distanceText)
        assertEquals("5:00", target.paceText)
    }

    @Test
    fun switchingUnitsRewritesTheValue() {
        val builder = graph.createWorkout("en")
        builder.add("brisk-walk")
        val key = builder.state.value.rows.single().key
        builder.setTarget(key, TargetKind.DURATION_PACE)
        assertEquals("10:00", builder.state.value.rows.single().target!!.paceText)
        assertEquals("6.0", builder.setPaceUnit(key, PaceUnit.KMH, "10:00"))
        assertEquals("6.0 km/h", builder.state.value.rows.single().target!!.paceLabel)

        builder.setTarget(key, TargetKind.DISTANCE)
        assertNull(builder.state.value.rows.single().target!!.speedKmh)
        assertEquals("2000", builder.setDistanceUnit(key, DistanceUnit.M, "2"))
        assertEquals("1.0", builder.setDistanceUnit(key, DistanceUnit.MI, "1609"))
    }

    @Test
    fun walksFitRehabAndCardioButNotAtADistanceInStrength() {
        val builder = graph.createWorkout("en")
        builder.rename("Back on my feet")
        builder.setType(WorkoutType.REHAB)
        builder.add("walk-with-support")
        builder.add("calf-raise")
        assertTrue(builder.state.value.canSave)

        val key = builder.state.value.rows.first().key
        builder.setTarget(key, TargetKind.DISTANCE)
        builder.setType(WorkoutType.STRENGTH)
        assertFalse(builder.state.value.rows.first().fitsType)
        builder.setType(WorkoutType.CONDITIONING)
        assertTrue(builder.state.value.rows.first().fitsType)
    }

    @Test
    fun valuePicksGoUpToTheMarathon() {
        val builder = graph.createWorkout("fr")
        assertEquals(listOf("10 min", "20 min", "30 min", "45 min", "1 h"), builder.durationPicks().map { it.label })
        assertEquals("Semi-marathon", builder.distancePicks(DistanceUnit.KM)[3].label)
        assertEquals(42_195.0, TargetInput.parseDistance(builder.distancePicks(DistanceUnit.MI).last().text, DistanceUnit.MI)!!, 1.0)
    }
}
