package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class AchievementsTest {
    private class Memory : KeyValueStore {
        val values = mutableMapOf<String, String>()
        override fun getString(key: String) = values[key]
        override fun putString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
    }

    private fun row(kind: SessionKind, km: Double?, speed: Double?) = SessionRow(
        id = "s", title = "s", kind = kind, formatLabel = null, startedAtMillis = 0, activeSeconds = 1800,
        setsDone = null, setsSkipped = null, setsPlanned = null, distanceMeters = km?.times(1000), averageSpeedKmh = speed,
        averageHeartRateBpm = null, maxHeartRateBpm = null, energyKcal = null, estimatedSteps = null,
    )

    @Test
    fun aTreadmillSessionAtWalkingSpeedIsAWalk() {
        assertEquals(listOf(Badge.FIRST_WALK), Badges.earnedFrom(listOf(row(SessionKind.TREADMILL, 2.0, 5.0))))
        assertEquals(listOf(Badge.FIRST_WALK), Badges.earnedFrom(listOf(row(SessionKind.TREADMILL, 3.0, 6.5))))
    }

    @Test
    fun runsWorkoutsAndSessionsWithoutDistanceAreNotWalks() {
        assertEquals(emptyList(), Badges.earnedFrom(listOf(row(SessionKind.TREADMILL, 5.0, 9.0))))
        assertEquals(emptyList(), Badges.earnedFrom(listOf(row(SessionKind.WORKOUT, 2.0, 5.0))))
        assertEquals(emptyList(), Badges.earnedFrom(listOf(row(SessionKind.TREADMILL, null, 5.0))))
        assertEquals(emptyList(), Badges.earnedFrom(listOf(row(SessionKind.TREADMILL, 0.0, 5.0))))
        assertEquals(emptyList(), Badges.earnedFrom(listOf(row(SessionKind.TREADMILL, 2.0, null))))
    }

    @Test
    fun aBadgeIsNeverTakenBack() {
        val ledger = AchievementLedger(Memory())
        assertEquals(listOf(Badge.FIRST_WALK), ledger.earned(listOf(Badge.FIRST_WALK)))
        assertEquals(listOf(Badge.FIRST_WALK), ledger.earned(emptyList()))
    }

    @Test
    fun latestBadgesComeNewestFirstAndLockedOnesAreTheRest() {
        val ui = AchievementsUi(listOf(Badge.FIRST_RECORD, Badge.FIRST_WALK))
        assertEquals(listOf(Badge.FIRST_WALK), ui.latest(1))
        assertEquals(listOf(Badge.GOAL_MET), ui.locked)
        assertEquals(listOf(Badge.FIRST_RECORD, Badge.FIRST_WALK), ui.earnedInOrder)
    }
}
