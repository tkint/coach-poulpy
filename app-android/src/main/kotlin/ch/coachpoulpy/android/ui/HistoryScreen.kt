package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.HistoryState
import ch.coachpoulpy.shared.PeriodUi
import ch.coachpoulpy.shared.SessionKind
import ch.coachpoulpy.shared.SessionRow
import ch.coachpoulpy.shared.WeekBar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun HistoryScreen(graph: AppGraph, language: String, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    val model = remember(graph, language) { graph.history(language) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()

    if (!state.loaded) return
    if (state.sessions.isEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth().padding(top = 96.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.history_empty_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.history_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item { PeriodCards(state) }
        item { ChartsCard(state) }
        item {
            Text(
                stringResource(R.string.history_sessions_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(state.sessions, key = { it.id }) { session ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(session.id) }) {
                ListItem(
                    leadingContent = { Icon(session.icon, contentDescription = null) },
                    headlineContent = { Text(session.title, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("${dateTime(session.startedAtMillis)} · ${session.headline()}") },
                )
            }
        }
    }
}

@Composable
private fun PeriodCards(state: HistoryState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PeriodCard(R.string.history_this_week, state.thisWeek, Modifier.weight(1f))
        PeriodCard(R.string.history_this_month, state.thisMonth, Modifier.weight(1f))
    }
}

@Composable
private fun PeriodCard(@StringRes title: Int, period: PeriodUi, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(pluralStringResource(R.plurals.history_sessions, period.sessions, period.sessions), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.history_minutes, period.activeMinutes), style = MaterialTheme.typography.bodyMedium)
            if (period.distanceMeters > 0) Text("${decimal(period.distanceMeters / 1000, 1)} km", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ChartsCard(state: HistoryState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            WeekChart(
                title = stringResource(R.string.history_chart_minutes),
                weeks = state.weeks,
                value = { it.activeMinutes.toDouble() },
                label = { it.toInt().toString() },
                color = MaterialTheme.colorScheme.primary,
            )
            if (state.hasDistance) {
                WeekChart(
                    title = stringResource(R.string.history_chart_distance),
                    weeks = state.weeks,
                    value = { it.distanceKm },
                    label = { decimal(it, 1) },
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

/** One bar per week, the current one last and fully coloured, the value above each bar. */
@Composable
private fun WeekChart(title: String, weeks: List<WeekBar>, value: (WeekBar) -> Double, label: (Double) -> String, color: Color) {
    val max = weeks.maxOfOrNull(value)?.takeIf { it > 0 } ?: 1.0
    val description = weeks.joinToString { "${shortDate(it.startMillis)}: ${label(value(it))}" }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp).clearAndSetSemantics { contentDescription = "$title. $description" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            weeks.forEach { week ->
                val v = value(week)
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.weight(1f))
                    if (v > 0) Text(label(v), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height((80 * (v / max)).dp.coerceAtLeast(2.dp))
                            .background(
                                if (week.isCurrent) color else color.copy(alpha = 0.45f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                            ),
                    )
                    Text(
                        shortDate(week.startMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
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
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(dateTime(session.startedAtMillis), style = MaterialTheme.typography.titleMedium)
            session.typeLabel?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Line(R.string.treadmill_summary_duration, clock(session.activeSeconds))
                    if (session.kind == SessionKind.WORKOUT) {
                        session.setsDone?.let { done ->
                            Line(R.string.history_sets_done, session.setsPlanned?.let { "$done / $it" } ?: "$done")
                        }
                        session.setsSkipped?.takeIf { it > 0 }?.let { Line(R.string.history_sets_skipped, "$it") }
                    }
                    session.distanceMeters?.let { Line(R.string.metric_distance, "${decimal(it / 1000, 2)} km") }
                    session.averageSpeedKmh?.let { Line(R.string.treadmill_summary_average_speed, "${decimal(it, 1)} km/h") }
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
        Text(stringResource(label))
        Text(value)
    }
}

private val SessionRow.icon
    get() = if (kind == SessionKind.TREADMILL) Icons.AutoMirrored.Filled.DirectionsRun else Icons.Filled.FitnessCenter

/** The one number that matters most for the row: distance for the treadmill, sets for a workout. */
@Composable
private fun SessionRow.headline(): String {
    val parts = mutableListOf(clock(activeSeconds))
    distanceMeters?.let { parts += "${decimal(it / 1000, 2)} km" }
    val done = setsDone
    val planned = setsPlanned
    if (kind == SessionKind.WORKOUT && done != null && planned != null) parts += stringResource(R.string.history_sets, done, planned)
    return parts.joinToString(" · ")
}

private fun dateTime(millis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

/** Day and month in the user's order, such as "21/10" or "10/21". */
private fun shortDate(millis: Long): String {
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "dM")
    return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
}

private fun decimal(value: Double, decimals: Int) = String.format(Locale.getDefault(), "%.${decimals}f", value)

private fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
