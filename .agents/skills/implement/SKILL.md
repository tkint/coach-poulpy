---
name: implement
description: Implement a well-scoped Coach Poulpy issue or approved change, following repository architecture, tests, UI conventions and verification requirements.
---

# Implement a Coach Poulpy change

1. Read the issue and all comments. Treat the maintainer's explicit requirements as authoritative; do not widen scope.
2. Read `AGENTS.md`, `CLAUDE.md`, the relevant section of `docs/agents/areas.md`, `CONTEXT.md`, relevant ADRs and architecture/design documentation.
3. Inspect the current implementation, callers, tests, mockups and generated-source workflow. Use the CodeGraph skill if available.
4. Before editing, identify:
   - expected behavior and acceptance criteria;
   - modules and files likely affected;
   - compatibility, migration, authorization and translation implications;
   - the smallest complete implementation.
5. If a consequential product or data decision is unresolved, stop and ask one focused question with a recommended default. Do not guess.
6. Implement the change using established patterns. Keep domain logic in shared Kotlin, use shared Vue/DaisyUI components, follow http4k route patterns and keep OpenAPI aligned with actual behavior.
7. Add or update regression tests. Update source translation files and regenerate generated outputs only through their documented generators.
8. Run focused tests first, then the relevant checks from `CLAUDE.md`. Inspect failures and fix regressions caused by the change.
9. Review the full diff. Check that no unrelated or generated files were changed accidentally.
10. Report changed behavior, tests and checks with exact results, CI status, and any unverified limitations.

Do not claim completion while required checks are still running or failing.
