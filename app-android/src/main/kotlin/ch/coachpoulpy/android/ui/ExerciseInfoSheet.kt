package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph

/**
 * What the builder shows of an exercise: its muscles and equipment, the body map with the muscles it works lit on
 * the person's figure, the steps, and one button ([onAction]). From the picker the button is "Add to the workout"
 * ([adding]), which goes on to the add sheet; from a step's sheet it is "Back to the step" ([closeLabel]). From the workout page
 * ([closeLabel] null) there is no button: the sheet opens partly over a scrim, closes with the cross at its top, and
 * drags up for the rest (`workout-page-exercise-details` mockups). The others open in full so the figure and the steps are in view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInfoSheet(
    graph: AppGraph,
    language: String,
    slug: String,
    onAction: (String) -> Unit,
    onDismiss: () -> Unit,
    adding: Boolean = true,
    @StringRes closeLabel: Int? = R.string.exercise_backToStep,
) {
    // From the picker the sheet can move along the exercise's variations; [current] is the one shown and added.
    var current by rememberSaveable(slug) { mutableStateOf(slug) }
    var listing by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable(current) { mutableStateOf(ExerciseTab.MUSCLES) }
    val detail = remember(graph, language, current) { graph.exerciseList(language).detail(current) } ?: return
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = adding || closeLabel != null),
        containerColor = colors.surface,
    ) {
        // Fills the sheet, so the steps scroll inside a fixed height and the sheet itself never moves with them.
        Column(Modifier.fillMaxWidth().fillMaxHeight()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = PoulpySpace.lg),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (adding && listing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { listing = false }) {
                            Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.action_back))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.exercise_variations), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                            Text(stringResource(R.string.exercise_variationsHint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Lucide.X, contentDescription = stringResource(R.string.action_close))
                        }
                    }
                    VariationRungs(detail.variations, onPick = { current = it; listing = false }, onCurrent = { listing = false })
                } else if (adding) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (detail.variations.isNotEmpty()) VariationButton(detail) { listing = true } else Box {}
                        IconButton(onClick = onDismiss) {
                            Icon(Lucide.X, contentDescription = stringResource(R.string.action_close))
                        }
                    }
                    ExerciseHeading(detail)
                    PillSwitch(listOf(ExerciseTab.MUSCLES to R.string.exercise_muscles, ExerciseTab.HOW to R.string.exercise_how), tab, onSelect = { tab = it })
                    ExerciseTabContent(graph, language, detail, tab)
                } else {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(detail.eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                            Text(detail.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Lucide.X, contentDescription = stringResource(R.string.action_close))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FactCard(stringResource(R.string.exercise_muscles), detail.muscles, Modifier.weight(1f), colors.surfaceVariant)
                        FactCard(stringResource(R.string.exercise_equipment), detail.equipment, Modifier.weight(1f), colors.surfaceVariant)
                    }
                    WorkedMuscles(graph, language, detail)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.exercise_how), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                        detail.instructions.forEachIndexed { index, step ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(28.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = colors.onPrimary)
                                }
                                Text(step, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            if (!(adding && listing) && (adding || closeLabel != null)) {
                HorizontalDivider(Modifier.padding(top = 12.dp), color = colors.surfaceVariant)
                Box(Modifier.navigationBarsPadding().padding(start = PoulpySpace.lg, end = PoulpySpace.lg, top = 12.dp, bottom = 16.dp)) {
                    Button(onClick = { onAction(detail.slug) }, modifier = Modifier.fillMaxWidth().height(PoulpySize.buttonLarge)) {
                        if (adding) {
                            Icon(Lucide.Plus, contentDescription = null)
                        }
                        Text(
                            stringResource(if (adding || closeLabel == null) R.string.exercise_addHere else closeLabel),
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = if (adding) 8.dp else 0.dp),
                        )
                    }
                }
            }
        }
    }
}
