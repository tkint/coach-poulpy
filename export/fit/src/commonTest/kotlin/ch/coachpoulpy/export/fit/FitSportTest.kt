package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.model.WorkoutFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class FitSportTest {
    @Test
    fun walksAndRunsAreRunningOnATreadmillWhenItSentSamples() {
        assertEquals(FitSport.TREADMILL_RUNNING, FitSport.of(WorkoutFormat.CIRCUIT, walksAndRunsOnly = true, treadmillSamples = true))
        assertEquals(FitSport.RUNNING, FitSport.of(WorkoutFormat.CIRCUIT, walksAndRunsOnly = true, treadmillSamples = false))
        assertEquals(FitSport.RUNNING, FitSport.of(WorkoutFormat.SETS, walksAndRunsOnly = true, treadmillSamples = false))
    }

    @Test
    fun anyOtherWorkoutFollowsItsFormat() {
        assertEquals(FitSport.STRENGTH_TRAINING, FitSport.of(WorkoutFormat.SETS, walksAndRunsOnly = false, treadmillSamples = false))
        assertEquals(FitSport.CARDIO_TRAINING, FitSport.of(WorkoutFormat.CIRCUIT, walksAndRunsOnly = false, treadmillSamples = true))
    }
}
