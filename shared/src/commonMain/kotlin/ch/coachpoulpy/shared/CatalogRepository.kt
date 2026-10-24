package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.data.local.LocalStore

/**
 * Where the app reads the exercise catalog from: the phone's database, never the
 * bundled file directly. A fresh install, or an app update that ships a newer
 * bundled catalog, seeds the database; a future server download goes through [apply].
 */
class CatalogRepository(private val store: LocalStore, bundled: Catalog) {
    var current: Catalog
        private set

    init {
        if ((store.catalogVersion() ?: 0) < bundled.version) {
            store.replaceCatalog(bundled.version, bundled.exercises)
        }
        current = load()
    }

    /** Stores [update] if it is newer than what the phone has. Returns whether it was applied. */
    fun apply(update: Catalog): Boolean {
        if (update.version <= current.version) return false
        store.replaceCatalog(update.version, update.exercises)
        current = load()
        return true
    }

    private fun load() = Catalog(store.catalogVersion() ?: 0, store.catalogExercises())
}
