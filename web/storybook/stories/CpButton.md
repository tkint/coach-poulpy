Primary, tonal, outline, ghost (a quieter cancel beside a save), grumpy and danger (crimson, for a deletion that can't be undone): `CpButton`. The large size is the default; `size="md"` drops `cp-btn-xl` for the actions of a card.

| Vue | Classes |
| --- | --- |
| `<CpButton>` | `cp-btn cp-btn-xl cp-btn-primary` |
| `<CpButton variant="tonal">` | `cp-btn cp-btn-xl cp-btn-secondary` |
| `<CpButton variant="outline">` | `cp-btn cp-btn-xl cp-btn-outline` |
| `<CpButton variant="ghost">` | `cp-btn cp-btn-xl cp-btn-ghost` |
| `<CpButton variant="grumpy">` | `cp-btn cp-btn-xl cp-btn-accent` |
| `<CpButton variant="danger">` | `cp-btn cp-btn-xl cp-btn-error` |
| `<CpButton size="md">` | the same without `cp-btn-xl` |

`loading` is for a button that waits for an answer, such as Save or Publish: it is disabled, keeps its width, and shows a spinner over its label (`cp-loading cp-loading-spinner`).
