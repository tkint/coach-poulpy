package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.data.local.LocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/** One exercise as the builder screen shows it. */
data class BuilderRow(
    val key: String,
    val name: String,
    val pattern: MovementPattern,
    val sets: Int,
    val amount: Int,
    val amountLabel: String,
    /** True when [amount] is a duration in seconds, so the UI can show a timer icon. */
    val isTimed: Boolean,
    val perSideLabel: String?,
    val loadKg: Double?,
    val restSeconds: Int,
    val restLabel: String,
    /** False when the chosen workout type cannot run this exercise. */
    val fitsType: Boolean,
    /** The folded card's line, as on the workout page: "3 × 10 · 20 kg · rest 90 s". */
    val summary: String,
    /** A walk or a run's target, edited instead of sets, reps and weight; null for any other exercise. */
    val target: WalkRunTarget? = null,
)

/**
 * A walk or a run's target as the builder shows it. The texts are what the value sheet
 * starts with, in [distanceUnit] and [paceUnit]; the labels add the unit.
 */
data class WalkRunTarget(
    val kind: TargetKind,
    val durationSeconds: Int?,
    val durationText: String,
    val durationLabel: String,
    val distanceMeters: Double?,
    val distanceText: String,
    val distanceLabel: String,
    val distanceUnit: DistanceUnit,
    val speedKmh: Double?,
    val paceText: String,
    val paceLabel: String,
    val paceUnit: PaceUnit,
    /** What the target works out to: "10.0 km/h · about 30 min"; null without a pace. */
    val hint: String?,
)

data class BuilderState(
    val name: String,
    val type: WorkoutType,
    val typeLabel: String,
    val rows: List<BuilderRow>,
    val canSave: Boolean,
)

data class WorkoutTypeOption(val type: WorkoutType, val label: String)

