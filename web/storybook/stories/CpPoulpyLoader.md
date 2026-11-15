`CpPoulpyLoader` is the loading indicator: Poulpy's outline seen from above, the flower of his eight tentacles, swinging half a turn one way and then half a turn the other way, forever, once every 3.6 s (once every 12 s when the page asks for reduced motion). Each tentacle hangs loose: it bends about its base behind the turn and follows a little later than the one before. `spin="clockwise"` is the other version: it only turns clockwise, a full turn every cycle, with the tentacles trailing behind. It replaces spinners. `size` is its width in pixels (48 by default). It is a status for a screen reader: `label` says what is loading, and says "Loading" in the page's language when it is left out. The phones' `PoulpyLoader` moves the same way, from `LoaderSwing` in the shared code.

The picture is `design/poulpy-3d/pictures/loader_tentacle.png`, one tentacle painted by `design/build-poulpy-pictures.mjs` in the brand blue and see-through, drawn eight times round the middle, so it sits on a light or a dark page.

| Vue | Classes |
| --- | --- |
| `<CpPoulpyLoader>` | `span.cp-poulpy-loader` around eight `span.cp-poulpy-loader-tentacle` |
