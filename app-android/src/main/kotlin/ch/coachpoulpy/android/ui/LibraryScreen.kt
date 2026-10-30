package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.WorkoutFormat
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
    onCopy: (String) -> Unit,
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
            LibraryView.WORKOUTS -> Box(Modifier.padding(top = 4.dp)) { Workouts(graph, language, onOpenWorkout, onStart, onNew, onCopy) }
            LibraryView.EXERCISES -> ExerciseList(graph, language, onPick = onOpenExercise, modifier = Modifier.padding(top = 4.dp))
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

/**
 * The saved workouts under the filter row the exercise list uses, with the sort at the
 * right of the count line; Poulpy's card when nothing matches or nothing is saved yet.
 */
@Composable
private fun Workouts(
    graph: AppGraph,
    language: String,
    onOpen: (String) -> Unit,
    onStart: (String) -> Unit,
    onNew: () -> Unit,
    onCopy: (String) -> Unit,
) {
    val model = remember(graph, language) { graph.library(language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf<WorkoutFormat?>(null) }
    var sort by rememberSaveable { mutableStateOf(LibrarySort.RECENT) }
    var sorting by remember { mutableStateOf(false) }
    val listing = remember(state, query, kind, sort) { model.filter(state, query, kind, sort) }
    val clearAll = {
        query = ""
        kind = null
    }
    // The workout whose plan sheet or delete dialog is open, by id so a fresh row is used.
    var planning by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }

    if (!state.loaded) return
    if (state.rows.isEmpty()) {
        PoulpyCard(
            title = stringResource(R.string.library_emptyTitle),
            body = stringResource(R.string.library_emptyBody),
            button = stringResource(R.string.library_buildFirst),
            onClick = onNew,
            buttonIcon = Icons.Filled.Add,
            poulpySize = 120.dp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
        )
        return
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ListFilters(
                query = query,
                onQuery = { query = it },
                placeholder = stringResource(R.string.library_search),
                label = stringResource(R.string.library_searchLabel),
                countLabel = listing.countLabel,
                chips = {
                    item { PoulpyChip(selected = kind == null, onClick = { kind = null }, label = stringResource(R.string.library_all)) }
                    items(state.kinds, key = { it.format.name }) { filter ->
                        PoulpyChip(
                            selected = kind == filter.format,
                            onClick = { kind = if (kind == filter.format) null else filter.format },
                            label = filter.label,
                        )
                    }
                },
                action = {
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
                },
            )
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(listing.rows, key = { it.id }) { row ->
                    WorkoutCard(
                        row,
                        onOpen = { onOpen(row.id) },
                        onStart = { onStart(row.id) },
                        onPlan = { planning = row.id },
                        onCopy = { onCopy(row.id) },
                        onDelete = { deleting = row.id },
                    )
                }
                if (listing.rows.isEmpty()) {
                    item(key = "none") {
                        PoulpyCard(
                            title = if (listing.searching) stringResource(R.string.library_nothingFor, query.trim()) else stringResource(R.string.library_noKind),
                            body = stringResource(R.string.library_nothingHint),
                            button = stringResource(R.string.exercises_clearAll),
                            onClick = clearAll,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNew,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.library_newWorkout)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
    state.rows.firstOrNull { it.id == planning }?.let { row ->
        PlanSheet(
            name = row.name,
            planned = row.plans,
            onSave = {
                model.setDays(row, it)
                planning = null
            },
            onDismiss = { planning = null },
        )
    }
    state.rows.firstOrNull { it.id == deleting }?.let { row ->
        DeleteWorkoutDialog(row.deletePrompt, onDelete = { deleting = null; model.delete(row) }, onDismiss = { deleting = null })
    }
}

/**
 * A tap opens the workout and Play starts it; a long press raises the card and opens
 * its menu: Plan for a day, Copy (builder-made workouts only), then a crimson Delete.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutCard(row: LibraryRow, onOpen: () -> Unit, onStart: () -> Unit, onPlan: () -> Unit, onCopy: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    var menu by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(PoulpyRadius.md)
    Box {
        Surface(
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline),
            shape = shape,
            shadowElevation = if (menu) 8.dp else 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menu = true
                    },
                    onLongClickLabel = stringResource(R.string.action_more),
                ),
        ) {
            Row(
                Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(row.format.icon, contentDescription = null, tint = colors.onPrimaryContainer)
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
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, offset = DpOffset(12.dp, 4.dp)) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.library_planDay)) },
                leadingIcon = { Icon(Icons.Filled.EditCalendar, contentDescription = null) },
                onClick = { menu = false; onPlan() },
            )
            if (row.editable) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_copy)) },
                    leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                    onClick = { menu = false; onCopy() },
                )
            }
            HorizontalDivider()
            DeleteMenuItem(onClick = { menu = false; onDelete() })
        }
    }
}

@Composable
private fun Note(row: LibraryRow) {
    val colors = MaterialTheme.colorScheme
    val millis = row.noteMillis
    when (row.note) {
        LibraryNote.PLANNED -> Text(
            stringResource(R.string.library_planned, millis?.let(::dayLabel) ?: "") + if (row.morePlanned > 0) " · +${row.morePlanned}" else "",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onPrimaryContainer,
            modifier = Modifier.padding(top = 3.dp).clip(RoundedCornerShape(PoulpyRadius.pill)).background(colors.primaryContainer).padding(horizontal = 8.dp, vertical = 2.dp),
        )
        LibraryNote.DONE_TODAY -> Text(
            stringResource(R.string.library_doneToday),
            style = MaterialTheme.typography.labelMedium,
            color = PoulpyTheme.extraColors.success,
            modifier = Modifier.padding(top = 3.dp),
        )
        LibraryNote.LAST_DONE -> Text(
            stringResource(R.string.library_lastDone, millis?.let(::dayLabel) ?: ""),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp),
        )
        LibraryNote.NOT_DONE_YET -> Text(
            stringResource(R.string.library_notDone),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

private val LibrarySort.label: Int
    get() = when (this) {
        LibrarySort.RECENT -> R.string.library_sortRecent
        LibrarySort.NEXT_PLANNED -> R.string.library_sortPlanned
        LibrarySort.ALPHABETICAL -> R.string.library_sortName
    }

internal val WorkoutFormat.icon: ImageVector
    get() = when (this) {
        WorkoutFormat.CIRCUIT -> Icons.Filled.Repeat
        WorkoutFormat.SETS -> Icons.AutoMirrored.Filled.List
    }
