package ch.coachpoulpy.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun interface Cancellable {
    fun cancel()
}

/** Callback form of a [StateFlow] for Swift, which cannot collect flows. */
internal fun <T> StateFlow<T>.watchIn(scope: CoroutineScope, onChange: (T) -> Unit): Cancellable {
    val job = scope.launch { collect { onChange(it) } }
    return Cancellable { job.cancel() }
}
