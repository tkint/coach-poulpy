package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.sample.SampleCatalog
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.data.local.CatalogRevision
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.remote.CatalogClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CatalogRepositoryTest {
    private val store = LocalStore(DriverFactory())
    private val sample = SampleCatalog.catalog
    private val squat = sample.exercises.first()

    @Test
    fun aFreshInstallHoldsNoExercisesAndNoCursor() {
        val repository = CatalogRepository(store)
        assertTrue(repository.current.exercises.isEmpty())
        assertNull(repository.cursor)
    }

    @Test
    fun relaunchingKeepsWhatTheServerPublishedAndItsCursor() {
        val repository = CatalogRepository(store)
        assertEquals(1, repository.apply(listOf(CatalogRevision(squat, 2)), "9"))
        assertEquals(2, repository.current.revisionOf(squat.slug))

        val relaunched = CatalogRepository(store)
        assertEquals(squat, relaunched.current[squat.slug])
        assertEquals(2, relaunched.current.revisionOf(squat.slug))
        assertEquals("9", relaunched.cursor)
    }

    @Test
    fun anOlderRevisionNeverReplacesANewerOne() {
        val repository = CatalogRepository(store)
        repository.apply(listOf(CatalogRevision(squat.copy(name = LocalizedText(mapOf("en" to "Revised"))), 3)), "2")
        assertEquals(0, repository.apply(listOf(CatalogRevision(squat, 2)), "3"))
        assertEquals("Revised", repository.current[squat.slug]!!.name.byLanguage["en"])
    }

    @Test
    fun anExerciseOfAnUpdateJoinsItsVariationGroup() {
        val repository = CatalogRepository(store)
        val group = sample.variationGroups.first()
        val place = group.mapIndexed { rank, slug -> slug to rank }.toMap()
        repository.apply(sample.exercises.filter { it.slug in place }.map { CatalogRevision(it, 1, 0, place.getValue(it.slug)) }, "1")
        assertEquals(group, repository.current.variationGroups.first())
    }

    private val answer = """
        {"cursor": "4", "exercises": [
            {"revision": 3, "exercise": {"slug": "squat-new", "name": {"en": "New squat"}, "movementPattern": "SQUAT", "muscleGroups": ["QUADRICEPS"], "measurement": "REPS", "focus": ["STRENGTH"], "positions": ["STANDING"], "instructions": [{"en": "Sit back."}]}, "variationGroup": null, "variationRank": null}
        ]}
    """.trimIndent()

    private fun updater(repository: CatalogRepository, status: HttpStatusCode, body: String, seen: MutableList<String> = mutableListOf()) =
        CatalogUpdater(
            repository,
            CatalogClient(
                "https://api.example.com",
                MockEngine { request ->
                    seen += request.url.toString()
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
            ),
        )

    @Test
    fun anUpdateStoresTheExercisesAndTheCursorAndTheNextOneAsksAfterIt() = runTest {
        val repository = CatalogRepository(store)
        val seen = mutableListOf<String>()
        assertEquals(1, updater(repository, HttpStatusCode.OK, answer, seen).update())
        assertEquals("4", repository.cursor)
        assertEquals(3, repository.current.revisionOf(ExerciseSlug("squat-new")))

        assertEquals(0, updater(repository, HttpStatusCode.NoContent, "", seen).update())
        assertEquals(listOf("https://api.example.com/api/v1/catalog/exercises", "https://api.example.com/api/v1/catalog/exercises?after=4"), seen)
    }

    @Test
    fun anUnreachableOrBrokenServerLeavesTheCatalogAndTheCursorAsTheyWere() = runTest {
        val repository = CatalogRepository(store)
        val before = repository.current.exercises.size
        assertFailsWith<Exception> { updater(repository, HttpStatusCode.ServiceUnavailable, "").update() }
        assertFailsWith<Exception> { updater(repository, HttpStatusCode.OK, "{\"cursor\": \"5\", \"exercises\": [{}]}").update() }
        assertEquals(before, repository.current.exercises.size)
        assertNull(repository.cursor)
    }
}
