package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.DiaryEntry
import ch.coachpoulpy.shared.indexOfDay
import ch.coachpoulpy.shared.HomeState
import ch.coachpoulpy.shared.NextSession
import ch.coachpoulpy.shared.TodayItem
import ch.coachpoulpy.shared.TodayStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Home, the diary: Poulpy, what is next, then the past as a timeline of weeks and days. Only the timeline scrolls. The
 * calendar button opens a sheet that is only a shortcut (a tap on a day closes it and scrolls the timeline there), and
 * Record opens a sheet to choose what to record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    graph: AppGraph,
    language: String,
    onOpenWorkout: (String) -> Unit,
    onStart: (String) -> Unit,
    onResume: () -> Unit,
    onOpenLive: () -> Unit,
    onOpenSession: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val model = remember(graph, language) { graph.home(language, AppPreferences(context)) }
    DisposableEffect(model) { onDispose { model.close() } }
    // The session left open changes in the player, which covers this screen.
    LifecycleResumeEffect(model) {
        model.refresh()
        onPauseOrDispose { }
    }
    val state by model.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var choosingRecord by rememberSaveable { mutableStateOf(false) }
    var jumping by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = PoulpySpace.lg, end = PoulpySpace.lg, top = PoulpySpace.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.tab_home), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            PoulpyButton(onClick = { choosingRecord = true }) {
                Icon(Lucide.CircleDot, contentDescription = null)
                Text(stringResource(R.string.home_record), modifier = Modifier.padding(start = 6.dp))
            }
        }
        Column(
            Modifier.padding(horizontal = PoulpySpace.lg).padding(top = PoulpySpace.sm),
            verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
        ) {
            state.poulpy?.let { poulpy -> PoulpyHeader(poulpy, Modifier.fillMaxWidth(), compact = true) }
            if (state.loaded) {
                val upNext = state.today.filter { it.status != TodayStatus.DONE }
                if (upNext.isEmpty() && state.next == null) {
                    RestCard(state, onOpenLibrary)
                } else {
                    SectionLabel(stringResource(R.string.home_upNext))
                    upNext.forEach { item ->
                        UpNextCard(
                            item,
                            onClick = {
                                when {
                                    item.status != TodayStatus.IN_PROGRESS -> item.workoutId?.let(onOpenWorkout)
                                    item.live -> onOpenLive()
                                    else -> onResume()
                                }
                            },
                            onDiscard = model::discardUnfinished,
                        )
                    }
                    state.next?.let { next -> NextCard(next) { onOpenWorkout(next.workoutId) } }
                }
                if (state.past.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel(stringResource(R.string.home_past), Modifier.weight(1f))
                        IconButton(onClick = { jumping = true }) {
                            Icon(Lucide.Calendar, contentDescription = stringResource(R.string.home_jumpToDay))
                        }
                    }
                }
            }
        }
        if (state.loaded) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = PoulpySpace.lg, end = PoulpySpace.lg, bottom = PoulpySpace.xl),
                verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
            ) {
                itemsIndexed(state.past, key = { index, entry -> entry.key(index) }) { _, entry ->
                    when (entry) {
                        is DiaryEntry.WeekHeading -> SectionLabel(
                            if (entry.isCurrent) stringResource(R.string.history_thisWeek) else stringResource(R.string.home_weekOf, shortDate(entry.startMillis)),
                            Modifier.padding(top = PoulpySpace.xs),
                        )
                        is DiaryEntry.WeekRecap -> Text(
                            "${pluralStringResource(R.plurals.history_sessions, entry.sessions, entry.sessions)} · ${stringResource(R.string.history_minutes, entry.activeMinutes)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                        is DiaryEntry.DayHeading -> SectionLabel(
                            if (entry.isToday) stringResource(R.string.history_todayDay, dayLabel(entry.dateMillis)) else dayLabel(entry.dateMillis),
                        )
                        is DiaryEntry.Session -> SessionCard(entry.row, highlighted = false) { onOpenSession(entry.row.id) }
                    }
                }
            }
        }
    }

    if (choosingRecord) {
        RecordSheet(
            onWalkOrRun = {
                choosingRecord = false
                onOpenLive()
            },
            onLibrary = {
                choosingRecord = false
                onOpenLibrary()
            },
            onClose = { choosingRecord = false },
        )
    }
    if (jumping) {
        JumpToDaySheet(
            graph = graph,
            language = language,
            onDay = { dayMillis ->
                jumping = false
                val index = state.past.indexOfDay(dayMillis)
                if (index >= 0) scope.launch { listState.animateScrollToItem(index) }
            },
            onClose = { jumping = false },
        )
    }
}

private fun DiaryEntry.key(index: Int): String = when (this) {
    is DiaryEntry.WeekHeading -> "week:$startMillis"
    is DiaryEntry.WeekRecap -> "recap:$index"
    is DiaryEntry.DayHeading -> "day:$dateMillis"
    is DiaryEntry.Session -> "session:${row.id}"
}

