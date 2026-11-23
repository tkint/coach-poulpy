package ch.coachpoulpy.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.shared.FormatChange
import ch.coachpoulpy.shared.FormatSwitch
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.AmountUnit
import ch.coachpoulpy.shared.BuilderModel
import ch.coachpoulpy.shared.BuilderPart
import ch.coachpoulpy.shared.BuilderRepeat
import ch.coachpoulpy.shared.BuilderRow
import ch.coachpoulpy.shared.DistanceUnit
import ch.coachpoulpy.shared.DurationKind
import ch.coachpoulpy.shared.IntensityKind
import ch.coachpoulpy.shared.PaceUnit
import ch.coachpoulpy.shared.WalkRunTarget
import ch.coachpoulpy.shared.WorkoutImport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(graph: AppGraph, language: String, workoutId: String?, onClose: () -> Unit, newFormat: WorkoutFormat? = null, firstExercise: String? = null, copyOf: String? = null, importing: WorkoutImport? = null) {
    val model = remember(graph, language, workoutId, copyOf, importing, newFormat) {
        importing?.builder()
            ?: copyOf?.let { graph.duplicateWorkout(it, language) }
            ?: workoutId?.let { graph.editWorkout(it, language) }
            ?: newFormat?.let { format -> firstExercise?.let { graph.createWorkoutWith(format, it, language) } ?: graph.createWorkout(format, language) }
            ?: error("A builder opens on a workout, a copy, an import or a new one with its format")
    }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    // The part the exercise being picked goes to; null when not picking.
    var pickingPart by rememberSaveable { mutableStateOf<String?>(null) }
    // The repeat of that part the exercise goes into; null when it goes to the part itself.
    var pickingRepeat by rememberSaveable { mutableStateOf<String?>(null) }
    // The repeat whose count is being edited in the sheet; null when none.
    var countingRepeat by rememberSaveable { mutableStateOf<String?>(null) }
    // The exercise whose details are open over the picker; null when none.
    var previewing by rememberSaveable { mutableStateOf<String?>(null) }
    // The step being edited in the sheet: the one just added, or the one tapped.
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    // The exercise whose details are open over the step's sheet; null when none.
    var detailing by rememberSaveable { mutableStateOf<String?>(null) }
    var changingFormat by rememberSaveable { mutableStateOf(false) }
    // The switch waiting for "Follow the new format" or "Keep the order"; null when none.
    var askingOrder by remember { mutableStateOf<FormatChange?>(null) }
    LaunchedEffect(state.addedKey) { state.addedKey?.let { editing = it } }
    val colors = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    pickingPart?.let { part ->
        BackHandler { pickingPart = null; pickingRepeat = null }
        Scaffold { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                AppBar(stringResource(R.string.builder_pickTitle), leading = { BackButton(onBack = { pickingPart = null; pickingRepeat = null }) })
                ExerciseList(
                    graph, language,
                    onPick = { model.add(it, part, pickingRepeat); pickingPart = null; pickingRepeat = null },
                    modifier = Modifier.weight(1f),
                    adding = true,
                    onOpen = { previewing = it },
                )
            }
        }
        previewing?.let { slug ->
            ExerciseInfoSheet(
                graph, language, slug,
                onAction = { model.add(it, part, pickingRepeat); previewing = null; pickingPart = null; pickingRepeat = null },
                onDismiss = { previewing = null },
            )
        }
        return
    }

    Scaffold(containerColor = colors.background) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            AppBar(
                title = stringResource(if (model.isEditing) R.string.builder_editTitle else R.string.builder_newTitle),
                leading = { CloseButton(onClose) },
                actions = {
                    PoulpyButton(
                        onClick = { if (model.save() != null) onClose() },
                        size = PoulpyButtonSize.Sm,
                        enabled = state.canSave,
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
            LazyColumn(
                state = list,
                modifier = Modifier.weight(1f).fillMaxWidth().navigationBarsPadding(),
                contentPadding = PaddingValues(start = PoulpySpace.lg, end = PoulpySpace.lg, top = 8.dp, bottom = 24.dp),
            ) {
                item {
                    PoulpyTextField(
                        stringResource(R.string.builder_name), state.name, model::rename,
                        Modifier.padding(bottom = 14.dp).fillMaxWidth(),
                        placeholder = stringResource(R.string.builder_namePlaceholder),
                    )
                }
                item { FormatLine(state.format, state.formatLabel, onClick = { changingFormat = true }) }
                state.parts.forEach { part ->
                    item(key = "part-${part.key}") {
                        Box(Modifier.padding(bottom = 8.dp)) {
                            BuilderPartBlock(model, part, onPick = { repeatKey -> pickingPart = part.key; pickingRepeat = repeatKey }, onEdit = { editing = it }, onCount = { countingRepeat = it })
                        }
                    }
                }
            }
        }
    }

    if (changingFormat) {
        FormatSheet(model.formatOptions, current = state.format, onChoose = { format ->
            changingFormat = false
            val change = model.formatChange(format)
            if (change != null) {
                if (change.asks) {
                    askingOrder = change
                } else {
                    model.changeFormat(format, FormatSwitch.KEEP_ORDER)
                }
            }
        }, onClose = { changingFormat = false })
    }
    askingOrder?.let { change ->
        FormatOrderSheet(change, onChoose = { switch -> model.changeFormat(change.format, switch); askingOrder = null }, onClose = { askingOrder = null })
    }
    state.parts.flatMap { it.steps }.firstNotNullOfOrNull { step -> step.repeatGroup?.takeIf { it.key == countingRepeat } }?.let { repeat ->
        RepeatCountSheet(model, repeat, onDone = { countingRepeat = null })
    }
    state.rows.firstOrNull { it.key == editing }?.let { row ->
        StepSheet(model, row, state.format, onDone = { editing = null }, onDetails = { detailing = row.slug })
    }
    detailing?.let { slug ->
        ExerciseInfoSheet(graph, language, slug, onAction = { detailing = null }, onDismiss = { detailing = null }, adding = false)
    }
}

