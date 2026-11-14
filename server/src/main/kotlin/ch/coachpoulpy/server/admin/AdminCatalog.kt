package ch.coachpoulpy.server.admin

import ch.coachpoulpy.core.model.BodyPosition
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.catalog.CatalogChanges
import ch.coachpoulpy.server.catalog.DraftChange
import ch.coachpoulpy.server.catalog.DraftExercise
import ch.coachpoulpy.server.catalog.DraftResult
import ch.coachpoulpy.server.catalog.PublishResult
import ch.coachpoulpy.server.catalog.ServerCatalog
import ch.coachpoulpy.server.catalog.missingLanguages
import org.http4k.events.Events

/** The columns the admin's exercise table sorts on. Without a sort the table follows the catalog's own order. */
enum class ExerciseSort { Name, Pattern, Languages, Change, Status }

/** One sort of the table: a column, ascending or descending. A list of them sorts by the first, then the second among its ties, and so on. */
data class ExerciseOrder(val column: ExerciseSort, val ascending: Boolean = true)

/** Where an exercise stands against the latest published version, apart from its lifecycle and its translations. */
enum class ContentState {
    /** Never published: a new exercise. */
    Draft,

    /** Published, with edits the next publication would carry. */
    Modified,

    /** The draft holds what the latest version holds, or only a lifecycle change. */
    Published,

    /** Anything the next publication would change: a new exercise, an edit, a retirement or an offer again. */
    Pending,
}

/** Whether an exercise is offered, about to be retired by the next publication, or already retired in the latest version. */
enum class Lifecycle { Active, RetirementPending, Retired }

/** What the admin asked of the exercise table: filters, one sort and a page. A null or empty filter lets everything through. */
data class ExerciseQuery(
    /** Part of the slug, of a name in any language or of an alias. */
    val text: String? = null,
    val pattern: MovementPattern? = null,
    /** An exercise that works any of these muscle groups, any of these positions, or needs any of these pieces of equipment. */
    val muscles: Set<MuscleGroup> = emptySet(),
    val positions: Set<BodyPosition> = emptySet(),
    val equipment: Set<Equipment> = emptySet(),
    /** True: every shipped language is written; false: some language is missing. */
    val translationComplete: Boolean? = null,
    val content: ContentState? = null,
    val lifecycle: Lifecycle? = null,
    /** Empty for the catalog's order, which also settles every tie. */
    val sort: List<ExerciseOrder> = emptyList(),
    val page: Int = 1,
    val size: Int = DEFAULT_PAGE_SIZE,
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}

/** The exercise's [ContentState] when it is [content]; [ContentState.Pending] holds whatever [DraftExercise.change] names. */
private fun DraftExercise.inState(content: ContentState): Boolean = when (content) {
    ContentState.Draft -> !published
    ContentState.Modified -> change == DraftChange.Edited
    ContentState.Published -> published && (change == null || change == DraftChange.Retired || change == DraftChange.Offered)
    ContentState.Pending -> change != null
}

/** An exercise retired in the draft but not yet in the latest version is only about to be: its retirement is pending. */
private fun DraftExercise.lifecycle(): Lifecycle = when {
    !exercise.retired -> Lifecycle.Active
    change == DraftChange.Retired -> Lifecycle.RetirementPending
    else -> Lifecycle.Retired
}

/** One page of exercises, and how many match the filters in all. */
data class ExercisePage(val exercises: List<DraftExercise>, val total: Int)

