`CpSidePanel`: the details of the row picked in a list, floating over the page from the right (the whole width on a phone) with a dim backdrop, so the list never moves. A click outside, the close button and Escape close it. Render it with `v-if` while a row is picked.

| Vue | Classes |
| --- | --- |
| `<CpSidePanel>` | `dialog.cp-modal.cp-modal-end` around a `div.cp-modal-box` (`w-full sm:w-96`) with an `h2.cp-type-headline` and a `cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm` close button, and a `form.cp-modal-backdrop` |
