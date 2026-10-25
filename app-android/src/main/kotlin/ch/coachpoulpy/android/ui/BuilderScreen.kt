package ch.coachpoulpy.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BuilderModel
import ch.coachpoulpy.shared.BuilderRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BuilderScreen(graph: AppGraph, language: String, workoutId: String?, onClose: () -> Unit, firstExercise: String? = null) {
    val model = remember(graph, language, workoutId) {
        workoutId?.let { graph.editWorkout(it, language) }
            ?: firstExercise?.let { graph.createWorkoutWith(it, language) }
            ?: graph.createWorkout(language)
    }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    var picking by rememberSaveable { mutableStateOf(false) }

    if (picking) {
        BackHandler { picking = false }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.builder_pick_title)) },
                    navigationIcon = { BackButton { picking = false } },
                )
            },
        ) { padding ->
            ExerciseList(
                graph, language, type = state.type,
                onPick = { model.add(it); picking = false },
                modifier = Modifier.padding(padding),
                adding = true,
            )
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (model.isEditing) R.string.builder_edit_title else R.string.builder_new_title)) },
                navigationIcon = { BackButton(onClose) },
                actions = {
                    TextButton(onClick = { if (model.save() != null) onClose() }, enabled = state.canSave) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = model::rename,
                    label = { Text(stringResource(R.string.builder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Text(stringResource(R.string.builder_type), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    model.typeOptions.forEach { option ->
                        FilterChip(
                            selected = state.type == option.type,
                            onClick = { model.setType(option.type) },
                            label = { Text(option.label) },
                        )
                    }
                }
            }
            if (state.rows.isEmpty()) {
                item { Text(stringResource(R.string.builder_empty), style = MaterialTheme.typography.bodyMedium) }
            }
            items(state.rows, key = { it.key }) { row -> EntryCard(model, row) }
            item {
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.builder_add), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun EntryCard(model: BuilderModel, row: BuilderRow) {
    var weight by remember(row.key) { mutableStateOf(row.loadKg?.let(::formatKg) ?: "") }

    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PatternBadge(row.pattern)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(row.name, style = MaterialTheme.typography.titleMedium)
                    row.perSideLabel?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                IconButton(onClick = { model.moveUp(row.key) }) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = stringResource(R.string.builder_move_up))
                }
                IconButton(onClick = { model.moveDown(row.key) }) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.builder_move_down))
                }
                IconButton(onClick = { model.remove(row.key) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.builder_remove))
                }
            }
            if (!row.fitsType) {
                Text(
                    stringResource(R.string.builder_does_not_fit),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Stepper(stringResource(R.string.builder_sets), row.sets.toString(), { model.setSets(row.key, row.sets - 1) }, { model.setSets(row.key, row.sets + 1) })
            Stepper(stringResource(R.string.builder_target), row.amountLabel, { model.changeAmount(row.key, -1) }, { model.changeAmount(row.key, 1) })
            Stepper(stringResource(R.string.builder_rest), row.restLabel, { model.changeRest(row.key, -1) }, { model.changeRest(row.key, 1) })
            if (!row.isTimed) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { text ->
                        weight = text
                        model.setLoadKg(row.key, text.replace(',', '.').toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text(stringResource(R.string.builder_weight)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(160.dp),
                )
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: String, onLess: () -> Unit, onMore: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onLess) { Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.builder_less)) }
        Text(value, style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(96.dp), textAlign = TextAlign.Center)
        IconButton(onClick = onMore) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.builder_more)) }
    }
}

private fun formatKg(kg: Double): String = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()
