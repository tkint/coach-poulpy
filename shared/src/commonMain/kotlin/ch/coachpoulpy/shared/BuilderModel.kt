package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.TimelineCompiler
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.WorkoutFormat
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
    /** The Recover tag above the name; null for exercise steps. */
    val roleLabel: String?,
    val role: StepRole,
    /** A rest: only its length, kept in [restSeconds], can change. */
    val isRest: Boolean,
    /** The part holding this step. */
    val partKey: String,
    /** The repeat holding this step, or null at the top of its part. */
    val repeatKey: String?,
    /** Whether the step right above is a repeat this step can move into. */
    val canMoveIntoRepeat: Boolean,
    /** Whether the up and down chevrons can move the step: not at the top or bottom of its part or repeat. */
    val canMoveUp: Boolean,
    val canMoveDown: Boolean,
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
    /** A rest's length; zero for exercises. */
    val restSeconds: Int,
    val restLabel: String,
    /** Whether the step sheet has a Sets stepper: exercises of a Sets workout, not walks or runs. */
    val showsSets: Boolean,
    /** The rest after each set but the last, shown from two sets on. */
    val restBetweenSets: Int,
    val restBetweenSetsLabel: String,
    /** Where the step is, under its name in the step sheet: "In Work 1 · once per round, 4 rounds", "In Work · 3 sets". */
    val place: String,
    /** The folded card's line, as on the workout page: "3 × 10 · 20 kg · 1 min between sets". */
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
data class BuilderRepeat(
    val key: String,
    val times: Int,
    val timesLabel: String,
    val rows: List<BuilderRow>,
    /** Whether the up and down chevrons can move the repeat: not at the top or bottom of its part. */
    val canMoveUp: Boolean,
    val canMoveDown: Boolean,
)

/** One step at the top of a part: either an exercise or rest ([row]), or a repeat ([repeatGroup]; "repeat" is a Swift keyword). */
data class BuilderStep(val key: String, val row: BuilderRow?, val repeatGroup: BuilderRepeat?)

/** A part of the workout under its header: "Work 1", "4 rounds · about 14 min", its steps, then "Add to Work 1". */
data class BuilderPart(
    val key: String,
    val kind: PartKind,
    /** "Warm-up", "Work", or "Work 2" when there are several work parts. */
    val name: String,
    /** The same line as on the workout page ([WorkoutFacts.partSummary]). */
    val summary: String,
    /** "Add to warm-up", "Add to Work 1". */
    val addLabel: String,
    val steps: List<BuilderStep>,
    /** Only work parts move, and only among themselves. */
    val canMoveUp: Boolean,
    val canMoveDown: Boolean,
    /** False for the last work part: a workout always has one. */
    val canDelete: Boolean,
)

/** A line of the "Add a part" sheet: "Work", "Added after Work 2 as Work 3", or "Already in this workout" when not [enabled]. */
data class BuilderPartOption(val kind: PartKind, val name: String, val note: String, val enabled: Boolean)

data class BuilderState(
    val name: String,
    /** Null until it is chosen, once, when the workout is created; the screen asks first. */
    val format: WorkoutFormat?,
    val formatLabel: String,
    val parts: List<BuilderPart>,
    /** Every exercise and rest of every part, repeats opened up, in order. */
    val rows: List<BuilderRow>,
    val partOptions: List<BuilderPartOption>,
    /** The exercise or rest added last, so the screen can open it. */
    val addedKey: String?,
    val canSave: Boolean,
)

/** A card of the format sheet: Circuit or Sets, with its name. */
data class WorkoutFormatOption(val format: WorkoutFormat, val label: String)

data class StepRoleOption(val role: StepRole, val label: String)

/**
 * The workout builder. The format, Circuit or Sets, is chosen once when the workout is
 * created. The workout is parts in order (warm-up, work parts, cool down), each a list of
 * steps (exercises and rests) and repeats that run the steps inside them several times.
 * In a circuit an exercise is one set and the repeat sets the rounds; in Sets an exercise
 * has its sets and the rest between them. A walk or a run has a duration and an intensity.
 */
