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
| Chip | 36 high, pill; on = `primary`, off = 1.5 px `outline` | Filters (muscle, equipment, workout type), with `aria-pressed` or selected state; `PoulpyChip` on both platforms |
| Search field | 48 high, pill, 1.5 px `outline`, `primary` while it holds text, a round clear button | `PillSearchField` on both platforms |
| Stepper | Inset `surfaceVariant` box, label, minus and plus round buttons of 44 | Sets, reps, load, rest |
| Pattern badge | 44 round, `primaryContainer`, one icon per movement pattern in `onPrimaryContainer` | Stands in for exercise pictures until real images exist |
| Filter row | Search field, a row of chips, then the count line with one action at its right | `ListFilters` on both platforms, shared by the exercise and workout lists |
| Poulpy card | 24 radius, `primaryContainer`, Poulpy happy on the blue side, a title, a line and one primary button | `PoulpyCard`: no results, an empty list |
| Item menu | A long press (iOS context menu) or ⋮; actions with icons, a divider, then the destructive one | Destructive items and their confirmation are `error` with an icon, never ember; the confirmation says what goes and what stays |
| Plan sheet | 24 radius sheet, a month from Monday; past days dimmed, today outlined, planned days `primary` | `PlanSheet` on both platforms; tick several days, save once |
| Error banner | `error` tint, icon plus text | Never ember |
| Loader | Poulpy from above, a blue outline of eight lobes, one turn every 2.4 s | `PoulpyLoader` on both platforms; replaces spinners |

## Screens

Mockups are plain HTML pages in `design/mockups/` ([ADR 0016](adr/0016-mockups-are-plain-html-in-the-repo.md)): drafts in `drafts/`, validated screens in `agreed/`, each split into `phone/`, `tablet/` and `web/`. Build a screen from its agreed mockup, and get a mockup agreed, through a PR that moves it out of `drafts/`, before building a screen that has none. Until the existing screens are converted, the mockups canvas in the Claude project still holds them: Home (planned, done, nothing planned), Library (workouts, exercises, search, long-press actions, empty), workout page (menu, plan days, delete), exercise detail, builder, the guided player (ready, set, rest, finished), Live and its summary, History (calendar, stats), session detail, and Settings (general, devices, data sources).

## Accessibility

- Touch targets at least 44 by 44.
- Text contrast at least 4.5:1, including small grey labels; 3:1 only for text 24 px and up.
- Every icon-only button has a label; decorative drawings of Poulpy are hidden from screen readers, and a pose that carries meaning has a description.
- Nothing depends on colour alone: mood changes also change Poulpy's face and words, errors carry an icon.
