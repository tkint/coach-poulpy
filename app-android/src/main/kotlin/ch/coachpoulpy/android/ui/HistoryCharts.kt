package ch.coachpoulpy.android.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
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
import ch.coachpoulpy.core.model.BodyFigure
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

/**
 * The History stats: this week and month, the weekly bar charts and the totals. Not shown by any screen yet; kept for the
 * graphs that come back, which may reuse it.
 */
@Composable
internal fun StatsView(state: HistoryState) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = PoulpySpace.lg, end = PoulpySpace.lg, top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PeriodCard(R.string.history_thisWeek, state.thisWeek, highlighted = true, Modifier.weight(1f))
            PeriodCard(R.string.history_thisMonth, state.thisMonth, highlighted = false, Modifier.weight(1f))
        }
        ChartsCard(state)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                stringResource(R.string.history_total),
                "${state.totalSessions}",
                pluralStringResource(R.plurals.history_sessionsUnit, state.totalSessions),
                Modifier.weight(1f),
            )
            StatTile(
                stringResource(R.string.history_longestRun),
                state.longestRunMeters?.let { distance(it, 1) } ?: "–",
                state.longestRunMillis?.let { shortDayMonth(it) } ?: "",
                Modifier.weight(1f),
            )
            StatTile(
                stringResource(R.string.history_fullOctopus),
                "×${state.fullOctopuses}",
                stringResource(R.string.history_tentaclesLit, state.tentaclesLit),
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
                title = stringResource(R.string.history_chartMinutes),
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
                    title = stringResource(R.string.history_chartDistance),
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
