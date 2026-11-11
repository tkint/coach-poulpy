# Agent instructions

This file is the entry point for Codex and other agents that follow `AGENTS.md`. Read it before changing the repository.

## Project guidance

1. Read [`CLAUDE.md`](CLAUDE.md) for the shared project rules and required checks. It remains the canonical detailed guide; do not duplicate it here.
2. Read [`CONTEXT.md`](CONTEXT.md) and the relevant ADRs in `docs/adr/` before changing domain concepts, data, APIs or architecture.
3. For cross-module work, read [`docs/architecture.md`](docs/architecture.md).
4. For UI, copy, colors, icons or mascot work, read [`docs/design-system.md`](docs/design-system.md), [`docs/brand.md`](docs/brand.md) and the relevant mockups.
5. Read the relevant area guidance in [`docs/agents/areas.md`](docs/agents/areas.md). The other guides in `docs/agents/` cover code-graph exploration, domain context, issue refinement, issue tracking and triage labels.
6. Follow [`CONTRIBUTING.md`](CONTRIBUTING.md) for the issue-to-PR workflow.

## Mandatory specialist skills

Before implementing or reviewing code, inspect the skills available to the current agent. Load and follow the applicable specialist skills below; these are required workflow dependencies, not optional references. Do not claim to have used a skill unless its `SKILL.md` was actually loaded.

| Work area | Required specialist skills |
| --- | --- |
| Any behavior change or feature implementation | Matt Pocock's `tdd` |
| Any bug diagnosis or bug fix | Matt Pocock's `diagnosing-bugs` |
| Module boundaries, abstractions or architectural changes | Matt Pocock's `codebase-design` |
| Final review of every code PR | Matt Pocock's `code-review` |
| Any Vue application or component work | `vue-best-practices`; also `vue-router-best-practices`, `vue-pinia-best-practices`, `vue-testing-best-practices` or `vue-debug-guides` when the task touches those areas |
| Any DaisyUI/Tailwind component, theme or styling work | daisyUI's `daisyui` |
| Any http4k server, route, filter, contract or test work | http4k's `http4k-development` |

Install missing skills into the current repository before starting implementation. Use the Skills CLI and install for the current agent (or all agents used by the team):

```sh
npx skills@latest add saadeghi/daisyui --skill daisyui
npx skills@latest add vuejs-ai/skills --skill vue-best-practices
npx skills@latest add vuejs-ai/skills --skill vue-router-best-practices
npx skills@latest add vuejs-ai/skills --skill vue-pinia-best-practices
npx skills@latest add vuejs-ai/skills --skill vue-testing-best-practices
npx skills@latest add vuejs-ai/skills --skill vue-debug-guides
npx skills@latest add https://github.com/http4k/agent-skills/tree/master/plugins/http4k/skills/http4k-development
npx skills@latest add mattpocock/skills --skill tdd
npx skills@latest add mattpocock/skills --skill diagnosing-bugs
npx skills@latest add mattpocock/skills --skill codebase-design
npx skills@latest add mattpocock/skills --skill code-review
```

The Vue skills repository is explicitly an early community experiment; use its guidance as specialist advice, but verify it against the installed Vue version and this repository's architecture. The local project rules, actual source, tests and ADRs remain authoritative when generic skill advice conflicts. If a required skill cannot be installed or loaded, stop and report the missing skill instead of silently substituting model knowledge.

## Engineering workflow

- **Inspect before editing.** Read the issue and its comments, inspect the current implementation, nearby tests, callers, generated sources and relevant history. Use CodeGraph when available; still read the source files you intend to change.
- **Verify, do not infer.** Treat implementation, tests, ADRs and current documentation as evidence. Do not invent routes, API behavior, data models, UI conventions or infrastructure.
- **Plan proportionately.** For cross-cutting or consequential changes, identify affected modules, compatibility risks and the smallest complete approach before editing. Ask one focused question if an unresolved decision materially changes the result.
- **Keep scope tight.** Prefer the smallest complete change. Avoid unrelated refactors, speculative abstractions, generated-file edits and duplicated sources of truth.
- **Preserve decisions.** Do not silently reverse an ADR or an explicit maintainer decision. If repository text is stale or contradictory, call it out rather than treating it as authoritative.
- **Test behavior.** Add or update focused tests for behavior changes. Do not weaken or delete tests to make a change pass.
- **Review the diff.** Check the complete diff for accidental changes, naming consistency, accessibility, responsive behavior, translations and compatibility.
- **Report evidence.** State exactly which checks ran and their results. Never claim a test, build, CI job, deployment or GitHub action succeeded without checking its result. Clearly identify anything not run or still uncertain.

## Technology-specific expectations

- **Vue.js:** use the Composition API and existing component patterns; keep components cohesive and state ownership explicit.
- **DaisyUI and Tailwind CSS:** use the project's shared `web/ui` components, `cp-` classes and design tokens. Do not add one-off styling or bypass shared components without a demonstrated need.
- **http4k:** follow the existing routing, filter, validation, error-handling and testing patterns. Keep transport concerns out of domain logic.
- **OpenAPI:** keep the contract aligned with implemented routes, schemas, authentication and error responses. Do not document speculative endpoints or silently change compatibility.
- **Kotlin Multiplatform:** keep domain/workout logic in shared Kotlin with tests; platform UI and hardware integration remain thin and follow the documented architecture.
- **Translations:** user-facing strings belong in the source message files for every supported language. Do not edit generated translations or native string resources directly.

## Explicit decision: per-exercise revisioning, not catalog versioning

The maintainer has rejected catalog-level versioning. The intended model is immutable revision history per exercise: a publication changes the revision of the exercise being published, not a global catalog snapshot or counter.

Issue [#400](https://github.com/tkint/coach-poulpy/issues/400) is the dedicated removal and migration task. It covers the active implementation, API/client update contract, database migration, CI, documentation and historical ADR annotations. Until that work is implemented, legacy catalog-version fields and behavior still exist; never describe them as already removed or design new features around them.

When working on catalog code or documentation, follow #400 and avoid extending the legacy model. Historical ADRs should be marked superseded as part of that dedicated change, not silently rewritten during unrelated work.

## Git and pull requests

- **Never work directly on `main`.** Do not commit to, push to, or otherwise update the `main` branch directly.
- **Use a dedicated feature branch for every change**, created from the current `main` unless the task explicitly depends on another base branch. Follow the branch naming convention in `CLAUDE.md`.
- **Commit autonomously on feature branches when a coherent unit of work is ready.** Do not wait for explicit commit authorization.
- **Minimize commits.** Prefer one well-organized commit per task or coherent change. Avoid incremental, checkpoint, cosmetic or fixup commits; split only when changes are genuinely independent or the user asks for separate commits.
- Push feature branches as needed to share work or open a pull request, but never push to `main`.
- Never force-push or rewrite shared branch history unless explicitly authorized. Do not discard user changes.
- When the work is ready for integration, use a pull request targeting `main`; never bypass the pull-request workflow.
- Do not merge or close issues/PRs unless the user explicitly requests it.
- Keep one coherent goal per PR and link it with `Closes #<number>` when applicable.
- Put review context and known limitations in the PR description.
- A task is not complete merely because edits were made: relevant local checks and required CI must be verified.
