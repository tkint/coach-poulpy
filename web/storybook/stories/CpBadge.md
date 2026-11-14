`CpBadge`: what kind of change, what state. Primary, neutral, outline, success (a state that is fine), or error (crimson, with its alert icon drawn in, so it never relies on colour alone); never ember or `warning`.

| Vue | Classes |
| --- | --- |
| `<CpBadge>` | `cp-badge cp-badge-primary cp-badge-soft` |
| `<CpBadge tone="neutral">` | `cp-badge cp-badge-neutral cp-badge-soft` |
| `<CpBadge tone="outline">` | `cp-badge cp-badge-outline` |
| `<CpBadge tone="success">` | `cp-badge cp-badge-success cp-badge-soft` |
| `<CpBadge tone="error">` | `cp-badge cp-badge-error cp-badge-soft gap-1` with the alert icon (`svg.size-3`) before the words |
| `<CpBadge small>` | adds `cp-badge-sm`, for the badge under a field that says where its translation stands; an error draws no icon |
