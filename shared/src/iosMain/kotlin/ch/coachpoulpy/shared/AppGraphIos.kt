package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.DriverFactory

/** Entry point for Swift: `IosAppGraphFactory().create()`. */
class IosAppGraphFactory {
    fun create(): AppGraph = AppGraph(DriverFactory())
}
