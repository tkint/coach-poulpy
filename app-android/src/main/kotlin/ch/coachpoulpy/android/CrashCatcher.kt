package ch.coachpoulpy.android

import android.content.Context
import android.os.Build
import ch.coachpoulpy.shared.CrashReport
import java.io.File
import java.time.OffsetDateTime

/**
 * Keeps the last crash on the phone, so the app can offer to send it the next time it opens. Nothing
 * leaves the phone unless the person shares it. Android's own handler still runs afterwards, so the
 * system's "keeps stopping" behaviour is unchanged.
 */
object CrashCatcher {
    private const val FILE = "last-crash.txt"

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
            previous?.uncaughtException(thread, error)
        }
    }

    /** The report of the crash the app has not shown yet, if any. */
    fun lastCrash(context: Context): String? =
        File(context.filesDir, FILE).takeIf { it.exists() }?.let { runCatching { it.readText() }.getOrNull() }

    /** Once shown, the report is gone, whatever the person picked. */
    fun forget(context: Context) {
        File(context.filesDir, FILE).delete()
    }
}
