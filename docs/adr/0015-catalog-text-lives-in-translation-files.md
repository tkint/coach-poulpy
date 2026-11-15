---
status: accepted, amended by 0026, catalog version superseded by 0030, superseded by 0033
date: 2026-10-26
---

# Catalog text lives in translation files

> **Superseded by [ADR 0033](./0033-the-server-database-is-the-catalog-and-nothing-ships-bundled.md):** the `catalog` translation namespace, its merge and the bundled catalog are gone; exercise text lives in the server database.

> **Superseded in part by [ADR 0030](./0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md):** the catalog `version` and the CI check that raises it are replaced by per-exercise revisions. Text in translation files, keyed by slug, still holds for the bundled catalog.

Amends [ADR 0008](./0008-curated-catalog-with-stable-slugs.md).

Exercise names, instructions, variation notes and aliases move out of `catalog/data/exercises.json` into the `catalog` namespace of the translation files (ADR 0014), keyed by slug: `goblet-squat.name`, `goblet-squat.instructions.0`, `goblet-squat.variationNote`, `goblet-squat.aliases`. Aliases are one comma-separated message per language, since a message is a single string; they are never shown. `exercises.json` keeps the structure: slug, movement pattern, muscles, equipment, measurement, variations, `retired` and `version`.

The text moves so translators can edit it in Fink alongside the rest of the app. What the phone receives does not change shape: the build merges the text back into the bundled catalog, so each catalog version stays one self-contained document, copied into the database and later published by the server as ADR 0008 says, with each text as a map from language to string (`{"en": "Goblet squat", "fr": "Squat gobelet"}`). That is the JSON phones already store, so existing catalog entries decode unchanged and no migration is needed.

## Consequences

- Changing an exercise's text raises the catalog `version`, as any catalog change does. Fink's pull requests don't, so CI fails when catalog text changes and `version` doesn't, and the version is raised before merging.
- A slug is also a message id prefix, one more reason it never changes.
- Reordering an exercise's instructions renumbers their ids, and translators see the change as new messages.
- The build fails when an exercise has no name in a shipped language, or a message names a slug the catalog lacks.
