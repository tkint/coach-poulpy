package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.StepRole
import ch.coachpoulpy.core.model.Target
import ch.coachpoulpy.core.model.WorkoutFormat

/**
 * Words that come from the domain (muscles, equipment, units) in the user's language, so Android and iOS
 * show the same labels. The text is in `i18n/messages/<locale>/domain.json` and reaches this class as the
 * generated [DomainText]; screen text stays in each app's own generated strings.
 */
internal class Labels(private val languageCode: String) {
    fun text(text: LocalizedText) = text.forLanguage(languageCode)

    fun muscle(group: MuscleGroup) = when (group) {
        MuscleGroup.CHEST -> DomainText.muscleChest(languageCode)
        MuscleGroup.BACK -> DomainText.muscleBack(languageCode)
        MuscleGroup.SHOULDERS -> DomainText.muscleShoulders(languageCode)
        MuscleGroup.BICEPS -> DomainText.muscleBiceps(languageCode)
        MuscleGroup.TRICEPS -> DomainText.muscleTriceps(languageCode)
        MuscleGroup.FOREARMS -> DomainText.muscleForearms(languageCode)
        MuscleGroup.CORE -> DomainText.muscleCore(languageCode)
        MuscleGroup.GLUTES -> DomainText.muscleGlutes(languageCode)
        MuscleGroup.QUADRICEPS -> DomainText.muscleQuadriceps(languageCode)
        MuscleGroup.HAMSTRINGS -> DomainText.muscleHamstrings(languageCode)
        MuscleGroup.CALVES -> DomainText.muscleCalves(languageCode)
        MuscleGroup.HIPS -> DomainText.muscleHips(languageCode)
        MuscleGroup.NECK -> DomainText.muscleNeck(languageCode)
        MuscleGroup.FULL_BODY -> DomainText.muscleFullBody(languageCode)
    }

    fun muscleArea(area: MuscleArea) = when (area) {
        MuscleArea.LEGS -> DomainText.muscleAreaLegs(languageCode)
        MuscleArea.GLUTES -> DomainText.muscleAreaGlutes(languageCode)
        MuscleArea.CORE -> DomainText.muscleAreaCore(languageCode)
        MuscleArea.BACK -> DomainText.muscleAreaBack(languageCode)
        MuscleArea.CHEST -> DomainText.muscleAreaChest(languageCode)
        MuscleArea.SHOULDERS -> DomainText.muscleAreaShoulders(languageCode)
        MuscleArea.ARMS -> DomainText.muscleAreaArms(languageCode)
        MuscleArea.FULL_BODY -> DomainText.muscleAreaFullBody(languageCode)
    }

    fun equipment(item: Equipment) = when (item) {
        Equipment.BODYWEIGHT -> DomainText.equipmentBodyweight(languageCode)
        Equipment.DUMBBELL -> DomainText.equipmentDumbbell(languageCode)
        Equipment.KETTLEBELL -> DomainText.equipmentKettlebell(languageCode)
        Equipment.BARBELL -> DomainText.equipmentBarbell(languageCode)
        Equipment.BAND -> DomainText.equipmentBand(languageCode)
        Equipment.MAT -> DomainText.equipmentMat(languageCode)
        Equipment.BENCH -> DomainText.equipmentBench(languageCode)
        Equipment.PULL_UP_BAR -> DomainText.equipmentPullUpBar(languageCode)
        Equipment.TREADMILL -> DomainText.equipmentTreadmill(languageCode)
        Equipment.MACHINE -> DomainText.equipmentMachine(languageCode)
    }

