package ch.coachpoulpy.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BuilderModel
import ch.coachpoulpy.shared.BuilderRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BuilderScreen(graph: AppGraph, language: String, workoutId: String?, onClose: () -> Unit, firstExercise: String? = null, copyOf: String? = null) {
    val model = remember(graph, language, workoutId, copyOf) {
        copyOf?.let { graph.duplicateWorkout(it, language) }
            ?: workoutId?.let { graph.editWorkout(it, language) }
            ?: firstExercise?.let { graph.createWorkoutWith(it, language) }
            ?: graph.createWorkout(language)
    }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    var picking by rememberSaveable { mutableStateOf(false) }
    // One card is open at a time: the first at start, then whichever was added or tapped last.
    var open by rememberSaveable { mutableStateOf(state.rows.firstOrNull()?.key) }
    var count by rememberSaveable { mutableStateOf(state.rows.size) }
    LaunchedEffect(state.rows.size) {
        if (state.rows.size > count) open = state.rows.last().key
        count = state.rows.size
    }
    val colors = MaterialTheme.colorScheme

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
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (model.isEditing) R.string.builder_edit_title else R.string.builder_new_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                    }
                },
                actions = {
                    Button(
                        onClick = { if (model.save() != null) onClose() },
                        enabled = state.canSave,
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        modifier = Modifier.padding(end = 12.dp).height(40.dp),
                    ) {
                        Text(stringResource(R.string.action_save), fontWeight = FontWeight.ExtraBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background, scrolledContainerColor = colors.background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel(stringResource(R.string.builder_name))
                    BoxField(state.name, model::rename, label = stringResource(R.string.builder_name), modifier = Modifier.fillMaxWidth().height(52.dp))
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(stringResource(R.string.builder_type))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        model.typeOptions.forEach { option ->
                            PoulpyChip(selected = state.type == option.type, onClick = { model.setType(option.type) }, label = option.label)
                        }
                    }
                }
            }
            if (state.rows.isEmpty()) {
                item { Text(stringResource(R.string.builder_empty), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
            }
            items(state.rows, key = { it.key }) { row ->
                EntryCard(model, row, open = row.key == open, onOpen = { open = row.key })
            }
            item { AddButton(onClick = { picking = true }) }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** A white field with a 1.5 px outline, `primary` once something is typed. */
@Composable
private fun BoxField(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    radius: Int = 16,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    center: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(radius.dp)
    BasicTextField(
        value = value,
        onValueChange = onValue,
        singleLine = true,
        keyboardOptions = keyboard,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = colors.onSurface,
            fontSize = if (center) 20.sp else 17.sp,
            fontWeight = if (center) FontWeight.ExtraBold else FontWeight.Bold,
            textAlign = if (center) TextAlign.Center else TextAlign.Start,
        ),
        cursorBrush = SolidColor(colors.primary),
        modifier = modifier
            .background(colors.surface, shape)
            .border(1.5.dp, if (value.isEmpty()) colors.outline else colors.primary, shape)
            .semantics { contentDescription = label },
        decorationBox = { field ->
            Box(Modifier.padding(horizontal = if (center) 4.dp else 16.dp), contentAlignment = Alignment.CenterStart) { field() }
        },
    )
}

/** An exercise of the workout: open, its four settings in a 2×2 grid; folded, its name and summary. */
@Composable
private fun EntryCard(model: BuilderModel, row: BuilderRow, open: Boolean, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(PoulpyRadius.md),
        color = colors.surface,
        border = if (open) BorderStroke(1.5.dp, PoulpyPalette.Lagoon300) else BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = if (open) 14.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReorderHandle(model, row)
                Column(
                    Modifier.weight(1f).then(if (open) Modifier else Modifier.clickable(onClick = onOpen)),
                ) {
                    Text(row.name, style = PoulpyTextStyles.Headline.copy(fontSize = 19.sp, lineHeight = 23.sp))
                    if (!open) {
                        Text(row.summary, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
                if (open) {
                    IconButton(onClick = { model.remove(row.key) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.builder_remove_named, row.name), tint = colors.onSurfaceVariant)
                    }
                } else {
                    IconButton(onClick = onOpen) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.builder_open, row.name), tint = colors.onSurfaceVariant)
                    }
                }
            }
            if (!row.fitsType) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.builder_does_not_fit), color = colors.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (open) {
                Column(Modifier.padding(end = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepperCell(stringResource(R.string.builder_sets), row.sets.toString(), { model.setSets(row.key, row.sets - 1) }, { model.setSets(row.key, row.sets + 1) }, Modifier.weight(1f))
                        StepperCell(stringResource(R.string.builder_target), row.amountLabel, { model.changeAmount(row.key, -1) }, { model.changeAmount(row.key, 1) }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepperCell(stringResource(R.string.builder_rest), row.restLabel, { model.changeRest(row.key, -1) }, { model.changeRest(row.key, 1) }, Modifier.weight(1f))
                        if (row.isTimed) Spacer(Modifier.weight(1f)) else WeightCell(model, row, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** The handle: until cards can be dragged, it opens Move up and Move down. */
@Composable
private fun ReorderHandle(model: BuilderModel, row: BuilderRow) {
    var menu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menu = true }, modifier = Modifier.size(32.dp, 44.dp)) {
            Icon(
                Icons.Filled.DragIndicator,
                contentDescription = stringResource(R.string.builder_reorder, row.name),
                tint = PoulpyPalette.Neutral500,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.builder_move_up)) }, onClick = { menu = false; model.moveUp(row.key) })
            DropdownMenuItem(text = { Text(stringResource(R.string.builder_move_down)) }, onClick = { menu = false; model.moveDown(row.key) })
        }
    }
}

@Composable
private fun SettingCell(label: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        content()
    }
}

@Composable
private fun StepperCell(label: String, value: String, onLess: () -> Unit, onMore: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    SettingCell(label, modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundStep("−", stringResource(R.string.builder_less_named, label), onLess)
            BasicText(
                value,
                style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 22.sp),
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
            )
            RoundStep("+", stringResource(R.string.builder_more_named, label), onMore)
        }
    }
}

@Composable
private fun RoundStep(sign: String, description: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = CircleShape, color = colors.surface, contentColor = colors.primary, modifier = Modifier.size(44.dp).semantics { contentDescription = description }) {
        Box(contentAlignment = Alignment.Center) {
            Text(sign, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun WeightCell(model: BuilderModel, row: BuilderRow, modifier: Modifier) {
    var weight by remember(row.key) { mutableStateOf(row.loadKg?.let(::formatKg) ?: "") }
    SettingCell(stringResource(R.string.builder_weight), modifier) {
        Row(Modifier.height(44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BoxField(
                weight,
                { text ->
                    weight = text
                    model.setLoadKg(row.key, text.replace(',', '.').toDoubleOrNull() ?: 0.0)
                },
                label = stringResource(R.string.builder_weight_kg),
                modifier = Modifier.width(64.dp).height(40.dp),
                radius = 10,
                keyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                center = true,
            )
            Text("kg", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The dashed "Add an exercise" tile at the end of the list. */
@Composable
private fun AddButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val dash = PoulpyPalette.Lagoon300
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = colors.primary)
        Text(stringResource(R.string.builder_add), color = colors.primary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatKg(kg: Double): String = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()
