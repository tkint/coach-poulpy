package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Part
import ch.coachpoulpy.core.model.PartId
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Step
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId

/** A workout of one work part holding [steps], for tests that don't care about parts. */
internal fun workoutOf(id: String, name: String, steps: List<Step>, format: WorkoutFormat = WorkoutFormat.SETS, version: Int = 1) =
    Workout(WorkoutId(id), version, name, format, listOf(Part(PartId("$id-work"), PartKind.WORK, steps)))
