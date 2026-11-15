package ch.coachpoulpy.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class CatalogClientTest {
    private val requests = mutableListOf<String>()
    private var status = HttpStatusCode.OK
    private var body = ""

    private val client = CatalogClient(
        "https://api.example.com/",
        MockEngine { request ->
            requests += request.url.toString()
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        },
    )

    private val answer = """
        {"cursor": "12", "exercises": [
            {"revision": 2, "exercise": {"slug": "squat", "name": {"en": "Squat"}, "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS"], "measurement": "REPS", "focus": ["STRENGTH"], "positions": ["STANDING"], "instructions": [{"en": "Sit back."}]}, "variationGroup": 0, "variationRank": 1},
            {"revision": 1, "exercise": {"slug": "lunge", "name": {"en": "Lunge"}, "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS"], "measurement": "REPS", "focus": ["STRENGTH"], "positions": ["STANDING"], "instructions": [{"en": "Step."}]}, "variationGroup": null, "variationRank": null}
        ]}
    """.trimIndent()

    @Test
    fun withoutACursorItAsksForEveryExercise() = runTest {
        body = answer
        val update = client.since(null)!!
        assertEquals("https://api.example.com/api/v1/catalog/exercises", requests.single())
        assertEquals("12", update.cursor)
        assertEquals(listOf("squat" to 2, "lunge" to 1), update.exercises.map { it.exercise.slug.value to it.revision })
        assertEquals(0, update.exercises[0].variationGroup)
        assertNull(update.exercises[1].variationGroup)
    }

    @Test
    fun withACursorItAsksForWhatCameAfter() = runTest {
        status = HttpStatusCode.NoContent
        assertNull(client.since("12"))
        assertEquals("https://api.example.com/api/v1/catalog/exercises?after=12", requests.single())
    }

    @Test
    fun anythingElseIsAnError() = runTest {
        status = HttpStatusCode.ServiceUnavailable
        assertEquals(503, assertFailsWith<UnexpectedAnswer> { client.since(null) }.status)
    }
}
