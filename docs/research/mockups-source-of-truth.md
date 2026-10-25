# Keeping the screen mockups as a versioned source of truth

Research notes, 25 October 2026. Nothing here is decided yet; the owner chooses.

## Short answer

Move the mockups into the repo as plain, standalone HTML pages (option 3), one file per screen under `design/mockups/`, styled only with the generated `--cp-*` CSS variables and the generated Poulpy SVGs, and show them on the docs site the way the brand book is shown today. Every change then goes through a PR, the history is Git's, Claude edits them like any other file, and a `--check` script in CI keeps them on the tokens, as ADR 0012 already does for themes and drawings. Do not commit the claude.ai canvas files as they are (option 2): they only render with the artifact type's runtime and their images only resolve on claude.ai. Once a screen ships, its truth moves to screenshot tests on each platform (option 5), added screen by screen later, and its mockup is marked shipped. The claude.ai canvas becomes a scratch space and comment preview, never the source.

## Context

- Coach Poulpy is a Kotlin Multiplatform core with native Compose and SwiftUI apps, a Vue web placeholder and a VitePress docs site in `site/`, built on every PR and published from `main` by `.github/workflows/pages.yml`. One developer works mostly through Claude agents.
- `docs/design-system.md` ("Screens") says the mockups canvas in the Claude project holds every screen, and that a new screen gets a mockup before it is built.
- That canvas is a claude.ai Design artifact: `project/canvas.json` plus 42 `project/*.dc.html` artboards at 390 by 844. The artboards use `<x-dc>` markup with `{{holes}}`, `<dc-import>` between artboards, a runtime supplied by the artifact type (`dc-runtime.js`, `app.js` of about 4.4 MB) and images or fonts stored as `/_blob/<id>` URLs that only resolve on claude.ai. These facts were read from the artifact's file listing and its type instructions on 25 October 2026.
- Precedents in the repo: `design/tokens.json` and `design/build-poulpy.mjs` are single sources generated into every platform, with `--check` in CI (`docs/adr/0012-design-tokens-generated-from-one-file.md`, `design/README.md`). The brand book is one standalone HTML file, `site/public/brand/book.html` (about 61 KB, its only external dependency is Google Fonts), embedded in an iframe by `site/brand-book.md`.
- The apps have no Compose Previews, SwiftUI Previews or screenshot tests yet (a search of the repo found none). The Android build uses AGP 8.13 and Kotlin 2.4 (`gradle/libs.versions.toml`).

### Mockups and shipped screens are two different truths

