package ch.coachpoulpy.android.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.DeletePrompt
import ch.coachpoulpy.shared.MuscleLoad
import ch.coachpoulpy.shared.PoulpyDrawing
import ch.coachpoulpy.shared.WorkoutFile
import ch.coachpoulpy.shared.WorkoutFileExport
import ch.coachpoulpy.shared.WorkoutPageState
import ch.coachpoulpy.shared.WorkoutStepRow
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * One saved workout: the muscles it works on a body map, and over it a sheet you drag up for its exercises, how it
 * went before, the days it is planned for, and Start.
 */
@Composable
fun WorkoutPageScreen(
    graph: AppGraph,
    language: String,
    workoutId: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
) {
    val context = LocalContext.current
    val memory = remember(context) { AppPreferences(context) }
    val model = remember(graph, language, workoutId) { graph.workoutPage(workoutId, language, memory) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    val figure = remember(graph, memory) { graph.profile(memory).profile.value.bodyFigure ?: BodyFigure.SHOULDERS }
    val shareTitle = stringResource(R.string.workout_shareTitle)
    var menu by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    // The exercise whose details are open; null when none.
    var detailing by rememberSaveable { mutableStateOf<String?>(null) }

    // Deleted here or elsewhere: nothing left to show.
    LaunchedEffect(state.loaded, state.found) { if (state.loaded && !state.found) onBack() }

    if (state.found) {
        DetailSheet(
            showBackgroundLabel = stringResource(R.string.workout_showMap),
            showDetailsLabel = stringResource(R.string.workout_showExercises),
            background = { room ->
                val mapText = stringResource(R.string.workout_mapLabel, state.muscles.joinToString(", ") { it.label })
                HeatMapPanel(
                    room, figure, state.heat, state.painAreas.toSet(),
                    description = if (state.painNote.isEmpty()) mapText else "$mapText. ${state.painNote}",
                )
            },
            floating = {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = PoulpySpace.lg, vertical = PoulpySpace.sm), horizontalArrangement = Arrangement.SpaceBetween) {
                    FloatingButton(onClick = onBack) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.action_back))
                    }
                    Box {
                        FloatingButton(onClick = { menu = true }) {
                            Icon(Lucide.EllipsisVertical, contentDescription = stringResource(R.string.action_more))
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                leadingIcon = { Icon(Lucide.Share2, contentDescription = null) },
                                onClick = { menu = false; shareWorkout(context, model.exportFile(), shareTitle) },
                            )
                            if (state.editable) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_copy)) },
                                    leadingIcon = { Icon(Lucide.Copy, contentDescription = null) },
                                    onClick = { menu = false; onCopy() },
                                )
                            }
                            HorizontalDivider()
                            DeleteMenuItem(onClick = { menu = false; deleting = true })
                        }
                    }
                }
            },
            actions = {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = PoulpySpace.lg, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.editable) {
                        OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(PoulpySize.buttonLarge)) {
                            Icon(Lucide.Pencil, contentDescription = null)
                            Text(stringResource(R.string.action_edit), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    Button(onClick = onStart, modifier = Modifier.weight(1.6f).height(PoulpySize.buttonLarge)) {
                        Icon(Lucide.Play, contentDescription = null)
                        Text(stringResource(R.string.workout_start), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            },
        ) {
            WorkoutDetails(state, onPlan = { picking = true }, onOpen = { detailing = it })
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

    detailing?.let { slug ->
        ExerciseInfoSheet(
            graph, language, slug,
            onAction = { detailing = null },
            onDismiss = { detailing = null },
            adding = false,
            closeLabel = null,
        )
    }
}

/** A round filled button over the map, with a thin outline so it stays visible on any heat. */
@Composable
internal fun FloatingButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(PoulpySize.iconButton),
    ) {
        IconButton(onClick = onClick) { content() }
    }
}

