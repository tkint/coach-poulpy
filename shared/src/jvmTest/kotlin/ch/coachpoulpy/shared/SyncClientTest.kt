package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.SessionId
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.SessionSource
import ch.coachpoulpy.core.model.SyncChange
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutRef
import ch.coachpoulpy.core.model.WorkoutVersionChange
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.applyPulled
import ch.coachpoulpy.data.local.syncCursor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class SyncClientTest {
    private val server = FakeSyncServer()
    private val legs = Workout(WorkoutId("w1"), 1, "Legs", WorkoutFormat.SETS, emptyList())

    private inner class Phone(name: String) {
        val store = LocalStore(DriverFactory())
        val memory = MemoryStore()
        val settings = SyncedSettings(memory, store)
        var refused = 0
        val sync = SyncClient(store, memory, server, name, { "en" }, CoroutineScope(Dispatchers.Default), onSignInRefused = { refused++ })

        fun signIn() = store.startRecordingChanges(SyncedSettings.values(memory))
        fun names() = store.savedWorkouts().map { it.workout.name }.sorted()
    }

    @Test
    fun twoPhonesEditingTheSameWorkoutKeepBothEdits() = runTest {
        val ana = Phone("Ana's iPhone")
        val ben = Phone("Ben's Pixel")
        ana.signIn()
        ana.store.saveWorkout(legs)
        assertEquals(SyncResult.SYNCED, ana.sync.run())
        ben.signIn()
        ben.sync.run()
        assertEquals(listOf("Legs"), ben.names())

        ana.store.saveWorkout(legs.copy(version = 2, name = "Legs, heavier"))
        ben.store.saveWorkout(legs.copy(version = 2, name = "Legs, longer"))
        ben.store.startSession(SessionRecord(SessionId("s1"), SessionSource.GUIDED, startedAt = kotlin.time.Instant.fromEpochSeconds(1), workout = WorkoutRef(legs.id, 2)))
        val plan = ben.store.planWorkout(legs.id, LocalDate(2026, 11, 9))
        ana.sync.run()

        assertEquals(SyncResult.SYNCED, ben.sync.run())
        ana.sync.run()

        val both = listOf("Legs, heavier", "Legs, longer (Ben's Pixel)")
        assertEquals(both, ana.names())
        assertEquals(both, ben.names())
        val copy = ben.store.savedWorkouts().single { it.workout.id != legs.id }.workout
        assertEquals(1, copy.version)
        assertEquals(WorkoutRef(copy.id, 1), ben.store.unfinishedRecord()?.workout)
        assertEquals(copy.id, ben.store.plannedWorkouts().first().single { it.id == plan }.workoutId)
        assertEquals(copy.id, ana.store.plannedWorkouts().first().single { it.id == plan }.workoutId)
        assertEquals(0, ben.store.outbox(10).size)
    }

    @Test
    fun aPushRefusedAsStaleForksAndGoesAgain() = runTest {
        val ana = Phone("Ana's iPhone")
        val ben = Phone("Ben's Pixel")
        ana.signIn()
        ana.store.saveWorkout(legs)
        ana.sync.run()
        ben.signIn()
        ben.sync.run()

        ben.store.saveWorkout(legs.copy(version = 2, name = "Legs, longer"))
        // Ana pushes her version 2 between Ben's pull and his push.
        server.beforePush = {
            server.beforePush = {}
            ana.store.saveWorkout(legs.copy(version = 2, name = "Legs, heavier"))
            ana.sync.run()
        }

        assertEquals(SyncResult.SYNCED, ben.sync.run())
        assertEquals(listOf("Legs, heavier", "Legs, longer (Ben's Pixel)"), ben.names())
        assertEquals(1, server.staleAnswers)
    }

    @Test
    fun aPushWhoseAnswerIsLostIsSentAgainWithoutDuplicates() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.signIn()
        ana.store.saveWorkout(legs)
        server.loseNextAnswer = true

        assertEquals(SyncResult.FAILED, ana.sync.run())
        assertEquals(1, ana.store.outbox(10).size)
        assertEquals(SyncResult.SYNCED, ana.sync.run())

        assertEquals(1, server.log.count { it is WorkoutVersionChange })
        assertEquals(0, ana.store.outbox(10).size)
    }

    @Test
    fun theProfileFollowsTheAccountButTheThemeStays() = runTest {
        val ana = Phone("Ana's iPhone")
        val ben = Phone("Ben's Pixel")
        ana.signIn()
        ana.settings.putString("profile.name", "Ana")
        ana.settings.putString("settings.theme", "DARK")
        ana.sync.run()
        ben.signIn()

        ben.sync.run()

        assertEquals("Ana", ben.memory.getString("profile.name"))
        assertNull(ben.memory.getString("settings.theme"))
        // What a pull brought isn't pushed back.
        assertEquals(0, ben.store.outbox(10).size)
    }

    @Test
    fun aChangeStillToPushKeepsThePhonesValue() = runTest {
        val ana = Phone("Ana's iPhone")
        val ben = Phone("Ben's Pixel")
        val day = LocalDate(2026, 11, 1)
        ana.signIn()
        ben.signIn()
        ana.store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 70.0))
        ana.sync.run()
        ben.store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, day, 71.0))

        // Ben's pull brings Ana's weight while his own still waits to be pushed.
        val page = server.pull(null, 500)
        ben.store.applyPulled(page, { it.name }) {}

        assertEquals(listOf(71.0), ben.store.bodyMeasurements(BodyMetric.WEIGHT).first().map { it.value })
    }

    @Test
    fun applyingAPageTwiceChangesNothing() = runTest {
        val ana = Phone("Ana's iPhone")
        val ben = Phone("Ben's Pixel")
        ana.signIn()
        ana.store.saveWorkout(legs)
        ana.store.planWorkout(legs.id, LocalDate(2026, 11, 9))
        ana.store.startSession(SessionRecord(SessionId("s1"), SessionSource.GUIDED, startedAt = kotlin.time.Instant.fromEpochSeconds(1)))
        ana.sync.run()

        val page = server.pull(null, 500)
        repeat(2) { ben.store.applyPulled(page, { it.name }) {} }

        assertEquals(listOf("Legs"), ben.names())
        assertEquals(1, ben.store.plannedWorkouts().first().size)
        assertEquals(SessionId("s1"), ben.store.unfinishedRecord()?.id)
        assertEquals(page.cursor, ben.store.syncCursor())
    }

    @Test
    fun signingOutWaitsForTheOutboxThenClearsWhatFollowsTheAccount() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.signIn()
        ana.store.saveWorkout(legs)
        ana.settings.putString("profile.name", "Ana")
        ana.memory.putString("settings.theme", "DARK")

        assertEquals(SignOutResult.CHANGES_WAITING, ana.sync.signOut())
        assertEquals(0, server.revoked)
        ana.sync.run()

        assertEquals(SignOutResult.SIGNED_OUT, ana.sync.signOut())
        assertEquals(1, server.revoked)
        assertEquals(emptyList(), ana.names())
        assertNull(ana.memory.getString("profile.name"))
        assertEquals("DARK", ana.memory.getString("settings.theme"))
        assertFalse(ana.store.recordingChanges())
        assertNull(ana.store.syncCursor())
        assertEquals(SyncResult.NOT_SIGNED_IN, ana.sync.run())
    }

    @Test
    fun signingOutAnywayDropsTheChangesWaiting() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.signIn()
        ana.store.saveWorkout(legs)

        assertEquals(SignOutResult.SIGNED_OUT, ana.sync.signOut(evenWithChanges = true))
        assertEquals(0, ana.store.outbox(10).size)
        assertEquals(emptyList(), ana.names())
    }

    @Test
    fun aRefusedSignInMakesAGuestThatKeepsItsData() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.signIn()
        ana.store.saveWorkout(legs)
        ana.settings.putString("profile.name", "Ana")
        server.refuse = true

        assertEquals(SyncResult.SIGNED_OUT, ana.sync.run())

        assertEquals(1, ana.refused)
        assertEquals(listOf("Legs"), ana.names())
        assertEquals("Ana", ana.memory.getString("profile.name"))
        assertFalse(ana.store.recordingChanges())
        assertEquals(0, ana.store.outbox(10).size)
        assertNull(ana.store.syncCursor())
    }

    @Test
    fun aGuestDoesNotSync() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.store.saveWorkout(legs)

        assertEquals(SyncResult.NOT_SIGNED_IN, ana.sync.run())
        assertEquals(0, server.pulls)
    }

    @Test
    fun aRunAskedForDuringAnotherRunsOnceItEnds() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.signIn()
        ana.store.saveWorkout(legs)
        server.beforePush = {
            server.beforePush = {}
            ana.store.saveWorkout(legs.copy(version = 2))
            assertEquals(SyncResult.ALREADY_RUNNING, ana.sync.run())
        }

        assertEquals(SyncResult.SYNCED, ana.sync.run())

        assertEquals(listOf(1, 2), server.log.filterIsInstance<WorkoutVersionChange>().map { it.workout.version })
        assertEquals(0, ana.store.outbox(10).size)
    }

    @Test
    fun aLargeOutboxGoesInSeveralPushes() = runTest {
        val ana = Phone("Ana's iPhone")
        ana.signIn()
        repeat(1200) { ana.store.logBodyMeasurement(BodyMeasurement(BodyMetric.WEIGHT, LocalDate(2020, 1, 1).plusDays(it), 70.0)) }

        assertEquals(SyncResult.SYNCED, ana.sync.run())

        assertEquals(listOf(500, 500, 200), server.pushSizes)
        assertTrue(ana.store.outbox(1).isEmpty())
    }

    private fun LocalDate.plusDays(days: Int) = LocalDate.fromEpochDays(toEpochDays() + days)
}

