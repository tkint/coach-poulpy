package ch.coachpoulpy.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.shared.AppGraph

/** The searchable catalog, used by the library and by the builder's picker. */
@Composable
fun ExerciseList(
    graph: AppGraph,
    language: String,
    type: WorkoutType?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = remember(graph, language) { graph.exerciseList(language) }
    var query by rememberSaveable { mutableStateOf("") }
    var muscle by rememberSaveable { mutableStateOf<MuscleGroup?>(null) }
    val rows = remember(model, query, muscle, type) { model.filter(query, muscle, type) }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.exercises_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(selected = muscle == null, onClick = { muscle = null }, label = { Text(stringResource(R.string.exercises_all)) })
            }
            items(model.muscleFilters, key = { it.group.name }) { filter ->
                FilterChip(
                    selected = muscle == filter.group,
                    onClick = { muscle = if (muscle == filter.group) null else filter.group },
                    label = { Text(filter.label) },
                )
            }
        }
        if (rows.isEmpty()) {
            Text(stringResource(R.string.exercises_none), modifier = Modifier.padding(24.dp))
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(rows, key = { it.slug }) { row ->
                ListItem(
                    leadingContent = { PatternBadge(row.pattern) },
                    headlineContent = { Text(row.name) },
                    supportingContent = { Text(row.details) },
                    modifier = Modifier.clickable { onPick(row.slug) },
                )
                HorizontalDivider()
            }
        }
    }
}
