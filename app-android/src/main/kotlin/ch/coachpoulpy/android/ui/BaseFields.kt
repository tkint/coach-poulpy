package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import ch.coachpoulpy.android.R
import kotlin.math.roundToInt

/*
 * The base fields every form is built from (docs/design-system.md "Base fields"):
 * a text field, a select field that opens a sheet, a number field that selects its
 * text and opens the numeric keyboard, option sheets (one choice, several, or a step
 * slider). Mockups: profile-edit, profile-*-sheet, body-log-sheet.
 */

/** The box shared by the text and number fields: 52 high, 16 radius, a 1.5 outline that turns primary on focus. */
@Composable
private fun FieldBox(focused: Boolean, height: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().height(height),
        shape = RoundedCornerShape(PoulpyRadius.md),
        color = colors.surface,
        border = BorderStroke(1.5.dp, if (focused) colors.primary else colors.outline),
        content = content,
    )
}

/** A labelled free-text field, with an optional note under it. */
@Composable
internal fun PoulpyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    note: String? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
        FieldBox(focused, 52.dp) {
            Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
                )
            }
        }
        note?.let { Note(it) }
    }
}

/**
 * A number typed with the numeric keyboard. A tap selects what is there, so typing replaces it.
 * [value] is the text; the caller parses it (see `BodyDisplay`) and decides what is valid.
 */
@Composable
internal fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    decimal: Boolean = false,
    large: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    var focused by remember { mutableStateOf(false) }
    var field by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (field.text != value) field = TextFieldValue(value, TextRange(value.length))
    }
    val style = (if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyLarge)
        .copy(fontWeight = FontWeight.Bold, color = colors.onSurface, textAlign = if (large) TextAlign.End else TextAlign.Start)
    FieldBox(focused, if (large) 64.dp else 52.dp, modifier) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                value = field,
                onValueChange = { typed ->
                    val clean = typed.text.filter { it.isDigit() || (decimal && (it == '.' || it == ',')) }.take(6)
                    field = if (clean == typed.text) typed else TextFieldValue(clean, TextRange(clean.length))
                    if (clean != value) onValueChange(clean)
                },
                singleLine = true,
                textStyle = style,
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = description }
                    .onFocusChanged {
                        focused = it.isFocused
                        if (it.isFocused) field = field.copy(selection = TextRange(0, field.text.length))
                    },
            )
            unit?.let { Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.onSurfaceVariant) }
        }
    }
}

/** A number field with a round − and + on each side, for small nudges after typing. */
@Composable
internal fun NumberEntry(
    onLess: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    lessLabel: String = stringResource(R.string.body_less),
    moreLabel: String = stringResource(R.string.body_more),
    field: @Composable (Modifier) -> Unit,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton(onClick = onLess, modifier = Modifier.size(48.dp).semantics { contentDescription = lessLabel }) {
            Text("−", style = MaterialTheme.typography.headlineSmall)
        }
        field(Modifier.weight(1f))
        FilledTonalIconButton(onClick = onMore, modifier = Modifier.size(48.dp).semantics { contentDescription = moreLabel }) { Text("+", style = MaterialTheme.typography.headlineSmall) }
    }
}

@Composable
internal fun Note(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
}

/** A labelled value that opens a sheet or dialog when pressed. */
@Composable
internal fun SelectField(label: String, value: String, onClick: () -> Unit) {
    OutlinedSurface(onClick = onClick) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The frame of every option sheet: a title, a line, the choices, then Save. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetFrame(
    title: String,
    why: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    clear: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            why?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            content()
            clear?.let { TextButton(onClick = { it(); onDismiss() }) { Text(stringResource(R.string.profile_clear), fontWeight = FontWeight.ExtraBold) } }
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                ) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
                Button(onClick = { onSave(); onDismiss() }, modifier = Modifier.weight(1.6f).height(56.dp)) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Pick one option in a sheet and save it. A null value is "not set". [extra] is shown under the
 * choices with the current one, for a choice that needs more (the custom pronouns field).
 */
@Composable
internal fun <T : Any> OptionSheet(
    title: String,
    why: String?,
    options: List<Pair<T?, String>>,
    selected: T?,
    note: String?,
    onSave: (T?) -> Unit,
    onDismiss: () -> Unit,
    extra: (@Composable (T?) -> Unit)? = null,
) {
    var choice by remember { mutableStateOf(selected) }
    SheetFrame(title, why, onDismiss, onSave = { onSave(choice) }) {
        Column(Modifier.selectableGroup()) {
            options.forEachIndexed { index, (value, label) ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .selectable(selected = value == choice, role = Role.RadioButton, onClick = { choice = value })
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    RadioButton(selected = value == choice, onClick = null)
                }
            }
        }
        extra?.invoke(choice)
        note?.let { Note(it) }
    }
}

/** Pick any number of options (including none) in a sheet and save them. */
@Composable
internal fun <T : Any> MultiOptionSheet(
    title: String,
    why: String?,
    options: List<Pair<T, String>>,
    selected: Set<T>,
    onSave: (Set<T>) -> Unit,
    onDismiss: () -> Unit,
) {
    var chosen by remember { mutableStateOf(selected) }
    SheetFrame(title, why, onDismiss, onSave = { onSave(chosen) }) {
        Column {
            options.forEachIndexed { index, (value, label) ->
                val on = value in chosen
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .selectable(selected = on, role = Role.Checkbox, onClick = { chosen = if (on) chosen - value else chosen + value })
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Checkbox(checked = on, onCheckedChange = null)
                }
            }
        }
    }
}

/**
 * A short scale of named steps on a slider with a dot per step, the chosen name above it and the two
 * ends named below. Starts on the middle step when nothing was chosen; Clear leaves it unset.
 */
@Composable
internal fun <T : Any> StepSheet(
    title: String,
    why: String?,
    steps: List<Pair<T, String>>,
    selected: T?,
    onSave: (T?) -> Unit,
    onDismiss: () -> Unit,
) {
    var index by remember { mutableIntStateOf(steps.indexOfFirst { it.first == selected }.takeIf { it >= 0 } ?: steps.size / 2) }
    SheetFrame(title, why, onDismiss, onSave = { onSave(steps[index].first) }, clear = { onSave(null) }) {
        Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(steps[index].second, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Slider(
                value = index.toFloat(),
                onValueChange = { index = it.roundToInt() },
                valueRange = 0f..(steps.size - 1).toFloat(),
                steps = steps.size - 2,
                modifier = Modifier.semantics { contentDescription = title },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(steps.first().second, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(steps.last().second, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
