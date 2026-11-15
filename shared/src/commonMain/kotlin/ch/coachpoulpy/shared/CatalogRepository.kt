package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.data.local.CatalogRevision
import ch.coachpoulpy.data.local.LocalStore

/**
 * Where the app reads the exercise catalog from: the phone's database. A fresh install holds no
 * exercises; an update from the server ([apply]) adds and replaces single exercises by their revision
 * (ADR 0030, ADR 0033).
 */
class CatalogRepository(private val store: LocalStore) {
    var current: Catalog = load()
        private set

    /** The cursor of the last update from the server, or null when there was none: ask for every exercise then. */
    val cursor: String? get() = store.catalogCursor()

    /**
     * Stores the exercises [update] publishes that are newer than the phone's, and its [cursor], all or
     * nothing. Returns how many exercises changed.
     */
    fun apply(update: List<CatalogRevision>, cursor: String): Int {
        val changed = store.applyCatalogRevisions(update, cursor)
        if (changed > 0) current = load()
        return changed
    }

    private fun load() = Catalog(store.catalogExercises(), store.catalogVariationGroups(), store.catalogRevisions())
}
