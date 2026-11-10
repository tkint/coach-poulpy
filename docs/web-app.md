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

They are agreed (see Mockups, Agreed, Web app on the site).

- **Sign in** is the landing route. A person who is already signed in goes straight to the profile. A wrong email or password gets one message that doesn't say which. (`app-sign-in`, `app-sign-in-error`)
- **Status**, at `/status`, says whether Coach Poulpy is up or down and nothing else, for anyone, signed in or not; the admin has the details ([Status](./admin.md#status)). (`app-status`, `app-status-down`)
- **Profile** shows the address and that the email is confirmed, a button that emails a password reset link, and a note that workouts and the body log live on the phone. It shows only what the server holds for the web: the profile fields stay on the phone, and `sync` is a scope of the apps ([Accounts](./accounts.md#sync)). (`app-profile`)

## Not yet

A "Forgot your password?" link on the sign-in page, the profile fields, workouts and sessions on the web.
