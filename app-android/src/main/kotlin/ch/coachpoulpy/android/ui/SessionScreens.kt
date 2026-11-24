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

/** In the diary the card is the timeline's: tighter corners and no time of day, since the day heading says when. */
@Composable
internal fun SessionCard(session: SessionRow, highlighted: Boolean, diary: Boolean = false, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = if (highlighted) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outline),
        shape = if (diary) RoundedCornerShape(PoulpyRadius.md) else CardDefaults.outlinedShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            if (diary) Modifier.padding(PoulpySpace.md) else Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(session.icon, contentDescription = null, tint = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (diary) session.headline() else "${time(session.startedAtMillis)} · ${session.headline()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
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
    val context = LocalContext.current
    val units = LocalUnitSystem.current
    val chooser = stringResource(R.string.shareImage_chooser)
    var sharing by remember { mutableStateOf(false) }
    val saveFit = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(FIT_MIME_TYPE)) { target ->
        target?.let { graph.sessionFitFile(sessionId)?.let { file -> saveFitFile(context, file, it) } }
    }
    val colors = MaterialTheme.colorScheme

    val recap = remember(graph, language, sessionId, session != null) { if (session == null) null else graph.sessionRecap(sessionId, language) }
    val dateLine = recap?.let { listOfNotNull(dateTime(it.startedAtMillis), it.formatLabel).joinToString(" · ") }.orEmpty()
    if (recap != null && recap.muscles.heat.isNotEmpty()) {
        val figure = remember(graph, context) { graph.profile(AppPreferences(context)).profile.value.bodyFigure ?: BodyFigure.SHOULDERS }
        SessionRecapSheet(recap, dateLine, figure, onBack = onBack, onShare = { sharing = true }, onDelete = { deleting = true })
    } else {
        Scaffold { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                AppBar(
                    title = null,
                    leading = { BackButton(onBack) },
                    actions = {
                        if (session != null) {
                            IconButton(onClick = { sharing = true }) {
                                Icon(Lucide.Share2, contentDescription = stringResource(R.string.shareSession_title))
                            }
                            IconButton(onClick = { deleting = true }) {
                                Icon(Lucide.Trash, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                    },
                )
                if (recap != null) SessionRecapContent(recap, dateLine, Modifier.weight(1f))
            }
        }
    }

    if (sharing && session != null) {
        ShareSheet(
            title = R.string.shareSession_title,
            why = R.string.shareSession_why,
            choices = listOf(
                ShareChoice(Lucide.Download, R.string.shareSession_fit, R.string.shareSession_fitNote) {
                    sharing = false
                    graph.sessionFitFile(session.id)?.let { saveFit.launch(it.fileName) }
                },
                ShareChoice(Lucide.Image, R.string.shareSession_image, R.string.shareSession_imageNote) {
                    sharing = false
                    graph.sessionShareCard(session.id, units, language)?.let { shareSessionImage(context, it, chooser, colors) }
                },
            ),
            onDismiss = { sharing = false },
        )
    }

    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text(stringResource(R.string.history_deleteTitle)) },
            text = { Text(stringResource(R.string.history_deleteBody)) },
            confirmButton = {
                TextButton(onClick = { model.delete(sessionId); deleting = false; onBack() }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleting = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}


private val SessionRow.icon
    get() = if (kind == SessionKind.TREADMILL) Lucide.Gauge else Lucide.Dumbbell

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

/** "Sat 24 Oct", in the user's order. */
internal fun dayLabel(millis: Long): String = pattern("EEEdMMM").format(zoned(millis))

/** "24 Oct", in the user's order. */
internal fun shortDayMonth(millis: Long): String = pattern("dMMM").format(zoned(millis))

/** Day and month in the user's order, such as "21/10" or "10/21". */
internal fun shortDate(millis: Long): String = pattern("dM").format(zoned(millis))

/** A distance in the units picked in Settings. */
@Composable
internal fun distance(meters: Double, decimals: Int): String {
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