    fun pattern(pattern: MovementPattern) = when (pattern) {
        MovementPattern.SQUAT -> DomainText.patternSquat(languageCode)
        MovementPattern.HINGE -> DomainText.patternHinge(languageCode)
        MovementPattern.LUNGE -> DomainText.patternLunge(languageCode)
        MovementPattern.PUSH -> DomainText.patternPush(languageCode)
        MovementPattern.PULL -> DomainText.patternPull(languageCode)
        MovementPattern.CARRY -> DomainText.patternCarry(languageCode)
        MovementPattern.CORE -> DomainText.patternCore(languageCode)
        MovementPattern.ROTATION -> DomainText.patternRotation(languageCode)
        MovementPattern.LOCOMOTION -> DomainText.patternLocomotion(languageCode)
        MovementPattern.MOBILITY -> DomainText.patternMobility(languageCode)
        MovementPattern.STRETCH -> DomainText.patternStretch(languageCode)
    }

    fun measurement(mode: MeasurementMode) = when (mode) {
        MeasurementMode.REPS -> DomainText.measurementReps(languageCode)
        MeasurementMode.TIME -> DomainText.measurementTime(languageCode)
        MeasurementMode.HOLD -> DomainText.measurementHold(languageCode)
        MeasurementMode.DISTANCE -> DomainText.measurementDistance(languageCode)
    }

    /** "3 of 5". */
    fun position(index: Int, count: Int) = DomainText.countPosition(languageCode, index, count)

    fun format(format: WorkoutFormat) = when (format) {
        WorkoutFormat.CIRCUIT -> DomainText.formatCircuit(languageCode)
        WorkoutFormat.SETS -> DomainText.formatSets(languageCode)
    }

    /** "Warm-up", "Work", or "Work 2" when the workout has several work parts ([number]). */
    fun part(kind: PartKind, number: Int? = null) = when (kind) {
        PartKind.WARM_UP -> DomainText.partWarmUp(languageCode)
        PartKind.WORK -> if (number == null) DomainText.partWork(languageCode) else DomainText.partWorkNumbered(languageCode, number)
        PartKind.COOL_DOWN -> DomainText.partCoolDown(languageCode)
    }

    /** "Add to warm-up", "Add to Work 2": the link at the end of a part in the builder; [number] as in [part]. */
    fun addTo(kind: PartKind, number: Int? = null) = when (kind) {
        PartKind.WARM_UP -> DomainText.partAddToWarmUp(languageCode)
        PartKind.WORK -> DomainText.partAddToWork(languageCode, number ?: 0)
        PartKind.COOL_DOWN -> DomainText.partAddToCoolDown(languageCode)
    }

    /** "In Work 2", where a step sits; [number] as in [part]. */
    fun inPart(kind: PartKind, number: Int? = null) = when (kind) {
        PartKind.WARM_UP -> DomainText.partInWarmUp(languageCode)
        PartKind.WORK -> DomainText.partInWork(languageCode, number ?: 0)
        PartKind.COOL_DOWN -> DomainText.partInCoolDown(languageCode)
    }

    /** "once per round, 4 rounds", for a step of a circuit's repeat. */
    fun oncePerRound(roundCount: Int) = DomainText.countOncePerRound(languageCode, rounds(roundCount))

    /** "3 sets", or "3 sets per round, 4 rounds" inside a repeat of [roundCount] rounds. */
    fun setsPerRound(sets: Int, roundCount: Int?): String {
        val count = DomainText.countSets(languageCode, sets)
        if (roundCount == null) return count
        return DomainText.countPerRound(languageCode, sets = count, rounds = rounds(roundCount))
    }

    fun rounds(count: Int) = DomainText.countRounds(languageCode, count)

    /** Why a part cannot be added again. */
    val alreadyInWorkout get() = DomainText.partAlreadyIn(languageCode)

    /** Where a new part goes: "Goes first", "Goes last", or "Added after Work 2 as Work 3". */
    fun partGoes(kind: PartKind, after: String, name: String) = when (kind) {
        PartKind.WARM_UP -> DomainText.partGoesFirst(languageCode)
        PartKind.WORK -> DomainText.partAddedAfter(languageCode, after = after, part = name)
        PartKind.COOL_DOWN -> DomainText.partGoesLast(languageCode)
    }

