package ch.coachpoulpy.server.api

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

    private fun publish(vararg slugs: String) = call(
        Request(POST, "/api/v1/admin/catalog/publish").let { if (slugs.isEmpty()) it else withJson(it, """{"slugs": [${slugs.joinToString { s -> "\"$s\"" }}]}""") },
    )

    private fun slugs(list: Any) = (list as kotlinx.serialization.json.JsonArray).map { it.jsonObject.getValue("slug").jsonPrimitive.content }

    private fun publicCatalog(after: String? = null) =
        http(Request(GET, "/api/v1/catalog/exercises").let { if (after == null) it else it.query("after", after) })

    /** The `seq` of the latest published revision: what the cursor counts. */
    private fun lastSeq(): Long? = test.database.queries.catalogQueries.lastSeq().executeAsOne().latest

    /** The slugs of an answer of the public route, in order. */
    private fun answered(response: Response) = body(response).getValue("exercises").jsonArray.map { it.jsonObject.getValue("exercise").jsonObject.getValue("slug").jsonPrimitive.content }

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
        assertEquals(bundled.exercises.size.toLong(), lastSeq(), "each seeded exercise takes a number of the cursor")
        assertEquals(bundled.exercises, test.serverCatalog.draft().map { it.exercise })
        assertTrue(test.serverCatalog.draft().all { it.published && it.change == null })
        assertFalse(test.serverCatalog.seed(), "a second start leaves the catalog alone")
        val rows = test.database.queries.catalogQueries.publishedExercises().executeAsList()
        assertEquals(bundled.exercises.map { it.slug.value }, rows.map { it.slug }, "one row per exercise, in the catalog's order")
        val response = publicCatalog()
        assertEquals(OK, response.status)
        val served = body(response).getValue("exercises").jsonArray.map { it.jsonObject.getValue("exercise") }
        assertEquals(bundled.exercises.map { Json.parseToJsonElement(ServerCatalog.encode(it)) }, served)
        assertTrue(body(response).getValue("exercises").jsonArray.all { it.jsonObject.getValue("revision").jsonPrimitive.int == 1 }, "bundled exercises are revision 1")
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
    fun publishingGivesEachChangedExerciseARevisionThePublicRouteServes() {
        val first = body(publicCatalog()).getValue("cursor").jsonPrimitive.content
        create(pikePushUp())
        retire("wall-sit")
        val published = publish()
        assertEquals(OK, published.status)
        assertEquals(listOf("pike-push-up", "wall-sit"), body(published).getValue("slugs").jsonArray.map { it.jsonPrimitive.content })
        assertEquals("admin.catalog.publish" to "pike-push-up,wall-sit", events.filterIsInstance<AdminAction>().last().let { it.action to it.target })

        val response = publicCatalog(after = first)
        assertEquals(OK, response.status)
        assertEquals(listOf("pike-push-up", "wall-sit"), answered(response), "only what was published after the cursor")
        val entries = body(response).getValue("exercises").jsonArray.map { it.jsonObject }
        assertEquals(listOf(1, 2), entries.map { it.getValue("revision").jsonPrimitive.int })
        assertEquals(pikePushUp(), ServerCatalog.decode(entries[0].getValue("exercise").toString()))
        assertEquals("true", entries[1].getValue("exercise").jsonObject.getValue("retired").jsonPrimitive.content)
        val cursor = body(response).getValue("cursor").jsonPrimitive.content
        assertEquals((bundled.exercises.size + 2).toString(), cursor)
        assertEquals(NO_CONTENT, publicCatalog(after = cursor).status)

        val after = changes()
        assertTrue(listOf("added", "edited", "retired").all { after.getValue(it).jsonArray.isEmpty() })
        assertEquals("true", body(get("pike-push-up")).getValue("published").jsonPrimitive.content)
        assertEquals(JsonNull, body(get("pike-push-up")).getValue("change"))
    }

    @Test
    fun onlyTheNamedExercisesArePublished() {
        create(pikePushUp())
        edit("goblet-squat", goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet"))))
        val seq = lastSeq()!!
        val published = publish("pike-push-up")
        assertEquals(OK, published.status)
        assertEquals(listOf("pike-push-up"), body(published).getValue("slugs").jsonArray.map { it.jsonPrimitive.content })
        assertEquals(seq + 1, lastSeq())
        assertEquals(listOf("goblet-squat"), slugs(changes().getValue("edited")), "the other exercise still waits")
        assertEquals(CONFLICT, publish("wall-sit").status, "an exercise with nothing to publish is no change")
        assertEquals(BAD_REQUEST, call(withJson(Request(POST, "/api/v1/admin/catalog/publish"), "{\"slugs\": 3}")).status)
    }

    @Test
    fun aTranslationMissingElsewhereDoesNotHoldBackTheNamedExercise() {
        create(pikePushUp(fr = false))
        edit("goblet-squat", goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet"))))
        assertEquals(OK, publish("goblet-squat").status)
        assertEquals(CONFLICT, publish("pike-push-up").status)
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
        assertEquals(bundled.exercises.size.toLong(), lastSeq())

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
    fun theListSearchesAndFiltersByLifecycle() {
        retire("wall-sit")
        fun list(vararg params: Pair<String, String>) =
            body(call(params.fold(Request(GET, "/api/v1/admin/catalog/exercises")) { request, (name, value) -> request.query(name, value) }))
        fun listed(vararg params: Pair<String, String>) =
            list(*params).getValue("exercises").jsonArray.map { it.jsonObject.getValue("exercise").jsonObject.getValue("slug").jsonPrimitive.content }

        assertEquals(bundled.exercises.size, list().getValue("total").jsonPrimitive.int)
        assertEquals(listOf("wall-sit"), listed("lifecycle" to "retiring"), "retired in the draft only: its retirement is pending")
        assertTrue("wall-sit" !in listed("lifecycle" to "active"))
        assertEquals(listOf("goblet-squat"), listed("q" to "GOBELET"), "a French name matches")
        assertEquals(BAD_REQUEST, call(Request(GET, "/api/v1/admin/catalog/exercises?lifecycle=maybe")).status)
        assertEquals(BAD_REQUEST, call(Request(GET, "/api/v1/admin/catalog/exercises?muscles=quadriceps,wings")).status)
    }

    @Test
    fun theListFiltersByWhatTheExerciseIs() {
        val squats = listed("pattern" to "squat").first
        assertTrue("goblet-squat" in squats)
        assertTrue(squats.all { bundled[ExerciseSlug(it)]!!.movementPattern == MovementPattern.SQUAT })
        val worked = listed("muscles" to "quadriceps,calves").first
        assertTrue(worked.all { slug -> bundled[ExerciseSlug(slug)]!!.muscleGroups.any { it == MuscleGroup.QUADRICEPS || it == MuscleGroup.CALVES } })
        assertEquals(listed("muscles" to "quadriceps,calves").second, worked.size)
        assertTrue(listed("pattern" to "squat", "muscles" to "chest").first.all { it in squats }, "filters narrow one another")
    }

    @Test
    fun theListSeparatesContentTranslationAndLifecycle() {
        create(pikePushUp(fr = false))
        edit("goblet-squat", goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat gobelet !"))))
        retire("wall-sit")

        assertEquals(listOf("pike-push-up"), listed("content" to "draft").first)
        assertEquals(listOf("goblet-squat"), listed("content" to "modified").first)
        assertEquals(setOf("pike-push-up", "goblet-squat", "wall-sit"), listed("content" to "pending").first.toSet())
        assertTrue("pike-push-up" !in listed("content" to "published").first)
        assertTrue("wall-sit" in listed("content" to "published").first, "a retirement is a lifecycle change, not a content one")
        assertEquals(listOf("pike-push-up"), listed("translation" to "incomplete").first)
        assertEquals(listOf("fr"), body(get("pike-push-up")).getValue("missing").jsonArray.map { it.jsonPrimitive.content }, "each exercise names its missing languages")
        assertTrue(body(get("wall-sit")).getValue("missing").jsonArray.isEmpty())
        assertTrue("pike-push-up" !in listed("translation" to "complete").first)
        assertEquals(listOf("pike-push-up"), listed("lifecycle" to "active", "translation" to "incomplete").first, "the dimensions combine")
    }

    private fun listed(vararg params: Pair<String, String>): Pair<List<String>, Int> {
        val list = body(call(params.fold(Request(GET, "/api/v1/admin/catalog/exercises")) { request, (name, value) -> request.query(name, value) }))
        val slugs = list.getValue("exercises").jsonArray.map { it.jsonObject.getValue("exercise").jsonObject.getValue("slug").jsonPrimitive.content }
        return slugs to list.getValue("total").jsonPrimitive.int
    }

    @Test
    fun theListIsPagedAndSorted() {
        val all = bundled.exercises.map { it.slug.value }
        assertEquals(all.take(20) to all.size, listed(), "20 a page by default, in the catalog's order")
        assertEquals(all.drop(8).take(8) to all.size, listed("size" to "8", "page" to "2"))
        val byName = bundled.exercises.sortedBy { it.name.byLanguage.getValue("en").lowercase() }.map { it.slug.value }
        assertEquals(byName.take(8), listed("sort" to "name", "size" to "8").first)
        assertEquals(byName.reversed().take(8), listed("sort" to "-name", "size" to "8").first)

        val byPatternThenName = bundled.exercises.sortedWith(compareBy({ it.movementPattern.name }, { it.name.byLanguage.getValue("en").lowercase() })).map { it.slug.value }
        assertEquals(byPatternThenName.take(8), listed("sort" to "pattern,name", "size" to "8").first, "a second column orders the ties of the first")
        val byPatternDescThenName = bundled.exercises.sortedWith(compareBy<Exercise>({ it.movementPattern.name }).reversed().thenBy { it.name.byLanguage.getValue("en").lowercase() }).map { it.slug.value }
        assertEquals(byPatternDescThenName.take(5), listed("sort" to "-pattern,name", "size" to "5").first, "each column has its own direction")

        create(pikePushUp(fr = false))
        assertEquals("pike-push-up", listed("sort" to "languages", "size" to "1").first.single(), "missing French comes first")
        assertEquals("pike-push-up", listed("sort" to "change", "size" to "1").first.single(), "changes come first")
        listOf("sort" to "colour", "sort" to "name,-name", "sort" to "name,,pattern", "lifecycle" to "maybe", "content" to "later", "translation" to "half").forEach { (name, value) ->
            assertEquals(BAD_REQUEST, call(Request(GET, "/api/v1/admin/catalog/exercises").query(name, value)).status, name)
        }
    }

    @Test
    fun aRetiredExerciseIsOfferedAgain() {
        retire("wall-sit")
        assertEquals(OK, call(Request(POST, "/api/v1/admin/catalog/exercises/wall-sit/offer")).status)
        assertEquals(JsonNull, body(get("wall-sit")).getValue("change"), "back as its latest published revision has it")

        retire("wall-sit")
        publish()
        val offered = call(Request(POST, "/api/v1/admin/catalog/exercises/wall-sit/offer"))
        assertEquals(OK, offered.status)
        assertEquals("offered", body(offered).getValue("change").jsonPrimitive.content)
        assertEquals(listOf("wall-sit"), slugs(changes().getValue("offered")))
        assertEquals("admin.catalog.offer" to "wall-sit", events.filterIsInstance<AdminAction>().last().let { it.action to it.target })
        assertEquals(OK, publish().status)
        val wallSit = body(publicCatalog()).getValue("exercises").jsonArray.map { it.jsonObject }
            .last { it.getValue("exercise").jsonObject.getValue("slug").jsonPrimitive.content == "wall-sit" }
        assertEquals(3, wallSit.getValue("revision").jsonPrimitive.int)
        assertEquals(null, wallSit.getValue("exercise").jsonObject["retired"], "offered again: the default, so left out of the JSON")
        assertEquals(NOT_FOUND, call(Request(POST, "/api/v1/admin/catalog/exercises/no-such-exercise/offer")).status)
    }

    @Test
    fun anEditedExerciseShowsEachChangedFieldBeforeAndAfter() {
        edit(
            "goblet-squat",
            goblet.copy(
                name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet")),
                muscleGroups = goblet.muscleGroups + MuscleGroup.HAMSTRINGS,
                instructions = listOf(goblet.instructions[0], LocalizedText(goblet.instructions[1].byLanguage + ("en" to "Sit between your heels, then stand."))),
            ),
        )
        val edited = changes().getValue("edited").jsonArray.single().jsonObject
        assertEquals("goblet-squat", edited.getValue("slug").jsonPrimitive.content)
        val fields = edited.getValue("fields").jsonArray.map { it.jsonObject }
        assertEquals(listOf("name.fr", "muscleGroups", "instructions.1.en"), fields.map { it.getValue("field").jsonPrimitive.content })
        assertEquals("Squat gobelet", fields[0].getValue("before").jsonPrimitive.content)
        assertEquals("Squat goblet", fields[0].getValue("after").jsonPrimitive.content)
        assertEquals(listOf("QUADRICEPS", "GLUTES", "CORE", "HAMSTRINGS"), fields[1].getValue("after").jsonArray.map { it.jsonPrimitive.content })
        assertEquals("Sit between your heels, then stand.", fields[2].getValue("after").jsonPrimitive.content)

        edit("goblet-squat", goblet.copy(variationNote = null))
        val removed = changes().getValue("edited").jsonArray.single().jsonObject.getValue("fields").jsonArray.map { it.jsonObject }
        assertEquals(listOf("variationNote.en", "variationNote.fr"), removed.map { it.getValue("field").jsonPrimitive.content })
        assertEquals(JsonNull, removed[0].getValue("after"), "a removed field has no value after")
    }

    private val revisions get() = test.database.queries.catalogQueries

    @Test
    fun aPublishWithOneEditedExerciseWritesOneRevision() {
        val before = revisions.revisionCount().executeAsOne()
        val renamed = goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet")))
        edit("goblet-squat", renamed)
        edit("goblet-squat", renamed.copy(muscleGroups = renamed.muscleGroups + MuscleGroup.HAMSTRINGS))
        assertEquals(before + 1, revisions.revisionCount().executeAsOne(), "edits change the one draft revision")
        assertEquals(OK, publish().status)
        assertEquals(before + 1, revisions.revisionCount().executeAsOne(), "the other exercises get no new row")
        assertEquals(
            listOf<Pair<Int, Long?>>(1 to bundled.exercises.indexOf(goblet) + 1L, 2 to bundled.exercises.size + 1L),
            revisions.revisionsOf("goblet-squat").executeAsList().map { it.revision to it.seq },
        )
    }

    @Test
    fun aDraftThatComesBackToTheLatestPublishedRevisionIsDropped() {
        val before = revisions.revisionCount().executeAsOne()
        edit("goblet-squat", goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet"))))
        edit("goblet-squat", goblet)
        assertEquals(before, revisions.revisionCount().executeAsOne())
        assertEquals(CONFLICT, publish().status, "nothing to publish")
        retire("wall-sit")
        call(Request(POST, "/api/v1/admin/catalog/exercises/wall-sit/offer"))
        assertEquals(before, revisions.revisionCount().executeAsOne())
    }

    @Test
    fun aPhoneThatAsksAfterItsCursorGetsOnlyTheLaterRevisions() {
        val first = body(publicCatalog()).getValue("cursor").jsonPrimitive.content
        edit("goblet-squat", goblet.copy(name = LocalizedText(mapOf("en" to "Goblet squat", "fr" to "Squat goblet"))))
        publish()
        val second = publicCatalog(after = first)
        assertEquals(listOf("goblet-squat"), answered(second))
        assertEquals(2, body(second).getValue("exercises").jsonArray.single().jsonObject.getValue("revision").jsonPrimitive.int)

        create(pikePushUp())
        edit("goblet-squat", goblet.copy(name = LocalizedText(mapOf("en" to "Dumbbell goblet squat", "fr" to "Squat goblet"))))
        publish()
        val third = publicCatalog(after = body(second).getValue("cursor").jsonPrimitive.content)
        assertEquals(listOf("goblet-squat", "pike-push-up"), answered(third).sorted())
        val goblet = body(third).getValue("exercises").jsonArray.map { it.jsonObject }.first { it.getValue("exercise").jsonObject.getValue("slug").jsonPrimitive.content == "goblet-squat" }
        assertEquals(3, goblet.getValue("revision").jsonPrimitive.int)
        assertEquals(bundled.exercises.size + 3, body(third).getValue("cursor").jsonPrimitive.content.toInt())

        val full = answered(publicCatalog())
        assertEquals(bundled.exercises.size + 1, full.size, "without a cursor, every exercise once, at its latest revision")
        assertEquals(full.size, full.toSet().size)
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
