package ch.coachpoulpy.shared

import ch.coachpoulpy.catalog.sample.SampleCatalog
import ch.coachpoulpy.data.local.CatalogRevision

/** The app graph of a test, holding the sample exercises as a phone does after its first download (ADR 0033). */
fun AppGraph.withSampleCatalog(): AppGraph = apply {
    val sample = SampleCatalog.catalog
    val place = sample.variationGroups.flatMapIndexed { group, slugs -> slugs.mapIndexed { rank, slug -> slug to (group to rank) } }.toMap()
    catalogRepository.apply(sample.exercises.map { CatalogRevision(it, 1, place[it.slug]?.first, place[it.slug]?.second) }, "0")
}
