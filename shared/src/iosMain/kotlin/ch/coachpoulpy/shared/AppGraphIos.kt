package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.device.ftms.enableBluetoothStateRestoration

/** Entry point for Swift: `IosAppGraphFactory().create()`. */
class IosAppGraphFactory {
    fun create(): AppGraph {
        // Before anything opens Bluetooth: the central can only be configured before its first use.
        enableBluetoothStateRestoration()
        return AppGraph(DriverFactory())
    }
}
