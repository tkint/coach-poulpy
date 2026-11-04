package ch.coachpoulpy.android.ui

import android.content.Context
import android.net.Uri
import ch.coachpoulpy.shared.SessionFitFile

/**
 * The type a .FIT file is saved with. Android knows no type for `.fit`, so the generic binary type is used. The file is
 * only saved, never opened in an app: Garmin Connect's app reads it as a course to place on a map.
 */
const val FIT_MIME_TYPE = "application/octet-stream"

/** Writes the session's .FIT [file] to [target], the place picked in the system's save dialog. */
fun saveFitFile(context: Context, file: SessionFitFile, target: Uri) {
    context.contentResolver.openOutputStream(target)?.use { it.write(file.bytes) }
}
