package ch.coachpoulpy.server.api

import ch.coachpoulpy.core.model.BodyMeasurementChange
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.PlannedWorkoutChange
import ch.coachpoulpy.core.model.PlannedWorkoutDeletedChange
import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.accounts.SignUp
import ch.coachpoulpy.server.auth.SignInResult
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.test.assertEquals

class SmallFieldsSyncTest {
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

    private fun pull(): List<SyncChange> {
        val response = test.http(Request(GET, "/api/v1/sync/pull").header("Authorization", "Bearer $phone"))
        assertEquals(OK, response.status, response.bodyString())
        return Json.decodeFromString(SyncPage.serializer(), response.bodyString()).changes
    }

    private fun at(minute: Int) = Instant.parse("2026-11-07T10:${minute.toString().padStart(2, '0')}:00Z")

    private fun planned(day: String, minute: Int) = PlannedWorkoutChange(PLAN, WorkoutId(WORKOUT), LocalDate.parse(day), at(0), at(minute))

    private fun weight(value: Double?, minute: Int) = BodyMeasurementChange(BodyMetric.WEIGHT, LocalDate.parse("2026-11-07"), value, at(minute))

    private fun units(value: String?, minute: Int) = SettingChange("settings.units", value, at(minute))

    @Test
    fun theLatestMoveWinsInEitherOrder() {
        push(planned("2026-11-09", 1), planned("2026-11-10", 3))
        push(planned("2026-11-11", 2))
        assertEquals(listOf<SyncChange>(planned("2026-11-10", 3)), pull())
    }

    @Test
    fun aDeleteWinsOverAnOlderMoveAndLosesToALaterOne() {
        push(planned("2026-11-09", 1), PlannedWorkoutDeletedChange(PLAN, at(5)))
        push(planned("2026-11-10", 4))
        assertEquals(listOf<SyncChange>(PlannedWorkoutDeletedChange(PLAN, at(5))), pull())
        push(planned("2026-11-12", 6))
        assertEquals(listOf<SyncChange>(planned("2026-11-12", 6)), pull())
    }

    @Test
    fun deletingAPlannedWorkoutTheServerNeverHadChangesNothing() {
        assertEquals(OK, push(PlannedWorkoutDeletedChange(PLAN, at(5))).status)
        assertEquals(emptyList(), pull())
    }

    @Test
    fun twoPhonesLoggingTheSameDayKeepTheLaterWeight() {
        push(weight(71.5, 2))
        push(weight(70.8, 1))
        assertEquals(listOf<SyncChange>(weight(71.5, 2)), pull())
        push(weight(null, 3))
        assertEquals(listOf<SyncChange>(weight(null, 3)), pull())
    }

    @Test
    fun aMeasurementIsAPositiveNumber() {
        assertEquals(BAD_REQUEST, push(weight(-2.0, 1)).status)
        assertEquals(BAD_REQUEST, push(weight(0.0, 1)).status)
    }

    @Test
    fun settingsKeepTheLatestWritePerKey() {
        push(units("IMPERIAL", 2), SettingChange("profile.name", "Ana", at(1)))
        push(units("METRIC", 1), SettingChange("profile.name", null, at(3)))
        assertEquals(listOf<SyncChange>(units("IMPERIAL", 2), SettingChange("profile.name", null, at(3))), pull())
    }

    @Test
    fun onlyKeysThatFollowTheAccountAreTaken() {
        assertEquals(BAD_REQUEST, push(SettingChange("settings.theme", "DARK", at(1))).status)
        assertEquals(BAD_REQUEST, push(SettingChange("devices.treadmill", "x", at(1))).status)
        assertEquals(OK, push(SettingChange("settings.language", "FRENCH", at(1))).status)
    }

    @Test
    fun theSameWriteAgainChangesNothing() {
        push(planned("2026-11-09", 1), weight(71.5, 1), units("METRIC", 1))
        val before = pull().size
        push(planned("2026-11-09", 1), weight(71.5, 1), units("METRIC", 1))
        assertEquals(3, before)
        assertEquals(3, pull().size)
        assertEquals("3", test.database.queries.syncQueries.lastChange(
            test.database.queries.accountQueries.byEmail("ana@example.com").executeAsOne().id,
        ).executeAsOne().toString())
    }

    companion object {
        const val PLAN = "0190d4d2-0000-7000-8000-000000000020"
        const val WORKOUT = "0190d4d2-0000-7000-8000-000000000009"
    }
}
