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
- **Each kind of token has its own authenticator,** picked by its prefix: `cp_at_` for access tokens, later `cp_pat_` for personal access tokens ([#228](https://github.com/tkint/coach-poulpy/issues/228)). Today only `cp_at_` is registered; a refresh token (`cp_rt_`) is never accepted as an access token.
- **Every route says who may call it.** In the code, each route sets its `security` to `Public` or to `requires(...)` with the scopes it needs; a server test fails when a route sets neither. With no token, `requires` answers 401; with a token missing a scope, 403 with `WWW-Authenticate: Bearer error="insufficient_scope"`. The phone app gets every scope.

## Routes

| Route | Who | What it does |
| --- | --- | --- |
| `GET /health` | Anyone | `ok` and the version, or 503 while the database doesn't answer; the Docker health check. Outside `/api/v1`, so it never changes with the API's version. |
| `GET /api/v1/openapi.json` | Anyone | The OpenAPI description of every route below, the OAuth ones included. |
| `GET /api/v1/catalog` | Anyone | The exercise catalog the server was built with, the same JSON the app bundles. With `?newerThan=<version>` it answers 204 when the phone already has that version or a newer one, so phones download it only when it changed ([#27](https://github.com/tkint/coach-poulpy/issues/27)). |
| `POST /api/v1/accounts` | Anyone | Signs up ([#25](https://github.com/tkint/coach-poulpy/issues/25)): `email`, `password` (8 to 256 characters), `language` such as `fr` for the account's emails, and `healthDataConsent`, which must be `true`. Creates an account whose email isn't confirmed and mails it a link valid 24 hours. Answers 202 whether or not the address already has an account, so addresses can't be probed; a 400 carries `{"error": "invalid-email"}`, `invalid-password` or `consent-required`. |
| `POST /api/v1/accounts/confirm` | Anyone | Takes `{"token": …}` from the link, which reads `<website>/confirm-email#<token>`, and confirms the email: 204, or 400 `invalid-token` for a token unknown, used or expired. A token works once. |
| `POST /api/v1/password-resets` | Anyone | A forgotten password: `{"email": …}`. Mails a link, `<website>/reset-password#<token>`, when the address belongs to a confirmed account, at most 3 an hour per account. Always 202, so addresses can't be probed. |
| `POST /api/v1/password-resets/confirm` | Anyone | Takes `{"token": …, "password": …}` and sets the new password: 204, or 400 `invalid-token` (unknown, used or older than 1 hour) or `invalid-password`. Every device of the account is signed out, and the account's other reset links stop working. |
| `POST /oauth/token` | Anyone | The OAuth 2.0 token endpoint ([RFC 6749](https://www.rfc-editor.org/rfc/rfc6749)), with form fields. `grant_type=password` with `username` (the email), `password` and `device_name`, such as `Ana's iPhone`, signs a device in. `grant_type=refresh_token` with `refresh_token` gives new tokens; each refresh token works once, and using one a second time signs that device out, since someone may hold a copy. Answers `access_token` (`cp_at_`, 15 minutes), `token_type`, `expires_in`, `refresh_token` (`cp_rt_`, expires after 90 days unused) and `scope`. A wrong email or password, an unconfirmed email, or a used or expired refresh token get 400 `invalid_grant`; after 5 failures for one email within 15 minutes the password grant answers 429 with `Retry-After`. `client_id` is optional and only `app` is known. |
| `POST /oauth/revoke` | Anyone | Token revocation ([RFC 7009](https://www.rfc-editor.org/rfc/rfc7009)): `token`, an access or a refresh token, signs its device out, both tokens included. Always 200, even for an unknown token. |
| `GET /api/v1/me` | Signed in | The `accountId`, `email`, `role`, and the `client` and `scopes` of the token sent ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)). |
| `DELETE /api/v1/me` | Signed in, the app | Deletes the account at once with every row of it, and signs its devices out. Takes `{"password": …}`, the account's own: 204, or 403 `wrong-password`, or `first-party-only` for a client other than Coach Poulpy's own apps. |
| `POST /api/v1/sync/push` | Signed in, `sync` | The phone's outbox, `{"changes": [...]}`, oldest first: at most 500 changes and 1 MB, stored whole or not at all, and the same changes sent again change nothing ([Accounts, Sync](./accounts.md#sync)). 200, or 400 `invalid-change` with the `index` of the change refused, or 413 `too-large`. Sessions only so far: `session`, `session-events` and `session-deleted`. |
| `GET /api/v1/sync/pull` | Signed in, `sync` | The changes stored after `cursor`, oldest first, at most `limit` (500 at most and by default): `{"changes": [...], "cursor": "57", "more": false}`. Keep calling with the new cursor while `more` is true; no cursor gives everything. A session's header always comes before its events. 400 `invalid-cursor` or `invalid-limit`. |

## Trying it locally

```sh
docker compose -f tools/compose.yaml up -d   # the local Postgres
./gradlew :server:run
curl http://localhost:8080/api/v1/openapi.json
curl -i "http://localhost:8080/api/v1/catalog?newerThan=1"
curl -i http://localhost:8080/api/v1/accounts -H 'Content-Type: application/json' \
  -d '{"email":"guest@example.com","password":"correct horse","language":"fr","healthDataConsent":true}'
```

Confirm the email with the token at the end of the printed link (`POST /api/v1/accounts/confirm`), then sign in:

```sh
curl -s http://localhost:8080/oauth/token -d grant_type=password \
  -d username=guest@example.com -d 'password=correct horse' -d device_name=curl
curl -i http://localhost:8080/api/v1/me -H 'Authorization: Bearer cp_at_…'
```

With no `SMTP_HOST` set, the server prints each email to its output instead of sending it, link included.
