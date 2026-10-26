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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BuilderModel
import ch.coachpoulpy.shared.BuilderRow
import ch.coachpoulpy.shared.DistanceUnit
import ch.coachpoulpy.shared.PaceUnit
import ch.coachpoulpy.shared.BuilderRepeat
import ch.coachpoulpy.shared.DurationKind
import ch.coachpoulpy.shared.IntensityKind
import ch.coachpoulpy.shared.WalkRunTarget

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
    // Where the exercise being picked goes (TOP_LEVEL or a repeat's key) and as what; null when not picking.
    var pickingFor by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingRole by rememberSaveable { mutableStateOf(StepRole.WORK) }
    // The step being edited in the sheet: the one just added, or the one tapped.
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.addedKey) { state.addedKey?.let { editing = it } }
    val colors = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    // A step moves among the steps of the list that holds it: the workout's, or its repeat's.
    fun siblings(key: String): List<String>? =
        state.steps.map { it.key }.takeIf { key in it }
            ?: state.steps.firstNotNullOfOrNull { step -> step.repeatGroup?.rows?.map { it.key }?.takeIf { key in it } }
    val drag = rememberDragReorder(
        list,
        // The name field, the kinds and the add tiles have keys that are not step keys.
        canDropOn = { key, onto -> onto is String && siblings(key as String)?.contains(onto) == true },
        onMove = { key, toKey ->
            val keys = siblings(key as String).orEmpty()
            val to = if (toKey is String) keys.indexOf(toKey) else -1
            val moved = to >= 0 && keys.indexOf(key) != to
            if (moved) model.move(key, to)
            moved
        },
    )

    pickingFor?.let { target ->
        BackHandler { pickingFor = null }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.builder_pick_title)) },
                    navigationIcon = { BackButton { pickingFor = null } },
                )
            },
        ) { padding ->
            ExerciseList(
                graph, language, type = state.type,
                onPick = { model.add(it, target.takeUnless { key -> key == TOP_LEVEL }, pickingRole); pickingFor = null },
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
        // No spacing from the list itself: a repeat is drawn as one tinted block out of several items.
        LazyColumn(
            state = list,
            modifier = Modifier.padding(padding).fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        ) {
            item {
                Column(Modifier.padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel(stringResource(R.string.builder_name))
                    BoxField(state.name, model::rename, label = stringResource(R.string.builder_name), modifier = Modifier.fillMaxWidth().height(52.dp))
                }
            }
            item {
                Column(Modifier.padding(bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(stringResource(R.string.builder_type))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        model.typeOptions.forEach { option ->
                            PoulpyChip(selected = state.type == option.type, onClick = { model.setType(option.type) }, label = option.label)
                        }
                    }
                }
            }
            if (state.steps.isEmpty()) {
                item { Text(stringResource(R.string.builder_empty), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 14.dp)) }
            }
            state.steps.forEach { step ->
                val row = step.row
                val repeat = step.repeatGroup
                if (row != null) {
                    item(key = row.key) {
                        StepCard(
                            model, row,
                            onEdit = { editing = row.key },
                            handle = Modifier.dragHandle(drag, row.key),
                            modifier = Modifier.draggedItem(this, drag, row.key).padding(bottom = 12.dp),
                        )
                    }
                } else if (repeat != null) {
                    item(key = repeat.key) {
                        RepeatHeader(model, repeat, handle = Modifier.dragHandle(drag, repeat.key), modifier = Modifier.draggedItem(this, drag, repeat.key))
                    }
                    items(repeat.rows, key = { it.key }) { child ->
                        Box(Modifier.draggedItem(this, drag, child.key).background(colors.primaryContainer).padding(start = 22.dp, end = 8.dp, bottom = 6.dp)) {
                            StepCard(
                                model, child,
                                onEdit = { editing = child.key },
                                handle = Modifier.dragHandle(drag, child.key),
                                modifier = Modifier,
                            )
                        }
                    }
                    item(key = "add-${repeat.key}") {
                        Box(
                            Modifier
                                .padding(bottom = 12.dp)
                                .background(colors.primaryContainer, RoundedCornerShape(bottomStart = PoulpyRadius.md, bottomEnd = PoulpyRadius.md))
                                .padding(start = 22.dp, end = 8.dp, bottom = 10.dp, top = 2.dp),
                        ) {
                            AddStepButton(
                                label = stringResource(R.string.builder_add_to_repeat),
                                onExercise = { role -> pickingRole = role; pickingFor = repeat.key },
                                onRest = { model.addRest(repeat.key) },
                                height = 44,
                            )
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AddStepButton(
                        label = stringResource(R.string.builder_add_step),
                        onExercise = { role -> pickingRole = role; pickingFor = TOP_LEVEL },
                        onRest = { model.addRest() },
                        modifier = Modifier.weight(1f),
                    )
                    DashedTile(stringResource(R.string.builder_add_repeat), Icons.Filled.Repeat, onClick = model::addRepeat, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    state.rows.firstOrNull { it.key == editing }?.let { row ->
        StepSheet(model, row, onDone = { editing = null })
    }
}

/** What the exercise picker adds to when it was opened from the end of the list. */
private const val TOP_LEVEL = "top-level"

/** A repeat's header: its handle, "Repeat", how many times as − N times +, and a menu to duplicate or remove it. */
@Composable
private fun RepeatHeader(model: BuilderModel, repeat: BuilderRepeat, handle: Modifier, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val moveUp = stringResource(R.string.builder_move_up)
    val moveDown = stringResource(R.string.builder_move_down)
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.primaryContainer, RoundedCornerShape(topStart = PoulpyRadius.md, topEnd = PoulpyRadius.md))
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(moveUp) { model.moveUp(repeat.key); true },
                    CustomAccessibilityAction(moveDown) { model.moveDown(repeat.key); true },
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.DragIndicator, contentDescription = null, tint = PoulpyPalette.Neutral500, modifier = handle.size(28.dp, 44.dp).padding(horizontal = 4.dp, vertical = 12.dp))
        Icon(Icons.Filled.Repeat, contentDescription = null, tint = colors.primary)
        Text(stringResource(R.string.builder_repeat), style = PoulpyTextStyles.Headline.copy(fontSize = 19.sp, lineHeight = 23.sp), color = colors.onPrimaryContainer, modifier = Modifier.weight(1f))
        Row(
            Modifier.background(colors.surface, CircleShape).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundStep("−", stringResource(R.string.builder_repeat_fewer), { model.setRepeatTimes(repeat.key, repeat.times - 1) })
            Text(repeat.timesLabel, fontWeight = FontWeight.ExtraBold, color = colors.onPrimaryContainer, textAlign = TextAlign.Center, modifier = Modifier.width(64.dp))
            RoundStep("+", stringResource(R.string.builder_repeat_more), { model.setRepeatTimes(repeat.key, repeat.times + 1) })
        }
        ActionsMenu(
            description = stringResource(R.string.builder_repeat_actions),
            onDuplicate = { model.duplicate(repeat.key) },
            onRemove = { model.remove(repeat.key) },
        )
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

/** A step of the workout, read-only: its role, name and summary. A tap edits it in a sheet; the menu has the rest. */
@Composable
private fun StepCard(model: BuilderModel, row: BuilderRow, onEdit: () -> Unit, handle: Modifier, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val moveUp = stringResource(R.string.builder_move_up)
    val moveDown = stringResource(R.string.builder_move_down)
    val moveIn = stringResource(R.string.builder_move_into_repeat)
    val moveOut = stringResource(R.string.builder_move_out_of_repeat)
    Surface(
        shape = RoundedCornerShape(PoulpyRadius.md),
        color = colors.surface,
        border = BorderStroke(1.dp, if (row.fitsType) colors.outline else colors.error),
        // Screen readers can't drag: they get the moves as actions on the card.
        modifier = modifier.fillMaxWidth().semantics {
            customActions = listOfNotNull(
                CustomAccessibilityAction(moveUp) { model.moveUp(row.key); true },
                CustomAccessibilityAction(moveDown) { model.moveDown(row.key); true },
                if (row.canMoveIntoRepeat) CustomAccessibilityAction(moveIn) { model.moveIntoRepeatAbove(row.key); true } else null,
                if (row.repeatKey != null) CustomAccessibilityAction(moveOut) { model.moveOutOfRepeat(row.key); true } else null,
            )
        },
    ) {
        Column(Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Filled.DragIndicator,
                    contentDescription = null,
                    tint = PoulpyPalette.Neutral500,
                    modifier = handle.size(28.dp, 44.dp).padding(horizontal = 4.dp, vertical = 12.dp),
                )
                Column(
                    Modifier.weight(1f).clickable(onClickLabel = stringResource(R.string.builder_open, row.name), onClick = onEdit),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    row.roleLabel?.let { RoleTag(it, row.role) }
                    Text(row.name, style = PoulpyTextStyles.Headline.copy(fontSize = 19.sp, lineHeight = 23.sp))
                    Text(row.summary, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                ActionsMenu(
                    description = stringResource(R.string.builder_more_actions, row.name),
                    onEdit = onEdit,
                    onDuplicate = { model.duplicate(row.key) },
                    onMoveIn = if (row.canMoveIntoRepeat) ({ model.moveIntoRepeatAbove(row.key) }) else null,
                    onMoveOut = if (row.repeatKey != null) ({ model.moveOutOfRepeat(row.key) }) else null,
                    onRemove = { model.remove(row.key) },
                )
            }
            if (!row.fitsType) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.builder_does_not_fit), color = colors.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * Edits one step: what it is for, then sets, reps and weight for an exercise, a duration and
 * an intensity for a walk or a run, or the length of a rest.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepSheet(model: BuilderModel, row: BuilderRow, onDone: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDone, containerColor = colors.surface) {
        Column(
            Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                row.roleLabel?.let { RoleTag(it, row.role) }
                Text(row.name, style = MaterialTheme.typography.titleLarge)
            }
            if (!row.fitsType) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.builder_does_not_fit), color = colors.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            val target = row.target
            if (row.isRest) {
                StepperCell(stringResource(R.string.builder_duration), row.restLabel, { model.changeRest(row.key, -1) }, { model.changeRest(row.key, 1) }, Modifier.fillMaxWidth())
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
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.action_done), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** The small tag above a step's name: "WARM-UP", "RECOVER", "COOL DOWN". */
@Composable
private fun RoleTag(label: String, role: StepRole) {
    val recover = role == StepRole.RECOVER
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = if (recover) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .background(if (recover) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** The ⋮ menu of a step or a repeat: edit, duplicate, the moves into or out of a repeat when they apply, and delete. */
@Composable
private fun ActionsMenu(
    description: String,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onMoveIn: (() -> Unit)? = null,
    onMoveOut: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = description, tint = colors.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            onEdit?.let { edit ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.builder_edit)) },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = { expanded = false; edit() },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_duplicate)) },
                leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                onClick = { expanded = false; onDuplicate() },
            )
            onMoveIn?.let { move ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.builder_move_into_repeat)) },
                    leadingIcon = { Icon(Icons.Filled.Repeat, contentDescription = null) },
                    onClick = { expanded = false; move() },
                )
            }
            onMoveOut?.let { move ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.builder_move_out_of_repeat)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    onClick = { expanded = false; move() },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_delete), color = colors.error) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = colors.error) },
                onClick = { expanded = false; onRemove() },
            )
        }
    }
}

