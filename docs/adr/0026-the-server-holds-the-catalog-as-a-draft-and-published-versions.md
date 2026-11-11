---
status: accepted
date: 2026-11-10
---

# The server holds the catalog as a draft and published versions

Builds on [ADR 0008](./0008-curated-catalog-with-stable-slugs.md) and [ADR 0015](./0015-catalog-text-lives-in-translation-files.md).

Until now the catalog changed only by editing `catalog/data/exercises.json` and the `catalog` translation files, then shipping a build. The admin now edits it on the server and publishes a new version without a release ([#337](https://github.com/tkint/coach-poulpy/issues/337)). The server keeps each exercise on its own in Postgres, as **revisions** (`catalog_exercise_revision`): the shared `Exercise` JSON with every text as a map from language to string (ADR 0007), and its place in the catalog and its variation group and rank as columns.

- **Published versions** are numbered (`catalog_version`). A revision records the version that published it (`published_in`), and version N holds, for each exercise, its latest revision published up to N. A published revision never changes, so neither does a version. The server assembles a version into the document the app bundles (`version`, `exercises`, `variations`) with the same `Catalog.toJson` as the bundled copy, and keeps the latest one assembled in memory.
- **The draft** is the latest version with each exercise's draft revision in its place: a revision not published yet (`published_in` null, at most one per exercise). Creating, editing, retiring or offering an exercise again changes its draft revision, or starts one after its latest published revision; a draft that comes back to what the latest version holds is dropped. Nothing reaches a phone yet.
- **Publishing** adds the version and marks every draft revision as published in it. Exercises the draft didn't touch get no new row, so a publish writes only what changed. `GET /api/v1/catalog` serves the latest, so a phone stores it exactly as it stores its bundled catalog, and no phone-side change is needed (#27 downloads it).

## Rules

- **Slugs never change once published.** A slug is editable in the draft until a version holds the exercise; after that the server refuses the change (`409 slug-published`). Exercises are retired, never deleted, as ADR 0008 says, and a retired exercise can be offered again.
- **Publishing needs every text in every shipped language** (English and French): the name, each instruction and the variation note. The draft may wait for French; only English, the fallback, is needed to save. Publishing with nothing changed is refused too.
- **The next version numbers past both the latest published one and the bundled one**, so a phone that came with a newer app build still takes it.
- **Variation groups are carried over** in each exercise's revisions; editing them is not part of the admin yet.

## Where the catalog comes from

- **The first start seeds the database from the bundled catalog**: one revision per exercise, published in the bundled version. Later starts leave the database alone, even when the bundled catalog is newer.
- **The database is the source of truth for the published catalog from then on.** The repository's files stay the source of the *bundled* catalog, the copy a fresh install starts with offline, and still go through the build of ADR 0015 (text in translation files, merged at build time, checked by CI).
- **ADR 0015 still holds for the bundled build, not for the server.** On the server, text lives in each revision next to the structure, since an admin form edits both at once and a published version must hold everything a phone needs. Translators keep proofreading the repository's files in Fink. Bringing a published version back into those files before a release, so the next build bundles it, is manual for now; a tool for it is a follow-up.

## Considered options

- **Keep the repository as the only source, the server serving the build's copy.** Every catalog fix would still need a release, which is what #26 removes.
- **One JSONB document per draft and per version.** Simpler to store and to serve, but every edit would rewrite the whole catalog, two edits could overwrite each other, and no query could reach one exercise.
- **A copy of every exercise in every version.** Each version would be one plain query, but every publish would write the whole catalog again to change one exercise. With revisions, a version is rebuilt with one query (each exercise's latest revision up to it), and storage grows only with what changes.
- **Store the text in translation rows on the server, like the files of ADR 0015.** It would keep one shape across the repository and the server, but the published document merges the text back anyway, and the admin form edits an exercise as a whole.

## Consequences

- An exercise added in the admin reaches phones only through the download of #27 until its version is copied back into the repository.
- A fresh install bundles an older catalog than the server's until then; the download on launch catches it up.
- Editing the repository's catalog files directly no longer reaches phones that already took a published version with a higher number, so catalog changes go through the admin once the server is live.