/** Filters, sorts and cuts [all], given in the catalog's order, as [query] says; ties after every sort keep the catalog's order. */
fun exercisePageOf(all: List<DraftExercise>, query: ExerciseQuery): ExercisePage {
    val needle = query.text?.trim()?.lowercase().orEmpty()
    val matching = all.withIndex().filter { (_, draft) ->
        val exercise = draft.exercise
        (query.pattern == null || exercise.movementPattern == query.pattern) &&
            (query.muscles.isEmpty() || exercise.muscleGroups.any { group -> group in query.muscles }) &&
            (query.positions.isEmpty() || exercise.positions.any { position -> position in query.positions }) &&
            (query.equipment.isEmpty() || exercise.allEquipment.any { piece -> piece in query.equipment }) &&
            (query.translationComplete == null || missingLanguages(exercise).isEmpty() == query.translationComplete) &&
            (query.content == null || draft.inState(query.content)) &&
            (query.lifecycle == null || draft.lifecycle() == query.lifecycle) &&
            (needle.isEmpty() || (listOf(exercise.slug.value) + exercise.name.all + exercise.aliases.all).any { text -> text.lowercase().contains(needle) })
    }
    fun comparator(column: ExerciseSort): Comparator<IndexedValue<DraftExercise>> = when (column) {
        ExerciseSort.Name -> compareBy { it.value.exercise.name.byLanguage.getValue(LocalizedText.ENGLISH).lowercase() }
        ExerciseSort.Pattern -> compareBy { it.value.exercise.movementPattern.name }
        // Missing languages first when ascending, so what blocks publishing is found at once.
        ExerciseSort.Languages -> compareBy { -missingLanguages(it.value.exercise).size }
        // The changed first when ascending.
        ExerciseSort.Change -> compareBy { it.value.change?.ordinal ?: Int.MAX_VALUE }
        ExerciseSort.Status -> compareBy { it.value.lifecycle() }
    }
    val order = query.sort.fold(compareBy<IndexedValue<DraftExercise>> { 0 }) { chain, sort ->
        chain.then(comparator(sort.column).let { if (sort.ascending) it else it.reversed() })
    }
    val sorted = matching.sortedWith(order.thenBy { it.index })
    val size = query.size.coerceIn(1, ExerciseQuery.MAX_PAGE_SIZE)
    return ExercisePage(sorted.drop((query.page.coerceAtLeast(1) - 1) * size).take(size).map { it.value }, matching.size)
}

/**
 * The admin's side of the catalog (#337): the draft's exercises, created, edited, retired and offered
 * again, and publishing. Each write emits an [AdminAction] whose target is the exercise's slug, or the
 * version number for a publish.
 */
class AdminCatalog(private val catalog: ServerCatalog, private val events: Events) {
    fun list(query: ExerciseQuery): ExercisePage = exercisePageOf(catalog.draft(), query)

    fun get(slug: ExerciseSlug): DraftExercise? = catalog.get(slug)

    fun create(actor: AccountId, exercise: Exercise): DraftResult = catalog.create(exercise).also {
        if (it is DraftResult.Saved) events(AdminAction(actor, "admin.catalog.create", exercise.slug.value))
    }

    /** Edits the exercise [slug]; a rename is written as `old -> new`. */
    fun edit(actor: AccountId, slug: ExerciseSlug, exercise: Exercise): DraftResult = catalog.edit(slug, exercise).also {
        val target = if (exercise.slug == slug) slug.value else "${slug.value} -> ${exercise.slug.value}"
        if (it is DraftResult.Saved) events(AdminAction(actor, "admin.catalog.edit", target))
    }

    fun retire(actor: AccountId, slug: ExerciseSlug): DraftResult = catalog.retire(slug).also {
        if (it is DraftResult.Saved) events(AdminAction(actor, "admin.catalog.retire", slug.value))
    }

    fun offer(actor: AccountId, slug: ExerciseSlug): DraftResult = catalog.offer(slug).also {
        if (it is DraftResult.Saved) events(AdminAction(actor, "admin.catalog.offer", slug.value))
    }

    fun changes(): CatalogChanges = catalog.changes()

    fun publish(actor: AccountId): PublishResult = catalog.publish().also {
        if (it is PublishResult.Published) events(AdminAction(actor, "admin.catalog.publish", it.version.toString()))
    }
}
