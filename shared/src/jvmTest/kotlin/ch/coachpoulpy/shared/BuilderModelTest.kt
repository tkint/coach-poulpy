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
}
