package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.StepRole
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

/** One exercise or rest as the builder screen shows it. */
data class BuilderRow(
    val key: String,
    /** The tag above the name ("Warm-up", "Recover"); null for plain exercise steps. */
    val roleLabel: String?,
    val role: StepRole,
    /** A rest: only its length, kept in [restSeconds], can change. */
    val isRest: Boolean,
    /** The repeat holding this step, or null at the top of the workout. */
    val repeatKey: String?,
    /** Whether the step right above is a repeat this step can move into. */
    val canMoveIntoRepeat: Boolean,
    /** The workout's last step, where "until button press" reads "until the end". */
    val isLast: Boolean,
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
    val duration: DurationKind,
    val intensity: IntensityKind,
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

/** Steps done [times] times in a row, as the builder shows them. */
data class BuilderRepeat(val key: String, val times: Int, val timesLabel: String, val rows: List<BuilderRow>)

/** One step at the top of the workout: either an exercise or rest ([row]), or a [repeat]. */
data class BuilderStep(val key: String, val row: BuilderRow?, val repeat: BuilderRepeat?)

data class BuilderState(
    val name: String,
    val type: WorkoutType,
    val typeLabel: String,
    val steps: List<BuilderStep>,
    /** Every exercise and rest, repeats opened up, in order. */
    val rows: List<BuilderRow>,
    /** The exercise or rest added last, so the screen can open it. */
    val addedKey: String?,
    val canSave: Boolean,
)

data class WorkoutTypeOption(val type: WorkoutType, val label: String)

data class StepRoleOption(val role: StepRole, val label: String)

/**
 * The workout builder: a list of steps (exercises and rests, each with a role) and repeats
 * that run the steps inside them several times. Each exercise has its sets, reps or time,
 * weight and rest; a walk or a run has a duration and an intensity instead.
 */
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
    private var draft = initial.copy(steps = initial.steps.mapLeaves { (it as? DraftEntry)?.inUnits(units()) ?: it })
    private var addedKey: String? = null
    private val mutableState = MutableStateFlow(render(draft))

    val isEditing: Boolean = initial.id != null

    val typeOptions: List<WorkoutTypeOption> = BUILDER_TYPES.map { WorkoutTypeOption(it, labels.workoutType(it)) }

    val roleOptions: List<StepRoleOption> = StepRole.entries.map { StepRoleOption(it, labels.roleName(it)) }

    val state: StateFlow<BuilderState> = mutableState.asStateFlow()

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: BuilderState get() = state.value

    fun watch(onChange: (BuilderState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun rename(name: String) = edit { copy(name = name) }

    fun setType(type: WorkoutType) = edit { copy(type = type) }

    /** Adds an exercise at the end of the workout, or at the end of the repeat [repeatKey]. */
    fun add(slug: String, repeatKey: String? = null) {
        val exercise = catalog[ExerciseSlug(slug)] ?: return
        addStep(DraftEntry.new(exercise, draft.type, units(), inRepeat = repeatKey != null), repeatKey)
    }

    /** Adds a one-minute rest at the end of the workout, or at the end of the repeat [repeatKey]. */
    fun addRest(repeatKey: String? = null) = addStep(DraftRest(Ids.newUuidV7(), DEFAULT_REST), repeatKey)

    /** Adds an empty repeat, done twice, at the end of the workout. */
    fun addRepeat() = edit { copy(steps = steps + DraftRepeat(Ids.newUuidV7(), 2, emptyList())) }

    fun setRepeatTimes(key: String, times: Int) = edit {
        copy(steps = steps.map { if (it is DraftRepeat && it.key == key) it.copy(times = times.coerceIn(1, MAX_TIMES)) else it })
    }

    /** Removes a step, or a repeat with everything in it. */
    fun remove(key: String) = edit {
        copy(steps = steps.filterNot { it.key == key }.map { if (it is DraftRepeat) it.copy(steps = it.steps.filterNot { s -> s.key == key }) else it })
    }

    fun moveUp(key: String) = moveBy(key, -1)
    fun moveDown(key: String) = moveBy(key, +1)

    /**
     * Moves a step to [toIndex] in the list that holds it (the workout, or its repeat), as a
     * drag drops it; out-of-range indexes clamp to the ends.
     */
    fun move(key: String, toIndex: Int) = editList(key) { list ->
        val from = list.indexOfFirst { it.key == key }
        val to = toIndex.coerceIn(0, list.lastIndex)
        if (to == from) list else list.toMutableList().apply { add(to, removeAt(from)) }
    }

    /** Puts a step at the end of the repeat right above it. */
    fun moveIntoRepeatAbove(key: String) = edit {
        val index = steps.indexOfFirst { it.key == key }
        val above = steps.getOrNull(index - 1) as? DraftRepeat
        val step = steps.getOrNull(index)
        if (above == null || step == null || step is DraftRepeat) return@edit this
        copy(steps = steps.toMutableList().apply { removeAt(index); set(index - 1, above.copy(steps = above.steps + step)) })
    }

    /** Takes a step out of its repeat and puts it right after the repeat. */
    fun moveOutOfRepeat(key: String) = edit {
        val index = steps.indexOfFirst { it is DraftRepeat && it.steps.any { s -> s.key == key } }
        val repeat = steps.getOrNull(index) as? DraftRepeat ?: return@edit this
        val step = repeat.steps.first { it.key == key }
        copy(steps = steps.toMutableList().apply { set(index, repeat.copy(steps = repeat.steps - step)); add(index + 1, step) })
    }

    /** What the step is for: warm-up, exercise, recover or cool down. A rest is always a Recover step. */
    fun setRole(key: String, role: StepRole) = editEntry(key) { copy(role = role) }

    fun setSets(key: String, sets: Int) = editEntry(key) { copy(sets = sets.coerceIn(1, 20)) }

    fun changeAmount(key: String, steps: Int) = editEntry(key) {
        val step = DraftEntry.amountStep(measurement)
        copy(amount = (amount + steps * step).coerceIn(step, 1000 * step))
    }

    /** Sets the weight; zero or less means no weight. */
    fun setLoadKg(key: String, kg: Double) = editEntry(key) { copy(loadKg = kg.takeIf { it > 0.0 }) }

    /** The rest after each set, or the length of a rest step. */
    fun changeRest(key: String, steps: Int) {
        edit {
            copy(
                steps = this.steps.mapLeaves {
                    if (it is DraftRest && it.key == key) it.copy(seconds = (it.seconds + steps * REST_STEP).coerceIn(REST_STEP, 600)) else it
                },
            )
        }
        editEntry(key) { copy(restSeconds = (restSeconds + steps * REST_STEP).coerceIn(0, 600)) }
    }

    /**
     * Says how a walk or a run ends. A distance starts at 5 km (3 mi) for a run and 2 km (1 mi)
     * for a walk; until button press keeps the duration and distance for when it comes back.
     */
    fun setDurationKind(key: String, kind: DurationKind) = editEntry(key) {
        if (!walkOrRun) return@editEntry this
        val run = isRun(exercise)
        when {
            kind == DurationKind.UNTIL_PRESSED -> copy(open = true)
            kind == DurationKind.DISTANCE && measurement != MeasurementMode.DISTANCE -> {
                val meters = if (distanceUnit == DistanceUnit.MI) (if (run) 3 else 1) * DistanceUnit.MI.meters else (if (run) 5000.0 else 2000.0)
                copy(measurement = MeasurementMode.DISTANCE, amount = meters.roundToInt(), open = false)
            }
            kind == DurationKind.TIME && measurement != MeasurementMode.TIME ->
                copy(measurement = MeasurementMode.TIME, amount = DraftEntry.WALK_OR_RUN_MINUTES * 60, open = false)
            else -> copy(open = false)
        }
    }

    /** Free, or at a pace that starts at 10 km/h for a run and 6 km/h for a walk. */
    fun setIntensityKind(key: String, kind: IntensityKind) = editEntry(key) {
        when {
            !walkOrRun -> this
            kind == IntensityKind.FREE -> copy(speedKmh = null)
            speedKmh != null -> this
            else -> copy(speedKmh = defaultSpeed(isRun(exercise), paceUnit))
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
        editEntry(key) { if (walkOrRun) copy(measurement = MeasurementMode.TIME, amount = seconds, open = false) else this }
        return true
    }

    /** Applies a typed distance in [unit], which the block keeps; false when it cannot be read. */
    fun setDistance(key: String, text: String, unit: DistanceUnit): Boolean {
        val meters = TargetInput.parseDistance(text, unit)?.takeIf { it in 1.0..MAX_METERS } ?: return false
        editEntry(key) { if (walkOrRun) copy(measurement = MeasurementMode.DISTANCE, amount = meters.roundToInt(), distanceUnit = unit, open = false) else this }
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
        val entry = entry(key) ?: return typed
        setDistance(key, typed, entry.distanceUnit)
        editEntry(key) { copy(distanceUnit = unit) }
        val meters = entry(key)!!.let { if (it.measurement == MeasurementMode.DISTANCE) it.amount.toDouble() else null }
        return meters?.let { TargetInput.formatDistance(it, unit) } ?: typed
    }

    /** Like [setDistanceUnit], for the pace: "6:00" /km becomes "10.0" km/h. */
    fun setPaceUnit(key: String, unit: PaceUnit, typed: String): String {
        val entry = entry(key) ?: return typed
        setPace(key, typed, entry.paceUnit)
        editEntry(key) { copy(paceUnit = unit) }
        val kmh = entry(key)!!.speedKmh
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
        val run = entry(key)?.let { isRun(it.exercise) } ?: false
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
        copy(steps = steps.mapLeaves { if (it is DraftEntry && it.key == key) it.change() else it })
    }

    /** Changes the list holding [key]: the workout's steps, or its repeat's. */
    private fun editList(key: String, change: (List<DraftStep>) -> List<DraftStep>) = edit {
        if (steps.any { it.key == key }) {
            copy(steps = change(steps))
        } else {
            copy(steps = steps.map { if (it is DraftRepeat && it.steps.any { s -> s.key == key }) it.copy(steps = change(it.steps)) else it })
        }
    }

    private fun addStep(step: DraftStep, repeatKey: String?) {
        edit {
            if (repeatKey == null) {
                copy(steps = steps + step)
            } else {
                copy(steps = steps.map { if (it is DraftRepeat && it.key == repeatKey) it.copy(steps = it.steps + step) else it })
            }
        }
        addedKey = step.key
        mutableState.value = render(draft)
    }

    private fun entry(key: String) = draft.leaves.firstOrNull { it.key == key } as? DraftEntry

    private fun moveBy(key: String, by: Int) {
        val list = draft.steps.takeIf { s -> s.any { it.key == key } }
            ?: draft.steps.filterIsInstance<DraftRepeat>().firstOrNull { r -> r.steps.any { it.key == key } }?.steps
            ?: return
        val to = list.indexOfFirst { it.key == key } + by
        if (to !in list.indices) return
        move(key, to)
    }

    private fun render(draft: WorkoutDraft): BuilderState {
        // "Until the end" only makes sense at the very end, not on every round of a repeat.
        val lastKey = draft.steps.lastOrNull()?.takeIf { it !is DraftRepeat }?.key
        fun row(step: DraftStep, repeatKey: String?, above: DraftStep?): BuilderRow = when (step) {
            is DraftEntry -> entryRow(step, repeatKey, above is DraftRepeat && repeatKey == null, step.key == lastKey)
            is DraftRest -> restRow(step, repeatKey, above is DraftRepeat && repeatKey == null, step.key == lastKey)
            is DraftRepeat -> error("A repeat holds no repeat")
        }
        val steps = draft.steps.mapIndexed { index, step ->
            if (step is DraftRepeat) {
                val rows = step.steps.mapIndexed { i, child -> row(child, step.key, step.steps.getOrNull(i - 1)) }
                BuilderStep(step.key, null, BuilderRepeat(step.key, step.times, labels.times(step.times), rows))
            } else {
                BuilderStep(step.key, row(step, null, draft.steps.getOrNull(index - 1)), null)
            }
        }
        val rows = steps.flatMap { it.row?.let(::listOf) ?: it.repeat!!.rows }
        return BuilderState(
            name = draft.name,
            type = draft.type,
            typeLabel = labels.workoutType(draft.type),
            steps = steps,
            rows = rows,
            addedKey = addedKey,
            canSave = draft.name.isNotBlank() && rows.any { !it.isRest } && rows.all { it.fitsType } &&
                steps.all { it.repeat?.rows?.isNotEmpty() ?: true },
        )
    }

    private fun restRow(rest: DraftRest, repeatKey: String?, canMoveIntoRepeat: Boolean, last: Boolean) = BuilderRow(
        key = rest.key,
        roleLabel = labels.role(StepRole.RECOVER),
        role = StepRole.RECOVER,
        isRest = true,
        repeatKey = repeatKey,
        canMoveIntoRepeat = canMoveIntoRepeat,
        isLast = last,
        name = labels.rest,
        pattern = MovementPattern.MOBILITY,
        sets = 1,
        amount = rest.seconds,
        amountLabel = labels.duration(rest.seconds),
        isTimed = true,
        perSideLabel = null,
        loadKg = null,
        restSeconds = rest.seconds,
        restLabel = labels.duration(rest.seconds),
        fitsType = true,
        summary = labels.longDuration(rest.seconds),
    )

    private fun entryRow(entry: DraftEntry, repeatKey: String?, canMoveIntoRepeat: Boolean, last: Boolean): BuilderRow {
        val exercise = catalog[entry.exercise]
        return BuilderRow(
            key = entry.key,
            roleLabel = labels.role(entry.role),
            role = entry.role,
            isRest = false,
            repeatKey = repeatKey,
            canMoveIntoRepeat = canMoveIntoRepeat,
            isLast = last,
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
            summary = labels.prescription(entry, last),
            target = entry.durationKind?.let { target(entry, it, entry.intensityKind!!) },
        )
    }

    private fun target(entry: DraftEntry, duration: DurationKind, intensity: IntensityKind): WalkRunTarget {
        val seconds = entry.amount.takeIf { !entry.open && entry.measurement == MeasurementMode.TIME }
        val meters = entry.amount.toDouble().takeIf { !entry.open && entry.measurement == MeasurementMode.DISTANCE }
        val kmh = entry.speedKmh
        val shownDistance = meters ?: 0.0
        val hint = kmh?.takeIf { !entry.open }?.let {
            val other = TargetInput.paceLabel(it, TargetInput.other(entry.paceUnit))
            val outcome = if (seconds != null) {
                TargetInput.distanceLabel(it * seconds / 3.6, entry.distanceUnit.takeIf { unit -> unit != DistanceUnit.M } ?: DistanceUnit.KM)
            } else {
                labels.longDuration(((shownDistance / 1000.0 / it * 3600 / 60).roundToInt() * 60).coerceAtLeast(60))
            }
            "$other · ${labels.about(outcome)}"
        }
        return WalkRunTarget(
            duration = duration,
            intensity = intensity,
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
        const val DEFAULT_REST = 60
        const val MAX_TIMES = 50
        const val MAX_SECONDS = 12 * 3600
        const val MAX_METERS = 200_000.0
        val RUN = ExerciseSlug("run")
        val BUILDER_TYPES = listOf(WorkoutType.STRENGTH, WorkoutType.CONDITIONING, WorkoutType.MOBILITY, WorkoutType.REHAB, WorkoutType.TREADMILL)
    }
}

/** Changes every exercise and rest, inside repeats too. */
internal fun List<DraftStep>.mapLeaves(change: (DraftStep) -> DraftStep): List<DraftStep> = map { step ->
    if (step is DraftRepeat) step.copy(steps = step.steps.map(change)) else change(step)
}
