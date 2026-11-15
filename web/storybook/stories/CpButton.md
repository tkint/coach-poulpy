Primary, tonal, outline, ghost (a quieter cancel beside a save), grumpy and danger (crimson, for a deletion that can't be undone): `CpButton`. `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given: a screen's main action is `size="xl"` (56 high), a card's actions are md (40).

| Vue | Classes |
| --- | --- |
| `<CpButton>` | `cp-btn cp-btn-md cp-btn-primary` |
| `<CpButton variant="tonal">` | `cp-btn cp-btn-md cp-btn-secondary` |
| `<CpButton variant="outline">` | `cp-btn cp-btn-md cp-btn-outline` |
| `<CpButton variant="ghost">` | `cp-btn cp-btn-md cp-btn-ghost` |
| `<CpButton variant="grumpy">` | `cp-btn cp-btn-md cp-btn-accent` |
| `<CpButton variant="danger">` | `cp-btn cp-btn-md cp-btn-error` |
| `<CpButton size="xl">` | `cp-btn-xl` in place of `cp-btn-md` (`cp-btn-xs`, `-sm`, `-lg` and `-xxl` the same way) |

`loading` is for a button that waits for an answer, such as Save or Publish: it is disabled, keeps its width, and shows a spinner over its label (`cp-loading cp-loading-spinner`).
