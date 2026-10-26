# Role threads

Five long-running threads in the Claude project each own a part of the repo. Short threads (a spec, a ticket) consult them instead of guessing, and hand back what they changed. A role's memory is its files, not its thread: every decision a role makes lands in the files it owns or in a GitHub issue, so a role thread that loses its context rebuilds it by rereading them.

## The roles

| Role | Owns | Consult it when |
| --- | --- | --- |
| **Architect** | `CONTEXT.md`, `docs/adr/`, `docs/architecture.md`, SQLDelight migrations, `build-logic`, the `area:shared` and `area:server` specs | A change touches the model, the engine or the database schema; needs a word the glossary lacks; adds a module; or contradicts an ADR |
| **Designer** | `docs/design-system.md`, `design/tokens.json`, the mockups, the English screen copy | A screen is added or changed, a ticket has no mockup, or Android and iOS disagree |
| **Poulpy keeper** | `docs/brand.md`, `docs/brand-decisions.md`, the brand book, `design/build-poulpy.mjs`, the rules behind `PoulpyCoach` (moods, sides, tentacles), Poulpy's voice | A change affects when Poulpy reacts, what lights a tentacle or what he says; adds a reward, a notification or a pose; or touches pain and rehab |
| **Translator** | Every language other than the English source, the per-language glossaries, the translation files and their generator, the proofreading tool | A change adds or edits user-facing text, or a language is added |
| **Maintainer** | `// CLEANUP:` markers, CI (`.github/workflows/`), `gradle/libs.versions.toml`, `.github/labels.yml`, the `device-test` checklists, triage | A change leaves a `// CLEANUP:` marker or a stopgap, CI is red on `main`, or a dependency needs a bump |

Roles advise; they don't overrule each other or the owner. A disagreement between roles, or anything that changes what is being built, goes to the owner as one decision.

## How a short thread works with them

1. **At spec time,** the spec thread asks each role whose files the idea touches, with the idea in a few lines and one precise question. The answers go into the spec and its tickets (Decided, Mockups, the ADR to write), so a ticket rarely needs a role again.
2. **At ticket time,** an implementation thread asks a role only when its ticket is silent on that role's area, or when it would contradict a role's file. It sends the issue number and one question, and keeps working on the most likely answer while it waits.
3. **Decisions ship with the change.** An ADR, glossary entry or brand decision goes in the same PR as the change it explains: the role writes the wording, the implementation thread commits it. A decision made at spec time with no code yet can go in a docs PR the role opens itself.
4. **Hand back.** Once the PR is up, the implementation thread sends a role the PR link if it touched that role's files or departed from what was decided, and the role reviews those files. Every new `// CLEANUP:` marker goes to the Maintainer with the PR link.

## Translations

Every surface (Android, iOS, the website, the server's API, the exercise catalog) will read its text from one JSON file per language and per namespace, with ICU MessageFormat for plurals and placeholders. Native speakers proofread in [Fink](https://fink.inlang.com/), which opens pull requests from a free GitHub account. A build-time generator writes the Android and iOS string files from the JSON; they are not committed. The server words only what it sends itself (emails, notifications, exports) and returns errors as codes the clients translate.

Until that move is done, the house rule stands: every string exists in English and French in Android `values` and `values-fr`, iOS `en.lproj` and `fr.lproj`, and the shared `Labels`.
