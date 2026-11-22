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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.ui.draw.clip
import ch.coachpoulpy.shared.PoulpyStatus
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.Instant
import java.time.YearMonth
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Home, the diary: Poulpy, what is next, then the past as a timeline of weeks and days. Only the timeline scrolls. The
 * calendar button opens a month calendar that marks the days with sessions (a picked day scrolls the timeline there), and
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
            state.poulpy?.let { poulpy -> MotivationRow(poulpy) }
            if (state.loaded) {
                val upNext = state.today.filter { it.status != TodayStatus.DONE }
                if (!state.hasWorkouts) {
                    RestCard(state, onOpenLibrary)
                } else {
                    SectionLabel(stringResource(R.string.home_upNext))
                    upNext.forEachIndexed { index, item ->
                        UpNextCard(
                            item,
                            highlighted = index == 0,
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
                    if (upNext.isEmpty()) RestRow(onOpenLibrary)
                    state.next?.let { next -> NextCard(next) { onOpenWorkout(next.workoutId) } }
                }
                if (state.past.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel(stringResource(R.string.home_past), Modifier.weight(1f))
                        // 40 square with the screens' corner, as drawn.
                        IconButton(onClick = { jumping = true }, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(PoulpyRadius.md))) {
                            Icon(Lucide.Calendar, contentDescription = stringResource(R.string.home_jumpToDay), modifier = Modifier.size(20.dp))
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
            ) {
                itemsIndexed(state.past, key = { index, entry -> entry.key(index) }) { index, entry ->
                    val colors = MaterialTheme.colorScheme
                    TimelineRow(
                        dot = when (entry) {
                            is DiaryEntry.Session -> colors.outline
                            is DiaryEntry.WeekRecap -> colors.primary
                            // Headings sit on the line without a dot, as drawn.
                            else -> null
                        },
                        first = index == 0,
                        last = index == state.past.lastIndex,
                    ) {
                        when (entry) {
                            is DiaryEntry.WeekHeading -> RuledLabel(
                                if (entry.isCurrent) stringResource(R.string.history_thisWeek) else stringResource(R.string.home_weekOf, shortDate(entry.startMillis)),
                                color = if (entry.isCurrent) colors.primary else colors.onSurfaceVariant,
                            )
                            is DiaryEntry.WeekRecap -> Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.lg)).background(colors.primaryContainer).padding(PoulpySpace.md),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(stringResource(R.string.home_yourWeek), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.onPrimaryContainer)
                                Text(
                                    "${pluralStringResource(R.plurals.history_sessions, entry.sessions, entry.sessions)} · ${stringResource(R.string.history_minutes, entry.activeMinutes)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimaryContainer,
                                )
                            }
                            is DiaryEntry.DayHeading -> RuledLabel(
                                if (entry.isToday) stringResource(R.string.history_todayDay, dayLabel(entry.dateMillis)) else dayLabel(entry.dateMillis),
                                color = if (entry.isToday) colors.primary else colors.onSurfaceVariant,
                            )
                            is DiaryEntry.Session -> SessionCard(entry.row, highlighted = false, diary = true) { onOpenSession(entry.row.id) }
                        }
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

/** A month calendar as a shortcut: days with sessions are filled, and a tap on a day closes the sheet and scrolls the timeline there. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JumpToDaySheet(graph: AppGraph, language: String, onDay: (Long) -> Unit, onClose: () -> Unit) {
    val history = remember(graph, language) { graph.history(language) }
    DisposableEffect(history) { onDispose { history.close() } }
    val historyState by history.state.collectAsStateWithLifecycle()
    val zone = ZoneId.systemDefault()
    val today = remember { LocalDate.now() }
    val activeDays = remember(historyState.sessions) {
        historyState.sessions.map { Instant.ofEpochMilli(it.startedAtMillis).atZone(zone).toLocalDate() }.toSet()
    }
    val thisMonth = YearMonth.from(today)
    val firstMonth = thisMonth.plusMonths(historyState.firstMonthOffset.toLong())
    val firstDay = historyState.firstDayMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
    val calendar = rememberCalendarState(startMonth = firstMonth, endMonth = thisMonth, firstVisibleMonth = thisMonth, firstDayOfWeek = DayOfWeek.MONDAY)
    val scope = rememberCoroutineScope()
    val shown = calendar.firstVisibleMonth.yearMonth
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(horizontal = PoulpySpace.lg).padding(bottom = 24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.weight(1f)) { SheetTitle(stringResource(R.string.home_jumpToDay), "") }
                IconButton(onClick = onClose) { Icon(Lucide.X, contentDescription = stringResource(R.string.action_close)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { calendar.animateScrollToMonth(shown.minusMonths(1)) } }, enabled = shown > firstMonth) {
                    Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.history_previousMonth))
                }
                Text(
                    shown.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { scope.launch { calendar.animateScrollToMonth(shown.plusMonths(1)) } }, enabled = shown < thisMonth) {
                    Icon(Lucide.ChevronRight, contentDescription = stringResource(R.string.history_nextMonth))
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clearAndSetSemantics { }) {
                daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY).forEach { day ->
                    Text(
                        day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalCalendar(
                state = calendar,
                dayContent = { day ->
                    if (day.position == DayPosition.MonthDate) {
                        JumpDay(day.date, today, firstDay, active = day.date in activeDays) { onDay(it.atStartOfDay(zone).toInstant().toEpochMilli()) }
                    }
                },
            )
        }
    }
}

/** One day of the jump calendar: filled when it had sessions, a dot under today, dimmed and inert before the first session and after today. */
@Composable
private fun JumpDay(date: LocalDate, today: LocalDate, firstDay: LocalDate?, active: Boolean, onPick: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Nothing to jump to before the first session, or after today.
    val future = date.isAfter(today)
    val pickable = !future && (firstDay == null || !date.isBefore(firstDay))
    val text = when {
        active -> colors.onPrimary
        !pickable -> colors.onSurfaceVariant.copy(alpha = 0.45f)
        else -> colors.onSurface
    }
    val description = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) + if (active) ", " + stringResource(R.string.home_jumpDayHasSessions) else ""
    Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(42.dp)
                .padding(3.dp)
                .clip(CircleShape)
                .background(if (active) colors.primary else Color.Transparent)
                .clickable(enabled = pickable, onClick = { onPick(date) })
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${date.dayOfMonth}",
                    color = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold,
                )
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (date != today) Color.Transparent else if (active) colors.onPrimary else colors.primary),
                )
            }
        }
    }
}

