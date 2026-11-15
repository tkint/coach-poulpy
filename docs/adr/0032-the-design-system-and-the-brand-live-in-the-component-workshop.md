---
status: accepted
date: 2026-11-15
---

# The design system and the brand live in the component workshop

The component workshop ([ADR 0028](./0028-storybook-is-the-component-workshop-of-the-design-system.md)) is the home of the whole design system and of the brand: the design system rules, the tokens, the components, the screens ([ADR 0029](./0029-mockups-are-stories-in-the-component-workshop.md)), Poulpy's rules and moods, his drawings, his 3D model, the brand book and the brand decisions. This amends ADR 0029, which kept the brand on the docs site.

The owner chose this on 15 November 2026, once every mockup was a story, so that a person finds everything about how Coach Poulpy looks and feels in one place, beside the components that draw it.

## What it means

- **The rules stay Markdown in the repo.** `docs/design-system.md`, `docs/brand.md`, `docs/brand-decisions.md` and `docs/poulpy-3d.md` are what agents read and what a PR changes. The workshop shows them one topic per page, cut at their second-level headings (`web/storybook/stories/markdown-pages.ts`), with the live parts beside them: the token swatches, the drawings, the turning 3D model and the brand book. A new section fails the workshop's build until it is given a page.
- **The live parts move into the workshop.** The 3D preview (`Poulpy3d.vue`, three.js) and the brand book (`brand-book/`) are under `web/storybook/stories/brand/`; the drawings page shows the Core > Poulpy story.
- **The docs site links to the workshop** from its top menu, its home page and its Design system and Brand menus. It still publishes the Markdown files, as plain text, so every guideline stays reachable from it and the links between docs keep working. It keeps what is not design system or brand: architecture, decisions, specs, translations and the roadmap.

## Why

- One place to look: a reviewer reads a rule next to the component or drawing it governs, in light and dark.
- One copy of each rule: the Markdown is the source, and the workshop and the site only show it.
- The live parts are checked: the workshop builds and tests them with the rest of the stories.

## Consequences

- The docs site's Drawings, Poulpy in 3D and Brand book pages and their Vue components are gone; three.js is a dependency of the workshop instead of the site.
- The 3D model's glTF files are linked from the repo on GitHub rather than downloaded from the site.
