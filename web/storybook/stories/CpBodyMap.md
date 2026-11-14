`CpBodyMap`: the full body map, the front and back figure drawn from the apps' shapes (`design/body-map.json`), with the areas worked lit and the areas that hurt in the pain colour, and an optional Less to More legend.

## Props

| Prop | Takes | Says |
| --- | --- | --- |
| `front`, `back` | areas | what is lit on that side of the figure |
| `front-injury`, `back-injury` | areas | what hurts on that side, in the pain colour |
| `captions` | `{ front, back }` | the text under each figure |
| `description` | text | the picture in words, for a screen reader |
| `figure` | `shoulders` (default) or `hips` | the body figure |
| `layout` | `both` (default), `front` or `back` | which figures are drawn |
| `legend` | `hidden` (default), `top` or `right` | where the Less to More scale goes |
| `less`, `more` | text | the words of the legend |
| `interactive`, `area-label` | flag, `(area) => text` | an area `area-label` names is a button; `pick` says its `area` and `side` |

## Areas

The four area props take the same object: an area by its name (`biceps`, `lower-back`, `quads`...), as the catalog and the API name it, and its value is a side or a side with a number.

```ts
{ biceps: 'left', pectoral: 'both', deltoid: { side: 'right', heat: 2 } }
```

- A side is `left`, `right` or `both`, the person's own left and right.
- `heat` runs from 1 (a little) to 3 (most) and is `3` when left out. On an injury it says how much it hurts.
- An area the figure does not have on that side is ignored, so `front` and `back` can be given the same object.
- Where a muscle and an injury meet, the injury is drawn.

| Vue | Classes |
| --- | --- |
| `<CpBodyMap>` | `div.cp-body-map` (`role="img"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3, `data-pain` 1 to 3) and `path.cp-body-outline`, and `p.cp-heat-legend` (only when `legend` is `top` or `right`) |
| `<CpBodyMap interactive>` | `role="group"`; a named area is a `path.cp-body-part.cp-body-part-pick[role=button]` (`aria-pressed` says whether it is lit) and says its area and side with `pick`, with the legend `hidden` or the screen's own |
