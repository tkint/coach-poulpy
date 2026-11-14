`CpBodyMap`: the full web body map, the front and back figure drawn from the apps' shapes (`design/body-map.json`; the figures and area names are under Core / Body map), with the areas worked lit and the areas that hurt in the pain colour, and an optional Less to More legend.

## Props

| Prop | Takes | Says |
| --- | --- | --- |
| `heat`, `v-model:heat` | areas | what is lit; `v-model:heat` in a form |
| `injury`, `v-model:injury` | areas | what hurts, in the pain colour; `v-model:injury` in a form |
| `editing` | `heat` (default) or `injury` | with `interactive`, which of the two a pick updates |
| `captions` | `{ front, back }` | the text under each figure |
| `description` | text | the picture in words, for a screen reader |
| `figure` | `shoulders` (default) or `hips` | the body figure |
| `layout` | `both` (default), `front` or `back` | which figures are drawn |
| `legend` | `hidden` (default), `top` or `right` | where the Less to More scale goes |
| `less`, `more` | text | the words of the legend |
| `interactive-mode` | `individual` (default) or `both` | with `interactive` and `v-model:heat` or `v-model:injury`: a tap toggles the side tapped, or both sides of the area |
| `interactive`, `area-label` | flag, `(area) => text` | an area `area-label` names is a button; `pick` says its `area` and `side` |

## Areas

`heat` and `injury` take the same object: an area by its name (`biceps`, `lower-back`, `quads`...), as the catalog and the API name it, and its value is a side or a side with a number.

```ts
{ biceps: 'left', pectoral: 'both', deltoid: { side: 'right', heat: 2 } }
```

- A side is `left`, `right` or `both`, the person's own left and right.
- `heat` runs from 1 (a little) to 3 (most) and is `3` when left out. On an injury it says how much it hurts.
- An area is drawn on the figure that has it, so there is one object for both the front and the back.
- Where a muscle and an injury meet, the injury is drawn.

| Vue | Classes |
| --- | --- |
| `<CpBodyMap>` | `div.cp-body-map` (`role="img"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3, `data-pain` 1 to 3) and `path.cp-body-outline`, and `p.cp-heat-legend` (only when `legend` is `top` or `right`) |
| `<CpBodyMap interactive>` | `role="group"`; a named area is a `path.cp-body-part.cp-body-part-pick[role=button]` (`aria-pressed` says whether it is lit) and says its area and side with `pick`, with the legend `hidden` or the screen's own |
