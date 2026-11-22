# Roadmap

What ships and in which order. **Version 1.0** is the app, the accounts and sync, the website and the admin as they are drawn in the agreed mockups. Everything built or mocked up today is in it. What is not mocked up yet, and everything after 1.0, is listed below with what blocks it.

Release numbers after the current one can change as the work is planned. The current version is the latest tag on the [Releases](https://github.com/tkint/coach-poulpy/releases) page.

## How it is planned

- A **phase** is a stage of the product. Phase 1 is Version 1; phase 2 is everything after 1.0.
- A **release** is a set of features that make sense together, cut when it is ready, not on a date ([Releasing](./releasing.md)). It has a version and a theme, such as `0.12 · Find and reuse`.
- A **milestone** on GitHub is one release, named after it and naming its phase. Every issue has exactly one milestone, `Later` when it is not planned.
- A **version** is `0.x` until Version 1 is complete, then `1.0`. Releases after 1.0 get their numbers when they are planned.

A GitHub release with the Android APK, plus the server deployed on Infomaniak, is production. Publishing in the app stores comes after 1.0.

## Where we are

The latest release is **v0.10.0**. The next one, 0.11, is complete on `main` and waits to be tagged.

## What already ships

- **The app on both phones.** A catalog of exercises downloaded from the server, the workout builder (circuit or sets, in parts), the library with search and filters, a workout page with its body map, the guided player, Live recording from a treadmill over Bluetooth (FTMS), the session recap, share cards, .FIT export, and workout files to send to another phone.
- **Accounts and sync.** Sign up with email and password, confirm by email, sign in and out, reset a password, delete an account, and sync workouts, sessions, planned workouts, the body log and settings between phones ([Accounts](./accounts.md), [Sync](./sync.md)).
- **The server.** The API, Postgres and a Docker deploy to one server ([Deploying the server](./deploying-the-server.md)).
- **The website.** Sign in, confirm email, reset a password, the workouts and the profile ([The web app](./web-app.md)).
- **The admin.** Audit log, accounts (find, disable, confirm by hand, delete), mail settings, server status, and the exercise catalog with drafts, publishing, retiring and offering by revision ([The admin area](./admin.md)).

## Phase 1 · Version 1

Each release is a milestone. The last column says what must happen first.

| Release | Theme | Issues | Before it can start |
| --- | --- | --- | --- |
| 0.11 | Home, Library, You | The three-tab bar, the diary Home with its Record sheet, the You page with Profile and Settings, Achievements ([#523](https://github.com/tkint/coach-poulpy/issues/523)), the discreet empty states, the Home date picker | Nothing: it is built. Tag it after a check on a phone |
| 0.12 | Find and reuse | [#511](https://github.com/tkint/coach-poulpy/issues/511) search and filters on the diary (mockup agreed), [#37](https://github.com/tkint/coach-poulpy/issues/37) swap an exercise for a variation in the builder, [#224](https://github.com/tkint/coach-poulpy/issues/224) a workout from any recorded session | #511 is ready. #37 and #224 need a mockup, and #224 needs its four decisions |
| 0.13 | Training plans | [#17](https://github.com/tkint/coach-poulpy/issues/17) follow a plan | A mockup, and the issue split into tickets after a grilling |
| 0.14 | Beyond the app | [#23](https://github.com/tkint/coach-poulpy/issues/23) Apple Health and Health Connect, [#14](https://github.com/tkint/coach-poulpy/issues/14) choose what a device measures from its page | #23 needs a real phone on both platforms. #14 needs a new design, since the switch was dropped |
| 0.15 | The admin, complete | [#381](https://github.com/tkint/coach-poulpy/issues/381) a super admin from environment variables, and the closing of the catalog workspace ([#26](https://github.com/tkint/coach-poulpy/issues/26), [#338](https://github.com/tkint/coach-poulpy/issues/338)) | #381 needs four decisions. #338 and #26 close once #389 and #390 are confirmed as replaced by the revision workflow |
| 1.0 | Version 1 complete | Whatever remains in the milestone, then the first release that is not a pre-release | |

## Phase 2 · After 1.0

Each theme is a milestone named `After 1.0 · <theme>`.

| Theme | Issues | What it waits on |
| --- | --- | --- |
| The stores | [#24](https://github.com/tkint/coach-poulpy/issues/24) the Apple Developer membership and TestFlight, then the App Store and Google Play | The maintainer's decision and date. Until then iPhone builds come from Xcode |
| Accounts and calendars | [#536](https://github.com/tkint/coach-poulpy/issues/536) Sign in with Apple and Google, [#241](https://github.com/tkint/coach-poulpy/issues/241) training days in other calendars | #536 is needed for the App Store; #241 needs plans (0.13), a mockup and its connection decision |
| Website sessions | [#29](https://github.com/tkint/coach-poulpy/issues/29) run the shared engine in the browser with Kotlin/JS, then plan, review and run sessions | A mockup |
| Sharing and coaching | Library sync or copy, coach links and comments | Not broken into issues yet |

## Not yet placed

These are in the `Later` milestone until they have a release.

- **Tailored workouts in the catalog.** Curated workouts at three levels, browsed in the catalog and locked until a person copies one to edit it ([ADR 0034](./adr/0034-curated-workouts-are-locked-catalog-items-at-three-levels.md)). The maintainer has decided the model; the issues, the admin editor and the mockups come next, and it is not placed in a release yet. Level suggestions, effort ratings after a session and curated plans stay out of the first version.
- **Rethinking the workout player and the live recorder as one idea** ([#225](https://github.com/tkint/coach-poulpy/issues/225)). A design discussion first; [ADR 0018](./adr/0018-live-plays-mixed-workouts-with-the-players-steps.md) stays in force until then.
- **Poulpy's animations** ([#537](https://github.com/tkint/coach-poulpy/issues/537)) and his looks (drawn as drafts in the brand pages).

Wanted but not planned: [#30](https://github.com/tkint/coach-poulpy/issues/30) control the treadmill, [#31](https://github.com/tkint/coach-poulpy/issues/31) heart rate straps, [#32](https://github.com/tkint/coach-poulpy/issues/32) user-created exercises, [#33](https://github.com/tkint/coach-poulpy/issues/33) progression rules, [#75](https://github.com/tkint/coach-poulpy/issues/75) guided building of a long run, [#124](https://github.com/tkint/coach-poulpy/issues/124) Poulpy's coaching style, [#228](https://github.com/tkint/coach-poulpy/issues/228) APIs and an MCP server, [#481](https://github.com/tkint/coach-poulpy/issues/481) direct Garmin telemetry, and the You blocks without a mockup ([#527](https://github.com/tkint/coach-poulpy/issues/527) to [#530](https://github.com/tkint/coach-poulpy/issues/530)).

Dropped: swapping an exercise during a session ([#38](https://github.com/tkint/coach-poulpy/issues/38)).

## Not tied to a release

Documentation and tooling work ([#227](https://github.com/tkint/coach-poulpy/issues/227) readability, [#353](https://github.com/tkint/coach-poulpy/issues/353) mockup interactions, [#398](https://github.com/tkint/coach-poulpy/issues/398) diagrams) and [#229](https://github.com/tkint/coach-poulpy/issues/229), which defines how releases are planned, go in the milestone of the next release to ship.
