package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.GroupKind
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Intensity
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** One step of a workout in the builder: an exercise, a rest or a repeat. */
sealed interface DraftStep {
    val key: String
}

/** A rest of [seconds], a Recover step with no exercise. */
data class DraftRest(override val key: String, val seconds: Int) : DraftStep {
    internal fun toStep() = Step.Rest(StepId(key), seconds.seconds)
}

/** Steps done [times] times in a row. A repeat holds exercises and rests, never another repeat. */
data class DraftRepeat(override val key: String, val times: Int, val steps: List<DraftStep>) : DraftStep {
    internal fun toStep() = Step.Group(StepId(key), GroupKind.SEQUENCE, rounds = times, children = steps.map { it.toModelStep() })
}

internal fun DraftStep.toModelStep(): Step = when (this) {
    is DraftEntry -> toStep()
    is DraftRest -> toStep()
    is DraftRepeat -> toStep()
}

/**
 * One exercise in the builder: the same number of sets, each with the same target and
 * load, one after the other. [amount] is reps, seconds or meters depending on [measurement].
 * Exercises done one side at a time get a left and a right set for each set. Rests are
 * steps of their own; to rest between sets, put one set and a rest in a repeat.
 *
 * A walk or a run ([walkOrRun]) is one block instead: a duration, a distance or until the
 * button is pressed ([open]), with an optional pace kept as [speedKmh]. [distanceUnit] and
 * [paceUnit] are how the builder shows them, and are not saved with the workout.
 */
