`CpChipSelectField`: any number of values in a form. The picked ones sit in the field as removable badges; typing narrows the list its panel shows while it is used, and a picked value is checked there. Clicking a value again, its badge's cross or Backspace in an empty field takes it out; Enter picks the first match (so it never sends the form); Escape or a click elsewhere closes the panel. `aria-label` names a field whose label is written beside it, such as a heading above.

| Vue | Classes |
| --- | --- |
| `<CpChipSelectField>` | `div.cp-fieldset` around a `label.cp-type-label`, a `label.cp-input` holding `span.cp-badge.cp-badge-neutral.cp-badge-soft` badges (each with a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-xs` cross), the `input[role=combobox]` and a chevron, a `cp-card` `ul[role=listbox]` of option `button`s (as the select field's) and an optional `p.cp-label` hint |
