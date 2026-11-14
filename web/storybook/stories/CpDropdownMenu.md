`CpDropdownMenu`: a button that opens a short menu of actions only while it is used. Choosing an entry, Escape or a click elsewhere closes it.

| Vue | Classes |
| --- | --- |
| `<CpDropdownMenu>` | `details` with a `summary.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` and a `ul.cp-menu` panel |

All the dropdowns (select, multi-select, menu) share the closing rule of `web/ui/src/dismissable.ts`, and their panels float over the page (`absolute`), so opening or closing one never moves anything.
