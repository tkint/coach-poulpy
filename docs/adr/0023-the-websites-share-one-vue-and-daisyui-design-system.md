---
status: accepted
date: 2026-11-07
---

# The websites share one Vue and DaisyUI design system

The landing site, the web app ([ADR 0021](./0021-the-landing-site-the-app-and-the-api-have-their-own-hostname.md)), the web mockups and the docs site draw with one package, `web/ui` (`@coach-poulpy/ui`). It is [DaisyUI 5](https://daisyui.com) on Tailwind CSS 4 with one Poulpy theme, a few `cp-*` classes for the shapes DaisyUI lacks, and thin Vue components named after the [design system](../design-system.md#components)'s table (`CpButton`, `CpChip`, `CpSearchField`, `CpSelectField`, `CpStepper`, `CpPoulpy`, `CpPoulpyCard`, `CpErrorNote`, `CpSheet`). The class names are the contract: a Vue component renders them, and a plain-HTML web mockup writes them. DaisyUI's classes carry the `cp-` prefix (`cp-btn cp-btn-primary`), so every class of the design system reads as ours, and our own classes never reuse a DaisyUI name.

## How it fits together

```text
design/tokens.json ──build-tokens.mjs──▶ web/ui/src/tokens.css   (--cp-*, light and dark)
web/ui/src/theme.css                     DaisyUI's colours, radii and sizes point at --cp-*
web/ui/src/components/*.vue              the Vue components
web/ui/build-css.mjs ──▶ web/ui/poulpy-ui.css        prebuilt, for web mockups (generated)
                    └──▶ web/ui/poulpy-ui-embed.css  no page-wide rule, for the docs site (generated)
web/app                                  imports tokens.css and theme.css into its own Tailwind build
```

- **One theme, light and dark from the tokens.** DaisyUI's variables are `var(--cp-*)` references, so `tokens.css` alone switches light and dark (system setting, or `data-theme="light"` / `"dark"` on `<html>`), as it does for the mockups today. There is no second copy of the palette to drift from `design/tokens.json` ([ADR 0012](./0012-design-tokens-generated-from-one-file.md)).
- **The roles map onto DaisyUI's names**: `primary` stays primary, `primaryContainer` is secondary (tonal), `grumpy` is accent, `surface`, `background` and `surfaceVariant` are base-100, 200 and 300. DaisyUI's warning is set to the error colour, because ember never means a warning.
- **Web mockups stay plain HTML** ([ADR 0016](./0016-mockups-are-plain-html-in-the-repo.md)). They link `tokens.css` and the prebuilt `poulpy-ui.css`, and need no browser-side build. Tailwind keeps only the classes it finds in the components and in `design/mockups/*/web/`; the web build generates both CSS files from source, and the docs-site build copies the mockup stylesheet into its published assets. Generated CSS is ignored by Git and is never committed.
- **The docs site shows the real components** on its Web components page, from source, with the embed build: no reset, no root colours, so the site keeps its own look. Each sample panel pins a theme with a `cp-theme-light` or `cp-theme-dark` class from `tokens.css`.

## Why

- The websites, the mockups that describe them and the docs that show them cannot drift apart when they share one set of classes.
- DaisyUI gives accessible, ready-made forms, dialogs and menus as plain classes, which plain-HTML mockups can use with no JavaScript; a Vue-only library could not be drawn in a mockup.
- Pointing the theme at the existing `--cp-*` variables keeps `design/tokens.json` the only place a colour is chosen.

## Consequences

- Phone and tablet mockups stay on `design/mockups/shared/mockup.css`, because they draw the native Compose and SwiftUI screens, which DaisyUI's look does not match (chosen by the owner on 7 November 2026). Only `web/` mockups use the shared CSS.
- `web/` is an npm workspace: `web/ui` and `web/app` today, `web/landing` when the landing site is written. `tokens.css` moved from `web/src/styles/` to `web/ui/src/`.
- DaisyUI and Tailwind are pinned to exact versions so builds produce reproducible CSS; upgrading them is a PR that regenerates the build output.
- A component the websites need goes in `web/ui` with its classes on the [Web components](https://tkint.github.io/coach-poulpy/web-components) page, never as one-off styles in an app.
