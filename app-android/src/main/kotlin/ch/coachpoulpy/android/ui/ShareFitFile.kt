package ch.coachpoulpy.android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import ch.coachpoulpy.shared.SessionFitFile
import java.io.File

/**
 * The type a .FIT file goes out with. Android knows no type for `.fit`, and the apps that open one (Garmin Connect
 * among them) listen for plain binary files, so the generic type reaches them where `application/vnd.ant.fit` does not.
 */
const val FIT_MIME_TYPE = "application/octet-stream"

/** Writes the session's .FIT [file] under the cache folder and returns it through [FileProvider] (see `res/xml/file_paths.xml`). */
private fun fitUri(context: Context, file: SessionFitFile): Uri {
    val folder = File(context.cacheDir, "shared").apply { mkdirs() }
    val written = File(folder, file.fileName).apply { writeBytes(file.bytes) }
    return FileProvider.getUriForFile(context, "${context.packageName}.files", written)
}

/** Hands the session's .FIT [file] to the system share sheet: messaging, mail, drives. */
fun shareFitFile(context: Context, file: SessionFitFile, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND)
        .setType(FIT_MIME_TYPE)
        .putExtra(Intent.EXTRA_STREAM, fitUri(context, file))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, chooserTitle))
}

/** Offers the session's .FIT [file] to the apps that open files, such as Garmin Connect. */
fun openFitFile(context: Context, file: SessionFitFile, chooserTitle: String) {
    val view = Intent(Intent.ACTION_VIEW)
        .setDataAndType(fitUri(context, file), FIT_MIME_TYPE)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(view, chooserTitle))
}

/** Writes the session's .FIT [file] to [target], the place picked in the system's save dialog. */
fun saveFitFile(context: Context, file: SessionFitFile, target: Uri) {
    context.contentResolver.openOutputStream(target)?.use { it.write(file.bytes) }
}
