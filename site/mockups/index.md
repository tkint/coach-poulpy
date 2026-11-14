---
aside: false
---

# Mockups

The screen mockups from [`design/mockups/`](https://github.com/tkint/coach-poulpy/tree/main/design/mockups), as they are on `main` ([ADR 0016](/docs/adr/0016-mockups-are-plain-html-in-the-repo)). Each app view has its own page, with its available states shown together. This keeps related states easy to compare without mixing separate areas of the app.

Mockups are split by platform, then by stage:

- **[Mobile](/mockups/mobile/)**: the Android and iOS apps (phone and tablet screens), drawn with `design/mockups/shared/mockup.css`.
  - [Agreed](/mockups/mobile/agreed): screens the owner validated. The apps are built from these.
  - [Drafts](/mockups/mobile/drafts): screens being explored or redesigned. A draft becomes agreed through a pull request that moves it out of `drafts/`.
- **Web**: the websites' screens are stories in the <WorkshopLink>component workshop</WorkshopLink>, under Web > Screens ([ADR 0029](/docs/adr/0029-mockups-are-stories-in-the-component-workshop)).
