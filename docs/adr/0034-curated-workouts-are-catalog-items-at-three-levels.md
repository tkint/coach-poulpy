---
status: accepted
date: 2026-11-22
---

# Curated workouts are catalog items at three levels

The catalog held only exercises ([ADR 0033](./0033-the-server-database-is-the-catalog-and-nothing-ships-bundled.md)). It also offers **curated workouts**: ready-made workouts at different levels. The shape was set by looking at how a personalised training app organises its sessions, without borrowing its names or content, and decided by the maintainer.

## The decision

- **A curated workout is a catalog item**, held in the server database beside the exercises. The admin writes, publishes and retires it, and it has a slug that never changes and numbered revisions ([ADR 0030](./0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md)). Phones download it item by item, without an account, and it is always available, like an exercise.
- **It syncs like a shared plan** ([ADR 0009](./0009-shared-plans-are-synced-or-copied.md)). A person can start it from the catalog, or save it to their library, synced to its latest revision or as a copy. Editing a synced one makes a personal copy, which no longer follows the catalog. A copy never changes.
- **Curated and personal share one list.** Workouts, and likewise plans, show in a single list on the phone. A **Curated** badge marks the catalog ones, and the list filters by origin (curated or mine) and by level.
- **Workouts and plans both have a level**, one of three fixed ones, the same for every goal and named starter, steady and strong until the brand pages settle the words. A curated one always has a level; a person's own can have one, which they set. A person picks a level; nothing suggests one.
- **One workout per level.** The same workout at three levels is three curated workouts, grouped in a workout family, so each level can change its exercises, reps, rests and rounds freely. Swapping exercises by level would need new engine logic and is not done.
- **The first version is a browsable list by level.** The admin writes about ten families to start.

## Plans

Catalog plans ([#17](https://github.com/tkint/coach-poulpy/issues/17)) already work this way under ADR 0009, and an enrollment pins one plan version. Curated workouts take the same save, sync and copy rule; plans get a level the same way as workouts.

## Left out of the first version

Suggesting a level from a questionnaire, an effort rating after a session, filters by time or equipment, and catalog plans, which come with #17. Each is its own decision later.

## Consequences

- The catalog's revision, retire and offer-again rules apply to curated workouts unchanged. A retired curated workout stays so copies and sessions still open.
- A curated workout uses exercises by slug. Retiring an exercise a curated workout uses needs the admin to see the workouts it affects.
- A session keeps the revision it was done against, as with a plan version, so a catalog fix never rewrites history.
- The workout and plan lists need an origin badge and filter, and a level filter: a mockup is needed before building. Whether a person's own item can go without a level is left to it.
