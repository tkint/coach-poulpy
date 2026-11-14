# Agent skills

A skill is a short set of instructions an agent follows for one kind of task: refining an issue, writing a test first, diagnosing a bug. The skills for this repo live in `.agents/skills/`, one folder per skill, and both Codex and Claude Code read them. This page is the map: which skill to use when, and an example of asking for it.

This page is updated in the same PR whenever a skill is added, renamed or removed. A skill that is missing here is not finished.

## How a skill gets used

There are two ways a skill runs.

- **Chosen by the agent.** The skill's description tells the agent when it fits, and the agent loads it on its own. Most skills work this way. You can also name one ("use the tdd skill").
- **Command only.** The skill's frontmatter sets `disable-model-invocation: true`, so the agent never picks it up by itself. You start it by name, with `/name` in Claude Code (for example `/grill-me`). Use this for steps that should run only when you ask, such as publishing a spec or triaging the tracker.

An agent that has already loaded a skill follows its instructions and does not need to be reminded. Skills that the agent should always read before a kind of work are listed in `CLAUDE.md` under "Agent skills".

## Start here: the issue-to-PR flow

Most work follows the path in [CONTRIBUTING](../../CONTRIBUTING.md). The skills map onto it like this:

| Step | Skill | Example |
| --- | --- | --- |
| A rough idea needs to become an agent-ready issue | `refine-issue` | "refine #42" |
| Issues need a state and labels | `triage-issues` (or `triage`, command only) | "triage the open issues" |
| The conversation becomes a spec on the tracker | `to-spec` (command only) | `/to-spec` |
| The spec is cut into tickets | `to-tickets` (command only) | `/to-tickets` |
| The work is built | `implement` or `implement-spec` (command only), or `coach-poulpy-implement` | `/implement-spec` |
| The change is reviewed before the PR | `code-review` | "review since main" |
| The PR body is written | `pr` | "write the PR body" |
| The issue tracker is read or changed | `issue-tracker` | "list the open issues about exercises" |

## Skill reference

### Workflow and tracker

| Skill | Use it when | Example |
| --- | --- | --- |
| `issue-tracker` | Creating, reading, refining or updating issues and PRs on GitHub | "open an issue for the rest timer" |
| `refine-issue` | A rough issue needs a brief an agent can build from, without widening it | "refine #58" |
| `triage-issues` | Moving issues through the repo's states and labels | "triage the new issues" |
| `triage` (command only) | The same triage, as a state machine with roles and briefs | `/triage` |
| `to-spec` (command only) | Turning what was just discussed into a spec, with no interview | `/to-spec` |
| `to-tickets` (command only) | Splitting a plan or spec into tracer-bullet tickets that block each other | `/to-tickets` |
| `to-questionnaire` (command only) | A decision cannot be settled by an agent, so someone else must answer | `/to-questionnaire` |
| `wayfinder` (command only) | A huge piece of work, too big for one session, needs a shared map of decisions | `/wayfinder` |
| `pr` | Writing a pull request body from the template | "write the PR body" |
| `setup-matt-pocock-skills` (command only) | Once per repo: set the tracker, label vocabulary and domain doc layout | `/setup-matt-pocock-skills` |

### Building and checking code

| Skill | Use it when | Example |
| --- | --- | --- |
| `coach-poulpy-implement` | Building a well-scoped issue or approved change in this repo, with its tests and checks | "implement #61" |
| `implement` (command only) | Building work from a spec or tickets, with TDD at agreed seams | `/implement` |
| `implement-spec` (command only) | Building what `to-spec` and `to-tickets` produced | `/implement-spec` |
| `tdd` | Writing a feature or fix test first, red then green | "fix the rest timer test-first" |
| `diagnosing-bugs` | Something is broken, throwing, failing or slow, and the cause is unknown | "debug why the set counter resets" |
| `code-review` | Checking a change against the repo's standards and the issue it answers | "review this branch against #61" |
| `code-graph` | Finding what a symbol touches before editing it | "what calls `WorkoutEngine.next`?" |
| `prototype` | A design or state question needs a throwaway experiment | "prototype the interval state model" |
| `wizard` (command only) | A human must walk through steps only they can do, such as adding a secret | "make a wizard for the Play Console key" |

