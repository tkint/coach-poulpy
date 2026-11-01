package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.BodyRange
import ch.coachpoulpy.core.model.BodyTrend
import ch.coachpoulpy.data.local.LocalStore
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** What a weight or height page shows. Values are kg or cm; the apps convert them with [Units] for display. */
data class BodyState(
    val metric: BodyMetric,
    val range: BodyRange,
    val trend: BodyTrend,
    /** Every entry, newest first, for the history list. */
    val history: List<BodyMeasurement>,
    val latest: BodyMeasurement?,
)

/** One log entry as plain values, so Swift reads it without Kotlin's date types. [value] is kg or cm. */
data class BodyPoint(val date: PlanDate, val epochDay: Long, val value: Double)

/** [BodyState] for Swift: entries oldest first in [points], newest first in [history]. */
data class BodyUi(
    val metric: BodyMetric,
    val range: BodyRange,
    val points: List<BodyPoint>,
    val history: List<BodyPoint>,
    val latest: BodyPoint?,
    val lowest: BodyPoint?,
    val highest: BodyPoint?,
    val change: Double?,
)

/** Weight and height: log, correct, delete and read the chart. One model per metric. */
class BodyModel(
    private val metric: BodyMetric,
    private val store: LocalStore,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val range = MutableStateFlow(BodyRange.THREE_MONTHS)

    val state: StateFlow<BodyState> = combine(store.bodyMeasurements(metric), range) { log, range ->
        BodyState(
            metric = metric,
            range = range,
            trend = BodyTrend.of(log, range, today()),
            history = log.sortedByDescending { it.day },
            latest = log.maxByOrNull { it.day },
        )
    }.stateIn(scope, SharingStarted.Eagerly, BodyState(metric, BodyRange.THREE_MONTHS, BodyTrend.of(emptyList(), BodyRange.THREE_MONTHS, today()), emptyList(), null))

    /** [state] in plain values, for the iOS screens. */
    val ui: StateFlow<BodyUi> = state.map { it.toUi() }.stateIn(scope, SharingStarted.Eagerly, state.value.toUi())

    fun selectRange(selected: BodyRange) {
        range.value = selected
    }

    /** Logs [value] (kg or cm) on [day], or today. A second entry for a day replaces the first. */
    fun log(value: Double, day: LocalDate = today()) {
        if (value <= 0.0 || day > today()) return
        store.logBodyMeasurement(BodyMeasurement(metric, day, value))
    }

    fun delete(day: LocalDate) = store.deleteBodyMeasurement(metric, day)

    /** [log] for Swift, which has no Kotlin date to pass. */
    fun logOn(value: Double, date: PlanDate) = log(value, date.date)

    fun deleteOn(date: PlanDate) = delete(date.date)

    /** Stops following the log once the screen is gone. */
    fun close() = scope.cancel()

    fun watch(onChange: (BodyUi) -> Unit): Cancellable = ui.watchIn(scope, onChange)
}

private fun BodyMeasurement.toPoint() = BodyPoint(PlanDate.of(day), day.toEpochDays().toLong(), value)

private fun BodyState.toUi() = BodyUi(
    metric = metric,
    range = range,
    points = trend.entries.map { it.toPoint() },
    history = history.map { it.toPoint() },
    latest = latest?.toPoint(),
    lowest = trend.lowest?.toPoint(),
    highest = trend.highest?.toPoint(),
    change = trend.change,
)
