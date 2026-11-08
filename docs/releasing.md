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

After CI, the same workflow builds the server image (amd64 only, like the server: [ADR 0019](./adr/0019-the-server-runs-with-docker-compose-on-one-server.md)) with the version baked in (the server's `/health` answers `ok 0.2.0`; any other build says `ok dev`), and pushes it as `ghcr.io/tkint/coach-poulpy-server` with three tags: `0.2.0`, `0.2` and `latest`. The web app's image, `ghcr.io/tkint/coach-poulpy-web`, gets the same tags, with `assetlinks.json` built from the signing key. The pre-release is created only once both images are pushed. `coach-poulpy-deploy.tar.gz` holds the Compose and Caddy files of that version and the deploying page as `DEPLOYING.md`, unpacked on the machine as in [Deploying the server](./deploying-the-server.md).

The first push creates the package as private. Either make it public once (the package's **Package settings > Change visibility**), or log the machine in with a token that can read packages (`docker login ghcr.io`).

## Deploy the server

A release never reaches the machine on its own. Open **Actions > Deploy > Run workflow**, pick what to deploy (`server`, or `website` for the web app), the `production` environment, enter `latest` or a version such as `0.2.0`, and run it. The server and the website are separate runs, each replacing only its own container and Caddy route. It writes the app's `.env` from that GitHub environment, uploads it with the deploy files from the selected workflow ref and deploys that server image behind the shared Caddy. The machine needs Python 3 and Docker Compose.

Set it up once:

1. On the machine, create a user in the `docker` group, and give it a key used only for deploys:

   ```sh
   ssh-keygen -t ed25519 -N '' -f coach-poulpy-deploy -C coach-poulpy-deploy
   # Add coach-poulpy-deploy.pub to that user's ~/.ssh/authorized_keys on the machine.
   ```

2. On GitHub, **Settings > Environments**, create `production` and add:

   | Name | Kind | Value |
   | --- | --- | --- |
   | `DEPLOY_SSH_KEY` | secret | The private key file |
   | `DEPLOY_HOST` | variable | The machine's address |
   | `DEPLOY_USER` | variable | The user from step 1 |
   | `DEPLOY_HOST_KEY` | variable | The output of `ssh-keyscan -t ed25519 HOST`, so the workflow refuses any other machine |
   | `DEPLOY_PORT` | variable | Optional, `22` by default |
   | `DOMAIN` | variable | The API hostname, without scheme, port or path |
   | `POSTGRES_PASSWORD` | secret | For example `openssl rand -hex 24`; used when the Postgres volume is created |
   | `SMTP_HOST` | variable | The EU mail provider's SMTP server. Optional until one is chosen: without it the server prints emails to its log instead of sending them |
   | `SMTP_PORT` | variable | Optional, `587` (STARTTLS) by default; `465` for implicit TLS |
   | `SMTP_USER` | variable | The SMTP login |
   | `SMTP_PASSWORD` | secret | The SMTP password |
   | `MAIL_FROM` | variable | The sender, for example `Coach Poulpy <hello@coach-poulpy.com>`; needed with `SMTP_HOST` |
   | `WEBSITE_URL` | variable | Optional, `https://app.coach-poulpy.com` by default: where the links in emails point |
   | `LOKI_URL` | variable | Optional: the push URL of a Grafana Cloud Loki, `https://logs-prod-XXX.grafana.net/loki/api/v1/push`. With it, the deploy starts a container that ships the logs there ([Logs](./deploying-the-server.md#logs)) |
   | `LOKI_USER` | variable | The Loki user ID, a number; needed with `LOKI_URL` |
   | `LOKI_PASSWORD` | secret | A Grafana Cloud access policy token that can only write logs; needed with `LOKI_URL` |
   | `WEB_DOMAIN` | variable | The web app's hostname, such as `app.coach-poulpy.com`; needed only to deploy `website` |

   The mail settings (`SMTP_*`, `MAIL_FROM`) may be variables or secrets, in the environment or in the repository's **Settings > Secrets and variables > Actions**; a value in the environment wins.

3. In the same `production` environment, tick **Required reviewers** and add yourself, so a deploy waits for your approval before it touches the machine.

The workflow stops with a message, and changes nothing, if a setting is missing or the version has characters other than letters, digits, `.`, `_` and `-`. Until there is a server, a run failing on missing settings is expected. Once it succeeds, `curl https://api.coach-poulpy.com/health` prints the version deployed. Deploying an older version is the way back if a release goes wrong.

The deployer creates or starts Caddy when needed and adds or replaces only this app's site through Caddy's admin API, leaving other sites alone. Caddy autosaves API updates and resumes them after a restart. Details: [Deploying the server](./deploying-the-server.md).

## Deploy a pull request

To try a pull request on the machine before it merges, open **Actions > Deploy > Run workflow**, keep `main` under **Use workflow from**, pick the environment and enter the pull request's number as the version, such as `275`. The workflow builds the pull request's latest commit, pushes it as `ghcr.io/tkint/coach-poulpy-server:pr-<number>` (or `coach-poulpy-web:pr-<number>` for `website`), and deploys it with the pull request's own Compose and Caddy files, since it may change them. `/health` then answers `ok pr-<number>`. Only open pull requests from branches of this repository are deployed, never forks. Nothing runs until you start it.

A branch with no pull request works too: pick it under **Use workflow from** and enter the version `branch`. Its image is `branch-<branch name>` (slashes become dashes).

Set up the `preview` environment once, like `production` above (the same `DEPLOY_*` settings). Its `APP_NAME` decides where the branch lands:

| `APP_NAME` in `preview` | `DOMAIN` in `preview` | Result |
| --- | --- | --- |
| Another name, such as `coach-poulpy-preview` | Its own hostname, such as `preview.api.coach-poulpy.com` | A second server and Postgres in `~/coach-poulpy-preview`, beside production, with its own data |
| Unset, or `coach-poulpy` | Production's | Replaces production until the next production deploy |

`APP_NAME` has lowercase letters, digits and dashes; production leaves it unset (`coach-poulpy`). It names the folder on the machine, the Compose project, the server's alias on the `edge` network and its Caddy route. Nothing is cleaned up on its own: deploy another branch over it, or stop it on the machine with `cd ~/coach-poulpy-preview && docker compose down`. A separate preview needs about as much memory as production (512 MB for the server, plus its Postgres), so check the machine has room.

Deploying runs the pull request's or branch's deploy files on the machine and replaces the shared `~/caddy/deploy.py` with its copy, so only deploy code you have read.

## What a release does not do yet

- **iPhone:** no file is attached. A phone build needs an Apple Developer account and signing; until then iOS is built from Xcode (README).
- **Debug build type:** the APK is the same `assembleDebug` build CI makes, the one that has been run on phones. A smaller, minified `assembleRelease` build is a later step once it has been tried on a phone.
- **Not a store release:** while the major is 0, the pre-release flag keeps it off the "latest" release, and nothing goes to Google Play or TestFlight.
