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
    private val graph = AppGraph(DriverFactory()).withSampleCatalog()

    private fun builder(format: WorkoutFormat, name: String = "Monday") = graph.createWorkout(format, "en").apply {
        rename(name)
    }

    @Test
    fun aNewWorkoutStartsInTheFormatItWasCreatedWith() {
        val circuit = builder(WorkoutFormat.CIRCUIT)
        assertEquals(WorkoutFormat.CIRCUIT, circuit.current.format)
        assertEquals("Circuit", circuit.current.formatLabel)
        val repeat = assertNotNull(circuit.current.parts[1].steps.single().repeatGroup, "a circuit's work starts as a repeat")
        assertEquals(3, repeat.times)

        circuit.add("push-up", circuit.work, repeat.key)
        val id = assertNotNull(circuit.save())
        assertEquals(WorkoutFormat.CIRCUIT, assertNotNull(graph.editWorkout(id, "en")).current.format)

        val sets = builder(WorkoutFormat.SETS)
        assertEquals(WorkoutFormat.SETS, sets.current.format)
        assertEquals("Sets", sets.current.formatLabel)
        assertTrue(sets.current.parts[1].steps.isEmpty(), "sets start with nothing in the work part")
    }

    @Test
    fun theFormatSheetOffersCircuitAndSetsBeforeAnyBuilderExists() {
        assertEquals(listOf("Circuit", "Sets"), graph.formatOptions("en").map { it.label })
        assertEquals(listOf(WorkoutFormat.CIRCUIT, WorkoutFormat.SETS), graph.formatOptions("fr").map { it.format })
    }

    @Test
    fun addToAWorkoutStartsTheNewWorkoutWithTheExerciseInEachFormat() {
        val circuit = graph.createWorkoutWith(WorkoutFormat.CIRCUIT, "diamond-push-up", "en")
        assertFalse(circuit.isEditing)
        val repeat = circuit.current.parts[1].steps.single().repeatGroup!!
        assertEquals(listOf("Diamond push-up"), repeat.rows.map { it.name })
        assertEquals(listOf("diamond-push-up"), repeat.rows.map { it.slug })

        val sets = graph.createWorkoutWith(WorkoutFormat.SETS, "diamond-push-up", "en")
        assertEquals(listOf("diamond-push-up"), sets.current.rows.map { it.slug })
        assertEquals(WorkoutFormat.SETS, sets.current.format)
    }

    @Test
    fun switchingTheFormatAsksOnlyWhenTheChoicesDiffer() {
        val builder = builder(WorkoutFormat.SETS)
        assertNull(builder.formatChange(WorkoutFormat.SETS), "already in that format")
        assertFalse(builder.formatChange(WorkoutFormat.CIRCUIT)!!.asks, "an empty workout has nothing to ask")

        builder.add("goblet-squat", builder.work)
        builder.add("push-up", builder.work)
        val change = builder.formatChange(WorkoutFormat.CIRCUIT)!!
        assertTrue(change.asks)
        assertEquals("Circuit", change.formatLabel)
        assertEquals("3 rounds of Goblet squat, Push-up", change.followLine)
        assertEquals("Goblet squat 3 times, then Push-up 3 times", change.keepLine)
    }

    @Test
    fun theSwitchAppliesToTheDraftAndOnlySavingStoresIt() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        val id = assertNotNull(builder.save())
        val edit = assertNotNull(graph.editWorkout(id, "en"))

        edit.changeFormat(WorkoutFormat.CIRCUIT, FormatSwitch.FOLLOW_NEW_FORMAT)
        assertEquals(WorkoutFormat.CIRCUIT, edit.current.format)
        assertEquals(WorkoutFormat.SETS, assertNotNull(graph.editWorkout(id, "en")).current.format, "nothing is saved yet")
        assertTrue(edit.current.canSave)

        edit.save()
        assertEquals(WorkoutFormat.CIRCUIT, assertNotNull(graph.editWorkout(id, "en")).current.format)
        assertEquals(2, graph.store.latestWorkout(WorkoutId(id))!!.version)
    }

    @Test
    fun weightIsTypedAndShownInThePoundsOfTheImperialSystem() {
        val builder = BuilderModel(WorkoutDraft.empty(WorkoutFormat.SETS), graph.catalog, graph.store, graph.compiler, "en", { UnitSystem.IMPERIAL })
        builder.rename("Legs")
        builder.add("goblet-squat", builder.work)
        val key = builder.current.addedKey!!
        builder.setWeight(key, "45")

        val row = builder.current.rows.single()
        assertEquals("45", row.weight)
        assertEquals("lb", row.weightUnit)
        assertTrue(row.summary.contains("45 lb"), row.summary)
        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        val kg = (workout.steps.first() as Step.ExerciseStep).prescription.sets.first().load as ch.coachpoulpy.core.model.Load.Kilograms
        assertEquals(20.41, kg.kg, 0.01)
    }

    @Test
    fun aCircuitHasNoSetsTheRepeatSetsTheRounds() {
        val builder = builder(WorkoutFormat.CIRCUIT)
        val repeat = builder.current.parts[1].steps.single().key
        builder.add("goblet-squat", builder.work, repeat)
        val key = builder.current.addedKey!!
        builder.setRepeatTimes(repeat, 4)
        builder.setSets(key, 3)
        builder.changeAmount(key, 5)
        builder.setWeight(key, "16")

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
        val superset = builder.current.parts[1].steps.last().key
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
        builder.setWeight(key, "12")
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
    fun theWarmUpTheWorkAndTheCoolDownAreAlwaysThere() {
        val builder = builder(WorkoutFormat.SETS)
        assertEquals(listOf("Warm-up", "Work", "Cool down"), builder.current.parts.map { it.name })
        assertEquals(listOf("Add to warm-up", "Add to Work", "Add to cool down"), builder.current.parts.map { it.addLabel })
        assertEquals(listOf("Optional", "Nothing yet", "Optional"), builder.current.parts.map { it.summary })
        assertTrue(builder.current.parts.all { it.steps.isEmpty() })

        val circuit = builder(WorkoutFormat.CIRCUIT)
        assertEquals(3, circuit.current.parts[1].steps.single().repeatGroup?.times, "a circuit's work starts as a repeat")
        assertTrue(circuit.current.parts[0].steps.isEmpty())
    }

    @Test
    fun aRepeatLeftEmptyAtTheEndOfAPartTakesTheNextStep() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("goblet-squat", builder.work)
        builder.addRepeat(builder.work)
        builder.addRest(builder.work)
        builder.add("push-up", builder.work)

        val steps = builder.current.parts[1].steps
        assertEquals(listOf("Goblet squat", null, "Push-up"), steps.map { it.row?.name })
        assertEquals(listOf("Rest"), steps[1].repeatGroup!!.rows.map { it.name })
    }

    @Test
    fun aWorkoutSavedWithoutANameIsNamedAfterItsExercises() {
        fun saved(vararg slugs: String, name: String = "  ", language: String = "en"): String {
            val builder = graph.createWorkout(WorkoutFormat.SETS, language)
            builder.rename(name)
            slugs.forEach { builder.add(it, builder.work) }
            assertTrue(builder.current.canSave, "a blank name does not stop the save")
            return graph.store.latestWorkout(WorkoutId(builder.save()!!))!!.name
        }
        assertEquals("Push-up", saved("push-up"))
        assertEquals("Push-up (+1)", saved("push-up", "plank"))
        assertEquals("Push-up (+1)", saved("push-up", "plank", "push-up"), "an exercise done twice counts once")
        assertEquals("Monday", saved("push-up", name = " Monday "), "a name the person typed is kept")
        assertTrue(saved("push-up", "plank", language = "fr").endsWith("(+1)"))
    }

    @Test
    fun anEmptyWarmUpAndCoolDownAreLeftOutWhenSaving() {
        val builder = builder(WorkoutFormat.SETS)
        assertFalse(builder.current.canSave, "the work has nothing in it")
        builder.add("walk", builder.part(PartKind.WARM_UP))
        assertFalse(builder.current.canSave, "a warm-up alone is not a workout")
        builder.add("push-up", builder.work)
        assertTrue(builder.current.canSave)

        val workout = graph.store.latestWorkout(WorkoutId(builder.save()!!))!!
        assertEquals(listOf(PartKind.WARM_UP, PartKind.WORK), workout.parts.map { it.kind })
        assertEquals(listOf(PartKind.WARM_UP, PartKind.WORK, PartKind.COOL_DOWN), assertNotNull(graph.editWorkout(workout.id.value, "en")).current.parts.map { it.kind })
    }

    @Test
    fun partSummariesInMetricAndImperial() {
        fun runWorkout(units: UnitSystem, pace: String, paceUnit: PaceUnit): BuilderModel {
            val builder = BuilderModel(WorkoutDraft.empty(WorkoutFormat.CIRCUIT), graph.catalog, graph.store, graph.compiler, "en", { units })
            builder.rename("Run")
            builder.add("run", builder.part(PartKind.WARM_UP))
            val run = builder.current.addedKey!!
            builder.setIntensityKind(run, IntensityKind.PACE)
            builder.setPace(run, pace, paceUnit)
            return builder
        }

        val metric = runWorkout(UnitSystem.METRIC, "6:00", PaceUnit.PER_KM)
        assertEquals("5.0 km · 6:00 /km", metric.current.rows.single().summary)
        assertEquals("1 step · about 30 min", metric.current.parts.first().summary)
        assertEquals("3 rounds", metric.current.parts[1].summary)

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
        val repeat = builder.current.parts[1].steps.single().key
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
        builder.remove(builder.current.parts[1].steps.single().key)
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
        val builder = graph.createWorkout(WorkoutFormat.SETS, "fr")
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

    @Test
    fun swapOffersTheWholeGroupAndKeepsThePrescription() {
        val builder = builder(WorkoutFormat.SETS)
        builder.add("push-up", builder.work)
        val key = builder.state.value.addedKey!!
        builder.setSets(key, 4)
        builder.setAmount(key, 12)
        builder.setWeight(key, "10")
        builder.setRestBetweenSets(key, 90)
        builder.add("plank", builder.work)
        val plank = builder.state.value.addedKey!!
        assertFalse(builder.current.parts[1].steps.last().row!!.canSwap, "no variations, no swap")
        assertTrue(builder.swapOptions(plank).isEmpty())
        assertTrue(builder.current.parts[1].steps.first().row!!.canSwap)

        val options = builder.swapOptions(key)
        assertEquals(listOf("knee-push-up", "incline-push-up", "push-up", "diamond-push-up", "one-arm-push-up"), options.map { it.slug })
        assertEquals(listOf("push-up"), options.filter { it.isCurrent }.map { it.slug })

        builder.swap(key, "diamond-push-up")
        val steps = builder.current.parts[1].steps
        assertEquals(listOf(key, plank), steps.map { it.key }, "same position")
        val row = steps.first().row!!
        assertEquals("diamond-push-up", row.slug)
        assertEquals(4, row.sets)
        assertEquals(12, row.amount)
        assertEquals("10", row.weight)
        assertEquals(90, row.restBetweenSets)
        assertTrue(builder.swapOptions(key).single { it.isCurrent }.slug == "diamond-push-up")

        builder.swap(key, "diamond-push-up")
        builder.swap(key, "plank")
        assertEquals("diamond-push-up", builder.current.parts[1].steps.first().row!!.slug, "only a variation can be swapped in")
    }

    @Test
    fun swapToAnExerciseMeasuredAnotherWayTakesItsDefaultAmountAndKeepsTheRest() {
        val graph = AppGraph(DriverFactory())
        val sample = ch.coachpoulpy.catalog.sample.SampleCatalog.catalog
        val group = listOf("push-up", "plank")
        graph.catalogRepository.apply(
            sample.exercises.map { ch.coachpoulpy.data.local.CatalogRevision(it, 1, if (it.slug.value in group) 0 else null, group.indexOf(it.slug.value).takeIf { i -> i >= 0 }) },
            "0",
        )
        val builder = graph.createWorkout(WorkoutFormat.SETS, "en")
        builder.add("push-up", builder.work)
        val key = builder.state.value.addedKey!!
        builder.setSets(key, 2)
        builder.setAmount(key, 15)
        builder.setWeight(key, "5")

        builder.swap(key, "plank")
        val row = builder.current.parts[1].steps.single().row!!
        assertEquals("plank", row.slug)
        assertTrue(row.isTimed, "reps become a hold")
        assertEquals(30, row.amount, "the plank's usual default")
        assertEquals(2, row.sets)
        assertEquals("5", row.weight)
    }
}
