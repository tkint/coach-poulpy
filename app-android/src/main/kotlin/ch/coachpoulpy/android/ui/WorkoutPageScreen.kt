package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.DeletePrompt
import ch.coachpoulpy.shared.WorkoutPageState
import ch.coachpoulpy.shared.WorkoutStepRow
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** One saved workout: its exercises, how it went before, the day it is planned for, and Start. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPageScreen(
    graph: AppGraph,
    language: String,
    workoutId: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    val model = remember(graph, language, workoutId) { graph.workoutPage(workoutId, language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    var menu by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }

    // Deleted here or elsewhere: nothing left to show.
    LaunchedEffect(state.loaded, state.found) { if (state.loaded && !state.found) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (state.found) {
                        Box {
                            IconButton(onClick = { menu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                if (state.editable) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_copy)) },
                                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                                        onClick = { menu = false; onCopy() },
                                    )
                                    HorizontalDivider()
                                }
                                DeleteMenuItem(onClick = { menu = false; deleting = true })
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (state.found) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (state.editable) {
                                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(52.dp)) {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                    Text(stringResource(R.string.action_edit), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 6.dp))
                                }
                            }
                            Button(onClick = onStart, modifier = Modifier.weight(1.6f).height(52.dp)) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Text(stringResource(R.string.workout_start), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (!state.found) return@Scaffold
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(state.typeLabel.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(state.name, style = MaterialTheme.typography.headlineMedium)
                Text(state.details, style = MaterialTheme.typography.bodyLarge)
            }
            PlanRow(state, onPick = { picking = true })
            Stats(state)
            Text(
                stringResource(R.string.workout_exercises).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
            // Exercises are numbered in order, through repeats; rests show a timer instead.
            var number = 0
            state.structure.forEach { step ->
                val row = step.row
                val group = step.repeatGroup
                if (row != null) {
                    StructureRow(row, if (row.isRest) null else ++number, onOpenExercise)
                } else if (group != null) {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(MaterialTheme.colorScheme.primaryContainer).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                "${stringResource(R.string.builder_repeat)} · ${group.timesLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        group.rows.forEach { child -> StructureRow(child, if (child.isRest) null else ++number, onOpenExercise) }
                    }
                }
            }
        }
    }

    if (picking) {
        PlanSheet(
            name = state.name,
            planned = state.plans,
            onSave = {
                model.setDays(it)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }

    if (deleting) DeleteWorkoutDialog(state.deletePrompt, onDelete = { deleting = false; model.delete() }, onDismiss = { deleting = false })
}

/** A step of the workout page: its number (a timer for a rest), role, name and prescription; an exercise opens its page. */
@Composable
private fun StructureRow(row: WorkoutStepRow, number: Int?, onOpenExercise: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PoulpyRadius.md))
            .background(colors.surface)
            .then(if (row.isRest) Modifier else Modifier.clickable { onOpenExercise(row.slug) })
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(if (row.isRest) colors.surfaceVariant else colors.primaryContainer), contentAlignment = Alignment.Center) {
            if (number != null) {
                Text("$number", style = MaterialTheme.typography.titleSmall, color = colors.onPrimaryContainer)
            } else {
                Icon(Icons.Filled.Timer, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            row.roleLabel?.takeIf { !row.isRest }?.let {
                Text(it.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
            Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(row.prescription, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (!row.isRest) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
    }
}

/** The crimson Delete of a workout's menus, with its icon: destructive, never ember. */
@Composable
internal fun DeleteMenuItem(onClick: () -> Unit) {
    val error = MaterialTheme.colorScheme.error
    DropdownMenuItem(
        text = { Text(stringResource(R.string.action_delete), color = error) },
        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = error) },
        onClick = onClick,
    )
}

/** "Delete Upper body?", what goes and what stays, then Cancel and a crimson Delete. */
@Composable
internal fun DeleteWorkoutDialog(prompt: DeletePrompt, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val error = MaterialTheme.colorScheme.error
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = error) },
        title = { Text(prompt.title) },
        text = { Text(prompt.message) },
        confirmButton = { TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = error, fontWeight = FontWeight.ExtraBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold) } },
    )
}

/**
 * "Planned for Tue 27 Oct", or "Planned on 3 days" with each upcoming day as a pill, and
 * "Change days"; a dashed button to plan it while it waits in the library.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanRow(state: WorkoutPageState, onPick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val plans = state.plans
    if (plans.isNotEmpty()) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.primaryContainer).padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = colors.onPrimaryContainer)
                val single = plans.singleOrNull()
                Text(
                    when {
                        single == null -> pluralStringResource(R.plurals.workout_planned_days, plans.size, plans.size)
                        single.daysAway == 0 -> stringResource(R.string.workout_planned_today)
                        else -> stringResource(R.string.workout_planned, dayLabel(single.dayMillis))
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onPick) { Text(stringResource(R.string.workout_change_days), fontWeight = FontWeight.ExtraBold) }
            }
            if (plans.size > 1) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plans.forEach { plan ->
                        Text(
                            if (plan.daysAway == 0) stringResource(R.string.home_today) else dayLabel(plan.dayMillis),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.clip(RoundedCornerShape(PoulpyRadius.pill)).background(colors.surface).padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    } else {
        OutlinedButton(
            onClick = onPick,
            border = BorderStroke(2.dp, PoulpyPalette.Lagoon300),
            shape = RoundedCornerShape(PoulpyRadius.md),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Icon(Icons.Filled.EditCalendar, contentDescription = null)
            Text(stringResource(R.string.workout_plan), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun Stats(state: WorkoutPageState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatBox(stringResource(R.string.workout_done), pluralStringResource(R.plurals.workout_times, state.timesDone, state.timesDone), Modifier.weight(1f))
        StatBox(stringResource(R.string.workout_last), state.lastDoneMillis?.let(::shortDay) ?: "–", Modifier.weight(1f))
        StatBox(stringResource(R.string.workout_usually), state.usualMinutes?.let { stringResource(R.string.workout_minutes, it) } ?: "–", Modifier.weight(1f))
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(label.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

/** "21 Oct", in the user's order. */
private fun shortDay(millis: Long): String =
    java.time.format.DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMM"), Locale.getDefault())
        .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
