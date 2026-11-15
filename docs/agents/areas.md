# Working by area

Five areas of the repo used to have a long-running role thread each. They no longer do: the knowledge lives in the files below, and any short thread reads them before it works. Only the **Maintainer** stays a long-running thread, because it collects `// CLEANUP:` markers over time.

A task thread reads the section for each area it touches, does the work itself, and asks the owner (one decision, with a recommended option) when the docs don't settle it. There is one review per PR, not one per area.

## Architect: model, engine, schema

Read `CONTEXT.md`, `docs/adr/`, `docs/architecture.md`.

- Name things with the glossary's words. A word the glossary lacks is added to `CONTEXT.md` in the same PR.
- A decision that would surprise the next reader gets an ADR in the same PR (next free number, then the sidebar picks it up). Say explicitly when a change contradicts one.
- Before changing a stored shape (database, catalog, files on the phone), check whether existing installs break. If they do, ask the owner whether migration or backward-compatibility code is wanted, and write none until they answer. A schema change is a new SQLDelight migration.
- Logic lives in shared Kotlin with a test. Apps and the server never decide what a workout is.
- Domain text reaches Kotlin through typed generated functions (`DomainText`), not string lookups ([ADR 0014](../adr/0014-text-lives-in-one-json-file-per-language-and-namespace.md)). A generator that writes files also deletes the stale ones it wrote before.
- A change in `build-logic` or the shared engine gets its tests run with `./gradlew jvmTest jsTest :server:test :tools:i18n:test`.

## Designer: screens and mockups

Read [the design system](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/design-system-introduction--docs) and [ADR 0029](../adr/0029-mockups-are-stories-in-the-component-workshop.md).

