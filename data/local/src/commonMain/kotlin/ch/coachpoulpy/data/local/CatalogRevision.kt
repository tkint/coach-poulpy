package ch.coachpoulpy.data.local

import ch.coachpoulpy.core.model.Exercise

/**
 * One exercise at one revision, as the server gives it (ADR 0030): its place in a
 * variation group (the group's number and its rank, easiest first), both null when it has none.
 */
data class CatalogRevision(val exercise: Exercise, val revision: Int, val variationGroup: Int? = null, val variationRank: Int? = null)
