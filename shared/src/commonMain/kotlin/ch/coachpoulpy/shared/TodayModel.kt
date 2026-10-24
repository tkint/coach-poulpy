package ch.coachpoulpy.shared

import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.data.local.LocalStore
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WorkoutRow(val id: String, val name: String, val details: String)

data class TodayState(val workouts: List<WorkoutRow> = emptyList())

/** The home screen: the workouts saved on this phone. */
class TodayModel internal constructor(
    store: LocalStore,
    private val compiler: TimelineCompiler,
    languageCode: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val labels = Labels(languageCode)

    val state: StateFlow<TodayState> = store.workouts()
        .map { workouts -> TodayState(workouts.map(::row)) }
        .stateIn(scope, SharingStarted.Eagerly, TodayState())

    fun watch(onChange: (TodayState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun close() = scope.cancel()

    private fun row(workout: Workout): WorkoutRow {
        val minutes = (estimatedLength(workout).inWholeSeconds + 59) / 60
        return WorkoutRow(
            id = workout.id.value,
            name = workout.name,
            details = "${labels.workoutType(workout.type)} · ~$minutes min",
        )
    }

    /** Timed phases count for their length; a set of reps is guessed at 40 seconds. */
    private fun estimatedLength(workout: Workout): Duration =
        runCatching { compiler.compile(workout).phases }.getOrDefault(emptyList())
            .fold(Duration.ZERO) { total, phase -> total + (phase.duration ?: MANUAL_SET) }

    private companion object {
        val MANUAL_SET = 40.seconds
    }
}
