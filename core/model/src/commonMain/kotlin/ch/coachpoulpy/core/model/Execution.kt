package ch.coachpoulpy.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/** How a session was run. Stored by name: a value is never renamed. */
@Serializable
enum class ExecutionSource {
    /** A workout followed in the player. */
    GUIDED,
    FREE,

    /** A free walk or run recorded in Live, with or without a device. */
    DEVICE_ONLY,

    /** A treadmill workout followed in Live. Before this value existed, Live stored these as [GUIDED]. */
    LIVE_WORKOUT,
    ;

    /** Recorded in Live: it comes back in Live, not in the player. */
    val isLive: Boolean get() = this == DEVICE_ONLY || this == LIVE_WORKOUT
}

/** The header of one session actually done. Its content is the fold of its event log. */
@Serializable
data class Execution(
    val id: ExecutionId,
    val source: ExecutionSource,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val workout: WorkoutRef? = null,
    val notes: String? = null,
)
