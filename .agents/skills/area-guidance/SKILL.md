---
name: area-guidance
description: Load the correct Coach Poulpy implementation and design rules for the areas touched by a task.
---

# Area guidance

Use [`docs/agents/areas.md`](../../../docs/agents/areas.md) as the detailed source.

Select every relevant area before editing and load its mandatory specialist skills from `AGENTS.md`:

- Model, workout engine, persistence or schema: Architect guidance; read `CONTEXT.md`, architecture and relevant ADRs. Use Matt Pocock's `tdd` for behavior changes and `codebase-design` when changing module boundaries.
- Screens, mockups, components or navigation: Designer guidance; read the design system and relevant agreed mockups. Use daisyUI's `daisyui` and Vue's `vue-best-practices`; add the Vue Router, Pinia, testing or debugging skill when that subsystem is touched.
- Mascot, rewards, notifications or illustrations: Poulpy keeper guidance; read brand rules and brand decisions.
- User-facing text: Translator guidance; read `i18n/README.md`, message namespaces and glossaries.
- CI, build logic, labels, cleanup markers or device verification: Maintainer guidance.
- Server routes, filters, contracts, OpenAPI integration or server tests: use http4k's `http4k-development`.

For bugs, load Matt Pocock's `diagnosing-bugs`; before completing a code PR, load `code-review`. If any required specialist skill is not installed or cannot be loaded, stop and report the missing dependency.

When a task crosses areas, apply all relevant rules. There is one review per PR, not separate reviews per area. Ask the maintainer only when the documentation and implementation do not settle a consequential decision.
