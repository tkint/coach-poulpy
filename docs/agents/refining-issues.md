# Refining an issue

How an agent turns a **Quick idea** (a few words, label `needs-triage`) into an agent-ready issue. It runs by hand: the maintainer says "refine #N" to an agent session. Nothing runs automatically when an idea is opened.

An issue written with the Feature or Bug form is already structured; refine it the same way when something is missing, and leave the parts that are fine.

## Steps

1. **Read the issue and its comments.** The maintainer's words are the requirement; do not widen them.
2. **Read the context.** `CONTEXT.md` for the terms, the ADRs and docs of the area (`docs/agents/areas.md` says which), and the code and mockups the idea touches. Use the terms of `CONTEXT.md` in the rewritten issue.
3. **Check the house rules** from `CLAUDE.md` that the idea implies:
   - every user-facing string in English and French;
   - a new screen or redesign starts as a mockup in `design/mockups/drafts/`;
   - logic goes in shared Kotlin with a test;
   - a schema change is a migration, and existing phones keep their data;
   - Poulpy is never guilt (blue side for pain or rehab, crimson with an icon for errors).
4. **Rewrite the issue** (title and body) to the Agent brief shape below.
5. **Set the labels and milestone**, then comment with what you changed and, if you set `needs-info`, the questions.

## Agent brief shape

```md
## Agent brief

### What and why
What should be possible, from the person's point of view, and why.

### Decided
Choices already made, so the implementing agent does not reopen them.

### Mockups
Existing mockups to follow, or "a draft comes first" when a screen is new.

### Acceptance criteria
- [ ] One checkable line each.

### Out of scope
What this issue deliberately does not cover.

### Device test
Only when it needs a phone or treadmill: what to check.
```

Keep it short. An issue that takes more than one PR is split, with each part a sub-issue.

## Labels and milestone

Only labels and milestones listed in `.github/labels.yml` exist; never invent one.

- **One type:** `bug`, `enhancement` or `documentation`.
- **One state:** see below.
- **At least one area:** `area:*`.
- **`device-test`** when the *Device test* section exists.
- **One milestone**, the phase it belongs to.

| State | When |
| --- | --- |
| `ready-for-agent` | Every section above is filled and nothing is left to decide |
| `needs-info` | A decision is missing |
| `ready-for-human` | Only a person can do it: a purchase, a test by hand |
| `wontfix` | It contradicts an ADR or the maintainer's stated direction; say which |

Remove `needs-triage` once another state is set.

## When to use `needs-info`

Do not guess a decision that changes the product, the data or the cost. Set `needs-info`, and put a **Decision needed** section in the issue with explicit questions, each with the options and the default you would take if no answer comes. Then stop; the maintainer answers in a comment and the issue is refined again.

A detail an agent can settle by reading the code or the docs is not a question.
