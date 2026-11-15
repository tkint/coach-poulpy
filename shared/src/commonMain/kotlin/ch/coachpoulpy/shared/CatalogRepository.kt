package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.data.local.CatalogRevision
import ch.coachpoulpy.data.local.LocalStore

/**
 * Where the app reads the exercise catalog from: the phone's database, never the bundled file
 * directly. Each launch adds what the bundled catalog has that the database lacks; an update from the
 * server ([apply]) replaces single exercises by their revision (ADR 0030).
 */
class CatalogRepository(private val store: LocalStore, bundled: Catalog) {
    var current: Catalog
        private set

    init {
        store.applyCatalogRevisions(revisionsOf(bundled))
        current = load()
    }

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

    private fun revisionsOf(catalog: Catalog): List<CatalogRevision> {
        val place = catalog.variationGroups.flatMapIndexed { group, slugs -> slugs.mapIndexed { rank, slug -> slug to (group to rank) } }.toMap()
        return catalog.exercises.map { CatalogRevision(it, catalog.revisionOf(it.slug), place[it.slug]?.first, place[it.slug]?.second) }
    }
}
