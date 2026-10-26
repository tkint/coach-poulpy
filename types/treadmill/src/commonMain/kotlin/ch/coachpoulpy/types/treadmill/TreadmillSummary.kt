package ch.coachpoulpy.types.treadmill

import ch.coachpoulpy.core.engine.Phase
import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.covering
import ch.coachpoulpy.core.model.DeviceMetrics
import ch.coachpoulpy.core.model.Intensity
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/** What one phase of a recorded session looked like, next to what was planned for it. */
data class PhaseResult(
    val phaseIndex: Int,
    val phase: Phase,
    val duration: Duration,
    val distanceMeters: Double?,
    val averageSpeedKmh: Double?,
    val averageInclinePercent: Double?,
    val targetSpeedKmh: Double?,
    val targetInclinePercent: Double?,
    /** Share of readings within [TreadmillSummary.SPEED_TOLERANCE_KMH] of the target speed, 0 to 1. */
    val timeOnTargetSpeed: Double?,
)

data class TreadmillSummary(
    val duration: Duration,
    val distanceMeters: Double?,
    val averageSpeedKmh: Double?,
    val averageHeartRateBpm: Int?,
    val maxHeartRateBpm: Int?,
    val energyKcal: Int?,
    val elevationGainMeters: Double?,
    /** Estimated from the speed: see [StepEstimate]. */
    val estimatedSteps: Int?,
    val phases: List<PhaseResult>,
) {
    companion object {
        const val SPEED_TOLERANCE_KMH = 0.3

        /**
         * Folds a session's events into totals and per-phase results. A phase's distance
         * runs from the last reading before it to its own last reading, so the phases add
         * up to the total.
         */
        fun of(timeline: Timeline, events: List<SessionEvent>): TreadmillSummary {
            val start = events.firstOrNull { it is SessionEvent.Started }?.at
            val end = events.lastOrNull { it is SessionEvent.Finished }?.at ?: events.maxOfOrNull { it.at }
            val ends = events
                .filter { it is SessionEvent.PhaseCompleted || it is SessionEvent.PhaseSkipped }
                .map { it.at }
                .sorted()
            // The recorder ticks the session before logging a reading, so log order gives each
            // reading the phase that was running when it was taken.
            val samplesByPhase = mutableMapOf<Int, MutableList<SessionEvent.Sample>>()
            var phaseIndex = 0
            for (event in events) when (event) {
                is SessionEvent.PhaseCompleted, is SessionEvent.PhaseSkipped -> phaseIndex++
                is SessionEvent.Sample -> samplesByPhase.getOrPut(phaseIndex) { mutableListOf() } += event
                else -> Unit
            }
            val samples = events.filterIsInstance<SessionEvent.Sample>()

            val pauses = pauses(events, end)
            var lastBefore: SessionEvent.Sample? = null
            val phases = timeline.covering(events).phases.mapIndexedNotNull { index, phase ->
                val from = if (index == 0) start else ends.getOrNull(index - 1)
                val to = ends.getOrNull(index) ?: end
                val inPhase = samplesByPhase[index].orEmpty()
                val atStart = lastBefore
                lastBefore = inPhase.lastOrNull() ?: lastBefore
                if (from == null || to == null || from > to) return@mapIndexedNotNull null
                val paused = pauses.sumOf { (pausedAt, resumedAt) ->
                    (minOf(to, resumedAt) - maxOf(from, pausedAt)).coerceAtLeast(Duration.ZERO).inWholeMilliseconds
                }.milliseconds
                phaseResult(index, phase, to - from - paused, inPhase, atStart)
            }

            val metrics = samples.map { it.metrics }
            val heartRates = metrics.mapNotNull { it.heartRateBpm }
            return TreadmillSummary(
                duration = if (start != null && end != null) {
                    end - start - pauses.sumOf { (from, to) -> (to - from).inWholeMilliseconds }.milliseconds
                } else {
                    Duration.ZERO
                },
                distanceMeters = metrics.counterDelta { it.distanceMeters },
                averageSpeedKmh = metrics.mapNotNull { it.speedKmh }.averageOrNull(),
                averageHeartRateBpm = heartRates.map { it.toDouble() }.averageOrNull()?.toInt(),
                maxHeartRateBpm = heartRates.maxOrNull(),
                energyKcal = metrics.counterDelta { it.energyKcal?.toDouble() }?.toInt(),
                elevationGainMeters = metrics.counterDelta { it.elevationGainMeters },
                estimatedSteps = StepEstimate.steps(events),
                phases = phases,
            )
        }

        /** Each pause, from its start to the resume, or to [end] when never resumed. */
        fun pauses(events: List<SessionEvent>, end: Instant?): List<Pair<Instant, Instant>> {
            val spans = mutableListOf<Pair<Instant, Instant>>()
            var pausedAt: Instant? = null
            for (event in events) when (event) {
                is SessionEvent.Paused -> if (pausedAt == null) pausedAt = event.at
                is SessionEvent.Resumed -> {
                    pausedAt?.let { spans += it to event.at }
                    pausedAt = null
                }
                else -> Unit
            }
            pausedAt?.let { from -> end?.let { spans += from to maxOf(from, it) } }
            return spans
        }

        /** [atStart] is the last reading before the phase began, the base for its distance. */
        private fun phaseResult(
            index: Int,
            phase: Phase,
            duration: Duration,
            samples: List<SessionEvent.Sample>,
            atStart: SessionEvent.Sample?,
        ): PhaseResult {
            val metrics = samples.map { it.metrics }
            val target = (phase as? Phase.Work)?.intensity as? Intensity.Speed
            val speeds = metrics.mapNotNull { it.speedKmh }
            return PhaseResult(
                phaseIndex = index,
                phase = phase,
                duration = duration,
                distanceMeters = (listOfNotNull(atStart?.metrics) + metrics).counterDelta { it.distanceMeters },
                averageSpeedKmh = speeds.averageOrNull(),
                averageInclinePercent = metrics.mapNotNull { it.inclinePercent }.averageOrNull(),
                targetSpeedKmh = target?.kmh,
                targetInclinePercent = target?.inclinePercent,
                timeOnTargetSpeed = target?.let { t ->
                    speeds.takeIf { it.isNotEmpty() }?.let { s -> s.count { abs(it - t.kmh) <= SPEED_TOLERANCE_KMH }.toDouble() / s.size }
                },
            )
        }

        /** How much a machine's running total grew across these readings. */
        private fun List<DeviceMetrics>.counterDelta(value: (DeviceMetrics) -> Double?): Double? {
            val values = mapNotNull(value)
            return if (values.size < 2) null else values.last() - values.first()
        }

        private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()
    }
}