    /** The player's part line: "Work 1 · round 2 of 4", or the [part] alone outside a repeat. */
    fun partLine(part: String, round: Int, rounds: Int) =
        if (rounds > 1) "$part · ${roundOf(round, rounds)}" else part

    /** "round 3 of 4", after a part or "Next up". */
    fun roundOf(round: Int, rounds: Int) = DomainText.countRoundOf(languageCode, round, rounds)

    fun amount(measurement: MeasurementMode, amount: Int) = when (measurement) {
        MeasurementMode.REPS -> DomainText.countReps(languageCode, amount)
        MeasurementMode.TIME, MeasurementMode.HOLD -> duration(amount)
        MeasurementMode.DISTANCE -> DomainText.unitMeters(languageCode, amount)
    }

    fun duration(seconds: Int): String = when {
        seconds < 60 -> DomainText.unitSeconds(languageCode, seconds)
        seconds % 60 == 0 -> DomainText.unitMinutes(languageCode, seconds / 60)
        else -> DomainText.unitMinutesSeconds(languageCode, seconds / 60, (seconds % 60).toString().padStart(2, '0'))
    }

    fun kilograms(kg: Double): String {
        val text = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()
        return DomainText.unitKilograms(languageCode, text)
    }

    fun side(side: Side) = when (side) {
        Side.LEFT -> DomainText.sideLeft(languageCode)
        Side.RIGHT -> DomainText.sideRight(languageCode)
        Side.BOTH -> null
    }

    val rest get() = DomainText.labelRest(languageCode)
    val freeRun get() = DomainText.labelFreeRun(languageCode)
    val demoIntervals get() = DomainText.labelDemoIntervals(languageCode)

    /** For a session whose workout was deleted before the history kept names. */
    val session get() = DomainText.labelSession(languageCode)
    val perSide get() = DomainText.labelPerSide(languageCode)
    val recentlyUsed get() = DomainText.labelRecentlyUsed(languageCode)
    val hold get() = DomainText.labelHold(languageCode)
    val orWord get() = DomainText.labelOr(languageCode)
    val andWord get() = DomainText.labelAnd(languageCode)
    val timed get() = DomainText.labelTimed(languageCode)

    fun parts(count: Int) = DomainText.countParts(languageCode, count)

    /** Why a workout file was refused; [unknown] are the slugs the catalog lacks. */
    fun workoutFileProblem(problem: WorkoutFileProblem, unknown: List<String>) = when (problem) {
        WorkoutFileProblem.UNREADABLE -> DomainText.workoutFileUnreadable(languageCode)
        WorkoutFileProblem.NOT_A_WORKOUT -> DomainText.workoutFileNotWorkout(languageCode)
        WorkoutFileProblem.NEWER_VERSION -> DomainText.workoutFileNewer(languageCode)
        WorkoutFileProblem.UNKNOWN_EXERCISES -> DomainText.workoutFileUnknown(languageCode, unknown.size, unknown.joinToString(", "))
        WorkoutFileProblem.INVALID_WORKOUT -> DomainText.workoutFileInvalid(languageCode)
    }

    fun steps(count: Int) = DomainText.countSteps(languageCode, count)

    fun exercises(count: Int) = DomainText.countExercises(languageCode, count)

    /** The name a copy starts with: "Upper body (copy)". */
    fun copyName(name: String) = DomainText.workoutCopyName(languageCode, name)

    /** Only says what applies: the planned days it comes off, the sessions that stay in History. */
    fun deletePrompt(name: String, planned: Int, done: Int): DeletePrompt {
        val goes = if (planned == 0) null else DomainText.workoutDeletePlanned(languageCode, planned)
        val stays = if (done == 0) null else DomainText.workoutDeleteDone(languageCode, done)
        val message = listOfNotNull(goes, stays).joinToString(" ").ifEmpty { DomainText.workoutDeleteFinal(languageCode) }
        return DeletePrompt(DomainText.workoutDeleteTitle(languageCode, name), message)
    }

