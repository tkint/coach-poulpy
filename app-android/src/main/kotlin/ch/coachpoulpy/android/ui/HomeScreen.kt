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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ch.coachpoulpy.shared.HomeState
import ch.coachpoulpy.shared.NextSession
import ch.coachpoulpy.shared.TodayItem
import ch.coachpoulpy.shared.TodayStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Home: Poulpy, what there is to do or was done today, and the next planned session. */
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = PoulpySpace.lg, bottom = PoulpySpace.xl),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "title") {
            Column {
                Text(
                    today().uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(stringResource(R.string.tab_home), style = MaterialTheme.typography.headlineMedium)
            }
        }
        state.poulpy?.let { poulpy ->
            item(key = "poulpy") { PoulpyHeader(poulpy, Modifier.fillMaxWidth(), compact = true) }
        }
        if (!state.loaded) return@LazyColumn
        if (state.today.isEmpty()) {
            item(key = "rest") { RestCard(state, onOpenLibrary) }
        } else {
            item(key = "today") { SectionLabel(stringResource(R.string.home_today)) }
            items(state.today, key = { it.key }) { item ->
                when (item.status) {
                    TodayStatus.DONE -> item.session?.let { session ->
                        DoneCard(item) { onOpenSession(session.id) }
                    }
                    else -> PlannedCard(
                        item,
                        onOpen = { item.workoutId?.let(onOpenWorkout) },
                        onGo = {
                            when {
                                item.status != TodayStatus.IN_PROGRESS -> item.workoutId?.let(onStart)
                                item.live -> onOpenLive()
                                else -> onResume()
                            }
                        },
                        onDiscard = model::discardUnfinished,
                    )
                }
            }
            item(key = "library") {
                TextButton(onClick = onOpenLibrary) {
                    Text(stringResource(R.string.home_somethingElse), fontWeight = FontWeight.ExtraBold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
        state.next?.let { next ->
            item(key = "next-label") { SectionLabel(stringResource(R.string.home_next), Modifier.padding(top = 8.dp)) }
            item(key = "next") { NextCard(next) { onOpenWorkout(next.workoutId) } }
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

/** A workout to do today, or a session left halfway, which can be opened (it comes back paused) or discarded. */
@Composable
private fun PlannedCard(item: TodayItem, onOpen: () -> Unit, onGo: () -> Unit, onDiscard: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val inProgress = item.status == TodayStatus.IN_PROGRESS
    var discarding by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.5.dp, PoulpyPalette.Lagoon300),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = colors.onPrimaryContainer)
                }
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleLarge)
                    if (item.details.isNotEmpty()) {
                        Text(item.details, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
            if (inProgress) {
                Text(stringResource(R.string.home_inProgress), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            } else if (item.exercises.isNotEmpty()) {
                Text(item.exercises, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (inProgress) {
                    // Destructive, so crimson with an icon, never ember.
                    OutlinedButton(
                        onClick = { discarding = true },
                        border = BorderStroke(1.dp, colors.error),
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = colors.error)
                        Text(
                            stringResource(R.string.home_discard),
                            color = colors.error,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                } else {
                    OutlinedButton(onClick = onOpen, enabled = item.workoutId != null, modifier = Modifier.weight(1f).height(48.dp)) {
                        Text(stringResource(R.string.home_seeWorkout), fontWeight = FontWeight.ExtraBold)
                    }
                }
                Button(onClick = onGo, modifier = Modifier.weight(1.4f).height(48.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    // "Open", not "Resume": the session comes back paused.
                    Text(
                        stringResource(if (inProgress) R.string.home_openSession else R.string.action_start),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 6.dp),
                    )
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

/** A session finished today, opening its page in the history. */
@Composable
private fun DoneCard(item: TodayItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val session = item.session ?: return
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(PoulpyTheme.extraColors.success), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.workout_done), tint = colors.surface)
            }
            Column(Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleLarge)
                Text("${time(session.startedAtMillis)} · ${clock(session.activeSeconds)}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
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
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                Modifier.size(52.dp, 56.dp).clip(RoundedCornerShape(14.dp)).background(colors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    DateTimeFormatter.ofPattern("EEE", Locale.getDefault()).format(day).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary,
                )
                Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge, color = colors.onPrimaryContainer)
            }
            Column(Modifier.weight(1f)) {
                Text(next.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(whenText, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
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
            .padding(top = 6.dp)
            .drawBehind {
                drawRoundRect(
                    dash,
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(if (state.hasWorkouts) R.string.home_restTitle else R.string.home_firstTitle),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            stringResource(if (state.hasWorkouts) R.string.home_restBody else R.string.home_firstBody),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onOpenLibrary, modifier = Modifier.padding(top = 8.dp).height(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null)
            Text(stringResource(R.string.home_openLibrary), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

/** "Saturday 24 October", in the user's order. */
private fun today(): String =
    DateTimeFormatter.ofPattern(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEdMMMM"), Locale.getDefault())
        .format(Instant.now().atZone(ZoneId.systemDefault()))

private fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
