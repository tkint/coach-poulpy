`CpSegmentedControl`: a few values side by side, one of them on, such as the language an exercise's text is written in. An option can be flagged with a crimson alert icon, read aloud as its `flagLabel`, such as a language some text still lacks.

| Vue | Classes |
| --- | --- |
| `<CpSegmentedControl>` | `div.cp-join[role=group]` of `button.cp-join-item.cp-btn` (`cp-btn-primary` for the picked one, `cp-btn-outline` for the others, `aria-current`), with the alert icon (`svg.size-4.text-error`) after a flagged option's text |
| `<CpSegmentedControl size="sm">` | `cp-btn-sm` on every button; `size` is the websites' scale (xs, sm, md, lg, xl, xxl), md when none is given |
