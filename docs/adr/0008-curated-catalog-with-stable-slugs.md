---
status: accepted, amended by 0015, catalog version superseded by 0030, bundled catalog superseded by 0033
date: 2026-10-24
---

# A curated catalog with stable slugs, updated without an account

> **Also superseded in part by [ADR 0033](./0033-the-server-database-is-the-catalog-and-nothing-ships-bundled.md):** nothing ships bundled any more; the server database is the catalog and phones download it.

> **Superseded in part by [ADR 0030](./0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md):** the catalog version number below no longer exists; exercises change by revision. Stable slugs, retiring instead of deleting and updates without an account still hold.

The catalog is curated only: no user-created exercises in v1, to avoid duplicates. Every exercise has a slug that never changes, because workouts reference exercises by slug; an exercise is retired, never deleted. The catalog carries a version number; the app copies its bundled catalog into the database when the version is higher, and a future admin menu on the server publishes new versions in the same format, downloaded on launch by guests too. Catalog updates are separate from syncing a user's own data.

## Consequences

- User-created exercises, when they come, need duplicate detection against slug, names and movement pattern.
