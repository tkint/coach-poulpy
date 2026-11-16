package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** One way out in a [ShareSheet]: an [icon], what it is, a [note] on what it does, and what tapping it does. */
class ShareChoice(val icon: ImageVector, @StringRes val title: Int, @StringRes val note: Int, val onClick: () -> Unit)

/** The ways a session or a workout goes out, in a sheet: a title, why, and one row per choice. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(@StringRes title: Int, @StringRes why: Int, choices: List<ShareChoice>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(start = PoulpySpace.lg, end = PoulpySpace.lg, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(stringResource(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(stringResource(why), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.clip(RoundedCornerShape(PoulpyRadius.md)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                choices.forEachIndexed { index, choice ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    ShareRow(choice)
                }
            }
        }
    }
}

@Composable
private fun ShareRow(choice: ShareChoice) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = PoulpySize.row).clickable(onClick = choice.onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(choice.icon, contentDescription = null)
        Column {
            Text(stringResource(choice.title), fontWeight = FontWeight.Bold)
            Text(stringResource(choice.note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
