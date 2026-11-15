package ch.coachpoulpy.server.catalog

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.server.db.Database
import java.time.Clock
import java.time.OffsetDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** What a draft exercise has become since its latest published revision; null when it is the same. */
enum class DraftChange(val code: String) { Added("added"), Edited("edited"), Retired("retired"), Offered("offered") }

/**
 * One field an edit changed, from the latest published revision ([before]) to the draft ([after]); null where
 * the field is absent. [field] is a path into the exercise JSON, one text per language and instruction:
 * `name.fr`, `instructions.1.en`, `variationNote.en`, `aliases.fr`, `muscleGroups`, `equipment`.
 */
data class FieldChange(val field: String, val before: JsonElement?, val after: JsonElement?)

/** An edited exercise of the draft, and what changed in it. */
data class EditedExercise(val exercise: Exercise, val fields: List<FieldChange>)

/** One exercise of the draft as the admin sees it: whether a revision of it is published, and what changed since. */
data class DraftExercise(val exercise: Exercise, val published: Boolean, val change: DraftChange?)

/** An exercise that can't be published yet, and the shipped languages some of its text lacks. */
data class MissingText(val exercise: Exercise, val languages: List<String>)

/**
 * What publishing now would do: the draft against each exercise's latest published revision.
 * [missing] lists what must be translated first.
 */
data class CatalogChanges(
    val added: List<Exercise>,
    val edited: List<EditedExercise>,
    val retired: List<Exercise>,
    /** Retired in the latest published revision and offered again in the draft. */
    val offered: List<Exercise>,
    val missing: List<MissingText>,
) {
    val isEmpty: Boolean get() = added.isEmpty() && edited.isEmpty() && retired.isEmpty() && offered.isEmpty()

    /** The slugs of every exercise with something to publish. */
    val pending: List<ExerciseSlug> get() = (added + edited.map { it.exercise } + retired + offered).map { it.slug }
}

/** What a write to the draft did, or why it was refused. */
sealed interface DraftResult {
    data class Saved(val exercise: DraftExercise) : DraftResult
    data object Unknown : DraftResult
    /** Another exercise already has the slug. */
    data object SlugTaken : DraftResult
    /** The slug of an exercise with a published revision never changes (ADR 0008). */
    data object SlugPublished : DraftResult
    data class Invalid(val reason: String) : DraftResult
}

/** What publishing did, or why it didn't. */
sealed interface PublishResult {
    /** The exercises that got a new published revision. */
    data class Published(val slugs: List<ExerciseSlug>) : PublishResult
    data object NothingChanged : PublishResult
    data class MissingTranslations(val missing: List<MissingText>) : PublishResult
}

/** [draft]'s change against [published], the same exercise's latest published revision, or null when it has none. */
fun changeOf(draft: Exercise, published: Exercise?): DraftChange? = when {
    published == null -> DraftChange.Added
    draft.retired && !published.retired -> DraftChange.Retired
    !draft.retired && published.retired -> DraftChange.Offered
    draft != published -> DraftChange.Edited
    else -> null
}

/**
 * The fields that differ between [before] and [after], in the order the exercise JSON names them. Texts
 * are compared per language and instruction; a list of plain values (muscles, equipment) is one field.
 * The retired state is not a field: retiring and offering again are changes of their own.
 */
fun fieldChanges(before: Exercise, after: Exercise): List<FieldChange> {
    val old = leaves(ServerCatalog.json.encodeToJsonElement(Exercise.serializer(), before))
    val new = leaves(ServerCatalog.json.encodeToJsonElement(Exercise.serializer(), after))
    return (new.keys + old.keys).distinct().filter { it != "retired" && old[it] != new[it] }.map { FieldChange(it, old[it], new[it]) }
}

/** [element] flattened to paths: objects and lists of objects open up, anything else is one leaf. */
private fun leaves(element: JsonElement, path: String = ""): Map<String, JsonElement> = when {
    element is JsonObject -> element.entries.fold(emptyMap()) { all, (key, value) -> all + leaves(value, join(path, key)) }
    element is JsonArray && element.any { it is JsonObject } ->
        element.foldIndexed(emptyMap()) { index, all, value -> all + leaves(value, join(path, index.toString())) }
    else -> mapOf(path to element)
}

