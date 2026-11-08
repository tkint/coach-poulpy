package ch.coachpoulpy.shared

/**
 * The text a crash leaves on the phone, for the person to send to whoever builds the app: what ran,
 * on what, and the error with its causes. It holds no workouts and no account details, only what the
 * error itself says.
 */
object CrashReport {
    /** Past this the text is cut, so it still fits in a share sheet. */
    const val MAX_LENGTH = 50_000

    fun write(error: Throwable, thread: String, appVersion: String, system: String, device: String, time: String): String {
        val text = buildString {
            appendLine("Coach Poulpy $appVersion")
            appendLine("$system, $device")
            appendLine(time)
            appendLine("Thread: $thread")
            appendLine()
            append(error.stackTraceToString())
        }
        return if (text.length <= MAX_LENGTH) text else text.take(MAX_LENGTH) + "\n…"
    }
}
