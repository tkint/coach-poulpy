# The admin area

The admin area is where the maintainer looks after the catalog, the accounts and the server's settings. It is a section of the website under `/admin`, backed by routes under `/api/v1/admin/`. The work is tracked in [#26](https://github.com/tkint/coach-poulpy/issues/26).

## Who can open it

- **Admin is a role on the account** (`member` or `admin`), not a scope anyone can ask for ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)).
- **Only the website's sign-in reaches it.** An admin route answers 401 to an anonymous caller and 403 to anyone who is not an admin signed in through the website: a member, an admin on the phone app, a personal access token or a third-party app.
- **The website signs in with cookies** (`POST /api/v1/web/session`, see [the API](./the-api.md)). The server reads the access cookie only for the website client, and a write whose `Origin` is not the website's gets 403, which is the CSRF defence of [ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md). The API answers the website's origin with `Access-Control-Allow-Credentials`, so the page calls it with `credentials: 'include'`.
- **One admin for now.** There are no finer rights.

## The screens

The admin lives at `/admin` ([agreed shell mockup](./../design/mockups/agreed/web/admin-shell.html)). A member who signs in is immediately signed out with the same "wrong email or password" message as any other refusal, so the sign-in flow does not disclose the account's role. The admin shell links to the audit log, accounts, settings and status; its **Coach Poulpy app** link returns to [the web app](./web-app.md).

## Accounts

