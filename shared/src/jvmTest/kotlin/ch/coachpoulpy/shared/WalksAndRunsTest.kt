package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/** Live offers, and a FIT export calls running, only workouts made of walks and runs. */
class WalksAndRunsTest {
    private val catalog = AppGraph(DriverFactory()).catalog

    private fun step(slug: String) =
        Step.ExerciseStep(StepId(slug), ExerciseSlug(slug), Prescription(listOf(SetPrescription(Target.Time(5.minutes)))))

    @Test
    fun onlyWalksAndRunsCount() {
        val run = workoutOf("run", "Run", listOf(step("walk"), step("easy-run"), Step.Rest(StepId("rest"), 1.minutes)), WorkoutFormat.CIRCUIT)
        val mixed = workoutOf("mixed", "Mixed", listOf(step("walk"), step("push-up")), WorkoutFormat.CIRCUIT)

        assertTrue(run.isWalksAndRuns(catalog))
        assertTrue(LiveModel.demoIntervals.isWalksAndRuns(catalog), "the treadmill run of the demo counts")
        assertFalse(mixed.isWalksAndRuns(catalog))
        assertFalse(workoutOf("empty", "Empty", emptyList()).isWalksAndRuns(catalog))
    }
}
