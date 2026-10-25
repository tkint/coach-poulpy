package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.LocalizedText
import ch.coachpoulpy.core.model.MeasurementMode
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.MuscleGroup
import ch.coachpoulpy.core.model.Side
import ch.coachpoulpy.core.model.WorkoutType

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

    fun workoutType(type: WorkoutType) = when (type) {
        WorkoutType.STRENGTH -> t("Strength", "Renforcement")
        WorkoutType.CONDITIONING -> t("Cardio circuit", "Circuit cardio")
        WorkoutType.REHAB -> t("Rehab", "Rééducation")
        WorkoutType.MOBILITY -> t("Mobility and stretching", "Mobilité et étirements")
        WorkoutType.TREADMILL -> t("Treadmill", "Tapis de course")
    }

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
    val hold get() = t("Hold", "Maintien")
    val orWord get() = t("or", "ou")
    val andWord get() = t("and", "et")
    val timed get() = t("Timed", "Chronométré")

    fun exercises(count: Int) = if (count == 1) t("1 exercise", "1 exercice") else t("$count exercises", "$count exercices")

    fun matches(count: Int) = if (count == 1) t("1 match", "1 résultat") else t("$count matches", "$count résultats")

    fun exercisesYouCanDo(count: Int) =
        if (count == 1) t("1 exercise you can do", "1 exercice à votre portée") else t("$count exercises you can do", "$count exercices à votre portée")

    fun matchesYouCanDo(count: Int) =
        if (count == 1) t("1 match you can do", "1 résultat à votre portée") else t("$count matches you can do", "$count résultats à votre portée")

    fun aboutMinutes(minutes: Int) = t("about $minutes min", "environ $minutes min")

    fun restAfter(seconds: Int) = t("rest ${duration(seconds)}", "repos ${duration(seconds)}")
}
