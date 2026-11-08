package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.SessionRecord
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.remote.AccountClient
import ch.coachpoulpy.device.ftms.KableCentral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone

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
    /** [memory] is the one the profile uses, so Poulpy knows about pain or rehab. */
    fun home(languageCode: String, memory: KeyValueStore): HomeModel {
        val profile = profile(memory)
        return HomeModel(store, compiler, catalog, languageCode, discardOpen = ::discardOpen, painActive = profile::painActive)
    }
    fun library(languageCode: String) = LibraryModel(store, compiler, catalog, languageCode)
    fun workoutPage(workoutId: String, languageCode: String) = WorkoutPageModel(WorkoutId(workoutId), store, compiler, catalog, languageCode, units = ::units)
    /** A workout file's text, read for the preview before it is added to the library. */
    fun importWorkout(fileText: String, languageCode: String) = WorkoutImport(fileText, store, catalog, languageCode)
    // CLEANUP: History's Poulpy header ignores pain or rehab, so it can show the ember side inside the blue-side window. Pass the profile's painActive here like home() does.
    fun history(languageCode: String) = HistoryModel(store, compiler, languageCode)

    /** The picture to share for a finished session of the history, the same one as straight after the session. */
    fun sessionShareCard(sessionId: String, units: UnitSystem, languageCode: String): ShareCard? =
        SessionShare(store, compiler, languageCode).card(sessionId, units)

    /** The .FIT file of a finished session, to import into Garmin Connect or upload to Strava; null when it never started. */
    fun sessionFitFile(sessionId: String): SessionFitFile? =
        SessionFitExport(store, compiler, catalog).file(sessionId, TimeZone.currentSystemDefault())

    private var profileModel: ProfileModel? = null

    /** The profile, created on first use. The apps pass the same [memory] every time. */
    fun profile(memory: KeyValueStore): ProfileModel =
        profileModel ?: ProfileModel(SyncedSettings(memory, store)).also { profileModel = it }

    /** The weight or height page's model; the chart follows the log, so a new one per screen is fine. */
    fun body(metric: BodyMetric) = BodyModel(metric, store)

    private var settingsModel: SettingsModel? = null
    private var devicesModel: DevicesModel? = null
    private var liveModel: LiveModel? = null

    /** The app's settings, created on first use. The apps pass the same [memory] every time. */
    fun settings(memory: KeyValueStore): SettingsModel =
        settingsModel ?: SettingsModel(SyncedSettings(memory, store)).also { settingsModel = it }

    /**
     * The one devices model of the app. Like [live], it outlives the screens so that
     * leaving a tab mid-walk keeps the devices connected and the recording going.
     */
    fun devices(memory: KeyValueStore): DevicesModel =
        devicesModel ?: DevicesModel(KableCentral(), memory).also { devicesModel = it }

    /** Live once a screen has opened it, for what follows its recording outside the screens, such as Android's notification. */
    val liveIfCreated: LiveModel? get() = liveModel

    fun live(memory: KeyValueStore, languageCode: String): LiveModel =
        liveModel ?: LiveModel(devices(memory), settings(memory).state, store, compiler, memory, catalog = { catalog }, languageCode = languageCode)
            .also {
                liveModel = it
                liveCreated.value = it
            }

    private val liveCreated = MutableStateFlow<LiveModel?>(null)
    private val playersOpen = MutableStateFlow(0)

    /** True while a guided session is open or Live records, so that nothing pops up over the workout. */
    @OptIn(ExperimentalCoroutinesApi::class)
    internal val training: Flow<Boolean> = combine(
        playersOpen,
        liveCreated.flatMapLatest { live -> live?.state?.map { it.recording != null } ?: flowOf(false) },
    ) { players, recording -> players > 0 || recording }.distinctUntilChanged()

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

    private var accountModel: AccountModel? = null

    /**
     * The account at [baseUrl], such as https://api.coach-poulpy.com, and its sync: one for the app's
     * life. [secrets] keeps the tokens, the Keychain on iOS and the Android Keystore; [deviceName]
     * names this phone in the server's device list and in the copies of a workout edited on two phones.
     * [memory] is the apps' own store, the one the profile and settings use. The emails are in the
     * language picked in Settings, or else in [languageCode], the phone's.
     */
    fun account(baseUrl: String, secrets: SecretStore, memory: KeyValueStore, deviceName: String, languageCode: String): AccountModel =
        accountModel ?: AccountModel(
            store, memory, secrets, AccountClient(baseUrl, SecretTokenStore(secrets)), deviceName,
            { LanguageChoice.entries.firstOrNull { it.name == memory.getString(SettingsModel.LANGUAGE) }?.code ?: languageCode },
            CoroutineScope(SupervisorJob() + Dispatchers.Main),
            training = training,
        ).also { accountModel = it }

    /** The units chosen in Settings, once the app has opened them; metric until then. */
    private fun units() = settingsModel?.state?.value?.units ?: UnitSystem.METRIC

    fun deleteWorkout(workoutId: String) = store.deleteWorkout(WorkoutId(workoutId))

    /** A new guided session of the latest version of a workout, ready to start. */
    fun player(workoutId: String, languageCode: String): PlayerModel? {
        val workout = store.latestWorkout(WorkoutId(workoutId)) ?: return null
        // Only one session is open at a time: one left behind ends where it was left. Live ends its own, with its summary.
        liveModel?.stopRecording()
        store.unfinishedRecord()?.let(::closeLeftOpen)
        return player(workout, Ids.newUuidV7(), languageCode)
    }

    /** The session left open when the app was closed, if any, exactly where it was. Null for a Live recording, which Live brings back. */
    fun resumeSession(languageCode: String): PlayerModel? {
        val record = store.unfinishedRecord()?.takeUnless { it.source.isLive } ?: return null
        val ref = record.workout
        val workout = ref?.let { store.workout(it.workoutId, it.version) }
        if (workout == null) {
            // Its workout was deleted: close it so it stops being offered.
            closeLeftOpen(record)
            return null
        }
        return player(workout, record.id.value, languageCode)
    }

    /**
     * Deletes [sessionId], the session left open, with everything it recorded: nothing of it shows in the history
     * or the totals. Live drops it itself when it holds the recording.
     */
    private fun discardOpen(sessionId: String) {
        if (liveModel?.discardRecording(sessionId) == true) return
        store.deleteSession(sessionId)
    }

    private fun closeLeftOpen(record: SessionRecord) = store.closeLeftOpen(record, store.timelineOf(record, compiler))

    private fun player(workout: Workout, sessionId: String, languageCode: String) = PlayerModel(
        workout = workout,
        timeline = compiler.compile(workout),
        catalog = catalog,
        store = store,
        languageCode = languageCode,
        units = ::units,
        sessionId = sessionId,
        events = store.events(sessionId),
        onClose = { playersOpen.update { it - 1 } },
    ).also { playersOpen.update { it + 1 } }
}
