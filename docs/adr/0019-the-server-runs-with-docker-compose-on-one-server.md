---
status: accepted
date: 2026-11-05
---

# The server runs with Docker Compose on one server

The server, its Postgres and the Caddy that serves them over HTTPS run as Docker containers on one Linux server with Docker. Another app already runs on that machine, so Coach Poulpy adds no cost. The setup is in [Deploying the server](../deploying-the-server.md), and the choice was made in [#28](https://github.com/tkint/coach-poulpy/issues/28).

## Why

- **Cheap enough to start before anyone pays.** The family and early users need far less than one small machine.
- **The GDPR is met.** The server is hosted in the EU or a country with an EU adequacy decision, as health data requires.
- **Docker keeps it portable.** The same Compose files run on the dev computer, and moving to another host means copying a folder and a database dump. A managed platform would tie the server to its own build and database offers.
- **One Caddy for the whole machine.** Caddy owns ports 80 and 443 and gets every site's certificate. Each app joins a shared `edge` network and brings one site file, so apps can be added or removed without touching each other.

## How it is released and deployed

- **The Release workflow publishes everything together.** The same tag push that publishes the APK pushes the server image to the GitHub Container Registry, tagged with the version (`0.3.0`, `0.3`, `latest`). It also attaches `coach-poulpy-deploy.tar.gz` (the Compose files and the Caddy files) to the release.
- **Deploying is a separate, manual step.** Actions > Deploy SSHes into the server and pulls the chosen image, so a release never reaches the server on its own.
- **The image is built for amd64 only,** like the server. Building for arm64 under emulation would multiply the release time for a machine we don't have.

## Consequences

- We keep the machine patched, the firewall closed apart from SSH, HTTP and HTTPS, and the backups running: no provider does it for us.
- Memory is the limit. The server is capped at 512 MB beside Postgres and the other app, and the image is never built on the server, only in CI. A bigger machine is the first step if it runs short, and a managed database the next.
- One machine means no failover. A crash or maintenance takes the server down, but phones keep working offline and sync when it returns ([ADR 0001](./0001-local-first-phone-is-the-source-of-truth.md)), so that is acceptable for now.
