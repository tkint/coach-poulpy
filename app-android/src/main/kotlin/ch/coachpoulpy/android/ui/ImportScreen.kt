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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph

/**
 * A workout file opened from another app: what is in it with Add to my library and Cancel, or why it can't be
 * added, crimson with an icon. [onAdded] gets the new workout's id; [onClose] leaves with nothing saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(graph: AppGraph, language: String, fileText: String, onAdded: (String) -> Unit, onClose: () -> Unit) {
    val model = remember(graph, language, fileText) { graph.importWorkout(fileText, language) }
    val state = model.state
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background, scrolledContainerColor = colors.background),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.ready) {
                    Button(
                        onClick = { model.add()?.let(onAdded) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                    ) { Text(stringResource(R.string.import_add), fontWeight = FontWeight.ExtraBold) }
                    TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.action_close), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.ready) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.import_someoneSent), style = MaterialTheme.typography.labelLarge, color = colors.primary)
                    Text(stringResource(R.string.import_title), style = MaterialTheme.typography.headlineMedium)
                }
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.surfaceVariant).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(state.name, style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            state.formatLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.clip(RoundedCornerShape(PoulpyRadius.pill)).background(colors.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                        Text("${state.partsLabel} · ${state.stepsLabel}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Text(stringResource(R.string.import_note), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            } else {
                Text(stringResource(R.string.import_errorTitle), style = MaterialTheme.typography.headlineMedium)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.surfaceVariant).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Crimson with an icon, never ember.
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(24.dp))
                    Text(state.errorMessage, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = colors.error)
                }
                Text(stringResource(R.string.import_nothingAdded), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}
