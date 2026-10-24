package ch.coachpoulpy.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class ExecutionSource { GUIDED, FREE, DEVICE_ONLY }

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