/** The workout builder: pick exercises, then set sets, reps or time, weight and rest for each. */
class BuilderModel internal constructor(
    initial: WorkoutDraft,
    private val catalog: Catalog,
    private val registry: WorkoutTypeRegistry,
    private val store: LocalStore,
    languageCode: String,
    private val units: () -> UnitSystem = { UnitSystem.METRIC },
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val labels = Labels(languageCode)
    private var draft = initial.copy(entries = initial.entries.map { it.inUnits(units()) })
    private val mutableState = MutableStateFlow(render(draft))

    val isEditing: Boolean = initial.id != null

    val typeOptions: List<WorkoutTypeOption> = BUILDER_TYPES.map { WorkoutTypeOption(it, labels.workoutType(it)) }

    val state: StateFlow<BuilderState> = mutableState.asStateFlow()

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: BuilderState get() = state.value

    fun watch(onChange: (BuilderState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun rename(name: String) = edit { copy(name = name) }

    fun setType(type: WorkoutType) = edit { copy(type = type) }

    fun add(slug: String) {
        val exercise = catalog[ExerciseSlug(slug)] ?: return
        edit { copy(entries = entries + DraftEntry.new(exercise, type, units())) }
    }

    fun remove(key: String) = edit { copy(entries = entries.filterNot { it.key == key }) }

    fun moveUp(key: String) = move(key, -1)
    fun moveDown(key: String) = move(key, +1)

    fun setSets(key: String, sets: Int) = editEntry(key) { copy(sets = sets.coerceIn(1, 20)) }

    fun changeAmount(key: String, steps: Int) = editEntry(key) {
        val step = DraftEntry.amountStep(measurement)
        copy(amount = (amount + steps * step).coerceIn(step, 1000 * step))
    }

    /** Sets the weight; zero or less means no weight. */
    fun setLoadKg(key: String, kg: Double) = editEntry(key) { copy(loadKg = kg.takeIf { it > 0.0 }) }

    fun changeRest(key: String, steps: Int) = editEntry(key) {
        copy(restSeconds = (restSeconds + steps * REST_STEP).coerceIn(0, 600))
    }

    /**
     * Gives a walk or a run another target. A distance starts at 5 km (3 mi) for a run and
     * 2 km (1 mi) for a walk, a pace at 10 km/h for a run and 6 km/h for a walk.
     */
    fun setTarget(key: String, kind: TargetKind) = editEntry(key) {
        if (!walkOrRun) return@editEntry this
        val byDistance = kind == TargetKind.DISTANCE || kind == TargetKind.DISTANCE_PACE
        val withPace = kind == TargetKind.DURATION_PACE || kind == TargetKind.DISTANCE_PACE
        val run = isRun(exercise)
        val measured = when {
            byDistance && measurement != MeasurementMode.DISTANCE -> {
                val meters = if (distanceUnit == DistanceUnit.MI) (if (run) 3 else 1) * DistanceUnit.MI.meters else (if (run) 5000.0 else 2000.0)
                copy(measurement = MeasurementMode.DISTANCE, amount = meters.roundToInt())
            }
            !byDistance && measurement != MeasurementMode.TIME -> copy(measurement = MeasurementMode.TIME, amount = DraftEntry.WALK_OR_RUN_MINUTES * 60)
            else -> this
        }
        when {
            !withPace -> measured.copy(speedKmh = null)
            measured.speedKmh != null -> measured
            else -> measured.copy(speedKmh = defaultSpeed(run, paceUnit))
        }
    }

    /** One minute more or less. */
    fun changeDuration(key: String, steps: Int) = editEntry(key) {
        if (!walkOrRun || measurement != MeasurementMode.TIME) return@editEntry this
        copy(amount = ((amount / 60 + steps) * 60).coerceIn(60, MAX_SECONDS))
    }

    /** A tenth of a kilometre or mile more or less, or 50 m. */
    fun changeDistance(key: String, steps: Int) = editEntry(key) {
        if (!walkOrRun || measurement != MeasurementMode.DISTANCE) return@editEntry this
        val step = if (distanceUnit == DistanceUnit.M) 50.0 else distanceUnit.meters / 10
        val meters = ((amount / step).roundToInt() + steps) * step
        copy(amount = meters.coerceIn(step, MAX_METERS).roundToInt())
    }

    /** Five seconds per kilometre or mile more or less, or a tenth of a km/h or mph: + raises the number shown. */
    fun changePace(key: String, steps: Int) = editEntry(key) {
        val kmh = speedKmh ?: return@editEntry this
        val next = if (paceUnit.isSpeed) {
            val shown = ((TargetInput.formatPace(kmh, paceUnit).toDouble() * 10).roundToInt() + steps) / 10.0
            TargetInput.parsePace(shown.coerceIn(0.5, 40.0).toString(), paceUnit)
        } else {
            val seconds = (paceUnit.meters / 1000.0 / kmh * 3600).roundToInt()
            val shown = (((seconds / 5.0).roundToInt() + steps) * 5).coerceIn(120, 3600)
            TargetInput.parsePace("${shown / 60}:${(shown % 60).toString().padStart(2, '0')}", paceUnit)
        }
        copy(speedKmh = next ?: kmh)
    }

    /** Applies a typed duration ("45", "1:05:00"); false when it cannot be read. */
    fun setDuration(key: String, text: String): Boolean {
        val seconds = TargetInput.parseDuration(text)?.takeIf { it <= MAX_SECONDS } ?: return false
        editEntry(key) { if (walkOrRun) copy(measurement = MeasurementMode.TIME, amount = seconds) else this }
        return true
    }

    /** Applies a typed distance in [unit], which the block keeps; false when it cannot be read. */
    fun setDistance(key: String, text: String, unit: DistanceUnit): Boolean {
        val meters = TargetInput.parseDistance(text, unit)?.takeIf { it in 1.0..MAX_METERS } ?: return false
        editEntry(key) { if (walkOrRun) copy(measurement = MeasurementMode.DISTANCE, amount = meters.roundToInt(), distanceUnit = unit) else this }
        return true
    }

    /** Applies a typed pace or speed in [unit], which the block keeps; false when it cannot be read. */
    fun setPace(key: String, text: String, unit: PaceUnit): Boolean {
        val kmh = TargetInput.parsePace(text, unit)?.takeIf { it in 0.5..40.0 } ?: return false
        editEntry(key) { if (walkOrRun) copy(speedKmh = kmh, paceUnit = unit) else this }
        return true
    }

    /**
     * Switches the value sheet to another distance unit: applies [typed] when it can be read,
     * keeps [unit] for the block, and returns the distance written in it.
     */
    fun setDistanceUnit(key: String, unit: DistanceUnit, typed: String): String {
        val entry = draft.entries.firstOrNull { it.key == key } ?: return typed
        setDistance(key, typed, entry.distanceUnit)
        editEntry(key) { copy(distanceUnit = unit) }
        val meters = draft.entries.first { it.key == key }.let { if (it.measurement == MeasurementMode.DISTANCE) it.amount.toDouble() else null }
        return meters?.let { TargetInput.formatDistance(it, unit) } ?: typed
    }

    /** Like [setDistanceUnit], for the pace: "6:00" /km becomes "10.0" km/h. */
    fun setPaceUnit(key: String, unit: PaceUnit, typed: String): String {
        val entry = draft.entries.firstOrNull { it.key == key } ?: return typed
        setPace(key, typed, entry.paceUnit)
        editEntry(key) { copy(paceUnit = unit) }
        val kmh = draft.entries.first { it.key == key }.speedKmh
        return kmh?.let { TargetInput.formatPace(it, unit) } ?: typed
    }

    /** The value sheet's quick picks for a duration: 10, 20, 30 and 45 minutes, and an hour. */
    fun durationPicks(): List<ValuePick> =
        listOf(10, 20, 30, 45, 60).map { ValuePick(labels.longDuration(it * 60), if (it == 60) "1:00:00" else "$it") }

    /** The value sheet's quick picks for a distance in [unit], up to the marathon. */
    fun distancePicks(unit: DistanceUnit): List<ValuePick> = when (unit) {
        DistanceUnit.M -> listOf("200", "400", "800", "1000").map { ValuePick("$it m", it) }
        DistanceUnit.KM -> listOf(
            ValuePick("1 km", "1"),
            ValuePick("5 km", "5"),
            ValuePick("10 km", "10"),
            ValuePick(labels.halfMarathon, "21.0975"),
            ValuePick(labels.marathon, "42.195"),
        )
        DistanceUnit.MI -> listOf(
            ValuePick("1 mi", "1"),
            ValuePick("5K", "3.10686"),
            ValuePick("10K", "6.21371"),
            ValuePick(labels.halfMarathon, "13.1094"),
            ValuePick(labels.marathon, "26.2188"),
        )
    }

    /** The value sheet's quick picks for a pace in [unit], slower ones for a walk and faster ones for a run. */
    fun pacePicks(key: String, unit: PaceUnit): List<ValuePick> {
        val run = draft.entries.firstOrNull { it.key == key }?.let { isRun(it.exercise) } ?: false
        val texts = when (unit) {
            PaceUnit.PER_KM -> if (run) listOf("7:00", "6:00", "5:30", "5:00", "4:30") else listOf("12:00", "10:00", "9:00", "8:00")
            PaceUnit.PER_MI -> if (run) listOf("11:00", "10:00", "9:00", "8:00", "7:00") else listOf("20:00", "18:00", "16:00", "14:00")
            PaceUnit.KMH -> if (run) listOf("8", "10", "12", "14") else listOf("4", "5", "6", "7")
            PaceUnit.MPH -> if (run) listOf("5", "6", "7", "8") else listOf("2.5", "3", "3.5", "4")
        }
        return texts.map { ValuePick("$it ${unit.symbol}", it) }
    }

    /** Saves the workout and returns its id, or null when it is not ready ([BuilderState.canSave]). */
    fun save(): String? {
        if (!state.value.canSave) return null
        val workout = draft.toWorkout()
        store.saveWorkout(workout)
        draft = draft.copy(id = workout.id, version = workout.version + 1)
        return workout.id.value
    }

    fun close() = scope.cancel()

    private fun edit(change: WorkoutDraft.() -> WorkoutDraft) {
        draft = draft.change()
        mutableState.value = render(draft)
    }

    private fun editEntry(key: String, change: DraftEntry.() -> DraftEntry) = edit {
        copy(entries = entries.map { if (it.key == key) it.change() else it })
    }

    private fun move(key: String, by: Int) = edit {
        val from = entries.indexOfFirst { it.key == key }
        val to = from + by
        if (from < 0 || to !in entries.indices) return@edit this
        copy(entries = entries.toMutableList().apply { add(to, removeAt(from)) })
    }

    private fun render(draft: WorkoutDraft): BuilderState {
        val rows = draft.entries.map { entry ->
            val exercise = catalog[entry.exercise]
            BuilderRow(
                key = entry.key,
                name = exercise?.let { labels.text(it.name) } ?: entry.exercise.value,
                pattern = exercise?.movementPattern ?: MovementPattern.CORE,
                sets = entry.sets,
                amount = entry.amount,
                amountLabel = labels.amount(entry.measurement, entry.amount),
                isTimed = entry.measurement == MeasurementMode.TIME || entry.measurement == MeasurementMode.HOLD,
                perSideLabel = if (entry.perSide) labels.perSide else null,
                loadKg = entry.loadKg,
                restSeconds = entry.restSeconds,
                restLabel = labels.duration(entry.restSeconds),
                fitsType = registry.fits(draft.type, entry),
                summary = labels.prescription(entry),
                target = entry.targetKind?.let { target(entry, it) },
            )
        }
        return BuilderState(
            name = draft.name,
            type = draft.type,
            typeLabel = labels.workoutType(draft.type),
            rows = rows,
            canSave = draft.name.isNotBlank() && rows.isNotEmpty() && rows.all { it.fitsType },
        )
    }

    private fun target(entry: DraftEntry, kind: TargetKind): WalkRunTarget {
        val seconds = entry.amount.takeIf { entry.measurement == MeasurementMode.TIME }
        val meters = entry.amount.toDouble().takeIf { entry.measurement == MeasurementMode.DISTANCE }
        val kmh = entry.speedKmh
        val shownDistance = meters ?: 0.0
        val hint = kmh?.let {
            val other = TargetInput.paceLabel(it, TargetInput.other(entry.paceUnit))
            val outcome = if (seconds != null) {
                TargetInput.distanceLabel(it * seconds / 3.6, entry.distanceUnit.takeIf { unit -> unit != DistanceUnit.M } ?: DistanceUnit.KM)
            } else {
                labels.longDuration(((shownDistance / 1000.0 / it * 3600 / 60).roundToInt() * 60).coerceAtLeast(60))
            }
            "$other · ${labels.about(outcome)}"
        }
        return WalkRunTarget(
            kind = kind,
            durationSeconds = seconds,
            durationText = TargetInput.formatDuration(seconds ?: DraftEntry.WALK_OR_RUN_MINUTES * 60),
            durationLabel = labels.longDuration(seconds ?: DraftEntry.WALK_OR_RUN_MINUTES * 60),
            distanceMeters = meters,
            distanceText = TargetInput.formatDistance(shownDistance, entry.distanceUnit),
            distanceLabel = TargetInput.distanceLabel(shownDistance, entry.distanceUnit),
            distanceUnit = entry.distanceUnit,
            speedKmh = kmh,
            paceText = kmh?.let { TargetInput.formatPace(it, entry.paceUnit) } ?: "",
            paceLabel = kmh?.let { TargetInput.paceLabel(it, entry.paceUnit) } ?: "",
            paceUnit = entry.paceUnit,
            hint = hint,
        )
    }

    private fun isRun(slug: ExerciseSlug) = catalog.variationsOf(RUN).any { it.slug == slug }

    private fun defaultSpeed(run: Boolean, unit: PaceUnit) = when (unit) {
        PaceUnit.PER_MI, PaceUnit.MPH -> TargetInput.parsePace(if (run) "10:00" else "16:00", PaceUnit.PER_MI)!!
        else -> if (run) 10.0 else 6.0
    }

    private companion object {
        const val REST_STEP = 15
        const val MAX_SECONDS = 12 * 3600
        const val MAX_METERS = 200_000.0
        val RUN = ExerciseSlug("run")
        val BUILDER_TYPES = listOf(WorkoutType.STRENGTH, WorkoutType.CONDITIONING, WorkoutType.MOBILITY, WorkoutType.REHAB, WorkoutType.TREADMILL)
    }
}
