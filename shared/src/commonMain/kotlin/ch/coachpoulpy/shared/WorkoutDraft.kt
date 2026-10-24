package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.engine.WorkoutTypeRegistry
import ch.coachpoulpy.core.model.Exercise
import ch.coachpoulpy.core.model.ExerciseSlug
import ch.coachpoulpy.core.model.Ids
import ch.coachpoulpy.core.model.Load
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.Prescription
import ch.coachpoulpy.core.model.SetPrescription
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.StepId
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.core.model.WorkoutType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * One exercise in the builder: the same number of sets, each with the same target,
 * load and rest. [amount] is reps, seconds or meters depending on [measurement].
 * Exercises done one side at a time get a left and a right set for each set.
 */
data class DraftEntry(
    val key: String,
    val exercise: ExerciseSlug,
    val measurement: MeasurementMode,
    val perSide: Boolean,
    val sets: Int,
    val amount: Int,
    val loadKg: Double?,
    val restSeconds: Int,
) {
    internal fun toStep(): Step.ExerciseStep {
        val target = when (measurement) {
            MeasurementMode.REPS -> Target.Reps(amount)
            MeasurementMode.TIME -> Target.Time(amount.seconds)
            MeasurementMode.HOLD -> Target.Hold(amount.seconds)
            MeasurementMode.DISTANCE -> Target.Distance(amount.toDouble())
        }
        val load = loadKg?.let { Load.Kilograms(it) }
        val rest = restSeconds.seconds
        val prescriptions = (1..sets).flatMap {
            if (perSide) {
                listOf(
                    SetPrescription(target, load, side = Side.LEFT, restAfter = if (target.isTimed) SWITCH_SIDES else Duration.ZERO),
                    SetPrescription(target, load, side = Side.RIGHT, restAfter = rest),
                )
            } else {
                listOf(SetPrescription(target, load, restAfter = rest))
            }
        }
        return Step.ExerciseStep(StepId(key), exercise, Prescription(prescriptions))
    }

    companion object {
        /** Time to change sides between the left and right halves of a timed set. */
        internal val SWITCH_SIDES = 5.seconds

        internal fun new(exercise: Exercise, type: WorkoutType): DraftEntry = DraftEntry(
            key = Ids.newUuidV7(),
            exercise = exercise.slug,
            measurement = exercise.measurement,
            perSide = exercise.perSide,
            sets = if (type == WorkoutType.MOBILITY) 1 else 3,
            amount = defaultAmount(exercise.measurement),
            loadKg = null,
            restSeconds = if (type == WorkoutType.STRENGTH) 60 else 20,
        )

        internal fun from(step: Step.ExerciseStep, exercise: Exercise?): DraftEntry? {
            val sets = step.prescription.sets
            val first = sets.firstOrNull() ?: return null
            val measurement = exercise?.measurement ?: first.target.measurement
            return DraftEntry(
                key = step.id.value,
                exercise = step.exercise,
                measurement = measurement,
                perSide = sets.any { it.side != Side.BOTH },
                sets = sets.count { it.side != Side.RIGHT },
                amount = first.target.amount,
                loadKg = (first.load as? Load.Kilograms)?.kg,
                restSeconds = sets.last().restAfter.inWholeSeconds.toInt(),
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
    val entries: List<DraftEntry>,
) {
    internal fun toWorkout(): Workout = Workout(
        id = id ?: WorkoutId(Ids.newUuidV7()),
        version = version,
        name = name.trim(),
        type = type,
        steps = entries.map { it.toStep() },
    )

    companion object {
        internal fun empty(type: WorkoutType = WorkoutType.STRENGTH) = WorkoutDraft(null, 1, "", type, emptyList())

        /** Only workouts made of plain exercise steps (as the builder makes them) can be edited. */
        internal fun from(workout: Workout, catalog: Catalog): WorkoutDraft? {
            val entries = workout.steps.map { step ->
                if (step !is Step.ExerciseStep) return null
                DraftEntry.from(step, catalog[step.exercise]) ?: return null
            }
            return WorkoutDraft(workout.id, workout.version + 1, workout.name, workout.type, entries)
        }
    }
}

internal val Target.isTimed: Boolean get() = this is Target.Time || this is Target.Hold

internal val Target.measurement: MeasurementMode get() = when (this) {
    is Target.Reps -> MeasurementMode.REPS
    is Target.Time -> MeasurementMode.TIME
    is Target.Hold -> MeasurementMode.HOLD
    is Target.Distance -> MeasurementMode.DISTANCE
}

internal val Target.amount: Int get() = when (this) {
    is Target.Reps -> count
    is Target.Time -> duration.inWholeSeconds.toInt()
    is Target.Hold -> duration.inWholeSeconds.toInt()
    is Target.Distance -> meters.toInt()
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
