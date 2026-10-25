# Design tokens

`tokens.json` is the single source for Coach Poulpy's colours, mood shades, type scale, spacing, radius and motion. Edit it, then regenerate the platform files:

```sh
node design/build-tokens.mjs
```

| Generated file | Used by |
| --- | --- |
| `app-android/.../ui/PoulpyTokens.kt` | `CoachPoulpyTheme` (Material 3 colour scheme, typography, shapes) plus `PoulpyPalette`, `PoulpyMood`, `PoulpySpace`, `PoulpyRadius`, `PoulpyMotion` |
| `app-ios/CoachPoulpy/Theme/PoulpyTokens.swift` | `PoulpyColor` (light and dark), `PoulpyPalette`, `PoulpyMood`, `PoulpyFont`, `PoulpySpace`, `PoulpyRadius`, `PoulpyMotion` |
| `app-ios/.../AccentColor.colorset/Contents.json` | The iOS accent colour (buttons, links, toggles) |
| `web/src/styles/tokens.css` | CSS custom properties (`--cp-*`), with dark values under `prefers-color-scheme` |

CI runs `node design/build-tokens.mjs --check` and fails if a generated file no longer matches `tokens.json`.

Colour rules:

- The `poulpy` colours (blue, ember, ink) are the mascot's fabric and are the same in light and dark.
- The blue side (`mood.proud`, `mood.happy`, `mood.recovering`) means the plan is on track; the ember side (`mood.annoyed`, `mood.grumpy`, `mood.fuming`) means a session slipped.
- The plush blue and ember are too light for white text: buttons use `primary` (lagoon 600) and `grumpy` (ember 600).
- `error` is a crimson, always shown with an icon and text, so it is never confused with a grumpy Poulpy.

The brand fonts are Baloo 2 (headings) and Nunito (body). The website loads them from Google Fonts. The apps bundle one static file per weight listed under `font` in `tokens.json`, in `app-android/src/main/res/font` and `app-ios/CoachPoulpy/Fonts`. After changing those weights, rebuild the files and the theme:

```sh
pip install fonttools
python3 design/fonts/build-fonts.py
node design/build-tokens.mjs
```

On iOS, also list any new file under `UIAppFonts` in `Info.plist`. Views use `.font(.poulpy(.headline))` instead of `.font(.headline)` to get the system text styles in the brand fonts. Both fonts are under the SIL Open Font License (`design/fonts/OFL-*.txt`).

## Poulpy's drawings

`build-poulpy.mjs` draws Poulpy's six mood portraits and twelve poses with the brand book's own drawing code, plus the loading animation's top view, and writes them for each platform:

```sh
node design/build-poulpy.mjs
```

| Generated file | Used by |
| --- | --- |
| `design/poulpy/*.svg` | Reference copies, to look at or reuse on the web |
| `app-android/src/main/res/drawable/poulpy_*.xml` | Android vector drawables (`R.drawable.poulpy_pose_champion`) |
| `app-ios/CoachPoulpy/Assets.xcassets/Poulpy/*.imageset` | iOS vector assets (`Image("PoulpyPoseChampion")`) |

Portraits and poses share one 248 × 226 frame, so they can replace each other without moving. The loader (`loader`, and `loader_small` with a thicker line for sizes under 32) is Poulpy seen from above as a blue outline of eight round lobes with short tails; `PoulpyLoader` on Android and iOS turns it clockwise once every 2.4 s (10 s when animations are reduced) and replaces the old Spinning pose. CI runs it with `--check` too. The shared `PoulpyDrawing` enum names each drawing, and `PoulpyCoach` picks which one Today shows.

The rules for using Poulpy are in [`docs/brand.md`](../docs/brand.md); the full brand book v6 is in `site/public/brand/book.html` and on the docs site.
