`CpSearchField`: a pill with a search icon. With `compact` it is one control of a filter bar: 14rem wide instead of filling its row (pass `size="sm"` for it to be as high as a small button). The input is `type="search"` with `autocomplete="off"`, so a password manager does not take a search for an email address as a sign-in.

| Vue | Classes |
| --- | --- |
| `<CpSearchField>` | `label.cp-input.cp-search-field` around the icon and an `input type="search"`; `cp-search-field-compact` with `compact` |
| `<CpSearchField size="lg">` | `cp-input-lg` on the field, as a list's own search; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |
