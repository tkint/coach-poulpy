`CpIcon` draws a Lucide icon at one of the scale's sizes: xs 12, sm 16, md 20, lg 24, xl 32, xxl 40. Pass the Lucide component itself (`<CpIcon :icon="Search" size="sm" />`); without a `size` it is md. It is always decorative (`aria-hidden`): a control that has only an icon carries its own label. The sizes are the `--cp-icon-*` tokens, generated from `design/tokens.json`.

| Vue | Classes |
| --- | --- |
| `<CpIcon :icon="Search">` | the Lucide `svg` with `shrink-0 cp-icon-md` |
| `<CpIcon :icon="Search" size="xl">` | the same with `cp-icon-xl` (`cp-icon-xs`, `-sm`, `-md`, `-lg`, `-xl`, `-xxl`) |
