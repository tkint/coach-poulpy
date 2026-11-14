# The web app

The web app is the signed-in website for members, at `app.coach-poulpy.com` ([ADR 0021](./adr/0021-the-landing-site-the-app-and-the-api-have-their-own-hostname.md)). It is separate from [the admin area](./admin.md), which stays under `/admin`. The work is tracked in [#355](https://github.com/tkint/coach-poulpy/issues/355).

## How it is built

- **The same stack as the admin**: Vue and DaisyUI from `web/ui` ([ADR 0023](./adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md)). A missing component is added there, never styled in the app.
- **The same sign-in as the admin**: cookies from `POST /api/v1/web/session` ([the API](./the-api.md)). The page never sees a token. Unlike the admin, any confirmed account may sign in.
- **A sidebar**, the admin's `CpDrawer` shell. It lists only the screens that exist; each feature adds its own entry. An **Admin** link to `/admin` sits at the bottom of the sidebar, just above the person, and appears only when `GET /api/v1/me` says the role is `admin`; a member never sees it, and the admin area still checks the role itself ([the admin area](./admin.md)). The admin's sidebar has the matching **Coach Poulpy app** link back to `/`, in the same place.

## Navigation lives in the URL

This holds for every screen of the websites, the web app and the admin.

- **Every screen has its own path.** A sidebar entry is a real link (`<a href>`), so it opens in a new tab and a reload lands on the same screen. Clicking it navigates without reloading the page (`navigate` in `web/app/src/route.ts`), and the back button works.
- **A screen keeps its state in the query string**: a table's filters, sort and page, the open tab, the picked row. The address of a view can be copied, bookmarked and sent to someone, and the back button steps through the changes. A value equal to its default isn't written, so the plain address stays plain (`withParams`).
- **A change a person made goes in as a new history entry; typing replaces the current one**, so a search box doesn't fill the history with one entry per letter.
- **The address is the one source of truth**: the screen derives its state from it (`search`, `path`) and changes it with `navigate`, never a copy that can drift. A link to a row that is not on the loaded page loads that row itself.

The admin's accounts table is the reference: `/admin/accounts?q=lea&role=admin&sort=email&order=asc&page=2&account=<id>` ([the admin area](./admin.md#accounts)).

## The screens

The agreed layouts and states are documented in the Web app mockups on the docs site.

- **Sign in** is the landing route. An already signed-in person goes straight to the workouts. Invalid credentials produce one generic refusal, without revealing which field was wrong.
- **Status**, at `/status`, is public and reports only whether Coach Poulpy is up or down; it does not expose server configuration ([admin status](./admin.md#status)).
- **Workouts**, at `/workouts`, is a searchable, paged table of the workouts the phones synced (`?q=squat&format=sets&page=2`). A row opens the **workout** at `/workouts/<id>`: its parts and steps beside a column on the right with its stats and the muscles it works (a body map); on a phone the stats and muscles open in a sheet (`?overview=1`). Both are read-only, with a note to change a workout on the phone. The website reads what the phones synced through the API's read routes and never writes it: the phone stays the source of truth. Every word of a workout comes from the API (`include=text`, [ADR 0027](./adr/0027-the-api-says-the-words-on-request.md)), so the website holds none of its own.
- **Profile**, at `/profile`, shows the account email and confirmation state, offers a password-reset email, and explains that body measurements and profile fields remain on the phone.

## Not yet

A "Forgot your password?" link on the sign-in page, the profile fields and sessions on the web.
