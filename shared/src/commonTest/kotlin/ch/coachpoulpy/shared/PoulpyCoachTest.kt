package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class PoulpyCoachTest {
    private val coach = PoulpyCoach("en", TimeZone.UTC)
    private val now = Instant.parse("2026-10-24T18:00:00Z")

    // Duration is a value class, so it cannot be a vararg.
    private fun ago(durations: List<Duration>) = durations.map { now - it }

    @Test
    fun noSessionYetIsAHappyHello() {
        val status = coach.status(emptyList(), now)
        assertEquals(PoulpyDrawing.MOOD_HAPPY, status.drawing)
        assertEquals(0, status.tentaclesLit)
        assertEquals(0, status.fullOctopuses)
    }

    @Test
    fun eachFinishedSessionLightsOneTentacle() {
        val status = coach.status(ago(listOf(3.hours, 2.days, 4.days)), now)
        assertEquals(3, status.tentaclesLit)
        assertEquals(PoulpyDrawing.MOOD_HAPPY, status.drawing)
    }

    @Test
    fun tentaclesNeverGoDarkOverALongBreak() {
        val status = coach.status(ago(listOf(60.days, 61.days, 90.days)), now)
        assertEquals(3, status.tentaclesLit)
        assertEquals(PoulpyDrawing.SLEEPING, status.drawing)
        assertEquals(true, status.blueSide)
    }

    @Test
    fun theEighthSessionMakesAChampionAndTheRingStaysFull() {
        val eight = ago(listOf(1.hours) + (1..7).map { (it * 3).days })
        val today = coach.status(eight, now)
        assertEquals(PoulpyDrawing.CHAMPION, today.drawing)
        assertEquals(PoulpyStatus.Mood.PROUD, today.mood)
        assertEquals(8, today.tentaclesLit)
        assertEquals(1, today.fullOctopuses)

        val later = coach.status(eight, now + 3.days)
        assertEquals(8, later.tentaclesLit)
        assertEquals(PoulpyDrawing.MOOD_HAPPY, later.drawing)
    }

    @Test
    fun theNinthSessionStartsANewOctopus() {
        val nine = ago(listOf(1.hours) + (1..8).map { (it * 3).days })
        val status = coach.status(nine, now)
        assertEquals(1, status.tentaclesLit)
        assertEquals(1, status.fullOctopuses)
    }

    @Test
    fun theDayAfterASessionPoulpyRecovers() {
        assertEquals(PoulpyDrawing.MOOD_RECOVERING, coach.status(ago(listOf(1.days)), now).drawing)
    }

    @Test
    fun comingBackAfterABreakGetsAShyWelcome() {
        assertEquals(PoulpyDrawing.SHY, coach.status(ago(listOf(2.hours, 10.days)), now).drawing)
    }

    @Test
    fun theFrenchCaptionAgreesWithOneTentacle() {
        val french = PoulpyCoach("fr", TimeZone.UTC)
        assertEquals("1 tentacule allumé sur 8", french.status(ago(listOf(3.hours)), now).tentaclesCaption)
        val ninth = french.status(ago((1..9).map { it.days }), now)
        assertEquals("1 tentacule allumé sur 8 · 1 poulpe complet", ninth.tentaclesCaption)
    }

    @Test
    fun painAlwaysWinsWithThePatchedUpPoseOnTheBlueSide() {
        val status = coach.status(ago(listOf(30.days)), now, painActive = true)
        assertEquals(PoulpyDrawing.PATCHED_UP, status.drawing)
        assertEquals(PoulpyStatus.Mood.RECOVERING, status.mood)
        assertEquals(true, status.blueSide)
        assertEquals("Taking it easy", status.title)
    }

    @Test
    fun painWinsOverAFullOctopusToo() {
        val sessions = ago((0 until 8).map { it.hours })
        assertEquals(PoulpyDrawing.PATCHED_UP, coach.status(sessions, now, painActive = true).drawing)
        assertEquals(PoulpyDrawing.CHAMPION, coach.status(sessions, now).drawing)
    }
}
