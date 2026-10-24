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
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

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
    val totalSessions: Int = 0,
    /** The longest treadmill distance in one session, and when it started; null without any. */
    val longestRunMeters: Double? = null,
    val longestRunMillis: Long? = null,
    /** Poulpy's tentacles, as on Home. */
    val fullOctopuses: Int = 0,
    val tentaclesLit: Int = 0,
    /** How far back the calendar goes: 0 for this month, -3 when the oldest session is three months ago. */
    val firstMonthOffset: Int = 0,
) {
    /** Whether the distance chart has anything to show. */
    val hasDistance: Boolean get() = weeks.any { it.distanceKm > 0 }
}

/** One day of the month grid. */
data class CalendarDay(val day: Int, val sessionCount: Int, val isToday: Boolean, val isFuture: Boolean) {
    val active: Boolean get() = sessionCount > 0
}

/** The sessions of one day, for the list under the calendar. No sessions: the selected day was a rest day. */
data class DaySection(val day: Int, val dateMillis: Long, val isToday: Boolean, val sessions: List<SessionRow>)

/** One month of the calendar, Monday first. [monthStartMillis] is the 1st at midnight, for the platform to name the month. */
data class CalendarMonth(
    val year: Int,
    /** 1 to 12. */
    val month: Int,
    val monthStartMillis: Long,
    /** Empty cells before the 1st, so it falls under its weekday. */
    val leadingBlanks: Int,
    val days: List<CalendarDay>,
    val activeDays: Int,
    val canGoBack: Boolean,
    /** False for the current month: the calendar never shows the future. */
    val canGoForward: Boolean,
    /** Days with sessions, newest first, plus the selected day when it had none. */
    val sections: List<DaySection>,
)

/**
 * The history screen: a month calendar over the sessions of that month, and the stats
 * (this week's and this month's totals, the last weeks as charts, records and Poulpy). Sessions done before
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

    private val coach = PoulpyCoach(languageCode, zone)

    val state: StateFlow<HistoryState> = combine(store.finishedSessions(), store.finishedSessionEnds(), ::build)
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, HistoryState())

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: HistoryState get() = state.value

    fun watch(onChange: (HistoryState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    /** Removes a session and everything recorded in it, for example a test recording. */
    fun delete(sessionId: String) = store.deleteSession(sessionId)

    fun close() = scope.cancel()

    /**
     * The month [offset] months from this one (0 now, -1 last month) out of [state], with
     * [selectedDay] (1 to 31, or 0 for none) kept in the list even if nothing was done that day.
     */
    fun month(state: HistoryState, offset: Int, selectedDay: Int): CalendarMonth {
        val today = clock.now().toLocalDateTime(zone).date
        val first = LocalDate(today.year, today.month, 1).plus(DatePeriod(months = offset.coerceAtMost(0)))
        val length = first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day
        val byDay = state.sessions
            .groupBy { Instant.fromEpochMilliseconds(it.startedAtMillis).toLocalDateTime(zone).date }
            .filterKeys { it.year == first.year && it.month == first.month }
            .mapKeys { it.key.day }
        val days = (1..length).map { day ->
            val date = LocalDate(first.year, first.month, day)
            CalendarDay(day, byDay[day]?.size ?: 0, isToday = date == today, isFuture = date > today)
        }
        val shown = byDay.keys + listOfNotNull(selectedDay.takeIf { it in 1..length && !days[it - 1].isFuture })
        return CalendarMonth(
            year = first.year,
            month = first.month.number,
            monthStartMillis = first.atStartOfDayIn(zone).toEpochMilliseconds(),
            leadingBlanks = first.dayOfWeek.isoDayNumber - 1,
            days = days,
            activeDays = byDay.size,
            canGoBack = offset > state.firstMonthOffset,
            canGoForward = offset < 0,
            sections = shown.sortedDescending().map { day ->
                val date = LocalDate(first.year, first.month, day)
                DaySection(day, date.atStartOfDayIn(zone).toEpochMilliseconds(), date == today, byDay[day].orEmpty())
            },
        )
    }

    private fun build(stored: List<StoredSession>, finishedEnds: List<Instant>): HistoryState {
        val done = stored.mapNotNull { session ->
            val totals = session.totals ?: backfill(session)
            if (totals.isEmpty) null else session to totals
        }
        val now = clock.now()
        val stats = HistoryStats.of(done.map { (s, t) -> DoneSession(s.execution.startedAt, t) }, now, zone)
        val longest = done.filter { (_, t) -> (t.distanceMeters ?: 0.0) > 0 }.maxByOrNull { (_, t) -> t.distanceMeters!! }
        val poulpy = coach.status(finishedEnds, now)
        val oldest = done.minOfOrNull { (s, _) -> s.execution.startedAt }?.toLocalDateTime(zone)?.date
        val today = now.toLocalDateTime(zone).date
        return HistoryState(
            loaded = true,
            sessions = done.map { (s, t) -> sessionRow(s, t, labels) },
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
            totalSessions = done.size,
            longestRunMeters = longest?.second?.distanceMeters,
            longestRunMillis = longest?.first?.execution?.startedAt?.toEpochMilliseconds(),
            fullOctopuses = poulpy.fullOctopuses,
            tentaclesLit = poulpy.tentaclesLit,
            firstMonthOffset = oldest?.let { -((today.year - it.year) * 12 + (today.month.number - it.month.number)) }?.coerceAtMost(0) ?: 0,
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
            type == WorkoutType.TREADMILL -> compiler.compile(LiveModel.demoIntervals)
            else -> null
        }
        val totals = sessionTotals(events, timeline)
        store.saveDetails(id, session.title ?: workout?.name, type, totals)
        return totals
    }

    /** Free runs and the demo intervals are the only sessions that follow no saved workout. */
    private fun isTreadmill(session: StoredSession, events: List<SessionEvent>) =
        session.execution.workout == null || events.any { it is SessionEvent.Sample }

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

/** A finished session as the history and Home show it. */
internal fun sessionRow(session: StoredSession, totals: SessionTotals, labels: Labels): SessionRow {
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
