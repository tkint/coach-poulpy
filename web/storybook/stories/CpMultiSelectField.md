`CpMultiSelectField`: any number of values from a long list. It is a button that opens its panel only while it is used: a search box narrows the list and each match is a checkbox. The button says how many are picked; Escape or a click elsewhere closes the panel.

| Vue | Classes |
| --- | --- |
| `<CpMultiSelectField>` | `details` with a `summary.cp-btn.cp-btn-sm.cp-btn-outline` and a `cp-card` panel of `input.cp-input.cp-input-sm` and a list of `cp-checkbox cp-checkbox-sm` |
| `<CpMultiSelectField size="sm">` | `cp-btn-sm` on the `summary` and `cp-input-sm` on its search box; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |
