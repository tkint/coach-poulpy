package ch.coachpoulpy.export.fit

import ch.coachpoulpy.core.model.WorkoutType

/**
 * The .FIT encoder will live here. For now it only holds the mapping from
 * workout types to FIT sports that Garmin Connect and Strava understand.
 */
enum class FitSport(val sport: Int, val subSport: Int) {
    STRENGTH_TRAINING(sport = 10, subSport = 20),
    CARDIO_TRAINING(sport = 10, subSport = 26),
    FLEXIBILITY(sport = 10, subSport = 19),
    TREADMILL_RUNNING(sport = 1, subSport = 1),
    ;

    companion object {
        fun of(type: WorkoutType): FitSport = when (type) {
            WorkoutType.STRENGTH -> STRENGTH_TRAINING
            WorkoutType.CONDITIONING -> CARDIO_TRAINING
            WorkoutType.REHAB, WorkoutType.MOBILITY -> FLEXIBILITY
            WorkoutType.TREADMILL -> TREADMILL_RUNNING
        }
    }
}
