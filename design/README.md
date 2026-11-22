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
| `web/ui/src/tokens.css` | CSS custom properties (`--cp-*`), with dark values under `prefers-color-scheme`, and `cp-theme-light` / `cp-theme-dark` to pin one theme on part of a page. The websites' DaisyUI theme (`web/ui/src/theme.css`) points at them ([ADR 0023](../docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md)) |

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

## Body map figures

`build-body-map.mjs` draws the front and back muscle chart in both builds (wider shoulders, wider hips) and writes it into the profile screens' stories (`profile-pain-sheet`, `profile-body-figure-sheet`) and into `design/body-map.json`, which the apps read. The muscle outlines come from `design/body-map-source/parts.json`, adapted from the MIT-licensed [hugobonifay/body-map](https://github.com/hugobonifay/body-map) (see `design/body-map-source/NOTICE.md`); the script turns them into plain curves, names each muscle, stretches the builds and leaves the skin transparent. To change a muscle, edit the script or the source outlines, then run:

```sh
node design/build-body-map.mjs
```

## Poulpy's drawings

`build-poulpy.mjs` draws the themed (one-colour) layers of the Android app icons with the brand book's own drawing code. His moods, poses, loader and app icons are pictures of the 3D model, from `build-poulpy-pictures.mjs`:

```sh
node design/build-poulpy.mjs
```

| Generated file | Used by |
| --- | --- |
| `design/poulpy-3d/moves/pose_champion_cheer_{1,2,3}.png` | the champion's tap on Home (three jumps, medal and confetti, ending on his still picture), 72 frames in three sheets of 24, 497 × 472 pixels a frame, from `build-poulpy-pictures.mjs` |
| `design/poulpy-3d/moves/pose_champion_party_{1,2,3,4}.png` | the champion on the finished screen, who never stops: a seamless loop of four jumps, 96 frames in four sheets of 24, same frame size, from `build-poulpy-pictures.mjs` |
| `design/poulpy-3d/moves/mood_move_<name>.png` | how the happy Poulpy loses patience on Home: the turns between his moods (`to_annoyed`, `to_fuming`, `calm_down`, `back_to_normal`) and his answers to a tap while annoyed or fuming (`shake`, `shudder`), one sheet each, 34 to 46 frames at 24 a second, from `design/poulpy-3d/moods.mjs` by `build-poulpy-pictures.mjs` |
| `design/poulpy-3d/moves/mood_{happy,proud}_{left,right}.png`, `mood_happy_spin.png` | Home's moves (a jump leaning each way, and the high jump with a full turn for happy), 60 frames each in one sheet at 48 a second, for every platform: the website, Android (assets) and iOS (bundle) read these files, from `build-poulpy-pictures.mjs` |
| `design/poulpy-3d/loader/{swing,clockwise}.png` | The loader, once for every platform: the website, Android (as assets) and iOS (in the bundle) all read these two files, from `build-poulpy-pictures.mjs` |
| `app-android/src/main/res/drawable-nodpi/poulpy_*.png` | His moods and poses as Android pictures (`R.drawable.poulpy_pose_champion`), from `build-poulpy-pictures.mjs` |
| `app-ios/CoachPoulpy/Assets.xcassets/Poulpy/Poulpy{Mood,Pose}*.imageset` | His moods and poses as iOS 3x pictures (`Image("PoulpyPoseChampion")`), from `build-poulpy-pictures.mjs` |
| `app-android/src/main/res/drawable-nodpi/ic_launcher_[<id>_]foreground.png`, `drawable/ic_launcher_background.xml`, `drawable/ic_launcher_[<id>_]monochrome.xml`, `mipmap-anydpi-v26/ic_launcher[_<id>].xml` | The Android launcher icon: an adaptive icon with a Deep sea background, a picture of Poulpy and a themed (monochrome) silhouette layer |
| `app-ios/CoachPoulpy/Assets.xcassets/AppIcon[<Id>].appiconset` | The iOS app icon: 1024 px PNGs for light, dark and tinted; the light and dark are alike, on Deep sea. The alternate icons Settings › App icon offers (`proud`, `annoyed`, `grumpy`, `champion`, `cocky`) are the sets with an id |

