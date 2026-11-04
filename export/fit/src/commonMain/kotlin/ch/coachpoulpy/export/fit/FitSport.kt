package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.model.WorkoutFormat

/** The FIT sports a session is exported as, the ones Garmin Connect and Strava understand. */
enum class FitSport(val sport: Int, val subSport: Int) {
    STRENGTH_TRAINING(sport = 10, subSport = 20),
    CARDIO_TRAINING(sport = 10, subSport = 26),
    RUNNING(sport = 1, subSport = 0),
    TREADMILL_RUNNING(sport = 1, subSport = 1),
    ;

    companion object {
        /**
         * A workout of only walks and runs is running, on a treadmill when the session has
         * [treadmillSamples]; any other workout follows its [format].
         */
        fun of(format: WorkoutFormat, walksAndRunsOnly: Boolean, treadmillSamples: Boolean): FitSport = when {
            walksAndRunsOnly && treadmillSamples -> TREADMILL_RUNNING
            walksAndRunsOnly -> RUNNING
            format == WorkoutFormat.SETS -> STRENGTH_TRAINING
            else -> CARDIO_TRAINING
        }
    }
}
