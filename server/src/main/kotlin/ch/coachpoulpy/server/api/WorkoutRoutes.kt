package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.Scope
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import ch.coachpoulpy.server.workouts.WorkoutDetail
import ch.coachpoulpy.server.workouts.WorkoutLibrary
import ch.coachpoulpy.shared.WorkoutOrder
import ch.coachpoulpy.shared.WorkoutPagePart
import ch.coachpoulpy.shared.WorkoutPageStep
import ch.coachpoulpy.shared.WorkoutStepRow
import java.util.UUID
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.div
import org.http4k.contract.meta

import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.uuid

/** What `include=text` adds to the list: the count line, "7 workouts" or "2 matches". */
@Serializable
data class WorkoutListText(val count: String)

/** What `include=text` adds to a row: the words under the name. */
@Serializable
data class WorkoutRowText(val format: String, val details: String, val exercises: String, val length: String)

@Serializable
data class WorkoutRow(
    val id: String,
    val name: String,
    val format: WorkoutFormat,
    val exerciseCount: Int,
    val minutes: Int,
    val timesDone: Int,
    val lastDoneAt: Instant?,
    val text: WorkoutRowText? = null,
)

@Serializable
data class WorkoutList(val items: List<WorkoutRow>, val total: Int, val page: Int, val size: Int, val text: WorkoutListText? = null)

/** How the workout went before: the sessions really done, whatever version they followed. */
@Serializable
data class WorkoutHistoryResponse(val timesDone: Int, val lastDoneAt: Instant?, val usualMinutes: Int?)

@Serializable
data class WorkoutStepText(val key: String, val slug: String, val name: String, val prescription: String, val roleLabel: String?, val isRest: Boolean)

@Serializable
data class WorkoutRepeatText(val key: String, val times: String, val rows: List<WorkoutStepText>)

@Serializable
data class WorkoutPartStepText(val key: String, val row: WorkoutStepText?, val repeat: WorkoutRepeatText?)

@Serializable
data class WorkoutPartText(val key: String, val kind: String, val name: String, val summary: String, val time: String, val steps: List<WorkoutPartStepText>)

@Serializable
data class WorkoutMuscleText(val group: String, val label: String, val sets: Int, val setsLabel: String, val heat: Int, val percent: Int)

/** What `include=text` adds to a workout: its page in words, the same the phone shows. */
@Serializable
data class WorkoutPageText(
    val format: String,
    val details: String,
    val equipment: List<String>,
    val parts: List<WorkoutPartText>,
    val muscles: List<WorkoutMuscleText>,
    /** Heat 1 to 3 of each body map area key to light. */
    val heat: Map<String, Int>,
    /** "3 times"; empty before the first session. */
    val timesDone: String,
    /** "31 min"; empty before the first session. */
    val usual: String,
)

@Serializable
data class WorkoutResponse(val workout: Workout, val history: WorkoutHistoryResponse, val text: WorkoutPageText? = null)

private val listBody = Body.auto<WorkoutList>().toLens()
private val workoutBody = Body.auto<WorkoutResponse>().toLens()
private val workoutProblemBody = Body.auto<Problem>().toLens()

private val workoutSearchQuery = Query.optional("q", "Words of the name, the format, or an exercise inside, in either language")
private val workoutFormatQuery = Query.optional("format", "circuit or sets")
private val workoutSortQuery = Query.optional("sort", "recent (done last first, never done last) or name; the best matches first while searching with q")
private val workoutPageQuery = Query.int().defaulted("page", 1, "The page, from 1")
private val workoutSizeQuery = Query.int().defaulted("size", WORKOUTS_PAGE_SIZE, "Workouts per page, 1 to $WORKOUTS_MAX_PAGE_SIZE")
private val includeQuery = Query.multi.optional("include", "Extras to add, comma separated or repeated: text (the wording, in the language of Accept-Language, else the account's)")
private val workoutId = Path.uuid().of("id", "The workout's id")

const val WORKOUTS_PAGE_SIZE = 25
const val WORKOUTS_MAX_PAGE_SIZE = 100

private val workoutsTag = Tag("workouts")
private const val TEXT = "text"
private val extras = setOf(TEXT)

/** The `include` extras asked for, or null when one is unknown (ADR 0027). */
private fun Request.included(): Set<String>? =
    includeQuery(this).orEmpty().flatMap { it.split(',') }.map { it.trim() }.filter { it.isNotEmpty() }.toSet().takeIf { extras.containsAll(it) }

/** The language of the answer: the first of the Accept-Language the wording exists in, else the account's. */
private fun Request.language(library: WorkoutLibrary, account: UUID): String {
    val asked = header("Accept-Language").orEmpty().split(',').mapNotNull { part ->
        val fields = part.split(';').map { it.trim() }
        val q = fields.drop(1).firstOrNull { it.startsWith("q=") }?.removePrefix("q=")?.toDoubleOrNull() ?: 1.0
        fields.first().substringBefore('-').lowercase().takeIf { it in WorkoutLibrary.LANGUAGES && q > 0 }?.let { it to q }
    }.sortedByDescending { it.second }.firstOrNull()?.first
    return asked ?: library.accountLanguage(account).takeIf { it in WorkoutLibrary.LANGUAGES } ?: WorkoutLibrary.DEFAULT_LANGUAGE
}

private fun Response.inLanguage(language: String) = header("Content-Language", language).header("Vary", "Accept-Language")