/** The server's merge rules that matter here: versions follow their base, every change is stored once. */
private class FakeSyncServer : SyncServer {
    val log = mutableListOf<SyncChange>()
    var refuse = false
    var loseNextAnswer = false
    var beforePush: suspend () -> Unit = {}
    var revoked = 0
    var pulls = 0
    var staleAnswers = 0
    val pushSizes = mutableListOf<Int>()

    override suspend fun pull(cursor: String?, limit: Int): SyncPage {
        if (refuse) throw SignInRefused()
        pulls++
        val after = cursor?.toInt() ?: 0
        val page = log.drop(after).take(limit)
        return SyncPage(page, (after + page.size).toString(), after + page.size < log.size)
    }

    override suspend fun push(push: SyncPush): PushAnswer {
        if (refuse) throw SignInRefused()
        beforePush()
        val latest = log.filterIsInstance<WorkoutVersionChange>().groupBy { it.workout.id }
            .mapValues { (_, versions) -> versions.maxBy { it.workout.version }.workout }.toMutableMap()
        val stale = mutableListOf<WorkoutId>()
        push.changes.filterIsInstance<WorkoutVersionChange>().forEach { change ->
            val known = log.filterIsInstance<WorkoutVersionChange>()
                .firstOrNull { it.workout.id == change.workout.id && it.workout.version == change.workout.version }
            when {
                known != null && known.workout == change.workout -> {}
                known == null && latest[change.workout.id]?.version == change.baseVersion -> latest[change.workout.id] = change.workout
                else -> stale += change.workout.id
            }
        }
        if (stale.isNotEmpty()) {
            staleAnswers++
            return PushAnswer.StaleBase(stale)
        }
        pushSizes += push.changes.size
        push.changes.forEach { if (it !in log) log += it }
        if (loseNextAnswer) {
            loseNextAnswer = false
            throw IllegalStateException("The answer was lost")
        }
        return PushAnswer.Accepted
    }

    override suspend fun revoke() {
        revoked++
    }
}
