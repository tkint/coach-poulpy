package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/** A workout leaves as a `.poulpyworkout` file and comes back as a copy, or is refused with nothing saved. */
class WorkoutFileTest {
    private val graph = AppGraph(DriverFactory())
    private val catalog = graph.catalog

    private fun exercise(id: String, slug: String, target: Target, side: Side = Side.BOTH) = Step.ExerciseStep(
        StepId(id), ExerciseSlug(slug), Prescription(listOf(SetPrescription(target, load = Load.Bodyweight, side = side, restAfter = 20.seconds))),
    )

    /** Every part kind, a repeat and a rest, in the circuit format. */
    private fun circuit(name: String = "Morning circuit") = Workout(
        WorkoutId("circuit"), 4, name, WorkoutFormat.CIRCUIT,
        listOf(
            Part(PartId("warm"), PartKind.WARM_UP, listOf(exercise("walk-in", "walk", Target.Time(5.minutes)))),
            Part(
                PartId("work"), PartKind.WORK,
                listOf(
                    Step.Group(
                        StepId("repeat"), GroupKind.CIRCUIT, rounds = 3,
                        children = listOf(exercise("push-up", "push-up", Target.Reps(10)), Step.Rest(StepId("rest"), 30.seconds)),
                        restBetweenRounds = 45.seconds,
                    ),
                ),
            ),
            Part(PartId("cool"), PartKind.COOL_DOWN, listOf(exercise("plank", "plank", Target.Hold(30.seconds)))),
        ),
        tags = listOf("morning"),
    )

    /** Two work parts in the sets format. */
    private fun sets() = Workout(
        WorkoutId("sets"), 2, "Upper body", WorkoutFormat.SETS,
        listOf(
            Part(PartId("one"), PartKind.WORK, listOf(exercise("a", "push-up", Target.Reps(8)))),
            Part(PartId("two"), PartKind.WORK, listOf(exercise("b", "plank", Target.Hold(40.seconds)), Step.Rest(StepId("r"), 60.seconds))),
        ),
    )

    private fun read(text: String, catalog: Catalog = this.catalog, existing: Set<String> = emptySet()) =
        WorkoutFile.read(text, catalog, existing) { "$it (copy)" }

    private fun assertRefused(problem: WorkoutFileProblem, text: String, catalog: Catalog = this.catalog) {
        val reading = read(text, catalog)
        assertEquals(problem, reading.problem)
        assertNull(reading.workout)
    }

    @Test
    fun aWorkoutComesBackEqualApartFromItsIdAndVersion() {
        for (workout in listOf(circuit(), sets())) {
            val back = assertNotNull(read(WorkoutFile.export(workout).content).workout)
            assertNotEquals(workout.id, back.id)
            assertEquals(1, back.version)
            assertEquals(workout.copy(id = back.id, version = 1), back)
        }
    }

    @Test
    fun theFileIsNamedAfterTheWorkoutWithItsOwnExtension() {
        assertEquals("morning-circuit.poulpyworkout", WorkoutFile.export(circuit()).fileName)
        assertEquals("seance-du-matin-pecs.poulpyworkout", WorkoutFile.export(circuit("Séance du matin — pecs!")).fileName)
        assertEquals("workout.poulpyworkout", WorkoutFile.export(circuit("???")).fileName)
    }

    @Test
    fun theFileCarriesTheMarkerTheSchemaVersionAndSlugsOnly() {
        val content = WorkoutFile.export(circuit()).content
        assertTrue("\"format\":\"coachpoulpy.workout\"" in content, content)
        assertTrue("\"schemaVersion\":1" in content, content)
        assertTrue("\"push-up\"" in content, content)
        assertFalse("instructions" in content, content)
    }

    @Test
    fun importingTwiceMakesTwoDifferentWorkouts() {
        val text = WorkoutFile.export(circuit()).content
        assertNotEquals(assertNotNull(read(text).workout).id, assertNotNull(read(text).workout).id)
    }

    @Test
    fun anUnknownExerciseIsRefusedAndNamed() {
        val text = WorkoutFile.export(circuit()).content.replace("\"push-up\"", "\"from-the-future\"").replace("\"plank\"", "\"another-one\"")
        val reading = read(text)
        assertEquals(WorkoutFileProblem.UNKNOWN_EXERCISES, reading.problem)
        assertEquals(listOf("from-the-future", "another-one"), reading.unknownExercises)
        assertNull(reading.workout)
    }

