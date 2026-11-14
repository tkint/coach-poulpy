`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn't one, passes `error` instead, which takes the hint's place in crimson.

| Vue | Classes |
| --- | --- |
| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |
| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input's `aria-describedby` |
| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.whitespace-normal.text-error` with `role="alert"` in the hint's place |
