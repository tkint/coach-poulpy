package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.device.ftms.KableCentral
import ch.coachpoulpy.types.conditioning.ConditioningModule
import ch.coachpoulpy.types.mobility.MobilityModule
import ch.coachpoulpy.types.rehab.RehabModule
import ch.coachpoulpy.types.strength.StrengthModule
import ch.coachpoulpy.types.treadmill.TreadmillModule

/**
 * Wires the app together once at start-up. Android builds it in its Application,
 * iOS in the App struct. Guest mode: nothing here talks to the network.
 */
class AppGraph(driverFactory: DriverFactory) {
    val registry = WorkoutTypeRegistry(listOf(StrengthModule, ConditioningModule, RehabModule, MobilityModule, TreadmillModule))
    val compiler = TimelineCompiler(registry)
    val catalog: Catalog get() = Catalog.bundled
    val store = LocalStore(driverFactory)

    fun exerciseList(languageCode: String) = ExerciseListModel(catalog, languageCode)
    fun today() = TodayModel(store)
    fun treadmill() = TreadmillModel(KableCentral(), store, compiler)
}
