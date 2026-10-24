package ch.coachpoulpy.data.local

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.model.Execution
import ch.coachpoulpy.core.model.ExecutionId
import ch.coachpoulpy.core.model.ExecutionSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class LocalStoreTest {
    @Test
    fun sessionEventsComeBackInOrder() {
        val store = LocalStore(DriverFactory())
        val t0 = Instant.fromEpochSeconds(1_700_000_000)
        store.startExecution(Execution(ExecutionId("e1"), ExecutionSource.GUIDED, startedAt = t0))

        val written = listOf(SessionEvent.Started(t0), SessionEvent.PhaseCompleted(t0, 0), SessionEvent.Paused(t0))
        written.forEach { store.appendEvent("e1", it) }

        assertEquals(written, store.events("e1"))
    }
}
