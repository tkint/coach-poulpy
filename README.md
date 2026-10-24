# Coach Poulpy

A personal training platform: build workouts, follow plans, run guided sessions and track progress. The first release is a guest-only mobile app for Android and iPhone that keeps everything on the phone. The backend and website join later as a sync and sharing layer over the same Kotlin domain.

## Layout

| Path | What it is |
| --- | --- |
| `core/model` | Domain types: exercises, workouts, steps, prescriptions, plans, executions |
| `core/engine` | Timeline compiler, session state machine, event log, `WorkoutTypeModule` |
| `core/stats` | Derived metrics (placeholder) |
| `types/*` | One module per workout type: strength, conditioning, rehab, mobility |
| `catalog` | Bundled exercise catalog, edited as JSON in `catalog/data/` |
| `data/local` | SQLDelight database on the phone |
| `export/fit` | .FIT export for Garmin Connect and Strava (placeholder) |
| `shared` | Umbrella module the apps use; exported to iOS as the `Shared` framework |
| `app-android` | Jetpack Compose app |
| `app-ios` | SwiftUI app, project generated with XcodeGen |
| `server` | http4k API (placeholder with `/health`) |
| `web` | Vue 3 + TypeScript website (placeholder) |
| `build-logic` | Gradle convention plugins: `coachpoulpy.kmp-library` (Android, iOS, JVM) and `coachpoulpy.kmp-web-library` (the same plus JavaScript, for the modules the website runs) |
| `design` | Brand tokens (`tokens.json`) and the script that generates the Android, iOS and web theme files from them |
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

Later, an admin menu on the server will publish new versions in the same format, and the app will download them on launch with no account needed (`CatalogRepository.apply`). That is separate from syncing a user's own data, so guests get catalog updates too.

## Installing on family phones

- **Android:** every CI run on a pull request uploads a debug APK as the `coach-poulpy-debug-apk` artifact. Download it on the phone and allow installing from that source. `./gradlew :app-android:assembleRelease` gives a smaller build, signed with the debug key until a release key exists.
- **iPhone:** set your Apple team in Xcode (Signing & Capabilities) and run on a connected iPhone. With a free Apple account the app expires after 7 days; TestFlight needs the paid Apple Developer Program.

## Continuous integration

`.github/workflows/ci.yml` runs the shared tests on the JVM and in JavaScript, the server tests, builds the Android APK, builds the iOS app for the simulator on macOS, and builds the website.