class BuilderModel internal constructor(
    initial: WorkoutDraft,
    private val catalog: Catalog,
    private val store: LocalStore,
    compiler: TimelineCompiler,
    languageCode: String,
    private val units: () -> UnitSystem = { UnitSystem.METRIC },
    /** An exercise to add once the format is chosen, for an exercise page's "Add to a workout". */
    private var pendingSlug: String? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val labels = Labels(languageCode)
    private val facts = WorkoutFacts(compiler, catalog, labels)
    private var draft = initial.mapLeaves { (it as? DraftEntry)?.inUnits(units()) ?: it }
    private var addedKey: String? = null
    private val mutableState = MutableStateFlow(render(draft))

    val isEditing: Boolean = initial.id != null

    /** What the format sheet offers, asked once for a new workout. */
    val formatOptions: List<WorkoutFormatOption> = WorkoutFormat.entries.map { WorkoutFormatOption(it, labels.format(it)) }

    val roleOptions: List<StepRoleOption> = StepRole.entries.map { StepRoleOption(it, labels.roleName(it)) }

    val state: StateFlow<BuilderState> = mutableState.asStateFlow()

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: BuilderState get() = state.value

    fun watch(onChange: (BuilderState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun rename(name: String) = edit { copy(name = name) }

    /**
     * Sets the format of a new workout; it never changes after. In a circuit the work part
     * starts as a repeat done three times, which holds the exercise waiting to be added.
     */
    fun chooseFormat(format: WorkoutFormat) {
        if (draft.format != null) return
        edit { copy(format = format, parts = parts.map { if (it.kind == PartKind.WORK && it.steps.isEmpty()) DraftPart.new(PartKind.WORK, format).copy(key = it.key) else it }) }
        val slug = pendingSlug ?: return
        pendingSlug = null
        val work = draft.parts.first { it.kind == PartKind.WORK }
        add(slug, work.key, (work.steps.singleOrNull() as? DraftRepeat)?.key)
    }

    /** Adds a part where it belongs: a warm-up first, a cool down last, a work part after the last one. */
    fun addPart(kind: PartKind) {
        if (state.value.partOptions.none { it.kind == kind && it.enabled }) return
        val part = DraftPart.new(kind, draft.format)
        edit {
            copy(
                parts = when (kind) {
                    PartKind.WARM_UP -> listOf(part) + parts
                    PartKind.COOL_DOWN -> parts + part
                    PartKind.WORK -> parts.toMutableList().apply { add(parts.indexOfLast { it.kind == PartKind.WORK } + 1, part) }
                },
            )
        }
    }

    fun movePartUp(key: String) = movePart(key, -1)
    fun movePartDown(key: String) = movePart(key, +1)

    /** Removes a part and its steps; the last work part stays. */
    fun removePart(key: String) = edit {
        val part = parts.firstOrNull { it.key == key } ?: return@edit this
        if (part.kind == PartKind.WORK && parts.count { it.kind == PartKind.WORK } == 1) this else copy(parts = parts - part)
    }

    /**
     * Adds an exercise with its [role] at the end of the part [partKey], or at the end of its
     * repeat [repeatKey]. A walk or a run starts with a distance.
     */
    fun add(slug: String, partKey: String, repeatKey: String? = null, role: StepRole = StepRole.WORK) {
        val exercise = catalog[ExerciseSlug(slug)] ?: return
        val format = draft.format ?: return
        val entry = DraftEntry.new(exercise, format, units(), inRepeat = repeatKey != null)
            .copy(role = role)
            .let { if (it.walkOrRun) it.withDuration(DurationKind.DISTANCE) else it }
        addStep(entry, partKey, repeatKey)
    }

    /** Puts a copy of a step, or of a repeat and its steps, right after it. */
    fun duplicate(key: String) = editList(key) { list ->
        val index = list.indexOfFirst { it.key == key }
        list.toMutableList().apply { add(index + 1, list[index].copied()) }
    }

    /** Adds a one-minute rest at the end of the part [partKey], or at the end of its repeat [repeatKey]. */
    fun addRest(partKey: String, repeatKey: String? = null) = addStep(DraftRest(Ids.newUuidV7(), DEFAULT_REST), partKey, repeatKey)

    /** Adds an empty repeat, done twice, at the end of the part [partKey]. */
    fun addRepeat(partKey: String) = editPart(partKey) { it + DraftRepeat(Ids.newUuidV7(), 2, emptyList()) }

    fun setRepeatTimes(key: String, times: Int) = edit {
        mapParts { steps -> steps.map { if (it is DraftRepeat && it.key == key) it.copy(times = times.coerceIn(1, MAX_TIMES)) else it } }
    }

    /** Removes a step, or a repeat with everything in it. */
    fun remove(key: String) = edit {
        mapParts { steps -> steps.filterNot { it.key == key }.map { if (it is DraftRepeat) it.copy(steps = it.steps.filterNot { s -> s.key == key }) else it } }
    }

    fun moveUp(key: String) = moveBy(key, -1)
    fun moveDown(key: String) = moveBy(key, +1)

    /**
     * Moves a step to [toIndex] in the list that holds it (its part, or its repeat), as a
     * drag drops it; out-of-range indexes clamp to the ends. A drag stays within its part.
     */
    fun move(key: String, toIndex: Int) = editList(key) { list ->
        val from = list.indexOfFirst { it.key == key }
        val to = toIndex.coerceIn(0, list.lastIndex)
        if (to == from) list else list.toMutableList().apply { add(to, removeAt(from)) }
    }

    /** Puts a step at the end of the repeat right above it. */
    fun moveIntoRepeatAbove(key: String) = edit {
        mapParts { steps ->
            val index = steps.indexOfFirst { it.key == key }
            val above = steps.getOrNull(index - 1) as? DraftRepeat
            val step = steps.getOrNull(index)
            if (above == null || step == null || step is DraftRepeat) steps
            else steps.toMutableList().apply { removeAt(index); set(index - 1, above.copy(steps = above.steps + step)) }
        }
    }

    /** Takes a step out of its repeat and puts it right after the repeat. */
    fun moveOutOfRepeat(key: String) = edit {
        mapParts { steps ->
            val index = steps.indexOfFirst { it is DraftRepeat && it.steps.any { s -> s.key == key } }
            val repeat = steps.getOrNull(index) as? DraftRepeat
            if (repeat == null) {
                steps
            } else {
                val step = repeat.steps.first { it.key == key }
                steps.toMutableList().apply { set(index, repeat.copy(steps = repeat.steps - step)); add(index + 1, step) }
            }
        }
    }

    /** Moves a step, or a repeat with its steps, to the end of the part [partKey]; a step leaves its repeat. */
    fun moveToPart(key: String, partKey: String) {
        val from = draft.parts.firstOrNull { part -> part.steps.any { it.key == key || (it is DraftRepeat && it.steps.any { s -> s.key == key }) } }
        if (from == null || from.key == partKey || draft.parts.none { it.key == partKey }) return
        val step = from.steps.firstOrNull { it.key == key }
            ?: from.steps.filterIsInstance<DraftRepeat>().flatMap { it.steps }.first { it.key == key }
        remove(key)
        editPart(partKey) { it + step }
    }

    /** What the step is for: an exercise, or recovering. A rest is always a Recover step. */
    fun setRole(key: String, role: StepRole) = editEntry(key) { copy(role = role) }

    /** A circuit has no sets, nor a walk or a run: the repeat around them sets the rounds. */
    fun setSets(key: String, sets: Int) {
        if (draft.format != WorkoutFormat.SETS) return
        editEntry(key) { if (walkOrRun) this else copy(sets = sets.coerceIn(1, 20)) }
    }

    fun changeAmount(key: String, steps: Int) = editEntry(key) {
        val step = DraftEntry.amountStep(measurement)
        copy(amount = (amount + steps * step).coerceIn(step, 1000 * step))
    }

    /** Sets the weight; zero or less means no weight. */
    fun setLoadKg(key: String, kg: Double) = editEntry(key) { copy(loadKg = kg.takeIf { it > 0.0 }) }

    /** Changes the length of a rest step. */
    fun changeRest(key: String, steps: Int) = edit {
        mapLeaves { if (it is DraftRest && it.key == key) it.copy(seconds = (it.seconds + steps * REST_STEP).coerceIn(REST_STEP, MAX_REST)) else it }
    }

    /** Changes the rest between an exercise's sets, down to none. */
    fun changeRestBetweenSets(key: String, steps: Int) = editEntry(key) {
        copy(restBetweenSets = (restBetweenSets + steps * REST_STEP).coerceIn(0, MAX_REST))
    }

    /**
     * Says how a walk or a run ends. A distance starts at 5 km (3 mi) for a run and 2 km (1 mi)
     * for a walk; until button press keeps the duration and distance for when it comes back.
     */
    fun setDurationKind(key: String, kind: DurationKind) = editEntry(key) { withDuration(kind) }

    private fun DraftEntry.withDuration(kind: DurationKind): DraftEntry {
        if (!walkOrRun) return this
        val run = isRun(exercise)
        return when {
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
        mapLeaves { if (it is DraftEntry && it.key == key) it.change() else it }
    }

    private fun editPart(key: String, change: (List<DraftStep>) -> List<DraftStep>) = edit {
        copy(parts = parts.map { if (it.key == key) it.copy(steps = change(it.steps)) else it })
    }

    /** Changes the list holding [key]: its part's steps, or its repeat's. */
    private fun editList(key: String, change: (List<DraftStep>) -> List<DraftStep>) = edit {
        mapParts { steps ->
            if (steps.any { it.key == key }) {
                change(steps)
            } else {
                steps.map { if (it is DraftRepeat && it.steps.any { s -> s.key == key }) it.copy(steps = change(it.steps)) else it }
            }
        }
    }

    private fun addStep(step: DraftStep, partKey: String, repeatKey: String?) {
        editPart(partKey) { steps ->
            if (repeatKey == null) steps + step else steps.map { if (it is DraftRepeat && it.key == repeatKey) it.copy(steps = it.steps + step) else it }
        }
        addedKey = step.key
        mutableState.value = render(draft)
    }

    private fun entry(key: String) = draft.leaves.firstOrNull { it.key == key } as? DraftEntry

    private fun moveBy(key: String, by: Int) {
        val lists = draft.parts.map { it.steps } + draft.parts.flatMap { part -> part.steps.filterIsInstance<DraftRepeat>().map { it.steps } }
        val list = lists.firstOrNull { l -> l.any { it.key == key } } ?: return
        val to = list.indexOfFirst { it.key == key } + by
        if (to !in list.indices) return
        move(key, to)
    }

    private fun movePart(key: String, by: Int) = edit {
        val from = parts.indexOfFirst { it.key == key }
        val to = from + by
        if (parts.getOrNull(from)?.kind != PartKind.WORK || parts.getOrNull(to)?.kind != PartKind.WORK) return@edit this
        copy(parts = parts.toMutableList().apply { add(to, removeAt(from)) })
    }

    private fun render(draft: WorkoutDraft): BuilderState {
        val lastPart = draft.parts.last()
        // "Until the end" only makes sense at the very end, not on every round of a repeat.
        val lastKey = lastPart.steps.lastOrNull()?.takeIf { it !is DraftRepeat }?.key
        val workCount = draft.parts.count { it.kind == PartKind.WORK }
        var workNumber = 0
        val parts = draft.parts.mapIndexed { partIndex, part ->
            if (part.kind == PartKind.WORK) workNumber++
            val number = workNumber.takeIf { workCount > 1 && part.kind == PartKind.WORK }
            val name = labels.part(part.kind, number)
            fun row(step: DraftStep, repeat: DraftRepeat?, above: DraftStep?, canUp: Boolean, canDown: Boolean): BuilderRow {
                val place = Place(part.key, labels.inPart(part.kind, number), repeat)
                val canMoveIn = above is DraftRepeat && repeat == null
                return when (step) {
                    is DraftEntry -> entryRow(step, draft.format, place, canMoveIn, canUp, canDown, step.key == lastKey)
                    is DraftRest -> restRow(step, place, canMoveIn, canUp, canDown, step.key == lastKey)
                    is DraftRepeat -> error("A repeat holds no repeat")
                }
            }
            val steps = part.steps.mapIndexed { index, step ->
                if (step is DraftRepeat) {
                    val rows = step.steps.mapIndexed { i, child -> row(child, step, step.steps.getOrNull(i - 1), i > 0, i < step.steps.lastIndex) }
                    BuilderStep(step.key, null, BuilderRepeat(step.key, step.times, labels.times(step.times), rows, index > 0, index < part.steps.lastIndex))
                } else {
                    BuilderStep(step.key, row(step, null, part.steps.getOrNull(index - 1), index > 0, index < part.steps.lastIndex), null)
                }
            }
            val work = part.kind == PartKind.WORK
            BuilderPart(
                key = part.key,
                kind = part.kind,
                name = name,
                summary = facts.partSummary(part.toPart(), endsWorkout = part == lastPart),
                addLabel = labels.addTo(part.kind, number),
                steps = steps,
                canMoveUp = work && draft.parts.getOrNull(partIndex - 1)?.kind == PartKind.WORK,
                canMoveDown = work && draft.parts.getOrNull(partIndex + 1)?.kind == PartKind.WORK,
                canDelete = !work || workCount > 1,
            )
        }
        val rows = parts.flatMap { part -> part.steps.flatMap { it.row?.let(::listOf) ?: it.repeatGroup!!.rows } }
        val lastWork = parts.last { it.kind == PartKind.WORK }.name
        val partOptions = PartKind.entries.map { kind ->
            val present = kind != PartKind.WORK && draft.parts.any { it.kind == kind }
            BuilderPartOption(
                kind = kind,
                name = labels.part(kind),
                note = if (present) labels.alreadyInWorkout else labels.partGoes(kind, lastWork, labels.part(PartKind.WORK, workCount + 1)),
                enabled = !present,
            )
        }
        return BuilderState(
            name = draft.name,
            format = draft.format,
            formatLabel = draft.format?.let { labels.format(it) } ?: "",
            parts = parts,
            rows = rows,
            partOptions = partOptions,
            addedKey = addedKey,
            canSave = draft.name.isNotBlank() && draft.format != null && rows.any { !it.isRest } &&
                draft.parts.all { it.steps.isNotEmpty() } && parts.all { part -> part.steps.all { it.repeatGroup?.rows?.isNotEmpty() ?: true } },
        )
    }

    /** Where a step sits: its part, "In Work 1", and the repeat around it. */
    private class Place(val partKey: String, val inPart: String, val repeat: DraftRepeat?)

    private fun restRow(rest: DraftRest, place: Place, canMoveIntoRepeat: Boolean, canMoveUp: Boolean, canMoveDown: Boolean, last: Boolean) = BuilderRow(
        key = rest.key,
        roleLabel = labels.role(StepRole.RECOVER),
        role = StepRole.RECOVER,
        isRest = true,
        partKey = place.partKey,
        repeatKey = place.repeat?.key,
        canMoveIntoRepeat = canMoveIntoRepeat,
        canMoveUp = canMoveUp,
        canMoveDown = canMoveDown,
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
        showsSets = false,
        restBetweenSets = 0,
        restBetweenSetsLabel = "",
        place = place.inPart,
        summary = labels.longDuration(rest.seconds),
    )

    private fun entryRow(entry: DraftEntry, format: WorkoutFormat?, place: Place, canMoveIntoRepeat: Boolean, canMoveUp: Boolean, canMoveDown: Boolean, last: Boolean): BuilderRow {
        val exercise = catalog[entry.exercise]
        val showsSets = format == WorkoutFormat.SETS && !entry.walkOrRun
        val detail = when {
            showsSets -> labels.setsPerRound(entry.sets, place.repeat?.times)
            place.repeat != null -> labels.oncePerRound(place.repeat.times)
            else -> null
        }
        return BuilderRow(
            key = entry.key,
            roleLabel = labels.role(entry.role),
            role = entry.role,
            isRest = false,
            partKey = place.partKey,
            repeatKey = place.repeat?.key,
            canMoveIntoRepeat = canMoveIntoRepeat,
            canMoveUp = canMoveUp,
            canMoveDown = canMoveDown,
        canMoveUp = canMoveUp,
        canMoveDown = canMoveDown,
            isLast = last,
            name = exercise?.let { labels.text(it.name) } ?: entry.exercise.value,
            pattern = exercise?.movementPattern ?: MovementPattern.CORE,
            sets = entry.sets,
            amount = entry.amount,
            amountLabel = labels.amount(entry.measurement, entry.amount),
            isTimed = entry.measurement == MeasurementMode.TIME || entry.measurement == MeasurementMode.HOLD,
            perSideLabel = if (entry.perSide) labels.perSide else null,
            loadKg = entry.loadKg,
            restSeconds = 0,
            restLabel = "",
            showsSets = showsSets,
            restBetweenSets = entry.restBetweenSets,
            restBetweenSetsLabel = labels.duration(entry.restBetweenSets),
            place = listOfNotNull(place.inPart, detail).joinToString(" · "),
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
        const val MAX_REST = 600
        const val DEFAULT_REST = 60
        const val MAX_TIMES = 50
        const val MAX_SECONDS = 12 * 3600
        const val MAX_METERS = 200_000.0
        val RUN = ExerciseSlug("run")
    }
}

/** Changes the steps of every part. */
internal fun WorkoutDraft.mapParts(change: (List<DraftStep>) -> List<DraftStep>) = copy(parts = parts.map { it.copy(steps = change(it.steps)) })

/** Changes every exercise and rest of every part, inside repeats too. */
internal fun WorkoutDraft.mapLeaves(change: (DraftStep) -> DraftStep) = mapParts { it.mapLeaves(change) }

/** Changes every exercise and rest, inside repeats too. */
internal fun List<DraftStep>.mapLeaves(change: (DraftStep) -> DraftStep): List<DraftStep> = map { step ->
    if (step is DraftRepeat) step.copy(steps = step.steps.map(change)) else change(step)
}

/** The same step with new keys, so the copy is its own step. */
internal fun DraftStep.copied(): DraftStep = when (this) {
    is DraftEntry -> copy(key = Ids.newUuidV7())
    is DraftRest -> copy(key = Ids.newUuidV7())
    is DraftRepeat -> copy(key = Ids.newUuidV7(), steps = steps.map { it.copied() })
}
