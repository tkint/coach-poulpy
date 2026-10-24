package ch.coachpoulpy.android.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Android 12 split Bluetooth into its own permissions; before that, scanning needed location. */
private val bluetoothPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

/** Runs Bluetooth work once it may: [whenReady] asks for the permissions, then to turn Bluetooth on, if needed. */
class BluetoothGate(
    val permissionDenied: Boolean,
    val bluetoothOff: Boolean,
    /** True when Bluetooth is allowed and on, so work can start without asking anything. */
    val isReady: Boolean,
    val whenReady: (() -> Unit) -> Unit,
)

@Composable
fun rememberBluetoothGate(): BluetoothGate {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    var bluetoothOff by remember { mutableStateOf(false) }
    val enableLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        bluetoothOff = !isBluetoothOn(context)
        if (!bluetoothOff) pending?.invoke()
    }
    val turnOnThen = { action: () -> Unit ->
        if (isBluetoothOn(context)) {
            bluetoothOff = false
            action()
        } else {
            pending = action
            enableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        permissionDenied = results.values.any { !it }
        if (!permissionDenied) pending?.let { turnOnThen(it) }
    }
    return BluetoothGate(
        permissionDenied = permissionDenied,
        bluetoothOff = bluetoothOff,
        isReady = hasPermissions(context) && isBluetoothOn(context),
        whenReady = { action ->
            val missing = bluetoothPermissions.filter { context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            if (missing.isEmpty()) {
                turnOnThen(action)
            } else {
                pending = action
                permissionLauncher.launch(missing.toTypedArray())
            }
        },
    )
}

private fun hasPermissions(context: Context) =
    bluetoothPermissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

private fun isBluetoothOn(context: Context): Boolean =
    context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
