package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.LiveModel
import ch.coachpoulpy.shared.LiveUiState
import ch.coachpoulpy.shared.PlayerState
import ch.coachpoulpy.shared.PlayerStatus
import ch.coachpoulpy.shared.PoulpyDrawing
import ch.coachpoulpy.shared.RecordingUi
import ch.coachpoulpy.shared.StepWait

/**
 * A workout that mixes walks and runs with other exercises, played from Live (#82): the workout player's steps full
 * screen, with Live's numbers on top. A walk or run waits for the belt, or for Start without a treadmill; a strength
 * step shows only what the phone is streamed, like the heart rate (`live-mixed-player-*` mockups).
 */
@Composable
internal fun LiveWorkoutPlayer(state: LiveUiState, recording: RecordingUi, player: PlayerState, model: LiveModel, modifier: Modifier = Modifier) {
    var confirmStop by remember { mutableStateOf(false) }
    val waiting = recording.wait != StepWait.NOT_WAITING
    // A step waiting to begin is paused by the app, not by the user: Start is its button, not the pause one.
    val paused = player.status == PlayerStatus.PAUSED && !waiting
    Cues(player)

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayerBar(
                player,
                paused,
                onToggle = { if (paused) model.resumeRecording() else model.pauseRecording() },
                onEnd = { confirmStop = true },
                canToggle = !waiting,
            )
            if (state.tiles.isNotEmpty()) {
                Spacer(Modifier.height(PoulpySpace.md))
                LivePanel(state.tiles)
            }
            when {
                waiting -> WaitingStep(player, recording.wait, onSkip = model::skipPhase, onStart = model::startStep)
                player.isRest -> RestStep(player, paused, onSkip = model::skipPhase, onAdjust = model::adjustRest)
                else -> WorkStep(player, paused, onSkip = model::skipPhase, onDone = model::completeStep)
            }
            state.finishPrompt?.let {
                Spacer(Modifier.height(PoulpySpace.md))
                FinishCard(it, model)
            }
        }
    }
    if (confirmStop) StopDialog(onConfirm = { confirmStop = false; model.stopRecording() }, onDismiss = { confirmStop = false })
}

/** A walk or run that has not begun: waiting for the belt to move, or for Start when there is no treadmill. */
@Composable
private fun ColumnScope.WaitingStep(state: PlayerState, wait: StepWait, onSkip: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(
            painterResource(PoulpyDrawing.STRAINING.drawable),
            contentDescription = null,
            modifier = Modifier.padding(bottom = PoulpySpace.md).size(width = 130.dp, height = 117.dp),
        )
        PartLine(state)
        Text(state.title, style = PoulpyTextStyles.Title, textAlign = TextAlign.Center)
        Spacer(Modifier.height(PoulpySpace.md))
        if (wait == StepWait.FOR_BELT) {
            Column(
                Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.live_stepWaitTitle), style = PoulpyTextStyles.Headline, textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.live_stepWaitBody),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Text(
                stringResource(R.string.live_stepNoTreadmill),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(PoulpySpace.md))
        Text(
            state.targetLabel.orEmpty(),
            style = PoulpyTextStyles.Timer.copy(fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-0.02).em),
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(PoulpySpace.lg))
    Row(horizontalArrangement = Arrangement.spacedBy(PoulpySpace.md), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onSkip,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.height(64.dp),
        ) { Text(stringResource(R.string.player_skip), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
        Button(onClick = onStart, modifier = Modifier.weight(1f).height(64.dp)) {
            Text(
                stringResource(if (wait == StepWait.FOR_BELT) R.string.live_startWithoutIt else R.string.action_start),
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}
