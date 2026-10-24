package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.CalendarDay
import ch.coachpoulpy.shared.CalendarMonth
import ch.coachpoulpy.shared.DaySection
import ch.coachpoulpy.shared.HistoryModel
import ch.coachpoulpy.shared.HistoryState
import ch.coachpoulpy.shared.PeriodUi
import ch.coachpoulpy.shared.SessionKind
import ch.coachpoulpy.shared.SessionRow
import ch.coachpoulpy.shared.Units
import ch.coachpoulpy.shared.WeekBar
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private enum class HistoryView { CALENDAR, STATS }

@Composable
fun HistoryScreen(graph: AppGraph, language: String, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    val model = remember(graph, language) { graph.history(language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    var view by rememberSaveable { mutableStateOf(HistoryView.CALENDAR) }

    Column(modifier.fillMaxSize()) {
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, top = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.tab_history), style = MaterialTheme.typography.headlineMedium)
            ViewSwitch(view, onSelect = { view = it })
        }
        if (!state.loaded) return@Column
        when (view) {
            HistoryView.CALENDAR -> CalendarView(model, state, onOpen)
            HistoryView.STATS -> StatsView(state)
        }
    }
}

/** Calendar | Stats, as a pill with the chosen half raised. */
@Composable
private fun ViewSwitch(selected: HistoryView, onSelect: (HistoryView) -> Unit) {
    val pill = RoundedCornerShape(PoulpyRadius.pill)
    Row(
        Modifier.fillMaxWidth().clip(pill).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp).selectableGroup(),
    ) {
        listOf(
            Triple(HistoryView.CALENDAR, R.string.history_view_calendar, Icons.Filled.CalendarMonth),
            Triple(HistoryView.STATS, R.string.history_view_stats, Icons.Filled.BarChart),
        ).forEach { (item, label, icon) ->
            val on = item == selected
            val color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(pill)
                    .background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(item) }),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(stringResource(label), color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun CalendarView(model: HistoryModel, state: HistoryState, onOpen: (String) -> Unit) {
    val today = remember { LocalDate.now().dayOfMonth }
    var offset by rememberSaveable { mutableIntStateOf(0) }
    // This month opens on today; an earlier one opens with no day picked.
    var selected by rememberSaveable { mutableIntStateOf(today) }
    val month = remember(state, offset, selected) { model.month(state, offset, selected) }
    val list = rememberLazyListState()

    val changeMonth = { by: Int ->
        val next = (offset + by).coerceIn(state.firstMonthOffset, 0)
        if (next != offset) {
            offset = next
            selected = if (next == 0) today else 0
        }
    }

    // A day's header sits after the rows of every newer day: scroll there when a day is picked.
    LaunchedEffect(offset, selected) {
        var index = 0
        for (section in month.sections) {
            if (section.day == selected) break
            index += 1 + maxOf(1, section.sessions.size)
        }
        if (month.sections.any { it.day == selected }) list.animateScrollToItem(index) else list.scrollToItem(0)
    }

    Column(Modifier.fillMaxSize()) {
        MonthCard(month, selected, onSelect = { selected = it }, onMonth = changeMonth)
        LazyColumn(
            state = list,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            if (month.sections.isEmpty() && state.sessions.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.history_empty_title), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.history_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            month.sections.forEach { section ->
                val highlighted = section.day == selected
                item(key = "day-${month.year}-${month.month}-${section.day}") { DayHeader(section, highlighted) }
                if (section.sessions.isEmpty()) {
                    item(key = "rest-${month.year}-${month.month}-${section.day}") { RestingRow() }
                } else {
                    items(section.sessions, key = { it.id }) { session ->
                        SessionCard(session, highlighted, onClick = { onOpen(session.id) })
                    }
                }
            }
        }
    }
}

/** The month grid, Monday first; swipe or the arrows change month, never past this one. */
@Composable
private fun MonthCard(month: CalendarMonth, selected: Int, onSelect: (Int) -> Unit, onMonth: (Int) -> Unit) {
    var drag by remember { mutableFloatStateOf(0f) }
    val cells: List<CalendarDay?> = List(month.leadingBlanks) { null } + month.days
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 14.dp)
            .fillMaxWidth()
            .pointerInput(month.year, month.month) {
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = {
                        if (drag > SWIPE_PX) onMonth(-1) else if (drag < -SWIPE_PX) onMonth(1)
                    },
                ) { _, amount -> drag += amount }
            },
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonth(-1) }, enabled = month.canGoBack) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.history_previous_month))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(monthTitle(month.monthStartMillis), style = MaterialTheme.typography.titleLarge)
                    Text(
                        pluralStringResource(R.plurals.history_active_days, month.activeDays, month.activeDays),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onMonth(1) }, enabled = month.canGoForward) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.history_next_month))
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clearAndSetSemantics { }) {
                DayOfWeek.values().forEach { day ->
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
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(Modifier.weight(1f).height(44.dp), contentAlignment = Alignment.Center) {
                            if (day != null) DayCell(month, day, day.day == selected, onSelect)
                        }
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DayCell(month: CalendarMonth, day: CalendarDay, selected: Boolean, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val text = when {
        day.active -> colors.onPrimary
        day.isFuture -> colors.onSurfaceVariant.copy(alpha = 0.45f)
        else -> colors.onSurface
    }
    val sessions = pluralStringResource(R.plurals.history_sessions, day.sessionCount, day.sessionCount)
    val description = fullDate(month.monthStartMillis, day.day) + if (day.active) ", $sessions" else ""
    Box(
        Modifier
            .size(42.dp)
            .then(if (selected) Modifier.border(2.dp, colors.onPrimaryContainer, CircleShape) else Modifier)
            .padding(3.dp)
            .clip(CircleShape)
            .background(if (day.active) colors.primary else Color.Transparent)
            .selectable(selected = selected, enabled = !day.isFuture, onClick = { onSelect(day.day) })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${day.day}",
                color = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.active) FontWeight.ExtraBold else FontWeight.Bold,
            )
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (!day.isToday) Color.Transparent else if (day.active) colors.onPrimary else colors.primary),
            )
        }
    }
}

