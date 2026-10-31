package ch.coachpoulpy.device.ftms

import com.juul.kable.CentralManager

private var restorationEnabled = false

/**
 * Gives Kable's central a restore identifier, so iOS relaunches the app in the background for a connected
 * device's data after the system closed it, and a Live recording keeps its samples. Call once at launch, before
 * any Bluetooth use. Kable restores nothing itself: the added devices reconnect as at any launch.
 */
fun enableBluetoothStateRestoration() {
    if (restorationEnabled) return
    restorationEnabled = true
    CentralManager.configure { stateRestoration = true }
}
