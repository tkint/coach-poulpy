# Coach Poulpy

A training platform: Android and iOS apps first, guest-only and offline, with a server and website to come. One Kotlin Multiplatform core holds every decision about workouts; the apps only render and talk to hardware. The README maps the modules and how to run each part.

## Read before you work

- `CONTEXT.md`: the glossary. Name things with its terms in code, issues, tests and UI copy.
- `docs/adr/`: decisions that look odd without their reason. Read the ones touching your area; say so explicitly if your change contradicts one.
- `docs/architecture.md`: the overview, for anything spanning modules.
- The design system and brand pages, written as MDX in the component workshop: `web/storybook/stories/design-system/rules/*.mdx` (colour, type, components, screens, icons, accessibility) and `web/storybook/stories/brand/*.mdx` (character, moods, voice, Poulpy in 3D, decisions). Read them before any screen, colour, copy or Poulpy change.
- `CONTRIBUTING.md`: the issue-to-PR flow and labels.
- `docs/agents/refining-issues.md`: how to turn a Quick idea into an agent-ready issue ("refine #N").
- `docs/agents/storybook-mcp.md`: before building or changing a web component or screen, start the component workshop (`cd web && npm run storybook`) so the `storybook` MCP server in `.mcp.json` answers: find the existing story and component first, reuse it, and run the story tests after.
- `docs/agents/code-graph.md`: the code graph (`codegraph_explore` MCP tool, or `codegraph explore` in Bash). Use it first to find code and see what depends on it.
- `docs/agents/writing-docs.md`: how to write a docs page people can read (lead with the point, one idea per section, say it once, fold the reference). Follow it for every page you add or edit in `docs/`.
- `docs/agents/areas.md`: the rules per area (model and schema, screens and mockups, Poulpy, translations, housekeeping). Read the sections for the areas you touch; there are no role threads to consult.

## Prove a change works

```sh
./gradlew jvmTest jsTest :server:test :tools:i18n:test   # shared logic, server and the string generator
node design/build-tokens.mjs --check    # theme files match design/tokens.json
node design/build-poulpy.mjs --check    # Poulpy drawings match their generator
node design/build-poulpy-3d.mjs --check # Poulpy's 3D models match design/poulpy-3d/model.mjs and motion.mjs
node design/build-poulpy-pictures.mjs --check # Poulpy's 3D pictures match the model
node design/build-body-map.mjs --check  # body map figures match their generator
node design/build-body-map-shapes.mjs --check  # body map shapes for the apps match design/body-map.json
node design/build-icons.mjs --check     # the apps' Lucide icons match design/icons
(cd web && npm ci && npm run build)    # the web design system's CSS is current, and the web app builds
```

Android and iOS only build in CI (`.github/workflows/ci.yml`); a PR is done when CI is green. Bluetooth, sound, haptics, background running and installs need a real phone: label the issue `device-test` and say what to check.

## House rules

