package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutDeletedChange
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.SignInResult
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutSyncTest {
    private val test = TestServer()
    private val phone = signedIn()

    private fun signedIn(): String {
        test.accounts.signUp(SignUp("ana@example.com", "correct horse", "en", healthDataConsent = true))
        check(test.accounts.confirm(test.mailer.sent.last().text.substringAfter("#").substringBefore("\n")))
        return (test.signIns.signIn("ana@example.com", "correct horse", "Phone") as SignInResult.Signed).tokens.accessToken
    }

    private fun push(vararg changes: SyncChange): Response = test.http(
        Request(POST, "/api/v1/sync/push").header("Authorization", "Bearer $phone").header("Content-Type", "application/json")
            .body(Json.encodeToString(SyncPush.serializer(), SyncPush(changes.toList()))),
    )

    private fun pull(cursor: String? = null): SyncPage {
        val response = test.http(
            Request(GET, "/api/v1/sync/pull").header("Authorization", "Bearer $phone").let { if (cursor != null) it.query("cursor", cursor) else it },
        )
        assertEquals(OK, response.status, response.bodyString())
        return Json.decodeFromString(SyncPage.serializer(), response.bodyString())
    }

    private fun version(n: Int, name: String = "Leg day", id: String = WORKOUT) = WorkoutVersionChange(
        Workout(WorkoutId(id), n, name, WorkoutFormat.SETS, listOf(Part(PartId("work"), PartKind.WORK, emptyList()))),
        baseVersion = (n - 1).takeIf { it > 0 },
        createdAt = Instant.parse("2026-11-07T10:00:00Z"),
    )

    private fun deleted(n: Int) = WorkoutDeletedChange(WorkoutId(WORKOUT), n, Instant.parse("2026-11-07T11:00:00Z"))

    private fun Response.json() = Json.parseToJsonElement(bodyString()).jsonObject

    @Test
    fun versionsComeBackOnAPullInOrder() {
        assertEquals(OK, push(version(1), version(2, "Leg day, harder")).status)
        val changes = pull().changes
        assertEquals(listOf(version(1), version(2, "Leg day, harder")), changes)
    }

    @Test
    fun theSameVersionAgainChangesNothing() {
        push(version(1))
        val cursor = pull().cursor
        assertEquals(OK, push(version(1)).status)
        assertEquals(emptyList(), pull(cursor).changes)
    }

    @Test
    fun aSecondPhoneMakingTheSameVersionGetsStaleBaseAndNothingIsStored() {
        push(version(1), version(2, "Ana's iPhone edit"))
        val cursor = pull().cursor
        val response = push(version(2, "Ana's iPad edit"), version(3, "Ana's iPad edit, again"))
        assertEquals(CONFLICT, response.status)
        assertEquals("stale-base", response.json().getValue("error").jsonPrimitive.content)
        assertEquals(listOf(WORKOUT), response.json().getValue("workouts").jsonArray.map { it.jsonPrimitive.content })
        assertEquals(emptyList(), pull(cursor).changes)
    }

    @Test
    fun aStaleWorkoutRefusesThePushWithEverythingInIt() {
        push(version(1), version(2))
        val response = push(version(1, id = OTHER), version(2, "From an old base"))
        assertEquals(CONFLICT, response.status)
        assertEquals(emptyList(), pull().changes.filterIsInstance<WorkoutVersionChange>().filter { it.workout.id.value == OTHER })
    }

    @Test
    fun aVersionNotMadeFromTheOneBeforeIsInvalid() {
        assertEquals(BAD_REQUEST, push(version(1).copy(baseVersion = 1)).status)
        assertEquals(BAD_REQUEST, push(version(3)).status)
    }

    @Test
    fun workoutContentThatDoesntParseIsInvalid() {
        val response = test.http(
            Request(POST, "/api/v1/sync/push").header("Authorization", "Bearer $phone").header("Content-Type", "application/json")
                .body("""{"changes":[{"type":"workout-version","workout":{"id":"$WORKOUT","version":1},"baseVersion":null,"createdAt":"2026-11-07T10:00:00Z"}]}"""),
        )
        assertEquals(BAD_REQUEST, response.status)
        assertEquals("invalid-change", response.json().getValue("error").jsonPrimitive.content)
    }

    @Test
    fun aDeletionReachesEveryPhone() {
        push(version(1), version(2))
        val cursor = pull().cursor
        assertEquals(OK, push(deleted(2)).status)
        assertEquals(listOf<SyncChange>(deleted(2)), pull(cursor).changes)
        // Deleting again changes nothing.
        val after = pull().cursor
        push(deleted(2))
        assertEquals(emptyList(), pull(after).changes)
    }

    @Test
    fun aDeletionLosesToANewerVersion() {
        push(version(1), version(2))
        val cursor = pull().cursor
        // The other phone deleted the workout at version 1, before it saw version 2.
        assertEquals(OK, push(deleted(1)).status)
        assertEquals(emptyList(), pull(cursor).changes)
    }

    @Test
    fun aNewVersionAfterADeletionBringsTheWorkoutBack() {
        push(version(1), deleted(1))
        val cursor = pull().cursor
        assertEquals(OK, push(version(2, "Edited before the delete arrived")).status)
        assertEquals(listOf<SyncChange>(version(2, "Edited before the delete arrived")), pull(cursor).changes)
        // A phone pulling everything gets the versions and no deletion.
        assertEquals(listOf(1, 2), pull().changes.map { (it as WorkoutVersionChange).workout.version })
    }

    companion object {
        const val WORKOUT = "0190d4d2-0000-7000-8000-000000000009"
        const val OTHER = "0190d4d2-0000-7000-8000-00000000000a"
    }
}
