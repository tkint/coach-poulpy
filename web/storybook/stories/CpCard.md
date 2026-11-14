`CpCard`: a raised panel, with a title when it has one. A `badge` slot puts the state of what the card is about beside the title (mail: configured), and a `description` says under it what the card holds. `CpPoulpyCard` is for empty states.

| Vue | Classes |
| --- | --- |
| `<CpCard>` | `section.cp-card.bg-base-100.shadow-sm` around `div.cp-card-body.gap-4.p-4.md:p-6` and, with a `title`, `h2.cp-card-title` |
| `<CpCard title description>` with `#badge` | the title in `div.flex.flex-col.gap-1`: `div.flex.flex-wrap.items-center.gap-3` holding the `h2.cp-card-title` and the badge, then `p.text-sm.text-base-content/70` |
