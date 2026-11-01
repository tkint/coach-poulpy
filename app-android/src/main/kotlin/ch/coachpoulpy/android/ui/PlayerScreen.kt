package ch.coachpoulpy.android.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.FinishedScreen
import ch.coachpoulpy.shared.FinishedStat
import ch.coachpoulpy.shared.PlayerModel
import ch.coachpoulpy.shared.PlayerState
import ch.coachpoulpy.shared.PlayerStatus
import ch.coachpoulpy.shared.PoulpyDrawing
import ch.coachpoulpy.shared.PoulpyStatus

/** The guided session. [workoutId] null reopens the session left open. */
@Composable
fun PlayerScreen(graph: AppGraph, language: String, workoutId: String?, onClose: () -> Unit) {
    val model = remember(graph, language, workoutId) {
        if (workoutId == null) graph.resumeSession(language) else graph.player(workoutId, language)
    }
    if (model == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()

    // The phone sits on the floor during a session: keep the screen on and beep on changes.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    Cues(state)

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.status) {
                PlayerStatus.READY -> Ready(state, onStart = model::start, onClose = onClose)
                PlayerStatus.FINISHED -> {
                    val context = LocalContext.current
                    val units = LocalUnitSystem.current
                    val chooser = stringResource(R.string.shareImage_chooser)
                    val finished = remember(units) { model.finished(units) }
                    Finished(state, finished, onShare = { model.shareCard(units)?.let { shareSessionImage(context, it, chooser) } }, onClose = onClose)
                }
                PlayerStatus.RUNNING, PlayerStatus.PAUSED -> Running(state, model)
            }
        }
    }
}

@Composable
private fun ColumnScope.Ready(state: PlayerState, onStart: () -> Unit, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        PlayerIconButton(Icons.Filled.Close, stringResource(R.string.action_back), onClick = onClose)
    }
    Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        PlayerLabel("${state.workoutName} · ${state.exerciseCountLabel}")
        Text(stringResource(R.string.player_ready), style = PoulpyTextStyles.Display, modifier = Modifier.padding(top = PoulpySpace.sm))
        Image(
            painterResource(PoulpyDrawing.STANDING_TALL.drawable),
            contentDescription = null,
            modifier = Modifier.padding(vertical = PoulpySpace.lg).size(width = 200.dp, height = 180.dp),
        )
        PlayerLabel(stringResource(R.string.player_firstUp), color = MaterialTheme.colorScheme.primary)
        Text(state.title, style = PoulpyTextStyles.Title, textAlign = TextAlign.Center)
        state.targetDetail?.let { Text(it, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
        Text(stringResource(R.string.player_start), modifier = Modifier.padding(start = PoulpySpace.sm), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ColumnScope.Running(state: PlayerState, model: PlayerModel) {
    var confirmStop by remember { mutableStateOf(false) }
    val paused = state.status == PlayerStatus.PAUSED

    // Pause and end sit on the bar over the whole session; the step's own buttons are below.
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.md)) {
        PlayerIconButton(
            if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
            stringResource(if (paused) R.string.player_resume else R.string.player_pause),
            onClick = { if (paused) model.resume() else model.pause() },
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PlayerLabel("${stringResource(R.string.player_step, state.phaseNumber, state.phaseCount)} · ${state.sessionClock}")
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(
                    Modifier.fillMaxWidth(state.phaseNumber.toFloat() / state.phaseCount.coerceAtLeast(1)).fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
        PlayerIconButton(Icons.Filled.Close, stringResource(R.string.player_stop), onClick = { confirmStop = true })
    }

    if (state.isRest) RestStep(state, model, paused) else WorkStep(state, model, paused)

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text(stringResource(R.string.player_stopTitle)) },
            text = { Text(stringResource(R.string.player_stopBody)) },
            confirmButton = { TextButton(onClick = { confirmStop = false; model.stop() }) { Text(stringResource(R.string.player_stop)) } },
            dismissButton = { TextButton(onClick = { confirmStop = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

/** An exercise: Poulpy straining, the part, the name, the set, the big target, what comes next, Skip and Done (`player-set`, `player-circuit-round`). */
@Composable
private fun ColumnScope.WorkStep(state: PlayerState, model: PlayerModel, paused: Boolean) {
    val remaining = state.remainingSeconds
    Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(
            painterResource(PoulpyDrawing.STRAINING.drawable),
            contentDescription = null,
            modifier = Modifier.padding(bottom = PoulpySpace.md).size(width = 130.dp, height = 117.dp),
        )
        PartLine(state)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
            state.roleLabel?.let { RecoverTag(it) }
            Text(state.title, style = PoulpyTextStyles.Title, textAlign = TextAlign.Center)
        }
        state.sideLabel?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
        if (state.setCount > 1) {
            Text(
                stringResource(R.string.player_set, state.setNumber, state.setCount),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SetDots(state.setNumber, state.setCount)
        } else {
            Spacer(Modifier.height(PoulpySpace.md))
        }
        // A rep target is a big number with its unit under it; a timer, a distance or an open target is one line.
        val bigNumber = remaining?.let { clock(it) } ?: state.targetValue
        Text(
            bigNumber ?: state.targetLabel.orEmpty(),
            style = PoulpyTextStyles.Timer.copy(
                fontSize = if (bigNumber != null) 120.sp else 48.sp,
                lineHeight = if (bigNumber != null) 120.sp else 56.sp,
                letterSpacing = (-0.02).em,
            ),
            textAlign = TextAlign.Center,
        )
        (state.targetUnit ?: state.loadLabel)?.let { Text(it, fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        if (paused) Text(stringResource(R.string.player_paused), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = PoulpySpace.sm))
    }

    state.nextLine?.let { line ->
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(PoulpyRadius.md)).padding(horizontal = PoulpySpace.lg, vertical = PoulpySpace.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlayerLabel(stringResource(R.string.player_next))
            Text(line, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
    Spacer(Modifier.height(PoulpySpace.lg))
    Row(horizontalArrangement = Arrangement.spacedBy(PoulpySpace.md), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = model::skip,
            enabled = !paused,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 22.dp),
            modifier = Modifier.then(if (state.isTimed) Modifier.weight(1f) else Modifier).height(64.dp),
        ) { Text(stringResource(R.string.player_skip), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
        if (!state.isTimed) {
            Button(onClick = model::done, enabled = !paused, modifier = Modifier.weight(1f).height(64.dp)) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                Text(stringResource(R.string.player_done), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = PoulpySpace.sm))
            }
        }
    }
}

/** A rest: the ring running down around Poulpy catching his breath, ±15 s, what is next, Skip rest (`player-rest`, `player-recover`). */
@Composable
private fun ColumnScope.RestStep(state: PlayerState, model: PlayerModel, paused: Boolean) {
    val remaining = state.remainingSeconds ?: 0
    val total = (state.totalSeconds ?: 0).coerceAtLeast(1)
    Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)) {
        if (state.inRepeat) PartLine(state)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.inRepeat) state.roleLabel?.let { RecoverTag(it) }
            Text(state.title, style = PoulpyTextStyles.Title, color = MaterialTheme.colorScheme.primary)
        }
        val track = MaterialTheme.colorScheme.surfaceVariant
        val left = PoulpyMood.Recovering.tone
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val width = size.width * 12f / 200f
                val inset = width / 2
                val arc = Size(size.width - width, size.height - width)
                drawArc(track, 0f, 360f, useCenter = false, topLeft = Offset(inset, inset), size = arc, style = Stroke(width))
                drawArc(left, -90f, 360f * (remaining.toFloat() / total).coerceIn(0f, 1f), useCenter = false, topLeft = Offset(inset, inset), size = arc, style = Stroke(width, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(PoulpySpace.xs)) {
                Image(painterResource(PoulpyDrawing.SIDE_GLANCE.drawable), contentDescription = null, modifier = Modifier.size(width = 92.dp, height = 83.dp))
                Text(clockPadded(remaining), style = PoulpyTextStyles.Timer)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PoulpySpace.md)) {
            AdjustButton(stringResource(R.string.player_restLess, REST_STEP), enabled = !paused) { model.adjustRest(-REST_STEP) }
            AdjustButton(stringResource(R.string.player_restMore, REST_STEP), enabled = !paused) { model.adjustRest(REST_STEP) }
        }
        if (paused) Text(stringResource(R.string.player_paused), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
    }

    state.nextHeadline?.let { headline ->
        Column(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(PoulpyRadius.md)).padding(horizontal = PoulpySpace.lg, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            PlayerLabel(state.nextRound?.let { stringResource(R.string.player_nextRound, it) } ?: stringResource(R.string.player_nextUp))
            Text(headline, style = PoulpyTextStyles.Headline)
            state.nextDetail?.let { Text(it, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
    Spacer(Modifier.height(PoulpySpace.lg))
    Button(
        onClick = model::skip,
        enabled = !paused,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
        modifier = Modifier.fillMaxWidth().height(64.dp),
    ) {
        Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(22.dp))
        Text(stringResource(R.string.player_skipRest), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = PoulpySpace.sm))
    }
}

/** How much the two buttons under the rest ring add or take away. */
private const val REST_STEP = 15

@Composable
private fun AdjustButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 18.dp),
        modifier = Modifier.height(48.dp),
    ) { Text(label, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
}

/** "WORK 1 · ROUND 2 OF 4": the part this step is in, with its icon. */
@Composable
private fun PartLine(state: PlayerState) {
    val part = state.partLabel ?: return
    Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        state.partKind?.let { Icon(it.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
        PlayerLabel(part, color = MaterialTheme.colorScheme.primary, size = 13.sp)
    }
}

/** The small capitals label of the mockups: the step count, "Next", "First up". */
@Composable
private fun PlayerLabel(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, size: TextUnit = TextUnit.Unspecified) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, fontSize = size, color = color, textAlign = TextAlign.Center)
}

/** The round button on a surface at the top of the player (`icon-button--filled`). */
@Composable
private fun PlayerIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onBackground),
    ) { Icon(icon, contentDescription = description, modifier = Modifier.size(22.dp)) }
}

