`CpTextarea`: a multiline field that grows to fit its content. It resizes after typing, when its model changes, and when its width changes and text wraps onto different lines. Its appearance comes from the shared DaisyUI theme and is included in both generated UI stylesheets.

| Vue | Classes |
| --- | --- |
| `<CpTextarea v-model label rows hint>` | `div.cp-fieldset` around `label.cp-type-label`, `textarea.cp-textarea.w-full`, and an optional `p.cp-label.whitespace-normal` helper; a `#status` slot under the helper takes a badge |