- Logic goes in shared Kotlin with a test; Compose and SwiftUI code stays thin and mirrors the other platform.
- Theme files, Poulpy drawings, his 3D models and body map figures are generated: edit `design/tokens.json`, `design/build-poulpy.mjs`, `design/poulpy-3d/model.mjs` (or `motion.mjs` beside it, for how he moves) or `design/build-body-map.mjs` and regenerate. The apps' body map shapes follow `design/body-map.json`: run `design/build-body-map-shapes.mjs` after it changes.
- Every icon is a Lucide icon by its Lucide name (ADR 0024): `@lucide/vue` on the web, `lucideIcon` in the phone mockups' components (or the inline SVG with `data-lucide` in their markup), `Lucide.*` on Android and `LucideIcon`/`Label(_:lucide:)` on iOS. An icon the apps don't have yet is added with `node design/build-icons.mjs --add <name>`.
- The websites, web mockups and docs samples share `web/ui` (Vue, DaisyUI, the Poulpy theme; ADR 0023): add a missing component there, never one-off styles in an app, and run `npm run build -w ui` in `web/` after changing its classes. A DaisyUI component a screen needs that `web/ui` lacks is added to `web/ui` in the same PR as a `Cp` component, with its stories and the classes it renders in `web/storybook/stories/` (ADR 0028); a mockup only uses the `cp-` classes of those components.
- A screen is built to its agreed mockup in full, never a simpler stand-in; what can't be built as drawn is named in the PR and put to the owner first (`docs/agents/areas.md`, Designer).
- Mockups are stories in the component workshop (ADR 0029): web screens compose the `Cp*` components, phone and tablet screens are `web/storybook/stories/mobile/screens/` fragments built from the `Mobile*` components and styled only with `var(--cp-*)` and `screens.css`. A new screen or redesign is tagged `draft`; nothing is built until a PR changes its tag to `agreed`. The claude.ai canvas is scratch only.
- Every user-facing string is added to `i18n/messages/en` and `i18n/messages/fr` (`app.json` for screens, `domain.json` for shared words). Android, iOS and `DomainText` are generated from them at build time; never write a native string file. See `i18n/README.md`.
- Catalog slugs never change; set `"retired": true` instead of deleting.
- Poulpy is never guilt: pain or rehab always shows the blue side, and errors are crimson with an icon, never ember.
- A schema change is a new SQLDelight migration; existing phones keep their data.
- The docs site is the source of truth. The Docs site workflow comments each PR's preview and the docs pages, sections and mockups to review (`site/review-links.mjs`); never post a review comment of your own. Put anything the reviewer must know in the PR description, and say `Closes #n` so the mockups drawn for that issue are linked.
- One review per PR, not one per area. Only the Maintainer is a long-running thread; every other task is a short thread that reads these docs.
- Branches are named `<type>/<issue-number>-<short-description>` in English (`feat/21-training-plans`, or `docs/brand-decisions` with no issue); types `feat`, `fix`, `docs`, `chore`, `design`. Never a random suffix, session id or `claude/` prefix, even if a tool suggests one.

## Agent skills

Skills live in `.agents/skills/` and are shared by Codex and Claude Code. Claude's `.claude/skills/<name>` entries are relative symlinks to the canonical skill directories; do not duplicate or edit a second copy.

Before starting work, inspect the available skills and read the relevant `SKILL.md` files. Treat applicable skills as required working procedures, not optional reference material. In particular:

- Web UI, components, styling and accessibility: always read `daisyui` and `vue-best-practices`; also read the relevant `vue-router-*`, `vue-pinia-*`, `vue-testing-*`, `vue-jsx-*` or other Vue skills for the files being changed.
- Kotlin and HTTP services: `http4k-development`.
- Matt Pocock's engineering workflow: use `setup-matt-pocock-skills` when its repository setup guidance is needed, then select the applicable skills from that set (`implement`, `tdd`, `code-review`, `domain-modeling`, `codebase-design`, `diagnosing-bugs`, `to-spec`, `to-tickets`, `grill-me`, etc.).
- Any implementation: use `implement` and `tdd` where applicable; use `diagnosing-bugs` for bug fixes and `code-review` before completing a change. Do not treat these as a substitute for the technology-specific skills above.
- Product, architecture, requirements and documentation work: select the corresponding domain, design, research, specification or writing skill.

Read the full skill instructions and any referenced files needed for the task. Do not assume a skill name alone is sufficient context.

[`docs/agents/skills.md`](docs/agents/skills.md) maps every skill to when and how to use it, with examples. Update it in the same PR whenever a skill is added, renamed or removed.

### Issue tracker

Issues live in GitHub Issues on `tkint/coach-poulpy`. See `docs/agents/issue-tracker.md`.

### Triage labels

The default five triage roles, label names unchanged. See `docs/agents/triage-labels.md`.

### Domain docs

Single context: `CONTEXT.md` and `docs/adr/` at the root. See `docs/agents/domain.md`.