Write the Docs describes docs as code as "writing documentation with the same tools as code": issue tracking, Git, plain-text markup, peer review and automated checks ([Write the Docs](https://writethedocs.org/guide/docs-as-code)). The UK Government Digital Service prototypes in HTML kept on GitHub because "every change ("commit") is tracked and can be rolled back" and "multiple people can contribute to a single prototype", while prototypes "are made to be tested and iterated (and if they fail, thrown away)" ([GDS design notes](https://designnotes.blog.gov.uk/2014/10/13/how-designers-prototype-at-gds/)); its prototype kit "is designed for prototyping, not for production code" ([GOV.UK Prototype Kit principles](https://prototype-kit.service.gov.uk/docs/principles)). The inference for this repo: a mockup is the intent before a screen exists and while it changes; once the screen ships, the app itself is the truth and the mockup should say so rather than silently drift.

## Comparison

| | 1. Canvas on claude.ai | 2. Canvas files in repo | 3. Plain HTML in repo | 4. Figma or Penpot | 5. Previews and screenshot tests | 6. Committed images |
| --- | --- | --- | --- | --- | --- | --- |
| Single source | Canvas | Two copies to sync | Repo | Design tool | App code | Repo, but exported from elsewhere |
| PR review and diff | None | Text diff of DC markup | Text diff, plus rendered pages from CI | None in Git | Image diff of references | GitHub image diff |
| History | claude.ai versions | Git, plus claude.ai | Git | Tool (30 days on Figma Starter) | Git | Git |
| Claude can edit | Yes, through the artifact API | Yes, but must re-sync | Yes, any agent with the repo | Figma: beta, paid plans; Penpot: MCP | Yes, it writes the screen | Only by regenerating |
| Docs site | Link only | Does not render without the runtime | Iframe, like the brand book | Live embed (Figma) | Show the reference PNGs | `<img>` |
| Drift with real screens | Not detected | Not detected | Token check only | Code Connect links components, not screens | Caught in CI | Not detected |
| Cost and lock-in | Free, tied to claude.ai | Tied to claude.ai runtime | None | Figma plans; Penpot free, self-hostable | None | LFS quota if used |
| Setup effort | None | Medium, ongoing sync | One-time conversion of 43 artboards | High, redraw everything | Medium per platform, per screen | Low |

## Option 1: keep the claude.ai canvas as the source

This is today. It is the cheapest and keeps comments and claude.ai version history. Its weaknesses are the ones the coordinator named: no PR review, a change can land without anyone seeing it next to the code it affects, the history lives outside the repo, and the canvas is private, so the docs site can only link to it. Committing exported PNG snapshots next to it (option 6) adds a visible history in Git but creates a second copy that drifts the day someone forgets to export (inference). Keep this as the place for quick exploration, not as the record.

## Option 2: commit the canvas files (`canvas.json` and `.dc.html`)

This gives a Git history and PRs, but the files are not the screens: they are templates for a runtime that the claude.ai artifact type supplies (`dc-runtime.js`, about 4.4 MB of `app.js`), and their images and fonts are `/_blob/<id>` URLs that only resolve on claude.ai (read from the artifact). So:

- The docs site cannot render them unless the runtime is vendored into the repo, which is a proprietary bundle the project does not control and whose licence for redistribution is unknown (not verified).
- A diff shows `<x-dc>` markup and `{{holes}}`, readable but not what the screen looks like.
- Two-way sync means every edit on either side must be copied to the other, with no tool that detects when they diverge (inference). With one developer and several agents, the two copies will drift.

Only worth it if the canvas editor stays the main editing tool, which conflicts with "every change goes through a PR".

## Option 3: plain standalone HTML mockups in the repo

Each screen is a small HTML page, 390 by 844, like the brand book: no runtime, Google Fonts or the bundled fonts, `web/src/styles/tokens.css` for colours, type and spacing, and `design/poulpy/*.svg` for Poulpy. This is docs as code in the Write the Docs sense ([Write the Docs](https://writethedocs.org/guide/docs-as-code)) and the GDS way of prototyping in HTML under version control ([GDS design notes](https://designnotes.blog.gov.uk/2014/10/13/how-designers-prototype-at-gds/)).

- **Source and edits.** The repo holds the only copy. Claude agents edit it with ordinary file tools, in a branch, through a PR; the owner can still open a file in a browser and tweak it by hand.
- **Review.** GitHub shows an HTML change as a text diff, not as a picture (inference from how GitHub renders non-code files: its list of rendered formats names images, CSV, PDF, 3D and maps, not HTML; [GitHub Docs](https://docs.github.com/en/repositories/working-with-files/using-files/working-with-non-code-files)). To see the change, a CI job can render each changed mockup to PNG with Playwright and upload the images as a build artifact. Playwright warns that "browser rendering can vary based on the host OS, version, settings, hardware", so pixel baselines only hold when made in the same environment ([Playwright](https://playwright.dev/docs/test-snapshots)); that is why this note suggests uploading renders for review rather than committing them as baselines.
- **Docs site.** The pages are static files, so the site can embed them in iframes the way `site/brand-book.md` embeds `book.html`, and the Pages build on every PR already fails if a page breaks.
- **Drift.** A script in the spirit of ADR 0012 can fail CI when a mockup uses a raw colour, a font size or a spacing value outside the tokens, links a missing Poulpy drawing, or is missing from the index. It cannot tell whether the shipped screen still matches; that is option 5's job.
- **Cost.** One conversion of the 42 artboards: inline each `<dc-import>`, fill the `{{holes}}` with real sample text, download each `/_blob/` image or replace it with a generated Poulpy SVG, and swap hard-coded values for `var(--cp-*)`. Claude can read the artifact's files to do this (inference: the Artifact tool can read an artifact's published files by path).
- **Loss.** Canvas comments and the canvas's free arrangement of artboards. An index page with sections and notes from `canvas.json` replaces the arrangement; PR comments replace canvas comments.

## Option 4: Figma or Penpot

Figma is the industry default and has the richest tooling, but most of it sits behind paid plans:

- On the Starter plan users "can only access up to 30 days of version history" ([Figma Learn](https://help.figma.com/hc/en-us/articles/360038006754)). Branching is "available on the Organization and Enterprise plans" and needs a Full seat ([Figma Learn](https://help.figma.com/hc/en-us/articles/360063144053)).
- Code Connect shows real code snippets in Dev Mode for React, HTML, SwiftUI and Jetpack Compose, and needs "a Dev or Full seat on the Professional, Organization, or Enterprise plan" ([Figma developer docs](https://developers.figma.com/docs/code-connect/quickstart-guide)). It links components, not whole screens, so it does not detect a screen drifting from its design.
- Agents can now edit files through the remote MCP server's `use_figma` tool, but "writing to the canvas is currently in beta", on Professional plans and up, and will become paid by usage ([Figma developer docs](https://developers.figma.com/docs/figma-mcp-server/write-to-canvas)).
- "Figma Live Embeds allow you to add live Figma files or prototypes into other websites or apps" ([Figma Learn](https://help.figma.com/hc/en-us/articles/360039827514)), so the docs site could embed frames.

Penpot is open source (MPL 2.0), "designed for self-hosting", "works with open standards like SVG, CSS, HTML, and JSON", and offers an MCP server and an open API ([Penpot on GitHub](https://github.com/penpot/penpot)). Its README did not describe file version history, so that is unverified here.

Either way the source moves out of Git into a tool with its own history, no PR review and a full redraw of every screen. For one developer working through agents, this adds a tool without solving review or drift.

## Option 5: code as the mockup, with screenshot tests

Here the truth is the shipped screen, rendered from Compose Previews and SwiftUI views, and the reference images committed next to the tests.

- **Android, Google's tool.** Compose Preview Screenshot Testing renders `@PreviewTest` previews on the host, stores references in `{module}/src/screenshotTest{Variant}/reference`, and fails `validateDebugScreenshotTest` with an HTML report on a difference. It is alpha ("features and APIs are subject to change substantially"), the standalone plugin is already deprecated in favour of AGP test suites from AGP 9.5, and the Gradle-only route needs AGP 8.5 or later ([Android Developers](https://developer.android.com/studio/preview/compose-screenshot-testing)). The repo's AGP 8.13 qualifies.
- **Android, Roborazzi.** Runs on the JVM with Robolectric, no emulator; `recordRoborazziDebug`, `compareRoborazziDebug` and `verifyRoborazziDebug` record, diff and fail; it can generate tests from `@Preview` functions with ComposablePreviewScanner. Its default output is `build/outputs/roborazzi`, so `outputDir` must point into `src/` for references to be committed ([Roborazzi](https://github.com/takahirom/roborazzi)).
- **iOS.** swift-snapshot-testing records a reference on the first run (the test fails once) into `__Snapshots__` next to the test, and warns that "snapshots must be compared using the exact same simulator that originally took the reference" ([swift-snapshot-testing](https://swiftpackageregistry.com/pointfreeco/swift-snapshot-testing)). CI's iOS job would need a pinned simulator.
- **Web.** Storybook "supports cross-browser visual testing natively using Chromatic, a cloud service" ([Storybook](https://storybook.js.org/docs/writing-tests/visual-testing)); not needed while the web app is a placeholder.

Committed PNG references get GitHub's image diff in PRs, with 2-up, swipe and onion skin views, the last of which "really comes in handy when elements move around by small, hard to notice amounts" ([GitHub Docs](https://docs.github.com/en/repositories/working-with-files/using-files/working-with-non-code-files)). This is the only option that catches a real screen drifting. It cannot hold a screen that does not exist yet, so it complements a mockup rather than replacing it.

## Option 6: exported images committed to the repo

GitHub renders PNG, JPG, GIF and SVG and diffs images in three views ([GitHub Docs](https://docs.github.com/en/repositories/working-with-files/using-files/working-with-non-code-files)), so committed exports review well. But an image is an output: whoever edits must export again, and nothing fails when they forget (inference). If the images grow large, Git LFS on GitHub Free includes 10 GiB of storage and 10 GiB of bandwidth ([GitHub Docs](https://docs.github.com/en/billing/concepts/product-billing/git-lfs)), and `actions/checkout` does not fetch LFS files unless `lfs: true` is set ([actions/checkout](https://github.com/actions/checkout)), so the Pages build would need that flag. At 42 phone screens the images are small enough that LFS is not needed (inference). Useful as a generated by-product of option 3, not as a source.

## Recommendation for this repo

Option 3 for intent, option 5 for shipped screens, the canvas as scratch.

1. **Record the decision.** Add ADR 0013, "Mockups are plain HTML in the repo", and rewrite the "Screens" section of `docs/design-system.md`: mockups live in `design/mockups/`, a new screen gets a mockup in a PR before it is built, and the claude.ai canvas is only a scratch space. This changes the rule in `docs/design-system.md`; it does not contradict ADR 0012, it extends it.
2. **Folder layout.** `design/mockups/index.json` lists the screens in order with their section (Home, Library, workout page, exercise detail, builder, guided player, Live, History, session detail, Settings), notes from `canvas.json`, and a status: `draft`, `agreed` or `shipped`. One file per artboard, such as `design/mockups/home-planned.html`, plus a shared `design/mockups/mockup.css` for the phone frame and common components (Primary button, Chip, Poulpy card, Plan sheet...). Mockups read `web/src/styles/tokens.css` and `design/poulpy/*.svg`; no copied hex values.
3. **Convert once.** A Claude session reads the canvas artboards from the artifact, inlines each `<dc-import>`, fills each `{{hole}}` with sample content in glossary terms (a Workout, a Session, a Plan), replaces `/_blob/` images with generated Poulpy drawings or committed files, and opens one PR per section so each stays reviewable. Keep the canvas link in the ADR as the archive of the old version.
4. **Show them on the site.** Extend `site/copy-docs.mjs` to copy `design/mockups/` and the token CSS and Poulpy SVGs they use into `site/public/mockups/`, and add `site/mockups.md`, a gallery of 390 by 844 iframes grouped by section with each status, linked from the Design system sidebar. The site then always shows `main`.
5. **Check in CI.** Add `design/check-mockups.mjs`, run in the website job of `ci.yml` next to the two existing `--check` steps. It fails when a mockup is missing from `index.json` or the reverse, uses a colour, font size, radius or spacing value that is not a `var(--cp-*)`, references a missing Poulpy SVG or image, or loads a script from anywhere but the repo. Optionally, a second job renders each changed mockup to PNG with Playwright and uploads them as a build artifact for review.
6. **How Claude edits.** Like any change: an issue, a `design/<issue>-<short-description>` branch, edits to the HTML, the check, a PR. To get comments on a draft, Claude may publish the file as a private artifact, but the PR is the record and nothing is copied back from claude.ai by hand.
7. **After a screen ships.** Add screenshot tests for it on both platforms in the same PR or the next one: Roborazzi (stable, previews-based, references under `app-android/src/test/screenshots`) or Google's Compose Preview Screenshot Testing once AGP test suites settle, and swift-snapshot-testing on a pinned simulator. Mark the mockup `shipped` and link its references; from then on a change to that screen updates the code and its references, and the mockup only changes when the screen is redesigned.

## Open questions for the owner

- Is losing canvas comments and free placement acceptable, or should the canvas stay the editing tool for exploration with a manual "promote to repo" step?
- `design/mockups/` (beside the other sources, copied into the site) or `site/public/mockups/` (beside the brand book)? This note assumes the first.
- Should mockups exist in French too, or English only with the French strings checked in the apps?
- Light only, or a dark variant of each screen? The token CSS already supports `prefers-color-scheme`.
- Should rendered PNGs be committed (reviewable in GitHub's image diff, but rendering differs between machines) or only uploaded by CI?
- Roborazzi or Google's alpha tool for Android, and is a pinned iOS simulator in CI acceptable for SwiftUI snapshots?
- Should a shipped mockup be kept, frozen, or deleted once screenshot references exist?


## Not verified

- Whether the claude.ai artifact runtime may be redistributed in a public repo.
- Penpot's file version history.
- That GitHub shows HTML files only as source in PR diffs is inferred, not read in GitHub's docs.