/** What the sheet holds, top to bottom: name and details, badges, Poulpy's pain note, planned days, stats, exercises, muscles. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.WorkoutDetails(state: WorkoutPageState, onPlan: () -> Unit, onOpen: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(state.name, style = MaterialTheme.typography.headlineMedium)
        Text(state.details, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Badge(state.formatLabel, Lucide.Zap, primary = true)
        state.equipment.forEach { Badge(it, Lucide.Dumbbell, primary = false) }
    }
    if (state.painNote.isNotEmpty()) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.primaryContainer).padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = PoulpySpace.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(painterResource(PoulpyDrawing.MOOD_RECOVERING.drawable), contentDescription = null, modifier = Modifier.size(44.dp))
            Text(state.painNote, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer)
        }
    }
    PlanRow(state, onPick = onPlan)
    Stats(state)
    SectionLabel(stringResource(R.string.workout_exercises))
    state.parts.forEach { part ->
        PartBlock(part.kind, part.name, part.summary, part.time) {
            var first = true
            part.steps.forEach { step ->
                if (!first) HorizontalDivider(color = colors.outline)
                first = false
                val row = step.row
                val group = step.repeatGroup
                if (row != null) {
                    ExerciseRow(row, onOpen)
                } else if (group != null) {
                    RepeatHeader(group.timesLabel)
                    group.rows.forEach { child ->
                        HorizontalDivider(color = colors.outline)
                        ExerciseRow(child, onOpen)
                    }
                }
            }
        }
    }
    if (state.muscles.isNotEmpty()) {
        SectionLabel(stringResource(R.string.workout_musclesWorked))
        Column(Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.muscles.forEach { MuscleRow(it) }
        }
    }
    Spacer(Modifier.height(PoulpySpace.lg))
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

/** The format (filled with the primary container) or one piece of equipment (outlined), as a small pill. */
@Composable
private fun Badge(text: String, icon: ImageVector, primary: Boolean) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(PoulpyRadius.pill)
    Row(
        Modifier
            .height(30.dp)
            .clip(shape)
            .background(if (primary) colors.primaryContainer else colors.surface)
            .then(if (primary) Modifier else Modifier.border(1.dp, colors.outline, shape))
            .padding(start = 10.dp, end = PoulpySpace.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (primary) colors.onPrimaryContainer else colors.onSurface, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = if (primary) colors.onPrimaryContainer else colors.onSurface, maxLines = 1)
    }
}

/** "Shoulders", a bar as long as its share of the busiest muscle in that muscle's heat, and "6 sets". */
@Composable
private fun MuscleRow(load: MuscleLoad) {
    val extras = PoulpyTheme.extraColors
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(load.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(92.dp))
        Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(PoulpyRadius.pill)).background(colors.surfaceVariant)) {
            Box(
                Modifier
                    .fillMaxWidth(load.percent / 100f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(PoulpyRadius.pill))
                    .background(heatColor(extras, load.heat)),
            )
        }
        Text(load.setsLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = colors.onSurfaceVariant, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.width(52.dp))
    }
}

private fun heatColor(extras: PoulpyExtraColors, heat: Int) = when (heat) {
    1 -> extras.heat1
    2 -> extras.heat2
    else -> extras.heat3
}

