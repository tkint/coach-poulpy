package ch.coachpoulpy.server.api

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.admin.AdminAction
import ch.coachpoulpy.server.admin.EmailAddress
import ch.coachpoulpy.server.admin.promoteFirstAdmin
import ch.coachpoulpy.server.app
import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import ch.coachpoulpy.server.catalog.ServerCatalog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.events.and
import org.http4k.testing.RecordingEvents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdminCatalogTest {
    private val test = TestServer()
    private val events = RecordingEvents()
    private val http = app(
        "0.7.0", test.catalog, test.database, test.accounts, test.signIns, test.passwordResets, test.accountDeletion, test.sync, test.audit,
        events = events.and(test.audit.events), settings = test.settings, mailer = test.outgoing,
    )
    private val bundled = test.catalog
    private val adminToken: String

    init {
        account("ana@example.com")
        promoteFirstAdmin(test.database, test.audit.events, EmailAddress.of("ana@example.com"))
        adminToken = signIn("ana@example.com", Client.Website).tokens.accessToken
    }

    private fun account(email: String) {
        test.accounts.signUp(SignUp(email, "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
    }

    private fun signIn(email: String, client: Client = Client.App) =
        test.signIns.signIn(email, "correct horse", "A device", client) as SignInResult.Signed

    private fun call(request: Request, token: String = adminToken): Response = http(request.header("Authorization", "Bearer $token"))

    private fun body(response: Response) = Json.parseToJsonElement(response.bodyString()).jsonObject

    private fun withJson(request: Request, json: String) = request.header("content-type", "application/json").body(json)

    private fun create(exercise: String, token: String = adminToken) = call(withJson(Request(POST, "/api/v1/admin/catalog/exercises"), exercise), token)

    private fun create(exercise: Exercise) = create(ServerCatalog.encode(exercise))

    private fun edit(slug: String, exercise: Exercise) =
        call(withJson(Request(PUT, "/api/v1/admin/catalog/exercises/$slug"), ServerCatalog.encode(exercise)))

    private fun retire(slug: String) = call(Request(POST, "/api/v1/admin/catalog/exercises/$slug/retire"))

    private fun get(slug: String) = call(Request(GET, "/api/v1/admin/catalog/exercises/$slug"))

    private fun changes() = body(call(Request(GET, "/api/v1/admin/catalog/changes")))

    private fun publish() = call(Request(POST, "/api/v1/admin/catalog/publish"))

    private fun slugs(list: Any) = (list as kotlinx.serialization.json.JsonArray).map { it.jsonObject.getValue("slug").jsonPrimitive.content }

    private fun publicCatalog(newerThan: Int? = null) =
        http(Request(GET, "/api/v1/catalog").let { if (newerThan == null) it else it.query("newerThan", newerThan.toString()) })

    private fun pikePushUp(fr: Boolean = true) = Exercise(
        slug = ExerciseSlug("pike-push-up"),
        name = LocalizedText(if (fr) mapOf("en" to "Pike push-up", "fr" to "Pompe en V") else mapOf("en" to "Pike push-up")),
        movementPattern = MovementPattern.PUSH,
        muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
        measurement = MeasurementMode.REPS,
        instructions = listOf(LocalizedText(if (fr) mapOf("en" to "Hips high, lower the head.", "fr" to "Hanches hautes, descendez la tête.") else mapOf("en" to "Hips high, lower the head."))),
    )

    private val goblet = bundled[ExerciseSlug("goblet-squat")]!!

    @Test
    fun theFirstStartSeedsTheCatalogFromTheBundledOne() {
        assertEquals(bundled.version, test.serverCatalog.latestVersion())
        assertEquals(bundled.exercises, test.serverCatalog.draft().map { it.exercise })
        assertTrue(test.serverCatalog.draft().all { it.published && it.change == null })
        assertFalse(test.serverCatalog.seed(), "a second start leaves the catalog alone")
        val response = publicCatalog()
        assertEquals(OK, response.status)
        assertEquals(Json.parseToJsonElement(bundled.toJson()), Json.parseToJsonElement(response.bodyString()))
    }

    @Test
    fun anExerciseIsCreatedEditedAndRetiredInTheDraft() {
        val created = create(pikePushUp())
        assertEquals(CREATED, created.status)
        assertEquals("false", body(created).getValue("published").jsonPrimitive.content)
        assertEquals("added", body(created).getValue("change").jsonPrimitive.content)

        val renamed = goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet")))
        assertEquals(OK, edit("goblet-squat", renamed).status)
        assertEquals("edited", body(get("goblet-squat")).getValue("change").jsonPrimitive.content)
        assertEquals("Squat goblet", test.serverCatalog.get(goblet.slug)!!.exercise.name.byLanguage["fr"])

        assertEquals(OK, retire("wall-sit").status)
        val wallSit = body(get("wall-sit"))
        assertEquals("retired", wallSit.getValue("change").jsonPrimitive.content)
        assertEquals("true", wallSit.getValue("exercise").jsonObject.getValue("retired").jsonPrimitive.content)
        assertEquals(OK, edit("wall-sit", bundled[ExerciseSlug("wall-sit")]!!).status)
        assertTrue(test.serverCatalog.get(ExerciseSlug("wall-sit"))!!.exercise.retired, "an edit keeps the exercise retired")

        val changes = changes()
        assertEquals(listOf("pike-push-up"), slugs(changes.getValue("added")))
        assertEquals(listOf("goblet-squat"), slugs(changes.getValue("edited")))
        assertEquals(listOf("wall-sit"), slugs(changes.getValue("retired")))
        assertEquals(bundled.version + 1, changes.getValue("nextVersion").jsonPrimitive.int)

        assertEquals(
            listOf("admin.catalog.create" to "pike-push-up", "admin.catalog.edit" to "goblet-squat", "admin.catalog.retire" to "wall-sit", "admin.catalog.edit" to "wall-sit"),
            events.filterIsInstance<AdminAction>().map { it.action to it.target },
        )
        assertEquals(NOT_FOUND, retire("no-such-exercise").status)
        assertEquals(NOT_FOUND, edit("no-such-exercise", pikePushUp()).status)
    }

    @Test
    fun aSlugChangeOnAPublishedExerciseIsRefused() {
        val response = edit("goblet-squat", goblet.copy(slug = ExerciseSlug("dumbbell-goblet-squat")))
        assertEquals(CONFLICT, response.status)
        assertEquals("slug-published", body(response).getValue("error").jsonPrimitive.content)
        assertEquals(OK, get("goblet-squat").status)
        assertEquals(NOT_FOUND, get("dumbbell-goblet-squat").status)
        assertTrue(events.none { it is AdminAction })
    }

    @Test
    fun aSlugIsEditableUntilTheExerciseIsFirstPublished() {
        create(pikePushUp())
        assertEquals(CONFLICT, edit("pike-push-up", pikePushUp().copy(slug = ExerciseSlug("goblet-squat"))).status, "taken by another exercise")
        assertEquals(OK, edit("pike-push-up", pikePushUp().copy(slug = ExerciseSlug("pike-pushup"))).status)
        assertEquals(NOT_FOUND, get("pike-push-up").status)
        assertEquals("admin.catalog.edit" to "pike-push-up -> pike-pushup", events.filterIsInstance<AdminAction>().last().let { it.action to it.target })

        assertEquals(OK, publish().status)
        val again = edit("pike-pushup", pikePushUp())
        assertEquals(CONFLICT, again.status)
        assertEquals("slug-published", body(again).getValue("error").jsonPrimitive.content)
    }

    @Test
    fun publishingMakesTheNextVersionWhichThePublicCatalogServesInTheBundledFormat() {
        create(pikePushUp())
        retire("wall-sit")
        val published = publish()
        assertEquals(OK, published.status)
        val version = bundled.version + 1
        assertEquals(version, body(published).getValue("version").jsonPrimitive.int)
        assertEquals("admin.catalog.publish" to version.toString(), events.filterIsInstance<AdminAction>().last().let { it.action to it.target })

        val response = publicCatalog(newerThan = bundled.version)
        assertEquals(OK, response.status)
        val document = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals(setOf("version", "exercises", "variations"), document.keys, "the bundled document's shape")
        val expected = Catalog(version, bundled.exercises.map { if (it.slug.value == "wall-sit") it.copy(retired = true) else it } + pikePushUp(), bundled.variationGroups)
        assertEquals(Json.parseToJsonElement(expected.toJson()), document)
        val served = Catalog.fromJson(response.bodyString())
        assertEquals(version, served.version)
        assertEquals(pikePushUp(), served[ExerciseSlug("pike-push-up")])
        assertTrue(served[ExerciseSlug("wall-sit")]!!.retired)
        assertEquals(NO_CONTENT, publicCatalog(newerThan = version).status)

        val after = changes()
        assertEquals(version, after.getValue("version").jsonPrimitive.int)
        assertTrue(listOf("added", "edited", "retired").all { after.getValue(it).jsonArray.isEmpty() })
        assertEquals("true", body(get("pike-push-up")).getValue("published").jsonPrimitive.content)
        assertEquals(JsonNull, body(get("pike-push-up")).getValue("change"))
    }

    @Test
    fun publishingNeedsEveryTextInEnglishAndFrench() {
        assertEquals(CREATED, create(pikePushUp(fr = false)).status, "the draft may wait for French")
        val missing = changes().getValue("missing").jsonArray.single().jsonObject
        assertEquals("pike-push-up", missing.getValue("slug").jsonPrimitive.content)
        assertEquals(listOf("fr"), missing.getValue("languages").jsonArray.map { it.jsonPrimitive.content })

        val refused = publish()
        assertEquals(CONFLICT, refused.status)
        assertEquals("missing-translations", body(refused).getValue("error").jsonPrimitive.content)
        assertEquals(listOf("pike-push-up"), slugs(body(refused).getValue("missing")))
        assertEquals(bundled.version, test.serverCatalog.latestVersion())

        edit("pike-push-up", pikePushUp().copy(variationNote = LocalizedText(mapOf("en" to "Hips up", "fr" to " "))))
        assertEquals(CONFLICT, publish().status, "a blank text is missing too")
        edit("pike-push-up", pikePushUp())
        assertEquals(OK, publish().status)
    }

    @Test
    fun publishingNothingIsRefused() {
        val response = publish()
        assertEquals(CONFLICT, response.status)
        assertEquals("no-changes", body(response).getValue("error").jsonPrimitive.content)
        assertTrue(events.none { it is AdminAction })
    }

    @Test
    fun anInvalidOrDuplicateExerciseIsRefused() {
        assertEquals(CONFLICT, create(goblet).status)
        assertEquals("slug-taken", body(create(goblet)).getValue("error").jsonPrimitive.content)
        listOf(
            ServerCatalog.encode(pikePushUp().copy(slug = ExerciseSlug("Pike Push-Up"))),
            ServerCatalog.encode(pikePushUp().copy(name = LocalizedText(mapOf("en" to " ", "fr" to "Pompe en V")))),
            """{"slug":"pike-push-up","name":{"fr":"Pompe en V"},"movementPattern":"PUSH","muscleGroups":[],"measurement":"REPS"}""",
            """{"slug":"pike-push-up","name":{"en":"Pike"},"movementPattern":"PUSH","muscleGroups":[],"measurement":"REPS","colour":"red"}""",
            "not json",
        ).forEach {
            val response = create(it)
            assertEquals(BAD_REQUEST, response.status, it)
            assertEquals("invalid-exercise", body(response).getValue("error").jsonPrimitive.content)
        }
        assertTrue(events.none { it is AdminAction })
    }

    @Test
    fun theListSearchesAndFiltersRetiredExercises() {
        retire("wall-sit")
        fun list(vararg params: Pair<String, String>) =
            body(call(params.fold(Request(GET, "/api/v1/admin/catalog/exercises")) { request, (name, value) -> request.query(name, value) }))
        fun listed(vararg params: Pair<String, String>) =
            list(*params).getValue("exercises").jsonArray.map { it.jsonObject.getValue("exercise").jsonObject.getValue("slug").jsonPrimitive.content }

        assertEquals(bundled.exercises.size, list().getValue("total").jsonPrimitive.int)
        assertEquals(listOf("wall-sit"), listed("retired" to "yes"))
        assertFalse("wall-sit" in listed("retired" to "no"))
        assertEquals(listOf("goblet-squat"), listed("q" to "GOBELET"), "a French name matches")
        assertEquals(BAD_REQUEST, call(Request(GET, "/api/v1/admin/catalog/exercises?retired=maybe")).status)
    }

    @Test
    fun onlyAnAdminOnTheWebsiteReachesTheCatalog() {
        account("lea@example.com")
        val member = signIn("lea@example.com", Client.Website).tokens.accessToken
        assertEquals(FORBIDDEN, call(Request(GET, "/api/v1/admin/catalog/exercises"), member).status)
        assertEquals(FORBIDDEN, create(ServerCatalog.encode(pikePushUp()), member).status)
        assertEquals(FORBIDDEN, call(Request(POST, "/api/v1/admin/catalog/publish"), signIn("ana@example.com").tokens.accessToken).status)
    }

    @Test
    fun theOpenApiDescriptionListsTheCatalogRoutes() {
        val paths = body(http(Request(GET, "/api/v1/openapi.json"))).getValue("paths").jsonObject
        listOf("/api/v1/admin/catalog/exercises", "/api/v1/admin/catalog/exercises/{slug}", "/api/v1/admin/catalog/changes", "/api/v1/admin/catalog/publish")
            .forEach { assertTrue(it in paths, "paths were ${paths.keys}") }
    }
}
