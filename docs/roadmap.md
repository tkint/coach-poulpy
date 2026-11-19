# Roadmap

What ships and in which order. A **phase** is a layer of the product ([architecture](./architecture.md#delivery-phases)); a **release** is a set of features inside a phase that makes sense together, cut when it is ready, not on a date.

> **Proposal.** The grouping below is a draft for the maintainer to approve in [#229](https://github.com/tkint/coach-poulpy/issues/229). Version numbers and release names are not final, and the GitHub milestones still follow the phases until it is decided. The current version is the latest tag on the [Releases](https://github.com/tkint/coach-poulpy/releases) page.

## What v1 includes

| Topic | v1 | Later |
| --- | --- | --- |
| Audience | The family, as guests | A public app |
| Accounts | Guest mode, all data on the phone; email and password, Sign in with Apple and Google when the server arrives | Guest data moves into a new account on sign-up |
| Website | Needs an account; plans, manages, reviews and runs guided sessions without devices | |
| Catalog | Built-in and curated, with simple images; no user-created exercises | User-created exercises with duplicate detection |
| Workouts | Circuit or sets, in parts (warm-up, work, cool down); strength, cardio, rehab, stretching and treadmill walks and runs | Outdoor running, walking, swimming with GPS; workout tags |
| Treadmill | FTMS read and record, alone or checked against a planned workout | Speed and incline control per phase |
| Progression | Every week written out by hand | Rules from recognised protocols, driven by sessions and goals |
| Sharing | Save to library synced with the author or as a copy | |
| Coaching | Athlete invites a coach and can revoke; coach edits assigned plans and comments on sessions | |
| Export | .FIT file for Garmin Connect and Strava; optional Apple Health and Health Connect | Direct Garmin and Strava links |
| Language and units | French and English, metric and imperial: distances, speeds and weights follow the setting (weights are stored in kilograms and shown in pounds in imperial), except a distance picked in whole miles or kilometres, which keeps the unit it was picked in | |
| Hosting | The whole stack runs on the dev computer; the server deploys with Docker to one server ([ADR 0019](./adr/0019-the-server-runs-with-docker-compose-on-one-server.md)) | A bigger machine or a managed database when it runs short |

## Phase 1 · Guest app

Everything on the phone, no account. Phase 1 is done when the app is complete on both platforms: **1.0**.

| Release | Theme | Issues |
| --- | --- | --- |
| 0.6 | Easier to read and use | [#222](https://github.com/tkint/coach-poulpy/issues/222) smaller Home cards, [#223](https://github.com/tkint/coach-poulpy/issues/223) exercise details in the session recap, [#221](https://github.com/tkint/coach-poulpy/issues/221) back from devices, [#220](https://github.com/tkint/coach-poulpy/issues/220) a clearer pause button |
| 0.7 | Reuse and share workouts | [#224](https://github.com/tkint/coach-poulpy/issues/224) a workout from any session, [#152](https://github.com/tkint/coach-poulpy/issues/152) details before importing, [#37](https://github.com/tkint/coach-poulpy/issues/37) and [#38](https://github.com/tkint/coach-poulpy/issues/38) swap an exercise for a variation |
| 0.8 | Training plans | [#17](https://github.com/tkint/coach-poulpy/issues/17) follow a plan |
| 0.9 | Beyond the app | [#22](https://github.com/tkint/coach-poulpy/issues/22) FIT export, [#23](https://github.com/tkint/coach-poulpy/issues/23) Apple Health and Health Connect, [#177](https://github.com/tkint/coach-poulpy/issues/177) app icon, [#24](https://github.com/tkint/coach-poulpy/issues/24) TestFlight |
| 1.0 | Phase 1 complete | Whatever remains, then the stores |

## Phase 2 · Treadmill

Runs alongside phase 1, because the treadmill link was the riskiest piece. Releases are cut with the phase 1 release they fall in.

| Theme | Issues |
| --- | --- |
| Live and the player work as one | [#225](https://github.com/tkint/coach-poulpy/issues/225) how much of the player Live shares ([ADR 0018](./adr/0018-live-plays-mixed-workouts-with-the-players-steps.md)), [#14](https://github.com/tkint/coach-poulpy/issues/14) choose what a device measures |

## Phase 3 · Accounts and sync

| Theme | Issues |
| --- | --- |
| A server to host | [#28](https://github.com/tkint/coach-poulpy/issues/28) hosting provider, [#27](https://github.com/tkint/coach-poulpy/issues/27) catalog updates on launch |
| Accounts | [#25](https://github.com/tkint/coach-poulpy/issues/25) accounts and sync between phones, [#26](https://github.com/tkint/coach-poulpy/issues/26) admin menu |

## Phase 4 · Website

[#29](https://github.com/tkint/coach-poulpy/issues/29) run the shared engine in the browser with Kotlin/JS, then plan, review and run guided sessions.

## Phase 5 · Sharing and coaching

Library sync or copy, coach links and comments. Not broken into issues yet.

## Later

Wanted, not planned: [#30](https://github.com/tkint/coach-poulpy/issues/30) control the treadmill, [#31](https://github.com/tkint/coach-poulpy/issues/31) heart rate straps, [#32](https://github.com/tkint/coach-poulpy/issues/32) user-created exercises, [#33](https://github.com/tkint/coach-poulpy/issues/33) progression rules, [#75](https://github.com/tkint/coach-poulpy/issues/75) guided building of a long run, [#124](https://github.com/tkint/coach-poulpy/issues/124) Poulpy's coaching style, [#228](https://github.com/tkint/coach-poulpy/issues/228) APIs and an MCP server.

## Not tied to a release

Documentation work ([#226](https://github.com/tkint/coach-poulpy/issues/226) diagrams, [#227](https://github.com/tkint/coach-poulpy/issues/227) readability) and [#229](https://github.com/tkint/coach-poulpy/issues/229), which defines how releases are planned, go in whichever release is open.
