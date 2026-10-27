package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
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

/** The first work part, where most tests add their steps. */
internal val BuilderModel.work: String get() = current.parts.first { it.kind == PartKind.WORK }.key

/** The part of [kind]; the last one for work. */
internal fun BuilderModel.part(kind: PartKind): String = current.parts.last { it.kind == kind }.key

class BuilderModelTest {
    private val graph = AppGraph(DriverFactory())

    private fun builder(format: WorkoutFormat, name: String = "Monday") = graph.createWorkout("en").apply {
        rename(name)
        chooseFormat(format)
    }

    @Test
    fun aNewWorkoutAsksForItsFormatOnce() {
        val builder = graph.createWorkout("en")
        builder.rename("Monday")
        assertNull(builder.current.format)
        assertEquals(listOf(PartKind.WORK), builder.current.parts.map { it.kind })
        builder.add("push-up", builder.work)
        assertTrue(builder.current.rows.isEmpty(), "nothing is added before the format")
        assertFalse(builder.current.canSave)

        builder.chooseFormat(WorkoutFormat.CIRCUIT)
        builder.chooseFormat(WorkoutFormat.SETS)
        assertEquals(WorkoutFormat.CIRCUIT, builder.current.format)
        assertEquals("Circuit", builder.current.formatLabel)
        val repeat = assertNotNull(builder.current.parts.single().steps.single().repeatGroup, "a circuit's work starts as a repeat")
        assertEquals(3, repeat.times)

        builder.add("push-up", builder.work, repeat.key)
        val id = assertNotNull(builder.save())
        val edit = assertNotNull(graph.editWorkout(id, "en"))
        assertEquals(WorkoutFormat.CIRCUIT, edit.current.format, "editing never asks again")
    }

    @Test
    fun addToAWorkoutAddsTheExerciseOnceTheFormatIsChosen() {
        val builder = graph.createWorkoutWith("diamond-push-up", "en")
        assertFalse(builder.isEditing)
        assertTrue(builder.current.rows.isEmpty())

        builder.chooseFormat(WorkoutFormat.CIRCUIT)
        val repeat = builder.current.parts.single().steps.single().repeatGroup!!
        assertEquals(listOf("Diamond push-up"), repeat.rows.map { it.name })
    }

    @Test
    fun aCircuitHasNoSetsTheRepeatSetsTheRounds() {
        val builder = builder(WorkoutFormat.CIRCUIT)
        val repeat = builder.current.parts.single().steps.single().key
        builder.add("goblet-squat", builder.work, repeat)
        val key = builder.current.addedKey!!
        builder.setRepeatTimes(repeat, 4)
        builder.setSets(key, 3)
        builder.changeAmount(key, 5)
        builder.setLoadKg(key, 16.0)

        val row = builder.current.rows.single()
        assertEquals(1, row.sets, "a circuit refuses sets")
        assertFalse(row.showsSets)
        assertEquals("15 reps · 16 kg", row.summary)
        assertEquals("In Work · once per round, 4 rounds", row.place)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        assertTrue(workout.problems().isEmpty())
        assertEquals(4, graph.compiler.compile(workout).phases.count { it is Phase.Work })
    }

    @Test
    fun setsStartAtThreeAndAtOneInASupersetOrAStretch() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("dumbbell-bench-press", builder.work)
        builder.add("hamstring-stretch", builder.work)
        builder.addRepeat(builder.work)
        val superset = builder.current.parts.single().steps.last().key
        builder.add("biceps-curl", builder.work, superset)

