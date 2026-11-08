package ch.coachpoulpy.android

import android.content.Context
import android.os.Build
import android.os.Looper
import ch.coachpoulpy.shared.CrashReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.OffsetDateTime

/** A crash report waiting for the person to send it or not; [closed] is true when the app died and was reopened. */
data class CrashNotice(val report: String, val closed: Boolean)

/**
 * Keeps the last crash on the phone, so the app can offer to send it. Nothing leaves the phone unless
 * the person shares it.
 *
 * An error on the main thread kills the app, as Android does; the report is offered the next time it
 * opens. An error on any other thread (an uncaught coroutine exception, say) leaves the app running,
 * so it is offered right away, in [notice].
 */
object CrashCatcher {
    private const val FILE = "last-crash.txt"

    private val current = MutableStateFlow<CrashNotice?>(null)

    /** The report to offer now, if any. */
    val notice: StateFlow<CrashNotice?> = current

    fun install(context: Context) {
        val file = File(context.filesDir, FILE)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                file.writeText(
                    CrashReport.write(
                        error,
                        thread = thread.name,
                        appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})",
                        system = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        device = "${Build.MANUFACTURER} ${Build.MODEL}",
                        time = OffsetDateTime.now().toString(),
                    ),
                )
            } catch (_: Throwable) {
                // Saving the report must never hide the crash itself.
            }
            if (thread == Looper.getMainLooper().thread) {
                previous?.uncaughtException(thread, error)
            } else {
                // Android would kill the app here; the app is still usable, so tell the person instead.
                (try { file.readText() } catch (_: Throwable) { null })?.let { current.value = CrashNotice(it, closed = false) }
            }
        }
    }

    /** On a fresh start: offers the report of a crash that closed the app, unless one is already showing. */
    fun offerLastCrash(context: Context) {
        if (current.value != null) return
        val report = File(context.filesDir, FILE).takeIf { it.exists() }?.let { runCatching { it.readText() }.getOrNull() }
        if (report != null) current.value = CrashNotice(report, closed = true)
    }

    /** Once shown, the report is gone, whatever the person picked. */
    fun forget(context: Context) {
        File(context.filesDir, FILE).delete()
        current.value = null
    }
}