/** A compact select: its label just above the chosen value, and a chevron; a tap lists the options. */
@Composable
private fun PoulpySelect(label: String, value: String, options: List<Pair<String, () -> Unit>>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = colors.surface,
            border = BorderStroke(1.5.dp, colors.outline),
            modifier = Modifier.fillMaxWidth().height(52.dp).semantics { contentDescription = "$label, $value" },
        ) {
            Row(Modifier.padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurfaceVariant, maxLines = 1)
                    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = colors.onSurfaceVariant)
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
        modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).padding(6.dp),
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
    val editLabel = stringResource(R.string.builder_edit_value, label)
    SettingCell(label, modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundStep("−", stringResource(R.string.builder_less_named, label), onLess)
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
                    .then(if (onValue != null) Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClickLabel = editLabel, onClick = onValue) else Modifier)
                    .padding(horizontal = 2.dp, vertical = 10.dp),
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

private enum class TargetField { DURATION, DISTANCE, PACE }

/** A walk or a run: one block with a duration (time, distance or until button press) and an intensity, instead of sets. */
@Composable
private fun TargetEditor(model: BuilderModel, row: BuilderRow, target: WalkRunTarget) {
    var editing by rememberSaveable(row.key) { mutableStateOf<TargetField?>(null) }
    val untilPressed = stringResource(if (row.isLast) R.string.builder_until_end else R.string.builder_until_pressed)
    val durations = listOf(
        DurationKind.TIME to stringResource(R.string.builder_time),
        DurationKind.DISTANCE to stringResource(R.string.builder_distance),
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
        StepperCell(stringResource(R.string.builder_rest), row.restLabel, { model.changeRest(row.key, -1) }, { model.changeRest(row.key, 1) }, Modifier.fillMaxWidth())
        Text(
            listOfNotNull(target.hint?.let { "≈ $it" }, stringResource(R.string.builder_tap_to_type)).joinToString(" · "),
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
            Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BoxField(
                    text,
                    { text = it; invalid = false },
                    label = title,
                    modifier = Modifier.weight(1f).height(56.dp),
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
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.builder_value_invalid), color = colors.error, style = MaterialTheme.typography.bodySmall)
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
            FieldLabel(stringResource(R.string.builder_quick_picks))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                picks.forEach { pick ->
                    PoulpyChip(selected = text == pick.text, onClick = { if (apply(pick.text)) onDone() }, label = pick.label)
                }
            }
            Button(onClick = { if (apply(text)) onDone() else invalid = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.action_done), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** A dashed tile, like "Add a repeat". */
@Composable
private fun DashedTile(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, height: Int = 56) {
    val colors = MaterialTheme.colorScheme
    val dash = PoulpyPalette.Lagoon300
    Row(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    cornerRadius = CornerRadius(if (height < 56) 12.dp.toPx() else 16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary)
        Text(label, color = colors.primary, fontWeight = FontWeight.ExtraBold, style = if (height < 56) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge)
    }
}

/**
 * "Add a step": a tap asks what the step is for. A warm-up, an exercise, a recovery or a cool
 * down is picked from the exercise list; a rest is added right away.
 */
@Composable
private fun AddStepButton(label: String, onExercise: (StepRole) -> Unit, onRest: () -> Unit, modifier: Modifier = Modifier, height: Int = 56) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        DashedTile(label, Icons.Filled.Add, onClick = { expanded = true }, height = height)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(
                Triple(StepRole.WARM_UP, R.string.builder_add_warm_up, Icons.Filled.WbSunny),
                Triple(StepRole.WORK, R.string.builder_add_exercise, Icons.Filled.FitnessCenter),
                Triple(StepRole.RECOVER, R.string.builder_add_recover, Icons.AutoMirrored.Filled.DirectionsWalk),
            ).forEach { (role, text, icon) ->
                DropdownMenuItem(
                    text = { Text(stringResource(text)) },
                    leadingIcon = { Icon(icon, contentDescription = null) },
                    onClick = { expanded = false; onExercise(role) },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_add_rest)) },
                leadingIcon = { Icon(Icons.Filled.Timer, contentDescription = null) },
                onClick = { expanded = false; onRest() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.builder_add_cool_down)) },
                leadingIcon = { Icon(Icons.Filled.Bedtime, contentDescription = null) },
                onClick = { expanded = false; onExercise(StepRole.COOL_DOWN) },
            )
        }
    }
}

private fun formatKg(kg: Double): String = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()