- A screen starts as a story in the component workshop tagged `draft`. A separate, later PR changes the tag to `agreed` once the owner has reviewed it in the PR's preview. Nothing is built from a draft, and a ticket with no agreed mockup waits for one.
- Mockups use `var(--cp-*)` roles only, so one story works in light and dark. Check both with the workshop's theme toolbar before opening the PR. A phone screen is built from the `Mobile*` components and patterns; a piece it needs that they lack becomes one, with a story per variation.
- A web screen or mockup that needs a DaisyUI component `web/ui` doesn't have yet adds it there first, in the same PR: a `Cp` Vue component with the `cp-` prefix, a story file in `web/storybook/stories/` with one story per state (open and closed for anything that opens, empty, error, disabled) and a `play` function for each behaviour, and the classes it renders in its `.md` description beside it, then `npm run build -w ui` in `web/`. A web mockup is a story that composes those components; never an unprefixed DaisyUI class, which draws nothing, and never a one-off style.
- A web screen has its own path, and its state (a table's filters, sort and page, the open tab, the picked row) lives in the query string, never only in the component: see [Navigation lives in the URL](../web-app.md#navigation-lives-in-the-url). A web mockup links its screens to each other with real `href`s.
- In a phone screen story, every tap target is a `<button type="button">` (or a `Mobile*` component, which renders one), never an `<a>`: a mockup's interactions either work or do nothing in a browser. Links between screens belong to web mockups only.
- Android and iOS mirror each other. When they disagree, the agreed mockup wins; when the mockup is wrong, fix it in a PR rather than diverging in code.
- A screen is built to its agreed mockup, all of it: every card, field, control, label and layout the mockup shows. Never ship a simpler stand-in (a plain select for a body map, no reordering, fewer fields) to get a working screen sooner, and never leave a part out without saying so. A part that can't be built as drawn, because the model, the server or a ticket held back by another issue doesn't support it, is named in the PR description and put to the owner before the work goes on; the owner decides between changing the mockup and building the missing piece. Compare the finished screen with the mockup, screenshot against screenshot, before opening the PR.
- Words never break: text is laid out for the longest language. A tab label has about 12 characters of room; shrink or shorten it before letting a word wrap.
- The warm-up is headed by the builder's sun icon in every mockup and screen.
- Icons are Lucide, by their Lucide name, the same meaning with the same icon on every platform (the table in [the design system](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/design-system-rules-icons--docs)). A phone mockup draws one with `<MobileIcon name="<name>" />` or a component's `icon` prop; an icon the apps don't have yet is added with `node design/build-icons.mjs --add <name>`, which also gives it to both apps.
- The English screen copy follows `web/storybook/stories/brand/brand.md`; every string is also added in French (see Translator below).
- Theme files are generated from `design/tokens.json`: edit the tokens, then run `node design/build-tokens.mjs`.

## Poulpy keeper: the mascot

Read [the brand](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/brand-introduction--docs) and [the brand decisions](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/brand-decisions--docs).

- Poulpy is never guilt. Pain or rehab always shows the blue side for 72 h; errors are crimson with an icon, never ember.
- Skipping a planned session never counts as missing it; only entries left hanging do. Ember is a comic mood, not a penalty.
- Poulpy speaks in short first-person lines, warm and a little cheeky. He says *tu* in French while the app says *vous*.
- A new reward, notification, pose or reaction rule is written into `web/storybook/stories/brand/brand.md` (and `web/storybook/stories/brand/brand-decisions.md` when it settles an open question) in the same PR. At most one notification a day.
- Drawings are generated: edit `design/build-poulpy.mjs`, then run `node design/build-poulpy.mjs`.
- The 3D model is generated too: edit `SHAPE`, `FACES`, `MOODS` or `LOOK` in `design/poulpy-3d/model.mjs`, `POSES` in `design/poulpy-3d/poses.mjs` for his poses, or `TURN`, `JUMP`, `CHEER`, `WINK` and `RIG` in `design/poulpy-3d/motion.mjs` for how he moves, then run `node design/build-poulpy-3d.mjs` and `node design/build-poulpy-pictures.mjs` (the pictures of his moods and poses). Its rules are in `web/storybook/stories/brand/poulpy-3d.md`, shown with the live model on the Brand > Poulpy in 3D page of the component workshop and, one move a page, under Brand > Poulpy moves; change that file in the same PR as a rule.
- The body map figures are generated too: edit `design/build-body-map.mjs` or the outlines in `design/body-map-source/` (keep its NOTICE.md), then run `node design/build-body-map.mjs`.

## Translator: text in every language

Read [`i18n/README.md`](../../i18n/README.md) and the glossaries in `i18n/glossary/`.

- A new or edited string goes into `i18n/messages/en` and `i18n/messages/fr` in the same PR. Never write a native string file; Android, iOS and `DomainText` are generated.
- Counts must agree with their number: use an ICU plural for anything countable, so "1 rep" and « 1 tentacule » read right. Check the French forms (`one`, `many`, `other`) too.
- French typography (non-breaking spaces before `: ; ? !` and inside « »), and the rest of the checks, are automated by `i18n/typography.mjs`.
- Native speakers proofread in Fink, which opens pull requests. The docs page for them is "Proofread and translate" page of the Translations section; keep it free of repo paths.
- The catalog lives in the server database and is edited in the admin (ADR 0033); no file holds it. Catalog slugs never change; retire an exercise rather than deleting it.
- Text that might not fit on a phone (tab labels, buttons, chips) is read in French on a device before the language is called done.

## Maintainer: housekeeping (long-running)

The Maintainer owns `// CLEANUP:` markers, CI (`.github/workflows/`), `gradle/libs.versions.toml`, `.github/labels.yml`, the `device-test` checklists and triage.

- A change that leaves a workaround, shim, stopgap or known gap marks it `// CLEANUP:` with what to remove or fix and why, plus an issue number when there is one, and says so in the PR description.
- Android and iOS only build in CI, so a PR is done when CI is green. Anything about Bluetooth, sound, haptics, background running or installs needs the `device-test` label and a line on what to check on a phone.

## Reviews

Each PR gets a single review, not one per area. The docs site workflow posts one comment with the preview link and the pages and Storybook stories to review; nobody posts a docs-review comment of their own. Put what the reviewer must know in the PR description.

## Docs on the site

Every guideline, glossary entry, rule or decision is reachable from the published docs site in the PR that writes it: add the file to `site/published.mjs` and a sidebar entry in `site/.vitepress/config.mts`. CI fails otherwise.
