package ch.coachpoulpy.android.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.shared.AppGraph

private enum class Tab(@StringRes val label: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Lucide.House),
    LIBRARY(R.string.tab_library, Lucide.LibraryBig),
    YOU(R.string.tab_you, Lucide.UserRound),
}

/**
 * The screen shown over the tabs, as a string so it survives rotation:
 * "import" (a workout file opened from another app), "builder:<id>", "builder-new:<format>" (new, in that format), "builder-add:<format>:<slug>" (new, holding that exercise), "builder-copy:<id>" (an unsaved copy), "player:<id>", "resume", "workout:<id>", "exercise:<slug>", "session:<id>",
 * "live" (recording a walk or run, from Record on Home), "device-add", "device-sources", "device:<key>", or "account:<step>" (see [AccountFlow]).
 */
private typealias Route = String

/** [openLive] shows Live over the tabs, as when the recording notification is tapped; [onLiveOpened] says it was done. */
@Composable
fun CoachPoulpyRoot(
    graph: AppGraph,
    openLive: Boolean = false,
    onLiveOpened: () -> Unit = {},
    /** The text of a workout file opened from another app, until it opens in the builder or is refused; [onImportOpened] says it was. */
    importFile: String? = null,
    onImportOpened: () -> Unit = {},
    /** An account step from an email link, such as "confirm:<token>"; [onAccountLinkOpened] says it showed. */
    accountLink: String? = null,
    onAccountLinkOpened: () -> Unit = {},
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var libraryView by rememberSaveable { mutableStateOf(LibraryView.WORKOUTS) }
    var route by rememberSaveable { mutableStateOf<Route?>(null) }
    var settingsPage by rememberSaveable { mutableStateOf(SettingsPage.HUB) }
    // A mixed workout played from Live (#82) takes the whole display, back button included.
    var liveFullScreen by remember { mutableStateOf(false) }
    val language = LocalConfiguration.current.locales[0].language
    // Where closing goes back to: the workout page for its editor and exercises, else the tabs.
    var back by rememberSaveable { mutableStateOf<Route?>(null) }
    val close = {
        route = back
        back = null
    }

    BackHandler(enabled = route != null) { close() }

    // The file lives in memory only: after a rotation the import is gone and the tabs show again.
    var opened by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(importFile) {
        if (importFile != null) {
            opened = importFile
            back = null
            route = "import"
            onImportOpened()
        }
    }

    // An email link opens its screen over the You tab, where Continue or Close leads.
    LaunchedEffect(accountLink) {
        if (accountLink != null) {
            tab = Tab.YOU
            settingsPage = SettingsPage.HUB
            back = null
            route = "account:$accountLink"
            onAccountLinkOpened()
        }
    }

    // Email confirmed, in a sheet over whatever is on screen: the link opened on this phone, or on another device
    // while the app is open.
    var emailConfirmed by rememberSaveable { mutableStateOf(false) }
    WatchEmailConfirmation {
        // Check your email is done with once the address is confirmed.
        if (route?.startsWith("account:") == true) close()
        emailConfirmed = true
    }
    if (emailConfirmed) EmailConfirmedSheet(onDismiss = { emailConfirmed = false })

    // The format of a new workout, asked in a sheet over the page that asked: "" for a workout of its own, or the slug of
    // the exercise it starts with. Closing it leaves the page as it was.
    var choosingFormatFor by rememberSaveable { mutableStateOf<String?>(null) }
    choosingFormatFor?.let { slug ->
        FormatSheet(
            options = remember(language) { graph.formatOptions(language) },
            current = null,
            onChoose = { format ->
                choosingFormatFor = null
                if (slug.isEmpty()) {
                    route = "builder-new:${format.name}"
                } else {
                    back = route
                    route = "builder-add:${format.name}:$slug"
                }
            },
            onClose = { choosingFormatFor = null },
        )
    }

    LaunchedEffect(openLive) {
        if (openLive) {
            back = null
            route = "live"
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
                    onOpenLive = { route = "live" },
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
                    onNew = { choosingFormatFor = "" },
                    onCopy = { route = "builder-copy:$it" },
                    onOpenExercise = { route = "exercise:$it" },
                    modifier = modifier,
                )
                Tab.YOU -> SettingsScreen(
                    graph,
                    language = language,
                    page = settingsPage,
                    onPage = { settingsPage = it },
                    onOpen = { device ->
                        route = when (device) {
                            DeviceRoute.Add -> "device-add"
                            DeviceRoute.Sources -> "device-sources"
                            is DeviceRoute.Device -> "device:${device.key}"
                        }
                    },
                    onAccount = { route = "account:$it" },
                    modifier = modifier,
                )
            }
        }
        "import" -> {
            val text = opened
            if (text == null) {
                LaunchedEffect(Unit) { close() }
            } else {
                val import = remember(text, language) { graph.importWorkout(text, language) }
                // A good file opens in the builder as an unsaved copy; one the builder can't hold is added and its page
                // opens; a refused one says why.
                when {
                    !import.state.ready -> ImportScreen(import.state, onClose = { opened = null; close() })
                    import.opensInBuilder -> BuilderScreen(graph, language, workoutId = null, importing = import, onClose = { opened = null; close() })
                    else -> LaunchedEffect(import) {
                        val id = import.add()
                        opened = null
                        route = if (id != null) "workout:$id" else null
                    }
                }
            }
        }
        "resume" -> PlayerScreen(graph, language, workoutId = null, onClose = close)
        // Recording a walk or run takes the whole display, with a way back to Home; recording goes on after closing.
        "live" -> Scaffold(
            topBar = {
                if (!liveFullScreen) IconButton(onClick = close) { Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.action_back)) }
            },
        ) { padding ->
            LiveScreen(
                graph,
                language,
                onManageDevices = {
                    settingsPage = SettingsPage.DEVICES
                    tab = Tab.YOU
                    back = null
                    route = null
                },
                modifier = Modifier.padding(padding),
                onFullScreen = { liveFullScreen = it },
            )
        }
        "device-add" -> DeviceAddScreen(graph, onBack = close)
        "device-sources" -> DataSourcesScreen(graph, onBack = close)
        else -> {
            val (kind, arg) = current.split(':', limit = 2)
            when (kind) {
                "builder" -> BuilderScreen(graph, language, workoutId = arg, onClose = close)
                "builder-new" -> BuilderScreen(graph, language, workoutId = null, newFormat = WorkoutFormat.valueOf(arg), onClose = close)
                "builder-add" -> {
                    val (format, slug) = arg.split(':', limit = 2)
                    BuilderScreen(graph, language, workoutId = null, newFormat = WorkoutFormat.valueOf(format), firstExercise = slug, onClose = close)
                }
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
                )
                // A variation replaces the page in place, so back still leads where this page came from.
                "exercise" -> key(arg) {
                    ExerciseDetailScreen(
                        graph, language, slug = arg,
                        onBack = close,
                        onOpenExercise = { route = "exercise:$it" },
                        onAddToWorkout = { choosingFormatFor = it },
                    )
                }
                "session" -> SessionDetailScreen(graph, language, sessionId = arg, onBack = close)
                "account" -> AccountFlow(
                    arg,
                    onStep = {
                        if (it == "confirmed") {
                            close()
                            emailConfirmed = true
                        } else {
                            route = "account:$it"
                        }
                    },
                    onClose = close,
                )
                "device" -> DeviceScreen(graph, key = arg, onBack = close, onSources = { route = "device-sources" })
                else -> LaunchedEffect(current) { close() }
            }
        }
    }
}
