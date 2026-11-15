`CpSelectField`: one value from a list. A button shows the current value and opens its panel only while it is used; the options look like the multi-select field's, without the checkboxes, and the chosen one is bold with a check. Choosing an option, Escape or a click elsewhere closes the panel. With `compact` it is one control of a filter bar: its label is hidden (still read aloud, and its first option usually says it too) and it is only as wide as its longest option.

| Vue | Classes |
| --- | --- |
| `<CpSelectField>` | `div.cp-fieldset`, a `span.cp-type-label` and a `details` with a `summary.cp-btn.cp-btn-outline` and a `cp-card` `ul[role=listbox]` of option `button`s (`flex w-full items-center gap-3 rounded-field px-2 py-1 hover:bg-base-200`); `cp-btn-sm w-auto` and a `sr-only` label with `compact` |
| `<CpSelectField size="sm">` | `cp-btn-sm` on the `summary`, as in a filter bar; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |
