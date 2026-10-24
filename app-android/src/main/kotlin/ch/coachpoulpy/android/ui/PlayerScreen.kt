package ch.coachpoulpy.android.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.PlayerModel
import ch.coachpoulpy.shared.PlayerState
import ch.coachpoulpy.shared.PlayerStatus

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

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.status) {
                PlayerStatus.READY -> Ready(state, onStart = model::start, onClose = onClose)
                PlayerStatus.FINISHED -> Finished(state, onClose = onClose)
                PlayerStatus.RUNNING, PlayerStatus.PAUSED -> Running(state, model)
            }
        }
    }
}

@Composable
private fun ColumnScope.Ready(state: PlayerState, onStart: () -> Unit, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_back)) }
    }
    Spacer(Modifier.weight(1f))
    Text(state.workoutName, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    Text(stringResource(R.string.player_ready), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    state.pattern?.let { PatternBadge(it, size = 120.dp, modifier = Modifier.padding(32.dp)) }
    Text(state.title, style = MaterialTheme.typography.titleLarge)
    state.targetLabel?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
    Spacer(Modifier.weight(1f))
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Text(stringResource(R.string.player_start), modifier = Modifier.padding(start = 8.dp), fontSize = 18.sp)
    }
}

@Composable
private fun ColumnScope.Running(state: PlayerState, model: PlayerModel) {
    var confirmStop by remember { mutableStateOf(false) }
    val paused = state.status == PlayerStatus.PAUSED

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { confirmStop = true }) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.player_stop)) }
        Text(
            stringResource(R.string.player_step, state.phaseNumber, state.phaseCount),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
    LinearProgressIndicator(
        progress = { (state.phaseNumber - 1).toFloat() / state.phaseCount.coerceAtLeast(1) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )

    Spacer(Modifier.weight(1f))
    if (!state.isRest) state.pattern?.let { PatternBadge(it, size = 96.dp, modifier = Modifier.padding(bottom = 16.dp)) }
    Text(
        state.title,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        color = if (state.isRest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    if (!state.isRest) {
        if (state.setCount > 1) Text(stringResource(R.string.player_set, state.setNumber, state.setCount), style = MaterialTheme.typography.titleMedium)
        state.sideLabel?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
    }

    val remaining = state.remainingSeconds
    val big = when {
        remaining != null -> clock(remaining)
        else -> state.targetLabel ?: ""
    }
    Text(big, fontSize = if (remaining != null) 88.sp else 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
    if (remaining != null && state.totalSeconds != null && state.totalSeconds!! > 0) {
        LinearProgressIndicator(
            progress = { 1f - remaining.toFloat() / state.totalSeconds!! },
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
    if (!state.isRest) state.loadLabel?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
    if (paused) Text(stringResource(R.string.player_paused), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)

    Spacer(Modifier.weight(1f))
    if (state.isRest || remaining != null) {
        state.nextTitle?.let { next ->
            Text(
                stringResource(R.string.player_next, next, state.nextDetail ?: ""),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { if (paused) model.resume() else model.pause() }, modifier = Modifier.weight(1f).height(56.dp)) {
            Icon(if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause, contentDescription = null)
            Text(stringResource(if (paused) R.string.player_resume else R.string.player_pause), modifier = Modifier.padding(start = 4.dp))
        }
        FilledTonalButton(onClick = model::skip, enabled = !paused, modifier = Modifier.weight(1f).height(56.dp)) {
            Icon(Icons.Filled.SkipNext, contentDescription = null)
            Text(stringResource(if (state.isRest) R.string.player_skip_rest else R.string.player_skip), modifier = Modifier.padding(start = 4.dp))
        }
    }
    if (!state.isTimed) {
        Button(onClick = model::done, enabled = !paused, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(72.dp)) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Text(stringResource(R.string.player_done), fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text(stringResource(R.string.player_stop_title)) },
            text = { Text(stringResource(R.string.player_stop_body)) },
            confirmButton = { TextButton(onClick = { confirmStop = false; model.stop() }) { Text(stringResource(R.string.player_stop)) } },
            dismissButton = { TextButton(onClick = { confirmStop = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun ColumnScope.Finished(state: PlayerState, onClose: () -> Unit) {
    val summary = state.summary
    Spacer(Modifier.weight(1f))
    Text(stringResource(R.string.player_finished), style = MaterialTheme.typography.displaySmall)
    Text(state.workoutName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
    if (summary != null) {
        Text(
            stringResource(R.string.player_summary, summary.setsDone, summary.setsPlanned, summary.durationLabel),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        if (summary.setsSkipped > 0) Text(stringResource(R.string.player_summary_skipped, summary.setsSkipped), style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(stringResource(R.string.player_close)) }
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
