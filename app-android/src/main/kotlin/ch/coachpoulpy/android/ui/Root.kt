package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph

private enum class Tab(@StringRes val label: Int, val symbol: String) {
    TODAY(R.string.tab_today, "●"),
    EXERCISES(R.string.tab_exercises, "≡"),
    TREADMILL(R.string.tab_treadmill, "⏵"),
}

@Composable
fun CoachPoulpyRoot(graph: AppGraph) {
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Text(item.symbol) },
                        label = { Text(stringResource(item.label)) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            Tab.TODAY -> TodayScreen(graph, modifier)
            Tab.EXERCISES -> ExercisesScreen(graph, modifier)
            Tab.TREADMILL -> TreadmillScreen(graph, modifier)
        }
    }
}
