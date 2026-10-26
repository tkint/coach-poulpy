---
status: accepted
date: 2026-10-26
---

# Mockups are plain HTML in the repo

Screen mockups live in `design/mockups/` as plain HTML pages, styled only with the generated token CSS and Poulpy drawings, and change through PRs like the code. The docs site shows them, and a CI check keeps them on the tokens, the way ADR 0012 keeps the themes and drawings on `design/tokens.json`. The claude.ai Design canvas that held them until now only renders with claude.ai's own runtime and loads its images from claude.ai, so it cannot be reviewed in a PR or shown on the site; it stays a scratch space for rough ideas, never the record. The research behind this is in [`docs/research/mockups-source-of-truth.md`](../research/mockups-source-of-truth.md).

## How the folder is organised

```
design/mockups/
  shared/                  mockup.css (the device frames and common components), shared images
  drafts/phone/            screens being explored or redesigned
  drafts/tablet/
  drafts/web/
  agreed/phone/            screens the owner validated; what the apps are built from
  agreed/tablet/
  agreed/web/
```

- **Drafts are kept apart from validated mockups.** A new screen or a redesign starts in `drafts/`. Validating it is a PR that moves it to `agreed/`, replacing the old version there, and the owner's merge is the approval. Nothing is built from a draft.
- **One folder per form factor.** Phone, tablet and website screens each get their own folder and device frame. A screen keeps the same file name in every folder (`home-planned.html`), so its phone, tablet and web versions, and its draft and agreed versions, are easy to pair.
- **Each file describes itself** in its `<head>`: `<title>`, a `mockup:section` meta tag, and `mockup:shipped` naming the screenshot test references once the screen ships. The check script and the site gallery read these tags; there is no separate index to keep in step.

## Rules

- English only, light only. French copy is checked in the apps; the token CSS already handles dark.
- No network: no `fetch`, no scripts or styles from outside the repo except the brand fonts.
- Real elements (`<button>`, `<a>`, labels), a `data-component` attribute naming the design-system component, and `id`s in glossary terms for the regions an issue would name.
- Rendered PNGs are uploaded by CI for review, never committed, because renders differ between machines.

## Consequences

- The canvas's 42 artboards are converted once, section by section, into `agreed/phone/`. Until that lands, the canvas still holds the existing screens.
- Once a screen ships, screenshot tests become its truth: Roborazzi on Android and swift-snapshot-testing on a pinned iOS simulator in CI. Its agreed mockup is kept, frozen, with `mockup:shipped` pointing at the references, and only changes when the screen is redesigned through a new draft.
- Canvas comments and free placement are lost; PR comments and the site's gallery replace them.
