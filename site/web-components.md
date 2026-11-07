---
outline: [2, 2]
---

# Web components

The landing site, the web app, the web mockups and this site draw with one package, [`web/ui`](web/ui/package.json): DaisyUI with the Poulpy theme, and a few Vue components on top ([ADR 0023](/docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system)). The samples below are those components, in light or dark with the switch (remembered apart from the site's own theme). The rules for using them are in the [design system](/docs/design-system#websites).

<SampleThemeSwitch />

## Button

Primary, tonal, outline and grumpy: `CpButton`.

<WebComponentSamples part="button" />

## Chip

`CpChip`, a toggle with `aria-pressed`.

<WebComponentSamples part="chip" />

## Search field

`CpSearchField`.

<WebComponentSamples part="search-field" />

## Stepper

`CpStepper`.

<WebComponentSamples part="stepper" />

## Select field

`CpSelectField`.

<WebComponentSamples part="select-field" />

## Poulpy card

`CpPoulpyCard`: no results, an empty list.

<WebComponentSamples part="poulpy-card" />

## Error

`CpErrorNote`: crimson with an icon, never ember.

<WebComponentSamples part="error" />

## Sheet

`CpSheet`: a bottom sheet on a phone, a dialog on a wider screen.

<WebComponentSamples part="sheet" />


## Classes

A web mockup is plain HTML, so it writes the classes the Vue components render. Every DaisyUI class carries the `cp-` prefix (`cp-btn`, not `btn`); the few classes of our own (`cp-chip`, `cp-stepper`, `cp-type-*`...) never reuse a DaisyUI name. Tailwind's own utilities (`w-full`, `bg-secondary`) have no prefix.

| Component | Vue | Classes |
| --- | --- | --- |
| Primary button | `<CpButton>` | `cp-btn cp-btn-xl cp-btn-primary` |
| Tonal button | `<CpButton variant="tonal">` | `cp-btn cp-btn-xl cp-btn-secondary` |
| Outline button | `<CpButton variant="outline">` | `cp-btn cp-btn-xl cp-btn-outline` |
| Grumpy button | `<CpButton variant="grumpy">` | `cp-btn cp-btn-xl cp-btn-accent` |
| Chip | `<CpChip v-model>` | `cp-btn cp-chip cp-btn-primary` when on, `cp-btn cp-chip cp-btn-outline` when off, with `aria-pressed` |
| Search field | `<CpSearchField>` | `label.cp-input.cp-search-field` around the icon and an `input type="search"` |
| Select field | `<CpSelectField>` | `label.cp-fieldset`, a `span.cp-type-label` and `select.cp-select` |
| Stepper | `<CpStepper>` | `.cp-stepper`, `.cp-stepper-row`, `cp-btn cp-btn-circle cp-btn-outline`, `output.cp-stepper-value` |
| Poulpy | `<CpPoulpy>` | `img.cp-poulpy` from `design/poulpy/` |
| Poulpy card | `<CpPoulpyCard>` | `cp-card bg-secondary text-secondary-content cp-poulpy-card` |
| Error | `<CpErrorNote>` | `cp-alert cp-alert-error cp-error-note` with an icon, `role="alert"` |
| Sheet | `<CpSheet>` | `dialog.cp-modal.cp-modal-bottom.sm:cp-modal-middle.cp-sheet` and `.cp-modal-box` |
| Type | | `cp-type-display`, `cp-type-title`, `cp-type-headline`, `cp-type-label` |