### Understanding the domain and architecture

| Skill | Use it when | Example |
| --- | --- | --- |
| `area-guidance` | A task touches an area and you need its rules (model, screens, Poulpy, translations) | "what rules apply to the translations?" |
| `domain-context` | Before proposing a change to domain behaviour, to use the glossary's words | "is 'set' the right word here?" |
| `domain-modeling` | Writing or editing `CONTEXT.md` or an ADR | "record this decision as an ADR" |
| `codebase-design` | Designing a module's interface or deciding where a seam goes | "where should the test seam for the engine go?" |
| `improve-codebase-architecture` (command only) | Scanning for modules that could be deeper, then grilling on one | `/improve-codebase-architecture` |
| `grilling` | Stress-testing a plan or idea with relentless questions | "grill me on this plan" |
| `grill-me` (command only) | The same interview, as a command | `/grill-me` |
| `grill-with-docs` (command only) | An interview that also writes ADRs and glossary entries as it goes | `/grill-with-docs` |
| `research` | Gathering facts from primary sources into a Markdown file in the repo | "research the Wear OS heart-rate API" |

### Sessions, hand-offs and writing

| Skill | Use it when | Example |
| --- | --- | --- |
| `handoff` (command only) | Packing the conversation into a document for a fresh agent | `/handoff the next session writes the server tests` |
| `claude-handoff` (command only) | Handing the conversation to a background agent that starts at once | `/claude-handoff` |
| `chief-of-staff` (command only) | One long goal, coordinated across subagents in a single session | `/chief-of-staff` |
| `retro` (command only) | A retrospective on a finished session | `/retro` |
| `teach` (command only) | Learning a skill or concept inside this workspace | `/teach kotlin flows` |
| `loop-me` (command only) | Grilling on specs for a workflow to build | `/loop-me` |
| `wait-what` (command only) | A message did not land, so it is re-pitched | `/wait-what` |
| `writing-for-agents` | Creating or editing a skill, `AGENTS.md` or `CLAUDE.md` | "tighten the CLAUDE.md house rules" |

### Web, Vue and server code

| Skill | Use it when | Example |
| --- | --- | --- |
| `daisyui` | Writing any HTML or JSX with DaisyUI classes (always read it for web UI) | "add a card to the landing page" |
| `vue-best-practices` | Any Vue, `.vue`, Vue Router, Pinia or Vite work (always read it for web UI) | "add a route for the workout page" |
| `vue-router-best-practices` | Route params, navigation guards, route lifecycle | "guard the admin route" |
| `vue-pinia-best-practices` | A Pinia store, its state or its reactivity | "move the session state into a store" |
| `vue-testing-best-practices` | Vitest, Vue Test Utils or Playwright tests | "test the login form" |
| `vue-debug-guides` | A Vue runtime error, warning, async failure or hydration issue | "why does this component warn?" |
| `vue-jsx-best-practices` | JSX syntax inside Vue | "convert this to JSX" |
| `vue-options-api-best-practices` | Only when a file uses the Options API | "fix this options-API component" |
| `create-adaptable-composable` | Writing a composable that takes plain values, refs or getters | "make `useTimer` accept a ref" |
| `http4k-development` | Any http4k module on the server | "add a route to the sync server" |

## What to do when a skill is missing

If a skill you expect is not listed by your agent, check two things. First, is it on this page? Second, is it committed under `.agents/skills/` with a `SKILL.md` that has a `name` matching its folder? A skill that is committed but still missing from a running session usually means the session started before it was added: start a new session.

## Adding or changing a skill

1. Create the folder under `.agents/skills/<name>/` with a `SKILL.md`. Do not edit the copies under `.claude/skills/`; they are symlinks.
2. Give it a `description` that says when to use it. Add `disable-model-invocation: true` only when the skill should run solely on request.
3. Add a row to the table above, in the group that fits, with a short example. Update this page in the same PR.
4. Run the skill once in a fresh session to check it loads.
