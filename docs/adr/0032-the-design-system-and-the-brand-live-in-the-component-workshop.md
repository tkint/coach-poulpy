---
status: accepted
date: 2026-11-15
---

# The design system and the brand live in the component workshop

The component workshop ([ADR 0028](./0028-storybook-is-the-component-workshop-of-the-design-system.md)) is the home of the whole design system and of the brand: the design system rules, the tokens, the components, the screens ([ADR 0029](./0029-mockups-are-stories-in-the-component-workshop.md)), Poulpy's rules and moods, his drawings, his 3D model, the brand book and the brand decisions. This amends ADR 0029, which kept the brand on the docs site.

The owner chose this on 15 November 2026, once every mockup was a story, so that a person finds everything about how Coach Poulpy looks and feels in one place, beside the components that draw it.

## What it means

- **The rules are MDX pages in the workshop.** The design system rules (`web/storybook/stories/design-system/rules/`) and the brand (`web/storybook/stories/brand/`: character, moods, voice, Poulpy in 3D, his moves, decisions, ideas) are written directly in the pages that show them, one page per topic, with the live parts beside the text. A PR changes those files, and agents read them there. They were first Markdown files cut into pages at build time; the owner chose on 15 November 2026 to keep one copy, in the pages, so nothing is shown twice. The addon-docs MDX build uses `remark-gfm` for tables.
- **The live parts move into the workshop.** The 3D preview (`Poulpy3d.vue`, three.js) and the brand book (`brand-book/`) are under `web/storybook/stories/brand/`; the drawings page shows the Core > Poulpy story.
- **The docs site links to the workshop** from its top menu, its home page and its Design system and Brand menus. It no longer publishes the rules and the brand as plain text (the owner decided this on 15 November 2026, amending the first version of this ADR): they live only in the workshop, so every guideline stays reachable from the docs site through its links to the workshop, and pages of the docs site link to the workshop pages. It keeps what is not design system or brand: architecture, decisions, specs, translations and the roadmap.

## Why

- One place to look: a reviewer reads a rule next to the component or drawing it governs, in light and dark.
- One copy of each rule, in one place: the text is written in the page that shows it, and neither a Markdown file nor the docs site carries a second copy.
- The live parts are checked: the workshop builds and tests them with the rest of the stories.

## Consequences

- The docs site's Drawings, Poulpy in 3D and Brand book pages and their Vue components are gone; three.js is a dependency of the workshop instead of the site.
- The 3D model's glTF files are linked from the repo on GitHub rather than downloaded from the site.
