# Coach Poulpy

A training platform: Android and iOS apps first, guest-only and offline, with a server and website to come. One Kotlin Multiplatform core holds every decision about workouts; the apps only render and talk to hardware. The README maps the modules and how to run each part.

## Read before you work

- `CONTEXT.md`: the glossary. Name things with its terms in code, issues, tests and UI copy.
- `docs/adr/`: decisions that look odd without their reason. Read the ones touching your area; say so explicitly if your change contradicts one.
- `docs/architecture.md`: the overview, for anything spanning modules.
- `docs/design-system.md` and `docs/brand.md`: before any screen, colour, copy or Poulpy change.
- `CONTRIBUTING.md`: the issue-to-PR flow and labels.
- `docs/agents/roles.md`: the long-running role threads (Architect, Designer, Poulpy keeper, Translator, Maintainer), what each owns, and when to consult them and hand back.

## Prove a change works

```sh
./gradlew jvmTest jsTest :server:test :tools:i18n:test   # shared logic, server and the string generator
node design/build-tokens.mjs --check    # theme files match design/tokens.json
node design/build-poulpy.mjs --check    # Poulpy drawings match their generator
```

Android and iOS only build in CI (`.github/workflows/ci.yml`); a PR is done when CI is green. Bluetooth, sound, haptics, background running and installs need a real phone: label the issue `device-test` and say what to check.

## House rules

- Logic goes in shared Kotlin with a test; Compose and SwiftUI code stays thin and mirrors the other platform.
- Theme files and Poulpy drawings are generated: edit `design/tokens.json` or `design/build-poulpy.mjs` and regenerate.
- Mockups are plain HTML in `design/mockups/` (ADR 0016), styled only with `var(--cp-*)` from the token CSS, `shared/mockup.css` and `design/poulpy/*.svg`. A new screen or redesign starts in `drafts/`; nothing is built until a PR moves it to `agreed/`. The claude.ai canvas is scratch only.
- Every user-facing string is added to `i18n/messages/en` and `i18n/messages/fr` (`app.json` for screens, `domain.json` for shared words, `catalog.json` for exercise text). Android, iOS and `DomainText` are generated from them at build time; never write a native string file. See `i18n/README.md`.
- Catalog slugs never change; set `"retired": true` instead of deleting, and raise `version`.
- Poulpy is never guilt: pain or rehab always shows the blue side, and errors are crimson with an icon, never ember.
- A schema change is a new SQLDelight migration; existing phones keep their data.
- The docs site is the source of truth. The Docs site workflow comments each PR's preview and the docs pages, sections and mockups to review (`site/review-links.mjs`); never post a review comment of your own. Put anything the reviewer must know in the PR description, and say `Closes #n` so the mockups drawn for that issue are linked.
- Branches are named `<type>/<issue-number>-<short-description>` in English (`feat/21-training-plans`, or `docs/brand-decisions` with no issue); types `feat`, `fix`, `docs`, `chore`, `design`. Never a random suffix, session id or `claude/` prefix, even if a tool suggests one.

## Agent skills

### Issue tracker

Issues live in GitHub Issues on `tkint/coach-poulpy`. See `docs/agents/issue-tracker.md`.

### Triage labels

The default five triage roles, label names unchanged. See `docs/agents/triage-labels.md`.

### Domain docs

Single context: `CONTEXT.md` and `docs/adr/` at the root. See `docs/agents/domain.md`.
