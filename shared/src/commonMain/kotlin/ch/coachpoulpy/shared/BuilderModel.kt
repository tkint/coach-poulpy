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
    /** The catalog slug of the exercise, which the step sheet's Details button opens; empty for a rest. */
    val slug: String,
    val pattern: MovementPattern,
    val sets: Int,
    val amount: Int,
    val amountLabel: String,
    /** What [amount] counts, for the unit beside the number field. */
    val amountUnit: AmountUnit = AmountUnit.REPS,
    /** True when [amount] is a duration in seconds, so the UI can show a timer icon. */
    val isTimed: Boolean,
    val perSideLabel: String?,
    /** The weight as typed in [weightUnit], "" for none; stored in kilograms whatever the unit system. */
    val weight: String,
    /** "kg" or "lb", from Settings; "" for a rest. */
    val weightUnit: String,
    /** A rest's length; zero for exercises. */
    val restSeconds: Int,
    val restLabel: String,
    /** Whether the step sheet has a Sets stepper: exercises of a Sets workout, not walks or runs. */
    val showsSets: Boolean,
    /** The rest after each set but the last, shown from two sets on. */
    val restBetweenSets: Int,
    val restBetweenSetsLabel: String,
    /** Where the step is, under its name in the step sheet: "In Work · once per round, 4 rounds", "In Work · 3 sets". */
    val place: String,
    /** The folded card's line, as on the workout page: "3 × 10 · 20 kg · 1 min between sets". */
    val summary: String,
    /** A walk or a run's target, edited instead of sets, reps and weight; null for any other exercise. */
    val target: WalkRunTarget? = null,
)

/** What a step's amount counts: the unit shown beside its number field. */
enum class AmountUnit { REPS, SECONDS, METERS }

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
    /** "Repeat 3 times", the one line that opens the count sheet. */
    val title: String,
    val rows: List<BuilderRow>,
    /** Whether the up and down chevrons can move the repeat: not at the top or bottom of its part. */
    val canMoveUp: Boolean,
    val canMoveDown: Boolean,
)

/** One step at the top of a part: either an exercise or rest ([row]), or a repeat ([repeatGroup]; "repeat" is a Swift keyword). */
data class BuilderStep(val key: String, val row: BuilderRow?, val repeatGroup: BuilderRepeat?)

/**
 * A part of the workout under its block's border: "Work", "4 rounds · about 14 min" (or "Optional" and
 * "Nothing yet" while it is empty), its steps, then "Add to Work". The warm-up, the work and the cool
 * down are always there; an empty warm-up or cool down is left out when the workout is saved.
 */
data class BuilderPart(
    val key: String,
    val kind: PartKind,
    /** "Warm-up", "Work" or "Cool down". */
    val name: String,
    /** The same line as on the workout page ([WorkoutFacts.partSummary]); "Optional" or "Nothing yet" when [steps] is empty. */
    val summary: String,
    /** "Add to warm-up", "Add to Work", "Add to cool down". */
    val addLabel: String,
    val steps: List<BuilderStep>,
)

data class BuilderState(
    val name: String,
    val format: WorkoutFormat,
    val formatLabel: String,
    val parts: List<BuilderPart>,
    /** Every exercise and rest of every part, repeats opened up, in order. */
    val rows: List<BuilderRow>,
    /** The exercise or rest added last, so the screen can open it. */
    val addedKey: String?,
    /** Whether the workout has what it takes to be saved; a blank name becomes one made from its exercises. */
    val canSave: Boolean,
)

/** A card of the format sheet: Circuit or Sets, with its name. */
data class WorkoutFormatOption(val format: WorkoutFormat, val label: String)

/**
 * What switching the workout to [format] would do. When [asks] is false both choices give the
 * same steps (an empty workout, say) and the switch applies at once; otherwise the screen
 * offers "Follow the new format" with [followLine] and "Keep the order" with [keepLine].
 */
