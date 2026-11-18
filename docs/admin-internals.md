# Admin internals

How the server enforces the rules of [the admin area](./admin.md). Read this when you change admin code. Every admin route is under `/api/v1/admin/` and described in the OpenAPI document, which is the full reference.

## Access

- Admin is a `role` on the account (`member` or `admin`), not a scope ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)).
- The server accepts the access cookie only from the website client ([the API](./the-api.md#browser-session-admin-website)). The API answers the website's origin with `Access-Control-Allow-Credentials`, so the page calls it with `credentials: 'include'`.
- An anonymous caller gets 401. A member, an admin on the phone app, a personal access token or a third-party app gets 403.
- A write whose `Origin` is not the website's gets 403. This is the CSRF defence.
- A member who signs in on the website is signed out with the same answer as any refusal, so the role isn't disclosed.
- `ADMIN_EMAIL` is read at start-up. The promotion is an `AdminAction`.

## Accounts

- `AdminAccounts.list` reads every account row and cuts the page in memory. Enough for a back office; move it into SQL if accounts reach the hundreds of thousands.
- The list takes `q`, `role`, `confirmed`, `signIn`, `sort`, `order`, `page` and `size`. A word that isn't allowed is `400 bad-query`.
- Disabling revokes every access and refresh token of the account.
- Deleting runs the same code as the user's own deletion and needs `{"confirmEmail": …}`.
- An admin acting on their own account gets `409 own-account`.

## Settings and mail

- Settings are one row each in the `settings` table (`ServerSettings`). The SMTP host, port, user and password stay in the environment.
- Every email goes through `SettingsMailer`, which reads `mail-from` when it sends, so a saved address applies with no restart. `MAIL_FROM` is the default.
- An address that doesn't parse is `400 invalid-address`.
- `SettingsMailer.last` keeps the last send (when, kind, masked recipient, error) in memory.
- Mail is configured when `SMTP_HOST` is set. Without it, sending fails with `No mail server is set: SMTP_HOST is unset`, and the error isn't printed to the log.
- Routes: `GET /api/v1/admin/settings`, `POST …/settings/mail-from`, `POST …/settings/test-email`.

## Status and pending registrations

- Routes: `GET /api/v1/admin/status`, `GET …/pending-registrations` and `POST …/pending-registrations/{id}/confirm` (`204`, `404 unknown-account`, `409 already-confirmed`).
- Confirming by hand has the same effect as the link: the account can sign in, its pending links stop working and a phone waiting for the link hears it.
- The public `GET /api/v1/status` never says how the server is configured, and a test holds it to that.

## Catalog

- Storage is `catalog_exercise_revision`. Publishing writes rows only for the exercises it names ([ADR 0030](./adr/0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md)).
- An exercise is the catalog's JSON, every text a map from language to string.
- Refusals: `409 slug-published`, `409 slug-taken`, `409 missing-translations` (listing exercises and languages), `409 no-changes`, `400 invalid-exercise` with a `reason`, `404 unknown-exercise`, `400 bad-query`.
- The list filters by `q`, `pattern`, `muscles`, `positions`, `equipment`, `translation`, `content` and `lifecycle`, and sorts by `name`, `pattern`, `languages`, `change` and `status`. Content, translation and lifecycle are separate dimensions.
- `GET …/catalog/changes` returns what publishing would do, and each edited field as `{field, before, after}`.
- `POST …/catalog/publish` takes an optional `{"slugs": […]}`.

## Audit log

- An admin action is an `AdminAction` event sent through http4k's `Events`. One sink stores it in `admin_audit` and another prints it to the server log. Tests check it with `RecordingEvents`.
- Actions: `admin.account.disable`, `admin.account.enable`, `admin.account.delete`, `admin.account.confirm`, `admin.settings.mail-from`, `admin.catalog.create`, `admin.catalog.edit`, `admin.catalog.retire`, `admin.catalog.offer` and `admin.catalog.publish`.
- `GET /api/v1/admin/audit` returns the last 100 rows.
- `EmailAddress` is a values4k type that prints masked, so an address that reaches a log by accident stays hidden. Read the real one with `.value` only where needed. An event never carries a password or an unmasked address.