**The moves of Home** are baked the same way. A tap on Home's Poulpy plays a move at random: for happy and proud his jump leaning to one side or the other, and for happy also a high jump with a full turn on himself (`createJump` with Height 1, heavy arms and an exact 360° step, as tuned on the workshop's "Jumping around" page; `spin`): `build-poulpy-pictures.mjs` poses the model with `posePoulpy` (in `motion.mjs`, the workshop rig's maths in plain matrices) from `createJump`, paints 60 frames at 48 a second with `paintPoulpy`, and writes one sheet for each mood and move to `design/poulpy-3d/moves/mood_<mood>_<left|right|spin>.png` (6 across and 10 down, each frame 355 × 336 pixels: the still picture's shot at 3 pixels to a point, on a canvas 24 points bigger every way so the jump is not cut off). The sides are as the viewer sees him. The website, Android (assets) and iOS (bundle) read those files from there; `PoulpyMoves` in the shared code says which pictures move, which sheet and which frame, and the script checks its numbers against it. With reduced motion a tap does nothing.

**The champion's celebration** on the end-of-session screen is baked the same way, from `createCheer` (three jumps of joy, his medal swinging on its hinge, confetti flying from each take-off): `posePoulpy` also places the medal and turns each piece of confetti, a small flat card in the room. 72 frames at 20 a second (3.6 s) go to `design/poulpy-3d/moves/pose_champion_cheer_<1-3>.png`, 24 frames to a sheet (6 across and 4 down, each frame 497 × 472 pixels: the still picture's shot at 1.2 pixels to a point, on a canvas 110 pixels bigger every way for the confetti), so no sheet is bigger than a phone's graphics chip takes. The confetti shrinks away in the last 0.7 s and the last frame is his still picture exactly, so he settles on it without a jump; this is the tap on Home. On the finished screen he never pauses: `createCheer({ cycle })` repeats the same four bursts, and with the same pauses (`PARTY` in the script) the whole state comes back exactly every 4.8 s, so 96 frames (`pose_champion_party_<1-4>.png`) loop with no seam and no confetti fade. `PoulpyCheer` and `PoulpyParty` in the shared code say which sheet and which frame, and the script checks their numbers against them.

Moods and poses are 744 × 678 pictures (the 3x of a 248 × 226 frame), so they can replace each other without moving. The loader is in `design/poulpy-3d/loader`, and only there: `swing.png` has the 48 frames of one cycle (8 across and 6 down, each a 240 px square) and `clockwise.png` is the one picture of the clockwise version, whose tentacles trail the same all the time. They show Poulpy's outline seen from straight below, with wider, shorter tentacles hanging loose on his arm springs, in the brand blue, see-through. The website, Android (as assets) and iOS (in the bundle) read those two files. `PoulpyLoader` on Android and iOS shows the frame of the moment and turns it, once every 3.6 s (12 s when animations are reduced), as `LoaderSwing` of the shared code says. It replaces the old Spinning pose. CI runs the generator with `--check` too. The shared `PoulpyDrawing` enum names each drawing, and `PoulpyCoach` picks which one Today shows.

The app icon is the 3D happy blue Poulpy on Deep sea (`theme.dark.background`), the same in light and dark, so it works whichever theme the phone is in; the tinted iOS icon is its greyscale. On iOS he is scaled to fill 80 % of the square. On Android, he is scaled so his farthest point sits on the 66 dp circle every launcher mask keeps; the themed icon is still his drawn silhouette at half strength with the eyes and smile at full. The pictures are painted in plain JavaScript so every machine gets the same pixels; `--check` compares those pixels, not the compressed bytes.

The alternate icons use the mood or pose picture of the same name, scaled the same way, so a pose with props or confetti draws Poulpy smaller. Their ids are those of `AppIcon` in the shared code and never change. Android switches between launcher aliases in the manifest; iOS lists the sets in `ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES` in `app-ios/project.yml`.

