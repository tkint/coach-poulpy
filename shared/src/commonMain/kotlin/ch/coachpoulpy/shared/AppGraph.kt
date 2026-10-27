package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.device.ftms.KableCentral

/**
 * Wires the app together once at start-up. Android builds it in its Application,
 * iOS in the App struct. Guest mode: nothing here talks to the network.
 */
class AppGraph(driverFactory: DriverFactory) {
    val compiler = TimelineCompiler()
    val store = LocalStore(driverFactory)
    val catalogRepository = CatalogRepository(store, Catalog.bundled)
    val catalog: Catalog get() = catalogRepository.current

    fun exerciseList(languageCode: String) = ExerciseListModel(catalog, languageCode) { recentlyUsedExercises(store) }

    /** What the user ticked in the exercise list's Equipment sheet. The apps pass the same [memory] every time. */
    fun equipmentFilter(memory: KeyValueStore) = EquipmentFilter(memory)
    fun home(languageCode: String) = HomeModel(store, compiler, catalog, languageCode)
    fun library(languageCode: String) = LibraryModel(store, compiler, catalog, languageCode)
    fun workoutPage(workoutId: String, languageCode: String) = WorkoutPageModel(WorkoutId(workoutId), store, compiler, catalog, languageCode)
    fun history(languageCode: String) = HistoryModel(store, compiler, languageCode)

    private var settingsModel: SettingsModel? = null
    private var devicesModel: DevicesModel? = null
    private var liveModel: LiveModel? = null

    /** The app's settings, created on first use. The apps pass the same [memory] every time. */
    fun settings(memory: KeyValueStore): SettingsModel =
        settingsModel ?: SettingsModel(memory).also { settingsModel = it }

    /**
     * The one devices model of the app. Like [live], it outlives the screens so that
     * leaving a tab mid-walk keeps the devices connected and the recording going.
     */
    fun devices(memory: KeyValueStore): DevicesModel =
        devicesModel ?: DevicesModel(KableCentral(), memory).also { devicesModel = it }

    fun live(memory: KeyValueStore, languageCode: String): LiveModel =
        liveModel ?: LiveModel(devices(memory), settings(memory).state, store, compiler, memory, catalog = { catalog }, languageCode = languageCode)
            .also { liveModel = it }

    // Not "newWorkout": Swift reserves the "new" prefix, and Kotlin would rename it.
    fun createWorkout(languageCode: String) =
        BuilderModel(WorkoutDraft.empty(), catalog, store, compiler, languageCode, ::units)

    /** A new workout that holds [exerciseSlug] once its format is chosen, for an exercise page's "Add to a workout". */
    fun createWorkoutWith(exerciseSlug: String, languageCode: String) =
        BuilderModel(WorkoutDraft.empty(), catalog, store, compiler, languageCode, ::units, pendingSlug = exerciseSlug)

    /** Null when the workout is gone or was not made with the builder. */
    fun editWorkout(workoutId: String, languageCode: String): BuilderModel? {
        val workout = store.latestWorkout(WorkoutId(workoutId)) ?: return null
        val draft = WorkoutDraft.from(workout, catalog) ?: return null
        return BuilderModel(draft, catalog, store, compiler, languageCode, ::units)
    }

    /**
     * An unsaved copy of a workout made with the builder: a new id, version 1, "(copy)"
     * after the name, the same format and parts, and no days or history. Null otherwise.
     * Not "copyWorkout": Objective-C reserves the "copy" prefix, and Kotlin would rename it.
     */
    fun duplicateWorkout(workoutId: String, languageCode: String): BuilderModel? {
        val workout = store.latestWorkout(WorkoutId(workoutId)) ?: return null
        val draft = WorkoutDraft.from(workout, catalog) ?: return null
        val copy = draft.copy(id = null, version = 1, name = Labels(languageCode).copyName(workout.name))
        return BuilderModel(copy, catalog, store, compiler, languageCode, ::units)
    }

    /** The units chosen in Settings, once the app has opened them; metric until then. */
    private fun units() = settingsModel?.state?.value?.units ?: UnitSystem.METRIC

    fun deleteWorkout(workoutId: String) = store.deleteWorkout(WorkoutId(workoutId))

    /** A new guided session of the latest version of a workout, ready to start. */
    fun player(workoutId: String, languageCode: String): PlayerModel? {
        val workout = store.latestWorkout(WorkoutId(workoutId)) ?: return null
        // Only one session is open at a time: one left behind ends where it was left.
        store.unfinishedExecution()?.let { open ->
            store.finishExecution(open.id.value, store.events(open.id.value).lastOrNull()?.at ?: open.startedAt)
        }
        return player(workout, Ids.newUuidV7(), languageCode)
    }

    /** The session left open when the app was closed, if any, exactly where it was. */
    fun resumeSession(languageCode: String): PlayerModel? {
        val execution = store.unfinishedExecution() ?: return null
        val ref = execution.workout
        val workout = ref?.let { store.workout(it.workoutId, it.version) }
        if (workout == null) {
            // Its workout was deleted: close it so it stops being offered.
            store.finishExecution(execution.id.value, execution.startedAt)
            return null
        }
        return player(workout, execution.id.value, languageCode)
    }

    private fun player(workout: Workout, executionId: String, languageCode: String) = PlayerModel(
        workout = workout,
        timeline = compiler.compile(workout),
        catalog = catalog,
        store = store,
        languageCode = languageCode,
        executionId = executionId,
        events = store.events(executionId),
    )
}
