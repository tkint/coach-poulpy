`CpPasswordField`, with a button that shows the password. In error, it turns crimson and a `CpErrorNote` under the form says why.

| Vue | Classes |
| --- | --- |
| `<CpPasswordField>` | `div.cp-fieldset` around a `label.cp-type-label`, `label.cp-input` (`cp-input-error` in error) holding the `input` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm`, and `p.cp-label.whitespace-normal` for the hint |
| `<CpPasswordField size="lg">` | `cp-input-lg` on the field, and the eye button and its icon follow it; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |
