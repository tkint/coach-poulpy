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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BodyView
import ch.coachpoulpy.shared.ExerciseDetail
import ch.coachpoulpy.shared.VariationRow

/** The two halves of an exercise's page. */
internal enum class ExerciseTab { MUSCLES, HOW }

/**
 * An exercise's page: a slim bar with Back and, in a variation group, the "Variation 3 of 5" button
 * that opens the variations sheet; then the eyebrow, name and equipment, and two tabs: Muscles (the
 * front and back figures, open by default) and How to do it. "Add to a workout" is pinned at the
 * bottom. Picking another variation replaces the page ([onOpenExercise]).
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
    var tab by rememberSaveable(slug) { mutableStateOf(ExerciseTab.MUSCLES) }
    val colors = MaterialTheme.colorScheme

    // A Surface, not a background, so text takes onBackground in the dark theme too.
    Surface(Modifier.fillMaxSize(), color = colors.background) {
        Column(Modifier.fillMaxSize()) {
            TopBar(detail, onBack, onVariations = { choosing = true })
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = PoulpySpace.lg, end = PoulpySpace.lg, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ExerciseHeading(detail)
                PillSwitch(listOf(ExerciseTab.MUSCLES to R.string.exercise_muscles, ExerciseTab.HOW to R.string.exercise_how), tab, onSelect = { tab = it })
                ExerciseTabContent(graph, language, detail, tab)
            }
            HorizontalDivider(color = colors.surfaceVariant)
            Box(Modifier.navigationBarsPadding().padding(start = PoulpySpace.lg, end = PoulpySpace.lg, top = 12.dp, bottom = 16.dp)) {
                Button(onClick = { onAddToWorkout(detail.slug) }, modifier = Modifier.fillMaxWidth().height(PoulpySize.buttonLarge)) {
                    Icon(Lucide.Plus, contentDescription = null)
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

/** Back, and the variations button when the exercise is in a variation group. */
@Composable
private fun TopBar(detail: ExerciseDetail, onBack: () -> Unit, onVariations: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = colors.surface, modifier = Modifier.size(44.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.action_back))
                }
            }
            if (detail.variations.isNotEmpty()) {
                VariationButton(detail, onVariations)
            }
        }
    }
}

/** The eyebrow, the name and the equipment chip. */
@Composable
internal fun ExerciseHeading(detail: ExerciseDetail) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(detail.eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.primary)
        Text(detail.name, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(
            "${stringResource(R.string.exercise_equipment).uppercase()} · ${detail.equipment}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.background(colors.surfaceVariant, RoundedCornerShape(PoulpyRadius.pill)).padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** One tab's content: the figures with the muscles named, or the steps. Placed in the caller's column. */
@Composable
internal fun ExerciseTabContent(graph: AppGraph, language: String, detail: ExerciseDetail, tab: ExerciseTab) {
    val colors = MaterialTheme.colorScheme
    when (tab) {
        ExerciseTab.MUSCLES -> {
            WorkedMuscles(graph, language, detail)
            Text(
                detail.muscles,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ExerciseTab.HOW -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

/** "Variation 3 of 5" with a drop arrow, which opens the group. */
@Composable
internal fun VariationButton(detail: ExerciseDetail, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(R.string.exercise_variationsA11y, detail.variationPosition)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = colors.surfaceVariant,
        contentColor = colors.onPrimaryContainer,
        modifier = Modifier.height(PoulpySize.button).semantics {
            contentDescription = label
            role = Role.Button
        },
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Lucide.Signal, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                stringResource(R.string.exercise_variationPosition, detail.variationPosition),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Icon(Lucide.ChevronDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

/** Front and back figures, on the person's own figure, with the muscles the exercise works lit. */
@Composable
internal fun WorkedMuscles(graph: AppGraph, language: String, detail: ExerciseDetail) {
    val context = LocalContext.current
    val figure = remember(graph, context) { graph.profile(AppPreferences(context)).profile.value.bodyFigure ?: BodyFigure.SHOULDERS }
    // The muscles are named in text beside it, so the picture is hidden from TalkBack.
    Row(
        Modifier.fillMaxWidth().clearAndSetSemantics { },
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        listOf(BodyView.FRONT to R.string.profile_figureFront, BodyView.BACK to R.string.profile_figureBack).forEach { (view, label) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BodyMapFigure(figure, view, detail.workedAreas, language, width = 136.dp)
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Modifier.verticalScroll(rememberScrollState()).padding(start = PoulpySpace.lg, end = PoulpySpace.lg, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.exercise_variations), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.exercise_variationsHint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            VariationRungs(variations, onPick, onCurrent = onDismiss)
        }
    }
}

/** The rungs of a variation group; the current one is checked and [onCurrent] runs when it is tapped. */
@Composable
internal fun VariationRungs(variations: List<VariationRow>, onPick: (String) -> Unit, onCurrent: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        variations.forEachIndexed { rung, variation ->
            Surface(
                onClick = { if (variation.current) onCurrent() else onPick(variation.slug) },
                shape = RoundedCornerShape(PoulpyRadius.md),
                color = if (variation.current) colors.primaryContainer else colors.surface,
                border = if (variation.current) BorderStroke(1.5.dp, colors.primary) else BorderStroke(1.dp, colors.outline),
                modifier = Modifier.fillMaxWidth().heightIn(min = PoulpySize.row).semantics { selected = variation.current },
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
                    if (variation.current) Icon(Lucide.Check, contentDescription = null, tint = colors.primary)
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
        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.action_back))
    }
}
