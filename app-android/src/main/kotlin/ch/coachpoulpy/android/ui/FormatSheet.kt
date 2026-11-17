package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.shared.FormatChange
import ch.coachpoulpy.shared.FormatSwitch
import ch.coachpoulpy.shared.WorkoutFormatOption

/**
 * The format, Circuit or Sets, each card with what it fits and an example. A tap on a card is the
 * choice: no card is selected when a workout is created ([current] null), and from the builder
 * [current] is the one it has. The ✕ and every other way of closing leave things as they were.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormatSheet(options: List<WorkoutFormatOption>, current: WorkoutFormat?, onChoose: (WorkoutFormat) -> Unit, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(
            Modifier.padding(horizontal = PoulpySpace.lg).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.weight(1f)) { SheetTitle(stringResource(R.string.builder_formatTitle), stringResource(R.string.builder_formatSubtitle)) }
                IconButton(onClick = onClose) { Icon(Lucide.X, contentDescription = stringResource(R.string.action_close)) }
            }
            options.forEach { option ->
                val format = option.format
                val (description, example) = when (format) {
                    WorkoutFormat.CIRCUIT -> R.string.builder_circuitDescription to R.string.builder_circuitExample
                    WorkoutFormat.SETS -> R.string.builder_setsDescription to R.string.builder_setsExample
                }
                val selected = current == format
                Surface(
                    onClick = { onChoose(format) },
                    shape = RoundedCornerShape(PoulpyRadius.md),
                    color = if (selected) colors.primaryContainer else colors.surface,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline),
                    modifier = Modifier.fillMaxWidth().semantics { this.selected = selected },
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (selected) colors.primary else colors.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(format.icon, contentDescription = null, tint = if (selected) colors.onPrimary else colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
                            }
                            Text(option.label, style = PoulpyTextStyles.Headline.copy(fontSize = 19.sp, lineHeight = 23.sp))
                        }
                        Text(stringResource(description), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(example), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * After picking the other format in the builder: "Follow the new format" reorders the exercises
 * the way it does them, "Keep the order" only rewrites the steps. Each says what it gives for this
 * workout. Cancel, or closing the sheet, changes nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormatOrderSheet(change: FormatChange, onChoose: (FormatSwitch) -> Unit, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(
            Modifier.padding(horizontal = PoulpySpace.lg).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetTitle(stringResource(R.string.builder_orderTitle, change.formatLabel), stringResource(R.string.builder_orderSubtitle))
            val follows = if (change.format == WorkoutFormat.CIRCUIT) R.string.builder_followToCircuit else R.string.builder_followToSets
            OrderCard(stringResource(R.string.builder_followTitle), stringResource(follows), change.followLine) { onChoose(FormatSwitch.FOLLOW_NEW_FORMAT) }
            OrderCard(stringResource(R.string.builder_keepTitle), stringResource(R.string.builder_keepDescription), change.keepLine) { onChoose(FormatSwitch.KEEP_ORDER) }
            PoulpyButton(onClick = onClose, kind = PoulpyButtonKind.Secondary, size = PoulpyButtonSize.Lg, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}

@Composable
private fun OrderCard(title: String, description: String, result: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PoulpyRadius.md),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = PoulpyTextStyles.Headline.copy(fontSize = 19.sp, lineHeight = 23.sp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Text(result, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
