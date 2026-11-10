package ch.coachpoulpy.server.catalog

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.server.db.Database
import java.time.Clock
import java.time.OffsetDateTime
import kotlinx.serialization.json.Json

/** What a draft exercise has become since the latest published version; null when it is the same. */
enum class DraftChange(val code: String) { Added("added"), Edited("edited"), Retired("retired") }

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
    val edited: List<Exercise>,
    val retired: List<Exercise>,
    val missing: List<MissingText>,
) {
    val isEmpty: Boolean get() = added.isEmpty() && edited.isEmpty() && retired.isEmpty()
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
    draft != published -> DraftChange.Edited
    else -> null
}

/** The languages of [required] that some text of [exercise] lacks or leaves blank: its name, an instruction or its variation note. */
fun missingLanguages(exercise: Exercise, required: List<String> = ServerCatalog.LANGUAGES): List<String> {
    val texts = listOf(exercise.name) + exercise.instructions + listOfNotNull(exercise.variationNote)
    return required.filter { language -> texts.any { it.byLanguage[language].isNullOrBlank() } }
}

/**
 * One exercise as a row of the draft or of a version: its place in the catalog, and its variation
 * group and rank in it (easiest first), null when it has no group.
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
 * The exercise catalog on the server (#337, ADR 0026): a working draft the admin edits, and numbered
 * published versions, both stored as one row per exercise. The server assembles a version into the
 * document phones store and the app bundles (ADR 0008, ADR 0015); the public catalog route serves the latest.
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
     * Fills an empty catalog from [bundled]: its version becomes the first published one and its
     * exercises the draft. Does nothing once a version exists. Returns whether it seeded.
     */
    fun seed(): Boolean = database.queries.transactionWithResult {
        if (queries.latestVersion().executeAsOne().latest != null) return@transactionWithResult false
        val now = OffsetDateTime.now(clock)
        val rows = rowsOf(bundled)
        insertVersion(bundled.version, rows)
        rows.forEach { queries.insertDraft(it.exercise.slug.value, it.position, encode(it.exercise), it.group, it.rank, now) }
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
            queries.insertDraft(added.slug.value, position, encode(added), null, null, OffsetDateTime.now(clock))
            DraftResult.Saved(view(added, published()))
        }
    }

    /**
     * Replaces the exercise [slug] with [exercise], which keeps its retired state ([retire] changes it).
     * A different slug in [exercise] renames it, only while no version holds it yet.
     */
    fun edit(slug: ExerciseSlug, exercise: Exercise): DraftResult = database.queries.transactionWithResult {
        val draft = draftExercises()
        val stored = draft.firstOrNull { it.slug == slug } ?: return@transactionWithResult DraftResult.Unknown
        invalidity(exercise)?.let { return@transactionWithResult DraftResult.Invalid(it) }
        val published = published()
        if (exercise.slug != slug) {
            if (published?.get(slug) != null) return@transactionWithResult DraftResult.SlugPublished
            if (draft.any { it.slug == exercise.slug }) return@transactionWithResult DraftResult.SlugTaken
        }
        val edited = exercise.copy(retired = stored.retired)
        queries.updateDraft(edited.slug.value, encode(edited), OffsetDateTime.now(clock), slug.value)
        DraftResult.Saved(view(edited, published))
    }

    /** Retires the exercise [slug] in the draft: it stays in the catalog, hidden, so workouts that use it still open. */
    fun retire(slug: ExerciseSlug): DraftResult = database.queries.transactionWithResult {
        val stored = draftExercises().firstOrNull { it.slug == slug } ?: return@transactionWithResult DraftResult.Unknown
        val retired = stored.copy(retired = true)
        if (!stored.retired) queries.updateDraft(slug.value, encode(retired), OffsetDateTime.now(clock), slug.value)
        DraftResult.Saved(view(retired, published()))
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
            edited = changed[DraftChange.Edited].orEmpty(),
            retired = changed[DraftChange.Retired].orEmpty(),
            missing = draft.mapNotNull { exercise -> missingLanguages(exercise).takeIf { it.isNotEmpty() }?.let { MissingText(exercise, it) } },
        )
    }

    /**
     * Publishes the draft as the next version: a copy of its rows, variation groups included. Refused
     * while nothing changed, or while some exercise lacks a text in one of the shipped [LANGUAGES].
     */
    fun publish(): PublishResult = database.queries.transactionWithResult {
        val changes = changes()
        if (changes.missing.isNotEmpty()) return@transactionWithResult PublishResult.MissingTranslations(changes.missing)
        if (changes.isEmpty) return@transactionWithResult PublishResult.NothingChanged
        val rows = draftRows()
        catalogOf(changes.nextVersion, rows) // checks the catalog's rules before anything is written
        insertVersion(changes.nextVersion, rows)
        PublishResult.Published(changes.nextVersion)
    }

    private fun insertVersion(version: Int, rows: List<CatalogRow>) {
        queries.insertVersion(version, OffsetDateTime.now(clock))
        rows.forEach { queries.insertVersionExercise(version, it.exercise.slug.value, it.position, encode(it.exercise), it.group, it.rank) }
    }

    private fun catalogAt(version: Int): Catalog = catalogOf(
        version,
        queries.versionExercises(version).executeAsList().map { CatalogRow(decode(it.content!!), it.position, it.variation_group, it.variation_rank) },
    )

    private fun draftRows(): List<CatalogRow> =
        queries.draft().executeAsList().map { CatalogRow(decode(it.content!!), it.position, it.variation_group, it.variation_rank) }

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
