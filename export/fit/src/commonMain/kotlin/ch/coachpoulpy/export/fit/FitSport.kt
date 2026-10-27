package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.model.WorkoutFormat

/**
 * The .FIT encoder will live here. For now it only holds the mapping from
 * workouts to FIT sports that Garmin Connect and Strava understand.
 */
enum class FitSport(val sport: Int, val subSport: Int) {
    STRENGTH_TRAINING(sport = 10, subSport = 20),
    CARDIO_TRAINING(sport = 10, subSport = 26),
    TREADMILL_RUNNING(sport = 1, subSport = 1),
    ;

    companion object {
        // CLEANUP: #91 derives the sport from the exercises too: walks and runs only give running,
        // or treadmill running with treadmill samples. Until then only the format decides.
        fun of(format: WorkoutFormat): FitSport = when (format) {
            WorkoutFormat.SETS -> STRENGTH_TRAINING
            WorkoutFormat.CIRCUIT -> CARDIO_TRAINING
        }
    }
}
