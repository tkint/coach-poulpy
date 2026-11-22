# Coach Poulpy

A personal training platform: build workouts, follow plans, run guided sessions and track progress. The Android and iPhone apps keep everything on the phone, as a guest or with an account that syncs between phones through the server. The website shows an account's workouts; sharing and coaching come later, over the same Kotlin domain.

The brand, design system and architecture are published at [docs.coach-poulpy.com](https://docs.coach-poulpy.com/); the site is built from `docs/` by the VitePress project in `site/`. How work moves from an idea to a merged PR is in [CONTRIBUTING.md](CONTRIBUTING.md).

## Layout

| Path | What it is |
| --- | --- |
| `core/model` | Domain types: exercises, workouts, steps, prescriptions, plans, session records |
| `core/engine` | Timeline compiler, session state machine, event log |
| `core/stats` | Derived metrics: session totals and the statistics built from them |
| `types/treadmill` | Treadmill recording, summaries and step estimate |
| `catalog` | The exercise catalog's shape and rules |
| `catalog-sample` | A frozen set of exercises for tests only |
| `data/local` | SQLDelight database on the phone |
| `data/remote` | The phone's calls to the account API (Ktor), with token refresh |
| `device/ftms` | Bluetooth treadmills (FTMS): decoding, connection with reconnect, Bluetooth through Kable |
| `export/fit` | .FIT export for Garmin Connect and Strava: a session written as an activity file |
| `shared` | Umbrella module the apps use; exported to iOS as the `Shared` framework |
| `app-android` | Jetpack Compose app |
| `app-ios` | SwiftUI app, project generated with XcodeGen |
| `server` | http4k API under `/api/v1` ([The API](docs/the-api.md)) and `/health`; image built from `server/Dockerfile` |
| `deploy` | Docker Compose files to run the server, Postgres and Caddy on one machine; see [Deploying the server](docs/deploying-the-server.md) |
| `web` | The websites, an npm workspace: `web/ui`, the shared Vue and DaisyUI design system ([ADR 0023](docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md)), `web/storybook`, the component workshop ([ADR 0028](docs/adr/0028-storybook-is-the-component-workshop-of-the-design-system.md)), and `web/app`, the web app |
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
./gradlew jvmTest jsTest :server:test :tools:i18n:test

# Android: build, or install on a phone connected over USB
./gradlew :app-android:assembleDebug
./gradlew :app-android:installDebug

# iOS: generate the Xcode project, then open it and press Run
cd app-ios && xcodegen && open CoachPoulpy.xcodeproj

# Server on http://localhost:8080, with its local Postgres. With no SMTP_HOST it sends no email:
# confirm a local account in Postgres (docs/the-api.md, "Trying it locally")
docker compose -f tools/compose.yaml up -d
./gradlew :server:run

# Website on http://localhost:5173 (talks to the server through /api)
cd web && npm install && npm run dev
```

The iOS build runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` itself, so the Kotlin code and the string tables are rebuilt whenever you build in Xcode; `xcodegen` makes them once first (`./gradlew :shared:generateAppleStrings`), since it lists them in the project.

## Read more

- [The catalog's shape](docs/the-catalog.md): the rules of an exercise. The catalog lives in the server database and phones download it; nothing ships bundled with the apps.
- [Releasing a beta](docs/releasing.md): installing a build on a phone, the Android signing key and the iPhone build.

## Continuous integration

`.github/workflows/ci.yml` runs the shared tests on the JVM and in JavaScript, the server tests, builds the Android APK, builds the iOS app for the simulator on macOS, and builds the website.
