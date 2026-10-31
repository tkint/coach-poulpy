package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A workout turned into a file to send: [fileName] ends in `.poulpyworkout`, [content] is its JSON text. */
data class WorkoutFileExport(val fileName: String, val content: String)

/** Why a workout file was refused. Nothing is saved for any of them. */
enum class WorkoutFileProblem {
    /** Not JSON, or JSON that does not hold a workout. */
    UNREADABLE,

    /** JSON, but not a Coach Poulpy workout file. */
    NOT_A_WORKOUT,

    /** Written by a newer version of the app. */
    NEWER_VERSION,

    /** Names exercises this app's catalog does not know. */
    UNKNOWN_EXERCISES,

    /** Breaks the rules of parts, repeats and formats ([Workout.problems]). */
    INVALID_WORKOUT,
}

/** What reading a workout file gave: the [workout] as it will be added, or the [problem] that refused it. */
class WorkoutFileReading internal constructor(
    val workout: Workout?,
    val problem: WorkoutFileProblem?,
    /** The slugs [WorkoutFileProblem.UNKNOWN_EXERCISES] names, in the order the file lists them. */
    val unknownExercises: List<String> = emptyList(),
)

/** The envelope around the workout: the marker and schema version let later versions read older files. */
@Serializable
private data class WorkoutFileEnvelope(val format: String, val schemaVersion: Int, val workout: Workout)

/** Just the envelope's markers, read first so a newer file is refused before its workout is decoded. */
@Serializable
private data class WorkoutFileHeader(val format: String? = null, val schemaVersion: Int? = null)

/**
 * The `.poulpyworkout` file: a workout in JSON inside an envelope with a `format` marker and a `schemaVersion`.
 * Exercises travel as catalog slugs (ADR 0008), never as copied catalog content. Reading checks everything and builds
 * the copy to add; saving is up to the caller, so a refused file never leaves anything behind.
 */
object WorkoutFile {
    const val FORMAT = "coachpoulpy.workout"
    const val SCHEMA_VERSION = 1
    const val EXTENSION = "poulpyworkout"
    const val MIME_TYPE = "application/vnd.coachpoulpy.workout+json"

    private val json = Json { ignoreUnknownKeys = true }

    fun export(workout: Workout) = WorkoutFileExport(
        fileName = "${slug(workout.name)}.$EXTENSION",
        content = json.encodeToString(WorkoutFileEnvelope.serializer(), WorkoutFileEnvelope(FORMAT, SCHEMA_VERSION, workout)),
    )

    /**
     * Reads [text] against [catalog]. The workout comes back as a copy: a new id made here, version 1, and the same name,
     * format and parts, so adding it never overwrites or merges into a workout of the library. [existingNames] are the
     * library's names; one already taken gets [copyName] ("Upper body (copy)").
     */
    fun read(text: String, catalog: Catalog, existingNames: Set<String>, copyName: (String) -> String): WorkoutFileReading {
        val header = try {
            json.decodeFromString(WorkoutFileHeader.serializer(), text)
        } catch (_: SerializationException) {
            return refused(WorkoutFileProblem.UNREADABLE)
        } catch (_: IllegalArgumentException) {
            return refused(WorkoutFileProblem.UNREADABLE)
        }
        if (header.format != FORMAT || header.schemaVersion == null) return refused(WorkoutFileProblem.NOT_A_WORKOUT)
        if (header.schemaVersion > SCHEMA_VERSION) return refused(WorkoutFileProblem.NEWER_VERSION)
        val sent = try {
            json.decodeFromString(WorkoutFileEnvelope.serializer(), text).workout
        } catch (_: SerializationException) {
            return refused(WorkoutFileProblem.UNREADABLE)
        } catch (_: IllegalArgumentException) {
            return refused(WorkoutFileProblem.UNREADABLE)
        }
        // A retired exercise is still in the catalog, so it imports; only a slug the catalog has never had is unknown.
        val unknown = exerciseSlugs(sent).filter { catalog[it] == null }.map { it.value }.distinct()
        if (unknown.isNotEmpty()) return refused(WorkoutFileProblem.UNKNOWN_EXERCISES, unknown)
        if (sent.problems().isNotEmpty()) return refused(WorkoutFileProblem.INVALID_WORKOUT)
        val name = if (sent.name in existingNames) copyName(sent.name) else sent.name
        return WorkoutFileReading(sent.copy(id = WorkoutId(Ids.newUuidV7()), version = 1, name = name), null)
    }

    private fun refused(problem: WorkoutFileProblem, unknown: List<String> = emptyList()) = WorkoutFileReading(null, problem, unknown)

    private fun exerciseSlugs(workout: Workout): List<ExerciseSlug> = workout.steps.flatMap { step ->
        when (step) {
            is Step.ExerciseStep -> listOf(step.exercise)
            is Step.Group -> step.children.filterIsInstance<Step.ExerciseStep>().map { it.exercise }
            is Step.Rest -> emptyList()
        }
    }

    /** "Upper body" becomes `upper-body`, "Séance dos" becomes `seance-dos`; `workout` when nothing is left. */
    internal fun slug(name: String): String {
        val plain = name.lowercase().map { ACCENTS[it] ?: it.toString() }.joinToString("")
        return plain.map { if (it in 'a'..'z' || it in '0'..'9') it else '-' }.joinToString("")
            .split('-').filter { it.isNotEmpty() }.joinToString("-").ifEmpty { "workout" }
    }

    private val ACCENTS = buildMap {
        "àáâãäå".forEach { put(it, "a") }
        "çćč".forEach { put(it, "c") }
        "èéêë".forEach { put(it, "e") }
        "ìíîï".forEach { put(it, "i") }
        "ñ".forEach { put(it, "n") }
        "òóôõö".forEach { put(it, "o") }
        "ùúûü".forEach { put(it, "u") }
        "ýÿ".forEach { put(it, "y") }
        put('œ', "oe")
        put('æ', "ae")
    }
}
