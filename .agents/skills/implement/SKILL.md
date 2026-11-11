---
name: implement
description: Implement a well-scoped Coach Poulpy issue or approved change, following repository architecture, tests, UI conventions and verification requirements.
---

# Implement a Coach Poulpy change

1. Read the issue and all comments. Treat the maintainer's explicit requirements as authoritative; do not widen scope.
2. Read `AGENTS.md`, `CLAUDE.md`, the relevant section of `docs/agents/areas.md`, `CONTEXT.md`, relevant ADRs and architecture/design documentation.
3. Inspect available specialist skills and load every skill required by the touched areas according to `AGENTS.md`. In particular:
   - Use Matt Pocock's `tdd` for behavior changes, `diagnosing-bugs` for bug fixes, `codebase-design` for module-boundary decisions and `code-review` for the final PR review.
   - Use daisyUI's `daisyui` and Vue's `vue-best-practices` (plus the matching router, Pinia, testing or debugging skill) for web UI work.
   - Use http4k's `http4k-development` for server work.
   - If a required skill is missing, stop and report it; do not silently claim or simulate that it was used.
4. Inspect the current implementation, callers, tests, mockups and generated-source workflow. Use the CodeGraph skill if available.
5. Before editing, identify:
   - expected behavior and acceptance criteria;
   - modules and files likely affected;
   - compatibility, migration, authorization and translation implications;
   - the smallest complete implementation.
6. If a consequential product or data decision is unresolved, stop and ask one focused question with a recommended default. Do not guess.
7. Implement the change using established patterns. Keep domain logic in shared Kotlin, use shared Vue/DaisyUI components, follow http4k route patterns and keep OpenAPI aligned with actual behavior.
8. Add or update regression tests. Update source translation files and regenerate generated outputs only through their documented generators.
9. Run focused tests first, then the relevant checks from `CLAUDE.md`. Inspect failures and fix regressions caused by the change.
10. Review the full diff. Check that no unrelated or generated files were changed accidentally.
11. Report changed behavior, tests and checks with exact results, CI status, and any unverified limitations.

Do not claim completion while required checks are still running or failing.
