# Coach Poulpy

A personal training platform: build workouts, follow plans, run guided sessions and track progress. The first release is a guest-only mobile app for Android and iPhone that keeps everything on the phone. The backend and website join later as a sync and sharing layer over the same Kotlin domain.

The brand, design system and architecture are published at [tkint.github.io/coach-poulpy](https://tkint.github.io/coach-poulpy/), built from `docs/`, `CONTEXT.md` and `CONTRIBUTING.md` by the VitePress project in `site/`. How work moves from an idea to a merged PR is in [CONTRIBUTING.md](CONTRIBUTING.md).

## Layout

| Path | What it is |
| --- | --- |
| `core/model` | Domain types: exercises, workouts, steps, prescriptions, plans, executions |
| `core/engine` | Timeline compiler, session state machine, event log, `WorkoutTypeModule` |
| `core/stats` | Derived metrics (placeholder) |
| `types/*` | One module per workout type: strength, conditioning, rehab, mobility, treadmill |
| `catalog` | Bundled exercise catalog, edited as JSON in `catalog/data/` |
| `data/local` | SQLDelight database on the phone |
| `device/ftms` | Bluetooth treadmills (FTMS): decoding, connection with reconnect, Bluetooth through Kable |
| `export/fit` | .FIT export for Garmin Connect and Strava (placeholder) |
| `shared` | Umbrella module the apps use; exported to iOS as the `Shared` framework |
| `app-android` | Jetpack Compose app |
| `app-ios` | SwiftUI app, project generated with XcodeGen |
| `server` | http4k API (placeholder with `/health`) |
| `web` | Vue 3 + TypeScript website (placeholder) |
| `build-logic` | Gradle convention plugins: `coachpoulpy.kmp-library` (Android, iOS, JVM) and `coachpoulpy.kmp-web-library` (the same plus JavaScript, for the modules the website runs) |
| `design` | Brand tokens (`tokens.json`) and the script that generates the Android, iOS and web theme files from them |
| `docs` | Architecture, ADRs, design system and brand, read by agents and published on the docs site |
| `site` | The docs site (VitePress), deployed to GitHub Pages by `.github/workflows/pages.yml` |
| `tools/compose.yaml` | Optional local Postgres, not needed yet |

Library versions live in `gradle/libs.versions.toml`. Package and app id: `ch.coachpoulpy`.

## Prerequisites

- JDK 21
- Android Studio (or the Android SDK with `ANDROID_HOME` set) for the Android app
- A Mac with Xcode 16+ and [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`) for the iOS app
- Node 22 for the website

## Run it on the dev computer

```sh
# Shared logic (JVM and JavaScript) and server tests, no Android SDK or Mac needed
./gradlew jvmTest jsTest :server:test

# Android: build, or install on a phone connected over USB
./gradlew :app-android:assembleDebug
./gradlew :app-android:installDebug

# iOS: generate the Xcode project, then open it and press Run
cd app-ios && xcodegen && open CoachPoulpy.xcodeproj

# Server on http://localhost:8080
./gradlew :server:run

# Website on http://localhost:5173 (talks to the server through /api)
cd web && npm install && npm run dev
```

The iOS build runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` itself, so the Kotlin code is rebuilt whenever you build in Xcode.

## The exercise catalog

`catalog/data/exercises.json` is the copy of the catalog shipped in the app. On start, the app copies it into its database when its `version` is higher than the one the phone already has, and every screen reads the catalog from the database.

To change it today, edit the JSON, raise `version` by one and ship a new build. Keep every `slug`: workouts point at exercises by slug. To stop offering an exercise, set `"retired": true` rather than deleting it, so workouts that use it still open.

`variations` lists the catalog's variation groups: each is the slugs of one movement made easier or harder, ordered from easiest to hardest. An exercise is in at most one group, and a retired one stays in its group but isn't shown. Every exercise in a group carries a `variationNote` in both languages, the one line the variations sheet shows under its name ("Knees down, a shorter lever").

An exercise's `equipment` lists what it needs, each need a list of alternatives: `[["DUMBBELL", "KETTLEBELL"]]` is a dumbbell or a kettlebell, `[["DUMBBELL"], ["BENCH"]]` is dumbbells and a bench. No need means bodyweight; `BODYWEIGHT` and `MAT` are never needs. Equipment that helps but isn't needed (a mat, a treadmill for a walk) goes in `optionalEquipment`. The Equipment filter shows an exercise when it uses something ticked, needed or optional, and every need has one alternative ticked; bodyweight-only exercises hide while anything is ticked, unless "No equipment" is.

An exercise's optional `aliases` are other names people search with, per language (`{ "en": ["RDL"], "fr": ["abdos"] }`). The search reads them like names but never shows them, and an alias can't be empty.

Later, an admin menu on the server will publish new versions in the same format, and the app will download them on launch with no account needed (`CatalogRepository.apply`). That is separate from syncing a user's own data, so guests get catalog updates too.

## Installing on family phones

- **Android:** every CI run uploads a debug APK as the `coach-poulpy-debug-apk` artifact. Download it on the phone and allow installing from that source. Each build has a higher version number and is signed with the same key, so a new APK installs over the old one and keeps the phone's data. `./gradlew :app-android:assembleRelease` gives a smaller build, signed with the same key.
- **iPhone:** set your Apple team in Xcode (Signing & Capabilities) and run on a connected iPhone. With a free Apple account the app expires after 7 days; TestFlight needs the paid Apple Developer Program.

### The signing key

Android only installs an update when it is signed with the same key as the app already on the phone. CI signs every APK with one fixed key, kept in two repository secrets:

| Secret | What it holds |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | The keystore file (`.jks`), base64 encoded on one line |
| `ANDROID_KEYSTORE_PASSWORD` | Its password (the key inside, `poulpy`, uses the same one) |

To make them, run `tools/make-signing-key.sh` once on your own computer (it needs a JDK) and follow what it prints. Keep the `.jks` file and its password in a password manager: losing them means every phone has to uninstall the app once more. Without the secrets, for example on a pull request from a fork, CI still builds but signs with a throwaway key and says so in a warning.

To sign local builds with the same key, put `storeFile` and `storePassword` in `app-android/keystore.properties`, which is not committed. The build also reads `POULPY_KEYSTORE_FILE`, `POULPY_KEYSTORE_PASSWORD`, and optionally `POULPY_KEY_ALIAS` and `POULPY_KEY_PASSWORD`, from the environment.

## Treadmill (Bluetooth FTMS)

The **Treadmill** tab finds a treadmill that speaks the Bluetooth Fitness Machine Service, shows its live readings and records a session, either free or following a treadmill workout (a built-in demo workout is there until the workout builder exists). Recording is read-only: the app never changes the treadmill's speed or incline yet, though `device/ftms` already encodes those commands.

Testing on a new machine: the bottom of the tab lists what the treadmill says it can report and counts any packets the decoder could not read. Treadmills differ in which fields they send, so a metric that never shows up is usually one the machine does not send.

## History

The **History** tab has two views. **Calendar** shows a month, Monday first, with the days you trained filled in; swipe or use the arrows to go back through earlier months. Under it, that month's sessions are grouped by day, newest first, and tapping a day scrolls the list to it (a day without sessions shows as a rest day). **Stats** shows this week's and this month's totals, charts of the last eight weeks (active minutes, treadmill kilometres), the total number of sessions, the longest run and Poulpy's full octopuses. Each session's totals (`core/stats`) are saved next to it when it ends, so the history opens without replaying the event log; sessions recorded before the history existed are summed up once, the first time the tab opens. Sessions can be deleted from their detail screen.

## Continuous integration

`.github/workflows/ci.yml` runs the shared tests on the JVM and in JavaScript, the server tests, builds the Android APK, builds the iOS app for the simulator on macOS, and builds the website.
