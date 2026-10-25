package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BeltStatus
import ch.coachpoulpy.shared.LiveConnection
import ch.coachpoulpy.shared.LiveMetric
import ch.coachpoulpy.shared.LiveModel
import ch.coachpoulpy.shared.LiveTile
import ch.coachpoulpy.shared.LiveUiState
import ch.coachpoulpy.shared.Measure
import ch.coachpoulpy.shared.RecordingUi
import ch.coachpoulpy.shared.SummaryUi
import ch.coachpoulpy.shared.UnitSystem
import ch.coachpoulpy.shared.Units
import java.util.Locale

/** The Live tab: every connected device's numbers together, the recorder under them, all on one screen. */
@Composable
fun LiveScreen(graph: AppGraph, language: String, onManageDevices: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val memory = remember(context) { AppPreferences(context) }
    // Shared by the whole app: leaving the tab keeps the devices connected and the recording going.
    val model = remember(graph) { graph.live(memory, language) }
    val devices = remember(graph) { graph.devices(memory) }
    val state by model.state.collectAsStateWithLifecycle()
    val bluetooth = rememberBluetoothGate()

    // The phone stays awake while recording, as it sits on the treadmill's console.
    val view = LocalView.current
    DisposableEffect(state.recording != null) {
        view.keepScreenOn = state.recording != null
        onDispose { view.keepScreenOn = false }
    }
    // Opening the tab connects the added devices, without asking for anything.
    LaunchedEffect(devices) { if (bluetooth.isReady) devices.connectAutomatically() }

    Column(
        modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = PoulpySpace.lg, bottom = PoulpySpace.md),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.tab_live), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            ConnectedChip(state.connectedCount, onManageDevices)
        }
        if (state.connection == LiveConnection.EMPTY && state.recording == null) {
            NothingConnected(onManageDevices, Modifier.weight(1f))
            return@Column
        }
        // Added devices still connecting: the view shows read-only, with Poulpy where the controls go.
        val connecting = state.connection == LiveConnection.CONNECTING && state.recording == null
        if (state.belt == BeltStatus.PAUSED || state.belt == BeltStatus.STOPPED) BeltBanner(state.belt)
        if (state.tiles.isNotEmpty()) LivePanel(state.tiles) else if (state.connectedCount > 0) {
            Text(stringResource(R.string.live_waiting), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (connecting) Connecting(state.connecting) else Recorder(state, model)
        if (state.hasTreadmill && !connecting) SyncRow(state.syncPause, model::setSyncPause)
    }
    state.summary?.let { SummaryDialog(it, state.units, onDismiss = model::clearSummary) }
}

@Composable
private fun ConnectedChip(count: Int, onClick: () -> Unit) {
    val success = PoulpyTheme.extraColors.success
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.height(36.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (count > 0) success else MaterialTheme.colorScheme.outline))
            Spacer(Modifier.size(6.dp))
            Text(
                if (count > 0) pluralStringResource(R.plurals.live_connected, count, count) else stringResource(R.string.live_none_connected),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun NothingConnected(onManageDevices: () -> Unit, modifier: Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.lg), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            Image(painterResource(R.drawable.poulpy_pose_standing_tall), contentDescription = null, modifier = Modifier.size(140.dp))
            Text(stringResource(R.string.live_empty_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(stringResource(R.string.live_empty_body), textAlign = TextAlign.Center)
            Button(onClick = onManageDevices, modifier = Modifier.padding(top = 8.dp).height(52.dp)) {
                Text(stringResource(R.string.live_manage_devices))
            }
        }
    }
}