/** A sheet's title and the line under it. */
@Composable
internal fun SheetTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Under the name: the format as a chip with a chevron; a tap opens the format sheet to change it. */
@Composable
private fun FormatLine(format: WorkoutFormat, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.builder_changeFormat, label)
    val interactionSource = remember { MutableInteractionSource() }
    Row(Modifier.padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FieldLabel(stringResource(R.string.builder_format))
        Box(
            Modifier
                .heightIn(min = 48.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClickLabel = description,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics { contentDescription = description },
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .indication(interactionSource, ripple())
                    .background(colors.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(format.icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(18.dp))
                Text(label, fontWeight = FontWeight.ExtraBold, color = colors.onPrimaryContainer)
                Icon(Lucide.ChevronDown, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * A part as an outlined block, like on the workout page: its steps as rows, a small + on the border to add to it,
 * and while it is empty one line that opens the exercise list. The warm-up and the cool down are optional.
 */
@Composable
private fun BuilderPartBlock(model: BuilderModel, part: BuilderPart, onPick: (String?) -> Unit, onEdit: (String) -> Unit, onCount: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val empty = part.steps.isEmpty()
    PartBlock(
        part.kind, part.name, part.summary, time = "",
        corner = {
            PartAddButton(
                label = part.addLabel,
                iconSize = if (empty) 16.dp else 20.dp,
                onExercise = { onPick(null) },
                onRest = { model.addRest(part.key) },
                onRepeat = { model.addRepeat(part.key) },
            )
        },
    ) {
        if (empty) {
            Text(
                stringResource(if (part.kind == PartKind.WORK) R.string.builder_emptyWork else R.string.builder_emptyOptional),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(PoulpyRadius.sm))
                    .clickable(onClickLabel = part.addLabel) { onPick(null) }
                    .padding(vertical = 13.dp),
            )
        } else {
            Column(Modifier.padding(vertical = 4.dp)) {
                part.steps.forEachIndexed { index, step ->
                    if (index > 0) HorizontalDivider(color = colors.outline)
                    val row = step.row
                    val repeat = step.repeatGroup
                    if (row != null) {
                        StepRow(model, row, onEdit = { onEdit(row.key) })
                    } else if (repeat != null) {
                        RepeatBlock(model, part, repeat, onEdit, onPick = { onPick(repeat.key) }, onCount = { onCount(repeat.key) })
                    }
                }
            }
        }
    }
}

/** The small square + on a block's border: a tap asks what to add. An exercise is picked from the exercise list; a repeat or a rest is added right away. */
@Composable
private fun PartAddButton(label: String, iconSize: Dp, onExercise: () -> Unit, onRest: () -> Unit, onRepeat: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.size(48.dp).clickable(onClickLabel = label, role = Role.Button) { expanded = true }.semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(28.dp).background(colors.background, RoundedCornerShape(PoulpyRadius.sm)).border(1.dp, colors.outline, RoundedCornerShape(PoulpyRadius.sm)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Plus, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(iconSize))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_addExercise)) },
                leadingIcon = { Icon(Lucide.PersonStanding, contentDescription = null) },
                onClick = { expanded = false; onExercise() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_addRepeat)) },
                leadingIcon = { Icon(Lucide.Repeat, contentDescription = null) },
                onClick = { expanded = false; onRepeat() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_addRest)) },
                leadingIcon = { Icon(Lucide.Timer, contentDescription = null) },
                onClick = { expanded = false; onRest() },
            )
        }
    }
}