/** A part's icon in its round badge, filled for work, as on the workout page and in the builder. */
@Composable
internal fun PartBadge(kind: PartKind) {
    val colors = MaterialTheme.colorScheme
    val work = kind == PartKind.WORK
    Box(Modifier.size(36.dp).clip(CircleShape).background(if (work) colors.primary else colors.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(kind.icon, contentDescription = null, tint = if (work) colors.onPrimary else colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
    }
}

/** The icon of a part, as the builder adds it: the sun for a warm-up, a bolt for work, the moon for a cool down. */
internal val PartKind.icon: ImageVector
    get() = when (this) {
        PartKind.WARM_UP -> Lucide.Sun
        PartKind.WORK -> Lucide.Zap
        PartKind.COOL_DOWN -> Lucide.Moon
    }

/** "Repeat · 3 times", heading the rows that go round together. */
@Composable
private fun RepeatHeader(timesLabel: String) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Lucide.Repeat, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        Text("${stringResource(R.string.builder_repeat)} · $timesLabel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = colors.primary)
    }
}

/**
 * An exercise (or a rest) in a part's block, as in the session recap: its movement badge (a timer for a rest), Recover
 * tag, name and prescription. An exercise has an info button where the recap has a time, which opens its details sheet.
 */
@Composable
private fun ExerciseRow(row: WorkoutStepRow, onOpen: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (row.isRest) {
            Box(Modifier.size(PoulpySize.avatarSmall).clip(CircleShape).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Lucide.Timer, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        } else {
            PatternBadge(row.pattern ?: MovementPattern.CORE, size = PoulpySize.avatarSmall)
        }
        Column(Modifier.weight(1f)) {
            row.roleLabel?.let {
                Text(it.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
            Text(row.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            Text(row.prescription, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        if (!row.isRest) {
            IconButton(onClick = { onOpen(row.slug) }, modifier = Modifier.size(PoulpySize.avatarSmall)) {
                Icon(Lucide.Info, contentDescription = stringResource(R.string.exercise_details, row.name), tint = colors.onSurfaceVariant)
            }
        }
    }
}

/** The crimson Delete of a workout's menus, with its icon: destructive, never ember. */
@Composable
internal fun DeleteMenuItem(onClick: () -> Unit) {
    val error = MaterialTheme.colorScheme.error
    DropdownMenuItem(
        text = { Text(stringResource(R.string.action_delete), color = error) },
        leadingIcon = { Icon(Lucide.Trash, contentDescription = null, tint = error) },
        onClick = onClick,
    )
}

/** "Delete Upper body?", what goes and what stays, then Cancel and a crimson Delete. */
@Composable
internal fun DeleteWorkoutDialog(prompt: DeletePrompt, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val error = MaterialTheme.colorScheme.error
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Lucide.Trash, contentDescription = null, tint = error) },
        title = { Text(prompt.title) },
        text = { Text(prompt.message) },
        confirmButton = { TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = error, fontWeight = FontWeight.ExtraBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold) } },
    )
}

/**
 * "3 sessions planned" and "Next on Fri 30 Oct" with "Change days"; a dashed button to plan it while it waits in the library.
 */
@Composable
private fun PlanRow(state: WorkoutPageState, onPick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val next = state.plans.firstOrNull()
    if (next != null) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.primaryContainer).padding(start = PoulpySpace.md, top = 10.dp, bottom = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Lucide.Calendar, contentDescription = null, tint = colors.onPrimaryContainer)
            Column(Modifier.weight(1f)) {
                Text(
                    pluralStringResource(R.plurals.workout_plannedSessions, state.plans.size, state.plans.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onPrimaryContainer,
                )
                Text(
                    if (next.daysAway == 0) stringResource(R.string.workout_nextToday) else stringResource(R.string.workout_nextOn, dayLabel(next.dayMillis)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
            }
            Text(
                stringResource(R.string.workout_changeDays),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface,
                modifier = Modifier
                    .heightIn(min = PoulpySize.buttonSmall)
                    .clip(RoundedCornerShape(PoulpyRadius.pill))
                    .background(colors.surface)
                    .clickable(role = Role.Button, onClick = onPick)
                    .padding(horizontal = PoulpySpace.md, vertical = 8.dp),
            )
        }
    } else {
        OutlinedButton(
            onClick = onPick,
            border = BorderStroke(2.dp, PoulpyPalette.Lagoon300),
            shape = RoundedCornerShape(PoulpyRadius.md),
            modifier = Modifier.fillMaxWidth().height(PoulpySize.button),
        ) {
            Icon(Lucide.CalendarPlus, contentDescription = null)
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
            .clip(RoundedCornerShape(PoulpyRadius.lg))
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

/**
 * Hands [file] to the system share sheet as a `.poulpyworkout` file. It is written under the cache folder and
 * shared through [FileProvider] (see `res/xml/file_paths.xml`), so the receiving app can read it and nothing else.
 */
private fun shareWorkout(context: Context, file: WorkoutFileExport?, title: String) {
    if (file == null) return
    val folder = java.io.File(context.cacheDir, "shared").apply { mkdirs() }
    val written = java.io.File(folder, file.fileName).apply { writeText(file.content) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", written)
    val send = Intent(Intent.ACTION_SEND)
        .setType(WorkoutFile.MIME_TYPE)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, title))
}