    @Test
    fun aRetiredExerciseStillImports() {
        val retired = Exercise(
            ExerciseSlug("old-move"), LocalizedText(mapOf("en" to "Old move")), movementPattern = MovementPattern.CORE,
            muscleGroups = listOf(MuscleGroup.CORE), measurement = MeasurementMode.REPS, retired = true,
        )
        val withRetired = Catalog(catalog.version, catalog.exercises + retired, catalog.variationGroups)
        val workout = sets().copy(parts = listOf(Part(PartId("one"), PartKind.WORK, listOf(exercise("a", "old-move", Target.Reps(5))))))
        assertNotNull(read(WorkoutFile.export(workout).content, withRetired).workout)
    }

    @Test
    fun aFileOfAnotherKindOrBrokenIsRefused() {
        val good = WorkoutFile.export(sets()).content
        assertRefused(WorkoutFileProblem.NOT_A_WORKOUT, good.replace("coachpoulpy.workout", "something.else"))
        assertRefused(WorkoutFileProblem.NOT_A_WORKOUT, "{}")
        assertRefused(WorkoutFileProblem.UNREADABLE, "not json at all")
        assertRefused(WorkoutFileProblem.UNREADABLE, good.take(good.length / 2))
        assertRefused(WorkoutFileProblem.UNREADABLE, good.replace("\"workout\":{", "\"workout\":{\"parts\":3,"))
        assertRefused(WorkoutFileProblem.UNREADABLE, "")
    }

    @Test
    fun aNewerSchemaSaysToUpdateTheApp() {
        val text = WorkoutFile.export(sets()).content.replace("\"schemaVersion\":1", "\"schemaVersion\":2")
        assertRefused(WorkoutFileProblem.NEWER_VERSION, text)
        // Even when the newer file changed the workout so this version could not read it.
        assertRefused(WorkoutFileProblem.NEWER_VERSION, """{"format":"coachpoulpy.workout","schemaVersion":2,"workout":{"other":1}}""")
    }

    @Test
    fun aWorkoutBreakingTheRulesIsRefused() {
        val noWork = sets().copy(parts = listOf(Part(PartId("w"), PartKind.WARM_UP, listOf(exercise("a", "walk", Target.Time(1.minutes))))))
        assertRefused(WorkoutFileProblem.INVALID_WORKOUT, WorkoutFile.export(noWork).content)
    }

    @Test
    fun aNameAlreadyInTheLibraryGetsTheCopyName() {
        val text = WorkoutFile.export(sets()).content
        assertEquals("Upper body (copy)", read(text, existing = setOf("Upper body")).workout?.name)
        assertEquals("Upper body", read(text, existing = setOf("Lower body")).workout?.name)
    }

    @Test
    fun addingSavesACopyAndARefusedFileSavesNothing() {
        val before = graph.store.savedWorkouts().size
        val model = graph.importWorkout(WorkoutFile.export(circuit("Imported circuit")).content, "en")
        assertEquals(
            WorkoutImportState(ready = true, name = "Imported circuit", formatLabel = "Circuit", partsLabel = "3 parts", stepsLabel = "4 steps"),
            model.state,
        )
        val id = assertNotNull(model.add())
        val saved = assertNotNull(graph.store.latestWorkout(WorkoutId(id)))
        assertEquals(1, saved.version)
        assertEquals(before + 1, graph.store.savedWorkouts().size)

        val again = graph.importWorkout(WorkoutFile.export(circuit("Imported circuit")).content, "en")
        assertEquals("Imported circuit (copy)", again.state.name)

        val broken = graph.importWorkout("nope", "en")
        assertFalse(broken.state.ready)
        assertEquals("This file can’t be read. It may be damaged.", broken.state.errorMessage)
        assertNull(broken.add())
        assertEquals(before + 1, graph.store.savedWorkouts().size)
    }

    @Test
    fun theUnknownExercisesAreNamedInEachLanguage() {
        val text = WorkoutFile.export(sets()).content.replace("\"push-up\"", "\"from-the-future\"")
        assertEquals(
            "This workout uses an exercise this version doesn’t have: from-the-future. Update the app to open it.",
            graph.importWorkout(text, "en").state.errorMessage,
        )
        assertTrue("un exercice que cette version ne connaît pas" in graph.importWorkout(text, "fr").state.errorMessage)
    }

    @Test
    fun theWorkoutPageHandsOverItsFile() {
        graph.store.saveWorkout(sets())
        val page = WorkoutPageModel(sets().id, graph.store, graph.compiler, catalog, "en", scope = CoroutineScope(Dispatchers.Default))
        assertEquals("upper-body.poulpyworkout", page.exportFile()?.fileName)
        page.close()
    }
}
