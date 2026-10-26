---
status: accepted
date: 2026-10-26
---

# Catalog text lives in translation files

Amends [ADR 0008](./0008-curated-catalog-with-stable-slugs.md).

Exercise names, instructions, variation notes and aliases move out of `catalog/data/exercises.json` into the `catalog` namespace of the translation files (ADR 0014), keyed by slug: `goblet-squat.name`, `goblet-squat.instructions.0`, `goblet-squat.variationNote`, `goblet-squat.aliases`. Aliases are one comma-separated message per language, since a message is a single string; they are never shown. `exercises.json` keeps the structure: slug, movement pattern, muscles, equipment, measurement, variations, `retired` and `version`.

The text moves so translators can edit it in Fink alongside the rest of the app. What the phone receives does not change shape: the build merges the text back into the bundled catalog, so each catalog version stays one self-contained document, copied into the database and later published by the server as ADR 0008 says, with each text as a map from language to string (`{"en": "Goblet squat", "fr": "Squat gobelet"}`). That is the JSON phones already store, so existing catalog entries decode unchanged and no migration is needed.

## Consequences

- Changing an exercise's text raises the catalog `version`, as any catalog change does.
- A slug is also a message id prefix, one more reason it never changes.
- Reordering an exercise's instructions renumbers their ids, and translators see the change as new messages.
- The build fails when an exercise has no name in a shipped language, or a message names a slug the catalog lacks.
