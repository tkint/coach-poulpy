# The admin area

The admin area is where the maintainer looks after the catalog, the accounts and the server's settings. It is a section of the website under `/admin`, backed by routes under `/api/v1/admin/`. The work is tracked in [#26](https://github.com/tkint/coach-poulpy/issues/26).

## Who can open it

- **Admin is a role on the account** (`member` or `admin`), not a scope anyone can ask for ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)).
- **Only the website's sign-in reaches it.** An admin route answers 401 to an anonymous caller and 403 to anyone who is not an admin signed in through the website: a member, an admin on the phone app, a personal access token or a third-party app.
- **The website signs in with cookies** (`POST /api/v1/web/session`, see [the API](./the-api.md)). The server reads the access cookie only for the website client, and a write whose `Origin` is not the website's gets 403, which is the CSRF defence of [ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md). The API answers the website's origin with `Access-Control-Allow-Credentials`, so the page calls it with `credentials: 'include'`.
- **One admin for now.** There are no finer rights.

## The screens

The admin lives at `/admin` on the website ([mockups](./../design/mockups/agreed/web/admin-shell.html)). With no admin session it shows the sign-in; the page asks `GET /api/v1/me`, and a member who signs in is signed out again at once with the same "wrong email or password" message, so the screen never says who is an admin. Once signed in it shows the `CpDrawer` shell and the audit log. The sidebar lists only the screens that exist; each admin feature adds its own entry.

## Making the first admin

No endpoint makes an admin. Set `ADMIN_EMAIL` in the `production` environment ([Releasing](./releasing.md#deploy-the-server)) and deploy: when the server starts, the confirmed account with that address becomes an admin. An address with no confirmed account does nothing, so signing up first with someone else's address cannot take the role. The promotion is written to the audit log.

## The audit log

An admin action is an `AdminAction` event (who, what, the target), sent through http4k's `Events` like any other event. One sink stores it in the `admin_audit` table (who is empty when the server itself did it, as at start-up; rows stay when the admin's own account is deleted), and another prints it to the server log, which the log shipping picks up. `GET /api/v1/admin/audit` returns the last 100 rows, and the first screen of the admin shows them. Each admin feature emits an `AdminAction` for its writes; tests check it with `RecordingEvents`.

Sensitive values are values4k types that print masked: `EmailAddress` prints as `a***@example.com`, so an address that reaches a log or an event by accident stays hidden, and the real one is read with `.value` only where it is needed. An event never carries a password or an unmasked address.

## What comes next

Accounts ([#336](https://github.com/tkint/coach-poulpy/issues/336)), the mail from-address ([#339](https://github.com/tkint/coach-poulpy/issues/339)), the status view ([#340](https://github.com/tkint/coach-poulpy/issues/340)), and the catalog ([#337](https://github.com/tkint/coach-poulpy/issues/337), [#338](https://github.com/tkint/coach-poulpy/issues/338)).