The **Accounts** screen lists accounts with their email, role, confirmation and sign-in status, and creation date. It supports filtering, sorting, paging and opening an account by id, including when it is not on the current page. The view is encoded in the address (`/admin/accounts?q=…&role=…&sort=…&order=…&page=…&account=<id>`), so it can be bookmarked and browser history restores previous views ([navigation](./web-app.md#navigation-lives-in-the-url)). It never exposes workouts or health data. Each account action is written to the audit log with the account id as the target, never its address:

- **Disable** blocks sign-in and signs every device of the account out, access and refresh tokens included. It is reversible: **Enable** lets the account sign in again. A disabled account is refused at sign-in with the same "wrong email or password" answer as any other refusal.
- **Delete** removes the account and every row of it through the same code as the user's own deletion, once the admin has typed the account's email again.
- An admin can't disable or delete their own account (`409 own-account`).

Filtering, sorting and paging happen on the server (`AdminAccounts.list`, which reads every account row and cuts the page in memory, enough for a back office; move it into SQL if accounts reach the hundreds of thousands). The routes are `GET /api/v1/admin/accounts/{id}` for one account (a link opens an account that isn't on the page), `GET /api/v1/admin/accounts` with `q`, `role`, `confirmed` (`yes`/`no`), `signIn` (`active`/`disabled`), `sort` (`email`, `role`, `confirmed`, `signIn`, `created`), `order`, `page` and `size`, answering `{accounts, total}` (a word that isn't allowed is a `400 bad-query`), `POST /api/v1/admin/accounts/{id}/disable`, `POST /api/v1/admin/accounts/{id}/enable` and `DELETE /api/v1/admin/accounts/{id}` with `{"confirmEmail": …}`, all described in the OpenAPI document and all for an admin signed in through the website.

## Settings

The **Settings** screen at `/admin/settings` changes the email from-address without a deploy.

- **Only non-secret settings live in the database**, one row per setting in the `settings` table (`ServerSettings`). The SMTP host, port, user and password stay in the environment ([Deploying the server](./deploying-the-server.md)) and are not shown or editable here.
- **`MAIL_FROM` is the default until a from-address is saved.** A saved one applies to the next email, with no restart: every email goes through `SettingsMailer`, which reads the setting when it sends. An address alone (`hello@example.com`) or with a name (`Coach Poulpy <hello@example.com>`) is accepted; anything else is refused (`400 invalid-address`) and nothing is saved.
- **Saving writes `admin.settings.mail-from` to the audit log**, with the masked address (`h***@example.com`) as the target.
- **Send test email** sends a message to the signed-in admin's own address with the saved settings and reports whether the mail server accepted it or returned an error.
- **The last send is remembered.** `SettingsMailer.last` keeps when the last email (a test or any other) went, which kind it was, to whom (masked) and the mail server's error if any, for the [Status](#status) screen. It lives in memory, so a restart forgets it.

The routes are `GET /api/v1/admin/settings` (`{mailFrom, mailFromSaved}`), `POST /api/v1/admin/settings/mail-from` with `{"mailFrom": …}` and `POST /api/v1/admin/settings/test-email` (`{sent, error}`), for an admin signed in through the website. The screen keeps no state of its own beyond its path.

## Status

The **Status** screen at `/admin/status` reports server health and mail configuration ([#340](https://github.com/tkint/coach-poulpy/issues/340)). It is read-only: it cannot change settings or send email. Further checks can be added independently.

- **Mail** is configured when `SMTP_HOST` is set. Without it, email sending fails with `No mail server is set: SMTP_HOST is unset`; the error is not printed to the log. Otherwise the status reports the last email attempt since server start, its kind (confirmation, password reset or test), time and outcome. The SMTP host, user and password are never shown.
- **Pending registrations** lists the accounts whose email isn't confirmed yet, newest first (the last 100): the address, when it signed up and when its latest confirmation link expires. Signing up again sends a new link, so the date moves. No email is sent from here and there is no action on a row; on a laptop with no mail server, this is where a developer sees who signed up, and confirms them in Postgres ([Trying it locally](./the-api.md#trying-it-locally)).

The routes are `GET /api/v1/admin/status` (`{mail: {configured, lastSend: {at, kind, sent, error} | null}}`) and `GET /api/v1/admin/pending-registrations` (`{registrations: [{email, signedUpAt, linkExpiresAt}]}`), for an admin signed in through the website.

The public side is the web app's `/status` page, which anyone can open. It asks `GET /api/v1/status` and shows only whether Coach Poulpy is up or down, down too when the server can't be reached; it never says how the server is configured, and a test holds it to that.

## Catalog

The server holds the exercise catalog as a **catalog draft** the admin edits and numbered **published versions** that phones download, each exercise stored on its own as revisions, so a publish writes only the exercises that changed (`catalog_exercise_revision`, `catalog_version`; [ADR 0026](./adr/0026-the-server-holds-the-catalog-as-a-draft-and-published-versions.md), [#337](https://github.com/tkint/coach-poulpy/issues/337)). The screens are [#338](https://github.com/tkint/coach-poulpy/issues/338); the routes come first.

- **The first start seeds it** from the catalog the server was built with: that version becomes the first published one and its exercises the draft. Later starts leave it alone.
- **An exercise in the draft is the catalog's JSON**, every text a map from language to string (`{"en": "Goblet squat", "fr": "Squat gobelet"}`). Saving needs the English name; the other shipped languages can wait.
- **A slug is editable only until the exercise is first published**; after that a rename is refused (`409 slug-published`), since workouts point at it ([ADR 0008](./adr/0008-curated-catalog-with-stable-slugs.md)). Exercises are retired, never deleted, and a retired exercise can be offered again; an edit keeps an exercise retired or offered.
- **Publishing** makes the draft the next version, numbered past both the latest published one and the bundled one, with the variation groups as they were. Only the exercises the draft changed get a new revision. It is refused while a name, instruction or variation note lacks English or French (`409 missing-translations`, listing the exercises and languages), or when nothing changed (`409 no-changes`). `GET /api/v1/catalog` serves the new version at once.
- **Every write goes to the audit log**: `admin.catalog.create`, `admin.catalog.edit` (a rename reads `old -> new`), `admin.catalog.retire` and `admin.catalog.offer` with the slug as the target, `admin.catalog.publish` with the version number.

The routes, for an admin signed in through the website, are:

- `GET /api/v1/admin/catalog/exercises` with `q` (part of the slug, a name in any language or an alias) and these filters, which combine: `pattern` (a movement pattern, lowercase: `squat`, `hinge`…), `muscles`, `positions` and `equipment` (comma-separated, lowercase, `pull_up_bar`: an exercise matching any of them), `translation` (`complete`, or `incomplete` when a shipped language is missing), `content` (`draft`, never published; `modified`, edited since; `published`, the draft holds what the latest version holds or only a lifecycle change; `pending`, anything the next publication would change) and `lifecycle` (`active`, `retiring`, retired in the draft but not yet in the latest version, or `retired`). Content, translation and lifecycle are separate dimensions. It also takes `sort` (`catalog`, the default and the catalog's order, `name`, `pattern`, `languages`, `change`, `status`), `order` (`asc`, the default, or `desc`), `page` and `size` (20 by default, up to 100), answering one page as `{exercises: [{exercise, published, change, missing}], total}` (a word that isn't allowed is a `400 bad-query`). `published` says whether a version holds the exercise, so whether its slug is still editable; `change` is `added`, `edited`, `retired`, `offered` or null against the latest version; `missing` lists the shipped languages some of its text lacks. Sorting by `languages` puts the exercises missing a language first, by `change` the changed ones first and by `status` the lifecycle.
- `GET /api/v1/admin/catalog/exercises/{slug}`, one exercise in the same shape.
- `POST /api/v1/admin/catalog/exercises` with an exercise (`201`, `400 invalid-exercise` with a `reason`, `409 slug-taken`).
- `PUT /api/v1/admin/catalog/exercises/{slug}` with the whole exercise; a different slug in the body renames it (`404 unknown-exercise`, `409 slug-published`, `409 slug-taken`).
- `POST /api/v1/admin/catalog/exercises/{slug}/retire` and `POST /api/v1/admin/catalog/exercises/{slug}/offer`, which offers a retired exercise again.
- `GET /api/v1/admin/catalog/changes`: what publishing would do, `{version, nextVersion, added, edited, retired, offered, missing}`, each change a `{slug, name}` and each missing entry `{slug, name, languages}`. An edited exercise also has `fields`: each changed field as `{field, before, after}`, with `before` from the latest version and `after` from the draft (null where absent). A text is one field per language and instruction (`name.fr`, `instructions.1.en`); a list of plain values (`muscleGroups`, `equipment`) is one field.
- `POST /api/v1/admin/catalog/publish`: `{version}`, or the 409s above.

## Making the first admin

No endpoint makes an admin. Set `ADMIN_EMAIL` in the `production` environment ([Releasing](./releasing.md#deploy-the-server)) and deploy: when the server starts, the confirmed account with that address becomes an admin. An address with no confirmed account does nothing, so signing up first with someone else's address cannot take the role. The promotion is written to the audit log.

## The audit log

An admin action is an `AdminAction` event (who, what, the target), sent through http4k's `Events` like any other event. One sink stores it in the `admin_audit` table (who is empty when the server itself did it, as at start-up; rows stay when the admin's own account is deleted), and another prints it to the server log, which the log shipping picks up. `GET /api/v1/admin/audit` returns the last 100 rows, and the first screen of the admin shows them. Each admin feature emits an `AdminAction` for its writes; tests check it with `RecordingEvents`.

Sensitive values are values4k types that print masked: `EmailAddress` prints as `a***@example.com`, so an address that reaches a log or an event by accident stays hidden, and the real one is read with `.value` only where it is needed. An event never carries a password or an unmasked address.

## What comes next

The catalog's screens ([#338](https://github.com/tkint/coach-poulpy/issues/338)), editing variation groups, and a tool that copies a published version back into the repository's catalog files so the next build bundles it ([ADR 0026](./adr/0026-the-server-holds-the-catalog-as-a-draft-and-published-versions.md)). The status will take more checks, such as the database and the catalog version, and log viewing is its own issue.
