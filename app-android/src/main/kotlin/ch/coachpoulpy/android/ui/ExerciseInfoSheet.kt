package ch.coachpoulpy.android.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BodyView

/**
 * What the builder shows of an exercise: its muscles and equipment, the body map with the muscles it works lit on
 * the person's figure, the steps, and one button ([onAction]). From the picker the button is "Add to the workout"
 * ([adding]), which goes on to the add sheet; from a step's sheet it is "Back to the step". Opened in full so the
 * figure and the steps are in view.
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
) {
    val detail = remember(graph, language, slug) { graph.exerciseList(language).detail(slug) } ?: return
    val context = LocalContext.current
    val figure = remember(graph, context) { graph.profile(AppPreferences(context)).profile.value.bodyFigure ?: BodyFigure.SHOULDERS }
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        // Fills the sheet, so the steps scroll inside a fixed height and the sheet itself never moves with them.
        Column(Modifier.fillMaxWidth().fillMaxHeight()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(detail.eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                        Text(detail.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FactCard(stringResource(R.string.exercise_muscles), detail.muscles, Modifier.weight(1f), colors.surfaceVariant)
                    FactCard(stringResource(R.string.exercise_equipment), detail.equipment, Modifier.weight(1f), colors.surfaceVariant)
                }
                // The text above already names the muscles, so the picture is hidden from TalkBack.
                Row(
                    Modifier.fillMaxWidth().clearAndSetSemantics { },
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf(BodyView.FRONT to R.string.profile_figureFront, BodyView.BACK to R.string.profile_figureBack).forEach { (view, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BodyMapFigure(figure, view, detail.workedAreas, language)
                            Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                        }
                    }
                }
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
            HorizontalDivider(Modifier.padding(top = 12.dp), color = colors.surfaceVariant)
            Box(Modifier.navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)) {
                Button(onClick = { onAction(detail.slug) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    if (adding) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                    Text(
                        stringResource(if (adding) R.string.exercise_addHere else R.string.exercise_backToStep),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = if (adding) 8.dp else 0.dp),
                    )
                }
            }
        }
    }
}
