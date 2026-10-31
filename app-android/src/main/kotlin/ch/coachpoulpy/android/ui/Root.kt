package ch.coachpoulpy.android.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph

private enum class Tab(@StringRes val label: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Icons.Filled.Home),
    LIBRARY(R.string.tab_library, Icons.AutoMirrored.Filled.LibraryBooks),
    LIVE(R.string.tab_live, Icons.Filled.Speed),
    HISTORY(R.string.tab_history, Icons.Filled.History),
    SETTINGS(R.string.tab_settings, Icons.Filled.Settings),
}

/**
 * The screen shown over the tabs, as a string so it survives rotation:
 * "import" (a workout file opened from another app), "builder:" (new), "builder:<id>", "builder-add:<slug>" (new, holding that exercise), "builder-copy:<id>" (an unsaved copy), "player:<id>", "resume", "workout:<id>", "exercise:<slug>", "session:<id>",
 * "device-add", "device-sources" or "device:<key>".
 */
private typealias Route = String

/** [openLive] shows the Live tab, as when the recording notification is tapped; [onLiveOpened] says it was done. */
@Composable
fun CoachPoulpyRoot(
    graph: AppGraph,
    openLive: Boolean = false,
    onLiveOpened: () -> Unit = {},
    /** The text of a workout file opened from another app, until the preview shows; [onImportOpened] says it was. */
    importFile: String? = null,
    onImportOpened: () -> Unit = {},
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var libraryView by rememberSaveable { mutableStateOf(LibraryView.WORKOUTS) }
    var route by rememberSaveable { mutableStateOf<Route?>(null) }
    var settingsSection by rememberSaveable { mutableStateOf(SettingsSection.GENERAL) }
    val language = LocalConfiguration.current.locales[0].language
    // Where closing goes back to: the workout page for its editor and exercises, else the tabs.
    var back by rememberSaveable { mutableStateOf<Route?>(null) }
    val close = {
        route = back
        back = null
    }

    BackHandler(enabled = route != null) { close() }

    // The file lives in memory only: after a rotation the preview is gone and the tabs show again.
    var opened by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(importFile) {
        if (importFile != null) {
            opened = importFile
            back = null
            route = "import"
            onImportOpened()
        }
    }

    LaunchedEffect(openLive) {
        if (openLive) {
            route = null
            back = null
            tab = Tab.LIVE
            onLiveOpened()
        }
    }

    when (val current = route) {
        null -> Scaffold(
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { FitText(stringResource(item.label)) },
                        )
                    }
                }
            },
        ) { padding ->
            val modifier = Modifier.padding(padding)
            when (tab) {
                Tab.HOME -> HomeScreen(
                    graph = graph,
                    language = language,
                    onOpenWorkout = { route = "workout:$it" },
                    onStart = { route = "player:$it" },
                    onResume = { route = "resume" },
                    onOpenLive = { tab = Tab.LIVE },
                    onOpenSession = { route = "session:$it" },
                    onOpenLibrary = {
                        libraryView = LibraryView.WORKOUTS
                        tab = Tab.LIBRARY
                    },
                    modifier = modifier,
                )
                Tab.LIBRARY -> LibraryScreen(
                    graph = graph,
                    language = language,
                    view = libraryView,
                    onView = { libraryView = it },
                    onOpenWorkout = { route = "workout:$it" },
                    onStart = { route = "player:$it" },
                    onNew = { route = "builder:" },
                    onCopy = { route = "builder-copy:$it" },
                    onOpenExercise = { route = "exercise:$it" },
                    modifier = modifier,
                )
                Tab.LIVE -> LiveScreen(
                    graph,
                    language,
                    onManageDevices = {
                        settingsSection = SettingsSection.DEVICES
                        tab = Tab.SETTINGS
                    },
                    modifier = modifier,
                )
                Tab.HISTORY -> HistoryScreen(graph, language, onOpen = { route = "session:$it" }, modifier = modifier)
                Tab.SETTINGS -> SettingsScreen(
                    graph,
                    section = settingsSection,
                    onSection = { settingsSection = it },
                    onOpen = { device ->
                        route = when (device) {
                            DeviceRoute.Add -> "device-add"
                            DeviceRoute.Sources -> "device-sources"
                            is DeviceRoute.Device -> "device:${device.key}"
                        }
                    },
                    modifier = modifier,
                )
            }
        }
        "import" -> {
            val text = opened
            if (text == null) {
                LaunchedEffect(Unit) { close() }
            } else {
                ImportScreen(
                    graph, language, fileText = text,
                    onAdded = {
                        opened = null
                        route = "workout:$it"
                    },
                    onClose = { opened = null; close() },
                )
            }
        }
        "resume" -> PlayerScreen(graph, language, workoutId = null, onClose = close)
        "device-add" -> DeviceAddScreen(graph, onBack = close)
        "device-sources" -> DataSourcesScreen(graph, onBack = close)
        else -> {
            val (kind, arg) = current.split(':', limit = 2)
            when (kind) {
                "builder" -> BuilderScreen(graph, language, workoutId = arg.ifEmpty { null }, onClose = close)
                "builder-add" -> BuilderScreen(graph, language, workoutId = null, firstExercise = arg, onClose = close)
                "builder-copy" -> BuilderScreen(graph, language, workoutId = null, copyOf = arg, onClose = close)
                "player" -> PlayerScreen(graph, language, workoutId = arg, onClose = close)
                "workout" -> WorkoutPageScreen(
                    graph, language, workoutId = arg,
                    onBack = close,
                    onStart = { route = "player:$arg" },
                    onEdit = {
                        back = current
                        route = "builder:$arg"
                    },
                    onCopy = {
                        back = current
                        route = "builder-copy:$arg"
                    },
                    onOpenExercise = {
                        back = current
                        route = "exercise:$it"
                    },
                )
                // A variation replaces the page in place, so back still leads where this page came from.
                "exercise" -> key(arg) {
                    ExerciseDetailScreen(
                        graph, language, slug = arg,
                        onBack = close,
                        onOpenExercise = { route = "exercise:$it" },
                        onAddToWorkout = {
                            back = current
                            route = "builder-add:$it"
                        },
                    )
                }
                "session" -> SessionDetailScreen(graph, language, sessionId = arg, onBack = close)
                "device" -> DeviceScreen(graph, key = arg, onBack = close, onSources = { route = "device-sources" })
                else -> LaunchedEffect(current) { close() }
            }
        }
    }
}
