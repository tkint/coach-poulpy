package ch.coachpoulpy.core.stats

/**
 * Statistics are derived from executions, never typed in. Each workout type
 * declares the metrics it produces; this module computes them. History so far
 * uses time and distance ([SessionTotals]); the rest come with per-exercise progress.
 */
enum class Metric { VOLUME_KG, ESTIMATED_ONE_REP_MAX, TIME_UNDER_TENSION, TIME_IN_HOLD, DISTANCE, AVERAGE_SPEED, ROUNDS }
