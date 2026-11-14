package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SessionChange
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutDeletedChange
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.SignInResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED

/** The workouts a signed-in website reads: the list's filters, order and pages, and one workout, with its wording on request (ADR 0027). */
class WorkoutsRoutesTest {
    private companion object {
        const val UPPER = "0190d4d2-1000-7000-8000-000000000001"
        const val CORE = "0190d4d2-1000-7000-8000-000000000002"
        const val LEGS = "0190d4d2-1000-7000-8000-000000000003"
        const val NOTHING = "0190d4d2-1000-7000-8000-0000000000ff"
    }

    private val test = TestServer()
    private val token = signedIn("ana@example.com", "fr")

    private fun signedIn(email: String, language: String): String {
        test.accounts.signUp(SignUp(email, "correct horse", language, healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
        return (test.signIns.signIn(email, "correct horse", "Phone") as SignInResult.Signed).tokens.accessToken
    }

    private fun exercise(id: String, slug: String) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(slug), Prescription(listOf(SetPrescription(Target.Reps(10)))))

    private fun workout(id: String, name: String, format: WorkoutFormat, slug: String = "push-up", version: Int = 1) = Workout(
        WorkoutId(id), version, name, format, listOf(Part(PartId("$id-work"), PartKind.WORK, listOf(exercise("$id-1", slug)))),
    )

    private fun version(workout: Workout, base: Int? = null) =
        WorkoutVersionChange(workout, base, Instant.parse("2026-11-07T10:00:00Z"))

    private fun session(id: String, workoutId: String, version: Int, startedAt: String, activeMinutes: Int? = 30) = SessionChange(
        SessionId(id), SessionSource.GUIDED, Instant.parse(startedAt), Instant.parse(startedAt) + kotlin.time.Duration.parse("PT${activeMinutes ?: 1}M"),
        WorkoutRef(WorkoutId(workoutId), version), "x", WorkoutFormat.SETS,
        totals = activeMinutes?.let { JsonObject(mapOf("active" to kotlinx.serialization.json.JsonPrimitive("PT${it}M"))) },
    )

    private fun push(vararg changes: SyncChange, as_: String = token) {
        val response = test.http(
            Request(POST, "/api/v1/sync/push").header("Authorization", "Bearer $as_").header("Content-Type", "application/json")
                .body(Json.encodeToString(SyncPush.serializer(), SyncPush(changes.toList()))),
        )
        assertEquals(OK, response.status, response.bodyString())
    }

    private fun get(path: String, bearer: String? = token, language: String? = null): Response = test.http(
        Request(GET, "/api/v1$path").let { if (bearer != null) it.header("Authorization", "Bearer $bearer") else it }
            .let { if (language != null) it.header("Accept-Language", language) else it },
    )

    private fun Response.json() = Json.parseToJsonElement(bodyString()).jsonObject
    private fun JsonObject.items() = getValue("items").jsonArray.map { it.jsonObject }
    private fun List<JsonObject>.ids() = map { it.getValue("id").jsonPrimitive.content }

    private val upper = workout(UPPER, "Upper body", WorkoutFormat.SETS)
    private val core = workout(CORE, "Core and mobility", WorkoutFormat.CIRCUIT, slug = "plank")
    private val legs = workout(LEGS, "Legs", WorkoutFormat.SETS, slug = "bodyweight-squat")

    private fun library() {
        push(
            version(upper), version(core), version(legs),
            session("0190d4d2-0000-7000-8000-000000000001", UPPER, 1, "2026-11-01T08:00:00Z", 30),
            session("0190d4d2-0000-7000-8000-000000000002", UPPER, 1, "2026-11-05T08:00:00Z", 34),
            session("0190d4d2-0000-7000-8000-000000000003", CORE, 1, "2026-11-06T08:00:00Z", 15),
        )
    }

    @Test
    fun theListPutsTheRecentlyDoneFirstAndTheNeverDoneLast() {
        library()
        val response = get("/workouts")
        assertEquals(OK, response.status, response.bodyString())
        val body = response.json()
        assertEquals(listOf(CORE, UPPER, LEGS), body.items().ids())
        assertEquals(3, body.getValue("total").jsonPrimitive.int)
        val upperRow = body.items()[1]
        assertEquals(2, upperRow.getValue("timesDone").jsonPrimitive.int)
        assertEquals("2026-11-05T08:00:00Z", upperRow.getValue("lastDoneAt").jsonPrimitive.content)
        assertEquals("SETS", upperRow.getValue("format").jsonPrimitive.content)
    }

    @Test
    fun aPureApiCallCarriesNoWording() {
        library()
        val row = get("/workouts").json().items().first()
        assertNull(row["text"])
        assertNull(get("/workouts").json()["text"])
        assertNull(get("/workouts/$UPPER").json()["text"])
    }

    @Test
    fun includingTextAddsTheWordingInTheAccountsLanguage() {
        library()
        val body = get("/workouts?include=text").json()
        assertEquals("3 séances", body.getValue("text").jsonObject.getValue("count").jsonPrimitive.content)
        val row = body.items().first { it.getValue("id").jsonPrimitive.content == UPPER }
        assertEquals("Séries", row.getValue("text").jsonObject.getValue("format").jsonPrimitive.content)
    }

    @Test
    fun theAcceptLanguageHeaderWinsOverTheAccountLanguage() {
        library()
        val response = get("/workouts?include=text", language = "en-GB,en;q=0.8")
        assertEquals("en", response.header("Content-Language"))
        assertEquals("Accept-Language", response.header("Vary"))
        assertEquals("3 workouts", response.json().getValue("text").jsonObject.getValue("count").jsonPrimitive.content)
    }

    @Test
    fun anUnknownLanguageFallsBackToTheAccountsOne() {
        library()
        val response = get("/workouts?include=text", language = "de")
        assertEquals("fr", response.header("Content-Language"))
    }

    @Test
    fun theListCanBeFilteredSortedSearchedAndPaged() {
        library()
        assertEquals(listOf(UPPER, LEGS), get("/workouts?format=sets").json().items().ids())
        assertEquals(listOf(CORE, LEGS, UPPER), get("/workouts?sort=name").json().items().ids())
        assertEquals(listOf(LEGS), get("/workouts?q=squat").json().items().ids())
        val second = get("/workouts?size=2&page=2").json()
        assertEquals(listOf(LEGS), second.items().ids())
        assertEquals(3, second.getValue("total").jsonPrimitive.int)
        assertEquals(2, second.getValue("page").jsonPrimitive.int)
    }

    @Test
    fun aValueThatIsNotAllowedIsABadQuery() {
        for (query in listOf("format=nope", "sort=nope", "include=nope", "page=0", "size=0", "size=1000")) {
            assertEquals(BAD_REQUEST, get("/workouts?$query").status, query)
        }
    }

    @Test
    fun aDeletedWorkoutIsNotListedAndHasNoPage() {
        library()
        push(WorkoutDeletedChange(WorkoutId(LEGS), 1, Instant.parse("2026-11-07T11:00:00Z")))
        assertEquals(listOf(CORE, UPPER), get("/workouts").json().items().ids())
        assertEquals(NOT_FOUND, get("/workouts/$LEGS").status)
    }

    @Test
    fun theLatestVersionIsTheOneShown() {
        push(version(upper), version(upper.copy(version = 2, name = "Upper body, harder"), base = 1))
        assertEquals(listOf("Upper body, harder"), get("/workouts").json().items().map { it.getValue("name").jsonPrimitive.content })
    }

    @Test
    fun aWorkoutHasItsVersionStepsAndHowItWentBefore() {
        library()
        val response = get("/workouts/$UPPER")
        assertEquals(OK, response.status, response.bodyString())
        val body = response.json()
        assertEquals("Upper body", body.getValue("workout").jsonObject.getValue("name").jsonPrimitive.content)
        val history = body.getValue("history").jsonObject
        assertEquals(2, history.getValue("timesDone").jsonPrimitive.int)
        assertEquals("2026-11-05T08:00:00Z", history.getValue("lastDoneAt").jsonPrimitive.content)
        assertEquals(34, history.getValue("usualMinutes").jsonPrimitive.int)
    }

    @Test
    fun includingTextGivesTheWorkoutPageItsWordsAndMuscles() {
        library()
        val text = get("/workouts/$UPPER?include=text", language = "en").json().getValue("text").jsonObject
        assertEquals("Sets", text.getValue("format").jsonPrimitive.content)
        val part = text.getValue("parts").jsonArray.first().jsonObject
        assertEquals("Work", part.getValue("name").jsonPrimitive.content)
        assertTrue(text.getValue("muscles").jsonArray.isNotEmpty())
        assertTrue(text.getValue("heat").jsonObject.isNotEmpty())
    }

    @Test
    fun aWorkoutOfAnotherAccountIsNotFound() {
        library()
        val other = signedIn("bo@example.com", "en")
        assertEquals(NOT_FOUND, get("/workouts/$UPPER", bearer = other).status)
        assertEquals(emptyList(), get("/workouts", bearer = other).json().items())
    }

    @Test
    fun anUnknownWorkoutIsNotFound() {
        assertEquals(NOT_FOUND, get("/workouts/$NOTHING").status)
    }

    @Test
    fun onlyASignedInCallerReads() {
        assertEquals(UNAUTHORIZED, get("/workouts", bearer = null).status)
        assertEquals(UNAUTHORIZED, get("/workouts/$UPPER", bearer = null).status)
    }
}
