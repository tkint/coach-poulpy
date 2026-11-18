# The admin area

The admin area is where the maintainer looks after the catalog, the accounts and the server's settings. It is a section of the website under `/admin` ([#26](https://github.com/tkint/coach-poulpy/issues/26)). It never shows workouts or health data.

Every route it uses is under `/api/v1/admin/` and described in the OpenAPI document, which is the full reference. This page says what each screen does and why.

## Who can open it

Admin is a role on the account (`member` or `admin`), not a scope anyone can ask for ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)). There is one admin for now and no finer rights.

Only the website's sign-in reaches it, with cookies ([the API](./the-api.md#browser-session-admin-website)). Everyone else is turned away:

- An anonymous caller gets 401.
- A member, an admin on the phone app, a personal access token or a third-party app gets 403.
- A write whose `Origin` is not the website's gets 403. This is the CSRF defence of ADR 0020.

A member who tries to sign in on the website is signed out at once with the usual "wrong email or password", so the sign-in never reveals an account's role.

## Making the first admin

No endpoint makes an admin. Set `ADMIN_EMAIL` in the `production` environment ([Releasing](./releasing.md#deploy-the-server)) and deploy. When the server starts, the confirmed account with that address becomes an admin. An address with no confirmed account does nothing, so signing up first with someone else's address can't take the role.

## Accounts

The **Accounts** screen lists accounts with email, role, confirmation, sign-in status and creation date. You can filter, sort, page and open an account by id. The view lives in the address, so it can be bookmarked ([navigation](./web-app.md#navigation-lives-in-the-url)).

An admin can:

- **Disable** an account. It can't sign in and every device is signed out. **Enable** reverses it. A disabled account gets the same "wrong email or password" as any other refusal.
- **Delete** an account after typing its email again. It removes the account and all its data, through the same code as a user deleting their own account.

An admin can't disable or delete their own account.

Filtering and paging happen on the server by reading every account row and cutting the page in memory. That is enough for a back office.

## Settings

The **Settings** screen changes the email from-address without a deploy.

- **Only non-secret settings live in the database.** The SMTP host, port, user and password stay in the environment ([Deploying the server](./deploying-the-server.md)) and never show here.
- **`MAIL_FROM` is the default** until an address is saved. A saved address applies from the next email, with no restart. Both `hello@example.com` and `Coach Poulpy <hello@example.com>` work. Anything else is refused and nothing is saved.
- **Send test email** sends a message to the signed-in admin and says whether the mail server accepted it.

## Status

The **Status** screen reports the server's mail setup and who is waiting to confirm. It is read-only, apart from one action ([#340](https://github.com/tkint/coach-poulpy/issues/340)).

- **Mail** is configured when `SMTP_HOST` is set. Without it, sending fails with `No mail server is set: SMTP_HOST is unset`. Otherwise the screen shows the last email since the server started: its kind (confirmation, password reset or test), when, and whether it worked. A restart forgets it. The SMTP details are never shown.
- **Pending registrations** are accounts whose email isn't confirmed, newest first, the last 100. Each row shows the address, when it signed up and when its latest link expires.
- **Confirm by hand** ([#382](https://github.com/tkint/coach-poulpy/issues/382)) lets an admin confirm a pending row, for example when the email never arrived or on a laptop with no mail server ([Trying it locally](./the-api.md#trying-it-locally)). It has the same effect as following the link. A dialog first says the admin vouches for an address nobody proved.

The public `/status` page of the web app is separate. Anyone can open it. It says only whether Coach Poulpy is up or down, never how the server is configured, and a test holds it to that.

## Catalog

The server keeps the exercise catalog in two forms: a **catalog draft** the admin edits, and **published revisions** that phones download. Each exercise has its own numbered revisions (1, 2, 3), so publishing writes only the exercises it names ([ADR 0030](./adr/0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md)). The first start seeds both from the catalog the server was built with.

The admin has an exercise list at `/admin/catalog` and an editor at `/admin/catalog/<slug>`. Publishing and retiring are in the list's selection menu ([#338](https://github.com/tkint/coach-poulpy/issues/338)).

- **An exercise is the catalog's JSON**, with every text a map from language to string. Saving needs the English name. Other languages can wait, but publishing needs English and French.
- **A slug can change only until the exercise is first published.** Workouts point at it, so after that a rename is refused ([ADR 0008](./adr/0008-curated-catalog-with-stable-slugs.md)).
- **Exercises are retired, never deleted.** A retired exercise can be offered again.
- **Publishing** gives each named exercise its next revision, or every exercise with a change when none is named. It is refused when a translation is missing or when nothing has changed. Phones see the new revisions at once.

<details>
<summary>Filters, change states and responses</summary>

The list filters by `q` (slug, a name in any language or an alias), `pattern`, `muscles`, `positions`, `equipment`, `translation`, `content` and `lifecycle`, and sorts by `name`, `pattern`, `languages`, `change` and `status`. The OpenAPI document has the exact values.

An exercise's **change** against its latest published revision is `added`, `edited`, `retired`, `offered` or none. Its **lifecycle** is `active`, `retiring` (retired in the draft, not yet in a published revision) or `retired`. `GET /api/v1/admin/catalog/changes` previews what publishing would do, with each edited field's before and after.

Refusals: `409 slug-published`, `409 slug-taken`, `409 missing-translations` (listing exercises and languages), `409 no-changes`, `400 invalid-exercise` and `400 bad-query`.

</details>

## The audit log

Every admin write is recorded in an audit log: who did it, what, and the target. The first screen of the admin shows the last 100 entries. Entries stay when the admin's own account is deleted, and one with no author means the server did it, as at start-up.

Targets never hold an address in clear. An account is recorded by its id, an email by its masked form (`a***@example.com`), and a password never appears. The `EmailAddress` type prints masked, so an address that reaches a log by accident stays hidden.

<details>
<summary>The actions that are recorded</summary>

`admin.account.disable`, `admin.account.enable`, `admin.account.delete`, `admin.account.confirm`, `admin.settings.mail-from`, `admin.catalog.create`, `admin.catalog.edit`, `admin.catalog.retire`, `admin.catalog.offer` and `admin.catalog.publish`, plus the promotion of the first admin.

In the code, each is an `AdminAction` event sent through http4k's `Events`. One sink stores it in `admin_audit` and another prints it to the server log. Tests check it with `RecordingEvents`.

</details>