/** A label followed by a hairline to the edge, as the day headings of the diary are drawn. */
@Composable
private fun RuledLabel(text: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal = PoulpySpace.xs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
        Text(text.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelMedium, color = color)
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
    }
}

/** One row of the diary's rail: a line down the left with a dot at the row's middle, primary for the week, outline for sessions, none for headings. */
@Composable
private fun TimelineRow(dot: Color?, first: Boolean, last: Boolean, content: @Composable () -> Unit) {
    val rail = MaterialTheme.colorScheme.outline
    val ground = MaterialTheme.colorScheme.background
    val gap = PoulpySpace.sm
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = 8.dp.toPx()
                val content = size.height - gap.toPx()
                val middle = content / 2
                // One line down the whole past, 12 in from its first and last rows.
                drawLine(rail, Offset(x, if (first) 12.dp.toPx() else 0f), Offset(x, if (last) content - 12.dp.toPx() else size.height), strokeWidth = 2.dp.toPx())
                if (dot != null) {
                    drawCircle(ground, radius = 8.dp.toPx(), center = Offset(x, middle))
                    drawCircle(dot, radius = 6.dp.toPx(), center = Offset(x, middle))
                }
            }
            .padding(start = PoulpySpace.xl, bottom = gap),
    ) { content() }
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
private fun UpNextCard(item: TodayItem, highlighted: Boolean, detail: String? = null, onClick: () -> Unit, onDiscard: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val inProgress = item.status == TodayStatus.IN_PROGRESS
    var discarding by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(
        onClick = onClick,
        enabled = inProgress || item.workoutId != null,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = if (highlighted) BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.5f)) else BorderStroke(1.dp, colors.outline),
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
                    val line = detail ?: if (inProgress) stringResource(R.string.home_inProgress) else item.details
                    if (line.isNotEmpty()) Text(line, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
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

/** The next planned session, drawn as an Up next row: "Tomorrow · Sets · about 25 min". */
@Composable
private fun NextCard(next: NextSession, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val whenText = if (next.daysAway == 1) stringResource(R.string.home_tomorrow, next.details)
    else pluralStringResource(R.plurals.home_inDays, next.daysAway, next.daysAway, next.details)
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(PoulpySpace.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Lucide.Dumbbell, contentDescription = null, tint = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(next.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(whenText, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/** Nothing to do today: a row the size of the Up next ones, opening the library. */
@Composable
private fun RestRow(onOpenLibrary: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onOpenLibrary,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.lg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(PoulpySpace.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Lucide.Moon, contentDescription = null, tint = colors.onSurfaceVariant)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_restTitle), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.home_restLine), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(Lucide.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/**
 * Home's motivation row (`home-diary` mockup): a small Poulpy and his eight tentacles as segments, never a streak.
 * The latest achievements and the way to Achievements join it with #523.
 */
@Composable
private fun MotivationRow(poulpy: PoulpyStatus) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.surface)
            .border(1.dp, colors.outline, RoundedCornerShape(percent = 50))
            .padding(start = PoulpySpace.xs, end = PoulpySpace.lg)
            .semantics(mergeDescendants = true) { contentDescription = poulpy.tentaclesCaption },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
    ) {
        Image(painterResource(poulpy.drawing.drawable), contentDescription = null, modifier = Modifier.padding(start = 4.dp).size(36.dp, 32.dp))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(PoulpyStatus.TENTACLES) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (index < poulpy.tentaclesLit) colors.primary else colors.onPrimaryContainer.copy(alpha = 0.18f)),
                )
            }
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
