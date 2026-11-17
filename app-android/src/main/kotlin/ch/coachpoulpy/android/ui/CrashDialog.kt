package ch.coachpoulpy.android.ui

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ch.coachpoulpy.android.R

/**
 * Offers to send the [report] as text, through any app the person picks. [closed] is true when the app
 * closed on the error last time, false when it is still running after one.
 */
@Composable
fun CrashDialog(report: String, closed: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.crash_send)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Lucide.CircleExclamationPoint, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(stringResource(if (closed) R.string.crash_title else R.string.crash_titleRunning), textAlign = TextAlign.Center) },
        text = { Text(stringResource(if (closed) R.string.crash_body else R.string.crash_bodyRunning), textAlign = TextAlign.Center) },
        confirmButton = {
            PoulpyButton(
                onClick = {
                val send = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_SUBJECT, "Coach Poulpy crash")
                    .putExtra(Intent.EXTRA_TEXT, report)
                context.startActivity(Intent.createChooser(send, chooserTitle))
                onDismiss()
            },
) { Text(stringResource(R.string.crash_send)) }
        },
        dismissButton = {
            PoulpyButton(onClick = onDismiss, kind = PoulpyButtonKind.Secondary) { Text(stringResource(R.string.crash_notNow)) }
        },
    )
}
