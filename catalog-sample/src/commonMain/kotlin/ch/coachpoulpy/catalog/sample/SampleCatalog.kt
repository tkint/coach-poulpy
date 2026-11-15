package ch.coachpoulpy.catalog.sample

import ch.coachpoulpy.catalog.Catalog

/**
 * A fixed catalog for tests: a copy of the exercises as they were when the catalog moved into the server
 * database (ADR 0033). The apps and the server never depend on it; the real catalog comes from the database.
 */
object SampleCatalog {
    val catalog: Catalog by lazy { Catalog.fromJson(SAMPLE_CATALOG_JSON) }
}
