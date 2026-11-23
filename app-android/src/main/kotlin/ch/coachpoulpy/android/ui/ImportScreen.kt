package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.WorkoutImportState

/**
 * A workout file that can't be opened in the builder: why, crimson with an icon, and Close. A good file never shows
 * here, it opens in the builder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(state: WorkoutImportState, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        bottomBar = {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = PoulpySpace.lg, vertical = 12.dp)) {
                PoulpyButton(onClick = onClose, kind = PoulpyButtonKind.Secondary, size = PoulpyButtonSize.Lg, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_close))
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            AppBar(title = null, leading = { CloseButton(onClose) })
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = PoulpySpace.lg, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.import_errorTitle), style = MaterialTheme.typography.headlineMedium)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.surfaceVariant).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Crimson with an icon, never ember.
                    Icon(Lucide.CircleExclamationPoint, contentDescription = null, tint = colors.error, modifier = Modifier.size(24.dp))
                    Text(state.errorMessage, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = colors.error)
                }
                Text(stringResource(R.string.import_nothingAdded), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}
