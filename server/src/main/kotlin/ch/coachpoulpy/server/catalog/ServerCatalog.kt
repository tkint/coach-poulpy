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

/** What a draft exercise has become since the latest published version; null when it is the same. */
enum class DraftChange(val code: String) { Added("added"), Edited("edited"), Retired("retired"), Offered("offered") }

/**
 * One field an edit changed, from the latest published version ([before]) to the draft ([after]); null where
 * the field is absent. [field] is a path into the exercise JSON, one text per language and instruction:
 * `name.fr`, `instructions.1.en`, `variationNote.en`, `aliases.fr`, `muscleGroups`, `equipment`.
 */
data class FieldChange(val field: String, val before: JsonElement?, val after: JsonElement?)

/** An edited exercise of the draft, and what changed in it. */
data class EditedExercise(val exercise: Exercise, val fields: List<FieldChange>)

/** One exercise of the draft as the admin sees it: whether a version already holds it, and what changed since. */
data class DraftExercise(val exercise: Exercise, val published: Boolean, val change: DraftChange?)

/** An exercise that can't be published yet, and the shipped languages some of its text lacks. */
data class MissingText(val exercise: Exercise, val languages: List<String>)

/**
 * What publishing now would do: the draft against [version], the latest published one. [nextVersion]
 * is the number publishing would give. [missing] lists what must be translated first.
 */
data class CatalogChanges(
    val version: Int,
    val nextVersion: Int,
    val added: List<Exercise>,
    val edited: List<EditedExercise>,
    val retired: List<Exercise>,
    /** Retired in the latest version and offered again in the draft. */
    val offered: List<Exercise>,
    val missing: List<MissingText>,
) {
    val isEmpty: Boolean get() = added.isEmpty() && edited.isEmpty() && retired.isEmpty() && offered.isEmpty()
}

/** What a write to the draft did, or why it was refused. */
sealed interface DraftResult {
    data class Saved(val exercise: DraftExercise) : DraftResult
    data object Unknown : DraftResult
    /** Another exercise already has the slug. */
    data object SlugTaken : DraftResult
    /** The slug of an exercise a version already holds never changes (ADR 0008). */
    data object SlugPublished : DraftResult
    data class Invalid(val reason: String) : DraftResult
}

/** What publishing did, or why it didn't. */
sealed interface PublishResult {
    data class Published(val version: Int) : PublishResult
    data object NothingChanged : PublishResult
    data class MissingTranslations(val missing: List<MissingText>) : PublishResult
}

/** [draft]'s change against [published], the same exercise in the latest version, or null when that version lacks it. */
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
 * One exercise as the draft or a version holds it: its place in the catalog, and its variation group and
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

/** The catalog document of [version] made of [rows]: the exercises in order and the variation groups they name. */
private fun catalogOf(version: Int, rows: List<CatalogRow>): Catalog {
    val groups = rows.filter { it.group != null }.groupBy { it.group!! }.toSortedMap().values
        .map { group -> group.sortedBy { it.rank }.map { it.exercise.slug } }
    return Catalog(version, rows.sortedBy { it.position }.map { it.exercise }, groups)
}

/**
 * The exercise catalog on the server (#337, ADR 0026): numbered published versions, and the draft the
 * admin edits. Each exercise is stored on its own as revisions: a publish marks the draft revisions as
 * published in the new version, so it writes only what changed, and version N holds each exercise's
 * latest revision published up to N. The server assembles a version into the document phones store and
 * the app bundles (ADR 0008, ADR 0015); the public catalog route serves the latest.
 *
 * [bundled] is the catalog this server was built with: it fills the database the first time ([seed]),
 * and a published version always numbers past it, so a phone with a newer app still takes the next one.
 */
