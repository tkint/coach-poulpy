package ch.coachpoulpy.core.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Every week is written out in full in v1. */
@Serializable
data class Plan(
    val id: PlanId,
    val version: Int,
    val name: String,
    val goal: String? = null,
    val weeks: List<PlanWeek>,
)

@Serializable
data class PlanWeek(val days: List<PlanDay>)

/** Day index 0 is the first day of the week the plan starts on. */
@Serializable
data class PlanDay(val dayIndex: Int, val workouts: List<WorkoutRef>)

@Serializable
data class WorkoutRef(val workoutId: WorkoutId, val version: Int)

/** A user following a plan, pinned to one plan version. */
@Serializable
data class Enrollment(
    val id: EnrollmentId,
    val plan: PlanId,
    val planVersion: Int,
    val startDate: LocalDate,
    val entries: List<CalendarEntry>,
)

@Serializable
data class CalendarEntry(
    val date: LocalDate,
    val workout: WorkoutRef,
    val status: CalendarStatus = CalendarStatus.PLANNED,
)

@Serializable
enum class CalendarStatus { PLANNED, DONE, SKIPPED, MOVED }

@Serializable
data class Goal(
    val id: GoalId,
    val description: String,
    val deadline: LocalDate? = null,
)
