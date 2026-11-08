# The web app

The web app is the signed-in website for members, at `app.coach-poulpy.com` ([ADR 0021](./adr/0021-the-landing-site-the-app-and-the-api-have-their-own-hostname.md)). It is separate from [the admin area](./admin.md), which stays under `/admin`. The work is tracked in [#355](https://github.com/tkint/coach-poulpy/issues/355).

## How it is built

- **The same stack as the admin**: Vue and DaisyUI from `web/ui` ([ADR 0023](./adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md)). A missing component is added there, never styled in the app.
- **The same sign-in as the admin**: cookies from `POST /api/v1/web/session` ([the API](./the-api.md)). The page never sees a token. Unlike the admin, any confirmed account may sign in.
- **A sidebar**, the admin's `CpDrawer` shell. It lists only the screens that exist; each feature adds its own entry. An **Admin** entry, a plain link to `/admin`, appears only when `GET /api/v1/me` says the role is `admin`; a member never sees it, and the admin area still checks the role itself ([the admin area](./admin.md)).

## The screens

They are drafts until the owner agrees them (see Mockups, Drafts, Web app on the site).

- **Sign in** is the landing route. A person who is already signed in goes straight to the profile. A wrong email or password gets one message that doesn't say which. (`app-sign-in`, `app-sign-in-error`)
- **Profile** shows the address and that the email is confirmed, a button that emails a password reset link, and a note that workouts and the body log live on the phone. It shows only what the server holds for the web: the profile fields stay on the phone, and `sync` is a scope of the apps ([Accounts](./accounts.md#sync)). (`app-profile`)

## Not yet

A "Forgot your password?" link on the sign-in page, the profile fields, workouts and sessions on the web.
