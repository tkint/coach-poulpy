# Architecture

Coach Poulpy is **local-first**: the mobile app holds the whole domain model and runs alone as a guest on family phones, and the server and website join later as a sync and sharing layer over the same model. One Kotlin Multiplatform core owns the domain, the workout engine and the database on Android and iOS, so the two apps cannot drift apart. The decisions behind this are recorded as [ADRs](./adr/); the words are defined in the [glossary](../CONTEXT.md).

## Decisions for v1

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
| Language and units | French and English, metric and imperial | |
| Hosting | The whole stack runs on the dev computer; Docker optional | A provider when accounts go live |

## Delivery phases

```mermaid
flowchart LR
    p1["1 · Guest app<br/>catalog, builder, player,<br/>plans, history, FIT export"]
    p2["2 · Treadmill<br/>FTMS recording,<br/>free or matched to a plan"]
    p3["3 · Accounts and sync<br/>server, Postgres, sign-in,<br/>push and pull sync"]
    p4["4 · Website<br/>plan, review and run<br/>guided sessions"]
    p5["5 · Sharing and coaching<br/>library sync or copy,<br/>coach links, comments"]
    p1 --> p3 --> p4 --> p5
    p2 -. alongside .- p1
```

Each phase adds a layer without reworking the one below it, because the domain, engine and storage are already shared. Phase 2 runs alongside phase 1 because the treadmill link was the riskiest piece. GitHub milestones follow these phases.

## Domain model

Every workout shares the same concepts, whatever its format. Planned content is versioned and immutable per version, so a session always points at the exact version it followed, a synced library entry follows the latest version, and a copy forks from one.

| Concept | What it holds |
| --- | --- |
| Exercise | Stable slug, names in French and English, movement pattern, muscle groups, equipment needs (each a list of alternatives) and optional equipment, measurement (reps, time, distance, hold), per-side flag, images, instruction steps |
| Workout | Name, format (circuit or sets), parts (an optional warm-up, one or more work parts, an optional cool down) each holding a tree of steps, estimated duration, tags |
| Step | An exercise step (exercise, prescription, and a role: exercise or recover), a group (sequence, superset, circuit, interval set with rounds), or a rest |
| Prescription | Sets, and per set a target (reps, duration, distance, hold), an optional load, intensity (RPE, speed, incline, heart rate zone), tempo, and rest before the next set |
| Plan | Name, goal, weeks of days, each day holding zero or more workouts |
| Enrollment | A person following a plan version from a start date, with one calendar entry per scheduled workout (planned, done, skipped, moved) |
| Goal | A measurable target with an optional deadline, computed from sessions |
| Session | One workout actually done: start and end, source (guided, free, device only), optional link to the planned workout version and calendar entry, set results, samples, notes, effort and pain |

Statistics are derived, never typed in. `core/stats` computes metrics from sessions: personal records, weekly load and goal progress all come from the same computation, on the phone for guests and on the server for accounts.

Sharing and coaching add three small concepts: a library entry (synced or copy), a coach link (status, revocation date) and a comment attached to a session, a set result or a calendar entry.

## Formats, parts and the engine

A workout's format is chosen when it is created ([ADR 0013](./adr/0013-workouts-have-a-format-not-a-type.md)):

| Format | Built as | An exercise holds |
| --- | --- | --- |
| Circuit | Rounds through several exercises: a repeat around them | One set: reps, time or hold and a load; a walk or run has a duration and an intensity |
| Sets | One exercise at a time, all its sets, then the next; repeats for supersets | Its sets, each with a target and load, and the rest between sets |

Its parts run in order: an optional warm-up, one or more work parts, an optional cool down. How a step finishes comes from its target alone: a time or a hold runs a timer; reps, a distance (unless a treadmill reports it) or "until button press" wait for a tap.

The engine compiles a workout into a flat **timeline** of work, rest and transition phases, then runs it as a state machine that the UI only observes. The clock is injected, so the engine is testable without waiting. It emits **cues** (3-2-1, switch sides, next exercise) that each platform turns into sound, vibration and text. Every action is a **session event** written at once, so a killed app resumes at the same second. Home shows a session left open as a card with Open and Discard; Discard deletes it, with everything it recorded. Open brings it back paused at its last event, so the time the app was gone never counts: a guided session in the player, a Live recording in Live with the question whether it is finished. A session closed without being finished, because another one started or its workout is gone, ends at its last event, never at its start, and keeps its totals.

## Modules

