package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId

/** One workout in the list, with the line the library shows under its name. */
data class WorkoutSummary(
    val id: String,
    val name: String,
    val format: WorkoutFormat,
    /** "Sets", "Circuit", in the language asked for. */
    val formatLabel: String,
    /** "Sets · 5 exercises · about 40 min". */
    val details: String,
    val exerciseCount: Int,
    /** "5 exercises". */
    val exercisesLabel: String,
    val minutes: Int,
    /** "about 40 min", or the exact length when every phase is timed. */
    val lengthLabel: String,
    val timesDone: Int,
    val lastDoneMillis: Long?,
)

/** How the list is ordered: recently done first (never done last), or by name. */
enum class WorkoutOrder { RECENT, NAME }

/** One page of the filtered, ordered list, with how many workouts matched in all. */
data class WorkoutsPage(
    val items: List<WorkoutSummary>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    /** "7 workouts", or "2 matches" while searching. */
    val countLabel: String,
)

/**
 * What the website says about saved workouts, the words and the muscle heat decided here once, the
 * same as on the phone ([LibraryModel], [WorkoutPageModel]): the server answers with it, so the
 * website holds no workout wording of its own.
 */
// CLEANUP: the server depends on this whole module for this one class and the wording behind it
// (Labels, WorkoutFacts, BodyMap); move that wording into its own module the phone and the server
// both depend on, so the server stops carrying the phone's database and Bluetooth code.
class WorkoutViews(private val catalog: Catalog, private val compiler: TimelineCompiler = TimelineCompiler()) {
    /**
     * [workouts] narrowed to [format] and to what [query] finds ([WorkoutSearch]: the name, the
     * format, or an exercise inside), ordered by [order] (the best matches first while searching),
     * and cut to the [page] of [pageSize]. [history] holds the past sessions of each workout.
     */
    fun list(
        workouts: List<Workout>,
        history: Map<WorkoutId, WorkoutHistory>,
        language: String,
        query: String = "",
        format: WorkoutFormat? = null,
        order: WorkoutOrder = WorkoutOrder.RECENT,
        page: Int = 1,
        pageSize: Int = 25,
    ): WorkoutsPage {
        val labels = Labels(language)
        val facts = WorkoutFacts(compiler, catalog, labels)
        val rows = workouts.map { workout ->
            val past = history[workout.id] ?: WorkoutHistory()
            WorkoutSummary(
                id = workout.id.value,
                name = workout.name,
                format = workout.format,
                formatLabel = labels.format(workout.format),
                details = facts.details(workout),
                exerciseCount = exerciseSteps(workout).size,
                exercisesLabel = labels.exercises(exerciseSteps(workout).size),
                minutes = facts.minutes(workout),
                lengthLabel = facts.length(workout),
                timesDone = past.startedAt.size,
                lastDoneMillis = past.startedAt.maxOrNull()?.toEpochMilliseconds(),
            )
        }
        val ranks = WorkoutSearch.of(workouts, catalog).rank(query)
        val byOrder = when (order) {
            WorkoutOrder.NAME -> compareBy<WorkoutSummary> { SearchText.fold(it.name) }
            WorkoutOrder.RECENT -> compareByDescending<WorkoutSummary> { it.lastDoneMillis ?: Long.MIN_VALUE }.thenBy { SearchText.fold(it.name) }
        }
        val matching = rows
            .filter { format == null || it.format == format }
            .filter { ranks == null || it.id in ranks }
            .sortedWith(if (ranks == null) byOrder else compareBy<WorkoutSummary> { ranks.getValue(it.id) }.then(byOrder))
        val size = pageSize.coerceAtLeast(1)
        val shown = matching.drop((page.coerceAtLeast(1) - 1) * size).take(size)
        val count = if (ranks == null) labels.workouts(matching.size) else labels.matches(matching.size)
        return WorkoutsPage(shown, matching.size, page.coerceAtLeast(1), size, count)
    }

    /** The workout's page: its parts and steps in words, how it went before, and the muscles it works. */
    fun detail(workout: Workout, history: WorkoutHistory, language: String): WorkoutPageState {
        val labels = Labels(language)
        return WorkoutPageBuilder(catalog, labels, WorkoutFacts(compiler, catalog, labels)).state(workout, history)
    }
}
