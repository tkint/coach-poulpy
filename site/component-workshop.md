---
outline: [2, 2]
---

# Component workshop

Every component of [`web/ui`](web/ui/package.json) in each of its states, from [Storybook](https://storybook.js.org) ([ADR 0028](/docs/adr/0028-storybook-is-the-component-workshop-of-the-design-system)). Each state is a story, and each story runs as a test in CI: a panel that is open on load, a button that stays enabled when it should not, a missing label or a colour with too little contrast fails the build. The [Web components](/web-components) page holds the classes each component renders, for web mockups.

<WorkshopLink>Open the whole workshop</WorkshopLink>: the component list on the left, a **Docs** page per component that lists all its states one under the other, the light and dark switch in the toolbar and an accessibility panel under each story.

## Select field

Shut on load, then open, as the owner sees it.

<StoryFrame story="fields-selectfield--closed" :height="120" />
<StoryFrame story="fields-selectfield--open" :height="260" />

## Sheet

A sheet never shows itself on load. Its second state is open.

<StoryFrame story="overlays-sheet--closed" :height="160" />
<StoryFrame story="overlays-sheet--open" :height="360" />

## Dropdown menu

<StoryFrame story="navigation-dropdownmenu--closed" :height="120" />
<StoryFrame story="navigation-dropdownmenu--open" :height="260" />

## Fields with a problem

<StoryFrame story="fields-textfield--with-an-error" :height="200" />
<StoryFrame story="fields-passwordfield--invalid" :height="200" />

## Running it

```sh
cd web && npm ci
npm run storybook                                  # the workshop at http://localhost:6006
npm test -w storybook                              # every story as a test
npx playwright install chromium                    # once, for the tests
```

On a machine that already has Chromium, `CHROMIUM_PATH=/path/to/chromium npm test -w storybook` uses it.