## Poulpy in 3D

`poulpy-3d/model.mjs` describes Poulpy's 3D shape in plain JavaScript, with no dependency: the proportions (`SHAPE`), his faces (`FACES`), his six moods (`MOODS`) and how he is lit and shaded (`LOOK`) sit at the top of the file. `poulpy-3d/poses.mjs` gives him his twelve poses, with what he wears and the effects around him. `poulpy-3d/motion.mjs` describes how he moves the same way: `TURN` is his idle, turning around in short steps with soft arms that trail and settle. `JUMP` is his little jump, `CHEER` the champion's jumps of joy, with his medal and confetti, and `WINK` the cocky one's wink, with his stars; they run live in the component workshop and are not written into the glTF files yet. `build-poulpy-pictures.mjs` paints his six moods and twelve poses as PNGs in `poulpy-3d/pictures`, with `poulpy-3d/picture.mjs`, in plain JavaScript so every machine writes the same pixels; CI checks them with `--check` too. `build-poulpy-3d.mjs` writes both as binary glTF files, one per side standing still and one per side moving, with his colours from `tokens.json`:

```sh
node design/build-poulpy-3d.mjs
```

| Generated file | Used by |
| --- | --- |
| `design/poulpy-3d/poulpy_blue.glb` | The blue, smiling side, for any 3D tool or engine |
| `design/poulpy-3d/poulpy_ember.glb` | The ember, grumpy side |
| `design/poulpy-3d/poulpy_blue_turn.glb` | The blue side, rigged, with the animation `turn` |
| `design/poulpy-3d/poulpy_ember_turn.glb` | The ember side, rigged, with the animation `turn` |

The script writes the files itself, byte for byte the same on every machine, and CI runs it with `--check`. The Brand > Poulpy in 3D page of the component workshop ([`web/storybook/stories/brand/Poulpy3d.mdx`](../web/storybook/stories/brand/Poulpy3d.mdx) and the live model) draws the same `model.mjs` and runs the same `motion.mjs` live, lists the rules of the shape and of the move and says how to reuse it; the apps do not use the model yet.

The rules for using Poulpy are in [`web/storybook/stories/brand/`](../web/storybook/stories/brand/); the full brand book is in `web/storybook/stories/brand/brand-book/` and on the Brand > Brand book page of the component workshop, and the decisions behind it are in [`web/storybook/stories/brand/Decisions.mdx`](../web/storybook/stories/brand/Decisions.mdx).

## Icons

Every icon is from [Lucide](https://lucide.dev) ([ADR 0024](../docs/adr/0024-lucide-is-the-icon-set.md)). `icons/` holds a copy of each Lucide icon the product uses, unchanged from the `lucide-static` package at the version `web/ui` pins for `@lucide/vue`, with Lucide's licence (`icons/LICENSE`). `build-icons.mjs` writes them for the apps:

```sh
node design/build-icons.mjs --add calendar-plus   # copy a Lucide icon in, then write
node design/build-icons.mjs                       # write the apps' icons again
```

| Generated file | Used by |
| --- | --- |
| `app-android/.../ui/Lucide.kt` | Compose `ImageVector`s, one per icon (`Icon(Lucide.CalendarPlus, …)`) |
| `app-ios/CoachPoulpy/Lucide.swift` | The `Lucide` enum (`.calendarPlus`), the `LucideIcon` view and `Label(_:lucide:)` |
| `app-ios/CoachPoulpy/Assets.xcassets/Lucide/*.imageset` | iOS template vector images (`LucideCalendarPlus`) |

The websites and the phone mockups take the same icons from `@lucide/vue` by name (`MobileIcon` in the phone stories), so a mockup cannot draw an icon other than the one it names. CI runs `build-icons.mjs --check`, which also fails when an image set is left over from an icon no longer in `icons/`. To move to a newer Lucide, raise `@lucide/vue` in `web/ui` and the docs site, then copy each icon again with `--add`.
