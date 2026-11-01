package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.ExerciseDetail
import ch.coachpoulpy.shared.VariationRow

/**
 * An exercise's page: a fixed picture area with Back and, in a variation group, the
 * "Variation 3 of 5" button that opens the variations sheet, so it never moves; then
 * the eyebrow, name, Muscles and Equipment cards and the steps, with "Add to a workout"
 * pinned at the bottom. Picking another variation replaces the page ([onOpenExercise]).
 */
@Composable
fun ExerciseDetailScreen(
    graph: AppGraph,
    language: String,
    slug: String,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
    onAddToWorkout: (String) -> Unit,
) {
    val detail = remember(graph, language, slug) { graph.exerciseList(language).detail(slug) } ?: return
    var choosing by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    // A Surface, not a background, so text takes onBackground in the dark theme too.
    Surface(Modifier.fillMaxSize(), color = colors.background) {
        Column(Modifier.fillMaxSize()) {
            PictureArea(detail, onBack, onVariations = { choosing = true })
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(detail.eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                    Text(detail.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FactCard(stringResource(R.string.exercise_muscles), detail.muscles, Modifier.weight(1f))
                    FactCard(stringResource(R.string.exercise_equipment), detail.equipment, Modifier.weight(1f))
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
            HorizontalDivider(color = colors.surfaceVariant)
            Box(Modifier.navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)) {
                Button(onClick = { onAddToWorkout(detail.slug) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.exercise_add), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
    if (choosing) {
        VariationsSheet(
            variations = detail.variations,
            onPick = {
                choosing = false
                onOpenExercise(it)
            },
            onDismiss = { choosing = false },
        )
    }
}

/** The pattern illustration until real pictures exist, with Back and the variations button over it. */
@Composable
private fun PictureArea(detail: ExerciseDetail, onBack: () -> Unit, onVariations: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().background(colors.primaryContainer)) {
        Box(
            Modifier.fillMaxWidth().statusBarsPadding().height(212.dp).padding(top = 36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                detail.pattern.icon(),
                contentDescription = stringResource(R.string.exercise_picture),
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(112.dp),
            )
        }
        Row(
            Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = colors.surface, modifier = Modifier.size(44.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
            if (detail.variations.isNotEmpty()) {
                val label = stringResource(R.string.exercise_variationsA11y, detail.variationPosition)
                Surface(
                    onClick = onVariations,
                    shape = RoundedCornerShape(PoulpyRadius.pill),
                    color = colors.surface,
                    contentColor = colors.onPrimaryContainer,
                    modifier = Modifier.height(44.dp).semantics {
                        contentDescription = label
                        role = Role.Button
                    },
                ) {
                    Row(
                        Modifier.padding(start = 14.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.SignalCellularAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            stringResource(R.string.exercise_variationPosition, detail.variationPosition),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun FactCard(title: String, body: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.surface) {
    Column(
        modifier
            .background(color, RoundedCornerShape(PoulpyRadius.md))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(body, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

/** The group easiest at the top, hardest at the bottom, each with a difficulty ladder and its note. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariationsSheet(variations: List<VariationRow>, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Opened in full, so the whole group is in view without scrolling.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.exercise_variations), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.exercise_variationsHint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                variations.forEachIndexed { rung, variation ->
                    val shape = RoundedCornerShape(PoulpyRadius.md)
                    Surface(
                        onClick = { if (variation.current) onDismiss() else onPick(variation.slug) },
                        shape = shape,
                        color = if (variation.current) colors.primaryContainer else colors.surface,
                        border = if (variation.current) BorderStroke(1.5.dp, colors.primary) else BorderStroke(1.dp, colors.outline),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).semantics { selected = variation.current },
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Ladder(rung, variations.size)
                            Column(Modifier.weight(1f)) {
                                Text(variation.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    if (variation.current) stringResource(R.string.exercise_variationCurrent) else variation.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            if (variation.current) Icon(Icons.Filled.Check, contentDescription = null, tint = colors.primary)
                        }
                    }
                }
            }
        }
    }
}

/** Bars rising left to right, lit up to this rung: how hard this variation is in its group. */
@Composable
private fun Ladder(rung: Int, count: Int) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.width(40.dp).height(24.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        repeat(count) { bar ->
            Box(
                Modifier
                    .width(5.dp)
                    .height((8 + bar * 16 / maxOf(count - 1, 1)).dp)
                    .background(if (bar <= rung) colors.primary else colors.outline, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
    }
}
