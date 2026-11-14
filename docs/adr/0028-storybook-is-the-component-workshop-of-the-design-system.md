---
status: accepted
date: 2026-11-14
---

# Storybook is the component workshop of the design system

The Vue components of `web/ui` ([ADR 0023](./0023-the-websites-share-one-vue-and-daisyui-design-system.md)) each have stories in `web/storybook`: one story per state, with the checks that state must keep. The docs site publishes the built Storybook under `/storybook/` and embeds stories on its [Component workshop](https://tkint.github.io/coach-poulpy/component-workshop) page, so the docs site stays the one place the owner reviews.

## What it means

- **One story file per component**, in `web/storybook/stories/Cp*.stories.ts`: a story for each state a person can reach (closed and open for a select, a menu or a sheet; empty, filled, hinted, invalid and disabled for a field). A new or changed component adds or updates its stories in the same PR.
- **A `play` function per behaviour**, run as a test in Chromium by `npm test -w storybook` (Vitest and the Storybook addon). A popover that opens on page load, an Escape that does not close a panel or a disabled button that still works fails CI.
- **Every story is also checked for accessibility** (axe, through `@storybook/addon-a11y`), in light and dark. A finding fails CI. A story that cannot pass yet is marked `a11y: { test: 'todo' }` with a `// CLEANUP:` comment that names the fix.
- **Docs pages come from the stories.** Every component has an autodocs page that lists all its stories one under the other, with their code.
- **The theme is the real one.** The workshop builds Tailwind, DaisyUI, `tokens.css` and `theme.css` as the web app does, with the stories as a source, and a toolbar switch sets `data-theme` to light or dark.
- **Agents read it through MCP.** `@storybook/addon-mcp` serves the running workshop at `http://localhost:6006/mcp`, declared in `.mcp.json`, so an agent can find the stories that render a component, preview them and run the story tests while it builds ([Storybook MCP](../agents/storybook-mcp.md)).
- **The hand-built samples stay for now.** The [Web components](https://tkint.github.io/coach-poulpy/web-components) page still holds each component's classes for web mockups, which stories cannot show. Its live samples may be dropped once the stories cover every state and the owner agrees.

## Why

- The Web components page was a hand-built gallery: nothing checked the states it showed, and popovers that opened on page load went unnoticed. A story with a `play` function checks that every time.
- Storybook supports Vue 3 and Vite directly, has the test and accessibility tooling in one place, and is widely maintained.
- [Histoire](https://histoire.dev) is lighter and Vue-only, but its testing and accessibility tooling is thinner, and it is updated less often.
- Storybook builds to static files, so the docs site publishes it like its other assets.

## Consequences

- `web/` gains the `web/storybook` workspace. Its dependencies are pinned to exact versions, like DaisyUI and Tailwind.
- The docs build runs `storybook build`, and the web job in CI runs the stories as tests, which needs Playwright's Chromium (`npx playwright install --with-deps chromium`).
- Locally, `CHROMIUM_PATH` points the tests at a Chromium already on the machine.
