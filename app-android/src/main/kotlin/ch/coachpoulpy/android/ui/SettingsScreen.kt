package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.BuildConfig
import ch.coachpoulpy.android.R
import ch.coachpoulpy.device.ftms.DeviceKind
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.ConnectionStatus
import ch.coachpoulpy.shared.DevicesModel
import ch.coachpoulpy.shared.KnownDeviceUi
import ch.coachpoulpy.shared.LanguageChoice
import ch.coachpoulpy.shared.Quantity
import ch.coachpoulpy.shared.ThemeChoice
import ch.coachpoulpy.shared.UnitSystem

enum class SettingsSection { GENERAL, DEVICES }

/** The Settings tab: General first, then Devices. Device pages open over the tabs through [onOpen]. */
@Composable
fun SettingsScreen(
    graph: AppGraph,
    section: SettingsSection,
    onSection: (SettingsSection) -> Unit,
    onOpen: (DeviceRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier.padding(start = 20.dp, end = 12.dp, top = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                if (section == SettingsSection.DEVICES) {
                    FilledTonalIconButton(onClick = { onOpen(DeviceRoute.Add) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.devices_add))
                    }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }
            Box(Modifier.padding(end = 8.dp)) { SectionSwitch(section, onSection) }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = PoulpySpace.lg, bottom = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (section) {
                SettingsSection.GENERAL -> General(graph)
                SettingsSection.DEVICES -> Devices(graph, onOpen)
            }
        }
    }
}

