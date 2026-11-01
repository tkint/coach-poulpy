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
- Radius: 10 for chips and small fields, 16 for cards, 24 for sheets, pill for buttons.
- Motion: the flip (700 ms with overshoot) is the one signature move, used only when Poulpy changes side. Mood shades fade in 300 ms. Everything else is 150 to 250 ms and plain. With reduced motion on, the loader slows to one turn in 10 s.

## Components

| Component | Shape | Notes |
| --- | --- | --- |
| Primary button | 56 high, pill, `primary` | One per screen |
| Tonal button | 56 high, pill, `primaryContainer` | Secondary actions |
| Outline button | 56 high, pill, 2 px `outline` | Cancel, less important choices |
| Grumpy button | 56 high, pill, `grumpy` | Only the catch-up action while Poulpy is ember |
| Chip | 36 high, pill; on = `primary`, off = 1.5 px `outline` | Filters (muscle, equipment, format), with `aria-pressed` or selected state; `PoulpyChip` on both platforms |
| Choosing one value | A select field that opens a sheet for a longer list, or a native menu for a short one | Pills and chips are for filters and for view switches; a form asks for a value with a select, never a row of pills |
| Search field | 48 high, pill, 1.5 px `outline`, `primary` while it holds text, a round clear button | `PillSearchField` on both platforms |
| Stepper | Inset `surfaceVariant` box, label, minus and plus round buttons of 44 | Sets, reps, load, rest |
| Pattern badge | 44 round, `primaryContainer`, one icon per movement pattern in `onPrimaryContainer` | Stands in for exercise pictures until real images exist |
| Filter row | Search field, a row of chips, then the count line with one action at its right | `ListFilters` on both platforms, shared by the exercise and workout lists |
| Poulpy card | 24 radius, `primaryContainer`, Poulpy happy on the blue side, a title, a line and one primary button | `PoulpyCard`: no results, an empty list |
| Item menu | A long press (iOS context menu) or ⋮; actions with icons, a divider, then the destructive one | Destructive items and their confirmation are `error` with an icon, never ember; the confirmation says what goes and what stays |
| Plan sheet | 24 radius sheet, a month from Monday; past days dimmed, today outlined, planned days `primary` | `PlanSheet` on both platforms; tick several days, save once |
| Error banner | `error` tint, icon plus text | Never ember |
| Loader | Poulpy from above, a blue outline of eight lobes, one turn every 2.4 s | `PoulpyLoader` on both platforms; replaces spinners |

## Base fields

The fields every form is built from. Reference mockups: `profile-edit`, the three `profile-*-sheet` pages, `body-log-sheet`, `body-log-height-sheet`. CSS classes are in `design/mockups/shared/mockup.css`. Build every new form from these, and move old screens to them as they are touched.

| Field | Shape | Behaviour |
| --- | --- | --- |
| Text field | 52 high, 10 radius, 1.5 px `outline`, label above, `primary` outline on focus | Free text: a name. A note under it in `onSurfaceVariant` |
| Select field | Same box, the chosen value (or a muted "Not set") and a chevron | Tapping opens a sheet; nothing else is shown on the screen. Never a row of pills |
| Option sheet | 24 radius sheet, a title, one row per choice | Single choice: a radio and the sheet closes on tap. Several choices: checkboxes and a Done button. A choice that needs text (Custom pronouns) shows its text field inside the sheet |
| Number field | Text field with a unit at its right (`bpm`, `kg`, `cm`) | Selects all its text on focus, opens the numeric keyboard, checks the range on leaving. Optional round − and + buttons on both sides (the `number-entry` pattern) for small nudges. Imperial height is two number fields, ft and in |
| Step slider | A bar of dots, `•--•--•--•--•`, the current label above it, the two ends named below | A fixed scale of five or fewer named steps, inside an option sheet |
| Line chart | Grid lines behind the line, the unit at the top of the axis, a value at each grid line, the date range under it | No grid colour that implies good or bad; a weight going up is never red |
| Empty chart card | `PoulpyCard` shape with Poulpy on the blue side, a line and the one action that adds the first entry | Shown instead of the chart when nothing is logged |
| Body map (draft) | A front and a back figure, drawn female or male as the person chose in the profile, side by side, each area a shape that fills with `primary` when picked; left and right are separate areas | Every area is a checkbox with a name ("Left knee"), so a screen reader and the text summary under the figure say the same. Left is the person's left: the right half of the front figure and the left half of the back one. Used to say where it hurts, and meant to show what an exercise works later |

## Screens

Mockups are plain HTML pages in `design/mockups/` ([ADR 0016](adr/0016-mockups-are-plain-html-in-the-repo.md)): drafts in `drafts/`, validated screens in `agreed/`, each split into `phone/`, `tablet/` and `web/`. The docs site shows them on the [Mockups](https://tkint.github.io/coach-poulpy/mockups/) pages, one page per screen, in light or dark. Build a screen from its agreed mockup, and get a mockup agreed, through a PR that moves it out of `drafts/`, before building a screen that has none. The player draws the same screens on both phones (`player-ready`, `player-set`, `player-circuit-round`, `player-repeat-in-sets`, `player-warm-up`, `player-rest`, `player-recover`): pause and end on the bar over the step count, Poulpy straining on an exercise and glancing aside inside the rest ring, a rep target as one big number with its unit under it, a line saying what comes next, and −15 s and +15 s under the ring to lengthen or shorten that one rest. The screen sits on the `background` role, not `surface`, so the cards on it stand out. The finished screen names the workout and its date, then shows three stats picked from what the exercises logged: Time always, then sets and weight lifted for sets and weights, distance and pace for runs and walks, and never heart rate; a stat with nothing logged, such as the weight of a bodyweight session, is left out (`player-finished` and `player-finished-run` on the Mockups pages). CI runs `node design/check-mockups.mjs` on every mockup: no literal colours, no px value where a spacing, radius or headline token has that size, no missing link, no script or style from outside the repo (the brand fonts apart), and the title, screen and section meta tags in place.

## Accessibility

- Touch targets at least 44 by 44.
- Text contrast at least 4.5:1, including small grey labels; 3:1 only for text 24 px and up.
- Every icon-only button has a label; decorative drawings of Poulpy are hidden from screen readers, and a pose that carries meaning has a description.
- Nothing depends on colour alone: mood changes also change Poulpy's face and words, errors carry an icon.
