---
status: accepted
date: 2026-11-06
---

# The landing site, the web app and the API each have their own hostname and container

The server stays an API only, and each part a browser or a developer reaches is its own deliverable in its own container, behind the shared Caddy ([ADR 0019](./0019-the-server-runs-with-docker-compose-on-one-server.md)):

| Hostname | Serves | Container |
| --- | --- | --- |
| `coach-poulpy.com` | The landing site: static pages for search engines and newcomers | A static file server |
| `app.coach-poulpy.com` | The signed-in website ([phase 4](../roadmap.md)): a Vue single-page app running the shared engine with Kotlin/JS | A static file server |
| `api.coach-poulpy.com` | The API under `/api/v1`, with its OpenAPI description, OAuth and `/.well-known/` routes | The server |

## Why

- **Different jobs, different rhythms.** The landing site changes with marketing copy and wants pre-rendered pages; the app changes with features and wants strict security headers and the sign-in cookie. Neither release restarts the other or the API.
- **A stable address for developers.** `api.coach-poulpy.com/api/v1` stays the same whatever the websites become.
- **The cookie stays first-party.** The three hostnames are one site, so the app's SameSite cookie is sent to the API; the API answers CORS only for `app.coach-poulpy.com` (and the landing site if it ever calls it).
- **Small cost.** A static file server needs about 15 MB of memory, against the server's 512 MB.

## Consequences

- The landing site and the app are separate builds in `web/`; the app's container arrives with phase 4, the landing site's when it is written.
- Caddy gets one route per hostname, each updated by its own deploy, as the API's is today.
- The API's CORS list and the cookie's settings follow these hostnames; a new website hostname means updating both.
