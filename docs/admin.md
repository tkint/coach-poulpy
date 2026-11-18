# The admin area

The admin area is where the maintainer looks after the catalog, the accounts and the server's settings. It is a section of the website under `/admin` ([#26](https://github.com/tkint/coach-poulpy/issues/26)). It never shows workouts or health data.

This page holds the rules: who can do what, and what happens when they do. How the server enforces them is in [Admin internals](./admin-internals.md).

## Who can open it

Admin is a role on the account, either member or admin. Nobody can ask for it ([ADR 0020](./adr/0020-every-credential-resolves-to-one-caller.md)). There is one admin for now and no finer rights.

An admin reaches the area only by signing in on the website. The phone app, a personal access token and third-party apps never reach it, even for an admin.

A member who tries to sign in on the website is signed out at once with the usual "wrong email or password". The sign-in never reveals an account's role.

## Making the first admin

An admin is made at deploy time, never from the website. The maintainer sets `ADMIN_EMAIL` ([Releasing](./releasing.md#deploy-the-server)) and the confirmed account with that address becomes an admin when the server starts. An address with no confirmed account does nothing, so signing up first with someone else's address can't take the role. The promotion is recorded in the audit log.

## Accounts

The **Accounts** screen lists accounts with email, role, confirmation, sign-in status and creation date. You can filter, sort, page and open an account by id. The view lives in the address, so it can be bookmarked ([navigation](./web-app.md#navigation-lives-in-the-url)).

An admin can:

- **Disable** an account. It can't sign in and every device is signed out. **Enable** reverses it. A disabled account gets the same "wrong email or password" as any other refusal.
- **Confirm email** for an unconfirmed account, in its side panel ([#382](https://github.com/tkint/coach-poulpy/issues/382)). Use it when the email never arrived, or when running locally with no mail server ([Trying it locally](./the-api.md#trying-it-locally)). It has the same effect as the person following the link. A dialog first says the admin vouches for an address nobody proved.
- **Delete** an account after typing its email again. This removes the account and all its data, exactly as when a user deletes their own account.

An admin can't disable or delete their own account.

## Settings

The **Settings** screen changes the email from-address without a deploy.

- **Only non-secret settings are editable here.** The mail server's host, user and password are set at deploy time and never shown ([Deploying the server](./deploying-the-server.md)).
- **A saved from-address replaces the default** from the next email. Both `hello@example.com` and `Coach Poulpy <hello@example.com>` work. Anything else is refused and nothing is saved.
- **Send test email** sends a message to the signed-in admin and says whether the mail server accepted it.

## Status

The **Status** screen shows whether mail works and who is waiting to confirm. It is read-only ([#340](https://github.com/tkint/coach-poulpy/issues/340)).

- **Mail** says whether a mail server is set up and, if so, how the last email went: its kind (confirmation, password reset or test), when, and whether it worked. A server restart forgets it. The mail server's details are never shown.
- **Pending registrations** are accounts whose email isn't confirmed, newest first, the last 100. Each row shows the address, when it signed up and when its latest link expires. To confirm one, open the account in [Accounts](#accounts).

The public `/status` page of the web app is separate. Anyone can open it. It says only whether Coach Poulpy is up or down, never how the server is configured.

## Catalog

The server keeps the exercise catalog in two forms: a **catalog draft** the admin edits, and **published revisions** that phones download. Each exercise has its own numbered revisions, so publishing touches only the exercises it names ([ADR 0030](./adr/0030-the-catalog-changes-by-exercise-revision-not-by-catalog-version.md)). The first start seeds both from the catalog the server was built with.

The admin has an exercise list at `/admin/catalog` and an editor at `/admin/catalog/<slug>`. Publishing and retiring are in the list's selection menu ([#338](https://github.com/tkint/coach-poulpy/issues/338)).

- **Every text is written per language.** Saving needs the English name. Other languages can wait, but publishing needs English and French.
- **A slug can change only until the exercise is first published.** Workouts point at it, so after that a rename is refused ([ADR 0008](./adr/0008-curated-catalog-with-stable-slugs.md)).
- **Exercises are retired, never deleted.** A retired exercise can be offered again.
- **Publishing** gives each named exercise its next revision, or every exercise with a change when none is named. It is refused when a translation is missing or when nothing has changed. Phones see the new revisions at once.

An exercise's **change** since its latest published revision is added, edited, retired, offered or none. Its **lifecycle** is active, retiring (retired in the draft, not yet in a published revision) or retired. A preview lists what publishing would do, with each edited field before and after.

## The audit log

Every admin write is recorded: who did it, what, and the target. The first screen of the admin shows the last 100 entries. Entries stay when the admin's own account is deleted, and an entry with no author means the server did it, as at start-up.

Targets never hold an address in clear. An account is recorded by its id and an email by its masked form (`a***@example.com`). A password is never recorded.
