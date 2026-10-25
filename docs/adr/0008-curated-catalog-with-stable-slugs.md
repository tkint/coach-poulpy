---
status: accepted
date: 2026-10-24
---

# A curated catalog with stable slugs, updated without an account

The catalog is curated only: no user-created exercises in v1, to avoid duplicates. Every exercise has a slug that never changes, because workouts reference exercises by slug; an exercise is retired, never deleted. The catalog carries a version number; the app copies its bundled catalog into the database when the version is higher, and a future admin menu on the server publishes new versions in the same format, downloaded on launch by guests too. Catalog updates are separate from syncing a user's own data.

## Consequences

- User-created exercises, when they come, need duplicate detection against slug, names and movement pattern.
