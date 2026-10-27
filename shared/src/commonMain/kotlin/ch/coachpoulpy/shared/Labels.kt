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
 * Words that come from the domain (muscles, equipment, units) in both languages, so
 * Android and iOS show the same labels. Screen text stays in each app's own strings.
 */
internal class Labels(private val languageCode: String) {
    private fun t(en: String, fr: String) = LocalizedText(en, fr).forLanguage(languageCode)

    fun text(text: LocalizedText) = text.forLanguage(languageCode)

    fun muscle(group: MuscleGroup) = when (group) {
        MuscleGroup.CHEST -> t("Chest", "Pectoraux")
        MuscleGroup.BACK -> t("Back", "Dos")
        MuscleGroup.SHOULDERS -> t("Shoulders", "Épaules")
        MuscleGroup.BICEPS -> t("Biceps", "Biceps")
        MuscleGroup.TRICEPS -> t("Triceps", "Triceps")
        MuscleGroup.FOREARMS -> t("Forearms", "Avant-bras")
        MuscleGroup.CORE -> t("Core", "Abdominaux")
        MuscleGroup.GLUTES -> t("Glutes", "Fessiers")
        MuscleGroup.QUADRICEPS -> t("Quadriceps", "Quadriceps")
        MuscleGroup.HAMSTRINGS -> t("Hamstrings", "Ischio-jambiers")
        MuscleGroup.CALVES -> t("Calves", "Mollets")
        MuscleGroup.HIPS -> t("Hips", "Hanches")
        MuscleGroup.NECK -> t("Neck", "Cou")
        MuscleGroup.FULL_BODY -> t("Full body", "Corps entier")
    }

    fun muscleArea(area: MuscleArea) = when (area) {
        MuscleArea.LEGS -> t("Legs", "Jambes")
        MuscleArea.GLUTES -> t("Glutes", "Fessiers")
        MuscleArea.CORE -> t("Core", "Abdos")
        MuscleArea.BACK -> t("Back", "Dos")
        MuscleArea.CHEST -> t("Chest", "Pectoraux")
        MuscleArea.SHOULDERS -> t("Shoulders", "Épaules")
        MuscleArea.ARMS -> t("Arms", "Bras")
        MuscleArea.FULL_BODY -> t("Full body", "Corps entier")
    }

    fun equipment(item: Equipment) = when (item) {
        Equipment.BODYWEIGHT -> t("No equipment", "Sans matériel")
        Equipment.DUMBBELL -> t("Dumbbells", "Haltères")
        Equipment.KETTLEBELL -> t("Kettlebell", "Kettlebell")
        Equipment.BARBELL -> t("Barbell", "Barre")
        Equipment.BAND -> t("Resistance band", "Élastique")
        Equipment.MAT -> t("Mat", "Tapis")
        Equipment.BENCH -> t("Bench or step", "Banc ou marche")
        Equipment.PULL_UP_BAR -> t("Pull-up bar", "Barre de traction")
        Equipment.TREADMILL -> t("Treadmill", "Tapis de course")
        Equipment.MACHINE -> t("Machine", "Machine")
    }

    fun pattern(pattern: MovementPattern) = when (pattern) {
        MovementPattern.SQUAT -> t("Squat", "Squat")
        MovementPattern.HINGE -> t("Hinge", "Charnière de hanche")
        MovementPattern.LUNGE -> t("Lunge", "Fente")
        MovementPattern.PUSH -> t("Push", "Poussée")
        MovementPattern.PULL -> t("Pull", "Tirage")
        MovementPattern.CARRY -> t("Carry", "Porté")
        MovementPattern.CORE -> t("Core", "Gainage")
        MovementPattern.ROTATION -> t("Rotation", "Rotation")
        MovementPattern.LOCOMOTION -> t("Cardio", "Cardio")
        MovementPattern.MOBILITY -> t("Mobility", "Mobilité")
        MovementPattern.STRETCH -> t("Stretch", "Étirement")
    }

    fun measurement(mode: MeasurementMode) = when (mode) {
        MeasurementMode.REPS -> t("Reps", "Répétitions")
        MeasurementMode.TIME -> t("Timed", "Chronométré")
        MeasurementMode.HOLD -> t("Hold", "Maintien")
        MeasurementMode.DISTANCE -> t("Distance", "Distance")
    }

