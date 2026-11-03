package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.core.model.Workout

/**
 * The name a workout gets when it is saved or imported without one: its first exercise, then "(+2)" for
 * each other exercise it uses, in the person's language ("Squat (+2)"). An exercise done twice counts once.
 */
internal fun Workout.autoName(catalog: Catalog, labels: Labels): String {
    val names = exerciseSteps(this).map { step -> catalog[step.exercise]?.let { labels.text(it.name) } ?: step.exercise.value }.distinct()
    val first = names.firstOrNull() ?: return labels.autoNameEmpty()
    return labels.autoName(first, names.size - 1)
}
