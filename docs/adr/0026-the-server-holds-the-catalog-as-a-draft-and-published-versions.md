---
status: accepted
date: 2026-11-10
---

# The server holds the catalog as a draft and published versions

Builds on [ADR 0008](./0008-curated-catalog-with-stable-slugs.md) and [ADR 0015](./0015-catalog-text-lives-in-translation-files.md).

Until now the catalog changed only by editing `catalog/data/exercises.json` and the `catalog` translation files, then shipping a build. The admin now edits it on the server and publishes a new version without a release ([#337](https://github.com/tkint/coach-poulpy/issues/337)). The server keeps it in Postgres in two parts:

- **A draft**: one row per exercise, retired ones included, holding the shared `Exercise` JSON with every text as a map from language to string (ADR 0007). The admin creates, edits and retires exercises there; nothing reaches a phone yet.
- **Published versions**, numbered, each the whole catalog document in the format the app bundles: `version`, `exercises`, `variations`, written by the same `Catalog.toJson` as the bundled copy. A version never changes once written. `GET /api/v1/catalog` serves the latest, so a phone stores it exactly as it stores its bundled catalog, and no phone-side change is needed (#27 downloads it).

## Rules

- **Slugs never change once published.** A slug is editable in the draft until a version holds the exercise; after that the server refuses the change (`409 slug-published`). Exercises are retired, never deleted, as ADR 0008 says.
- **Publishing needs every text in every shipped language** (English and French): the name, each instruction and the variation note. The draft may wait for French; only English, the fallback, is needed to save. Publishing with nothing changed is refused too.
- **The next version numbers past both the latest published one and the bundled one**, so a phone that came with a newer app build still takes it.
- **Variation groups are carried over** from the latest version; editing them is not part of the admin yet.

## Where the catalog comes from

- **The first start seeds the database from the bundled catalog**: its version becomes the first published one and its exercises the draft. Later starts leave the database alone, even when the bundled catalog is newer.
- **The database is the source of truth for the published catalog from then on.** The repository's files stay the source of the *bundled* catalog, the copy a fresh install starts with offline, and still go through the build of ADR 0015 (text in translation files, merged at build time, checked by CI).
- **ADR 0015 still holds for the bundled build, not for the server.** On the server, text lives in the draft next to the structure, since an admin form edits both at once and a published version must be self-contained. Translators keep proofreading the repository's files in Fink. Bringing a published version back into those files before a release, so the next build bundles it, is manual for now; a tool for it is a follow-up.

## Considered options

- **Keep the repository as the only source, the server serving the build's copy.** Every catalog fix would still need a release, which is what #26 removes.
- **One JSONB document for the whole draft.** Simpler to store, but every edit would rewrite the catalog and two edits could overwrite each other; one row per exercise keeps a write to the exercise it touches.
- **Store the text in translation rows on the server, like the files of ADR 0015.** It would keep one shape across the repository and the server, but the published document merges the text back anyway, and the admin form edits an exercise as a whole.

## Consequences

- An exercise added in the admin reaches phones only through the download of #27 until its version is copied back into the repository.
- A fresh install bundles an older catalog than the server's until then; the download on launch catches it up.
- Editing the repository's catalog files directly no longer reaches phones that already took a published version with a higher number, so catalog changes go through the admin once the server is live.
