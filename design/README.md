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

The brand fonts are Baloo 2 (headings) and Nunito (body). The website loads them from Google Fonts. The apps use the system font (rounded on iOS) until the font files are added to the apps.

The full brand book, with the logo, mood rules and poses, is the "Poulpy Brand Book" page in the project.
