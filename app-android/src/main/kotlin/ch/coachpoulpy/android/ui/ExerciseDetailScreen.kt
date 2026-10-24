package ch.coachpoulpy.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(graph: AppGraph, language: String, slug: String, onBack: () -> Unit) {
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