private fun join(path: String, key: String) = if (path.isEmpty()) key else "$path.$key"

/** The languages of [required] that some text of [exercise] lacks or leaves blank: its name, an instruction or its variation note. */
fun missingLanguages(exercise: Exercise, required: List<String> = ServerCatalog.LANGUAGES): List<String> {
    val texts = listOf(exercise.name) + exercise.instructions + listOfNotNull(exercise.variationNote)
    return required.filter { language -> texts.any { it.byLanguage[language].isNullOrBlank() } }
}

/**
 * One exercise as the draft or its latest published revision holds it: its place in the catalog, and its variation group and
 * rank in it (easiest first), null when it has no group.
 */
private data class CatalogRow(val exercise: Exercise, val position: Int, val group: Int?, val rank: Int?)

/** [catalog] as rows: its order, and each exercise's variation group by the group's index. */
private fun rowsOf(catalog: Catalog): List<CatalogRow> {
    val groupOf = catalog.variationGroups.flatMapIndexed { group, slugs -> slugs.mapIndexed { rank, slug -> slug to (group to rank) } }.toMap()
    return catalog.exercises.mapIndexed { position, exercise ->
        val (group, rank) = groupOf[exercise.slug] ?: (null to null)
        CatalogRow(exercise, position, group, rank)
    }
}

/** The catalog made of [rows]: the exercises in order and the variation groups they name. */
private fun catalogOf(rows: List<CatalogRow>): Catalog {
    val groups = rows.filter { it.group != null }.groupBy { it.group!! }.toSortedMap().values
        .map { group -> group.sortedBy { it.rank }.map { it.exercise.slug } }
    return Catalog(rows.sortedBy { it.position }.map { it.exercise }, groups)
}

/** One published revision of an exercise as a phone receives it: its [revision], its [seq] in the publication stream, and its variation group and rank. */
data class PublishedRevision(val exercise: Exercise, val revision: Int, val seq: Long, val group: Int?, val rank: Int?)

/** What a phone gets for a cursor: the exercises published since it, and the cursor to echo next time. */
data class CatalogUpdate(val cursor: String, val exercises: List<PublishedRevision>)

/**
 * The exercise catalog on the server (#337, #437, ADR 0030): the exercises' published revisions, and the
 * draft revisions the admin edits. An exercise's revisions are numbered 1, 2, 3 and a published one never
 * changes. Publishing gives the draft revision of each named exercise the next number of the publication
 * cursor (`seq`) and writes nothing else. There is no catalog version: a phone asks for the exercises
 * published after the cursor it last received ([since]).
 */
