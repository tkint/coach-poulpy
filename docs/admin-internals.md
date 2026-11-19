# Admin internals

What to know before changing admin code: the decisions and the traps. The routes, filters, status codes and fields are in the OpenAPI document, generated from the code, so they aren't repeated here. The rules these enforce are in [the admin area](./admin.md).

## Access

- Admin is a `role` on the account, not a scope ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)). The server accepts the access cookie only from the website client ([the API](./the-api.md#signing-in)).
- A write whose `Origin` isn't the website's gets 403. This is the CSRF defence.
- A member who signs in on the website gets the same answer as any refusal, so the role never leaks.
- `ADMIN_EMAIL` is read at start-up, and the promotion is an `AdminAction`.

## Accounts

- `AdminAccounts.list` reads every account row and cuts the page in memory. That's enough for a back office. Move it into SQL if accounts reach the hundreds of thousands.
- Confirming an account's email by hand (from Accounts) has the same effect as the link: the account can sign in, its pending links stop working and a phone waiting for the link hears it.
- Disabling revokes every access and refresh token. Deleting runs the same code as the user's own deletion.

## Settings and mail

- Settings are one row each in the `settings` table (`ServerSettings`). Only non-secret ones go there, so the SMTP details stay in the environment.
- Every email goes through `SettingsMailer`, which reads `mail-from` when it sends. That's why a saved address applies with no restart.
- Mail is configured when `SMTP_HOST` is set. Without it, sending fails with `No mail server is set: SMTP_HOST is unset`, and the error isn't printed to the log.
- `SettingsMailer.last` keeps the last send in memory, so a restart forgets it.
- The public `GET /api/v1/status` must never say how the server is configured. A test holds it to that.

## Catalog

- Revisions live in `catalog_exercise_revision`, and publishing writes rows only for the exercises it names ([ADR 0030](./adr/0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md)).
- Content, translation and lifecycle are separate dimensions in the list's filters. Don't fold one into another.

## Audit log

- An admin action is an `AdminAction` event sent through http4k's `Events`. One sink stores it in `admin_audit` and another prints it to the server log. Tests check it with `RecordingEvents`. Every admin write must emit one.
- `EmailAddress` is a values4k type that prints masked, so an address that reaches a log by accident stays hidden. Read the real one with `.value` only where needed. An event never carries a password or an unmasked address.
