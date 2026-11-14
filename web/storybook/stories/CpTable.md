`CpTable`: scrolls sideways on a phone. It draws on an opaque, bordered surface (`bg-base-100`), so its rows read the same on a card, on the page and in a dark theme.

| Vue | Classes |
| --- | --- |
| `<CpTable>` | `div.overflow-x-auto.rounded-box.border.border-base-300.bg-base-100` around `table.cp-table.bg-base-100` |

Sortable columns put a `cp-btn cp-btn-ghost cp-btn-sm` in the header cell, with the arrow of the order beside the label, and `aria-sort` on the `th`. A table of records the person opens has a button in the first cell and a `cp-hover` row; a row picked or selected has `aria-selected="true"` and `bg-base-200`. A table whose rows are selected for a bulk action starts each row with a `CpCheckbox`.
