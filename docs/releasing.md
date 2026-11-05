# Releasing a beta

Coach Poulpy is in beta. A release is a GitHub pre-release with the Android APK attached, plus the server image on the GitHub Container Registry, made by one button and no agent. Deploying the server is a second button.

## The version

The version people see (Settings, "Version 0.1.0") is the one line in [`VERSION`](https://github.com/tkint/coach-poulpy/blob/main/VERSION) at the repository root. Android reads it for `versionName` and iOS gets it as `MARKETING_VERSION`, so there is nowhere else to change it. It is three numbers, like `0.2.0`; while in beta it stays `0.x.y`.

## Cut a release

1. Raise `VERSION` in a pull request (`chore/release-0.2.0`) and merge it once CI is green.
2. On GitHub, open **Actions > Release > Run workflow**, keep the branch `main`, and run it.
3. A few minutes later the **Releases** page has a pre-release `v0.2.0` with `coach-poulpy-v0.2.0.apk`, `coach-poulpy-deploy.tar.gz` and notes listing the merged pull requests since the last one.
4. On a phone, download the APK from that page and install it over the old one; the phone's data is kept.

The workflow stops with a message, and publishes nothing, if `VERSION` is not three numbers, if the tag for that version already exists (raise `VERSION` first), or if the signing key secrets are missing (see the README, "The signing key").

## The server

The same workflow builds the server image (amd64 only, like the server: [ADR 0019](./adr/0019-the-server-runs-with-docker-compose-on-one-server.md)) and pushes it as `ghcr.io/tkint/coach-poulpy-server` with three tags: `0.2.0`, `0.2` and `latest`. The pre-release is created only once the image is pushed. `coach-poulpy-deploy.tar.gz` holds the Compose and Caddy files of that version, unpacked on the machine as in [Deploying the server](./deploying-the-server.md).

The first push creates the package as private. Either make it public once (the package's **Package settings > Change visibility**), or log the machine in with a token that can read packages (`docker login ghcr.io`).

## Deploy the server

A release never reaches the machine on its own. Open **Actions > Deploy > Run workflow**, enter `latest` or a version such as `0.2.0`, and run it. It SSHes into the machine, pulls that image and restarts the server.

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

The workflow stops with a message, and changes nothing, if a setting is missing or the version has characters other than letters, digits, `.`, `_` and `-`. Deploying an older version is the way back if a release goes wrong.

## What a release does not do yet

- **iPhone:** no file is attached. A phone build needs an Apple Developer account and signing; until then iOS is built from Xcode (README).
- **Debug build type:** the APK is the same `assembleDebug` build CI makes, the one that has been run on phones. A smaller, minified `assembleRelease` build is a later step once it has been tried on a phone.
- **Not a store release:** the pre-release flag keeps it off the "latest" release, and nothing goes to Google Play or TestFlight.
