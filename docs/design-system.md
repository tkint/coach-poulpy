# Design system

The app is calm and rounded, like the plush: one blue accent for actions, white or near-white surfaces, and only Poulpy and the Home header tint with his mood. Values live in [`design/tokens.json`](../design/tokens.json) and are generated into each platform (see [`design/README.md`](../design/README.md)); this page says how to use them. The live swatches are on the site's Tokens page.

## Colour roles

Use roles, never raw palette steps, in screens. Each role has a light and a dark value.

| Role | Use it for |
| --- | --- |
| `primary` / `onPrimary` | The main action on a screen (Start session, Save), selected chips, links, progress |
| `primaryContainer` / `onPrimaryContainer` | Tonal buttons, selected rows, Poulpy's speech on the blue side |
| `grumpy` / `onGrumpy` | The one action offered while Poulpy is on the ember side (Catch up, Flip him back) |
| `grumpyContainer` / `onGrumpyContainer` | The Home header and cards while Poulpy is on the ember side |
| `background`, `surface`, `surfaceVariant` | Screen, cards and sheets, inset fields and steppers |
| `onSurfaceVariant`, `outline` | Secondary text and captions; card borders and dividers |
| `success` | A confirmed save or a finished phase, with a check icon |
| `error` / `onError` | Errors only: crimson, always with an icon and words |
| `heat1`, `heat2`, `heat3` | How much a workout works each muscle on the body map, from a little to most. Orange, on the body map only: never for Poulpy's moods |
| `pain` | A worked area that hurts, outlined on the body map. Purple, so it reads apart from the heat and is never mistaken for an error |

Rules:

- `poulpy.blue`, `poulpy.ember` and `poulpy.ink` are Poulpy's fabric and never change between light and dark.
- The plush blue and ember are too light for white text: buttons use `primary` and `grumpy`.
- Ember means "a session slipped", never "something broke". An error is crimson, so a cross Poulpy is never read as a failure.
- Colours that must be told apart also differ in lightness, not hue alone.

## Mood shades

Six shades, from `mood.*`, only on Poulpy himself, the Home header tint and mood-coloured charts:

| Side | Shades, calmest to strongest |
| --- | --- |
| Blue (on track) | `recovering`, `happy`, `proud` |
| Ember (a session slipped) | `annoyed`, `grumpy`, `fuming` |

