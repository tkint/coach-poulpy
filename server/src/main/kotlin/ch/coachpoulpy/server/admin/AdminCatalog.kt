package ch.coachpoulpy.server.admin

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.catalog.CatalogChanges
import ch.coachpoulpy.server.catalog.DraftExercise
import ch.coachpoulpy.server.catalog.DraftResult
import ch.coachpoulpy.server.catalog.PublishResult
import ch.coachpoulpy.server.catalog.ServerCatalog
import ch.coachpoulpy.server.catalog.missingLanguages
import org.http4k.events.Events

/** The columns the admin's exercise table sorts on; [Catalog] is the catalog's own order. */
enum class ExerciseSort { Catalog, Name, Pattern, Languages, Change, Status }

/** What the admin asked of the exercise table: filters, one sort and a page. A null filter lets everything through. */
data class ExerciseQuery(
    /** Part of the slug, of a name in any language or of an alias. */
    val text: String? = null,
    val retired: Boolean? = null,
    val sort: ExerciseSort = ExerciseSort.Catalog,
    val ascending: Boolean = true,
    val page: Int = 1,
    val size: Int = DEFAULT_PAGE_SIZE,
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}

/** One page of exercises, and how many match the filters in all. */
data class ExercisePage(val exercises: List<DraftExercise>, val total: Int)

/** Filters, sorts and cuts [all], given in the catalog's order, as [query] says; ties keep the catalog's order. */
fun exercisePageOf(all: List<DraftExercise>, query: ExerciseQuery): ExercisePage {
    val needle = query.text?.trim()?.lowercase().orEmpty()
    val matching = all.withIndex().filter { (_, it) ->
        val exercise = it.exercise
        (query.retired == null || exercise.retired == query.retired) &&
            (needle.isEmpty() || (listOf(exercise.slug.value) + exercise.name.all + exercise.aliases.all).any { text -> text.lowercase().contains(needle) })
    }
    val key: Comparator<IndexedValue<DraftExercise>> = when (query.sort) {
        ExerciseSort.Catalog -> compareBy { it.index }
        ExerciseSort.Name -> compareBy { it.value.exercise.name.byLanguage.getValue(LocalizedText.ENGLISH).lowercase() }
        ExerciseSort.Pattern -> compareBy { it.value.exercise.movementPattern }
        // Missing languages first when ascending, so what blocks publishing is found at once.
        ExerciseSort.Languages -> compareBy { -missingLanguages(it.value.exercise).size }
        ExerciseSort.Change -> compareBy { it.value.change?.ordinal ?: Int.MAX_VALUE }
        ExerciseSort.Status -> compareBy { it.value.exercise.retired }
    }
    val sorted = matching.sortedWith((if (query.ascending) key else key.reversed()).thenBy { it.index })
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
