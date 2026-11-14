---
status: accepted
date: 2026-11-14
---

# Mockups are stories in the component workshop

Screen mockups are stories in the Storybook workshop ([ADR 0028](./0028-storybook-is-the-component-workshop-of-the-design-system.md)), and the workshop is the home of the whole design system documentation: rules, tokens, components and mockups. This replaces [ADR 0016](./0016-mockups-are-plain-html-in-the-repo.md), which kept mockups as plain HTML pages under `design/mockups/`, and amends [ADR 0023](./0023-the-websites-share-one-vue-and-daisyui-design-system.md), whose web mockups were plain HTML that wrote the components' classes.

The owner chose this on 14 November 2026, after the workshop showed it can hold written pages, live components and embedded content, and keeping the mockups apart meant describing the same components in two places.

## What it means

- **Components are the Vue components.** The stories of `web/ui` are the documentation of each component, its states and the classes it renders.
- **A web mockup is a story that composes the Vue components** into a full screen or part of a screen. It draws what the web app will render, because it is built from the same pieces. A component a web mockup needs and `web/ui` lacks is added to `web/ui` first, with its stories, as before.
- **A phone or tablet mockup is a story drawn with an isolated stylesheet** that reads the shared tokens (`--cp-*`) and nothing from the Vue components' CSS. It describes the Compose and SwiftUI screens, which DaisyUI's look does not match, as `mockup.css` did.
- **Draft and agreed are tags on the story**, not folders. A new screen or a redesign is tagged `draft`. Validating it is a PR that changes the tag to `agreed` and replaces the agreed story it redesigns; the owner's merge is the approval. Nothing is built from a `draft` story. The tag also puts the story in the matching part of the workshop's sidebar.
- **The rules of ADR 0016 carry over**, now as story rules: English only; light and dark from the roles, never a palette step; no network; real elements, a `data-component` attribute and glossary `id`s for the regions an issue would name; one story per screen state, with the same name for its phone, tablet and web versions. The workshop's accessibility check covers every mockup story.
- **The agreed mockup is still the contract.** Moving a mockup into a story changes its form, not its content: each converted mockup is compared with the old HTML by screenshot, light and dark, before the old file is deleted, and any difference is listed in the PR.

## Why

- One place describes the design system: its rules, its components and the screens drawn from them. A reviewer no longer compares a gallery page with a mockup page with a component page.
- A web mockup built from the components cannot drift from them, and a component change shows up in every screen that uses it.
- Stories are checked: accessibility, light and dark, and the interaction a mockup shows.
- Agents reach mockups through the Storybook MCP like any other story.

## Consequences

- The migration is staged, in this order: the workshop pages and the tag; the phone and tablet stylesheet; the 41 agreed web mockups; the 124 agreed phone mockups and the draft. Until a mockup is converted, its HTML file stays the contract and ADR 0016's checks still apply to it.
- `design/check-mockups.mjs`, the docs site's mockup pages and its "Review in the docs" mockup links are replaced by the story tests and links to the stories as each stage lands.
- `design/mockups/` goes once every mockup is converted. Git history keeps the originals.
- The docs site keeps what is not design system: architecture, decisions, the brand, translations and the roadmap. Its top menu links to the workshop.
