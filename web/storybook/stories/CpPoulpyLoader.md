`CpPoulpyLoader` is the loading indicator: Poulpy's outline seen from underneath, drawn frame by frame from the 3D model with his tentacles loose, swinging half a turn one way and then half a turn the other way, forever, once every 3.6 s (once every 12 s when the page asks for reduced motion). `spin="clockwise"` is the other version: it only turns clockwise, a full turn every cycle, with his tentacles trailing the same way all the time. It replaces spinners. `size` is its width in pixels (48 by default). It is a status for a screen reader: `label` says what is loading, and says "Loading" in the page's language when it is left out. The phones' `PoulpyLoader` moves the same way, from `LoaderSwing` in the shared code.

The pictures are in `design/poulpy-3d/loader`, painted by `design/build-poulpy-pictures.mjs` in the brand blue and see-through, so they sit on a light or a dark page: `swing.png` has the 48 frames (8 across and 6 down) and `clockwise.png` is the one picture of the clockwise version. The phones read the same files, so they exist once.

| Vue | Classes |
| --- | --- |
| `<CpPoulpyLoader>` | `span.cp-poulpy-loader` around `span.cp-poulpy-loader-frames` |
