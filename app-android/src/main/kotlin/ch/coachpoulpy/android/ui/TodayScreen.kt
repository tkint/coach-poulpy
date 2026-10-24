package ch.coachpoulpy.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.WorkoutRow

@Composable
fun TodayScreen(
    graph: AppGraph,
    language: String,
    onNew: () -> Unit,
    onEdit: (String) -> Unit,
    onStart: (String) -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = remember(graph, language) { graph.today(language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    // Read each time the tab is shown, so it is current after coming back from a session.
    val unfinished = remember(graph) { graph.unfinishedSessionName() }
    var deleting by remember { mutableStateOf<WorkoutRow?>(null) }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            state.poulpy?.let { poulpy ->
                item(key = "poulpy") { PoulpyHeader(poulpy, Modifier.fillMaxWidth()) }
            }
            if (unfinished != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.today_resume, unfinished), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.today_resume_body), style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = onResume) { Text(stringResource(R.string.player_resume)) }
                        }
                    }
                }
            }
            if (state.workouts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(R.string.today_empty_title), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.today_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            items(state.workouts, key = { it.id }) { workout ->
                WorkoutCard(workout, onStart = { onStart(workout.id) }, onEdit = { onEdit(workout.id) }, onDelete = { deleting = workout })
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNew,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.today_new_workout)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    deleting?.let { workout ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_body)) },
            confirmButton = {
                TextButton(onClick = { graph.deleteWorkout(workout.id); deleting = null }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun WorkoutCard(workout: WorkoutRow, onStart: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card {
        ListItem(
            headlineContent = { Text(workout.name, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(workout.details) },
            trailingContent = {
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.action_edit)) }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text(stringResource(R.string.action_delete)) }, onClick = { menu = false; onDelete() })
                    }
                }
            },
        )
        FilledTonalButton(onClick = onStart, modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text(stringResource(R.string.action_start), modifier = Modifier.padding(start = 8.dp))
        }
    }
}
