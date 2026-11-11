---
name: area-guidance
description: Load the correct Coach Poulpy implementation and design rules for the areas touched by a task.
---

# Area guidance

Use [`docs/agents/areas.md`](../../../docs/agents/areas.md) as the detailed source.

Select every relevant area before editing:

- Model, workout engine, persistence or schema: Architect guidance; read `CONTEXT.md`, architecture and relevant ADRs.
- Screens, mockups, components or navigation: Designer guidance; read the design system and relevant agreed mockups.
- Mascot, rewards, notifications or illustrations: Poulpy keeper guidance; read brand rules and brand decisions.
- User-facing text: Translator guidance; read `i18n/README.md`, message namespaces and glossaries.
- CI, build logic, labels, cleanup markers or device verification: Maintainer guidance.

When a task crosses areas, apply all relevant rules. There is one review per PR, not separate reviews per area. Ask the maintainer only when the documentation and implementation do not settle a consequential decision.
