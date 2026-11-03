package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

/** A workout that mixes walks and runs with other exercises runs from Live (#82). */
class LiveMixedWorkoutTest {
    private class Models(val devices: DevicesModel, val live: LiveModel)

    private fun TestScope.models(central: FakeCentral, store: LocalStore): Models {
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(1_700_000_000_000 + testScheduler.currentTime)
        }
        val memory = MemoryStore()
        val settings = SettingsModel(memory, backgroundScope)
        val devices = DevicesModel(central, memory, backgroundScope, clock)
        return Models(devices, LiveModel(devices, settings.state, store, TimelineCompiler(), memory, backgroundScope, clock))
    }

    private fun TestScope.addTreadmill(models: Models) {
        models.devices.startScan()
        runCurrent()
        models.devices.add("AA:BB")
        models.devices.stopScan()
        runCurrent()
    }

    private fun exercise(id: String, slug: String, target: Target) =
        Step.ExerciseStep(StepId(id), ExerciseSlug(slug), Prescription(listOf(SetPrescription(target))))

    /** A warm-up walk, push-ups, then a walk to cool down. */
    private val mixed = workoutOf(
        "mixed", "Walk, push-ups, walk",
        listOf(
            exercise("warm-up", "walk", Target.Time(5.minutes)),
            exercise("push-ups", "push-up", Target.Reps(10)),
            exercise("cool-down", "walk", Target.Time(10.minutes)),
        ),
        WorkoutFormat.CIRCUIT,
    )

    /** Strength first, then a walk. */
    private val strengthFirst = workoutOf(
        "strength-first", "Push-ups, walk",
        listOf(exercise("push-ups", "push-up", Target.Reps(10)), exercise("cool-down", "walk", Target.Time(10.minutes))),
        WorkoutFormat.CIRCUIT,
    )

    private fun TestScope.saved(store: LocalStore, m: Models, vararg workouts: Workout) {
        workouts.forEach(store::saveWorkout)
        // Saved workouts are read on the IO dispatcher, outside the test clock: wait for them.
        var tries = 0
        while (workouts.any { w -> m.live.state.value.workouts.none { it.key == w.id.value } } && tries++ < 500) {
            Thread.sleep(10)
            runCurrent()
        }
    }

    private fun TestScope.moving(central: FakeCentral, meters: Int) {
        central.treadmill.send(speedCentiKmh = 500, meters = meters, bpm = 120)
        runCurrent()
    }

    @Test
    fun offersAnyWorkoutWithAWalkOrRunTheOnesMadeOfThemFirst() = runTest {
        val store = LocalStore(DriverFactory())
        val m = models(FakeCentral(), store)
        val strengthOnly = workoutOf("strength", "Strength", listOf(exercise("push-ups", "push-up", Target.Reps(10))), WorkoutFormat.CIRCUIT)
        store.saveWorkout(strengthOnly)
        saved(store, m, mixed, LiveModel.demoIntervals)
        runCurrent()
        assertEquals(listOf("demo-intervals", "mixed"), m.live.state.value.workouts.map { it.key })
    }

    @Test
    fun aMixedWorkoutShowsThePlayerWithTheTreadmillNumbersOnItsWalks() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, mixed)
        moving(central, 0)
        m.live.startRecording("mixed")
        val recording = assertNotNull(m.live.state.value.recording)
        assertEquals("walk", recording.player?.exerciseSlug)
        assertEquals(true, recording.onTreadmill)
        assertEquals(StepWait.NOT_WAITING, recording.wait)
        assertTrue(m.live.state.value.tiles.any { it.metric == LiveMetric.SPEED })
    }

    @Test
    fun aStrengthStepShowsOnlyTheHeartRateAndRecordsNoTreadmillSamples() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, mixed)
        moving(central, 0)
        m.live.startRecording("mixed")
        moving(central, 5)
        m.live.completeStep()
        runCurrent()

        val recording = assertNotNull(m.live.state.value.recording)
        assertEquals("push-up", recording.player?.exerciseSlug)
        assertEquals(false, recording.onTreadmill)
        assertEquals(listOf(LiveMetric.HEART_RATE), m.live.state.value.tiles.map { it.metric })
        val samplesBefore = store.events(openSession(store)).count { it is SessionEvent.Sample }
        moving(central, 8)
        moving(central, 11)
        assertEquals(samplesBefore, store.events(openSession(store)).count { it is SessionEvent.Sample })
    }

    @Test
    fun theBeltDoesNotPauseOrFinishAStrengthStep() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, mixed)
        moving(central, 0)
        m.live.startRecording("mixed")
        m.live.completeStep()
        runCurrent()

        central.treadmill.send(speedCentiKmh = 0, meters = 10, bpm = 120)
        runCurrent()
        assertEquals(false, m.live.state.value.recording?.paused)
        advanceTimeBy(11.minutes)
        assertNull(m.live.state.value.finishPrompt, "a still belt is no reason to ask during strength")
        assertEquals(false, m.live.state.value.recording?.paused)
    }

    @Test
    fun aWalkAfterStrengthWaitsForTheBeltAndStartsWithIt() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, strengthFirst)
        moving(central, 0)
        m.live.startRecording("strength-first") // opens with strength: no waiting for the belt
        assertNull(m.live.state.value.waitingFor)
        central.treadmill.send(speedCentiKmh = 0, meters = 0, bpm = 120)
        runCurrent()
        m.live.completeStep()
        runCurrent()

        var recording = assertNotNull(m.live.state.value.recording)
        assertEquals("walk", recording.player?.exerciseSlug)
        assertEquals(StepWait.FOR_BELT, recording.wait)
        assertEquals(true, recording.paused)

        advanceTimeBy(20.seconds)
        moving(central, 0)
        recording = assertNotNull(m.live.state.value.recording)
        assertEquals(StepWait.NOT_WAITING, recording.wait)
        assertEquals(false, recording.paused)
        assertEquals(0L, recording.stepElapsedSeconds, "the wait is not part of the step")
    }

    @Test
    fun aWaitingWalkStartsWithoutTheBeltAndIsNotAskedAboutAtOnce() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, strengthFirst)
        moving(central, 0)
        m.live.startRecording("strength-first")
        central.treadmill.status(0x02, 0x01) // Stop on the console
        runCurrent()
        m.live.completeStep()
        runCurrent()
        assertEquals(StepWait.FOR_BELT, m.live.state.value.recording?.wait)
        assertNull(m.live.state.value.finishPrompt, "waiting for the belt is not a Stop pressed on the console")

        m.live.startStep()
        val recording = assertNotNull(m.live.state.value.recording)
        assertEquals(StepWait.NOT_WAITING, recording.wait)
        assertEquals(false, recording.paused)
    }

    @Test
    fun aWalkWaitsForStartEvenWhenTheBeltAlreadyMoves() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, strengthFirst)
        moving(central, 0)
        m.live.startRecording("strength-first")
        m.live.completeStep()
        runCurrent()
        assertEquals(StepWait.FOR_TAP, m.live.state.value.recording?.wait)
        assertEquals(true, m.live.state.value.recording?.paused)
        m.live.startStep()
        assertEquals(StepWait.NOT_WAITING, m.live.state.value.recording?.wait)
    }

    @Test
    fun discardingARecordingLeavesNoTrace() = runTest {
        val store = LocalStore(DriverFactory())
        val m = models(FakeCentral(), store)
        saved(store, m, strengthFirst)
        m.live.startRecording("strength-first")
        m.live.discardCurrent()
        assertNull(m.live.state.value.recording)
        assertNull(store.unfinishedSession())
    }

    @Test
    fun withoutATreadmillAWalkWaitsForStart() = runTest {
        val store = LocalStore(DriverFactory())
        val m = models(FakeCentral(), store)
        saved(store, m, strengthFirst)
        m.live.startRecording("strength-first")
        m.live.completeStep()
        runCurrent()

        var recording = assertNotNull(m.live.state.value.recording)
        assertEquals(StepWait.FOR_START, recording.wait)
        assertEquals(true, recording.paused)
        advanceTimeBy(5.minutes)
        assertNull(m.live.state.value.finishPrompt)

        m.live.startStep()
        recording = assertNotNull(m.live.state.value.recording)
        assertEquals(StepWait.NOT_WAITING, recording.wait)
        assertEquals(false, recording.paused)
        advanceTimeBy(30.seconds)
        assertTrue((m.live.state.value.recording?.stepElapsedSeconds ?: 0L) in 29L..30L, "the step counts from Start, not from the wait")
    }

    @Test
    fun skippingAWaitingWalkMovesOnWithoutStayingPaused() = runTest {
        val store = LocalStore(DriverFactory())
        val m = models(FakeCentral(), store)
        saved(store, m, mixed)
        m.live.startRecording("mixed") // no treadmill: the first walk starts at once
        m.live.completeStep()
        m.live.completeStep() // push-ups done, the cool-down walk waits for Start
        runCurrent()
        assertEquals(StepWait.FOR_START, m.live.state.value.recording?.wait)

        m.live.skipPhase()
        runCurrent()
        assertNull(m.live.state.value.recording, "the walk was the last step")
        assertNotNull(m.live.state.value.summary)
    }

    @Test
    fun aWorkoutOfWalksAndRunsOnlyKeepsTheLiveLayout() = runTest {
        val central = FakeCentral()
        val store = LocalStore(DriverFactory())
        val m = models(central, store)
        addTreadmill(m)
        saved(store, m, LiveModel.demoIntervals)
        moving(central, 0)
        m.live.startRecording("demo-intervals")
        val recording = assertNotNull(m.live.state.value.recording)
        assertNull(recording.player)
        assertEquals(StepWait.NOT_WAITING, recording.wait)
    }

    private fun openSession(store: LocalStore): String = assertNotNull(store.unfinishedSession()).record.id.value
}
