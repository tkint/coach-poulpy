# Copilot cloud agent

`AGENTS.md` holds the project's rules; read it first. This file only covers the sandbox the cloud agent works in.

## What is ready

`.github/workflows/copilot-setup-steps.yml` runs before you start and prepares:

- JDK 21 and Gradle, with every plugin and dependency for the shared modules, the server, `:tools:i18n` and `:app-android`, plus the Node.js and Yarn that `jsTest` uses.
- Node 22, with `npm ci` done in `web/` and `site/`.
- `markdownlint-cli2` and `codegraph`, with the code graph already indexed. There is no MCP server: use the `codegraph` CLI (`docs/agents/code-graph.md`).

If one of those steps failed, its log is at the top of the session; treat what it prepares as unavailable.

## No downloads

A firewall blocks most of the internet, including nodejs.org, the Gradle plugin portal and Docker Hub. Anything not prepared above can't be fetched, so:

- Never install or upgrade a tool or package: no `apt`, `brew`, `sdkmanager`, `pip`, `npm install`, `npx` of an uncached package, or a different JDK or Gradle.
- Run Gradle with `--offline`, so a missing dependency fails at once instead of timing out.
- If a command fails because something can't be downloaded, don't retry it or look for a workaround. Note it under "How it was checked" in the PR and leave it to CI.
- A change that adds a dependency (a new library in `gradle/libs.versions.toml`, a new npm package) can't be checked here. Make the change, say in the PR that only CI can build it, and stop there.

## What you can run

```sh
./gradlew --offline jvmTest jsTest :server:test :tools:i18n:test   # shared logic, server and the string generator
./gradlew --offline :app-android:compileDebugKotlin                # the Android app compiles
node design/build-tokens.mjs --check
node design/build-poulpy.mjs --check
node design/build-poulpy-3d.mjs --check
node design/build-body-map.mjs --check
node design/build-body-map-shapes.mjs --check
node design/check-mockups.mjs
(cd web && npm run build)
(cd site && npm run build && node check-published.mjs)
markdownlint-cli2
```

## What only CI or a phone can check

Don't attempt these; tick them off as left to CI in the PR:

- The iOS app and anything in `app-ios/`: it needs macOS and Xcode.
- The full Android APK (`:app-android:assembleDebug`) and its signing.
- The server Docker image and the files in `deploy/`.
- The link check of the Markdown (lychee): it needs the internet.
- Bluetooth, sound, haptics, background running and installs: label the issue `device-test`, as `AGENTS.md` says.
