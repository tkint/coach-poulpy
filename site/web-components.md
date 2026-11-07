---
outline: [2, 2]
---

# Web components

The landing site, the web app, the web mockups and this site draw with one package, [`web/ui`](web/ui/package.json): DaisyUI with the Poulpy theme, and a few Vue components on top ([ADR 0023](/docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system)). The samples below are those components, in light or dark with the switch (remembered apart from the site's own theme). The rules for using them are in the [design system](/docs/design-system#websites). Under each sample are the classes a web mockup writes, since a mockup is plain HTML and writes what the Vue component renders. Every DaisyUI class carries the `cp-` prefix (`cp-btn`, not `btn`); the few classes of our own (`cp-chip`, `cp-stepper`, `cp-type-*`...) never reuse a DaisyUI name. Tailwind's own utilities (`w-full`, `bg-secondary`) have no prefix.

<SampleThemeSwitch />

## Button

Primary, tonal, outline and grumpy: `CpButton`.

<WebComponentSamples part="button" />

| Vue | Classes |
| --- | --- |
| `<CpButton>` | `cp-btn cp-btn-xl cp-btn-primary` |
| `<CpButton variant="tonal">` | `cp-btn cp-btn-xl cp-btn-secondary` |
| `<CpButton variant="outline">` | `cp-btn cp-btn-xl cp-btn-outline` |
| `<CpButton variant="grumpy">` | `cp-btn cp-btn-xl cp-btn-accent` |

## Chip

`CpChip`, a toggle with `aria-pressed`.

<WebComponentSamples part="chip" />

| Vue | Classes |
| --- | --- |
| `<CpChip v-model>` | `cp-btn cp-chip cp-btn-primary` when on, `cp-btn cp-chip cp-btn-outline` when off, with `aria-pressed` |

## Search field

`CpSearchField`.

<WebComponentSamples part="search-field" />

| Vue | Classes |
| --- | --- |
| `<CpSearchField>` | `label.cp-input.cp-search-field` around the icon and an `input type="search"` |

## Stepper

`CpStepper`.

<WebComponentSamples part="stepper" />

| Vue | Classes |
| --- | --- |
| `<CpStepper>` | `.cp-stepper`, `.cp-stepper-row`, `cp-btn cp-btn-circle cp-btn-outline`, `output.cp-stepper-value` |

## Select field

`CpSelectField`.

<WebComponentSamples part="select-field" />

| Vue | Classes |
| --- | --- |
| `<CpSelectField>` | `label.cp-fieldset`, a `span.cp-type-label` and `select.cp-select` |

## Password field

`CpPasswordField`, with a button that shows the password. In error, it turns crimson and a `CpErrorNote` under the form says why.

<WebComponentSamples part="password-field" />

| Vue | Classes |
| --- | --- |
| `<CpPasswordField>` | `div.cp-fieldset` around a `label.cp-type-label`, `label.cp-input` (`cp-input-error` in error) holding the `input` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm`, and `span.cp-label` for the hint |

## Poulpy card

`CpPoulpyCard`: no results, an empty list.

<WebComponentSamples part="poulpy-card" />

| Vue | Classes |
| --- | --- |
| `<CpPoulpy>` | `img.cp-poulpy` from `design/poulpy/` |
| `<CpPoulpyCard>` | `cp-card bg-secondary text-secondary-content cp-poulpy-card` |

## Error

`CpErrorNote`: crimson with an icon, never ember.

<WebComponentSamples part="error" />

| Vue | Classes |
| --- | --- |
| `<CpErrorNote>` | `cp-alert cp-alert-error cp-error-note` with an icon, `role="alert"` |

## Sheet

`CpSheet`: a bottom sheet on a phone, a dialog on a wider screen.

<WebComponentSamples part="sheet" />

| Vue | Classes |
| --- | --- |
| `<CpSheet>` | `dialog.cp-modal.cp-modal-bottom.sm:cp-modal-middle.cp-sheet` and `.cp-modal-box` |

## Type

Plain classes, no component: `cp-type-display`, `cp-type-title`, `cp-type-headline`, `cp-type-label`.