    fun workouts(count: Int) = DomainText.countWorkouts(languageCode, count)

    fun matches(count: Int) = DomainText.countMatches(languageCode, count)

    fun exercisesYouCanDo(count: Int) = DomainText.countExercisesYouCanDo(languageCode, count)

    fun matchesYouCanDo(count: Int) = DomainText.countMatchesYouCanDo(languageCode, count)

    fun aboutMinutes(minutes: Int) = DomainText.countAboutMinutes(languageCode, minutes)

    /** "45 min", or "1 h" and "1 h 05" from an hour on. */
    fun longDuration(seconds: Int): String {
        if (seconds < 3600) return duration(seconds)
        val minutes = seconds % 3600 / 60
        val hours = seconds / 3600
        return if (minutes > 0) DomainText.unitHoursMinutes(languageCode, hours, minutes.toString().padStart(2, '0')) else DomainText.unitHours(languageCode, hours)
    }

    /**
     * One exercise's plan in a line: "3 × 10 · each side · 14 kg · 1 min between sets", "15 reps",
     * "40 s hold" for one set, or "5.0 km · 6:00 /km"
     * and "5:30 /km · until button press" for a walk or a run; [last] says "until the end" instead.
     */
    fun prescription(entry: DraftEntry, last: Boolean = false): String {
        if (entry.walkOrRun) {
            val pace = entry.speedKmh?.let { TargetInput.paceLabel(it, entry.paceUnit) }
            if (entry.open) return listOfNotNull(pace, until(last).lowercase()).joinToString(" · ")
            val main = if (entry.measurement == MeasurementMode.DISTANCE) {
                TargetInput.distanceLabel(entry.amount.toDouble(), entry.distanceUnit)
            } else {
                longDuration(entry.amount)
            }
            return listOfNotNull(main, pace).joinToString(" · ")
        }
        val several = entry.sets > 1
        val amount = when (entry.measurement) {
            MeasurementMode.REPS -> if (several) entry.amount.toString() else amount(MeasurementMode.REPS, entry.amount)
            MeasurementMode.HOLD -> DomainText.countHold(languageCode, duration(entry.amount))
            else -> amount(entry.measurement, entry.amount)
        }
        return listOfNotNull(
            if (several) "${entry.sets} × $amount" else amount,
            perSide.takeIf { entry.perSide },
            entry.loadKg?.let(::kilograms),
            entry.restBetweenSets.takeIf { several && it > 0 }?.let { betweenSets(longDuration(it)) },
        ).joinToString(" · ")
    }

    /** "2 min between sets". */
    fun betweenSets(rest: String) = DomainText.countBetweenSets(languageCode, rest)

    fun about(text: String) = DomainText.countAbout(languageCode, text)

    val halfMarathon get() = DomainText.labelHalfMarathon(languageCode)
    val marathon get() = DomainText.labelMarathon(languageCode)

    /** "Until button press", or "Until the end" on the last step, where the press finishes the session. */
    fun until(last: Boolean) = if (last) DomainText.labelUntilEnd(languageCode) else DomainText.labelUntilPress(languageCode)

    /** What a phase asks for in the player: "10 reps", "30 s", "400 m", or "Until you tap Next". */
    fun target(target: Target) = if (target is Target.Open) DomainText.labelUntilNext(languageCode) else amount(target.measurement, target.amount)

    /** The Recover tag on rests and recover steps; null for work steps, whose part says where they are. */
    fun role(role: StepRole) = when (role) {
        StepRole.RECOVER -> DomainText.roleRecover(languageCode)
        StepRole.WORK -> null
    }

    /** The name of a role in the step select: Exercise or Recover. */
    fun roleName(role: StepRole) = role(role) ?: DomainText.roleExercise(languageCode)

    fun times(count: Int) = DomainText.countTimes(languageCode, count)
}
