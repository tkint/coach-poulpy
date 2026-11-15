`CpPoulpyLoader` is the loading indicator: Poulpy's outline seen from above, with his tentacles loose, swinging 45 degrees one way and then back the other way, forever, once every 2.4 s (once every 10 s when the page asks for reduced motion). It replaces spinners. `size` is its width in pixels (48 by default). It is a status for a screen reader: `label` says what is loading, and says "Loading" in the page's language when it is left out. The phones' `PoulpyLoader` swings the same way, from `LoaderSwing` in the shared code.

The picture is `design/poulpy-3d/pictures/loader.png`, painted by `design/build-poulpy-pictures.mjs` in the brand blue and see-through, so it sits on a light or a dark page.

| Vue | Classes |
| --- | --- |
| `<CpPoulpyLoader>` | `span.cp-poulpy-loader` around the loader picture |