private fun workoutExample() = WorkoutResponse(
    Workout(ch.coachpoulpy.core.model.WorkoutId("0190d4d2-0000-7000-8000-000000000003"), 1, "Leg day", WorkoutFormat.SETS, emptyList()),
    WorkoutHistoryResponse(2, Instant.parse("2026-11-05T08:00:00Z"), 32),
)

/** The account's workouts, latest version of each, for the website (ADR 0027). */
fun workoutsRoute(library: WorkoutLibrary): ContractRoute = "$API_PREFIX/workouts" meta {
    summary = "The workouts"
    description = "The account's workouts as the phones saved them, the latest version of each one not deleted, filtered, ordered and paged. " +
        "Add include=text for the wording in the caller's language."
    operationId = "listWorkouts"
    tags += workoutsTag
    security = requires(Scope.WorkoutsRead)
    queries += listOf(workoutSearchQuery, workoutFormatQuery, workoutSortQuery, workoutPageQuery, workoutSizeQuery, includeQuery)
    returning(
        OK,
        listBody to WorkoutList(
            listOf(WorkoutRow("0190d4d2-0000-7000-8000-000000000003", "Leg day", WorkoutFormat.SETS, 5, 40, 2, Instant.parse("2026-11-05T08:00:00Z"))),
            total = 1, page = 1, size = WORKOUTS_PAGE_SIZE,
        ),
        "One page of workouts",
        definitionId = "WorkoutList",
    )
    returning(BAD_REQUEST, workoutProblemBody to Problem("bad-query"), "A filter, sort, paging or include value isn't allowed", definitionId = "Problem")
} bindContract GET to { request ->
    val caller = request.caller as Caller.Signed
    val format = workoutFormatQuery(request)?.let { name -> WorkoutFormat.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: return@to badWorkoutQuery() }
    val order = when (workoutSortQuery(request)) {
        null, "recent" -> WorkoutOrder.RECENT
        "name" -> WorkoutOrder.NAME
        else -> return@to badWorkoutQuery()
    }
    val page = workoutPageQuery(request)
    val size = workoutSizeQuery(request)
    val included = request.included()
    if (included == null || page < 1 || size !in 1..WORKOUTS_MAX_PAGE_SIZE) return@to badWorkoutQuery()
    val account = caller.accountId.value
    val language = request.language(library, account)
    val found = library.list(account, language, workoutSearchQuery(request).orEmpty(), format, order, page, size)
    val withText = TEXT in included
    Response(OK).with(
        listBody of WorkoutList(
            found.items.map {
                WorkoutRow(
                    it.id, it.name, it.format, it.exerciseCount, it.minutes, it.timesDone,
                    it.lastDoneMillis?.let(Instant::fromEpochMilliseconds),
                    text = if (withText) WorkoutRowText(it.formatLabel, it.details, it.exercisesLabel, it.lengthLabel) else null,
                )
            },
            found.total, found.page, found.pageSize,
            text = if (withText) WorkoutListText(found.countLabel) else null,
        ),
    ).inLanguage(language)
}

/** One workout of the account, with how it went before. */
fun workoutRoute(library: WorkoutLibrary): ContractRoute = "$API_PREFIX/workouts" / workoutId meta {
    summary = "One workout"
    description = "The latest version of the workout, with how it went before. Add include=text for its page in words and the muscles it works, in the caller's language."
    operationId = "getWorkout"
    tags += workoutsTag
    security = requires(Scope.WorkoutsRead)
    queries += includeQuery
    returning(OK, workoutBody to workoutExample(), "The workout", definitionId = "WorkoutResponse")
    returning(BAD_REQUEST, workoutProblemBody to Problem("bad-query"), "An include value isn't allowed", definitionId = "Problem")
    returning(NOT_FOUND, workoutProblemBody to Problem("unknown-workout"), "No such workout, or it was deleted", definitionId = "Problem")
} bindContract GET to { id ->
    { request ->
        val caller = request.caller as Caller.Signed
        val included = request.included()
        if (included == null) {
            badWorkoutQuery()
        } else {
            val account = caller.accountId.value
            val language = request.language(library, account)
            library.find(account, id, language)?.let { Response(OK).with(workoutBody of it.response(TEXT in included)).inLanguage(language) }
                ?: Response(NOT_FOUND).with(workoutProblemBody of Problem("unknown-workout"))
        }
    }
}

private fun badWorkoutQuery() = Response(BAD_REQUEST).with(workoutProblemBody of Problem("bad-query"))

private fun WorkoutDetail.response(withText: Boolean) = WorkoutResponse(
    workout,
    WorkoutHistoryResponse(page.timesDone, page.lastDoneMillis?.let(Instant::fromEpochMilliseconds), page.usualMinutes),
    text = if (withText) page.text() else null,
)

private fun ch.coachpoulpy.shared.WorkoutPageState.text() = WorkoutPageText(
    format = formatLabel,
    details = details,
    equipment = equipment,
    parts = parts.map { it.text() },
    muscles = muscles.map { WorkoutMuscleText(it.group.name, it.label, it.sets, it.setsLabel, it.heat, it.percent) },
    heat = heat,
    timesDone = timesDoneLabel,
    usual = usualLabel,
)

private fun WorkoutPagePart.text() = WorkoutPartText(key, kind.name, name, summary, time, steps.map { it.text() })

private fun WorkoutPageStep.text() = WorkoutPartStepText(
    key, row?.text(), repeatGroup?.let { group -> WorkoutRepeatText(group.key, group.timesLabel, group.rows.map { it.text() }) },
)

private fun WorkoutStepRow.text() = WorkoutStepText(key, slug, name, prescription, roleLabel, isRest)