    /** "3 of 5". */
    fun position(index: Int, count: Int) = t("$index of $count", "$index sur $count")

    fun format(format: WorkoutFormat) = when (format) {
        WorkoutFormat.CIRCUIT -> t("Circuit", "Circuit")
        WorkoutFormat.SETS -> t("Sets", "Séries")
    }

    /** "Warm-up", "Work", or "Work 2" when the workout has several work parts ([number]). */
    fun part(kind: PartKind, number: Int? = null) = when (kind) {
        PartKind.WARM_UP -> t("Warm-up", "Échauffement")
        PartKind.WORK -> if (number == null) t("Work", "Corps de séance") else t("Work $number", "Corps de séance $number")
        PartKind.COOL_DOWN -> t("Cool down", "Retour au calme")
    }

    /** "Add to warm-up", "Add to Work 2": the link at the end of a part in the builder; [number] as in [part]. */
    fun addTo(kind: PartKind, number: Int? = null) = when (kind) {
        PartKind.WARM_UP -> t("Add to warm-up", "Ajouter à l’échauffement")
        PartKind.WORK -> if (number == null) t("Add to Work", "Ajouter au corps de séance") else t("Add to Work $number", "Ajouter au corps de séance $number")
        PartKind.COOL_DOWN -> t("Add to cool down", "Ajouter au retour au calme")
    }

    /** "In Work 2", where a step sits; [number] as in [part]. */
    fun inPart(kind: PartKind, number: Int? = null) = when (kind) {
        PartKind.WARM_UP -> t("In the warm-up", "Dans l’échauffement")
        PartKind.WORK -> if (number == null) t("In Work", "Dans le corps de séance") else t("In Work $number", "Dans le corps de séance $number")
        PartKind.COOL_DOWN -> t("In the cool down", "Dans le retour au calme")
    }

    /** "once per round, 4 rounds", for a step of a circuit's repeat. */
    fun oncePerRound(roundCount: Int) = rounds(roundCount).let { t("once per round, $it", "une fois par tour, $it") }

    /** "3 sets", or "3 sets per round, 4 rounds" inside a repeat of [roundCount] rounds. */
    fun setsPerRound(sets: Int, roundCount: Int?): String {
        val count = if (sets == 1) t("1 set", "1 série") else t("$sets sets", "$sets séries")
        if (roundCount == null) return count
        val total = rounds(roundCount)
        return t("$count per round, $total", "$count par tour, $total")
    }

    fun rounds(count: Int) = if (count == 1) t("1 round", "1 tour") else t("$count rounds", "$count tours")

    /** Why a part cannot be added again. */
    val alreadyInWorkout get() = t("Already in this workout", "Déjà dans cette séance")

    /** Where a new part goes: "Goes first", "Goes last", or "Added after Work 2 as Work 3". */
    fun partGoes(kind: PartKind, after: String, name: String) = when (kind) {
        PartKind.WARM_UP -> t("Goes first", "Se place en premier")
        PartKind.WORK -> t("Added after $after as $name", "Ajouté après $after : $name")
        PartKind.COOL_DOWN -> t("Goes last", "Se place en dernier")
    }

    /** The player's part line: "Work 1 · round 2 of 4", or the [part] alone outside a repeat. */
    fun partLine(part: String, round: Int, rounds: Int) =
        if (rounds > 1) "$part · ${roundOf(round, rounds)}" else part

    /** "round 3 of 4", after a part or "Next up". */
    fun roundOf(round: Int, rounds: Int) = t("round $round of $rounds", "tour $round sur $rounds")

    fun amount(measurement: MeasurementMode, amount: Int) = when (measurement) {
        MeasurementMode.REPS -> t("$amount reps", "$amount répétitions")
        MeasurementMode.TIME, MeasurementMode.HOLD -> duration(amount)
        MeasurementMode.DISTANCE -> "$amount m"
    }

    fun duration(seconds: Int): String = when {
        seconds < 60 -> "$seconds s"
        seconds % 60 == 0 -> "${seconds / 60} min"
        else -> "${seconds / 60} min ${(seconds % 60).toString().padStart(2, '0')}"
    }

    fun kilograms(kg: Double): String {
        val text = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()
        return "$text kg"
    }

    fun side(side: Side) = when (side) {
        Side.LEFT -> t("Left side", "Côté gauche")
        Side.RIGHT -> t("Right side", "Côté droit")
        Side.BOTH -> null
    }

