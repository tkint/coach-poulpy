package ch.coachpoulpy.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.UnfinishedSessionUi

/** Asked once when the app opens on a session left open: Resume, Discard, or dismiss to decide later. */
@Composable
fun UnfinishedSessionDialog(session: UnfinishedSessionUi, onResume: () -> Unit, onDiscard: () -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (session.live) stringResource(R.string.unfinished_liveTitle)
                else stringResource(R.string.unfinished_title, session.workoutName.orEmpty()),
            )
        },
        text = { Text(stringResource(R.string.unfinished_body, clock(session.stoppedAtSeconds))) },
        confirmButton = { Button(onClick = onResume) { Text(stringResource(R.string.player_resume)) } },
        // Destructive, so crimson with an icon, never ember.
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = colors.error)
                Text(stringResource(R.string.unfinished_discard), color = colors.error)
            }
        },
    )
}

private fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
