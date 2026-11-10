package ch.coachpoulpy.server.admin

import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.catalog.CatalogChanges
import ch.coachpoulpy.server.catalog.DraftExercise
import ch.coachpoulpy.server.catalog.DraftResult
import ch.coachpoulpy.server.catalog.PublishResult
import ch.coachpoulpy.server.catalog.ServerCatalog
import org.http4k.events.Events

/** What the admin asked of the exercise list: part of a slug, name or alias, and retired or not. A null filter lets everything through. */
data class ExerciseQuery(val text: String? = null, val retired: Boolean? = null)

/** The exercises of [all] that [query] keeps, in the catalog's order. */
fun exercisesMatching(all: List<DraftExercise>, query: ExerciseQuery): List<DraftExercise> {
    val needle = query.text?.trim()?.lowercase().orEmpty()
    return all.filter { (exercise) ->
        (query.retired == null || exercise.retired == query.retired) &&
            (needle.isEmpty() || (listOf(exercise.slug.value) + exercise.name.all + exercise.aliases.all).any { it.lowercase().contains(needle) })
    }
}

/**
 * The admin's side of the catalog (#337): the draft's exercises, created, edited and retired, and
 * publishing. Each write emits an [AdminAction] whose target is the exercise's slug, or the version
 * number for a publish.
 */
class AdminCatalog(private val catalog: ServerCatalog, private val events: Events) {
    fun list(query: ExerciseQuery): List<DraftExercise> = exercisesMatching(catalog.draft(), query)

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

    fun changes(): CatalogChanges = catalog.changes()

    fun publish(actor: AccountId): PublishResult = catalog.publish().also {
        if (it is PublishResult.Published) events(AdminAction(actor, "admin.catalog.publish", it.version.toString()))
    }
}