@Composable
private fun Connecting(names: List<String>) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PoulpyLoader(size = 48.dp)
            Text(
                if (names.size == 1) stringResource(R.string.live_connecting_one, names.single()) else stringResource(R.string.live_connecting_many),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BeltBanner(belt: BeltStatus) {
    val extras = PoulpyTheme.extraColors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(extras.grumpyContainer).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Pause, contentDescription = null, tint = extras.grumpy, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(10.dp))
        Text(
            stringResource(if (belt == BeltStatus.PAUSED) R.string.treadmill_belt_paused else R.string.treadmill_belt_stopped),
            color = extras.onGrumpyContainer,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** Speed and pace large, the rest two by two. Six small tiles at most, so the screen never scrolls. */
@Composable
private fun LivePanel(tiles: List<LiveTile>) {
    val big = tiles.filter { it.metric == LiveMetric.SPEED || it.metric == LiveMetric.PACE }
    val small = (tiles - big.toSet()).take(6)
    Surface(shape = RoundedCornerShape(PoulpyRadius.lg), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (big.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    big.forEach { BigTile(it, Modifier.weight(1f)) }
                }
            }
            small.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { SmallTile(it, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BigTile(tile: LiveTile, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(PoulpyRadius.md), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(stringResource(tile.metric.label).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
            Text(tile.formatted(), fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(tile.unit, style = MaterialTheme.typography.labelLarge)
            tile.source?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SmallTile(tile: LiveTile, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                listOfNotNull(stringResource(tile.metric.label), tile.source).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${tile.formatted()} ${tile.unit}".trim(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun Recorder(state: LiveUiState, model: LiveModel) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val recording = state.recording
            if (recording == null) ChoicePicker(state, model) else Recording(recording, state.units, model)
        }
    }
}

@Composable
private fun ChoicePicker(state: LiveUiState, model: LiveModel) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Text(
        stringResource(R.string.live_what_doing).uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelMedium,
        color = colors.onSurfaceVariant,
        modifier = Modifier.semantics { heading() },
    )
    // One field showing the choice; the sheet holds the recent ones and every other.
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(PoulpyRadius.md))
            .border(1.5.dp, colors.outline, RoundedCornerShape(PoulpyRadius.md))
            .clickable(onClickLabel = stringResource(R.string.live_change)) { picking = true }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(state.choice?.name.orEmpty(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
            val note = state.choice?.note
            if (!note.isNullOrEmpty()) {
                Text(note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = colors.onSurfaceVariant)
    }
    Button(onClick = { model.startRecording(state.selected) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        Text(stringResource(R.string.treadmill_start))
    }
    if (picking) {
        ChoiceSheet(state, onPick = { model.select(it); picking = false }, onDismiss = { picking = false })
    }
}

/** The recent choices, every treadmill walk and run, then the workouts, one tap to pick. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceSheet(state: LiveUiState, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp).selectableGroup()) {
            Text(stringResource(R.string.live_what_doing), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            listOf(
                R.string.live_group_recent to state.recent,
                R.string.live_group_walking to state.walking,
                R.string.live_group_running to state.running,
                R.string.live_group_workouts to state.workouts,
            ).forEach { (title, choices) ->
                if (choices.isEmpty()) return@forEach
                Text(
                    stringResource(title).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).semantics { heading() },
                )
                choices.forEach { choice ->
                    val on = choice.key == state.selected
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(PoulpyRadius.md))
                            .background(if (on) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .selectable(selected = on, role = Role.RadioButton) { onPick(choice.key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(choice.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
                            if (choice.note.isNotEmpty()) {
                                Text(choice.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        RadioButton(selected = on, onClick = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun Recording(recording: RecordingUi, units: UnitSystem, model: LiveModel) {
    val statusColor = if (recording.paused) PoulpyTheme.extraColors.grumpy else MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(clock(recording.elapsedSeconds), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
        Spacer(Modifier.size(6.dp))
        Text(
            stringResource(if (recording.paused) R.string.treadmill_paused else R.string.live_recording),
            color = statusColor,
            fontWeight = FontWeight.ExtraBold,
        )
    }
    if (!recording.freeRun) {
        val parts = listOfNotNull(
            stringResource(R.string.treadmill_phase, recording.phaseNumber, recording.phaseCount),
            if (recording.isRest) stringResource(R.string.treadmill_rest) else null,
            recording.targetSpeedKmh?.let { stringResource(R.string.treadmill_target_speed, measure(Units.speed(it, units))) },
            recording.targetDistanceMeters?.let { stringResource(R.string.treadmill_target_distance, it.toInt()) },
        )
        Row {
            Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            recording.phaseRemainingSeconds?.let { Text(stringResource(R.string.treadmill_remaining, clock(it)), style = MaterialTheme.typography.bodyMedium) }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(
            onClick = if (recording.paused) model::resumeRecording else model::pauseRecording,
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                if (recording.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = stringResource(if (recording.paused) R.string.treadmill_resume else R.string.treadmill_pause),
            )
        }
        if (!recording.freeRun) {
            OutlinedButton(onClick = model::skipPhase, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.treadmill_skip)) }
        }
        Button(onClick = model::stopRecording, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.treadmill_stop)) }
    }
}

@Composable
private fun SyncRow(on: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(PoulpyRadius.md),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(12.dp))
            Text(stringResource(R.string.live_sync_belt), fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Switch(checked = on, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun SummaryDialog(summary: SummaryUi, units: UnitSystem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_done)) } },
        title = { Text(stringResource(R.string.treadmill_summary_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SummaryLine(R.string.treadmill_summary_duration, clock(summary.durationSeconds))
                summary.distanceMeters?.let { SummaryLine(R.string.metric_distance, measure(Units.distance(it, units))) }
                summary.averageSpeedKmh?.let { SummaryLine(R.string.treadmill_summary_average_speed, measure(Units.speed(it, units))) }
                summary.averageHeartRateBpm?.let { SummaryLine(R.string.treadmill_summary_average_hr, "$it bpm") }
                summary.maxHeartRateBpm?.let { SummaryLine(R.string.treadmill_summary_max_hr, "$it bpm") }
                summary.energyKcal?.let { SummaryLine(R.string.metric_energy, "$it kcal") }
                summary.estimatedSteps?.let { SummaryLine(R.string.metric_steps, "≈ $it") }
                if (summary.phases.size > 1) {
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    summary.phases.forEach { phase ->
                        val parts = listOfNotNull(
                            clock(phase.durationSeconds),
                            phase.averageSpeedKmh?.let { measure(Units.speed(it, units)) },
                            phase.onTargetPercent?.let { stringResource(R.string.treadmill_summary_on_target, it) },
                        )
                        val title = if (phase.isRest) stringResource(R.string.treadmill_rest) else "#${phase.phaseNumber}"
                        Text("$title  ${parts.joinToString(" · ")}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
    )
}

@Composable
private fun SummaryLine(@StringRes label: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

private val LiveMetric.label: Int
    get() = when (this) {
        LiveMetric.SPEED -> R.string.metric_speed
        LiveMetric.PACE -> R.string.metric_pace
        LiveMetric.DISTANCE -> R.string.metric_distance
        LiveMetric.HEART_RATE -> R.string.metric_heart_rate
        LiveMetric.CADENCE -> R.string.metric_cadence
        LiveMetric.INCLINE -> R.string.metric_incline
        LiveMetric.ENERGY -> R.string.metric_energy
        LiveMetric.POWER -> R.string.metric_power
        LiveMetric.STEPS_ESTIMATED -> R.string.metric_steps
    }

/** A missing value keeps its tile with a dash; a pace dash keeps the shape of a time. */
private fun LiveTile.formatted(): String {
    val v = value ?: return if (metric.isTime) "–:––" else "–"
    val text = if (metric.isTime) clock(v.toLong()) else decimal(v, decimals)
    return if (metric == LiveMetric.STEPS_ESTIMATED) "≈ $text" else text
}

private fun measure(m: Measure) = "${decimal(m.value, m.decimals)} ${m.unit}"

private fun decimal(value: Double, decimals: Int) = String.format(Locale.getDefault(), "%.${decimals}f", value)

private fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