/**
 * A repeat inside a block: one compact line, "Repeat 3 times", that opens the count sheet, and a menu; its steps sit in it
 * as rows. An empty repeat says so in one line that opens the exercise list, like an empty block.
 */
@Composable
private fun RepeatBlock(model: BuilderModel, part: BuilderPart, repeat: BuilderRepeat, onEdit: (String) -> Unit, onPick: () -> Unit, onCount: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(colors.primaryContainer, RoundedCornerShape(PoulpyRadius.md))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 36.dp)
                    .clip(RoundedCornerShape(PoulpyRadius.sm))
                    .clickable(onClickLabel = stringResource(R.string.builder_editRepeats), role = Role.Button, onClick = onCount)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Lucide.Repeat, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Text(repeat.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = colors.onPrimaryContainer)
            }
            ActionsMenu(
                description = stringResource(R.string.builder_repeatActions),
                onEditRepeats = onCount,
                onAddExercise = onPick,
                onAddRest = { model.addRest(part.key, repeat.key) },
                onDuplicate = { model.duplicate(repeat.key) },
                onMoveUp = if (repeat.canMoveUp) ({ model.moveUp(repeat.key) }) else null,
                onMoveDown = if (repeat.canMoveDown) ({ model.moveDown(repeat.key) }) else null,
                onRemove = { model.remove(repeat.key) },
                compact = true,
            )
        }
        if (repeat.rows.isEmpty()) {
            Text(
                stringResource(R.string.builder_emptyWork),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(PoulpyRadius.sm))
                    .clickable(onClickLabel = stringResource(R.string.builder_addExercise), onClick = onPick)
                    .padding(vertical = 13.dp),
            )
        }
        repeat.rows.forEach { child -> StepRow(model, child, onEdit = { onEdit(child.key) }, inRepeat = true) }
    }
}

/** The sheet that sets how many times a repeat runs: a number typed or nudged with − and +. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatCountSheet(model: BuilderModel, repeat: BuilderRepeat, onDone: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var text by remember(repeat.times) { mutableStateOf(repeat.times.toString()) }
    ModalBottomSheet(onDismissRequest = onDone, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.builder_repeat), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FieldLabel(stringResource(R.string.builder_repeatHowMany))
                NumberEntry(
                    onLess = { model.setRepeatTimes(repeat.key, repeat.times - 1) },
                    onMore = { model.setRepeatTimes(repeat.key, repeat.times + 1) },
                    Modifier.fillMaxWidth(),
                    lessLabel = stringResource(R.string.builder_repeatFewer),
                    moreLabel = stringResource(R.string.builder_repeatMore),
                ) { fieldModifier ->
                    NumberField(
                        value = text,
                        onValueChange = { typed ->
                            text = typed
                            typed.toIntOrNull()?.let { model.setRepeatTimes(repeat.key, it) }
                        },
                        description = stringResource(R.string.builder_repeatHowMany),
                        modifier = fieldModifier,
                        unit = stringResource(R.string.builder_repeatUnit),
                    )
                }
            }
            PoulpyButton(onClick = onDone, size = PoulpyButtonSize.Lg, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_done))
            }
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

/**
 * A step of the workout as a row, like on the workout page: its movement badge, name (with the Recover tag on its line)
 * and what it asks for. A tap edits it in a sheet; the ⋮ menu has the rest. Inside a repeat the row sits on a card.
 */