@Composable
private fun DayHeader(section: DaySection, highlighted: Boolean) {
    val day = dayLabel(section.dateMillis)
    val label = if (section.isToday) stringResource(R.string.history_today_day, day) else day
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
    }
}

/** A dashed box for a picked day without sessions. */
@Composable
private fun RestingRow() {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Text(
        stringResource(R.string.history_resting),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}

@Composable
private fun SessionCard(session: SessionRow, highlighted: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = if (highlighted) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(session.icon, contentDescription = null, tint = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    "${time(session.startedAtMillis)} · ${session.headline()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatsView(state: HistoryState) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PeriodCard(R.string.history_this_week, state.thisWeek, highlighted = true, Modifier.weight(1f))
            PeriodCard(R.string.history_this_month, state.thisMonth, highlighted = false, Modifier.weight(1f))
        }
        ChartsCard(state)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                stringResource(R.string.history_total),
                "${state.totalSessions}",
                pluralStringResource(R.plurals.history_sessions_unit, state.totalSessions),
                Modifier.weight(1f),
            )
            StatTile(
                stringResource(R.string.history_longest_run),
                state.longestRunMeters?.let { distance(it, 1) } ?: "–",
                state.longestRunMillis?.let { shortDayMonth(it) } ?: "",
                Modifier.weight(1f),
            )
            StatTile(
                stringResource(R.string.history_full_octopus),
                "×${state.fullOctopuses}",
                stringResource(R.string.history_tentacles_lit, state.tentaclesLit),
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PeriodCard(@StringRes title: Int, period: PeriodUi, highlighted: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val content = if (highlighted) colors.onPrimaryContainer else colors.onSurface
    OutlinedCard(
        modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = if (highlighted) colors.primaryContainer else colors.surface),
        border = BorderStroke(1.dp, if (highlighted) colors.primaryContainer else colors.outline),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(title).uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = if (highlighted) content else colors.primary,
            )
            Text(
                pluralStringResource(R.plurals.history_sessions, period.sessions, period.sessions),
                style = MaterialTheme.typography.headlineMedium,
                color = content,
            )
            val parts = listOfNotNull(
                stringResource(R.string.history_minutes, period.activeMinutes),
                period.distanceMeters.takeIf { it > 0 }?.let { distance(it, 1) },
            )
            Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = content)
        }
    }
}

