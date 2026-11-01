package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class ShareCardTest {
    private val status = PoulpyCoach("en").status(emptyList(), Instant.fromEpochMilliseconds(1_000L))
    private val screen = FinishedScreen(
        heading = "Well done!",
        quote = "“Worth it. Probably.”",
        drawing = PoulpyDrawing.EXHAUSTED,
        workoutName = "Lower body strength",
        startedAtMillis = 1_000L,
        summary = "17 of 18 sets done in 38:24 · 1 skipped",
        stats = listOf(FinishedStat("Time", "38:24"), FinishedStat("Sets", "17"), FinishedStat("Lifted", "2.1 t")),
        poulpy = status,
    )

    @Test
    fun theCardIsTheFinishedScreenAsAPicture() {
        val card = ShareCard.of(screen, "en")
        assertEquals("Well done!", card.heading)
        assertEquals("Lower body strength", card.title)
        assertEquals(1_000L, card.startedAtMillis)
        assertEquals("“Worth it. Probably.”", card.quote)
        assertEquals(screen.stats, card.lines)
        assertEquals(status, card.poulpy)
        assertEquals(PoulpyDrawing.EXHAUSTED, card.drawing)
        assertEquals("Coach Poulpy", card.appName)
    }

    @Test
    fun theCardSpeaksFrench() {
        assertEquals("Coach Poulpy", ShareCard.of(screen, "fr").appName)
        assertEquals("1:05:09", ShareCard.clock(3600 + 5 * 60 + 9))
        assertEquals("5,2\u00a0km", ShareCard.measure(Units.distance(5200.0, UnitSystem.METRIC, decimals = 1), "fr"))
    }

    @Test
    fun theEighthTentacleMakesPoulpyAChampionWithHisOwnWords() {
        val now = Instant.fromEpochMilliseconds(10_000_000L)
        val eight = PoulpyCoach("en").status(List(8) { Instant.fromEpochMilliseconds(1_000L * it) }, now)
        val champion = FinishedScreens.of("Lower body strength", null, null, emptyList(), eight, UnitSystem.METRIC, "en")
        val ordinary = FinishedScreens.of("Lower body strength", null, null, emptyList(), status, UnitSystem.METRIC, "en")
        assertEquals(PoulpyDrawing.CHAMPION, champion.drawing)
        assertEquals(PoulpyDrawing.EXHAUSTED, ordinary.drawing)
        assertNotEquals(ordinary.heading, champion.heading)
        assertNotEquals(ordinary.quote, champion.quote)
    }
}
