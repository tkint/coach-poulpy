package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.data.local.LocalStore
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** The period switch on You's Statistics. It sets every block. */
enum class StatsPeriod { DAYS_7, DAYS_30, YEAR, ALL_TIME }

/** A block of You's Statistics the person can switch off and reorder. Blocks are listed in their default order. */
enum class StatBlock(val key: String) {
    /** The Sessions card: how many, how long and how far. */
    SESSIONS_AND_TIME("sessions"),

    /** The Weight card, the longest run and the full octopuses. */
    RECORDS_AND_WEIGHT("records"),
}

/** Which blocks You shows and in what order, kept on the phone. */
data class StatBlockChoice(val block: StatBlock, val shown: Boolean)

/** What the sessions of one period add up to. A session counts on the day it started. */
data class PeriodStats(
    val sessions: Int,
    val activeMinutes: Int,
    val distanceMeters: Double,
    /** The longest distance of one session in the period, and when it started; null without any. */
    val longestRunMeters: Double?,
    val longestRunMillis: Long?,
) {
    companion object {
        /** The first day [period] covers, ending today: 7 days, 30 days or one year counting today; null for all time. */
        fun firstDay(period: StatsPeriod, today: LocalDate): LocalDate? = when (period) {
            StatsPeriod.DAYS_7 -> today.minus(DatePeriod(days = 6))
            StatsPeriod.DAYS_30 -> today.minus(DatePeriod(days = 29))
            StatsPeriod.YEAR -> today.minus(DatePeriod(years = 1)).plus(DatePeriod(days = 1))
            StatsPeriod.ALL_TIME -> null
        }

        fun of(rows: List<SessionRow>, period: StatsPeriod, now: Instant, zone: TimeZone): PeriodStats {
            val today = now.toLocalDateTime(zone).date
            val first = firstDay(period, today)
            val inPeriod = rows.filter {
                val day = Instant.fromEpochMilliseconds(it.startedAtMillis).toLocalDateTime(zone).date
                day <= today && (first == null || day >= first)
            }
            val longest = inPeriod.filter { (it.distanceMeters ?: 0.0) > 0 }.maxByOrNull { it.distanceMeters!! }
            return PeriodStats(
                sessions = inPeriod.size,
                activeMinutes = (inPeriod.sumOf { it.activeSeconds } / 60).toInt(),
                distanceMeters = inPeriod.sumOf { it.distanceMeters ?: 0.0 },
                longestRunMeters = longest?.distanceMeters,
                longestRunMillis = longest?.startedAtMillis,
            )
        }
    }
}

/** The latest logged weight in kg, and how many whole days ago it was logged. */
data class WeightNow(val kilograms: Double, val daysAgo: Int)

/** Everything You's Statistics shows. */
data class YouState(
    val loaded: Boolean = false,
    val period: StatsPeriod = StatsPeriod.DAYS_30,
    val stats: PeriodStats = PeriodStats(0, 0, 0.0, null, null),
    val weight: WeightNow? = null,
    /** Poulpy's tentacles over all time, not the period. */
    val fullOctopuses: Int = 0,
    val tentaclesLit: Int = 0,
    /** Every block in the person's order, with whether it shows. */
    val blocks: List<StatBlockChoice> = StatBlocks.DEFAULT,
)

/** The order and visibility of the statistics blocks, as one string in the phone's memory. */
object StatBlocks {
    internal const val KEY = "you.statBlocks"
    val DEFAULT: List<StatBlockChoice> = StatBlock.entries.map { StatBlockChoice(it, true) }

    /** Reads "records:0,sessions:1": unknown blocks are dropped and blocks missing from the text are added last, shown. */
    fun parse(text: String?): List<StatBlockChoice> {
        val saved = text.orEmpty().split(',').mapNotNull { part ->
            val (key, flag) = part.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
            StatBlock.entries.firstOrNull { it.key == key }?.let { StatBlockChoice(it, flag != "0") }
        }.distinctBy { it.block }
        return saved + StatBlock.entries.filter { block -> saved.none { it.block == block } }.map { StatBlockChoice(it, true) }
    }

    fun write(blocks: List<StatBlockChoice>): String = blocks.joinToString(",") { "${it.block.key}:${if (it.shown) 1 else 0}" }

    /** [blocks] with [block] moved to [to] (0 first); a position past either end goes to that end. */
    fun move(blocks: List<StatBlockChoice>, block: StatBlock, to: Int): List<StatBlockChoice> {
        val picked = blocks.firstOrNull { it.block == block } ?: return blocks
        val rest = blocks - picked
        return rest.toMutableList().apply { add(to.coerceIn(0, rest.size), picked) }
    }

    fun show(blocks: List<StatBlockChoice>, block: StatBlock, shown: Boolean): List<StatBlockChoice> =
        blocks.map { if (it.block == block) it.copy(shown = shown) else it }
}

/**
 * You's Statistics: the sessions of the chosen period, the latest weight and Poulpy's tentacles, and which blocks
 * show and in what order. The numbers are in metres and kilograms; the apps format them with the person's units.
 */
class YouModel internal constructor(
    private val history: HistoryModel,
    store: LocalStore,
    private val memory: KeyValueStore,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val period = MutableStateFlow(StatsPeriod.DAYS_30)
    private val blocks = MutableStateFlow(StatBlocks.parse(memory.getString(StatBlocks.KEY)))

    val state: StateFlow<YouState> = combine(history.state, store.bodyMeasurements(BodyMetric.WEIGHT), period, blocks) { past, weights, period, blocks ->
        val now = clock.now()
        val today = clock.todayIn(zone)
        val latest = weights.maxByOrNull { it.day }
        YouState(
            loaded = past.loaded,
            period = period,
            stats = PeriodStats.of(past.sessions, period, now, zone),
            weight = latest?.let { WeightNow(it.value, latest.day.daysUntil(today).coerceAtLeast(0)) },
            fullOctopuses = past.fullOctopuses,
            tentaclesLit = past.tentaclesLit,
            blocks = blocks,
        )
    }.stateIn(scope, SharingStarted.Eagerly, YouState(blocks = blocks.value))

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: YouState get() = state.value

    fun watch(onChange: (YouState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun selectPeriod(selected: StatsPeriod) {
        period.value = selected
    }

    fun setShown(block: StatBlock, shown: Boolean) = save(StatBlocks.show(blocks.value, block, shown))

    /** Moves [block] to [to], 0 first. */
    fun move(block: StatBlock, to: Int) = save(StatBlocks.move(blocks.value, block, to))

    private fun save(updated: List<StatBlockChoice>) {
        memory.putString(StatBlocks.KEY, StatBlocks.write(updated))
        blocks.value = updated
    }

    fun close() {
        history.close()
        scope.cancel()
    }
}
