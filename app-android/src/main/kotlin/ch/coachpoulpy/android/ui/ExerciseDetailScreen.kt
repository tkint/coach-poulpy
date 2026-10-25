package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.VariationRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    graph: AppGraph,
    language: String,
    slug: String,
    onBack: () -> Unit,
    onOpenExercise: (String) -> Unit,
) {
    val detail = remember(graph, language, slug) { graph.exerciseList(language).detail(slug) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.name ?: "") },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        if (detail == null) return@Scaffold
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PatternBadge(detail.pattern, size = 72.dp)
                Text(detail.patternLabel, style = MaterialTheme.typography.titleMedium)
            }
            Section(stringResource(R.string.exercise_muscles), detail.muscles)
            Section(stringResource(R.string.exercise_equipment), detail.equipment)
            if (detail.perSide) Text(stringResource(R.string.exercise_per_side), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.exercise_how), style = MaterialTheme.typography.titleMedium)
            detail.instructions.forEachIndexed { index, step ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${index + 1}.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Text(step, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (detail.variations.isNotEmpty()) Variations(detail.variations, onOpenExercise)
        }
    }
}

/** The exercise's group, easiest first; this exercise is marked in place and the others open. */
@Composable
private fun Variations(variations: List<VariationRow>, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.exercise_variations), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.exercise_variations_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        variations.forEach { variation ->
            val shape = RoundedCornerShape(16.dp)
            val row = Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape)
            Row(
                modifier = if (variation.current) {
                    row.background(MaterialTheme.colorScheme.primaryContainer)
                } else {
                    row.border(1.5.dp, MaterialTheme.colorScheme.outline, shape).clickable { onOpen(variation.slug) }
                }.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    variation.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (variation.current) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (variation.current) {
                    Text(
                        stringResource(R.string.exercise_variation_current),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
    }
}
