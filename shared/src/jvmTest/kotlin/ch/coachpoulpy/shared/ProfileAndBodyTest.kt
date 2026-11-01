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

        val state = model.state.first { it.history.size == 1 }
        assertEquals(72.9, state.latest!!.value)
    }

    @Test
    fun heightsConvertToFeetAndHalfInchesAndBack() {
        assertEquals(FeetInches(5, 9.0), Units.feetAndInches(175.0))
        assertEquals(FeetInches(5, 0.0), Units.feetAndInches(152.4))
        assertEquals(FeetInches(1, 0.0), Units.feetAndInches(30.4))
        assertEquals(175.26, Units.centimetresOf(5, 9.0), 1e-9)
    }
}