class ServerCatalog(
    private val database: Database,
    private val bundled: Catalog,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val queries get() = database.queries.catalogQueries

    /** The latest version's document, kept once assembled: a version never changes, and every phone launch asks for it. */
    @Volatile
    private var served: Pair<Int, String>? = null

    /**
     * Fills an empty catalog from [bundled]: one revision per exercise, published in the bundled version.
     * Does nothing once a version exists. Returns whether it seeded.
     */
    fun seed(): Boolean = database.queries.transactionWithResult {
        if (queries.latestVersion().executeAsOne().latest != null) return@transactionWithResult false
        val now = OffsetDateTime.now(clock)
        queries.insertVersion(bundled.version, now)
        rowsOf(bundled).forEach {
            queries.insertRevision(it.exercise.slug.value, 1, it.position, encode(it.exercise), it.group, it.rank, bundled.version, now)
        }
        true
    }

    /** The latest published version's number, or null before [seed]. */
    fun latestVersion(): Int? = queries.latestVersion().executeAsOne().latest

    /** The latest published version as the public route serves it: the bundled JSON format. Null before [seed]. */
    fun latestDocument(): Pair<Int, String>? {
        val version = latestVersion() ?: return null
        served?.takeIf { it.first == version }?.let { return it }
        return (version to catalogAt(version).toJson()).also { served = it }
    }

    /** The latest published version, or null before [seed]. */
    fun published(): Catalog? = latestVersion()?.let(::catalogAt)

    /** Published version [version], rebuilt from the revisions it holds; null for a version never published. */
    fun version(version: Int): Catalog? = catalogAt(version).takeIf { it.exercises.isNotEmpty() }

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
     * A different slug in [exercise] renames it, only while no version holds it yet.
     */
    fun edit(slug: ExerciseSlug, exercise: Exercise): DraftResult = database.queries.transactionWithResult {
        val draft = draftRows()
        val stored = draft.firstOrNull { it.exercise.slug == slug } ?: return@transactionWithResult DraftResult.Unknown
        invalidity(exercise)?.let { return@transactionWithResult DraftResult.Invalid(it) }
        val published = published()
        if (exercise.slug != slug) {
            if (published?.get(slug) != null) return@transactionWithResult DraftResult.SlugPublished
            if (draft.any { it.exercise.slug == exercise.slug }) return@transactionWithResult DraftResult.SlugTaken
        }
        val edited = exercise.copy(retired = stored.exercise.retired)
        save(stored, edited)
        DraftResult.Saved(view(edited, published))
    }

    /** Offers a retired exercise again in the draft: the next version lists it for browsing and building again. */
    fun offer(slug: ExerciseSlug): DraftResult = setRetired(slug, false)

    /** Retires the exercise [slug] in the draft: it stays in the catalog, hidden, so workouts that use it still open. */
    fun retire(slug: ExerciseSlug): DraftResult = setRetired(slug, true)

    private fun setRetired(slug: ExerciseSlug, retired: Boolean): DraftResult = database.queries.transactionWithResult {
        val stored = draftRows().firstOrNull { it.exercise.slug == slug } ?: return@transactionWithResult DraftResult.Unknown
        val changed = stored.exercise.copy(retired = retired)
        if (stored.exercise.retired != retired) save(stored, changed)
        DraftResult.Saved(view(changed, published()))
    }

    /**
     * Writes [exercise] as the draft revision of [stored]'s exercise (renamed when its slug differs):
     * changes the draft revision, or starts one after the latest published revision. A draft that comes
     * back to what the latest version holds is dropped, so publishing writes nothing for it.
     */
    private fun save(stored: CatalogRow, exercise: Exercise) {
        val slug = stored.exercise.slug.value
        val now = OffsetDateTime.now(clock)
        val published = latestVersion()?.let { version -> publishedRows(version).firstOrNull { it.exercise.slug == stored.exercise.slug } }
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

    /** What the draft changes since the latest published version, and what it still lacks to be published. */
    fun changes(): CatalogChanges {
        val published = checkNotNull(published()) { "The catalog isn't seeded" }
        val draft = draftExercises()
        val changed = draft.groupBy { changeOf(it, published[it.slug]) }
        return CatalogChanges(
            version = published.version,
            nextVersion = maxOf(published.version, bundled.version) + 1,
            added = changed[DraftChange.Added].orEmpty(),
            edited = changed[DraftChange.Edited].orEmpty().map { EditedExercise(it, fieldChanges(published[it.slug]!!, it)) },
            retired = changed[DraftChange.Retired].orEmpty(),
            offered = changed[DraftChange.Offered].orEmpty(),
            missing = draft.mapNotNull { exercise -> missingLanguages(exercise).takeIf { it.isNotEmpty() }?.let { MissingText(exercise, it) } },
        )
    }

    /**
     * Publishes the draft as the next version: its draft revisions become published in it, and the
     * exercises it didn't touch get no new row. Refused while nothing changed, or while some exercise
     * lacks a text in one of the shipped [LANGUAGES].
     */
    fun publish(): PublishResult = database.queries.transactionWithResult {
        val changes = changes()
        if (changes.missing.isNotEmpty()) return@transactionWithResult PublishResult.MissingTranslations(changes.missing)
        if (changes.isEmpty) return@transactionWithResult PublishResult.NothingChanged
        catalogOf(changes.nextVersion, draftRows()) // checks the catalog's rules before anything is written
        queries.insertVersion(changes.nextVersion, OffsetDateTime.now(clock))
        queries.publishDraftRevisions(changes.nextVersion)
        PublishResult.Published(changes.nextVersion)
    }

    private fun publishedRows(version: Int): List<CatalogRow> =
        queries.versionExercises(version).executeAsList().map { CatalogRow(decode(it.content!!), it.position, it.variation_group, it.variation_rank) }

    private fun catalogAt(version: Int): Catalog = catalogOf(version, publishedRows(version))

    /** The draft as the admin edits it: the latest version's exercises, with each draft revision in place of its exercise. */
    private fun draftRows(): List<CatalogRow> {
        val drafts = queries.draftRevisions().executeAsList().map { CatalogRow(decode(it.content!!), it.position, it.variation_group, it.variation_rank) }
        val bySlug = latestVersion()?.let(::publishedRows).orEmpty().associateBy { it.exercise.slug.value }.toMutableMap()
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
        return runCatching { Catalog(0, listOf(exercise)) }.exceptionOrNull()?.message
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
