package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.Timeline
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.ExecutionSource
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.core.stats.DoneSession
import ch.coachpoulpy.core.stats.HistoryStats
import ch.coachpoulpy.core.stats.PeriodTotals
import ch.coachpoulpy.core.stats.SessionTotals
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.StoredSession
import ch.coachpoulpy.types.treadmill.TreadmillModule
import ch.coachpoulpy.types.treadmill.TreadmillSummary
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** A guided workout, or a treadmill session (free run or treadmill workout), which shows distance rather than sets. */
enum class SessionKind { WORKOUT, TREADMILL }

/** One finished session. Dates are epoch milliseconds, which each platform formats in the user's locale. */
data class SessionRow(
    val id: String,
    val title: String,
    val kind: SessionKind,
    /** The kind of workout, in the user's language, or null for a free run. */
    val typeLabel: String?,
    val startedAtMillis: Long,
    val activeSeconds: Long,
    val setsDone: Int?,
    val setsSkipped: Int?,
    val setsPlanned: Int?,
    val distanceMeters: Double?,
    val averageSpeedKmh: Double?,
    val averageHeartRateBpm: Int?,
    val maxHeartRateBpm: Int?,
    val energyKcal: Int?,
    /** Estimated from the speed, shown with "≈". */
    val estimatedSteps: Int?,
)

data class PeriodUi(val sessions: Int, val activeMinutes: Int, val distanceMeters: Double)

/** One bar of the weekly charts. [startMillis] is Monday at midnight, local time. */
data class WeekBar(val startMillis: Long, val activeMinutes: Int, val distanceKm: Double, val isCurrent: Boolean)

data class HistoryState(
    /** False until the sessions have been read once, so the screen does not flash "nothing yet". */
    val loaded: Boolean = false,
    val sessions: List<SessionRow> = emptyList(),
    val thisWeek: PeriodUi = PeriodUi(0, 0, 0.0),
    val thisMonth: PeriodUi = PeriodUi(0, 0, 0.0),
    /** The last eight weeks, oldest first, the current one last. */
    val weeks: List<WeekBar> = emptyList(),
) {
    /** Whether the distance chart has anything to show. */
    val hasDistance: Boolean get() = weeks.any { it.distanceKm > 0 }
}

/**
 * The history screen: every finished session on this phone, newest first, with this
 * week's and this month's totals and the last weeks as charts. Sessions done before
 * the history existed are summed up once, the first time it opens.
 */
class HistoryModel internal constructor(
    private val store: LocalStore,
    private val compiler: TimelineCompiler,
    languageCode: String,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val labels = Labels(languageCode)

    val state: StateFlow<HistoryState> = store.finishedSessions()
        .map(::build)
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, HistoryState())

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: HistoryState get() = state.value

    fun watch(onChange: (HistoryState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    /** Removes a session and everything recorded in it, for example a test recording. */
    fun delete(sessionId: String) = store.deleteSession(sessionId)

    fun close() = scope.cancel()

    private fun build(stored: List<StoredSession>): HistoryState {
        val done = stored.mapNotNull { session ->
            val totals = session.totals ?: backfill(session)
            if (totals.isEmpty) null else session to totals
        }
        val stats = HistoryStats.of(done.map { (s, t) -> DoneSession(s.execution.startedAt, t) }, clock.now(), zone)
        return HistoryState(
            loaded = true,
            sessions = done.map { (s, t) -> row(s, t) },
            thisWeek = stats.thisWeek.toUi(),
            thisMonth = stats.thisMonth.toUi(),
            weeks = stats.weeks.mapIndexed { index, week ->
                WeekBar(
                    startMillis = week.monday.atStartOfDayIn(zone).toEpochMilliseconds(),
                    activeMinutes = week.totals.active.inWholeMinutes.toInt(),
                    distanceKm = week.totals.distanceMeters / 1000,
                    isCurrent = index == stats.weeks.lastIndex,
                )
            },
        )
    }

    /** Sums up a session from before the history, and keeps its name and type while its workout is still here. */
    private fun backfill(session: StoredSession): SessionTotals {
        val id = session.execution.id.value
        val events = store.events(id)
        val workout = session.execution.workout?.let { store.workout(it.workoutId, it.version) }
        val type = session.type ?: workout?.type ?: if (isTreadmill(session, events)) WorkoutType.TREADMILL else null
        val timeline = when {
            workout != null -> runCatching { compiler.compile(workout) }.getOrNull()
            session.execution.source == ExecutionSource.DEVICE_ONLY -> TreadmillModule.freeRun
            type == WorkoutType.TREADMILL -> compiler.compile(TreadmillModel.demoIntervals)
            else -> null
        }
        val totals = sessionTotals(events, timeline)
        store.saveDetails(id, session.title ?: workout?.name, type, totals)
        return totals
    }

    /** Free runs and the demo intervals are the only sessions that follow no saved workout. */
    private fun isTreadmill(session: StoredSession, events: List<SessionEvent>) =
        session.execution.workout == null || events.any { it is SessionEvent.Sample }

    private fun row(session: StoredSession, totals: SessionTotals): SessionRow {
        val execution = session.execution
        val freeRun = execution.source == ExecutionSource.DEVICE_ONLY
        val treadmill = freeRun || execution.workout == null || session.type == WorkoutType.TREADMILL || totals.distanceMeters != null
        return SessionRow(
            id = execution.id.value,
            title = session.title ?: when {
                freeRun -> labels.freeRun
                treadmill && execution.workout == null -> labels.demoIntervals
                else -> labels.session
            },
            kind = if (treadmill) SessionKind.TREADMILL else SessionKind.WORKOUT,
            typeLabel = if (freeRun) null else (session.type ?: if (treadmill) WorkoutType.TREADMILL else null)?.let(labels::workoutType),
            startedAtMillis = execution.startedAt.toEpochMilliseconds(),
            activeSeconds = totals.active.inWholeSeconds,
            setsDone = totals.setsDone,
            setsSkipped = totals.setsSkipped,
            setsPlanned = totals.setsPlanned,
            distanceMeters = totals.distanceMeters,
            averageSpeedKmh = totals.averageSpeedKmh,
            averageHeartRateBpm = totals.averageHeartRateBpm,
            maxHeartRateBpm = totals.maxHeartRateBpm,
            energyKcal = totals.energyKcal,
            estimatedSteps = totals.estimatedSteps,
        )
    }

    private fun PeriodTotals.toUi() = PeriodUi(sessions, active.inWholeMinutes.toInt(), distanceMeters)
}

/**
 * Time and sets of a session, plus what a connected machine reported. [timeline] is
 * the plan it followed, or null when that is no longer on the phone.
 */
internal fun sessionTotals(events: List<SessionEvent>, timeline: Timeline?): SessionTotals {
    val totals = SessionTotals.of(events, timeline)
    if (events.none { it is SessionEvent.Sample }) return totals
    // The machine totals do not depend on the plan's phases.
    val machine = TreadmillSummary.of(timeline ?: Timeline(emptyList()), events)
    return totals.copy(
        distanceMeters = machine.distanceMeters,
        averageSpeedKmh = machine.averageSpeedKmh,
        averageHeartRateBpm = machine.averageHeartRateBpm,
        maxHeartRateBpm = machine.maxHeartRateBpm,
        energyKcal = machine.energyKcal,
        estimatedSteps = machine.estimatedSteps,
    )
}