Which shade shows when is in [Brand](./brand.md#moods).

## Type

Baloo 2 for display and titles, Nunito for everything else. Platforms map each role to a system text style so Dynamic Type and font scaling keep working (`.font(.poulpy(.headline))` on iOS, `MaterialTheme.typography` on Android).

| Role | Font | Size / line | Use |
| --- | --- | --- | --- |
| `display` | Baloo 2 800 | 40 / 44 | Poulpy's line on Home, end-of-session headline |
| `title` | Baloo 2 700 | 28 / 32 | Screen titles, workout name on its page |
| `headline` | Baloo 2 700 | 21 / 25 | Section heads, the current exercise in the player |
| `body` | Nunito 500 | 16 / 24 | Everything you read |
| `label` | Nunito 800, tracked 0.08 em, upper case | 12.5 / 16 | Eyebrows, tab labels, "Next up" |
| `timer` | Nunito 800, tabular figures | 64 / 64 | Timers and rep counts, which must not jitter |

**Words never break.** Translations run longer than English (« Bibliothèque », « Échauffement »), so text is laid out for any length:

- Text in a space of fixed width (tab labels, chips, stat tiles, stepper values) stays on one line and shrinks to fit, never below 9 sp / pt; past that it ends in an ellipsis. `FitText` on Android, `.lineLimit(1).minimumScaleFactor(…)` on iOS, whose tab bar already does it.
- Buttons wrap their label between words rather than shrink it.
- Every other text wraps between words only, never inside one, and never hyphenates.

## Space, radius, motion

- Spacing steps: 4, 8, 12, 16, 24, 32, 48 (`xs` to `xxxl`). Pick a step; never a value between them.
- Radius: 8 for chips and small fields, 10 for cards and rows, 14 for sheets, pill for buttons. Small cards stay close to square so their text reads first.
- Controls stay compact: a standard button is 44 high, a large one 52, an icon button 40 (its touch area stays 44), a small button 34. The side gutter of a screen is 16, and rows are 56 high with a 32 to 40 avatar. In the apps these come from one place, `PoulpySize` (generated from `design/tokens.json`, next to `PoulpySpace` and `PoulpyRadius`); a screen never writes a literal height, gutter or corner radius.
- Motion: the flip (700 ms with overshoot) is the one signature move, used only when Poulpy changes side. Mood shades fade in 300 ms. Everything else is 150 to 250 ms and plain. With reduced motion on, the loader slows to one turn in 10 s.

## Components

| Component | Shape | Notes |
| --- | --- | --- |
| Primary button | 44 high, pill, `primary` | One per screen |
| Tonal button | 44 high, pill, `primaryContainer` | Secondary actions |
| Outline button | 44 high, pill, 2 px `outline` | Cancel, less important choices |
| Grumpy button | 44 high, pill, `grumpy` | Only the catch-up action while Poulpy is ember |
| Chip | 32 high, pill; on = `primary`, off = 1.5 px `outline` | Filters (muscle, equipment, format), with `aria-pressed` or selected state; `PoulpyChip` on both platforms |
| Choosing one value | A select field that opens a sheet for a longer list, or a native menu for a short one | Pills and chips are for filters and for view switches; a form asks for a value with a select, never a row of pills |
| Search field | 44 high, pill, 1.5 px `outline`, `primary` while it holds text, a round clear button | `PillSearchField` on both platforms |
| Stepper | Inset `surfaceVariant` box, label, minus and plus round buttons of 44 | Sets, reps, load, rest |
| Pattern badge | 44 round, `primaryContainer`, one icon per movement pattern in `onPrimaryContainer` | Stands in for exercise pictures until real images exist |
| Filter row | Search field, a row of chips, then the count line with one action at its right | `ListFilters` on both platforms, shared by the exercise and workout lists |
| Poulpy card | 14 radius, `primaryContainer`, Poulpy happy on the blue side, a title, a line and one primary button | `PoulpyCard`: no results, an empty list |
| Item menu | A long press (iOS context menu) or ⋮; actions with icons, a divider, then the destructive one | Destructive items and their confirmation are `error` with an icon, never ember; the confirmation says what goes and what stays |
| Plan sheet | 14 radius sheet, a month from Monday; past days dimmed, today outlined, planned days `primary` | `PlanSheet` on both platforms; tick several days, save once |
| Error banner | `error` tint, icon plus text | Never ember |
| Loader | Poulpy from above, a blue outline of eight lobes, one turn every 2.4 s | `PoulpyLoader` on both platforms; replaces spinners |

## Base fields

The fields every form is built from. Reference mockups: `profile-edit`, the three `profile-*-sheet` pages, `body-log-sheet`, `body-log-height-sheet`. CSS classes are in `design/mockups/shared/mockup.css`. Build every new form from these, and move old screens to them as they are touched.

| Field | Shape | Behaviour |
| --- | --- | --- |
| [Text field](https://tkint.github.io/coach-poulpy/mobile-components#text-field) | 46 high, 8 radius, 1.5 px `outline`, label above, `primary` outline on focus | Free text: a name. A note under it in `onSurfaceVariant` |
| [Select field](https://tkint.github.io/coach-poulpy/mobile-components#select-field) | Same box, the chosen value (or a muted "Not set") and a chevron | Tapping opens a sheet; nothing else is shown on the screen. Never a row of pills |
| [Option sheet](https://tkint.github.io/coach-poulpy/mobile-components#option-sheet) | 14 radius sheet, a title, one row per choice | Single choice: a radio and the sheet closes on tap. Several choices: checkboxes and a Done button. A choice that needs text (Custom pronouns) shows its text field inside the sheet |
| [Number field](https://tkint.github.io/coach-poulpy/mobile-components#number-field) | Text field with a unit at its right (`bpm`, `kg`, `cm`) | Selects all its text on focus, opens the numeric keyboard, checks the range on leaving. Optional round − and + buttons on both sides (the `number-entry` pattern) for small nudges. Imperial height is two number fields, ft and in. Every numeric value in the app uses it, including the builder's sets, reps, rest between sets, rest and weight (`builder-sets-edit-step`, `builder-circuit-edit-step`); the − and + step by the same amount as before, and the typed value keeps the same limits |
| [Step slider](https://tkint.github.io/coach-poulpy/mobile-components#step-slider) | A bar of dots, `•--•--•--•--•`, the current label above it, the two ends named below | A fixed scale of five or fewer named steps, inside an option sheet |
| Password field (draft, #272) | Text field with a show or hide eye button inside its right end; a hint under it (`8 characters or more.`) | `password-field` in the mockups. Errors under a field are `error` text with an icon and the field outline turns `error`; a message about the whole form is an error card (`error` outline and text on `surfaceVariant`, with an icon) |
| [Line chart](https://tkint.github.io/coach-poulpy/mobile-components#line-chart) | Grid lines behind the line, the unit at the top of the axis, a value at each grid line, the date range under it | No grid colour that implies good or bad; a weight going up is never red |
| [Empty chart card](https://tkint.github.io/coach-poulpy/mobile-components#empty-chart-card) | `PoulpyCard` shape with Poulpy on the blue side, a line and the one action that adds the first entry | Shown instead of the chart when nothing is logged |
| Body map | A front and a back figure, drawn in the build the person chose in the profile, as a muscle chart with transparent skin, side by side, each area a shape that fills with `primary` when picked; left and right are separate areas | Every area is a checkbox with a name ("Left knee"), so a screen reader and the text summary under the figure say the same. Left is the person's left: the right half of the front figure and the left half of the back one. Used to say where it hurts, and to show what an exercise works |
| Body map heat map ([#244](https://github.com/tkint/coach-poulpy/issues/244), built in #326) | The body map read-only for a whole workout: areas it doesn't work are outlines only, worked areas fill with `heat1`, `heat2` or `heat3` by sets per muscle group compared with the most-worked one (below 40 %, from 40 %, from 75 %); a worked area that hurts gets a 3 px `pain` outline on a `background` halo, drawn above its neighbours; a Less / More legend, with Hurts when something hurts | The figure is one image whose label is the text summary ("shoulders and triceps most, then chest, back and biceps, a little core"), and the heat steps differ in lightness, so nothing depends on colour alone. On the workout page; the format and equipment badges sit under the workout name in the sheet |
| Details sheet ([#244](https://github.com/tkint/coach-poulpy/issues/244), built in #326) | 14 radius sheet over the body map with no scrim; a handle, the workout name at its top, its content, then the action bar | Three stops (peek, half, full), the handle drags or taps between them: dragged down it shows only the name and actions under a full-screen map; half up by default; full screen its content scrolls, and scrolling down past its top drags the sheet back down. The map keeps the size it has at half while the sheet goes higher. The back and menu buttons float above it at every stop |
| Plan summary ([#244](https://github.com/tkint/coach-poulpy/issues/244), built in #326) | `primaryContainer` row: a calendar icon, "3 sessions planned" over "Next on Fri 30 Oct", and a Change days button | Replaces one pill per planned day on the workout page |

## Screens

Mockups are plain HTML pages in `design/mockups/` ([ADR 0016](adr/0016-mockups-are-plain-html-in-the-repo.md)): drafts in `drafts/`, validated screens in `agreed/`, each split into `phone/`, `tablet/` and `web/`. The docs site shows them on the [Mockups](https://tkint.github.io/coach-poulpy/mockups/) pages, one page per screen, in light or dark. Build a screen from its agreed mockup, and get a mockup agreed, through a PR that moves it out of `drafts/`, before building a screen that has none. The player draws the same screens on both phones (`player-ready`, `player-set`, `player-circuit-round`, `player-repeat-in-sets`, `player-warm-up`, `player-rest`, `player-recover`): pause and end on the bar over the step count, Poulpy straining on an exercise and glancing aside inside the rest ring, a rep target as one big number with its unit under it, a line saying what comes next, and −15 s and +15 s under the ring to lengthen or shorten that one rest. The screen sits on the `background` role, not `surface`, so the cards on it stand out. The finished screen names the workout and its date, then shows three stats picked from what the exercises logged: Time always, then sets and weight lifted for sets and weights, distance and pace for runs and walks, and never heart rate; a stat with nothing logged, such as the weight of a bodyweight session, is left out (`player-finished` and `player-finished-run` on the Mockups pages). CI runs `node design/check-mockups.mjs` on every mockup: no literal colours, no px value where a spacing, radius or headline token has that size, no missing link, no script or style from outside the repo (the brand fonts apart), and the title, screen and section meta tags in place.

## Websites

The landing site, the web app, the web mockups and the docs site share one package, `web/ui`: DaisyUI with the Poulpy theme and a few Vue components ([ADR 0023](adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md)). The live samples and the class of each component are on the [Web components](https://tkint.github.io/coach-poulpy/web-components) page.

- Use the shared components (`CpButton`, `CpChip`...) in Vue, and their classes in a web mockup; never restyle a DaisyUI class in an app. A missing shape goes in `web/ui`, with its sample and its section on the Web components page in the same PR.
- Every DaisyUI class carries the `cp-` prefix (`cp-btn cp-btn-primary`). Our own classes (`cp-chip`, `cp-stepper`, `cp-type-label`...) never reuse a DaisyUI name, so the two cannot clash.
- The roles above keep their meaning: `cp-btn-primary` is `primary`, `cp-btn-secondary` is the tonal `primaryContainer`, `cp-btn-accent` is `grumpy` and only for the catch-up action. Never use DaisyUI's `warning` classes: ember is not a warning, and a problem is an `error`.
- A web mockup that fills the browser window, such as one with a `CpDrawer` sidebar, adds `<meta name="mockup:viewport" content="800">`: the docs site then shows it in a window of that height and the page scrolls inside it, since a frame sized to a page as tall as the frame would grow forever. `design/mockups/shared/mockup-drawer.js` opens such a drawer on a large screen. Web mockups link `web/ui/src/tokens.css` then `web/ui/poulpy-ui.css`. After using a class no component or web mockup used before, run `npm run build -w ui` in `web/` and commit `poulpy-ui.css`; CI checks it.
- Phone and tablet mockups keep `design/mockups/shared/mockup.css`: they draw the native screens.
- Icons on the websites and in web mockups come from Lucide, as in the [Icons](#icons) section ([ADR 0024](adr/0024-lucide-is-the-icon-set.md)).

## Icons

Every icon is from [Lucide](https://lucide.dev), on every platform ([ADR 0024](adr/0024-lucide-is-the-icon-set.md)). Use its name for the action, never a home-made drawing of one Lucide already has. Icons take their colour from the roles above, and an error keeps its icon. The existing icons are being moved to Lucide in a follow-up issue; the Android and iOS Lucide packages are chosen there.

## Accessibility

- Touch targets at least 44 by 44; a visually smaller control, such as the 40 icon button, extends its touch area to 44.
- Text contrast at least 4.5:1, including small grey labels; 3:1 only for text 24 px and up.
- Every icon-only button has a label; decorative drawings of Poulpy are hidden from screen readers, and a pose that carries meaning has a description.
- Nothing depends on colour alone: mood changes also change Poulpy's face and words, errors carry an icon.
