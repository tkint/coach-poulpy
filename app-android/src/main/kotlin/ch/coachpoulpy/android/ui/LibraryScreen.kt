package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.LibraryNote
import ch.coachpoulpy.shared.LibraryRow
import ch.coachpoulpy.shared.LibrarySort

enum class LibraryView { WORKOUTS, EXERCISES }

/** The saved workouts, with or without a day, and the exercise catalog beside them. */
@Composable
fun LibraryScreen(
    graph: AppGraph,
    language: String,
    view: LibraryView,
    onView: (LibraryView) -> Unit,
    onOpenWorkout: (String) -> Unit,
    onStart: (String) -> Unit,
    onNew: () -> Unit,
    onOpenExercise: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, top = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.tab_library), style = MaterialTheme.typography.headlineMedium)
            ViewSwitch(view, onView)
        }
        when (view) {
            LibraryView.WORKOUTS -> Workouts(graph, language, onOpenWorkout, onStart, onNew)
            LibraryView.EXERCISES -> ExerciseList(graph, language, type = null, onPick = onOpenExercise, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Workouts | Exercises, as a pill with the chosen half raised, like History's. */
@Composable
private fun ViewSwitch(selected: LibraryView, onSelect: (LibraryView) -> Unit) {
    val pill = RoundedCornerShape(PoulpyRadius.pill)
    Row(Modifier.fillMaxWidth().clip(pill).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp).selectableGroup()) {
        listOf(LibraryView.WORKOUTS to R.string.library_workouts, LibraryView.EXERCISES to R.string.library_exercises).forEach { (item, label) ->
            val on = item == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(pill)
                    .background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(item) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(label),
                    color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun Workouts(graph: AppGraph, language: String, onOpen: (String) -> Unit, onStart: (String) -> Unit, onNew: () -> Unit) {
    val model = remember(graph, language) { graph.library(language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf<WorkoutType?>(null) }
    var sort by rememberSaveable { mutableStateOf(LibrarySort.RECENT) }
    var sorting by remember { mutableStateOf(false) }
    val rows = remember(state, query, kind, sort) { model.filter(state, query, kind, sort) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.library_search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(PoulpyRadius.pill),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (state.kinds.size > 1) {
                item(key = "kinds") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = kind == null, onClick = { kind = null }, label = { Text(stringResource(R.string.library_all)) }) }
                        items(state.kinds, key = { it.type.name }) { filter ->
                            FilterChip(
                                selected = kind == filter.type,
                                onClick = { kind = if (kind == filter.type) null else filter.type },
                                label = { Text(filter.label) },
                            )
                        }
                    }
                }
            }
            if (state.loaded && state.rows.isEmpty()) {
                item(key = "empty") {
                    Column(Modifier.padding(top = 24.dp, start = 4.dp, end = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.library_empty_title), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.library_empty_body), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (state.loaded) {
                item(key = "count") {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            pluralStringResource(R.plurals.library_count, rows.size, rows.size),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Box {
                            TextButton(onClick = { sorting = true }) {
                                Text(stringResource(sort.label), fontWeight = FontWeight.ExtraBold)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = sorting, onDismissRequest = { sorting = false }) {
                                LibrarySort.entries.forEach { option ->
                                    DropdownMenuItem(text = { Text(stringResource(option.label)) }, onClick = { sort = option; sorting = false })
                                }
                            }
                        }
                    }
                }
                if (rows.isEmpty()) {
                    item(key = "none") { Text(stringResource(R.string.library_none), modifier = Modifier.padding(4.dp)) }
                }
                items(rows, key = { it.id }) { row -> WorkoutCard(row, onOpen = { onOpen(row.id) }, onStart = { onStart(row.id) }) }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNew,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.library_new_workout)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@Composable
private fun WorkoutCard(row: LibraryRow, onOpen: () -> Unit, onStart: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onOpen,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(row.type.icon, contentDescription = null, tint = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(row.details, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Note(row)
            }
            FilledTonalIconButton(onClick = onStart, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.action_start) + " " + row.name)
            }
        }
    }
}

@Composable
private fun Note(row: LibraryRow) {
    val colors = MaterialTheme.colorScheme
    val millis = row.noteMillis
    when (row.note) {
        LibraryNote.PLANNED -> Text(
            stringResource(R.string.library_planned, millis?.let(::dayLabel) ?: ""),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onPrimaryContainer,
            modifier = Modifier.padding(top = 3.dp).clip(RoundedCornerShape(PoulpyRadius.pill)).background(colors.primaryContainer).padding(horizontal = 8.dp, vertical = 2.dp),
        )
        LibraryNote.DONE_TODAY -> Text(
            stringResource(R.string.library_done_today),
            style = MaterialTheme.typography.labelMedium,
            color = PoulpyTheme.extraColors.success,
            modifier = Modifier.padding(top = 3.dp),
        )
        LibraryNote.LAST_DONE -> Text(
            stringResource(R.string.library_last_done, millis?.let(::dayLabel) ?: ""),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp),
        )
        LibraryNote.NOT_DONE_YET -> Text(
            stringResource(R.string.library_not_done),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

private val LibrarySort.label: Int
    get() = when (this) {
        LibrarySort.RECENT -> R.string.library_sort_recent
        LibrarySort.ALPHABETICAL -> R.string.library_sort_name
    }

internal val WorkoutType.icon: ImageVector
    get() = when (this) {
        WorkoutType.TREADMILL -> Icons.AutoMirrored.Filled.DirectionsRun
        WorkoutType.MOBILITY, WorkoutType.REHAB -> Icons.Filled.SelfImprovement
        else -> Icons.Filled.FitnessCenter
    }
