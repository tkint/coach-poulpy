---
outline: [2, 2]
---

# Web components

The landing site, the web app, the web mockups and this site draw with one package, [`web/ui`](web/ui/package.json): DaisyUI with the Poulpy theme, and a few Vue components on top ([ADR 0023](/docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system)). The samples below are those components, in light and dark. The rules for using them are in the [design system](/docs/design-system#websites).

<WebComponentSamples />

## Classes

A web mockup is plain HTML, so it writes the classes the Vue components render:

| Component | Vue | Classes |
| --- | --- | --- |
| Primary button | `<CpButton>` | `btn cp-button btn-primary` |
| Tonal button | `<CpButton variant="tonal">` | `btn cp-button btn-secondary` |
| Outline button | `<CpButton variant="outline">` | `btn cp-button btn-outline` |
| Grumpy button | `<CpButton variant="grumpy">` | `btn cp-button btn-accent` |
| Chip | `<CpChip v-model>` | `btn cp-chip btn-primary` when on, `btn cp-chip btn-outline` when off, with `aria-pressed` |
| Search field | `<CpSearchField>` | `label.input.cp-search-field` around the icon and an `input type="search"` |
| Select field | `<CpSelectField>` | `label.fieldset`, a `span.cp-label` and `select.select` |
| Stepper | `<CpStepper>` | `.cp-stepper`, `.cp-stepper-row`, `btn btn-circle btn-outline`, `output.cp-stepper-value` |
| Poulpy | `<CpPoulpy>` | `img.cp-poulpy` from `design/poulpy/` |
| Poulpy card | `<CpPoulpyCard>` | `card bg-secondary text-secondary-content cp-poulpy-card` |
| Error | `<CpErrorNote>` | `alert alert-error cp-error-note` with an icon, `role="alert"` |
| Sheet | `<CpSheet>` | `dialog.modal.modal-bottom.sm:modal-middle.cp-sheet` and `.modal-box` |
| Type | | `cp-display`, `cp-title`, `cp-headline`, `cp-label` |
