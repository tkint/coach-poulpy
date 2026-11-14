package ch.coachpoulpy.server.workouts

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.server.db.Database
import ch.coachpoulpy.shared.WorkoutHistory
import ch.coachpoulpy.shared.WorkoutOrder
import ch.coachpoulpy.shared.WorkoutPageState
import ch.coachpoulpy.shared.WorkoutViews
import ch.coachpoulpy.shared.WorkoutsPage
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** A workout with how it went before, and its page in the language asked for. */
class WorkoutDetail(val workout: Workout, val history: WorkoutHistory, val page: WorkoutPageState)

/**
 * What the signed-in website reads of an account's workouts: the latest version of each one not deleted,
 * and the sessions done of it, built into lists and pages by the shared [WorkoutViews] (ADR 0027).
 * Read-only: the phone is the source of truth and the website never writes a workout.
 */
class WorkoutLibrary(private val database: Database, catalog: Catalog) {
    private val views = WorkoutViews(catalog)
    private val queries get() = database.queries.syncQueries

    /** The language of the account's emails and screens, such as "fr". */
    fun accountLanguage(account: UUID): String =
        database.queries.accountQueries.contactById(account).executeAsOneOrNull()?.language ?: DEFAULT_LANGUAGE

    fun list(
        account: UUID,
        language: String,
        query: String,
        format: WorkoutFormat?,
        order: WorkoutOrder,
        page: Int,
        size: Int,
    ): WorkoutsPage {
        val workouts = queries.latestWorkouts(account).executeAsList().map { parse(it) }
        return views.list(workouts, histories(account), language, query, format, order, page, size)
    }

    fun find(account: UUID, id: UUID, language: String): WorkoutDetail? {
        val workout = queries.latestWorkout(account, id).executeAsOneOrNull()?.let { parse(it) } ?: return null
        val history = histories(account)[workout.id] ?: WorkoutHistory()
        return WorkoutDetail(workout, history, views.detail(workout, history, language))
    }

    private fun parse(content: String) = Json.decodeFromString(Workout.serializer(), content)

    /** The sessions that really happened, by workout, oldest first; one the phone saved empty totals for did not. */
    private fun histories(account: UUID): Map<WorkoutId, WorkoutHistory> =
        queries.workoutSessions(account).executeAsList().mapNotNull { row ->
            val active = activeOf(row.totals ?: return@mapNotNull null)
            if (active == null || active == Duration.ZERO) return@mapNotNull null
            Triple(WorkoutId(row.workout_id.toString()), Instant.fromEpochMilliseconds(row.started_at.toInstant().toEpochMilli()), active.inWholeMinutes.toInt())
        }.groupBy({ it.first }, { it.second to it.third }).mapValues { (_, done) ->
            val sorted = done.sortedBy { it.first }
            WorkoutHistory(sorted.map { it.first }, sorted.map { it.second })
        }

    private fun activeOf(totals: String): Duration? = runCatching {
        Duration.parse(Json.parseToJsonElement(totals).jsonObject.getValue("active").jsonPrimitive.content)
    }.getOrNull()

    companion object {
        /** The languages the wording exists in; the first is the fallback. */
        val LANGUAGES = listOf("en", "fr")
        const val DEFAULT_LANGUAGE = "en"
    }
}
