package ch.coachpoulpy.android.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import ch.coachpoulpy.shared.AppGraph

@Composable
fun ExercisesScreen(graph: AppGraph, modifier: Modifier = Modifier) {
    val language = LocalConfiguration.current.locales[0].language
    val rows = remember(graph, language) { graph.exerciseList(language).rows }

    LazyColumn(modifier.fillMaxSize()) {
        items(rows, key = { it.slug }) { row ->
            ListItem(
                headlineContent = { Text(row.name) },
                supportingContent = { Text(row.details) },
            )
            HorizontalDivider()
        }
    }
}
