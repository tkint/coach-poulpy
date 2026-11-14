---
outline: [2, 2]
---

# Web components

The landing site, the web app, the web mockups and this site draw with one package, [`web/ui`](web/ui/package.json): DaisyUI with the Poulpy theme, and a few Vue components on top ([ADR 0023](/docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system)). The samples below are those components, in light or dark with the switch (remembered apart from the site's own theme). The rules for using them are in the [design system](/docs/design-system#websites). Under each sample are the classes a web mockup writes, since a mockup is plain HTML and writes what the Vue component renders. Every DaisyUI class carries the `cp-` prefix (`cp-btn`, not `btn`); the few classes of our own (`cp-chip`, `cp-stepper`, `cp-type-*`...) never reuse a DaisyUI name. Tailwind's own utilities (`w-full`, `bg-secondary`) have no prefix.

<SampleThemeSwitch />

## Button

Primary, tonal, outline, grumpy and danger (crimson, for a deletion that can't be undone): `CpButton`. The large size is the default; `size="md"` drops `cp-btn-xl` for the actions of a card.

<WebComponentSamples part="button" />

| Vue | Classes |
| --- | --- |
| `<CpButton>` | `cp-btn cp-btn-xl cp-btn-primary` |
| `<CpButton variant="tonal">` | `cp-btn cp-btn-xl cp-btn-secondary` |
| `<CpButton variant="outline">` | `cp-btn cp-btn-xl cp-btn-outline` |
| `<CpButton variant="grumpy">` | `cp-btn cp-btn-xl cp-btn-accent` |
| `<CpButton variant="danger">` | `cp-btn cp-btn-xl cp-btn-error` |
| `<CpButton size="md">` | the same without `cp-btn-xl` |

## Chip

`CpChip`, a toggle with `aria-pressed`. With `compact` it is as high as the small controls of a filter bar.

<WebComponentSamples part="chip" />

| Vue | Classes |
| --- | --- |
| `<CpChip v-model>` | `cp-btn cp-chip cp-btn-primary` when on, `cp-btn cp-chip cp-btn-outline` when off, with `aria-pressed`; `cp-chip-compact` with `compact` |

## Search field

`CpSearchField`: a pill with a search icon. With `compact` it is one control of a filter bar: as high as a small button and 14rem wide instead of filling its row. The input is `type="search"` with `autocomplete="off"`, so a password manager does not take a search for an email address as a sign-in.

<WebComponentSamples part="search-field" />

| Vue | Classes |
| --- | --- |
| `<CpSearchField>` | `label.cp-input.cp-search-field` around the icon and an `input type="search"`; `cp-search-field-compact` with `compact` |

## Stepper

`CpStepper`.

<WebComponentSamples part="stepper" />

| Vue | Classes |
| --- | --- |
| `<CpStepper>` | `.cp-stepper`, `.cp-stepper-row`, `cp-btn cp-btn-circle cp-btn-outline`, `output.cp-stepper-value` |

## Select field

`CpSelectField`: one value from a list. A button shows the current value and opens its panel only while it is used; the options look like the multi-select field's, without the checkboxes, and the chosen one is bold with a check. Choosing an option, Escape or a click elsewhere closes the panel. With `compact` it is one control of a filter bar: its label is hidden (still read aloud, and its first option usually says it too) and it is only as wide as its longest option.

<WebComponentSamples part="select-field" />

| Vue | Classes |
| --- | --- |
| `<CpSelectField>` | `div.cp-fieldset`, a `span.cp-type-label` and a `details` with a `summary.cp-btn.cp-btn-outline` and a `cp-card` `ul[role=listbox]` of option `button`s (`flex w-full items-center gap-3 rounded-field px-2 py-1 hover:bg-base-200`); `cp-btn-sm w-auto` and a `sr-only` label with `compact` |

## Multi-select field

`CpMultiSelectField`: any number of values from a long list. It is a button that opens its panel only while it is used: a search box narrows the list and each match is a checkbox. The button says how many are picked; Escape or a click elsewhere closes the panel.

<WebComponentSamples part="multi-select-field" />

| Vue | Classes |
| --- | --- |
| `<CpMultiSelectField>` | `details` with a `summary.cp-btn.cp-btn-sm.cp-btn-outline` and a `cp-card` panel of `input.cp-input.cp-input-sm` and a list of `cp-checkbox cp-checkbox-sm` |

## Dropdown menu

`CpDropdownMenu`: a button that opens a short menu of actions only while it is used. Choosing an entry, Escape or a click elsewhere closes it.

<WebComponentSamples part="dropdown-menu" />

| Vue | Classes |
| --- | --- |
| `<CpDropdownMenu>` | `details` with a `summary.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` and a `ul.cp-menu` panel |

All the dropdowns (select, multi-select, menu) share the closing rule of `web/ui/src/dismissable.ts`, and their panels float over the page (`absolute`), so opening or closing one never moves anything.

## Checkbox

`CpCheckbox`: a checkbox with no visible label, for a table row or a header that selects rows. Its label names what it selects.

<WebComponentSamples part="checkbox" />

| Vue | Classes |
| --- | --- |
| `<CpCheckbox>` | `label.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` around `input.cp-checkbox` |

## Text field

`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn't one, passes `error` instead, which takes the hint's place in crimson.

<WebComponentSamples part="text-field" />

| Vue | Classes |
| --- | --- |
| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |
| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input's `aria-describedby` |
| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.whitespace-normal.text-error` with `role="alert"` in the hint's place |

## Textarea

`CpTextarea`: a multiline field that grows to fit its content. It resizes after typing, when its model changes, and when its width changes and text wraps onto different lines. Its appearance comes from the shared DaisyUI theme and is included in both generated UI stylesheets.

<WebComponentSamples part="textarea" />

| Vue | Classes |
| --- | --- |
| `<CpTextarea v-model label rows hint>` | `div.cp-fieldset` around `label.cp-type-label`, `textarea.cp-textarea.w-full`, and an optional `p.cp-label.whitespace-normal` helper |

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

## Success

`CpSuccessNote`: something went through, such as a test email sent. Green with a check and words; a status, not an alert.

<WebComponentSamples part="success" />

| Vue | Classes |
| --- | --- |
| `<CpSuccessNote>` | `cp-alert cp-alert-success` with a check icon, `role="status"` |

## Sheet

`CpSheet`: a bottom sheet on a phone, a dialog on a wider screen.

<WebComponentSamples part="sheet" />

| Vue | Classes |
| --- | --- |
| `<CpSheet>` | `dialog.cp-modal.cp-modal-bottom.sm:cp-modal-middle.cp-sheet` and `.cp-modal-box` |

## Badge

`CpBadge`: what kind of change, what state. Primary, neutral, outline, success (a state that is fine), or error (crimson, with its alert icon drawn in, so it never relies on colour alone); never ember or `warning`.

<WebComponentSamples part="badge" />

| Vue | Classes |
| --- | --- |
| `<CpBadge>` | `cp-badge cp-badge-primary cp-badge-soft` |
| `<CpBadge tone="neutral">` | `cp-badge cp-badge-neutral cp-badge-soft` |
| `<CpBadge tone="outline">` | `cp-badge cp-badge-outline` |
| `<CpBadge tone="success">` | `cp-badge cp-badge-success cp-badge-soft` |
| `<CpBadge tone="error">` | `cp-badge cp-badge-error cp-badge-soft gap-1` with the alert icon (`svg.size-3`) before the words |

## Avatar

`CpAvatar`: initials in a circle, until there are profile pictures.

<WebComponentSamples part="avatar" />

| Vue | Classes |
| --- | --- |
| `<CpAvatar>` | `div.cp-avatar.cp-avatar-placeholder` around `div.rounded-full.w-8.bg-secondary.text-secondary-content` and a `span` |
| `<CpAvatar tone="primary" size="md">` | the same with `w-10 bg-primary text-primary-content` |
| `<CpAvatar tone="primary" size="lg">` | the same with `w-16 text-2xl` |

## Card

`CpCard`: a raised panel, with a title when it has one. A `badge` slot puts the state of what the card is about beside the title (mail: configured), and a `description` says under it what the card holds. `CpPoulpyCard` is for empty states.

<WebComponentSamples part="card" />

<WebComponentSamples part="card-badge" />

| Vue | Classes |
| --- | --- |
| `<CpCard>` | `section.cp-card.bg-base-100.shadow-sm` around `div.cp-card-body.gap-4.p-4.md:p-6` and, with a `title`, `h2.cp-card-title` |
| `<CpCard title description>` with `#badge` | the title in `div.flex.flex-col.gap-1`: `div.flex.flex-wrap.items-center.gap-3` holding the `h2.cp-card-title` and the badge, then `p.text-sm.text-base-content/70` |

## Divider

`CpDivider`: a line between two parts of a card, such as a form and an action of its own. It adds no space; the card's gap does.

<WebComponentSamples part="divider" />

| Vue | Classes |
| --- | --- |
| `<CpDivider>` | `div.cp-divider.my-0` with `role="separator"` |

## Table

`CpTable`: scrolls sideways on a phone. It draws on an opaque, bordered surface (`bg-base-100`), so its rows read the same on a card, on the page and in a dark theme.

<WebComponentSamples part="table" />

| Vue | Classes |
| --- | --- |
| `<CpTable>` | `div.overflow-x-auto.rounded-box.border.border-base-300.bg-base-100` around `table.cp-table.bg-base-100` |

Sortable columns put a `cp-btn cp-btn-ghost cp-btn-sm` in the header cell, with the arrow of the order beside the label, and `aria-sort` on the `th`. A table of records the person opens has a button in the first cell and a `cp-hover` row; a row picked or selected has `aria-selected="true"` and `bg-base-200`. A table whose rows are selected for a bulk action starts each row with a `CpCheckbox`.

## Pagination

`CpPagination`: previous, the page numbers and next, joined in one row; a long list keeps the first and last pages and those around the current one, with a gap between. Put the count of what is shown beside it.

<WebComponentSamples part="pagination" />

| Vue | Classes |
| --- | --- |
| `<CpPagination>` | `nav` around `div.cp-join` of `cp-join-item cp-btn cp-btn-sm` buttons, `cp-btn-active` and `aria-current="page"` on the current page |

## Side panel

`CpSidePanel`: the details of the row picked in a list, floating over the page from the right (the whole width on a phone) with a dim backdrop, so the list never moves. A click outside, the close button and Escape close it. Render it with `v-if` while a row is picked.

<WebComponentSamples part="side-panel" />

| Vue | Classes |
| --- | --- |
| `<CpSidePanel>` | `dialog.cp-modal.cp-modal-end` around a `div.cp-modal-box` (`w-full sm:w-96`) with an `h2.cp-type-headline` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm` close button, and a `form.cp-modal-backdrop` |

## Stats

`CpStats` and `CpStat`: a row of figures, stacked on a phone. `vertical` keeps them stacked, for a narrow column beside other content (the workout page).

<WebComponentSamples part="stats" />

| Vue | Classes |
| --- | --- |
| `<CpStats>` | `cp-stats cp-stats-vertical md:cp-stats-horizontal bg-base-100 shadow-sm` (`cp-stats cp-stats-vertical` with `vertical`) |
| `<CpStat>` | `cp-stat` with `cp-stat-title`, `cp-stat-value` (`text-primary` with `accent`) and `cp-stat-desc` |

## Body map

`CpBodyMap`: the muscles worked, as the front and back figure with each area lit from heat 1 (a little) to 3 (most), and a Less to More legend. The figures come from the generated body map shapes; `description` says in words what the picture shows.

<WebComponentSamples part="body-map" />

| Vue | Classes |
| --- | --- |
| `<CpBodyMap>` | `div.cp-body-map` (`role="img"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3) and `path.cp-body-outline`, then `p.cp-heat-legend` |

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

`CpDrawer`: the admin's page frame. A full-height sidebar from a large screen up, folded to icons by the button in the top bar; on a smaller screen it slides over the page. It fills the browser window, so it has no live sample here: see it in the [Admin · Audit mockup](/mockups/web/agreed/admin-audit).

| Vue | Classes |
| --- | --- |
| `<CpDrawer>` | `cp-drawer lg:cp-drawer-open` with `input.cp-drawer-toggle`, `div.cp-drawer-content` (a `cp-navbar` and the page) and `div.cp-drawer-side` with `label.cp-drawer-overlay` |
| `<CpDrawerItem label>` | `li` and a `button` (an `a` when it has `href`, a link to another page) with `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` and `data-tip`; `cp-menu-active` when current |
| In the sidebar | `cp-is-drawer-close:hidden` hides a label when folded, `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` with `data-tip` names the icon. The variants carry the `cp-` prefix. |

## Type

Plain classes, no component: `cp-type-display`, `cp-type-title`, `cp-type-headline`, `cp-type-label`.
