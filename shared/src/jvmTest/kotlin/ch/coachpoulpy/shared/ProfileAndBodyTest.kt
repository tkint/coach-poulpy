package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.PainDuration
import ch.coachpoulpy.core.model.Experience
import ch.coachpoulpy.core.model.TrainingGoal
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

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
    fun severalGoalsAreKeptAndExperienceHasFiveSteps() {
        val memory = MemoryStore()
        val model = ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined))
        model.setGoals(setOf(TrainingGoal.RUN_FURTHER, TrainingGoal.BUILD_STRENGTH))
        model.setExperience(Experience.VERY_EXPERIENCED)

        val reopened = ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined)).profile.value
        assertEquals(setOf(TrainingGoal.BUILD_STRENGTH, TrainingGoal.RUN_FURTHER), reopened.goals)
        assertEquals(Experience.VERY_EXPERIENCED, reopened.experience)
        assertEquals(5, Experience.entries.size)

        model.setGoals(emptySet())
        assertEquals(emptySet(), ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined)).profile.value.goals)
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

        model.setGoalsTo(listOf(TrainingGoal.RUN_FURTHER, TrainingGoal.STAY_ACTIVE))
        assertEquals(listOf(TrainingGoal.STAY_ACTIVE, TrainingGoal.RUN_FURTHER), model.ui.value.goals)

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

    private fun profileAt(memory: MemoryStore, now: () -> Instant) =
        ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined), { today }, now)

    @Test
    fun painKeepsPoulpyOnTheBlueSideUntilItsTimeIsUp() {
        var now = Instant.parse("2026-11-01T10:00:00Z")
        val model = profileAt(MemoryStore()) { now }
        assertFalse(model.painActive())

        model.setPain(listOf("knee-left", "hip-left"), PainDuration.HOURS_72)
        assertTrue(model.painActive())
        assertEquals("Left hip, Left knee", model.painSummary("en"))

        now += 71.hours
        assertTrue(model.painActive())
        now += 2.hours
        assertFalse(model.painActive())
    }

    @Test
    fun painLastingUntilClearedNeverEndsAndSettingItAgainStartsTheTimeOver() {
        var now = Instant.parse("2026-11-01T10:00:00Z")
        val model = profileAt(MemoryStore()) { now }
        model.setPain(listOf("back"), PainDuration.UNTIL_CLEARED)
        assertFalse(model.painActive(), "back is not an area key, so nothing was set")
        model.setPain(listOf("lower-back-left"), PainDuration.UNTIL_CLEARED)
        now += 365.days
        assertTrue(model.painActive())
    }

    @Test
    fun painIsKeptOnThePhoneWithItsAreasInReadingOrder() {
        val memory = MemoryStore()
        val now = Instant.parse("2026-11-01T10:00:00Z")
        profileAt(memory) { now }.setPain(listOf("knee-right", "neck-left", "nowhere", "other"), PainDuration.ONE_WEEK)

        val reopened = profileAt(memory) { now + 24.hours }
        val ui = reopened.ui.value
        assertEquals(listOf("neck-left", "knee-right", "other"), ui.painAreas)
        assertEquals(PainDuration.ONE_WEEK, ui.painDuration)
        assertEquals(PlanDate(2026, 11, 8), ui.painUntil)
        assertTrue(ui.painActive)
    }

    @Test
    fun clearingPainOrChoosingNoAreaEndsIt() {
        val now = Instant.parse("2026-11-01T10:00:00Z")
        val model = profileAt(MemoryStore()) { now }
        model.setPain(listOf("knee-left"), PainDuration.TWO_WEEKS)
        model.clearPain()
        assertFalse(model.painActive())
        assertNull(model.ui.value.painDuration)

        model.setPain(listOf("knee-left"), PainDuration.TWO_WEEKS)
        model.setPain(emptyList(), PainDuration.TWO_WEEKS)
        assertFalse(model.painActive())
    }

    @Test
    fun theBodyFigureIsItsOwnChoiceAndIsKept() {
        val memory = MemoryStore()
        val model = ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined))
        assertNull(model.profile.value.bodyFigure)
        assertNull(model.profile.value.sexAssignedAtBirth)

        model.setBodyFigure(BodyFigure.HIPS)
        assertEquals(BodyFigure.HIPS, ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined)).profile.value.bodyFigure)
        assertNull(ProfileModel(memory, CoroutineScope(Dispatchers.Unconfined)).profile.value.sexAssignedAtBirth)
    }

    @Test
    fun everyAreaOfEveryFigureHasAFrenchAndAnEnglishName() {
        val keys = BodyFigure.entries.flatMap { figure -> BodyView.entries.flatMap { BodyMapShapes.regions(figure, it) } }.mapNotNull { it.key }.toSet()
        assertTrue(keys.all { it in BodyMap.AREAS }, "a drawn area that the model does not know")
        assertTrue(BodyMap.AREAS.all { it in keys }, "an area that no figure draws")
        BodyMap.AREAS.forEach {
            assertTrue(BodyMap.label(it, "en") != it && BodyMap.label(it, "fr") != it, it)
        }
        assertEquals("Genou gauche", BodyMap.label("knee-left", "fr"))
        assertEquals("Épaule droite", BodyMap.label("deltoid-right", "fr"))
    }
}
