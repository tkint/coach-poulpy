package ch.coachpoulpy.android.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph

private enum class Tab(@StringRes val label: Int, val icon: ImageVector) {
    TODAY(R.string.tab_today, Icons.Filled.Today),
    EXERCISES(R.string.tab_exercises, Icons.AutoMirrored.Filled.List),
    TREADMILL(R.string.tab_treadmill, Icons.AutoMirrored.Filled.DirectionsRun),
}

/**
 * The screen shown over the tabs, as a string so it survives rotation:
 * "builder:" (new), "builder:<id>", "player:<id>", "resume" or "exercise:<slug>".
 */
private typealias Route = String

@Composable
fun CoachPoulpyRoot(graph: AppGraph) {
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    var route by rememberSaveable { mutableStateOf<Route?>(null) }
    val language = LocalConfiguration.current.locales[0].language
    val close = { route = null }

    BackHandler(enabled = route != null) { close() }

    when (val current = route) {
        null -> Scaffold(
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label)) },
                        )
                    }
                }
            },
        ) { padding ->
            val modifier = Modifier.padding(padding)
            when (tab) {
                Tab.TODAY -> TodayScreen(
                    graph = graph,
                    language = language,
                    onNew = { route = "builder:" },
                    onEdit = { route = "builder:$it" },
                    onStart = { route = "player:$it" },
                    onResume = { route = "resume" },
                    modifier = modifier,
                )
                Tab.EXERCISES -> ExercisesScreen(graph, language, onOpen = { route = "exercise:$it" }, modifier = modifier)
                Tab.TREADMILL -> TreadmillScreen(graph, modifier)
            }
        }
        "resume" -> PlayerScreen(graph, language, workoutId = null, onClose = close)
        else -> {
            val (kind, arg) = current.split(':', limit = 2)
            when (kind) {
                "builder" -> BuilderScreen(graph, language, workoutId = arg.ifEmpty { null }, onClose = close)
                "player" -> PlayerScreen(graph, language, workoutId = arg, onClose = close)
                "exercise" -> ExerciseDetailScreen(graph, language, slug = arg, onBack = close)
                else -> LaunchedEffect(current) { close() }
            }
        }
    }
}