@Composable
private fun ChartsCard(state: HistoryState) {
    val dark = isSystemInDarkTheme()
    OutlinedCard(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            WeekChart(
                title = stringResource(R.string.history_chart_minutes),
                weeks = state.weeks,
                value = { it.activeMinutes.toDouble() },
                label = { it.toInt().toString() },
                current = MaterialTheme.colorScheme.primary,
                past = if (dark) PoulpyPalette.Lagoon800 else PoulpyPalette.Lagoon300,
                barHeight = 76,
            )
            if (state.hasDistance) {
                // Deep blue, not ember: ember is kept for Poulpy's grumpy side.
                WeekChart(
                    title = stringResource(R.string.history_chart_distance),
                    weeks = state.weeks,
                    value = { it.distanceKm },
                    label = { decimal(it, 1) },
                    current = if (dark) PoulpyPalette.Lagoon100 else PoulpyPalette.Lagoon800,
                    past = if (dark) PoulpyPalette.Neutral700 else PoulpyPalette.Neutral300,
                    barHeight = 56,
                )
            }
            Row(Modifier.fillMaxWidth().clearAndSetSemantics { }, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.weeks.forEach { week ->
                    Text(
                        shortDate(week.startMillis),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** One bar per week, the current one last in the stronger colour, the value above each bar. */
@Composable
private fun WeekChart(
    title: String,
    weeks: List<WeekBar>,
    value: (WeekBar) -> Double,
    label: (Double) -> String,
    current: Color,
    past: Color,
    barHeight: Int,
) {
    val max = weeks.maxOfOrNull(value)?.takeIf { it > 0 } ?: 1.0
    val description = weeks.joinToString { "${shortDate(it.startMillis)}: ${label(value(it))}" }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().height((barHeight + 24).dp).clearAndSetSemantics { contentDescription = "$title. $description" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            weeks.forEach { week ->
                val v = value(week)
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.weight(1f))
                    if (v > 0) Text(label(v), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp)
                            .height((barHeight * (v / max)).dp.coerceAtLeast(2.dp))
                            .background(
                                if (week.isCurrent) current else past,
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, caption: String, modifier: Modifier) {
    OutlinedCard(modifier, colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(caption, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(graph: AppGraph, language: String, sessionId: String, onBack: () -> Unit) {
    val model = remember(graph, language) { graph.history(language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    val session = state.sessions.firstOrNull { it.id == sessionId }
    var deleting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.title ?: "") },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (session != null) {
                        IconButton(onClick = { deleting = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (session == null) return@Scaffold
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(PoulpyRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Image(painterResource(R.drawable.poulpy_pose_exhausted), contentDescription = null, modifier = Modifier.width(96.dp).height(86.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        session.typeLabel?.let {
                            Text(
                                it.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Text(dateTime(session.startedAtMillis), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            OutlinedCard(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Line(R.string.treadmill_summary_duration, clock(session.activeSeconds))
                    if (session.kind == SessionKind.WORKOUT) {
                        session.setsDone?.let { done ->
                            Line(R.string.history_sets_done, session.setsPlanned?.let { "$done / $it" } ?: "$done")
                        }
                        session.setsSkipped?.takeIf { it > 0 }?.let { Line(R.string.history_sets_skipped, "$it") }
                    }
                    session.distanceMeters?.let { Line(R.string.metric_distance, distance(it, 2)) }
                    session.averageSpeedKmh?.let { kmh -> Line(R.string.treadmill_summary_average_speed, Units.speed(kmh, LocalUnitSystem.current).let { "${decimal(it.value, it.decimals)} ${it.unit}" }) }
                    session.averageHeartRateBpm?.let { Line(R.string.treadmill_summary_average_hr, "$it bpm") }
                    session.maxHeartRateBpm?.let { Line(R.string.treadmill_summary_max_hr, "$it bpm") }
                    session.energyKcal?.let { Line(R.string.metric_energy, "$it kcal") }
                    session.estimatedSteps?.let { Line(R.string.metric_steps, "≈ $it") }
                }
            }
        }
    }

    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = { Text(stringResource(R.string.history_delete_body)) },
            confirmButton = {
                TextButton(onClick = { model.delete(sessionId); deleting = false; onBack() }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun Line(@StringRes label: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Text(value, fontWeight = FontWeight.ExtraBold)
    }
}

private const val SWIPE_PX = 120f

private val SessionRow.icon
    get() = if (kind == SessionKind.TREADMILL) Icons.AutoMirrored.Filled.DirectionsRun else Icons.Filled.FitnessCenter

/** The numbers that matter most for the row: duration, then distance for the treadmill or sets for a workout. */
@Composable
private fun SessionRow.headline(): String {
    val parts = mutableListOf(clock(activeSeconds))
    distanceMeters?.let { parts += distance(it, 2) }
    val done = setsDone
    val planned = setsPlanned
    if (kind == SessionKind.WORKOUT && done != null && planned != null) parts += stringResource(R.string.history_sets, done, planned)
    return parts.joinToString(" · ")
}

private fun zoned(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())

private fun pattern(skeleton: String) =
    DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton), Locale.getDefault())

private fun dateTime(millis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(Locale.getDefault()).format(zoned(millis))

internal fun time(millis: Long): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()).format(zoned(millis))

/** "October 2026", in the standalone form some languages need for a title. */
private fun monthTitle(millis: Long): String =
    pattern("LLLLyyyy").format(zoned(millis)).replaceFirstChar { it.titlecase(Locale.getDefault()) }

/** "Sat 24 Oct", in the user's order. */
internal fun dayLabel(millis: Long): String = pattern("EEEdMMM").format(zoned(millis))

/** The whole date of [day] in the month starting at [monthStartMillis], for screen readers. */
private fun fullDate(monthStartMillis: Long, day: Int): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()).format(zoned(monthStartMillis).withDayOfMonth(day))

/** "24 Oct", in the user's order. */
private fun shortDayMonth(millis: Long): String = pattern("dMMM").format(zoned(millis))

/** Day and month in the user's order, such as "21/10" or "10/21". */
private fun shortDate(millis: Long): String = pattern("dM").format(zoned(millis))

/** A distance in the units picked in Settings. */
@Composable
private fun distance(meters: Double, decimals: Int): String {
    val m = Units.distance(meters, LocalUnitSystem.current, decimals)
    return "${decimal(m.value, m.decimals)} ${m.unit}"
}

private fun decimal(value: Double, decimals: Int) = String.format(Locale.getDefault(), "%.${decimals}f", value)

private fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
