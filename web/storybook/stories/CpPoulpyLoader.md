`CpPoulpyLoader` is the loading indicator: Poulpy's outline seen from underneath, drawn frame by frame from the 3D model with his tentacles loose, swinging half a turn one way and then half a turn the other way, forever, once every 3.6 s (once every 12 s when the page asks for reduced motion). `spin="clockwise"` is the other version: it only turns clockwise, a full turn every cycle. It replaces spinners. `size` is its width in pixels (48 by default). It is a status for a screen reader: `label` says what is loading, and says "Loading" in the page's language when it is left out. The phones' `PoulpyLoader` moves the same way, from `LoaderSwing` in the shared code.

The frames are `design/poulpy-3d/pictures/loader_swing.png` and `loader_clockwise.png`, 48 frames each (8 across and 6 down) painted by `design/build-poulpy-pictures.mjs` in the brand blue and see-through, so they sit on a light or a dark page.

| Vue | Classes |
| --- | --- |
| `<CpPoulpyLoader>` | `span.cp-poulpy-loader` around `span.cp-poulpy-loader-frames` |
