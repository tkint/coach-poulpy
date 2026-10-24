package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutType
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
    val type: WorkoutType,
    /** "Strength · 5 exercises · about 40 min". */
    val details: String,
    val note: LibraryNote,
    /** The planned day at midnight for [LibraryNote.PLANNED], when it was last done for [LibraryNote.LAST_DONE]. */
    val noteMillis: Long?,
    /** When it was last done, for sorting; null if never. */
    val lastDoneMillis: Long?,
)

/** A kind of workout the list can be narrowed to, only offered when a saved workout has it. */
data class KindFilter(val type: WorkoutType, val label: String)

enum class LibrarySort { RECENT, ALPHABETICAL }

data class LibraryState(
    val loaded: Boolean = false,
    val rows: List<LibraryRow> = emptyList(),
    val kinds: List<KindFilter> = emptyList(),
)

/** The workouts saved on this phone, with or without a day. */
class LibraryModel internal constructor(
    store: LocalStore,
    compiler: TimelineCompiler,
    catalog: Catalog,
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

    /** Rows matching [query] (in the name) and [type], most recently done first or by name. */
    fun filter(state: LibraryState, query: String = "", type: WorkoutType? = null, sort: LibrarySort = LibrarySort.RECENT): List<LibraryRow> {
        val needle = query.trim().lowercase()
        val rows = state.rows
            .filter { needle.isEmpty() || needle in it.name.lowercase() }
            .filter { type == null || it.type == type }
        return when (sort) {
            LibrarySort.ALPHABETICAL -> rows.sortedBy { it.name.lowercase() }
            LibrarySort.RECENT -> rows.sortedWith(compareByDescending<LibraryRow> { it.lastDoneMillis ?: Long.MIN_VALUE }.thenBy { it.name.lowercase() })
        }
    }

    private fun build(workouts: List<Workout>, plans: List<PlannedWorkout>, sessions: List<StoredSession>): LibraryState {
        val today = clock.now().toLocalDateTime(zone).date
        val lastDone = sessions.filter { it.counts }
            .mapNotNull { session -> session.execution.workout?.workoutId?.let { it to session.execution.startedAt } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, starts) -> starts.max() }
        val rows = workouts.map { workout ->
            val last = lastDone[workout.id]
            val doneToday = last?.toLocalDateTime(zone)?.date == today
            // A plan for today that was already done is not shown again.
            val plan = plans.firstOrNull { it.workoutId == workout.id && (it.day > today || (it.day == today && !doneToday)) }
            val (note, noteMillis) = when {
                plan != null -> LibraryNote.PLANNED to plan.day.startMillis(zone)
                doneToday -> LibraryNote.DONE_TODAY to last?.toEpochMilliseconds()
                last != null -> LibraryNote.LAST_DONE to last.toEpochMilliseconds()
                else -> LibraryNote.NOT_DONE_YET to null
            }
            LibraryRow(workout.id.value, workout.name, workout.type, facts.details(workout), note, noteMillis, last?.toEpochMilliseconds())
        }
        val kinds = workouts.map { it.type }.distinct().sortedBy { it.ordinal }.map { KindFilter(it, labels.workoutType(it)) }
        return LibraryState(loaded = true, rows = rows, kinds = kinds)
    }
}
