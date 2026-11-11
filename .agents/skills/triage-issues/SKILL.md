---
name: triage-issues
description: Triage Coach Poulpy issues using the repository's canonical states, labels and refinement workflow.
---

# Triage issues

Read [`docs/agents/issue-tracker.md`](../../../docs/agents/issue-tracker.md), [`docs/agents/triage-labels.md`](../../../docs/agents/triage-labels.md) and [`docs/agents/refining-issues.md`](../../../docs/agents/refining-issues.md).

- Use only labels and milestones declared in `.github/labels.yml`.
- Preserve the reporter's intent. Do not turn a small request into a larger project.
- Distinguish missing information from details discoverable in the repository.
- Use `needs-info` for unresolved decisions that materially affect product behavior, data or cost.
- Use `ready-for-agent` only when the brief is actionable and no consequential decision is outstanding.
- Use `ready-for-human` when the work requires a person, such as a physical-device test or purchase.
- Use `wontfix` only when it conflicts with a documented decision or maintainer direction; explain the conflict.
- Do not apply labels or close an issue unless the requested task authorizes that action.
