package ch.coachpoulpy.core.model

import kotlin.time.Duration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Each workout type is implemented by its own module; this id ties content to that module. */
@Serializable
enum class WorkoutType { STRENGTH, CONDITIONING, REHAB, MOBILITY, TREADMILL }

/**
 * Planned content is versioned and immutable per version: an execution always
 * points at the exact version it followed.
 */
@Serializable
data class Workout(
    val id: WorkoutId,
    val version: Int,
    val name: String,
    val type: WorkoutType,
    val steps: List<Step>,
    val tags: List<String> = emptyList(),
)

/** One node of the workout tree. Groups nest, so "3 rounds of (40 s work, 20 s rest)" is one group. */
@Serializable
sealed interface Step {
    val id: StepId

    @Serializable @SerialName("exercise")
    data class ExerciseStep(
        override val id: StepId,
        val exercise: ExerciseSlug,
        val prescription: Prescription,
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

@Serializable
enum class GroupKind { SEQUENCE, SUPERSET, CIRCUIT, INTERVALS }

@Serializable
data class Prescription(
    val sets: List<SetPrescription>,
)

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