class ServerCatalog(
    private val database: Database,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val queries get() = database.queries.catalogQueries

    /**
     * Fills an empty catalog from [catalog]: one revision per exercise, each published under its own
     * number of the publication cursor. Does nothing once an exercise is stored. Returns whether it seeded.
     * Only tests call it: the real catalog is made in the admin (ADR 0033).
     */
    fun seed(catalog: Catalog): Boolean = database.queries.transactionWithResult {
        queries.ensurePublication()
        if (queries.revisionCount().executeAsOne() > 0) return@transactionWithResult false
        val now = OffsetDateTime.now(clock)
        val rows = rowsOf(catalog)
        val last = queries.reserveSeqs(rows.size.toLong()).executeAsOne()
        rows.forEachIndexed { index, it ->
            queries.insertRevision(it.exercise.slug.value, 1, it.position, encode(it.exercise), it.group, it.rank, last - rows.size + 1 + index, now)
        }
        true
    }

    /** The exercises whose latest published revision came after [after] (all of them without it), or null when there is none. */
    fun since(after: Long?): CatalogUpdate? = database.queries.transactionWithResult {
        val rows = queries.publishedAfter(after ?: 0L).executeAsList().map {
            PublishedRevision(decode(it.content!!), it.revision, it.seq!!, it.variation_group, it.variation_rank)
        }
        if (rows.isEmpty()) null else CatalogUpdate(rows.maxOf { it.seq }.toString(), rows)
    }

    /** The latest published revision of each exercise, as a catalog; empty before anything is published. */
    fun published(): Catalog = catalogOf(publishedRows())

    /** The draft, in the catalog's order, retired exercises included. */
    fun draft(): List<DraftExercise> {
        val published = published()
        return draftExercises().map { view(it, published) }
    }

    /** One exercise of the draft, or null. */
    fun get(slug: ExerciseSlug): DraftExercise? = draft().firstOrNull { it.exercise.slug == slug }

    /** Adds [exercise] to the draft, offered (not retired), after the others. */
    fun create(exercise: Exercise): DraftResult {
        invalidity(exercise)?.let { return DraftResult.Invalid(it) }
        val added = exercise.copy(retired = false)
        return database.queries.transactionWithResult {
            val draft = draftRows()
            if (draft.any { it.exercise.slug == added.slug }) return@transactionWithResult DraftResult.SlugTaken
            val position = (draft.maxOfOrNull { it.position } ?: -1) + 1
            queries.insertRevision(added.slug.value, 1, position, encode(added), null, null, null, OffsetDateTime.now(clock))
            DraftResult.Saved(view(added, published()))
        }
    }

    /**
     * Replaces the exercise [slug] with [exercise], which keeps its retired state ([retire] and [offer] change it).
     * A different slug in [exercise] renames it, only while it has no published revision.
     */
    fun edit(slug: ExerciseSlug, exercise: Exercise): DraftResult = database.queries.transactionWithResult {
        val draft = draftRows()
        val stored = draft.firstOrNull { it.exercise.slug == slug } ?: return@transactionWithResult DraftResult.Unknown
        invalidity(exercise)?.let { return@transactionWithResult DraftResult.Invalid(it) }
        val published = published()
        if (exercise.slug != slug) {
            if (published[slug] != null) return@transactionWithResult DraftResult.SlugPublished
            if (draft.any { it.exercise.slug == exercise.slug }) return@transactionWithResult DraftResult.SlugTaken
        }
        val edited = exercise.copy(retired = stored.exercise.retired)
        save(stored, edited)
        DraftResult.Saved(view(edited, published))
    }

    /** Offers a retired exercise again in the draft: its next published revision lists it for browsing and building again. */
    fun offer(slug: ExerciseSlug): DraftResult = setRetired(slug, false)

    /** Retires the exercise [slug] in the draft: it stays in the catalog, hidden, so workouts that use it still open. */
    fun retire(slug: ExerciseSlug): DraftResult = setRetired(slug, true)

    /**
     * Deletes the exercise [slug] for good, published revisions included (beta): the next published catalog no longer holds it,
     * though a phone that already downloaded it keeps its copy. Returns whether it existed.
     */
    fun delete(slug: ExerciseSlug): Boolean = database.queries.transactionWithResult {
        if (draftRows().none { it.exercise.slug == slug }) return@transactionWithResult false
        queries.deleteExercise(slug.value)
        true
    }

    private fun setRetired(slug: ExerciseSlug, retired: Boolean): DraftResult = database.queries.transactionWithResult {
        val stored = draftRows().firstOrNull { it.exercise.slug == slug } ?: return@transactionWithResult DraftResult.Unknown
        val changed = stored.exercise.copy(retired = retired)
        if (stored.exercise.retired != retired) save(stored, changed)
        DraftResult.Saved(view(changed, published()))
    }

    /**
     * Writes [exercise] as the draft revision of [stored]'s exercise (renamed when its slug differs):
     * changes the draft revision, or starts one after the latest published revision. A draft that comes
     * back to what the latest published revision holds is dropped, so publishing writes nothing for it.
     */
    private fun save(stored: CatalogRow, exercise: Exercise) {
        val slug = stored.exercise.slug.value
        val now = OffsetDateTime.now(clock)
        val published = publishedRows().firstOrNull { it.exercise.slug == stored.exercise.slug }
        val row = stored.copy(exercise = exercise)
        val hasDraft = queries.draftRevisions().executeAsList().any { it.slug == slug }
        when {
            published != null && row == published -> queries.deleteDraftRevision(slug)
            hasDraft -> queries.updateDraftRevision(exercise.slug.value, encode(exercise), now, slug)
            else -> {
                val revision = (queries.latestRevision(slug).executeAsOne().latest ?: 0) + 1
                queries.insertRevision(slug, revision, row.position, encode(exercise), row.group, row.rank, null, now)
            }
        }
    }

    /** What the draft changes since each exercise's latest published revision, and what it still lacks to be published. */
    fun changes(): CatalogChanges {
        val published = published()
        val draft = draftExercises()
        val changed = draft.groupBy { changeOf(it, published[it.slug]) }
        return CatalogChanges(
            added = changed[DraftChange.Added].orEmpty(),
            edited = changed[DraftChange.Edited].orEmpty().map { EditedExercise(it, fieldChanges(published[it.slug]!!, it)) },
            retired = changed[DraftChange.Retired].orEmpty(),
            offered = changed[DraftChange.Offered].orEmpty(),
            missing = draft.mapNotNull { exercise -> missingLanguages(exercise).takeIf { it.isNotEmpty() }?.let { MissingText(exercise, it) } },
        )
    }

    /**
     * Publishes the draft revisions of [slugs], or of every exercise with something to publish when null:
     * each gets the next number of the publication cursor, and no other exercise is written. A slug with
     * nothing to publish is ignored. Refused while nothing is left to publish, or while an exercise being
     * published lacks a text in one of the shipped [LANGUAGES].
     */
    fun publish(slugs: List<ExerciseSlug>? = null): PublishResult = database.queries.transactionWithResult {
        val changes = changes()
        val chosen = changes.pending.filter { slugs == null || it in slugs }
        if (chosen.isEmpty()) return@transactionWithResult PublishResult.NothingChanged
        val missing = changes.missing.filter { it.exercise.slug in chosen }
        if (missing.isNotEmpty()) return@transactionWithResult PublishResult.MissingTranslations(missing)
        catalogOf(draftRows()) // checks the catalog's rules before anything is written
        val last = queries.reserveSeqs(chosen.size.toLong()).executeAsOne()
        chosen.forEachIndexed { index, slug -> queries.publishDraftRevision(last - chosen.size + 1 + index, slug.value) }
        PublishResult.Published(chosen)
    }

    private fun publishedRows(): List<CatalogRow> =
        queries.publishedExercises().executeAsList().map { CatalogRow(decode(it.content!!), it.position, it.variation_group, it.variation_rank) }

    /** The draft as the admin edits it: each exercise's latest published revision, with each draft revision in place of its exercise. */
    private fun draftRows(): List<CatalogRow> {
        val drafts = queries.draftRevisions().executeAsList().map { CatalogRow(decode(it.content!!), it.position, it.variation_group, it.variation_rank) }
        val bySlug = publishedRows().associateBy { it.exercise.slug.value }.toMutableMap()
        drafts.forEach { bySlug.remove(it.exercise.slug.value) }
        return (bySlug.values + drafts).sortedWith(compareBy({ it.position }, { it.exercise.slug.value }))
    }

    private fun draftExercises(): List<Exercise> = draftRows().map { it.exercise }

    private fun view(exercise: Exercise, published: Catalog?): DraftExercise {
        val before = published?.get(exercise.slug)
        return DraftExercise(exercise, before != null, changeOf(exercise, before))
    }

    /** Why [exercise] can't go in the catalog, or null when it can. */
    private fun invalidity(exercise: Exercise): String? {
        if (!SLUG.matches(exercise.slug.value)) return "A slug is lowercase letters and digits joined by dashes, like goblet-squat"
        if (exercise.name.byLanguage[LocalizedText.ENGLISH].isNullOrBlank()) return "The English name is empty"
        return runCatching { Catalog(listOf(exercise)) }.exceptionOrNull()?.message
    }

    companion object {
        /** The languages the apps ship: every text needs each of them before it is published. */
        val LANGUAGES = listOf("en", "fr")

        private val SLUG = Regex("[a-z0-9]+(-[a-z0-9]+)*")

        /** The shared types' JSON, as the catalog stores it; an unknown field is a mistake, not a newer phone. */
        val json = Json { }

        fun encode(exercise: Exercise): String = json.encodeToString(Exercise.serializer(), exercise)

        fun decode(text: String): Exercise = json.decodeFromString(Exercise.serializer(), text)
    }
}