data class FormatChange(val format: WorkoutFormat, val formatLabel: String, val asks: Boolean, val followLine: String, val keepLine: String)

data class StepRoleOption(val role: StepRole, val label: String)

/**
 * The workout builder. The format, Circuit or Sets, is chosen when the workout is created and
 * can be switched from the builder ([changeFormat]). The workout is three parts in order (warm-up, work, cool down), each a list of
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
    /** An exercise to start with, for an exercise page's "Add to a workout". */
    firstExercise: String? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val labels = Labels(languageCode, units)
    private val facts = WorkoutFacts(compiler, catalog, labels)
    private var draft = initial.withAllParts().mapLeaves { (it as? DraftEntry)?.inUnits(units()) ?: it }
    private var addedKey: String? = null
    private val mutableState = MutableStateFlow(render(draft))
    init {
        firstExercise?.let { slug ->
            add(slug, draft.parts.first { it.kind == PartKind.WORK }.key)
        }
    }

    val isEditing: Boolean = initial.id != null

    /** What the format sheet offers: when a workout is created, and from the builder's format line. */
    val formatOptions: List<WorkoutFormatOption> = WorkoutFormat.entries.map { WorkoutFormatOption(it, labels.format(it)) }

    val roleOptions: List<StepRoleOption> = StepRole.entries.map { StepRoleOption(it, labels.roleName(it)) }

    val state: StateFlow<BuilderState> = mutableState.asStateFlow()

    /** The latest state, for Swift, which cannot read a StateFlow's typed value. */
    val current: BuilderState get() = state.value

    fun watch(onChange: (BuilderState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    fun rename(name: String) = edit { copy(name = name) }

    /** What switching to [format] would do, or null when the workout already has it. */
    fun formatChange(format: WorkoutFormat): FormatChange? {
        if (draft.format == format) return null
        val follow = draft.switchedTo(format, FormatSwitch.FOLLOW_NEW_FORMAT)
        val keep = draft.switchedTo(format, FormatSwitch.KEEP_ORDER)
        return FormatChange(format, labels.format(format), asks = follow.parts != keep.parts, followLine = orderLine(follow), keepLine = orderLine(keep))
    }

    /** How the first two steps of the first work part are done: "3 rounds of Bench press, Row", or "Bench press 3 times, then Row 3 times". */
    private fun orderLine(of: WorkoutDraft): String {
        fun name(entry: DraftEntry) = catalog[entry.exercise]?.let { labels.text(it.name) } ?: entry.exercise.value
        val steps = of.parts.firstOrNull { it.kind == PartKind.WORK }?.steps.orEmpty().filter { it !is DraftRest }.take(2)
        val parts = steps.mapNotNull { step ->
            when (step) {
                is DraftEntry -> if (step.hasSets()) labels.timesOf(name(step), step.sets) else name(step)
                is DraftRepeat -> {
                    val entries = step.steps.filterIsInstance<DraftEntry>()
                    if (entries.size == 1) labels.timesOf(name(entries.single()), step.times) else labels.roundsOf(step.times, entries.joinToString(", ") { name(it) })
                }
                is DraftRest -> null
            }
        }
        return when (parts.size) {
            0 -> ""
            1 -> parts[0]
            else -> labels.then(parts[0], parts[1])
        }
    }

    /** Switches the workout to [format], its steps rewritten as [how] says; nothing is saved until [save]. */
    fun changeFormat(format: WorkoutFormat, how: FormatSwitch) {
        if (draft.format == format) return
        edit { switchedTo(format, how) }
    }

    /**
     * Adds an exercise at the end of the part [partKey], or at the end of its repeat [repeatKey];
     * a repeat still empty at the end of the part takes it. A walk or a run starts with a distance.
     */
    fun add(slug: String, partKey: String, repeatKey: String? = null) {
        val exercise = catalog[ExerciseSlug(slug)] ?: return
        val format = draft.format
        val entry = DraftEntry.new(exercise, format, units(), inRepeat = repeatKey != null)
            .let { if (it.walkOrRun) it.withDuration(DurationKind.DISTANCE) else it }
        addStep(entry, partKey, repeatKey)
    }

    /** The workout as it was before a swap began, kept until the step sheet confirms or cancels it. */
    private var beforeSwap: WorkoutDraft? = null

    /**
     * Starts swapping step [key] for any other catalog exercise, picked in the exercise picker. The
     * step sheet then shows the new exercise's values: the place, role, sets, weight and rest of the
     * old step, and its amount too unless the new exercise is measured another way (reps against
     * time) or is a walk or a run, whose target differs: it then starts at its usual default.
     * [confirmSwap] keeps the result, [cancelSwap] gives the old exercise back.
     */
    fun startSwap(key: String, slug: String) {
        val old = entry(key) ?: return
        val exercise = catalog[ExerciseSlug(slug)]?.takeIf { it.slug != old.exercise && !it.retired } ?: return
        val fresh = DraftEntry.new(exercise, draft.format, units())
            .let { if (it.walkOrRun) it.withDuration(DurationKind.DISTANCE) else it }
        beforeSwap = draft
        editEntry(key) {
            if (old.walkOrRun || fresh.walkOrRun) {
                fresh.copy(key = old.key, role = old.role)
            } else {
                copy(
                    exercise = fresh.exercise,
                    perSide = fresh.perSide,
                    measurement = fresh.measurement,
                    amount = if (fresh.measurement == old.measurement) old.amount else fresh.amount,
                )
            }
        }
    }

    /** The step sheet's Swap button: keeps the new exercise. */
    fun confirmSwap() {
        beforeSwap = null
    }

    /** Closing the step sheet without Swap: the step keeps the exercise it had. */
    fun cancelSwap() {
        val before = beforeSwap ?: return
        beforeSwap = null
        edit { before }
    }

    /** Puts a copy of a step, or of a repeat and its steps, right after it. */
    fun duplicate(key: String) = editList(key) { list ->
        val index = list.indexOfFirst { it.key == key }
        list.toMutableList().apply { add(index + 1, list[index].copied()) }
    }

    /** Adds a one-minute rest at the end of the part [partKey], or at the end of its repeat [repeatKey]; a repeat still empty at the end of the part takes it. */
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

    /** Sets the reps, seconds or metres typed in the number field, kept within what the − and + allow. */
    fun setAmount(key: String, value: Int) = editEntry(key) {
        val step = DraftEntry.amountStep(measurement)
        copy(amount = value.coerceIn(1, 1000 * step))
    }

    /** Sets the weight typed in the user's unit (kilograms or pounds, "," or "."); nothing readable or zero or less means no weight. */
    fun setWeight(key: String, text: String) {
        val typed = text.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
        val kg = typed?.let { if (units() == UnitSystem.IMPERIAL) Units.kilogramsOf(it) else it }
        editEntry(key) { copy(loadKg = kg) }
    }

    /** A stored weight as the field shows it: "16", "17.5", "35.3". */
    private fun weightText(kg: Double): String {
        val shown = kotlin.math.round(Units.weight(kg, units()).value * 10) / 10
        return if (shown % 1.0 == 0.0) shown.toLong().toString() else shown.toString()
    }

    /** Changes the length of a rest step. */
    fun changeRest(key: String, steps: Int) = edit {
        mapLeaves { if (it is DraftRest && it.key == key) it.copy(seconds = (it.seconds + steps * REST_STEP).coerceIn(REST_STEP, MAX_REST)) else it }
    }

    /** Sets the length of a rest step typed in seconds. */
    fun setRest(key: String, seconds: Int) = edit {
        mapLeaves { if (it is DraftRest && it.key == key) it.copy(seconds = seconds.coerceIn(1, MAX_REST)) else it }
    }

    /** Sets the rest between an exercise's sets typed in seconds, down to none. */
    fun setRestBetweenSets(key: String, seconds: Int) = editEntry(key) { copy(restBetweenSets = seconds.coerceIn(0, MAX_REST)) }

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
        val workout = draft.toWorkout { it.autoName(catalog, labels) }
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
            val into = repeatKey ?: (steps.lastOrNull() as? DraftRepeat)?.takeIf { it.steps.isEmpty() }?.key
            if (into == null) steps + step else steps.map { if (it is DraftRepeat && it.key == into) it.copy(steps = it.steps + step) else it }
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

    private fun render(draft: WorkoutDraft): BuilderState {
        val lastPart = draft.parts.lastOrNull { it.steps.isNotEmpty() }
        // "Until the end" only makes sense at the very end, not on every round of a repeat.
        val lastKey = lastPart?.steps?.lastOrNull()?.takeIf { it !is DraftRepeat }?.key
        val parts = draft.parts.map { part ->
            fun row(step: DraftStep, repeat: DraftRepeat?, above: DraftStep?, canUp: Boolean, canDown: Boolean): BuilderRow {
                val place = Place(part.key, labels.inPart(part.kind), repeat)
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
                    BuilderStep(step.key, null, BuilderRepeat(step.key, step.times, labels.times(step.times), labels.repeatTitle(step.times), rows, index > 0, index < part.steps.lastIndex))
                } else {
                    BuilderStep(step.key, row(step, null, part.steps.getOrNull(index - 1), index > 0, index < part.steps.lastIndex), null)
                }
            }
            BuilderPart(
                key = part.key,
                kind = part.kind,
                name = labels.part(part.kind),
                summary = if (part.steps.isEmpty()) labels.partEmpty(part.kind) else facts.partSummary(part.toPart(), endsWorkout = part == lastPart),
                addLabel = labels.addTo(part.kind),
                steps = steps,
            )
        }
        val rows = parts.flatMap { part -> part.steps.flatMap { it.row?.let(::listOf) ?: it.repeatGroup!!.rows } }
        return BuilderState(
            name = draft.name,
            format = draft.format,
            formatLabel = labels.format(draft.format),
            parts = parts,
            rows = rows,
            addedKey = addedKey,
            canSave = rows.any { !it.isRest } &&
                draft.parts.first { it.kind == PartKind.WORK }.steps.isNotEmpty() &&
                parts.all { part -> part.steps.all { it.repeatGroup?.rows?.isNotEmpty() ?: true } },
        )
    }

    /** Where a step sits: its part, "In Work", and the repeat around it. */
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
        slug = "",
        pattern = MovementPattern.MOBILITY,
        sets = 1,
        amount = rest.seconds,
        amountLabel = labels.duration(rest.seconds),
        amountUnit = AmountUnit.SECONDS,
        isTimed = true,
        perSideLabel = null,
        weight = "",
        weightUnit = "",
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
            isLast = last,
            name = exercise?.let { labels.text(it.name) } ?: entry.exercise.value,
            slug = entry.exercise.value,
            pattern = exercise?.movementPattern ?: MovementPattern.CORE,
            sets = entry.sets,
            amount = entry.amount,
            amountLabel = labels.amount(entry.measurement, entry.amount),
            amountUnit = when (entry.measurement) {
                MeasurementMode.REPS -> AmountUnit.REPS
                MeasurementMode.TIME, MeasurementMode.HOLD -> AmountUnit.SECONDS
                MeasurementMode.DISTANCE -> AmountUnit.METERS
            },
            isTimed = entry.measurement == MeasurementMode.TIME || entry.measurement == MeasurementMode.HOLD,
            perSideLabel = if (entry.perSide) labels.perSide else null,
            weight = entry.loadKg?.let { weightText(it) } ?: "",
            weightUnit = Units.weight(0.0, units()).unit,
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
