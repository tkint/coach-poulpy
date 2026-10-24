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

`build-poulpy.mjs` draws Poulpy's six mood portraits and thirteen poses with the brand book's own drawing code, and writes them for each platform:

```sh
node design/build-poulpy.mjs
```

| Generated file | Used by |
| --- | --- |
| `design/poulpy/*.svg` | Reference copies, to look at or reuse on the web |
| `app-android/src/main/res/drawable/poulpy_*.xml` | Android vector drawables (`R.drawable.poulpy_pose_champion`) |
| `app-ios/CoachPoulpy/Assets.xcassets/Poulpy/*.imageset` | iOS vector assets (`Image("PoulpyPoseChampion")`) |

All drawings share one 248 × 226 frame, so a mood portrait and a pose can replace each other without moving. CI runs it with `--check` too. The shared `PoulpyDrawing` enum names each drawing, and `PoulpyCoach` picks which one Today shows.

The full brand book, with the logo, mood rules and poses, is the "Poulpy Brand Book" page in the project.
