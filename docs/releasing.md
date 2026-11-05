# Releasing a beta

Coach Poulpy is in beta. **Pushing a version tag is the release:** the tag is the version, CI runs in full, then a GitHub pre-release with the Android APK and the server image on the GitHub Container Registry are published. No version pull request, no button, no agent. Deploying the server is a separate button.

## The version

The version people see (Settings, "Version 0.6.0") is the tag without its `v`. The Release workflow passes it to the builds as `POULPY_VERSION_NAME`: Android uses it as `versionName` and iOS as `MARKETING_VERSION`. Every other build, on a computer or in CI, says "Version dev". A tag is three numbers after a `v`, like `v0.6.0`; while in beta it stays `v0.x.y`, and a release is a pre-release until the first `v1.0.0`.

The Android `versionCode` is the minute the build ran (minutes since 2026-01-01), as in CI, so a release installs over any earlier build.

## Cut a release

1. Make sure `main` is where you want it and CI is green on it.
2. Tag it and push the tag:

   ```sh
   git switch main && git pull
   git tag v0.6.0
   git push origin v0.6.0
   ```

3. **Actions > Release** starts on its own. It runs the whole of CI (shared tests, server, Android APK, iOS build, website), then publishes.
4. Once it is done, the **Releases** page has `v0.6.0` with `coach-poulpy-v0.6.0.apk`, `coach-poulpy-deploy.tar.gz` and notes listing the merged pull requests since the last one.
5. On a phone, download the APK from that page and install it over the old one; the phone's data is kept.

Publishing a release from GitHub's **Releases** page with a new tag also works, as it pushes the tag. But that release is visible straight away, before CI has passed, and stays without an APK if CI fails. The workflow then adds the APK and the deploy bundle to it, sets its title, and writes the same notes if you left them empty. Pushing the tag is the better way.

The workflow stops with a message, and publishes nothing, if the tag is not `v` and three numbers, if the signing key secrets are missing (see the README, "The signing key"), or if any CI check fails. To try again, delete the tag (`git push origin :refs/tags/v0.6.0` and `git tag -d v0.6.0`), fix `main`, and tag again.

## The server

After CI, the same workflow builds the server image (amd64 only, like the server: [ADR 0019](./adr/0019-the-server-runs-with-docker-compose-on-one-server.md)) with the version baked in (the server's `/health` answers `ok 0.2.0`; any other build says `ok dev`), and pushes it as `ghcr.io/tkint/coach-poulpy-server` with three tags: `0.2.0`, `0.2` and `latest`. The pre-release is created only once the image is pushed. `coach-poulpy-deploy.tar.gz` holds the Compose and Caddy files of that version and the deploying page as `DEPLOYING.md`, unpacked on the machine as in [Deploying the server](./deploying-the-server.md).

The first push creates the package as private. Either make it public once (the package's **Package settings > Change visibility**), or log the machine in with a token that can read packages (`docker login ghcr.io`).

## Deploy the server

A release never reaches the machine on its own. Open **Actions > Deploy > Run workflow**, enter `latest` or a version such as `0.2.0`, and run it. It uploads deployment files from the selected workflow ref, deploys that server image and reconciles the shared Caddy. The machine needs Python 3 and Docker Compose 2.24.4 or newer, and `~/coach-poulpy/.env` must already contain `DOMAIN` and `POSTGRES_PASSWORD`.

Set it up once:

1. On the machine, create a user in the `docker` group that owns `~/coach-poulpy`, and give it a key used only for deploys:

   ```sh
   ssh-keygen -t ed25519 -N '' -f coach-poulpy-deploy -C coach-poulpy-deploy
   # Add coach-poulpy-deploy.pub to that user's ~/.ssh/authorized_keys on the machine.
   ```

2. On GitHub, **Settings > Environments**, create `production`. Add the secret `DEPLOY_SSH_KEY` (the private key file) and these variables (or secrets):

   | Name | Value |
   | --- | --- |
   | `DEPLOY_HOST` | The machine's address |
   | `DEPLOY_USER` | The user from step 1 |
   | `DEPLOY_HOST_KEY` | The output of `ssh-keyscan -t ed25519 HOST`, so the workflow refuses any other machine |
   | `DEPLOY_PORT` | Optional, `22` by default |
   | `DEPLOY_DIR` | Optional, `~/coach-poulpy` by default |
   | `EDGE_DIR` | Optional, `~/edge` by default; use the same directory for all shared-edge deployments |

3. In the same `production` environment, tick **Required reviewers** and add yourself, so a deploy waits for your approval before it touches the machine.

The workflow stops with a message, and changes nothing, if a setting is missing or the version has characters other than letters, digits, `.`, `_` and `-`. Until there is a server, a run failing on missing settings is expected. Once it succeeds, `curl https://api.example.com/health` prints the version deployed. Deploying an older version is the way back if a release goes wrong.

The deployer creates or starts Caddy when needed, updates only this app's site, preserves existing sites and automatically migrates a standard app-owned Docker Caddy with its certificate volumes. It also works when the app has not been started yet. Shared configuration updates are serialized on the server. Details and migration limits: [Deploying the server](./deploying-the-server.md).

## What a release does not do yet

- **iPhone:** no file is attached. A phone build needs an Apple Developer account and signing; until then iOS is built from Xcode (README).
- **Debug build type:** the APK is the same `assembleDebug` build CI makes, the one that has been run on phones. A smaller, minified `assembleRelease` build is a later step once it has been tried on a phone.
- **Not a store release:** while the major is 0, the pre-release flag keeps it off the "latest" release, and nothing goes to Google Play or TestFlight.