@Composable
private fun StepRow(model: BuilderModel, row: BuilderRow, onEdit: () -> Unit, inRepeat: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (inRepeat) Modifier.background(colors.surface, RoundedCornerShape(PoulpyRadius.sm)).padding(start = 8.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
                .clickable(onClickLabel = stringResource(R.string.builder_open, row.name), onClick = onEdit)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (row.isRest) {
                Box(Modifier.size(PoulpySize.avatarSmall).clip(CircleShape).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Lucide.Timer, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            } else {
                PatternBadge(row.pattern, size = PoulpySize.avatarSmall)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(row.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    row.roleLabel?.let { RoleTag(it, row.role) }
                }
                Text(row.summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        ActionsMenu(
            description = stringResource(R.string.builder_moreActions, row.name),
            onEdit = onEdit,
            onDuplicate = { model.duplicate(row.key) },
            onMoveUp = if (row.canMoveUp) ({ model.moveUp(row.key) }) else null,
            onMoveDown = if (row.canMoveDown) ({ model.moveDown(row.key) }) else null,
            onMoveIn = if (row.canMoveIntoRepeat) ({ model.moveIntoRepeatAbove(row.key) }) else null,
            onMoveOut = if (row.repeatKey != null) ({ model.moveOutOfRepeat(row.key) }) else null,
            onRemove = { model.remove(row.key) },
        )
    }
}

/**
 * Edits one step: what it is for, then reps and weight for an exercise, with its sets and the
 * rest between them in a Sets workout; a duration and an intensity for a walk or a run; or the
 * length of a rest.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepSheet(model: BuilderModel, row: BuilderRow, format: WorkoutFormat?, onDone: () -> Unit, onDetails: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Opens fully, so every field shows without dragging the sheet up.
    ModalBottomSheet(onDismissRequest = onDone, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(
            Modifier.padding(horizontal = PoulpySpace.lg).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.roleLabel?.let { RoleTag(it, row.role) }
                    SheetTitle(row.name, row.place)
                }
                if (!row.isRest) {
                    PoulpyButton(onClick = onDetails, kind = PoulpyButtonKind.Tonal) {
                        Icon(Lucide.Info, contentDescription = null, modifier = Modifier.size(PoulpyControlIcon.md))
                        Text(stringResource(R.string.builder_details), modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            val target = row.target
            if (row.isRest) {
                NumberCell(
                    stringResource(R.string.builder_duration), row.restSeconds, AmountUnit.SECONDS,
                    { model.setRest(row.key, it) }, { model.changeRest(row.key, -1) }, { model.changeRest(row.key, 1) },
                )
            } else {
                PoulpySelect(
                    stringResource(R.string.builder_step),
                    model.roleOptions.first { it.role == row.role }.label,
                    model.roleOptions.map { option -> option.label to { model.setRole(row.key, option.role) } },
                    Modifier.fillMaxWidth(),
                )
                if (target != null) {
                    TargetEditor(model, row, target)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (row.showsSets) {
                            NumberCell(
                                stringResource(R.string.builder_sets), row.sets, null,
                                { model.setSets(row.key, it) }, { model.setSets(row.key, row.sets - 1) }, { model.setSets(row.key, row.sets + 1) },
                            )
                        }
                        NumberCell(
                            stringResource(R.string.builder_target), row.amount, row.amountUnit,
                            { model.setAmount(row.key, it) }, { model.changeAmount(row.key, -1) }, { model.changeAmount(row.key, 1) },
                        )
                        if (row.showsSets && row.sets >= 2) {
                            NumberCell(
                                stringResource(R.string.builder_restBetweenSets), row.restBetweenSets, AmountUnit.SECONDS,
                                { model.setRestBetweenSets(row.key, it) }, { model.changeRestBetweenSets(row.key, -1) }, { model.changeRestBetweenSets(row.key, 1) },
                            )
                        }
                        if (!row.isTimed) WeightCell(model, row)
                        Text(
                            stringResource(if (format == WorkoutFormat.CIRCUIT) R.string.builder_circuitHint else R.string.builder_setsHint),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
            PoulpyButton(onClick = onDone, size = PoulpyButtonSize.Lg, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}

/** The small tag on a step's name line: "RECOVER". */
@Composable
private fun RoleTag(label: String, role: StepRole) {
    val recover = role == StepRole.RECOVER
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, letterSpacing = 0.06.em),
        color = if (recover) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .background(if (recover) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

/**
 * The ⋮ menu of a step or a repeat, an icon on every line: edit (a step), edit repetitions and add an exercise or a rest
 * (a repeat), duplicate, the moves that apply (up, down, into the repeat above, out of its repeat), and delete.
 */
@Composable
private fun ActionsMenu(
    description: String,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onEditRepeats: (() -> Unit)? = null,
    onAddExercise: (() -> Unit)? = null,
    onAddRest: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onMoveIn: (() -> Unit)? = null,
    onMoveOut: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = if (compact) Modifier.size(36.dp) else Modifier) {
            Icon(Lucide.EllipsisVertical, contentDescription = description, tint = colors.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            onEdit?.let { MenuLine(stringResource(R.string.builder_edit), Lucide.Pencil) { expanded = false; it() } }
            onEditRepeats?.let {
                MenuLine(stringResource(R.string.builder_editRepeats), Lucide.Pencil) { expanded = false; it() }
                HorizontalDivider()
            }
            if (onAddExercise != null && onAddRest != null) {
                MenuLine(stringResource(R.string.builder_addExercise), Lucide.PersonStanding) { expanded = false; onAddExercise() }
                MenuLine(stringResource(R.string.builder_addRest), Lucide.Timer) { expanded = false; onAddRest() }
                HorizontalDivider()
            }
            MenuLine(stringResource(R.string.builder_duplicate), Lucide.Copy) { expanded = false; onDuplicate() }
            onMoveUp?.let { MenuLine(stringResource(R.string.builder_moveUp), Lucide.ArrowUp) { expanded = false; it() } }
            onMoveDown?.let { MenuLine(stringResource(R.string.builder_moveDown), Lucide.ArrowDown) { expanded = false; it() } }
            onMoveIn?.let { MenuLine(stringResource(R.string.builder_moveIntoRepeat), Lucide.ListIndentIncrease) { expanded = false; it() } }
            onMoveOut?.let { MenuLine(stringResource(R.string.builder_moveOutOfRepeat), Lucide.ListIndentDecrease) { expanded = false; it() } }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_delete), color = colors.error) },
                leadingIcon = { Icon(Lucide.Trash, contentDescription = null, tint = colors.error) },
                onClick = { expanded = false; onRemove() },
            )
        }
    }
}

