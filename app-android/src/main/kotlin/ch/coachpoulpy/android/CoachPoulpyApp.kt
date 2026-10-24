package ch.coachpoulpy.android

import android.app.Application
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.shared.AppGraph

class CoachPoulpyApp : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(DriverFactory(this))
    }
}
