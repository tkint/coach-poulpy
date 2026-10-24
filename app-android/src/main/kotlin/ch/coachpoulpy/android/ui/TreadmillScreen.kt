package ch.coachpoulpy.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.ConnectionStatus
import ch.coachpoulpy.shared.LiveMetric
import ch.coachpoulpy.shared.LiveTile
import ch.coachpoulpy.shared.PlanOption
import ch.coachpoulpy.shared.RecordingUi
import ch.coachpoulpy.shared.SummaryUi
import ch.coachpoulpy.shared.TreadmillModel
import ch.coachpoulpy.shared.TreadmillUiState
import java.util.Locale

/** Android 12 split Bluetooth into its own permissions; before that, scanning needed location. */
private val bluetoothPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

@Composable
fun TreadmillScreen(graph: AppGraph, modifier: Modifier = Modifier) {
    val model = remember(graph) { graph.treadmill() }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()

    // The phone stays awake while recording, as it sits on the treadmill's console.
    val view = LocalView.current
    DisposableEffect(state.recording != null) {
        view.keepScreenOn = state.recording != null
        onDispose { view.keepScreenOn = false }
    }

    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        permissionDenied = results.values.any { !it }
        if (!permissionDenied) model.startScan()
    }
    val scan = {
        val missing = bluetoothPermissions.filter { context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isEmpty()) model.startScan() else permissionLauncher.launch(missing.toTypedArray())
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ConnectionCard(state, model, scan, permissionDenied)
        if (state.connection == ConnectionStatus.IDLE || state.connection == ConnectionStatus.FAILED) {
            DeviceList(state, model)
        }
        if (state.connection == ConnectionStatus.CONNECTED || state.connection == ConnectionStatus.RECONNECTING) {
            LiveTiles(state.tiles)
        }
        val recording = state.recording
        if (recording != null) RecordingCard(recording, model) else RecordCard(state.plans, model)
        state.summary?.let { SummaryCard(it) }
        if (state.supportedMetrics.isNotEmpty() || state.malformedPackets > 0) {
            Text(
                stringResource(R.string.treadmill_machine_reports, state.supportedMetrics.joinToString(", ")) +
                    if (state.malformedPackets > 0) "\n" + stringResource(R.string.treadmill_malformed, state.malformedPackets) else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConnectionCard(state: TreadmillUiState, model: TreadmillModel, scan: () -> Unit, permissionDenied: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val name = state.deviceName.orEmpty()
            Text(
                when (state.connection) {
                    ConnectionStatus.IDLE -> stringResource(R.string.treadmill_status_idle)
                    ConnectionStatus.CONNECTING -> stringResource(R.string.treadmill_status_connecting, name)
                    ConnectionStatus.CONNECTED -> stringResource(R.string.treadmill_status_connected, name)
                    ConnectionStatus.RECONNECTING -> stringResource(R.string.treadmill_status_reconnecting, name)
                    ConnectionStatus.FAILED -> stringResource(R.string.treadmill_status_failed)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (permissionDenied) Text(stringResource(R.string.treadmill_permission_needed), color = MaterialTheme.colorScheme.error)
            state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            when {
                state.connection == ConnectionStatus.CONNECTING ||
                    state.connection == ConnectionStatus.CONNECTED ||
                    state.connection == ConnectionStatus.RECONNECTING ->
                    OutlinedButton(onClick = model::disconnect) { Text(stringResource(R.string.treadmill_disconnect)) }
                state.scanning -> OutlinedButton(onClick = model::stopScan) { Text(stringResource(R.string.treadmill_stop_scan)) }
                else -> Button(onClick = scan) { Text(stringResource(R.string.treadmill_scan)) }
            }
        }
    }
}

@Composable
private fun DeviceList(state: TreadmillUiState, model: TreadmillModel) {
    if (state.scanning && state.devices.isEmpty()) Text(stringResource(R.string.treadmill_scanning))
    Column {
        state.devices.forEach { device ->
            ListItem(
                headlineContent = { Text(device.name) },
                supportingContent = { if (device.advertisesFtms) Text(stringResource(R.string.treadmill_ftms_badge)) },
                trailingContent = { Text("${device.rssi} dBm", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.clickable { model.connect(device.key) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun LiveTiles(tiles: List<LiveTile>) {
    if (tiles.isEmpty()) {
        Text(stringResource(R.string.treadmill_waiting_data), style = MaterialTheme.typography.bodyMedium)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tile ->
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(stringResource(tile.metric.label), style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${tile.formatted()} ${tile.metric.unit}".trim(),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecordCard(plans: List<PlanOption>, model: TreadmillModel) {
    var selected by rememberSaveable { mutableStateOf("free-run") }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.treadmill_record_title), style = MaterialTheme.typography.titleMedium)
            plans.forEach { plan ->
                Row(
                    Modifier.fillMaxWidth().selectable(selected = selected == plan.key, onClick = { selected = plan.key }),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == plan.key, onClick = { selected = plan.key })
                    Text(planName(plan))
                }
            }
            Button(onClick = { model.startRecording(selected) }, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.treadmill_start))
            }
        }
    }
}

@Composable
private fun planName(plan: PlanOption) = when (plan.key) {
    "free-run" -> stringResource(R.string.treadmill_plan_free_run)
    "demo-intervals" -> stringResource(R.string.treadmill_plan_demo)
    else -> plan.name
}

@Composable
private fun RecordingCard(recording: RecordingUi, model: TreadmillModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.treadmill_elapsed, clock(recording.elapsedSeconds)), style = MaterialTheme.typography.headlineSmall)
            if (recording.paused) Text(stringResource(R.string.treadmill_paused), color = MaterialTheme.colorScheme.error)
            if (!recording.freeRun) {
                Text(stringResource(R.string.treadmill_phase, recording.phaseNumber, recording.phaseCount), style = MaterialTheme.typography.titleMedium)
                if (recording.isRest) Text(stringResource(R.string.treadmill_rest))
                recording.targetSpeedKmh?.let { Text(stringResource(R.string.treadmill_target_speed, decimal(it, 1))) }
                recording.targetDistanceMeters?.let { Text(stringResource(R.string.treadmill_target_distance, it.toInt())) }
                recording.phaseRemainingSeconds?.let { Text(stringResource(R.string.treadmill_remaining, clock(it))) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (recording.paused) {
                    OutlinedButton(onClick = model::resumeRecording) { Text(stringResource(R.string.treadmill_resume)) }
                } else {
                    OutlinedButton(onClick = model::pauseRecording) { Text(stringResource(R.string.treadmill_pause)) }
                }
                if (!recording.freeRun) OutlinedButton(onClick = model::skipPhase) { Text(stringResource(R.string.treadmill_skip)) }
                Button(onClick = model::stopRecording) { Text(stringResource(R.string.treadmill_stop)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: SummaryUi) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.treadmill_summary_title), style = MaterialTheme.typography.titleMedium)
            SummaryLine(R.string.treadmill_summary_duration, clock(summary.durationSeconds))
            summary.distanceMeters?.let { SummaryLine(R.string.metric_distance, "${decimal(it / 1000, 2)} km") }
            summary.averageSpeedKmh?.let { SummaryLine(R.string.treadmill_summary_average_speed, "${decimal(it, 1)} km/h") }
            summary.averageHeartRateBpm?.let { SummaryLine(R.string.treadmill_summary_average_hr, "$it bpm") }
            summary.maxHeartRateBpm?.let { SummaryLine(R.string.treadmill_summary_max_hr, "$it bpm") }
            summary.energyKcal?.let { SummaryLine(R.string.metric_energy, "$it kcal") }
            if (summary.phases.size > 1) {
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                summary.phases.forEach { phase ->
                    val parts = listOfNotNull(
                        clock(phase.durationSeconds),
                        phase.distanceMeters?.let { "${it.toInt()} m" },
                        phase.averageSpeedKmh?.let { "${decimal(it, 1)} km/h" },
                        phase.targetSpeedKmh?.let { "→ ${decimal(it, 1)}" },
                        phase.onTargetPercent?.let { stringResource(R.string.treadmill_summary_on_target, it) },
                    )
                    val title = if (phase.isRest) stringResource(R.string.treadmill_rest) else "#${phase.phaseNumber}"
                    Text("$title  ${parts.joinToString(" · ")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(@StringRes label: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label))
        Text(value)
    }
}

private val LiveMetric.label: Int
    get() = when (this) {
        LiveMetric.SPEED -> R.string.metric_speed
        LiveMetric.INCLINE -> R.string.metric_incline
        LiveMetric.DISTANCE_KM -> R.string.metric_distance
        LiveMetric.HEART_RATE -> R.string.metric_heart_rate
        LiveMetric.ENERGY -> R.string.metric_energy
        LiveMetric.MACHINE_TIME -> R.string.metric_machine_time
        LiveMetric.POWER -> R.string.metric_power
    }

private val LiveMetric.unit: String
    get() = when (this) {
        LiveMetric.SPEED -> "km/h"
        LiveMetric.INCLINE -> "%"
        LiveMetric.DISTANCE_KM -> "km"
        LiveMetric.HEART_RATE -> "bpm"
        LiveMetric.ENERGY -> "kcal"
        LiveMetric.MACHINE_TIME -> ""
        LiveMetric.POWER -> "W"
    }

private fun LiveTile.formatted() = if (metric == LiveMetric.MACHINE_TIME) clock(value.toLong()) else decimal(value, decimals)

private fun decimal(value: Double, decimals: Int) = String.format(Locale.getDefault(), "%.${decimals}f", value)

private fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