| Module | Runs on | Responsibility |
| --- | --- | --- |
| `core/model` | Android, iOS, JVM, JS | Domain types, serializable, no dependencies |
| `core/engine` | Android, iOS, JVM, JS | Timeline compiler, session state machine, event fold |
| `types/treadmill` | Android, iOS, JVM, JS | Treadmill recording, summaries and step estimate |
| `core/stats` | Android, iOS, JVM | Metrics, records, goal progress, weekly summaries |
| `catalog` | Android, iOS, JVM | The bundled, versioned exercise catalog, merged with its text at build time |
| `data/local` | Android, iOS | SQLDelight schema, repositories, event storage, migrations |
| `data/sync` (later) | Android, iOS | Ktor client and the sync protocol; off for guests |
| `device/ftms` | Android, iOS | FTMS decoding in common code; Bluetooth through Kable |
| `export/fit` | Android, iOS, JVM | A small .FIT encoder |
| `shared` | Android, iOS | App view models; exported to iOS as the `Shared` framework |
| `app-android` | Android | Compose UI, a connected-device foreground service for Live recordings, Health Connect |
| `app-ios` | iOS | SwiftUI UI, the Bluetooth background mode with state restoration, HealthKit |
| `web` | Browser | Vue 3, TypeScript, Vite, Pinia; the engine through Kotlin/JS |
| `server` | JVM | http4k routes, Flyway, jOOQ, reusing model and stats for validation |
| `tools/i18n` | JVM, build time | Generates Android and iOS string files, the shared `DomainText` and the merged catalog from `i18n/messages` |

## Text and translations

Every word a person reads is in `i18n/messages/<locale>/<namespace>.json` ([ADR 0014](./adr/0014-text-lives-in-one-json-file-per-language-and-namespace.md)). At build time `tools/i18n`, run by the `coachpoulpy.i18n` Gradle plugin, writes the Android `strings.xml`, the iOS `.strings` and `.stringsdict`, the shared `DomainText` object (one typed function per `domain` message, so a wrong id or argument fails the build) and the catalog with its text merged back in ([ADR 0015](./adr/0015-catalog-text-lives-in-translation-files.md)). None of it is committed, and the build fails when a shipped language lacks a message. How to add a string is in [the translation files' README](../i18n/README.md).

## Storage and sync

- The phone database is the source of truth; the server is a replica ([ADR 0001](./adr/0001-local-first-phone-is-the-source-of-truth.md)).
- Ids are UUIDv7 made on the device ([ADR 0005](./adr/0005-ids-are-uuidv7-made-on-the-device.md)).
- Sessions never conflict; workouts and plans keep both edits; small fields take the latest write ([ADR 0006](./adr/0006-sync-keeps-both-edits.md)).
- The website is online-first, using the same push and pull calls, and buffers a running session's events in IndexedDB.
- The catalog ships with the app and updates without an account ([ADR 0008](./adr/0008-curated-catalog-with-stable-slugs.md)).

## Devices

A treadmill is the first **metric source**: anything that turns a device into timestamped samples (speed, incline, distance, heart rate, cadence). The engine only sees samples. On Android a Live recording runs in a connected-device foreground service, from Start until it finishes or is discarded, with an ongoing notification that opens Live and has Pause or Resume and Finish buttons; on Android 14 and later it needs the Bluetooth permission, and without it a timer-only recording runs in the app alone. On iOS the Bluetooth background mode keeps recording on a locked phone, and Kable's central has a restore identifier so the system relaunches the app for the device's data after closing it. Guided sessions in the player have neither: they rely on the screen staying on, and on resuming from their events. A dropped link is retried for 60 seconds while the session keeps its timer, and the gap is marked in the samples rather than filled. Many treadmills send only some FTMS fields, so the decoder reads the feature flags first and the UI hides what a machine does not send.

## Accounts and security

- A guest's data never leaves the phone; the operating system's file encryption protects it.
- Sign-up pushes the whole local database as a first sync, keeping ids.
- Email and password hashed with Argon2id, plus Sign in with Apple and Google, whose ID tokens the server verifies.
- 15-minute access tokens and rotating refresh tokens stored hashed; Keychain or Android Keystore on phones; an HttpOnly, SameSite cookie on the web.
- One http4k filter resolves the user, every query is scoped by owner, and coach access is checked against an active coach link on each request.
- Health data is sensitive under the GDPR: consent at sign-up, export as JSON and FIT, full deletion, hosting in the EU.

## Deployment

The first release needs no server. Android phones install the signed APK from CI; iPhones need Xcode (7-day installs with a free account) or TestFlight with a paid Apple Developer membership. The server builds into one JVM program that can run in a container, on a VPS or on a managed platform, chosen when accounts go live.
