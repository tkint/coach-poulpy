---
status: accepted
date: 2026-11-06
---

# Every credential resolves to one caller with scopes

The API will be called by the phone apps first, then the website, a person's own scripts and the MCP server, and later third-party apps ([#228](https://github.com/tkint/coach-poulpy/issues/228)). So authentication and authorization are kept apart: each way of signing in only answers "who is calling, with which rights", and the routes only ask "does this caller hold the scope, and is it their data". Adding a mechanism, or moving token issuing to an identity provider, is one new authenticator; no route changes.

## How a request is checked

1. **Authenticate.** One http4k filter reads the credential (an `Authorization: Bearer` token, or the website's cookie), picks the authenticator for that kind of token by its prefix, and stores a **caller** on the request: the account, the client holding the credential (the app, the website, a personal access token, a third-party app) and its granted scopes. No credential gives an anonymous caller, which only public routes accept; a bad or expired one is a 401.
2. **Check the scope.** Each route declares the scopes it needs, next to it in the http4k contract, so the OpenAPI description shows them. A missing scope is a 403. Scopes are `resource:action`: `workouts:read`, `workouts:write`, `plans:read`, `plans:write`, `sessions:read`, `sessions:write`, `profile:read`, `profile:write`, and `sync` for the phones' push and pull.
3. **Check ownership.** Every repository call takes the caller's account id, so a query only sees that account's rows. Coach access is checked against an active coach link on each request.

Some actions are never grantable to a token or an outside app: changing the email or password, deleting the account, creating tokens, granting a coach. They need a first-party sign-in. Admin ([#26](https://github.com/tkint/coach-poulpy/issues/26)) is a role on the account, not a scope anyone can ask for. `/health` and the catalog ([#27](https://github.com/tkint/coach-poulpy/issues/27)) are public.

## Tokens are opaque, and `/api/v1/me` says who you are

The server issues its own tokens for now. They are random 256-bit values with a readable prefix (`cp_at_` access, `cp_rt_` refresh, `cp_pat_` personal access token) so secret scanners spot a leak; Postgres keeps only their SHA-256. A client that wants to know who it is calls `GET /api/v1/me`, which returns the account id, email, role, the client and the scopes of the token used.

| Who | Signs in with | Holds | Scopes | When |
| --- | --- | --- | --- | --- |
| Phone app | Email and password (Argon2id), Sign in with Apple or Google (ID token verified by the server) | 15-minute access token and a rotating refresh token, in Keychain or Android Keystore | All user scopes and `sync` | Phase 3 ([#25](https://github.com/tkint/coach-poulpy/issues/25)) |
| Website | The same sign-in | The same tokens, in an HttpOnly, Secure, SameSite cookie | All user scopes | Phase 4 |
| A person's scripts, the MCP server | A personal access token made in Settings: named, scoped, with an expiry, shown once | `Authorization: Bearer cp_pat_…` | Those picked when making it | With #228 |
| Third-party apps | OAuth 2.1 authorization code with PKCE, approved on a consent page | Access and refresh tokens tied to that app | Those approved | Later, with #228 |

- **Refresh tokens rotate.** Each use returns a new one, and reusing an old one revokes its whole family.
- **Everything is revocable and visible.** Settings lists devices, personal tokens and connected apps with their last use.
- **Health data read by tokens is logged.** Personal tokens and third-party apps leave an audit row per request on sessions and profile data.
- **The website's cookie is guarded against CSRF** by SameSite and an `Origin` check on every write, and CORS allows only Coach Poulpy's own sites.
- **The MCP server needs nothing new.** MCP authorizes with OAuth 2.1 and PKCE plus a protected-resource document under `/.well-known/`, which this design serves.

## Considered options

- **Signed JWTs as access tokens.** They carry the caller's details and need no lookup, but anyone can read them, and they cannot be revoked before they expire. With one server a database lookup per request is cheap and revocation is instant, so `/me` gives the details instead. If an identity provider takes over issuing, its JWTs get their own authenticator.
- **An identity provider now (Keycloak, Zitadel, Auth0).** Keycloak needs more memory than the machine has left beside the server ([ADR 0019](./0019-the-server-runs-with-docker-compose-on-one-server.md)), and a hosted one costs money and adds a processor of health-adjacent data. The caller model keeps that move open.

## Consequences

- The server owns password hashing, token storage, rotation and revocation, and later the OAuth endpoints (`http4k-security-oauth` provides the pieces).
- Every authenticated request costs one indexed lookup in the token table.
- Every route declares its scopes or is marked public, and a test checks that none is left out.