data class DraftEntry(
    override val key: String,
    val exercise: ExerciseSlug,
    val measurement: MeasurementMode,
    val perSide: Boolean,
    val sets: Int,
    val amount: Int,
    val loadKg: Double?,
    val walkOrRun: Boolean = false,
    val speedKmh: Double? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val paceUnit: PaceUnit = PaceUnit.PER_KM,
    val role: StepRole = StepRole.WORK,
    val open: Boolean = false,
) : DraftStep {
    /** How a walk or a run ends; null for any other exercise. */
    val durationKind: DurationKind?
        get() = when {
            !walkOrRun -> null
            open -> DurationKind.UNTIL_PRESSED
            measurement == MeasurementMode.DISTANCE -> DurationKind.DISTANCE
            else -> DurationKind.TIME
        }

    /** A walk or a run's effort; null for any other exercise. */
    val intensityKind: IntensityKind?
        get() = if (!walkOrRun) null else if (speedKmh != null) IntensityKind.PACE else IntensityKind.FREE

    internal fun toStep(): Step.ExerciseStep {
        val target = if (walkOrRun && open) Target.Open else when (measurement) {
            MeasurementMode.REPS -> Target.Reps(amount)
            MeasurementMode.TIME -> Target.Time(amount.seconds)
            MeasurementMode.HOLD -> Target.Hold(amount.seconds)
            MeasurementMode.DISTANCE -> Target.Distance(amount.toDouble())
        }
        val load = loadKg?.let { Load.Kilograms(it) }
        if (walkOrRun) {
            val set = SetPrescription(target, intensity = speedKmh?.let { Intensity.Speed(it) })
            return Step.ExerciseStep(StepId(key), exercise, Prescription(listOf(set)), role)
        }
        val prescriptions = (1..sets).flatMap {
            if (perSide) {
                listOf(
                    SetPrescription(target, load, side = Side.LEFT, restAfter = if (target.isTimed) SWITCH_SIDES else Duration.ZERO),
                    SetPrescription(target, load, side = Side.RIGHT),
                )
            } else {
                listOf(SetPrescription(target, load))
            }
        }
        return Step.ExerciseStep(StepId(key), exercise, Prescription(prescriptions), role)
    }

    companion object {
        /** Time to change sides between the left and right halves of a timed set. */
        internal val SWITCH_SIDES = 5.seconds

        /** A new exercise step; one added inside a repeat starts at one set, since the repeat does the repeating. */
        internal fun new(exercise: Exercise, type: WorkoutType, units: UnitSystem = UnitSystem.METRIC, inRepeat: Boolean = false): DraftEntry {
            if (exercise.isWalkOrRun) {
                return DraftEntry(
                    key = Ids.newUuidV7(),
                    exercise = exercise.slug,
                    measurement = MeasurementMode.TIME,
                    perSide = false,
                    sets = 1,
                    amount = WALK_OR_RUN_MINUTES * 60,
                    loadKg = null,
                    walkOrRun = true,
                ).inUnits(units)
            }
            return DraftEntry(
                key = Ids.newUuidV7(),
                exercise = exercise.slug,
                measurement = exercise.measurement,
                perSide = exercise.perSide,
                sets = if (type == WorkoutType.MOBILITY || inRepeat) 1 else 3,
                amount = defaultAmount(exercise.measurement),
                loadKg = null,
            )
        }

        /** Twenty minutes, the duration a new walk or run starts with. */
        internal const val WALK_OR_RUN_MINUTES = 20

        internal fun from(step: Step.ExerciseStep, exercise: Exercise?): DraftEntry? {
            val sets = step.prescription.sets
            val first = sets.firstOrNull() ?: return null
            val walkTarget = first.target is Target.Time || first.target is Target.Distance || first.target is Target.Open
            if (exercise?.isWalkOrRun == true && sets.size == 1 && walkTarget) {
                val open = first.target is Target.Open
                return DraftEntry(
                    key = step.id.value,
                    exercise = step.exercise,
                    measurement = if (open) MeasurementMode.TIME else first.target.measurement,
                    perSide = false,
                    sets = 1,
                    amount = if (open) WALK_OR_RUN_MINUTES * 60 else first.target.amount,
                    loadKg = null,
                    walkOrRun = true,
                    speedKmh = (first.intensity as? Intensity.Speed)?.kmh,
                    role = step.role,
                    open = open,
                )
            }
            if (sets.any { it.target is Target.Open }) return null
            val measurement = exercise?.measurement ?: first.target.measurement
            return DraftEntry(
                key = step.id.value,
                exercise = step.exercise,
                measurement = measurement,
                perSide = sets.any { it.side != Side.BOTH },
                sets = sets.count { it.side != Side.RIGHT },
                amount = first.target.amount,
                loadKg = (first.load as? Load.Kilograms)?.kg,
                role = step.role,
            )
        }

        fun defaultAmount(measurement: MeasurementMode) = when (measurement) {
            MeasurementMode.REPS -> 10
            MeasurementMode.TIME, MeasurementMode.HOLD -> 30
            MeasurementMode.DISTANCE -> 400
        }

        /** How much one tap on + or - changes [amount]. */
        fun amountStep(measurement: MeasurementMode) = when (measurement) {
            MeasurementMode.REPS -> 1
            MeasurementMode.TIME, MeasurementMode.HOLD -> 5
            MeasurementMode.DISTANCE -> 50
        }
    }
}

/** A workout being created or edited. Saving it stores a new version; old versions stay for past sessions. */
data class WorkoutDraft(
    val id: WorkoutId?,
    val version: Int,
    val name: String,
    val type: WorkoutType,
    val steps: List<DraftStep>,
) {
    internal fun toWorkout(): Workout = Workout(
        id = id ?: WorkoutId(Ids.newUuidV7()),
        version = version,
        name = name.trim(),
        type = type,
        steps = steps.map { it.toModelStep() },
    )

    /** Every exercise and rest, repeats opened up, in order. */
    internal val leaves: List<DraftStep> get() = steps.flatMap { if (it is DraftRepeat) it.steps else listOf(it) }

    companion object {
        internal fun empty(type: WorkoutType = WorkoutType.STRENGTH) = WorkoutDraft(null, 1, "", type, emptyList())

        /**
         * Only workouts the builder could have made can be edited: exercises, rests and
         * repeats one level deep with no rest of their own between rounds.
         */
        internal fun from(workout: Workout, catalog: Catalog): WorkoutDraft? {
            fun leaf(step: Step): DraftStep? = when (step) {
                is Step.ExerciseStep -> DraftEntry.from(step, catalog[step.exercise])
                is Step.Rest -> DraftRest(step.id.value, step.duration.inWholeSeconds.toInt())
                is Step.Group -> null
            }
            val steps = workout.steps.map { step ->
                if (step is Step.Group) {
                    if (step.restBetweenRounds > Duration.ZERO) return null
                    DraftRepeat(step.id.value, step.rounds, step.children.map { leaf(it) ?: return null })
                } else {
                    leaf(step) ?: return null
                }
            }
            return WorkoutDraft(workout.id, workout.version + 1, workout.name, workout.type, steps)
        }
    }
}

