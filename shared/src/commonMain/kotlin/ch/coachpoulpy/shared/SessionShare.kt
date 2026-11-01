package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.SessionEvent
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.data.local.LocalStore

/**
 * The picture to share for a session of the history: the finished screen it had, rebuilt from what the phone kept, so
 * it looks like the picture shared straight after the session. Poulpy says what he said when the session ended.
 */
internal class SessionShare(private val store: LocalStore, private val compiler: TimelineCompiler, private val languageCode: String) {
    fun card(sessionId: String, units: UnitSystem): ShareCard? {
        val stored = store.finishedSession(sessionId) ?: return null
        val events = store.events(sessionId)
        val workout = stored.execution.workout?.let { store.workout(it.workoutId, it.version) }
        val timeline = sessionTimeline(stored, workout, compiler)
        val totals = stored.totals ?: sessionTotals(events, timeline)
        val title = sessionRow(stored, totals, Labels(languageCode)).title
        val endedAt = stored.execution.endedAt ?: events.filterIsInstance<SessionEvent.Finished>().lastOrNull()?.at ?: stored.execution.startedAt
        // Only the sessions up to this one count for the tentacle it lit.
        val poulpy = PoulpyCoach(languageCode).status(store.finishedSessionEndsNow().filter { it <= endedAt }, endedAt)
        return ShareCard.of(FinishedScreens.of(title, workout, timeline, events, poulpy, units, languageCode), languageCode)
    }
}
