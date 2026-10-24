package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.DriverFactory

/** Entry point for Swift, which sees it as `AppGraphIosKt.createAppGraph()`. */
fun createAppGraph(): AppGraph = AppGraph(DriverFactory())
