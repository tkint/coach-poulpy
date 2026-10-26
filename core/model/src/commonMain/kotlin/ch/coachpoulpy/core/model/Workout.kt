package ch.coachpoulpy.core.model

import kotlin.time.Duration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How a workout is built, chosen when it is created. A circuit goes round several exercises,
 * each done once per round, so its exercise steps hold one set. Sets does one exercise at a
 * time, all its sets with a rest between them, then the next.
 */
@Serializable
enum class WorkoutFormat { CIRCUIT, SETS }

/**
 * Planned content is versioned and immutable per version: an execution always
 * points at the exact version it followed.
 */
@Serializable
data class Workout(
    val id: WorkoutId,
    val version: Int,
    val name: String,
    val format: WorkoutFormat,
    val parts: List<Part>,
    val tags: List<String> = emptyList(),
) {
    /** Every part's steps, in order, for whatever does not care which part a step is in. */
    val steps: List<Step> get() = parts.flatMap { it.steps }

    /** What breaks the rules of parts, repeats and formats; empty when the workout can be saved and run. */
    fun problems(): Set<WorkoutProblem> = buildSet {
        val kinds = parts.map { it.kind }
        if (PartKind.WORK !in kinds) add(WorkoutProblem.NO_WORK_PART)
        if (kinds.withIndex().any { (i, kind) -> kind == PartKind.WARM_UP && i != 0 }) add(WorkoutProblem.WARM_UP_NOT_FIRST)
        if (kinds.withIndex().any { (i, kind) -> kind == PartKind.COOL_DOWN && i != kinds.lastIndex }) add(WorkoutProblem.COOL_DOWN_NOT_LAST)
        val groups = steps.filterIsInstance<Step.Group>()
        if (groups.any { group -> group.children.any { it is Step.Group } }) add(WorkoutProblem.NESTED_REPEAT)
        val exercises = steps.flatMap { if (it is Step.Group) it.children else listOf(it) }.filterIsInstance<Step.ExerciseStep>()
        // A set done one side at a time is a left and a right prescription, still one set.
        if (format == WorkoutFormat.CIRCUIT && exercises.any { step -> step.prescription.sets.count { it.side != Side.RIGHT } > 1 }) {
            add(WorkoutProblem.SETS_IN_CIRCUIT)
        }
    }
}

/** A rule of [Workout.problems] that a workout breaks. */
@Serializable
enum class WorkoutProblem { NO_WORK_PART, WARM_UP_NOT_FIRST, COOL_DOWN_NOT_LAST, NESTED_REPEAT, SETS_IN_CIRCUIT }

/**
 * One section of a workout: the warm-up, a work part or the cool down. A workout has at most
 * one warm-up, first, at least one work part, and at most one cool down, last.
 */
@Serializable
data class Part(
    val id: PartId,
    val kind: PartKind,
    val steps: List<Step>,
)

@Serializable
enum class PartKind { WARM_UP, WORK, COOL_DOWN }

/** One node of the workout tree. Groups nest, so "3 rounds of (40 s work, 20 s rest)" is one group. */
@Serializable
sealed interface Step {
    val id: StepId

    @Serializable @SerialName("exercise")
    data class ExerciseStep(
        override val id: StepId,
        val exercise: ExerciseSlug,
        val prescription: Prescription,
        val role: StepRole = StepRole.WORK,
    ) : Step

    @Serializable @SerialName("group")
    data class Group(
        override val id: StepId,
        val kind: GroupKind,
        val rounds: Int = 1,
        val children: List<Step>,
        val restBetweenRounds: Duration = Duration.ZERO,
    ) : Step

    @Serializable @SerialName("rest")
    data class Rest(
        override val id: StepId,
        val duration: Duration,
    ) : Step
}

/** What a step is for within its part: work, or recovering from it. Warming up and cooling down are parts. */
@Serializable
enum class StepRole { WORK, RECOVER }

@Serializable
enum class GroupKind { SEQUENCE, SUPERSET, CIRCUIT, INTERVALS }

@Serializable
data class Prescription(
    val sets: List<SetPrescription>,
)

/** One set of an exercise step; [restAfter] is the rest before the next set, none after the last. */
@Serializable
data class SetPrescription(
    val target: Target,
    val load: Load? = null,
    val intensity: Intensity? = null,
    val tempo: String? = null,
    val side: Side = Side.BOTH,
    val restAfter: Duration = Duration.ZERO,
)

/** Targets form a sealed type so each workout type module can choose the ones it accepts. */
@Serializable
sealed interface Target {
    @Serializable @SerialName("reps") data class Reps(val count: Int) : Target
    @Serializable @SerialName("duration") data class Time(val duration: Duration) : Target
    @Serializable @SerialName("distance") data class Distance(val meters: Double) : Target
    @Serializable @SerialName("hold") data class Hold(val duration: Duration) : Target

    /** No set amount: the step ends when the person taps Next, or Finish on the last step. */
    @Serializable @SerialName("open") data object Open : Target
}

@Serializable
sealed interface Load {
    @Serializable @SerialName("kg") data class Kilograms(val kg: Double) : Load
    @Serializable @SerialName("percent1rm") data class PercentOfOneRepMax(val percent: Double) : Load
    @Serializable @SerialName("bodyweight") data object Bodyweight : Load
    @Serializable @SerialName("band") data class Band(val label: String) : Load
}

@Serializable
sealed interface Intensity {
    @Serializable @SerialName("rpe") data class Rpe(val value: Double) : Intensity
    @Serializable @SerialName("speed") data class Speed(val kmh: Double, val inclinePercent: Double = 0.0) : Intensity
    @Serializable @SerialName("hrZone") data class HeartRateZone(val zone: Int) : Intensity
}

@Serializable
enum class Side { LEFT, RIGHT, BOTH }
