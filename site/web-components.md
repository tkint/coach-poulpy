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

`CpSearchField`: a search is never a sign-in, so the input sets `autocomplete="off"` and the ignore attributes of Proton Pass, 1Password and LastPass, and a password manager does not offer to fill it, even when people search for an email address.

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

## Text field

`CpTextField`, a one-line field such as an email. In error, it turns crimson and a `CpErrorNote` under the form says why.

<WebComponentSamples part="text-field" />

| Vue | Classes |
| --- | --- |
| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input` |

## Password field

`CpPasswordField`, with a button that shows the password. In error, it turns crimson and a `CpErrorNote` under the form says why.

<WebComponentSamples part="password-field" />

| Vue | Classes |
| --- | --- |
| `<CpPasswordField>` | `div.cp-fieldset` around a `label.cp-type-label`, `label.cp-input` (`cp-input-error` in error) holding the `input` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm`, and `p.cp-label.whitespace-normal` for the hint |

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

## Badge

`CpBadge`: what kind of change, what state. Primary, neutral, outline, or error (crimson, with an icon beside it); never ember or `warning`.

<WebComponentSamples part="badge" />

| Vue | Classes |
| --- | --- |
| `<CpBadge>` | `cp-badge cp-badge-primary cp-badge-soft` |
| `<CpBadge tone="neutral">` | `cp-badge cp-badge-neutral cp-badge-soft` |
| `<CpBadge tone="outline">` | `cp-badge cp-badge-outline` |
| `<CpBadge tone="error">` | `cp-badge cp-badge-error cp-badge-soft` |

## Avatar

`CpAvatar`: initials in a circle, until there are profile pictures.

<WebComponentSamples part="avatar" />

| Vue | Classes |
| --- | --- |
| `<CpAvatar>` | `div.cp-avatar.cp-avatar-placeholder` around `div.rounded-full.w-8.bg-secondary.text-secondary-content` and a `span` |
| `<CpAvatar tone="primary" size="md">` | the same with `w-10 bg-primary text-primary-content` |
| `<CpAvatar tone="primary" size="lg">` | the same with `w-16 text-2xl` |

## Card

`CpCard`: a raised panel, with a title when it has one. `CpPoulpyCard` is for empty states.

<WebComponentSamples part="card" />

| Vue | Classes |
| --- | --- |
| `<CpCard>` | `section.cp-card.bg-base-100.shadow-sm` around `div.cp-card-body.gap-4.p-4.md:p-6` and, with a `title`, `h2.cp-card-title` |

## Table

`CpTable`: scrolls sideways on a phone. Put it in a `CpCard`.

<WebComponentSamples part="table" />

| Vue | Classes |
| --- | --- |
| `<CpTable>` | `div.overflow-x-auto` around `table.cp-table` |

## Stats

`CpStats` and `CpStat`: a row of figures, stacked on a phone.

<WebComponentSamples part="stats" />

| Vue | Classes |
| --- | --- |
| `<CpStats>` | `cp-stats cp-stats-vertical md:cp-stats-horizontal bg-base-100 shadow-sm` |
| `<CpStat>` | `cp-stat` with `cp-stat-title`, `cp-stat-value` (`text-primary` with `accent`) and `cp-stat-desc` |

## Breadcrumbs

`CpBreadcrumbs`: where a page sits, as links to the pages above it and the page itself last. Use it on a page that opens from a list, in place of a one-off back link.

<WebComponentSamples part="breadcrumbs" />

| Vue | Classes |
| --- | --- |
| `<CpBreadcrumbs label>` | `nav.cp-breadcrumbs` around `ul`, with an `li` per step (`a` for the pages above, plain text for the current one) |

## Navigation menu

`CpNavMenu` and `CpNavItem`: the admin's side navigation. Put a Lucide icon before each label.

<WebComponentSamples part="nav-menu" />

| Vue | Classes |
| --- | --- |
| `<CpNavMenu>` | `ul.cp-menu.cp-menu-md.w-full.gap-1.p-0` |
| `<CpNavItem active>` | `li` and `a.cp-menu-active` with `aria-current="page"` |

## Drawer

`CpDrawer`: the admin's page frame. A full-height sidebar from a large screen up, folded to icons by the button in the top bar; on a smaller screen it slides over the page. It fills the browser window, so it has no live sample here: see it in the [Admin mockup](/mockups/web/agreed/admin).

| Vue | Classes |
| --- | --- |
| `<CpDrawer>` | `cp-drawer lg:cp-drawer-open` with `input.cp-drawer-toggle`, `div.cp-drawer-content` (a `cp-navbar` and the page) and `div.cp-drawer-side` with `label.cp-drawer-overlay` |
| `<CpDrawerItem label>` | `li` and a `button` (an `a` when it has `href`, a link to another page) with `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` and `data-tip`; `cp-menu-active` when current |
| In the sidebar | `cp-is-drawer-close:hidden` hides a label when folded, `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` with `data-tip` names the icon. The variants carry the `cp-` prefix. |

## Type

Plain classes, no component: `cp-type-display`, `cp-type-title`, `cp-type-headline`, `cp-type-label`.
