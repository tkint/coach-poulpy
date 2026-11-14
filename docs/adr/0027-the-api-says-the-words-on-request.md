---
status: accepted
date: 2026-11-14
---

# The API says the words on request

Builds on [ADR 0020](./0020-every-credential-resolves-to-one-caller.md) and [ADR 0021](./0021-the-landing-site-the-app-and-the-api-have-their-own-hostname.md).

The signed-in website shows the account's workouts the way the phone does: "Sets · 5 exercises · about 40 min", the muscles worked, the prescription of each step. That wording (and the muscle heat behind the body map) is decided once, in the shared Kotlin core, in English and French. The website must not grow a second copy of it in TypeScript.

- **The server builds the words.** `GET /api/v1/workouts` and `GET /api/v1/workouts/{id}` read the latest version of each workout and the sessions done, and build the list and the page with the shared `WorkoutViews`, the same code the phone's library and workout page use.
- **Words come on request, not by default.** Every answer carries structured values only. A route that can say more takes `include`, with one or several values, comma separated or repeated (`include=text`), like GitLab's API. A plain API call (a script, a future client) is not padded with text, and later routes can add other extras. An unknown value is 400 `bad-query`. We chose this over a separate endpoint for the wording (two calls to join, and a list would need one per row) and over always sending text (it pollutes pure calls and fixes one language in the payload).
- **The language** is the first of `Accept-Language` the app has words for, otherwise the account's language. The answer says `Content-Language` and `Vary: Accept-Language`.
- **Read-only.** The website holds the `workouts:read` scope like every first-party client and never writes: the phone is the source of truth ([Accounts, Sync](../accounts.md#sync)).

## Consequences

- The server depends on the `shared` module for this one class and the wording behind it, which brings the phone's database and Bluetooth code onto its classpath. A `// CLEANUP:` in `WorkoutViews` marks it: move the wording into its own module the phone and the server both depend on.
- Each new website page that shows computed words adds an `include=text` view next to its route, tested at the HTTP seam.
