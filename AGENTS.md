# Agent instructions

This file is the entry point for Codex and other agents that follow `AGENTS.md`. Read it before changing the repository.

## Project guidance

1. Read [`CLAUDE.md`](CLAUDE.md) for the shared project rules and required checks. It remains the canonical detailed guide; do not duplicate it here.
2. Read [`CONTEXT.md`](CONTEXT.md) and the relevant ADRs in `docs/adr/` before changing domain concepts, data, APIs or architecture.
3. For cross-module work, read [`docs/architecture.md`](docs/architecture.md).
4. For UI, copy, colors, icons or mascot work, read [`docs/design-system.md`](docs/design-system.md), [`docs/brand.md`](docs/brand.md) and the relevant mockups.
5. Read the relevant area guidance in [`docs/agents/areas.md`](docs/agents/areas.md). The other guides in `docs/agents/` cover code-graph exploration, domain context, issue refinement, issue tracking and triage labels.
6. Follow [`CONTRIBUTING.md`](CONTRIBUTING.md) for the issue-to-PR workflow.

## Engineering workflow

- **Inspect before editing.** Read the issue and its comments, inspect the current implementation, nearby tests, callers, generated sources and relevant history. Use CodeGraph when available; still read the source files you intend to change.
- **Verify, do not infer.** Treat implementation, tests, ADRs and current documentation as evidence. Do not invent routes, API behavior, data models, UI conventions or infrastructure.
- **Plan proportionately.** For cross-cutting or consequential changes, identify affected modules, compatibility risks and the smallest complete approach before editing. Ask one focused question if an unresolved decision materially changes the result.
- **Keep scope tight.** Prefer the smallest complete change. Avoid unrelated refactors, speculative abstractions, duplicated sources of truth and generated-file edits.
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

- Use the branch naming convention in `CLAUDE.md`.
- Keep one coherent goal per PR and link it with `Closes #<number>` when applicable.
- Do not merge or close issues/PRs unless explicitly asked.
- Put review context and known limitations in the PR description.
- A task is not complete merely because edits were made: relevant local checks and required CI must be verified.