        val (press, stretch, curl) = builder.current.rows
        assertEquals(listOf(3, 1, 1), listOf(press.sets, stretch.sets, curl.sets))
        assertTrue(press.showsSets)
        assertEquals("In Work · 3 sets", press.place)
        assertEquals("In Work · 1 set per round, 2 rounds", curl.place)
        assertEquals("30 s hold · each side", stretch.summary)
    }

    @Test
    fun theRestBetweenSetsIsSavedAndComesBack() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        val key = builder.current.addedKey!!
        builder.setSets(key, 2)
        builder.changeAmount(key, 2)
        builder.setLoadKg(key, 12.0)
        assertEquals("2 × 12 · 12 kg · 1 min between sets", builder.current.rows.single().summary)
        builder.changeRestBetweenSets(key, 2)
        assertEquals("1 min 30", builder.current.rows.single().restBetweenSetsLabel)
        builder.addRest(builder.work)
        builder.changeRest(builder.current.addedKey!!, -2)
        builder.add("push-up", builder.work)

        val id = assertNotNull(builder.save())
        val workout = assertNotNull(graph.store.latestWorkout(WorkoutId(id)))
        val phases = graph.compiler.compile(workout).phases
        // 12 reps, 90 s between the sets, 12 reps, the 30 s rest step, then the push-ups.
        assertEquals(listOf(true, false, true, false, true), phases.take(5).map { it is Phase.Work })
        assertEquals(Target.Reps(12), (phases[0] as Phase.Work).target)
        assertEquals(listOf(90.seconds, 30.seconds), listOf(phases[1].duration, phases[3].duration))
        val sets = (workout.steps.first() as Step.ExerciseStep).prescription.sets
        assertEquals(0.seconds, sets.last().restAfter, "no rest after the last set")

        assertEquals(90, graph.editWorkout(id, "en")!!.current.rows.first().restBetweenSets)

        builder.setSets(key, 1)
        assertEquals("12 reps · 12 kg", builder.current.rows.first().summary)
        builder.setSets(key, 3)
        builder.changeRestBetweenSets(key, -20)
        assertEquals("3 × 12 · 12 kg", builder.current.rows.first().summary)
    }

    @Test
    fun perSideTimedExerciseGetsLeftAndRightWithTimeToSwitch() {
        val builder = builder(WorkoutFormat.SETS, "Stretch")
        builder.add("hamstring-stretch", builder.work)
        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!

        val phases = graph.compiler.compile(workout).phases
        assertEquals(listOf(Side.LEFT, Side.RIGHT), phases.filterIsInstance<Phase.Work>().map { it.side })
        assertEquals(listOf(30.seconds, 5.seconds, 30.seconds), phases.map { it.duration })
    }

    @Test
    fun partsGoInTheirOrder() {
        val builder = builder(WorkoutFormat.SETS)
        fun options() = builder.current.partOptions.associate { it.kind to (it.note to it.enabled) }
        assertEquals(
            mapOf(
                PartKind.WARM_UP to ("Goes first" to true),
                PartKind.WORK to ("Added after Work as Work 2" to true),
                PartKind.COOL_DOWN to ("Goes last" to true),
            ),
            options(),
        )

        builder.addPart(PartKind.COOL_DOWN)
        builder.addPart(PartKind.WORK)
        builder.addPart(PartKind.WARM_UP)
        builder.addPart(PartKind.WARM_UP)
        assertEquals(listOf("Warm-up", "Work 1", "Work 2", "Cool down"), builder.current.parts.map { it.name })
        assertEquals(listOf("Add to warm-up", "Add to Work 1", "Add to Work 2", "Add to cool down"), builder.current.parts.map { it.addLabel })
        assertEquals("Already in this workout" to false, options()[PartKind.WARM_UP])
        assertEquals("Added after Work 2 as Work 3" to true, options()[PartKind.WORK])
    }

    @Test
    fun onlyWorkPartsMoveAndTheLastOneStays() {
        val builder = builder(WorkoutFormat.SETS)
        builder.addPart(PartKind.WARM_UP)
        builder.addPart(PartKind.WORK)
        builder.addPart(PartKind.COOL_DOWN)
        val (warmUp, first, second, coolDown) = builder.current.parts.map { it.key }
        builder.add("push-up", first)

        assertEquals(listOf(false, false, true, false), builder.current.parts.map { it.canMoveUp })
        assertEquals(listOf(false, true, false, false), builder.current.parts.map { it.canMoveDown })
        builder.movePartUp(first)
        builder.movePartUp(coolDown)
        builder.movePartDown(warmUp)
        assertEquals(listOf(warmUp, first, second, coolDown), builder.current.parts.map { it.key })

        builder.movePartDown(first)
        assertEquals(listOf(warmUp, second, first, coolDown), builder.current.parts.map { it.key })
        assertEquals("Work 2", builder.current.parts[2].name)
        assertEquals("Push-up", builder.current.parts[2].steps.single().row?.name)

        builder.removePart(second)
        builder.removePart(warmUp)
        assertEquals(listOf(first, coolDown), builder.current.parts.map { it.key })
        assertFalse(builder.current.parts.first().canDelete)
        builder.removePart(first)
        assertEquals("Work", builder.current.parts.first().name)
    }

    @Test
    fun aNewWorkPartOfACircuitIsARepeat() {
        val builder = builder(WorkoutFormat.CIRCUIT)
        builder.addPart(PartKind.WORK)
        builder.addPart(PartKind.WARM_UP)
        assertEquals(3, builder.current.parts[2].steps.single().repeatGroup?.times)
        assertTrue(builder.current.parts[0].steps.isEmpty())
    }

    @Test
    fun aStepMovesToAnotherPart() {
        val builder = builder(WorkoutFormat.SETS)
        builder.addPart(PartKind.WARM_UP)
        builder.add("push-up", builder.work)
        val push = builder.current.addedKey!!
        builder.addRepeat(builder.work)
        val repeat = builder.current.parts[1].steps.last().key
        builder.add("plank", builder.work, repeat)
        val plank = builder.current.addedKey!!

        val warmUp = builder.part(PartKind.WARM_UP)
        builder.moveToPart(plank, warmUp)
        builder.moveToPart(push, warmUp)
        assertEquals(listOf(plank, push), builder.current.parts[0].steps.map { it.key })
        assertTrue(builder.current.rows.all { it.partKey == warmUp && it.repeatKey == null })
        assertTrue(builder.current.parts[1].steps.single().repeatGroup!!.rows.isEmpty())

        builder.moveToPart(repeat, warmUp)
        assertTrue(builder.current.parts[1].steps.isEmpty())
        assertEquals(repeat, builder.current.parts[0].steps.last().key)
    }

    @Test
    fun everyPartNeedsAStepToSave() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("push-up", builder.work)
        assertTrue(builder.current.canSave)
        builder.addPart(PartKind.COOL_DOWN)
        assertFalse(builder.current.canSave, "an empty cool down")
        builder.add("walk", builder.part(PartKind.COOL_DOWN))
        assertTrue(builder.current.canSave)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        assertEquals(listOf(PartKind.WORK, PartKind.COOL_DOWN), workout.parts.map { it.kind })
    }

    @Test
    fun partSummariesInMetricAndImperial() {
        fun runWorkout(units: UnitSystem, pace: String, paceUnit: PaceUnit): BuilderModel {
            val builder = BuilderModel(WorkoutDraft.empty(), graph.catalog, graph.store, graph.compiler, "en", { units })
            builder.rename("Run")
            builder.chooseFormat(WorkoutFormat.CIRCUIT)
            builder.addPart(PartKind.WARM_UP)
            builder.add("run", builder.part(PartKind.WARM_UP))
            val run = builder.current.addedKey!!
            builder.setIntensityKind(run, IntensityKind.PACE)
            builder.setPace(run, pace, paceUnit)
            return builder
        }

        val metric = runWorkout(UnitSystem.METRIC, "6:00", PaceUnit.PER_KM)
        assertEquals("5.0 km · 6:00 /km", metric.current.rows.single().summary)
        assertEquals("1 step · about 30 min", metric.current.parts.first().summary)
        assertEquals("3 rounds", metric.current.parts.last().summary)

        val imperial = runWorkout(UnitSystem.IMPERIAL, "10:00", PaceUnit.PER_MI)
        assertEquals("3.0 mi · 10:00 /mi", imperial.current.rows.single().summary)
        assertEquals("1 step · about 30 min", imperial.current.parts.first().summary)
    }

    @Test
    fun editingSavesANewVersionWithTheSameExercises() {
        val builder = builder(WorkoutFormat.SETS, "Legs")
        builder.add("bodyweight-squat", builder.work)
        builder.add("reverse-lunge", builder.work)
        val id = builder.save()!!

        val edit = assertNotNull(graph.editWorkout(id, "en"))
        assertTrue(edit.isEditing)
        assertEquals(listOf("Bodyweight squat", "Reverse lunge"), edit.current.rows.map { it.name })
        edit.moveUp(edit.current.rows[1].key)
        edit.save()

        val latest = graph.store.latestWorkout(WorkoutId(id))!!
        assertEquals(2, latest.version)
        assertEquals(listOf("Reverse lunge", "Bodyweight squat"), graph.editWorkout(id, "en")!!.current.rows.map { it.name })
        assertEquals(3, edit.current.rows.single { it.name == "Reverse lunge" }.sets)
    }

    @Test
    fun aWalkIsOneTwoKilometreBlockNotSetsOfShortReps() {
        val builder = builder(WorkoutFormat.CIRCUIT, "Walk")
        val repeat = builder.current.parts.single().steps.single().key
        builder.remove(repeat)
        builder.add("walk", builder.work)
        val row = builder.current.rows.single()
        assertEquals(DurationKind.DISTANCE, row.target?.duration)
        assertEquals(IntensityKind.FREE, row.target?.intensity)
        assertEquals("2.0 km", row.summary)
        assertFalse(row.showsSets)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val phases = graph.compiler.compile(workout).phases
        assertEquals(1, phases.size)
        assertEquals(Target.Distance(2000.0), (phases.single() as Phase.Work).target)
    }

    @Test
    fun distanceAndPaceTargetsSaveAndComeBack() {
        val builder = builder(WorkoutFormat.CIRCUIT, "Long run")
        builder.remove(builder.current.parts.single().steps.single().key)
        builder.add("run", builder.work)
        val key = builder.current.rows.single().key

        builder.setDurationKind(key, DurationKind.DISTANCE)
        builder.setIntensityKind(key, IntensityKind.PACE)
        var target = builder.current.rows.single().target!!
        assertEquals("5.0 km", target.distanceLabel)
        assertEquals("6:00 /km", target.paceLabel)
        assertEquals("10.0 km/h · about 30 min", target.hint)

        assertTrue(builder.setDistance(key, "10", DistanceUnit.KM))
        assertTrue(builder.setPace(key, "5:30", PaceUnit.PER_KM))
        assertFalse(builder.setPace(key, "fast", PaceUnit.PER_KM))
        builder.changeDistance(key, 2)
        builder.changePace(key, -6)
        assertEquals("10.2 km · 5:00 /km", builder.current.rows.single().summary)

        val id = builder.save()!!
        val step = graph.store.latestWorkout(WorkoutId(id))!!.steps.single() as Step.ExerciseStep
        val set = step.prescription.sets.single()
        assertEquals(Target.Distance(10_200.0), set.target)
        assertEquals(12.0, (set.intensity as ch.coachpoulpy.core.model.Intensity.Speed).kmh, 0.001)

        target = graph.editWorkout(id, "en")!!.current.rows.single().target!!
        assertEquals(DurationKind.DISTANCE, target.duration)
        assertEquals(IntensityKind.PACE, target.intensity)
        assertEquals("10.2", target.distanceText)
        assertEquals("5:00", target.paceText)
    }

    @Test
    fun switchingUnitsRewritesTheValue() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("brisk-walk", builder.work)
        val key = builder.current.rows.single().key
        builder.setIntensityKind(key, IntensityKind.PACE)
        assertEquals("10:00", builder.current.rows.single().target!!.paceText)
        assertEquals("6.0", builder.setPaceUnit(key, PaceUnit.KMH, "10:00"))
        assertEquals("6.0 km/h", builder.current.rows.single().target!!.paceLabel)

        builder.setDurationKind(key, DurationKind.DISTANCE)
        builder.setIntensityKind(key, IntensityKind.FREE)
        assertNull(builder.current.rows.single().target!!.speedKmh)
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
        val builder = builder(WorkoutFormat.SETS)
        listOf("goblet-squat", "push-up", "plank").forEach { builder.add(it, builder.work) }
        fun order() = builder.current.rows.map { it.key }
        val (squat, pushUp, plank) = builder.current.rows.map { it.key }

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