/** What are you recording? A walk or run opens Live; a library workout sends to the library to pick one. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSheet(onWalkOrRun: () -> Unit, onLibrary: () -> Unit, onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = colors.surface) {
        Column(
            Modifier.padding(horizontal = PoulpySpace.lg).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.weight(1f)) { SheetTitle(stringResource(R.string.record_title), stringResource(R.string.record_subtitle)) }
                IconButton(onClick = onClose) { Icon(Lucide.X, contentDescription = stringResource(R.string.action_close)) }
            }
            RecordChoice(Lucide.Gauge, R.string.record_walkTitle, R.string.record_walkDetail, onWalkOrRun)
            RecordChoice(Lucide.LibraryBig, R.string.record_libraryTitle, R.string.record_libraryDetail, onLibrary)
        }
    }
}

@Composable
private fun RecordChoice(icon: ImageVector, @StringRes title: Int, @StringRes detail: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(PoulpySpace.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(detail), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/** The month grid as a shortcut: a tap on a day closes the sheet and the timeline scrolls to that day. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JumpToDaySheet(graph: AppGraph, language: String, onDay: (Long) -> Unit, onClose: () -> Unit) {
    val history = remember(graph, language) { graph.history(language) }
    DisposableEffect(history) { onDispose { history.close() } }
    val historyState by history.state.collectAsStateWithLifecycle()
    var offset by rememberSaveable { mutableIntStateOf(0) }
    val month = history.month(historyState, offset, selectedDay = 0)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(bottom = 24.dp).navigationBarsPadding()) {
            Row(Modifier.padding(horizontal = PoulpySpace.lg), verticalAlignment = Alignment.Top) {
                Box(Modifier.weight(1f)) { SheetTitle(stringResource(R.string.home_jumpToDay), "") }
                IconButton(onClick = onClose) { Icon(Lucide.X, contentDescription = stringResource(R.string.action_close)) }
            }
            MonthCard(month, selected = 0, onSelect = { day ->
                val start = Instant.ofEpochMilli(month.monthStartMillis).atZone(ZoneId.systemDefault()).toLocalDate().plusDays(day - 1L)
                onDay(start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            }, onMonth = { step -> offset = (offset + step).coerceAtMost(0) })
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 6.dp),
    )
}

/** A workout to do or a session left halfway, as a card ending in a chevron; a session left halfway can also be discarded. */
@Composable
private fun UpNextCard(item: TodayItem, onClick: () -> Unit, onDiscard: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val inProgress = item.status == TodayStatus.IN_PROGRESS
    var discarding by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(
        onClick = onClick,
        enabled = inProgress || item.workoutId != null,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.5.dp, PoulpyPalette.Lagoon300),
        shape = RoundedCornerShape(PoulpyRadius.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(PoulpySpace.sm), verticalArrangement = Arrangement.spacedBy(PoulpySpace.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Lucide.Dumbbell, contentDescription = null, tint = colors.onPrimaryContainer)
                }
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    val detail = if (inProgress) stringResource(R.string.home_inProgress) else item.details
                    if (detail.isNotEmpty()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
            }
            if (inProgress) {
                // Destructive, so crimson with an icon, never ember.
                TextButton(onClick = { discarding = true }) {
                    Icon(Lucide.Trash, contentDescription = null, tint = colors.error)
                    Text(stringResource(R.string.home_discard), color = colors.error, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
    if (discarding) {
        AlertDialog(
            onDismissRequest = { discarding = false },
            title = { Text(stringResource(R.string.home_discardTitle)) },
            text = { Text(stringResource(R.string.home_discardBody)) },
            confirmButton = {
                TextButton(onClick = { discarding = false; onDiscard() }) {
                    Text(stringResource(R.string.home_discard), color = colors.error)
                }
            },
            dismissButton = { TextButton(onClick = { discarding = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun NextCard(next: NextSession, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val day = Instant.ofEpochMilli(next.dayMillis).atZone(ZoneId.systemDefault())
    val whenText = if (next.daysAway == 1) stringResource(R.string.home_tomorrow, next.details)
    else pluralStringResource(R.plurals.home_inDays, next.daysAway, next.daysAway, next.details)
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(PoulpySpace.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                Modifier.size(44.dp, 48.dp).clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    DateTimeFormatter.ofPattern("EEE", Locale.getDefault()).format(day).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary,
                )
                Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(next.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(whenText, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/** Nothing planned or done today: rest, or go and pick something. */
@Composable
private fun RestCard(state: HomeState, onOpenLibrary: () -> Unit) {
    val dash = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = PoulpySpace.xs)
            .drawBehind {
                drawRoundRect(
                    dash,
                    cornerRadius = CornerRadius(PoulpyRadius.lg.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(PoulpySpace.md),
        verticalArrangement = Arrangement.spacedBy(PoulpySpace.xs),
    ) {
        Text(
            stringResource(if (state.hasWorkouts) R.string.home_restTitle else R.string.home_firstTitle),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            stringResource(if (state.hasWorkouts) R.string.home_restBody else R.string.home_firstBody),
            style = MaterialTheme.typography.bodyMedium,
        )
        PoulpyButton(onClick = onOpenLibrary, modifier = Modifier.padding(top = PoulpySpace.xs)) {
            Icon(Lucide.LibraryBig, contentDescription = null)
            Text(stringResource(R.string.home_openLibrary), modifier = Modifier.padding(start = 8.dp))
        }
    }
}