/** General | Devices, as a pill with the chosen half raised, like History's switch. */
@Composable
private fun SectionSwitch(selected: SettingsSection, onSelect: (SettingsSection) -> Unit) {
    val pill = RoundedCornerShape(PoulpyRadius.pill)
    Row(Modifier.fillMaxWidth().clip(pill).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp).selectableGroup()) {
        listOf(SettingsSection.GENERAL to R.string.settings_general, SettingsSection.DEVICES to R.string.settings_devices).forEach { (item, label) ->
            val on = item == selected
            val color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(pill)
                    .background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(item) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(label), color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ColumnScope.General(graph: AppGraph) {
    val context = LocalContext.current
    val model = remember(graph) { graph.settings(AppPreferences(context)) }
    val state by model.state.collectAsStateWithLifecycle()

    SectionLabel(R.string.settings_appearance)
    ChoiceCard(
        title = R.string.settings_theme,
        options = listOf(ThemeChoice.SYSTEM to stringResource(R.string.theme_system), ThemeChoice.LIGHT to stringResource(R.string.theme_light), ThemeChoice.DARK to stringResource(R.string.theme_dark)),
        selected = state.theme,
        onSelect = model::setTheme,
        hint = R.string.settings_themeHint,
    )
    SectionLabel(R.string.settings_region)
    // Language names stay in their own language, so anyone can find theirs.
    ChoiceCard(
        title = R.string.settings_language,
        options = listOf(LanguageChoice.SYSTEM to stringResource(R.string.language_system), LanguageChoice.ENGLISH to "English", LanguageChoice.FRENCH to "Français"),
        selected = state.language,
        onSelect = model::setLanguage,
    )
    ChoiceCard(
        title = R.string.settings_units,
        options = listOf(UnitSystem.METRIC to stringResource(R.string.units_metric), UnitSystem.IMPERIAL to stringResource(R.string.units_imperial)),
        selected = state.units,
        onSelect = model::setUnits,
        hint = R.string.settings_unitsHint,
    )
    Text(
        stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
    )
}

@Composable
private fun <T> ChoiceCard(@StringRes title: Int, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, @StringRes hint: Int? = null) {
    OutlinedSurface {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), fontWeight = FontWeight.ExtraBold)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (value, label) ->
                    FilterChip(
                        selected = value == selected,
                        onClick = { onSelect(value) },
                        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            hint?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun ColumnScope.Devices(graph: AppGraph, onOpen: (DeviceRoute) -> Unit) {
    val context = LocalContext.current
    val model = remember(graph) { graph.devices(AppPreferences(context)) }
    val state by model.state.collectAsStateWithLifecycle()
    val bluetooth = rememberBluetoothGate()
    LaunchedEffect(model) { if (bluetooth.isReady) model.connectAutomatically() }

    BluetoothProblems(bluetooth)
    if (state.known.isEmpty()) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.lg), color = MaterialTheme.colorScheme.primaryContainer) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(painterResource(R.drawable.poulpy_pose_standing_tall), contentDescription = null, modifier = Modifier.size(140.dp))
                Text(stringResource(R.string.devices_emptyTitle), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(stringResource(R.string.devices_emptyBody), textAlign = TextAlign.Center)
                Button(onClick = { onOpen(DeviceRoute.Add) }, modifier = Modifier.padding(top = 8.dp).height(56.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.devices_addFirst))
                }
                Text(stringResource(R.string.devices_switchOn), style = MaterialTheme.typography.bodySmall)
            }
        }
        return
    }
    SectionLabel(R.string.devices_yours)
    state.known.forEach { device ->
        KnownRow(device, onClick = { onOpen(DeviceRoute.Device(device.key)) }, onConnect = { bluetooth.whenReady { model.connect(device.key) } })
    }
    if (state.sources.isNotEmpty()) {
        OutlinedSurface(onClick = { onOpen(DeviceRoute.Sources) }) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.devices_sources), fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.devices_sourcesRowHint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun KnownRow(device: KnownDeviceUi, onClick: () -> Unit, onConnect: () -> Unit) {
    OutlinedSurface(onClick = onClick) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            DeviceAvatar(device.kind, device.status == ConnectionStatus.CONNECTED)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.ExtraBold)
                StatusLine(device)
                Text(
                    device.provides.map { stringResource(it.label) }.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (device.status == ConnectionStatus.IDLE || device.status == ConnectionStatus.FAILED) {
                FilledTonalButton(onClick = onConnect) { Text(stringResource(R.string.devices_connect)) }
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusLine(device: KnownDeviceUi) {
    val connected = device.status == ConnectionStatus.CONNECTED
    val color = if (connected) PoulpyTheme.extraColors.success else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (device.status == ConnectionStatus.CONNECTING || device.status == ConnectionStatus.RECONNECTING) {
            PoulpyLoader(size = 16.dp)
        } else {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (connected) color else MaterialTheme.colorScheme.outline))
        }
        Spacer(Modifier.size(6.dp))
        val text = listOfNotNull(
            stringResource(device.status.label),
            device.batteryPercent?.let { stringResource(R.string.devices_battery, it) },
        ).joinToString(" · ")
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun DeviceAvatar(kind: DeviceKind?, on: Boolean, size: Int = 48) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            kind.icon,
            contentDescription = null,
            tint = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The screens Settings › Devices opens over the tabs. */
sealed interface DeviceRoute {
    data object Add : DeviceRoute
    data object Sources : DeviceRoute
    data class Device(val key: String) : DeviceRoute
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { BackButton(onBack) }) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

/** Searches for devices to add, for as long as the screen is open. */
@Composable
fun DeviceAddScreen(graph: AppGraph, onBack: () -> Unit) {
    val context = LocalContext.current
    val model = remember(graph) { graph.devices(AppPreferences(context)) }
    val state by model.state.collectAsStateWithLifecycle()
    val bluetooth = rememberBluetoothGate()
    LaunchedEffect(model) { bluetooth.whenReady(model::startScan) }
    DisposableEffect(model) { onDispose { model.stopScan() } }

    PageScaffold(stringResource(R.string.devices_add), onBack) {
        BluetoothProblems(bluetooth, onAllow = { bluetooth.whenReady(model::startScan) })
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.lg), color = MaterialTheme.colorScheme.primaryContainer) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.scanning) PoulpyLoader(size = 112.dp)
                Text(stringResource(R.string.treadmill_scanning), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(stringResource(R.string.devices_scanHint), textAlign = TextAlign.Center)
                if (state.scanning) {
                    OutlinedButton(onClick = onBack) { Text(stringResource(R.string.treadmill_stopScan)) }
                }
            }
        }
        state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        if (state.found.isNotEmpty()) SectionLabel(R.string.devices_found)
        state.found.forEach { device ->
            OutlinedSurface {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    DeviceAvatar(device.kind, on = device.kind != null, size = 44)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(device.name, fontWeight = FontWeight.ExtraBold)
                        Text(stringResource(device.kind.label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { model.add(device.key); onBack() }) { Text(stringResource(R.string.devices_addOne)) }
                }
            }
        }
        Text(stringResource(R.string.devices_addedHidden), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** One added device: what it measures and which of those numbers Live uses, plus connect, auto-connect and forget. */
@Composable
fun DeviceScreen(graph: AppGraph, key: String, onBack: () -> Unit, onSources: () -> Unit) {
    val context = LocalContext.current
    val model = remember(graph) { graph.devices(AppPreferences(context)) }
    val state by model.state.collectAsStateWithLifecycle()
    val bluetooth = rememberBluetoothGate()
    val device = state.known.firstOrNull { it.key == key }
    var confirmForget by remember { mutableStateOf(false) }
    if (device == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    PageScaffold(device.name, onBack) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.lg), color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                DeviceAvatar(device.kind, device.status == ConnectionStatus.CONNECTED, size = 56)
                Spacer(Modifier.size(14.dp))
                Column {
                    Text(stringResource(device.kind.label), style = MaterialTheme.typography.titleMedium)
                    StatusLine(device)
                    device.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        SectionLabel(R.string.devices_measures)
        OutlinedSurface {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                device.provides.forEach { quantity ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(quantity.label), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (device.status == ConnectionStatus.CONNECTED) {
                            val used = quantity in device.usedFor
                            val instead = state.known.firstOrNull { it.key != device.key && quantity in it.usedFor }
                            Pill(
                                when {
                                    used -> stringResource(R.string.devices_used)
                                    instead != null -> stringResource(R.string.devices_usedInstead, instead.name)
                                    else -> null
                                },
                                highlighted = used,
                            )
                        }
                    }
                }
            }
        }
        if (state.sources.isNotEmpty()) {
            TextButton(onClick = onSources) { Text(stringResource(R.string.devices_changeSources)) }
        }
        SectionLabel(R.string.settings_devices)
        OutlinedSurface {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.devices_autoConnect), fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.devices_autoConnectHint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = device.autoConnect, onCheckedChange = { model.setAutoConnect(device.key, it) })
            }
        }
        BluetoothProblems(bluetooth)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (device.status == ConnectionStatus.IDLE || device.status == ConnectionStatus.FAILED) {
                Button(onClick = { bluetooth.whenReady { model.connect(device.key) } }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(stringResource(R.string.devices_connect))
                }
            } else {
                OutlinedButton(onClick = { model.disconnect(device.key) }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(stringResource(R.string.devices_disconnect))
                }
            }
            TextButton(onClick = { confirmForget = true }, modifier = Modifier.weight(1f).height(48.dp)) {
                Text(stringResource(R.string.devices_forget), color = PoulpyTheme.extraColors.grumpy)
            }
        }
    }
    if (confirmForget) {
        AlertDialog(
            onDismissRequest = { confirmForget = false },
            title = { Text(stringResource(R.string.devices_forgetTitle, device.name)) },
            text = { Text(stringResource(R.string.devices_forgetBody)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmForget = false
                    model.forget(device.key)
                }) { Text(stringResource(R.string.devices_forget), color = PoulpyTheme.extraColors.grumpy) }
            },
            dismissButton = { TextButton(onClick = { confirmForget = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

/** For each number two added devices measure: automatic, or one of them. */
@Composable
fun DataSourcesScreen(graph: AppGraph, onBack: () -> Unit) {
    val context = LocalContext.current
    val model = remember(graph) { graph.devices(AppPreferences(context)) }
    val state by model.state.collectAsStateWithLifecycle()

    PageScaffold(stringResource(R.string.devices_sources), onBack) {
        Text(stringResource(R.string.devices_sourcesHint), style = MaterialTheme.typography.bodyMedium)
        state.sources.forEach { source ->
            OutlinedSurface {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(source.quantity.label), fontWeight = FontWeight.ExtraBold)
                    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = source.choiceKey == null,
                            onClick = { model.setSource(source.quantity, null) },
                            label = { Text(stringResource(R.string.devices_sourceAutomatic)) },
                        )
                        source.options.forEach { option ->
                            FilterChip(
                                selected = source.choiceKey == option.key,
                                onClick = { model.setSource(source.quantity, option.key) },
                                label = { Text(option.name) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BluetoothProblems(bluetooth: BluetoothGate, onAllow: (() -> Unit)? = null) {
    if (bluetooth.bluetoothOff) {
        Text(stringResource(R.string.devices_bluetoothOff), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
    }
    if (bluetooth.permissionDenied) {
        Text(stringResource(R.string.devices_permissionNeeded), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        onAllow?.let { Button(onClick = it) { Text(stringResource(R.string.devices_allowBluetooth)) } }
    }
}

@Composable
private fun Pill(text: String?, highlighted: Boolean) {
    text ?: return
    Surface(
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun SectionLabel(@StringRes text: Int) {
    Text(
        stringResource(text).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun OutlinedSurface(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(PoulpyRadius.md)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = shape, color = MaterialTheme.colorScheme.surface, border = border, content = content)
    } else {
        Surface(Modifier.fillMaxWidth(), shape = shape, color = MaterialTheme.colorScheme.surface, border = border, content = content)
    }
}

private val ConnectionStatus.label: Int
    get() = when (this) {
        ConnectionStatus.CONNECTED -> R.string.devices_statusConnected
        ConnectionStatus.CONNECTING -> R.string.devices_statusConnecting
        ConnectionStatus.RECONNECTING -> R.string.devices_statusReconnecting
        ConnectionStatus.FAILED -> R.string.devices_statusFailed
        ConnectionStatus.IDLE -> R.string.devices_statusIdle
    }

private val Quantity.label: Int
    get() = when (this) {
        Quantity.SPEED -> R.string.quantity_speed
        Quantity.HEART_RATE -> R.string.quantity_heartRate
        Quantity.CADENCE -> R.string.quantity_cadence
        Quantity.INCLINE -> R.string.quantity_incline
        Quantity.ENERGY -> R.string.quantity_energy
        Quantity.POWER -> R.string.quantity_power
    }

private val DeviceKind?.label: Int
    get() = when (this) {
        DeviceKind.TREADMILL -> R.string.kind_treadmill
        DeviceKind.FOOT_POD -> R.string.kind_footPod
        DeviceKind.HEART_RATE -> R.string.kind_heartRate
        null -> R.string.kind_unknown
    }

private val DeviceKind?.icon: ImageVector
    get() = when (this) {
        DeviceKind.TREADMILL, DeviceKind.FOOT_POD -> Icons.AutoMirrored.Filled.DirectionsRun
        DeviceKind.HEART_RATE -> Icons.Filled.Favorite
        null -> Icons.Filled.Bluetooth
    }
