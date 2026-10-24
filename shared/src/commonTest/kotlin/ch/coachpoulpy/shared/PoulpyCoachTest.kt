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
}
