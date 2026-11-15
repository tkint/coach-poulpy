`CpTextField`, a one-line field such as an email, with a `hint` under it when it needs one. In error, it turns crimson and a `CpErrorNote` under the form says why; a field checked on its own, such as an address that isn't one, passes `error` instead, which takes the hint's place in crimson.

| Vue | Classes |
| --- | --- |
| `<CpTextField>` | `div.cp-fieldset` around a `label.cp-type-label` and `label.cp-input` (`cp-input-error` in error) holding the `input`; `type="search"` (with `autocomplete="off"`) is for searches and adds a search icon in the `label.cp-input`, at the height of the other fields |
| `<CpTextField hint>` | adds `p.cp-label.whitespace-normal` under the field, named by the input's `aria-describedby` |
| `<CpTextField error>` | `cp-input-error`, `aria-invalid`, and `p.cp-label.flex.items-center.gap-1.whitespace-normal.text-error` with `role="alert"` and the alert icon (`svg.size-4`), in the hint's place |
| `<CpTextField placeholder>` | a placeholder, such as "Needed before publishing" in a translation still to write |
| `<CpTextField>` with `#icon` | an icon before the text inside `label.cp-input`, such as a lock on a slug that can't change |
| `<CpTextField disabled>` | a field that can't be changed, such as a slug locked once its exercise is published |
| `<CpTextField>` with `#status` | a slot under the hint for a badge, such as a field's translation status |
| `<CpTextField size="lg">` | `cp-input-lg` in place of `cp-input-md`; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |
