package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.PlannedWorkout
import ch.coachpoulpy.data.local.StoredSession
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** The small line under a workout in the library, by what matters most: a plan, then the last time. */
enum class LibraryNote { PLANNED, DONE_TODAY, LAST_DONE, NOT_DONE_YET }

data class LibraryRow(
    val id: String,
    val name: String,
    val format: WorkoutFormat,
    /** "Sets · 5 exercises · about 40 min". */
    val details: String,
    val note: LibraryNote,
    /** The planned day at midnight for [LibraryNote.PLANNED], when it was last done for [LibraryNote.LAST_DONE]. */
    val noteMillis: Long?,
    /** When it was last done, for sorting; null if never. */
    val lastDoneMillis: Long?,
    /** The next planned day at midnight, for sorting; null if none is coming. */
    val nextPlannedMillis: Long? = null,
    /** How many more planned days follow the next one: the "+2" after it. */
    val morePlanned: Int = 0,
    /** Every upcoming planned day, soonest first, for the plan sheet. */
    val plans: List<PlannedDay> = emptyList(),
    /** Only workouts made with the builder can be copied. */
    val editable: Boolean = false,
    val deletePrompt: DeletePrompt = DeletePrompt("", ""),
)

/** A format the list can be narrowed to; both are offered, like the exercise list's muscles. */
data class KindFilter(val format: WorkoutFormat, val label: String)

/** Recently done first, the soonest planned day first (unplanned after), or by name. */
enum class LibrarySort { RECENT, NEXT_PLANNED, ALPHABETICAL }

data class LibraryState(
    val loaded: Boolean = false,
    val rows: List<LibraryRow> = emptyList(),
    val kinds: List<KindFilter> = emptyList(),
    /** What [LibraryModel.filter] searches; a new one each time, so an edited workout always reaches the state. */
    val search: WorkoutSearch = WorkoutSearch.EMPTY,
)

/** The workouts as shown, with the count line over them: "7 workouts", or "2 matches" while searching. */
data class WorkoutListing(val rows: List<LibraryRow>, val countLabel: String, val searching: Boolean)

/** The workouts saved on this phone, with or without a day. */
class LibraryModel internal constructor(
    private val store: LocalStore,
    compiler: TimelineCompiler,
    private val catalog: Catalog,
    languageCode: String,
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val labels = Labels(languageCode)
    private val facts = WorkoutFacts(compiler, catalog, labels)

    val state: StateFlow<LibraryState> =
        combine(store.workouts(), store.plannedWorkouts(), store.finishedSessions(), ::build)
            .stateIn(scope, SharingStarted.Eagerly, LibraryState())

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: LibraryState get() = state.value

    fun watch(onChange: (LibraryState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun close() = scope.cancel()

    /** Plans [row]'s workout on exactly [days] from today on, as ticked on the plan sheet. */
    fun setDays(row: LibraryRow, days: List<PlanDate>) =
        store.setDays(WorkoutId(row.id), row.plans, days, clock.now().toLocalDateTime(zone).date)

    fun delete(row: LibraryRow) = store.deleteWorkout(WorkoutId(row.id))

    /**
     * Rows of [format] matching [query] ([WorkoutSearch]: the name, the format, or an exercise
     * inside). While searching, the best matches come first and [sort] orders the ties.
     */
    fun filter(state: LibraryState, query: String = "", format: WorkoutFormat? = null, sort: LibrarySort = LibrarySort.RECENT): WorkoutListing {
        val ranks = state.search.rank(query)
        val order = when (sort) {
            LibrarySort.ALPHABETICAL -> compareBy<LibraryRow> { SearchText.fold(it.name) }
            LibrarySort.RECENT -> compareByDescending<LibraryRow> { it.lastDoneMillis ?: Long.MIN_VALUE }.thenBy { SearchText.fold(it.name) }
            LibrarySort.NEXT_PLANNED -> compareBy<LibraryRow> { it.nextPlannedMillis ?: Long.MAX_VALUE }
                .thenByDescending { it.lastDoneMillis ?: Long.MIN_VALUE }
                .thenBy { SearchText.fold(it.name) }
        }
        val rows = state.rows
            .filter { format == null || it.format == format }
            .filter { ranks == null || it.id in ranks }
            .sortedWith(if (ranks == null) order else compareBy<LibraryRow> { ranks.getValue(it.id) }.then(order))
        val countLabel = if (ranks == null) labels.workouts(rows.size) else labels.matches(rows.size)
        return WorkoutListing(rows, countLabel, searching = ranks != null)
    }

    private fun build(workouts: List<Workout>, plans: List<PlannedWorkout>, sessions: List<StoredSession>): LibraryState {
        val today = clock.now().toLocalDateTime(zone).date
        val timesDone = sessions.filter { it.counts }.mapNotNull { it.execution.workout?.workoutId }.groupingBy { it }.eachCount()
        val lastDone = sessions.filter { it.counts }
            .mapNotNull { session -> session.execution.workout?.workoutId?.let { it to session.execution.startedAt } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, starts) -> starts.max() }
        val rows = workouts.map { workout ->
            val last = lastDone[workout.id]
            val doneToday = last?.toLocalDateTime(zone)?.date == today
            val upcoming = upcomingDays(plans, workout.id, today, doneToday, zone)
            val plan = upcoming.firstOrNull()
            val (note, noteMillis) = when {
                plan != null -> LibraryNote.PLANNED to plan.dayMillis
                doneToday -> LibraryNote.DONE_TODAY to last?.toEpochMilliseconds()
                last != null -> LibraryNote.LAST_DONE to last.toEpochMilliseconds()
                else -> LibraryNote.NOT_DONE_YET to null
            }
            LibraryRow(
                workout.id.value, workout.name, workout.format, facts.details(workout), note, noteMillis, last?.toEpochMilliseconds(),
                nextPlannedMillis = plan?.dayMillis,
                morePlanned = (upcoming.size - 1).coerceAtLeast(0),
                plans = upcoming,
                editable = WorkoutDraft.from(workout, catalog) != null,
                deletePrompt = labels.deletePrompt(workout.name, upcoming.size, timesDone[workout.id] ?: 0),
            )
        }
        val kinds = WorkoutFormat.entries.map { KindFilter(it, labels.format(it)) }
        return LibraryState(loaded = true, rows = rows, kinds = kinds, search = WorkoutSearch.of(workouts, catalog))
    }
}
