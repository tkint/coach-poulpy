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

Read [the design system](../design-system.md) and [ADR 0016](../adr/0016-mockups-are-plain-html-in-the-repo.md).

- A screen starts as an HTML mockup in `design/mockups/drafts/`. A separate, later PR moves it to `agreed/` once the owner has reviewed it on the docs site. Nothing is built from a draft, and a ticket with no agreed mockup waits for one.
- Mockups use `var(--cp-*)` roles only, so one file works in light and dark. Check both on the docs site before opening the PR.
- Android and iOS mirror each other. When they disagree, the agreed mockup wins; when the mockup is wrong, fix it in a PR rather than diverging in code.
- Words never break: text is laid out for the longest language. A tab label has about 12 characters of room; shrink or shorten it before letting a word wrap.
- The warm-up is headed by the builder's sun icon in every mockup and screen.
- The English screen copy follows `docs/brand.md`; every string is also added in French (see Translator below).
- Theme files are generated from `design/tokens.json`: edit the tokens, then run `node design/build-tokens.mjs`.

## Poulpy keeper: the mascot

Read [the brand](../brand.md) and [the brand decisions](../brand-decisions.md).

- Poulpy is never guilt. Pain or rehab always shows the blue side for 72 h; errors are crimson with an icon, never ember.
- Skipping a planned session never counts as missing it; only entries left hanging do. Ember is a comic mood, not a penalty.
- Poulpy speaks in short first-person lines, warm and a little cheeky. He says *tu* in French while the app says *vous*.
- A new reward, notification, pose or reaction rule is written into `docs/brand.md` (and `docs/brand-decisions.md` when it settles an open question) in the same PR. At most one notification a day.
- Drawings are generated: edit `design/build-poulpy.mjs`, then run `node design/build-poulpy.mjs`.

## Translator: text in every language

Read [`i18n/README.md`](../../i18n/README.md) and the glossaries in `i18n/glossary/`.

- A new or edited string goes into `i18n/messages/en` and `i18n/messages/fr` in the same PR. Never write a native string file; Android, iOS and `DomainText` are generated.
- Counts must agree with their number: use an ICU plural for anything countable, so "1 rep" and « 1 tentacule » read right. Check the French forms (`one`, `many`, `other`) too.
- French typography (non-breaking spaces before `: ; ? !` and inside « »), and the rest of the checks, are automated by `i18n/typography.mjs`.
- Native speakers proofread in Fink, which opens pull requests. The docs page for them is "Proofread and translate" page of the Translations section; keep it free of repo paths.
- Exercise text lives in `catalog.json`. Catalog slugs never change; retire an entry with `"retired": true` and raise `version`.
- Text that might not fit on a phone (tab labels, buttons, chips) is read in French on a device before the language is called done.

## Maintainer: housekeeping (long-running)

The Maintainer owns `// CLEANUP:` markers, CI (`.github/workflows/`), `gradle/libs.versions.toml`, `.github/labels.yml`, the `device-test` checklists and triage.

- A change that leaves a workaround, shim, stopgap or known gap marks it `// CLEANUP:` with what to remove or fix and why, plus an issue number when there is one, and says so in the PR description.
- Android and iOS only build in CI, so a PR is done when CI is green. Anything about Bluetooth, sound, haptics, background running or installs needs the `device-test` label and a line on what to check on a phone.

## Reviews

Each PR gets a single review, not one per area. The docs site workflow comments the preview and the pages to review; nobody posts a docs-review comment of their own. Put what the reviewer must know in the PR description.

## Docs on the site

Every guideline, glossary entry, rule or decision is reachable from the published docs site in the PR that writes it: add the file to `site/published.mjs` and a sidebar entry in `site/.vitepress/config.mts`. CI fails otherwise.