/** How a walk or a run ends: after a time, a distance, or when the button is pressed ("until the end" on the last step). */
enum class DurationKind { DISTANCE, TIME, UNTIL_PRESSED }

/** A walk or a run's effort: free, or at a pace. */
enum class IntensityKind { FREE, PACE }

/** Walks and runs, from rehab to marathon training: the exercises a treadmill can do. */
internal val Exercise.isWalkOrRun: Boolean get() = Equipment.TREADMILL in optionalEquipment

/** Shows distance and pace the way [units] does. */
internal fun DraftEntry.inUnits(units: UnitSystem) = if (!walkOrRun) {
    this
} else if (units == UnitSystem.IMPERIAL) {
    copy(distanceUnit = DistanceUnit.MI, paceUnit = PaceUnit.PER_MI)
} else {
    copy(distanceUnit = DistanceUnit.KM, paceUnit = PaceUnit.PER_KM)
}

internal val Target.isTimed: Boolean get() = this is Target.Time || this is Target.Hold

internal val Target.measurement: MeasurementMode get() = when (this) {
    is Target.Reps -> MeasurementMode.REPS
    is Target.Time -> MeasurementMode.TIME
    is Target.Hold -> MeasurementMode.HOLD
    is Target.Distance -> MeasurementMode.DISTANCE
    Target.Open -> MeasurementMode.TIME
}

internal val Target.amount: Int get() = when (this) {
    is Target.Reps -> count
    is Target.Time -> duration.inWholeSeconds.toInt()
    is Target.Hold -> duration.inWholeSeconds.toInt()
    is Target.Distance -> meters.toInt()
    Target.Open -> 0
}

/** The types a walk or a run fits whatever its target: a treadmill workout, a cardio circuit, rehab. */
private val WALK_OR_RUN_TYPES = setOf(WorkoutType.TREADMILL, WorkoutType.CONDITIONING, WorkoutType.REHAB)

/** Whether the builder can add [exercise] to a workout of [type]; a treadmill workout only takes walks and runs. */
internal fun WorkoutTypeRegistry.fits(type: WorkoutType, exercise: Exercise): Boolean =
    if (type == WorkoutType.TREADMILL) {
        exercise.isWalkOrRun
    } else {
        accepts(type, exercise.measurement) || (exercise.isWalkOrRun && type in WALK_OR_RUN_TYPES)
    }

/** Whether a workout of [type] can run [entry] as it is set. */
internal fun WorkoutTypeRegistry.fits(type: WorkoutType, entry: DraftEntry): Boolean =
    if (type == WorkoutType.TREADMILL) {
        entry.walkOrRun
    } else if (entry.open) {
        runCatching { this[type] }.getOrNull()?.acceptedTargets?.contains(Target.Open::class) == true
    } else {
        accepts(type, entry.measurement) || (entry.walkOrRun && type in WALK_OR_RUN_TYPES)
    }

/** Whether a workout of [type] can run an exercise measured this way. */
internal fun WorkoutTypeRegistry.accepts(type: WorkoutType, measurement: MeasurementMode): Boolean {
    val target = when (measurement) {
        MeasurementMode.REPS -> Target.Reps::class
        MeasurementMode.TIME -> Target.Time::class
        MeasurementMode.HOLD -> Target.Hold::class
        MeasurementMode.DISTANCE -> Target.Distance::class
    }
    return runCatching { this[type] }.getOrNull()?.acceptedTargets?.contains(target) == true
}
