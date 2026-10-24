package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.LocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayState(val workoutNames: List<String> = emptyList())

/** The home screen: for now, the user's saved workouts. */
class TodayModel(store: LocalStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val state: StateFlow<TodayState> = store.workouts()
        .map { workouts -> TodayState(workouts.map { it.name }) }
        .stateIn(scope, SharingStarted.Eagerly, TodayState())

    /** Callback form of [state] for Swift. Returns a handle to stop watching. */
    fun watch(onChange: (TodayState) -> Unit): Cancellable {
        val job = scope.launch { state.collect { onChange(it) } }
        return Cancellable { job.cancel() }
    }

    fun close() = scope.cancel()
}

fun interface Cancellable {
    fun cancel()
}