/** A line of a ⋮ menu, with its icon. */
@Composable
private fun MenuLine(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(text) }, leadingIcon = { Icon(icon, contentDescription = null) }, onClick = onClick)
}

/** A compact select: its label just above the chosen value, and a chevron; a tap lists the options. */
@Composable
private fun PoulpySelect(label: String, value: String, options: List<Pair<String, () -> Unit>>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(PoulpyRadius.md),
            color = colors.surface,
            border = BorderStroke(1.5.dp, colors.outline),
            modifier = Modifier.fillMaxWidth().height(PoulpySize.boxField).semantics { contentDescription = "$label, $value" },
        ) {
            Row(Modifier.padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurfaceVariant, maxLines = 1)
                    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Lucide.ChevronDown, contentDescription = null, tint = colors.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (text, pick) ->
                DropdownMenuItem(text = { Text(text, fontWeight = if (text == value) FontWeight.ExtraBold else FontWeight.Normal) }, onClick = { expanded = false; pick() })
            }
        }
    }
}

@Composable
private fun SettingCell(label: String, modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(PoulpyRadius.md)).padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        content()
    }
}

/** A value between − and +; with [onValue], tapping the value opens a sheet to type it. */
@Composable
private fun StepperCell(label: String, value: String, onLess: () -> Unit, onMore: () -> Unit, modifier: Modifier, onValue: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    val editLabel = stringResource(R.string.builder_editValue, label)
    SettingCell(label, modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundStep("−", stringResource(R.string.builder_lessNamed, label), onLess)
            BasicText(
                value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (onValue != null) colors.primary else colors.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontFeatureSettings = "tnum",
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 22.sp),
                modifier = Modifier
                    .weight(1f)
                    .then(if (onValue != null) Modifier.clip(RoundedCornerShape(PoulpyRadius.sm)).clickable(onClickLabel = editLabel, onClick = onValue) else Modifier)
                    .padding(horizontal = 2.dp, vertical = 10.dp),
            )
            RoundStep("+", stringResource(R.string.builder_moreNamed, label), onMore)
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

/** A label above a number field with − and +: sets, reps, seconds or metres, typed or nudged. */
@Composable
private fun NumberCell(label: String, value: Int, unit: AmountUnit?, onValue: (Int) -> Unit, onLess: () -> Unit, onMore: () -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
        NumberEntry(
            onLess, onMore, Modifier.fillMaxWidth(),
            lessLabel = stringResource(R.string.builder_lessNamed, label),
            moreLabel = stringResource(R.string.builder_moreNamed, label),
        ) { fieldModifier ->
            NumberField(
                value = text,
                onValueChange = { typed ->
                    text = typed
                    typed.toIntOrNull()?.let(onValue)
                },
                description = stringResource(R.string.builder_editValue, label),
                modifier = fieldModifier,
                unit = unit?.let { stringResource(unitLabel(it)) },
            )
        }
    }
}

