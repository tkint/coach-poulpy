---
name: refine-issue
description: Turn a rough Coach Poulpy GitHub issue into a concise, implementation-ready brief without widening the request.
---

# Refine an issue

Follow [`docs/agents/refining-issues.md`](../../../docs/agents/refining-issues.md).

1. Read the issue, comments, labels and milestone.
2. Treat the maintainer's wording as the requirement. Do not widen it.
3. Read `CONTEXT.md`, relevant ADRs, area guidance, implementation and mockups.
4. Resolve details from repository evidence. Do not ask the maintainer questions that code or docs can answer.
5. Do not guess consequential product, data or cost decisions. Use `needs-info` and list explicit options plus a recommended default.
6. Shape the brief with: What and why; Decided; Mockups; checkable acceptance criteria; Out of scope; Device test when required.
7. Use only labels and milestones defined in `.github/labels.yml`. Apply one type, one state and at least one area label.
8. Keep the issue short. Split work that cannot reasonably fit in one PR.
9. Report what was clarified, what remains undecided and any label/milestone changes.
