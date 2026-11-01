package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.Pronouns
import ch.coachpoulpy.core.model.SexAssignedAtBirth
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileAndBodyTest {
    private val today = LocalDate(2026, 11, 1)

    @Test
    fun profileIsKeptOnThePhoneAndEveryFieldIsOptional() {
        val memory = MemoryStore()
        val model = ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined))
        assertNull(model.profile.value.sexAssignedAtBirth)

        model.setDisplayName("  Alex ")
        model.setBirthdate(LocalDate(1991, 3, 14))
        model.setSexAssignedAtBirth(SexAssignedAtBirth.PREFER_NOT_TO_SAY)
        model.setPronouns(Pronouns.CUSTOM, " ze / hir ")

        val reopened = ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined)).profile.value
        assertEquals("Alex", reopened.displayName)
        assertEquals(35, reopened.age(today))
        assertEquals(SexAssignedAtBirth.PREFER_NOT_TO_SAY, reopened.sexAssignedAtBirth)
        assertEquals("ze / hir", reopened.customPronouns)
    }

    @Test
    fun customPronounsAreForgottenWhenAnotherChoiceIsMade() {
        val model = ProfileModel(MemoryStore(), CoroutineScope(Dispatchers.Unconfined))
        model.setPronouns(Pronouns.CUSTOM, "ze / hir")
        model.setPronouns(Pronouns.THEY_THEM, "ze / hir")
        assertNull(model.profile.value.customPronouns)
        model.setDisplayName("  ")
        assertNull(model.profile.value.displayName)
        model.setRestingHeartRate(400)
        assertNull(model.profile.value.restingHeartRate)
    }

    @Test
    fun loggingTwiceInADayKeepsOneEntryAndFutureDaysAreRefused() = runTest {
        val model = BodyModel(BodyMetric.WEIGHT, LocalStore(DriverFactory()), { today }, CoroutineScope(Dispatchers.Unconfined))
        model.log(72.6)
        model.log(72.9)
        model.log(70.0, LocalDate(2026, 11, 2))
        model.log(-1.0, LocalDate(2026, 10, 1))

        val state = model.state.first { it.latest?.value == 72.9 }
        assertEquals(1, state.history.size)
    }

    @Test
    fun heightsConvertToFeetAndHalfInchesAndBack() {
        assertEquals(FeetInches(5, 9.0), Units.feetAndInches(175.0))
        assertEquals(FeetInches(5, 0.0), Units.feetAndInches(152.4))
        assertEquals(FeetInches(1, 0.0), Units.feetAndInches(30.4))
        assertEquals(175.26, Units.centimetresOf(5, 9.0), 1e-9)
    }

    @Test
    fun theProfileReachesSwiftAsPlainValues() {
        val model = ProfileModel(MemoryStore(), CoroutineScope(Dispatchers.Unconfined), { today })
        assertEquals("", model.ui.value.displayName)
        assertNull(model.ui.value.birthdate)

        model.setBirthdateTo(PlanDate(1991, 3, 14))
        model.setRestingHeartRateText("5a8 bpm")

        val ui = model.ui.value
        assertEquals(PlanDate(1991, 3, 14), ui.birthdate)
        assertEquals(35, ui.age)
        assertEquals("58", ui.restingHeartRate)

        model.setBirthdateTo(PlanDate(2001, 2, 31))
        assertNull(model.ui.value.birthdate)
        model.clearBirthdate()
        assertNull(model.ui.value.age)
    }

    @Test
    fun theBodyLogReachesSwiftAsPlainPoints() = runTest {
        val model = BodyModel(BodyMetric.WEIGHT, LocalStore(DriverFactory()), { today }, CoroutineScope(Dispatchers.Unconfined))
        model.logOn(73.0, PlanDate(2026, 10, 15))
        model.logOn(72.6, PlanDate(2026, 10, 29))

        val ui = model.ui.first { it.points.size == 2 }
        assertEquals(listOf(PlanDate(2026, 10, 15), PlanDate(2026, 10, 29)), ui.points.map { it.date })
        assertEquals(PlanDate(2026, 10, 29), ui.history.first().date)
        assertEquals(72.6, ui.lowest!!.value)
        assertEquals(-0.4, ui.change!!, 1e-9)
    }
}