private fun unitLabel(unit: AmountUnit) = when (unit) {
    AmountUnit.REPS -> R.string.builder_unitReps
    AmountUnit.SECONDS -> R.string.builder_unitSeconds
    AmountUnit.METERS -> R.string.builder_unitMeters
}

/** The weight in the user's unit, typed with the decimal keyboard. */
@Composable
private fun WeightCell(model: BuilderModel, row: BuilderRow) {
    var weight by remember(row.key) { mutableStateOf(row.weight) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.builder_weight).uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
        NumberField(
            value = weight,
            onValueChange = { text ->
                weight = text
                model.setWeight(row.key, text)
            },
            description = stringResource(if (row.weightUnit == "lb") R.string.builder_weightLb else R.string.builder_weightKg),
            modifier = Modifier.fillMaxWidth(),
            unit = row.weightUnit,
            decimal = true,
        )
    }
}

private enum class TargetField { DURATION, DISTANCE, PACE }

/** A walk or a run: one block with a duration (time, distance or until button press) and an intensity, instead of sets. */
@Composable
private fun TargetEditor(model: BuilderModel, row: BuilderRow, target: WalkRunTarget) {
    var editing by rememberSaveable(row.key) { mutableStateOf<TargetField?>(null) }
    val untilPressed = stringResource(if (row.isLast) R.string.builder_untilEnd else R.string.builder_untilPressed)
    val durations = listOf(
        DurationKind.DISTANCE to stringResource(R.string.builder_distance),
        DurationKind.TIME to stringResource(R.string.builder_time),
        DurationKind.UNTIL_PRESSED to untilPressed,
    )
    val intensities = listOf(
        IntensityKind.FREE to stringResource(R.string.builder_free),
        IntensityKind.PACE to stringResource(R.string.builder_pace),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PoulpySelect(
                stringResource(R.string.builder_duration),
                durations.first { it.first == target.duration }.second,
                durations.map { (kind, text) -> text to { model.setDurationKind(row.key, kind) } },
                Modifier.weight(1f),
            )
            PoulpySelect(
                stringResource(R.string.builder_intensity),
                intensities.first { it.first == target.intensity }.second,
                intensities.map { (kind, text) -> text to { model.setIntensityKind(row.key, kind) } },
                Modifier.weight(1f),
            )
        }
        if (target.durationSeconds != null) {
            StepperCell(
                stringResource(R.string.builder_time), target.durationLabel,
                { model.changeDuration(row.key, -1) }, { model.changeDuration(row.key, 1) },
                Modifier.fillMaxWidth(), onValue = { editing = TargetField.DURATION },
            )
        }
        if (target.distanceMeters != null) {
            StepperCell(
                stringResource(R.string.builder_distance), target.distanceLabel,
                { model.changeDistance(row.key, -1) }, { model.changeDistance(row.key, 1) },
                Modifier.fillMaxWidth(), onValue = { editing = TargetField.DISTANCE },
            )
        }
        if (target.speedKmh != null) {
            StepperCell(
                stringResource(R.string.builder_pace), target.paceLabel,
                { model.changePace(row.key, -1) }, { model.changePace(row.key, 1) },
                Modifier.fillMaxWidth(), onValue = { editing = TargetField.PACE },
            )
        }
        Text(
            listOfNotNull(target.hint?.let { "≈ $it" }, stringResource(R.string.builder_tapToType)).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    editing?.let { field -> ValueSheet(model, row.key, target, field, onDone = { editing = null }) }
}

/** Types a duration, distance or pace, with its unit and quick picks such as 10 km, 1 h or 5:00 /km. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ValueSheet(model: BuilderModel, key: String, target: WalkRunTarget, field: TargetField, onDone: () -> Unit) {
    var text by remember(field) {
        mutableStateOf(
            when (field) {
                TargetField.DURATION -> target.durationText
                TargetField.DISTANCE -> target.distanceText
                TargetField.PACE -> target.paceText
            },
        )
    }
    var invalid by remember(field) { mutableStateOf(false) }
    val title = stringResource(
        when (field) {
            TargetField.DURATION -> R.string.builder_duration
            TargetField.DISTANCE -> R.string.builder_distance
            TargetField.PACE -> R.string.builder_pace
        },
    )
    fun apply(value: String) = when (field) {
        TargetField.DURATION -> model.setDuration(key, value)
        TargetField.DISTANCE -> model.setDistance(key, value, target.distanceUnit)
        TargetField.PACE -> model.setPace(key, value, target.paceUnit)
    }
    val picks = when (field) {
        TargetField.DURATION -> model.durationPicks()
        TargetField.DISTANCE -> model.distancePicks(target.distanceUnit)
        TargetField.PACE -> model.pacePicks(key, target.paceUnit)
    }
    val decimal = field == TargetField.DISTANCE || (field == TargetField.PACE && target.paceUnit.isSpeed)
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDone, containerColor = colors.surface) {
        Column(
            Modifier.padding(horizontal = PoulpySpace.lg).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BoxField(
                    text,
                    { text = it; invalid = false },
                    label = title,
                    modifier = Modifier.weight(1f).height(PoulpySize.boxField),
                    keyboard = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Text),
                    center = true,
                )
                if (field != TargetField.DURATION) {
                    Text(
                        if (field == TargetField.DISTANCE) target.distanceUnit.symbol else target.paceUnit.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            if (invalid) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Lucide.CircleExclamationPoint, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.builder_valueInvalid), color = colors.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (field != TargetField.DURATION) {
                FieldLabel(stringResource(R.string.builder_unit))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (field == TargetField.DISTANCE) {
                        DistanceUnit.entries.forEach { unit ->
                            PoulpyChip(selected = target.distanceUnit == unit, onClick = { text = model.setDistanceUnit(key, unit, text); invalid = false }, label = unit.symbol)
                        }
                    } else {
                        PaceUnit.entries.forEach { unit ->
                            PoulpyChip(selected = target.paceUnit == unit, onClick = { text = model.setPaceUnit(key, unit, text); invalid = false }, label = unit.symbol)
                        }
                    }
                }
            }
            FieldLabel(stringResource(R.string.builder_quickPicks))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                picks.forEach { pick ->
                    PoulpyChip(selected = text == pick.text, onClick = { if (apply(pick.text)) onDone() }, label = pick.label)
                }
            }
            PoulpyButton(onClick = { if (apply(text)) onDone() else invalid = true }, size = PoulpyButtonSize.Lg, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}
