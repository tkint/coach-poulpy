# Storybook MCP

The component workshop ([ADR 0028](../adr/0028-storybook-is-the-component-workshop-of-the-design-system.md)) serves an MCP server through `@storybook/addon-mcp`, declared in `.mcp.json` as `storybook` (`http://localhost:6006/mcp`). It lets an agent check what the design system already has while it builds a web screen, instead of drawing a simpler control of its own.

## Use it

1. Start the workshop: `cd web && npm ci && npm run storybook`. The server only exists while it runs; with it stopped, the tools are unavailable and the stories can be read in `web/storybook/stories/`.
2. Before writing or changing a web component or screen, call `get-storybook-story-instructions`, then `stories-find-by-component` to find the stories that render the component you touch. Reuse an existing `Cp*` component; a missing one is added to `web/ui` ([areas](areas.md)), never drawn in the app.
3. After a change that alters how the UI looks, call `test-run` for the stories you touched and `stories-preview` to look at them. A new state is a new story with a `play` function.

## What it does not do

- It does not judge visual quality. The agreed mockup is the contract (see [CLAUDE.md](../../CLAUDE.md)): compare the screen with it, light and dark.
- It sees the Vue components of `web/ui` only, not the Android and iOS screens.
- It is young: if a tool misbehaves, fall back to reading the stories and `npm test -w storybook`, and note the problem in the PR.
