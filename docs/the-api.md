# The API

The server is an API only, at `https://api.coach-poulpy.com/api/v1` ([ADR 0021](./adr/0021-the-landing-site-the-app-and-the-api-have-their-own-hostname.md)). Its routes are described in OpenAPI at `/api/v1/openapi.json`, generated from the code, so that file, not this page, is the full reference. This page says how a request is handled and which routes exist so far.

## How a request is handled

Every request under `/api/v1` passes one filter that finds its **caller** ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)):

```mermaid
flowchart LR
    request(["Request"]) --> header{"Authorization<br/>header?"}
    header -- "none" --> anonymous["Anonymous caller"]
    header -- "Bearer cp_…" --> authenticator["Authenticator<br/>for that prefix"]
    authenticator -- "accepts" --> signed["Signed caller<br/>account · client · scopes"]
    authenticator -- "refuses, or no such prefix" --> denied(["401"])
    header -- "anything else" --> denied
    anonymous --> route["Route: public,<br/>or checks its scopes"]
    signed --> route
```

- **A bad credential is never treated as no credential.** An unknown prefix, an expired token or a `Basic` header is a 401, even on a public route.
- **Each kind of token has its own authenticator,** picked by its prefix: `cp_at_` for access tokens, `cp_pat_` for personal access tokens. None is registered until accounts arrive ([#25](https://github.com/tkint/coach-poulpy/issues/25)), so for now every caller is anonymous and every token is refused.
- **Every route says who may call it.** In the code, each route sets its `security` to `Public` or to the scopes it needs; a server test fails when a route sets neither. Scoped routes arrive with accounts.

## Routes

| Route | Who | What it does |
| --- | --- | --- |
| `GET /health` | Anyone | `ok` and the version, or 503 while the database doesn't answer; the Docker health check. Outside `/api/v1`, so it never changes with the API's version. |
| `GET /api/v1/openapi.json` | Anyone | The OpenAPI description of every route below. |
| `GET /api/v1/catalog` | Anyone | The exercise catalog the server was built with, the same JSON the app bundles. With `?newerThan=<version>` it answers 204 when the phone already has that version or a newer one, so phones download it only when it changed ([#27](https://github.com/tkint/coach-poulpy/issues/27)). |

## Trying it locally

```sh
docker compose -f tools/compose.yaml up -d   # the local Postgres
./gradlew :server:run
curl http://localhost:8080/api/v1/openapi.json
curl -i "http://localhost:8080/api/v1/catalog?newerThan=1"
```