    val rest get() = t("Rest", "Repos")
    val freeRun get() = t("Free run", "Course libre")
    val demoIntervals get() = t("Demo intervals", "Fractionné de démo")

    /** For a session whose workout was deleted before the history kept names. */
    val session get() = t("Workout", "Séance")
    val perSide get() = t("each side", "par côté")
    val recentlyUsed get() = t("Recently used", "Utilisés récemment")
    val hold get() = t("Hold", "Maintien")
    val orWord get() = t("or", "ou")
    val andWord get() = t("and", "et")
    val timed get() = t("Timed", "Chronométré")

    fun steps(count: Int) = if (count == 1) t("1 step", "1 étape") else t("$count steps", "$count étapes")

    fun exercises(count: Int) = if (count == 1) t("1 exercise", "1 exercice") else t("$count exercises", "$count exercices")

    /** The name a copy starts with: "Upper body (copy)". */
    fun copyName(name: String) = t("$name (copy)", "$name (copie)")

    /** Only says what applies: the planned days it comes off, the sessions that stay in History. */
    fun deletePrompt(name: String, planned: Int, done: Int): DeletePrompt {
        val goes = when (planned) {
            0 -> null
            1 -> t("It comes off its planned day.", "Elle est retirée de son jour prévu.")
            else -> t("It comes off its $planned planned days.", "Elle est retirée de ses $planned jours prévus.")
        }
        val stays = when (done) {
            0 -> null
            1 -> t("The time you did it stays in History.", "La fois où vous l’avez faite reste dans l’historique.")
            else -> t("The $done times you did it stay in History.", "Les $done fois où vous l’avez faite restent dans l’historique.")
        }
        val message = listOfNotNull(goes, stays).joinToString(" ").ifEmpty { t("This can’t be undone.", "C’est définitif.") }
        return DeletePrompt(t("Delete $name?", "Supprimer « $name » ?"), message)
    }

    fun workouts(count: Int) = if (count == 1) t("1 workout", "1 séance") else t("$count workouts", "$count séances")

    fun matches(count: Int) = if (count == 1) t("1 match", "1 résultat") else t("$count matches", "$count résultats")

    fun exercisesYouCanDo(count: Int) =
        if (count == 1) t("1 exercise you can do", "1 exercice à votre portée") else t("$count exercises you can do", "$count exercices à votre portée")

    fun matchesYouCanDo(count: Int) =
        if (count == 1) t("1 match you can do", "1 résultat à votre portée") else t("$count matches you can do", "$count résultats à votre portée")

    fun aboutMinutes(minutes: Int) = t("about $minutes min", "environ $minutes min")

    /** "45 min", or "1 h" and "1 h 05" from an hour on. */
    fun longDuration(seconds: Int): String {
        if (seconds < 3600) return duration(seconds)
        val minutes = seconds % 3600 / 60
        return "${seconds / 3600} h" + if (minutes > 0) " ${minutes.toString().padStart(2, '0')}" else ""
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
            MeasurementMode.HOLD -> t("${duration(entry.amount)} hold", "${duration(entry.amount)} de maintien")
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
    fun betweenSets(rest: String) = t("$rest between sets", "$rest entre les séries")

    fun about(text: String) = t("about $text", "environ $text")

    val halfMarathon get() = t("Half marathon", "Semi-marathon")
    val marathon get() = t("Marathon", "Marathon")

    /** "Until button press", or "Until the end" on the last step, where the press finishes the session. */
    fun until(last: Boolean) = if (last) t("Until the end", "Jusqu’à la fin") else t("Until button press", "Jusqu’à l’appui")

    /** What a phase asks for in the player: "10 reps", "30 s", "400 m", or "Until you tap Next". */
    fun target(target: Target) = if (target is Target.Open) t("Until you tap Next", "Jusqu’à « Suivant »") else amount(target.measurement, target.amount)

    /** The Recover tag on rests and recover steps; null for work steps, whose part says where they are. */
    fun role(role: StepRole) = when (role) {
        StepRole.RECOVER -> t("Recover", "Récupération")
        StepRole.WORK -> null
    }

    /** The name of a role in the step select: Exercise or Recover. */
    fun roleName(role: StepRole) = role(role) ?: t("Exercise", "Exercice")

    fun times(count: Int) = t("$count times", "$count fois")
}
