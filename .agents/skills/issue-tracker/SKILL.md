---
name: issue-tracker
description: Create, inspect, refine and update Coach Poulpy GitHub issues and pull requests using repository conventions.
---

# Issue tracker

Follow [`docs/agents/issue-tracker.md`](../../../docs/agents/issue-tracker.md) and [`docs/agents/triage-labels.md`](../../../docs/agents/triage-labels.md).

- The repository is `tkint/coach-poulpy`.
- Read `.github/labels.yml` before applying labels or milestones. Never invent labels.
- New issues need one type label, one state label, at least one `area:` label and the milestone for their phase.
- Read issue comments before changing requirements or status.
- Use `Closes #<number>` in PR descriptions when the PR completes an issue.
- Distinguish issues from pull requests when resolving a GitHub number.
- Do not close issues, merge PRs, or change their state unless the user explicitly requests it.
- When creating or updating an issue, summarize the exact changes and link the resulting GitHub item.
