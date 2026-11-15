`CpDropdownMenu`: a button that opens a short menu of actions only while it is used. Choosing an entry, Escape or a click elsewhere closes it.

| Vue | Classes |
| --- | --- |
| `<CpDropdownMenu>` | `details` with a `summary.cp-btn.cp-btn-ghost.cp-btn-square.cp-btn-sm` and a `ul.cp-menu` panel |

All the dropdowns (select, multi-select, menu) share the closing rule of `web/ui/src/dismissable.ts`, and their panels float over the page, so opening or closing one never moves anything. The select panels are `absolute`; the menu's panel is `fixed` under its button, so a table's own overflow never clips it or adds a scrollbar to a small table, and it closes when the page scrolls or resizes.
