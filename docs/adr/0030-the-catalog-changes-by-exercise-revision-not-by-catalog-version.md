---
status: accepted, bundled catalog superseded by 0032
date: 2026-11-14
---

# The catalog changes by exercise revision, not by catalog version

> **Superseded in part by [ADR 0032](./0032-the-server-database-is-the-catalog-and-nothing-ships-bundled.md):** the repository's catalog files and the bundled catalog are gone; the server database is the only source.

Supersedes the catalog version of [ADR 0008](./0008-curated-catalog-with-stable-slugs.md), [ADR 0015](./0015-catalog-text-lives-in-translation-files.md) and [ADR 0026](./0026-the-server-holds-the-catalog-as-a-draft-and-published-versions.md) ([#400](https://github.com/tkint/coach-poulpy/issues/400)).

ADR 0026 numbers every publication (`catalog_version`) and serves "the latest version" whole. A catalog-wide number is a release concept the catalog doesn't need: fixing one exercise's French would make a new version of every exercise, and every phone would compare, store and download a number to learn whether it is up to date. The unit of change is the exercise. What stays: stable slugs, retiring instead of deleting, the draft, revisions stored per exercise, the translation files that own the *bundled* text, and catalog updates that need no account.

## The decision

- **An exercise has revisions.** Its published revisions are numbered 1, 2, 3… by the server (`revision`) and never change. Editing starts a draft revision after the latest published one; publishing gives it the next number. Nothing else changes: publishing an exercise writes that exercise only, and there is no catalog version, release number, snapshot or counter that names the catalog as a whole.
- **Retiring and offering again are revisions** of the exercise (the `retired` flag), published like any edit. They are lifecycle changes, not events of the catalog.
- **A variation group is no entity of its own.** Each member's revision carries its group and its rank, as in ADR 0026. Reordering a group publishes a new revision of each member whose rank changed.
- **The admin publishes exercises**: one, several or all of the pending ones, in one transaction. Publishing never touches the exercises it doesn't name.

## How a phone asks for changes: the cursor

A phone doesn't compare a catalog number. It asks for what was published since the last answer it stored.

- Every published revision takes the next number of a **publication cursor** (`seq`), a counter of publications on the server, taken under a lock on the catalog so numbers follow commit order and a phone never skips a revision committed late. This is the pull cursor of [ADR 0025](./0025-the-phone-forks-a-conflicting-workout-and-the-server-counts-changes.md) over the stream of published revisions. It is **not** a catalog version: it names a position in a stream, not a state of the catalog; nobody compares two cursors, shows one or decides anything from its value; the phone only echoes the last one it received; the bundled catalog has none; and no exercise's identity depends on it. The identity of what an exercise holds is its slug and its `revision`.
- `GET /api/v1/catalog/exercises?after=<cursor>` answers the exercises whose latest published revision is newer than the cursor, each as its full JSON with its `revision`, and the new cursor: `{cursor, exercises}`. Without `after`, it answers every exercise, which is the clean first download and the full refresh. When there is nothing newer, `204`. It needs no account.
- **The phone applies the answer in one database transaction**, the exercises and the new cursor together, so an interrupted or failed update leaves the old cursor and the old exercises, and asking again is always safe. It keeps an exercise only if the answer's `revision` is higher than its own. Offline, it uses what it has, as it does now.
- **The bundled catalog carries no cursor.** Its exercises are revision 1. The first launch after a fresh install, or after the cursor was lost, downloads the whole catalog once; every later launch downloads only the changes.

## Existing data

The beta APKs (v0.x) hold the catalog in the phone's database with the bundled file's version, and the server holds the numbered versions. Nothing is reset:

- **Phone:** a new SQLDelight migration adds the exercise's `revision` and a one-row cursor table, keeps every row and slug, and gives each stored exercise revision 1. Workouts and sessions reference exercises by slug and are not touched. The old catalog `version` is read by nothing once the app uses revisions; its column is dropped by a later migration, after the apps that read it are no longer in use (marked `CLEANUP:`).
- **Server:** a new Postgres migration adds `revision` and `seq` to `catalog_exercise_revision`, filled from the published revisions in publication order, keeping every slug and every published revision. `catalog_version` and `published_in` stay, marked `CLEANUP:`, until nothing reads them; a later migration drops them.
- **Compatibility:** no released app downloads from the server yet, so `GET /api/v1/catalog` is kept until `GET /api/v1/catalog/exercises` ships and is then removed.
- **The repository's catalog files** stay the source of the bundled catalog. Each exercise there may carry a `revision` (1 when absent). Exercises change from the admin, so CI has no check on the files, and the catalog version check of ADR 0015 is gone.

## Considered options

- **Keep the catalog version and bump it on every publish.** It is what exists; it makes one exercise's fix a catalog release and has every phone compare a number that doesn't mean anything about one exercise.
- **A catalog watermark under another name** (the highest revision, or a hash of all of them). It would behave as a catalog version, which the issue rules out. The cursor is allowed only because it is opaque to the phone and stands for a position in a stream.
- **The phone sends what it has** (every slug with its revision) and the server answers the difference. No cursor, but every launch sends and compares the whole catalog.
- **Only the full catalog, always.** The simplest contract, but every launch downloads everything to learn that nothing changed.

## Consequences

- The admin's publish and retirement screens ([#389](https://github.com/tkint/coach-poulpy/issues/389), [#390](https://github.com/tkint/coach-poulpy/issues/390)) publish exercises and no longer name a version; the admin shows what is pending per exercise.
- A phone keeps only the latest revision of an exercise it has applied. A workout shows the exercise's latest text, as it does today; what a finished session stored about its exercises is not rewritten and not changed by this decision.
- Publishing is serialised on the server, which suits one admin and a few publications a day.
- The work is split: this decision, then the server and its API, the Android and iOS clients, the admin, and the CI and documentation. The documentation that describes the running code ([the API](../the-api.md), [the admin](../admin.md), the glossary) changes with the code, not with this ADR.
