`CpBodyMap`: the muscles worked, as the front and back figure with each area lit from heat 1 (a little) to 3 (most), and a Less to More legend. The figures come from the generated body map shapes; `description` says in words what the picture shows.

| Vue | Classes |
| --- | --- |
| `<CpBodyMap>` | `div.cp-body-map` (`role="img"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3) and `path.cp-body-outline`, then `p.cp-heat-legend` (left out when `less` and `more` are) |
| `<CpBodyMap interactive>` | `role="group"`; an area with a `key` and a `label` is a `path.cp-body-part.cp-body-part-pick[role=button]` (`aria-pressed` says whether it is lit) and says its key with `pick`, so the picture is a field: the screen writes its own legend, or none |
