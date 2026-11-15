package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
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
    private val bundled = Catalog.bundled
    private val squat = bundled.exercises.first()

    @Test
    fun aFreshInstallHoldsTheBundledCatalogAtRevisionOneWithNoCursor() {
        val repository = CatalogRepository(store, bundled)
        assertEquals(bundled.exercises.map { it.slug }.toSet(), repository.current.exercises.map { it.slug }.toSet())
        assertEquals(bundled.exercises.map { it.slug }.toSet(), store.catalogRevisions().filterValues { it == 1 }.keys)
        assertNull(repository.cursor)
        assertEquals(bundled.variationGroups.size, repository.current.variationGroups.size)
    }

    @Test
    fun relaunchingChangesNothingAndKeepsAnExerciseTheServerRevised() {
        val repository = CatalogRepository(store, bundled)
        val revised = squat.copy(name = LocalizedText(mapOf("en" to "Revised")))
        assertEquals(1, repository.apply(listOf(CatalogRevision(revised, 2)), "9"))
        assertEquals("Revised", repository.current[squat.slug]!!.name.byLanguage["en"])

        val relaunched = CatalogRepository(store, bundled)
        assertEquals("Revised", relaunched.current[squat.slug]!!.name.byLanguage["en"], "the bundled revision 1 doesn't undo it")
        assertEquals("9", relaunched.cursor)
    }

    @Test
    fun anAppUpdateThatBundlesAHigherRevisionReplacesTheStoredExercise() {
        CatalogRepository(store, bundled)
        val newer = Catalog(
            bundled.exercises.map { if (it.slug == squat.slug) it.copy(name = LocalizedText(mapOf("en" to "From the new app"))) else it },
            bundled.variationGroups,
            mapOf(squat.slug to 2),
        )
        val repository = CatalogRepository(store, newer)
        assertEquals("From the new app", repository.current[squat.slug]!!.name.byLanguage["en"])
        assertEquals(2, repository.current.revisionOf(squat.slug))
    }

    @Test
    fun aPhoneFromBeforeRevisionsGainsTheVariationGroupsAtLaunch() {
        // What the migration leaves: every exercise at revision 1, no group on any of them.
        store.applyCatalogRevisions(bundled.exercises.map { CatalogRevision(it, 1) })
        assertTrue(store.catalogVariationGroups().isEmpty())
        assertEquals(bundled.variationGroups.size, CatalogRepository(store, bundled).current.variationGroups.size)
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
        val repository = CatalogRepository(store, bundled)
        val seen = mutableListOf<String>()
        assertEquals(1, updater(repository, HttpStatusCode.OK, answer, seen).update())
        assertEquals("4", repository.cursor)
        assertEquals(3, repository.current.revisionOf(ExerciseSlug("squat-new")))

        assertEquals(0, updater(repository, HttpStatusCode.NoContent, "", seen).update())
        assertEquals(listOf("https://api.example.com/api/v1/catalog/exercises", "https://api.example.com/api/v1/catalog/exercises?after=4"), seen)
    }

    @Test
    fun anUnreachableOrBrokenServerLeavesTheCatalogAndTheCursorAsTheyWere() = runTest {
        val repository = CatalogRepository(store, bundled)
        val before = repository.current.exercises.size
        assertFailsWith<Exception> { updater(repository, HttpStatusCode.ServiceUnavailable, "").update() }
        assertFailsWith<Exception> { updater(repository, HttpStatusCode.OK, "{\"cursor\": \"5\", \"exercises\": [{}]}").update() }
        assertEquals(before, repository.current.exercises.size)
        assertNull(repository.cursor)
    }
}