/** The merged finished screen (`player-finished` and `player-finished-run` mockups): everything it shows is decided in [PlayerModel.finished]. */
@Composable
private fun ColumnScope.Finished(state: PlayerState, finished: FinishedScreen?, onShare: () -> Unit, onClose: () -> Unit) {
    val summary = state.summary
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Image(painterResource(PoulpyDrawing.EXHAUSTED.drawable), contentDescription = null, modifier = Modifier.size(150.dp))
        Text(stringResource(R.string.player_finished), style = MaterialTheme.typography.displaySmall)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.workoutName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            if (finished != null) {
                Text("${dayLabel(finished.startedAtMillis)} · ${time(finished.startedAtMillis)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (summary != null) {
                Text(
                    stringResource(R.string.player_summary, summary.setsDone, summary.setsPlanned, summary.durationLabel) +
                        if (summary.setsSkipped > 0) " · " + stringResource(R.string.player_summarySkipped, summary.setsSkipped) else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (finished != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                finished.stats.forEach { FinishedTile(it, Modifier.weight(1f)) }
            }
            TentacleLitCard(finished.poulpy)
        }
    }
    OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text(stringResource(R.string.shareImage_button))
    }
    Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp)) { Text(stringResource(R.string.player_close)) }
}

@Composable
private fun FinishedTile(stat: FinishedStat, modifier: Modifier) {
    OutlinedCard(modifier, colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stat.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(stat.value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
    }
}

/** The ring with the tentacle this session lit, and what Poulpy says about it. */
@Composable
private fun TentacleLitCard(status: PoulpyStatus) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(PoulpySpace.md), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TentacleRing(
                lit = status.tentaclesLit,
                on = MaterialTheme.colorScheme.primary,
                off = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp).semantics { contentDescription = status.tentaclesCaption },
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(status.title, style = MaterialTheme.typography.titleLarge)
                Text(status.line, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** A short beep for the last three seconds of a timer and a longer one when a phase starts. */
@Composable
private fun Cues(state: PlayerState) {
    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull() }
    DisposableEffect(tone) { onDispose { tone?.release() } }
    LaunchedEffect(state.phaseNumber, state.status) {
        if (state.status == PlayerStatus.RUNNING) tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
    }
    LaunchedEffect(state.remainingSeconds) {
        val left = state.remainingSeconds ?: return@LaunchedEffect
        if (state.status == PlayerStatus.RUNNING && left in 1..3) tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    }
}

private fun clock(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

private fun clockPadded(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

/** The grey Recover tag beside "Rest" or a recover step. */
@Composable
private fun RecoverTag(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** One dot per set: done ones green, the current one in the primary colour. */
@Composable
private fun SetDots(number: Int, count: Int) {
    Row(Modifier.padding(top = 10.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { index ->
            val color = when {
                index < number - 1 -> PoulpyTheme.extraColors.success
                index == number - 1 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            Box(Modifier.size(width = 28.dp, height = 8.dp).background(color, CircleShape))
        }
    }
}
