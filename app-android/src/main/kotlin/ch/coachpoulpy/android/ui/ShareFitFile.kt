package ch.coachpoulpy.android.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import ch.coachpoulpy.shared.SessionFitFile
import java.io.File

/**
 * Writes the session's .FIT [file] under the cache folder and hands it to the system share sheet through
 * [FileProvider] (see `res/xml/file_paths.xml`), to open in Garmin Connect or keep for an upload to Strava.
 */
fun shareFitFile(context: Context, file: SessionFitFile, chooserTitle: String) {
    val folder = File(context.cacheDir, "shared").apply { mkdirs() }
    val written = File(folder, file.fileName).apply { writeBytes(file.bytes) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", written)
    val send = Intent(Intent.ACTION_SEND)
        .setType("application/vnd.ant.fit")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
