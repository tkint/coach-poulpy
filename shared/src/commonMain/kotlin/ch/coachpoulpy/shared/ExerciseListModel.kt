package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog

/** One row of the exercise list, already localized so the UI only renders it. */
data class ExerciseRow(val slug: String, val name: String, val details: String)

class ExerciseListModel(catalog: Catalog, languageCode: String) {
    val rows: List<ExerciseRow> = catalog.exercises
        .map { exercise ->
            ExerciseRow(
                slug = exercise.slug.value,
                name = exercise.name.forLanguage(languageCode),
                details = exercise.muscleGroups.joinToString(" · ") { it.name.lowercase().replace('_', ' ') },
            )
        }
        .sortedBy { it.name.lowercase() }
}
