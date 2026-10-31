# English source: conventions and translator notes

English is the source language: every other language translates from it. This page holds the rules every translation follows and a note for each message whose meaning isn't obvious from the English alone. Fink shows only the id and the text, so look an id up here when in doubt.

## How a message is written

- **Ids** are `<group>.<camelCaseName>`: the group is the screen or area (`library.sortPlanned`, `muscle.chest`). Catalog ids are `<slug>.<field>`, with `<slug>.instructions.<n>` for each instruction in order. No id is the prefix of another.
- **Placeholders** are named ICU arguments. `{name}` is text, `{count, number}` is a whole number. Keep every placeholder of the English, with the same name; move it wherever the sentence needs it.
- **Plurals** are ICU `plural`: `{count, plural, one {# session} other {# sessions}}`. `#` is the number. Give every form your language needs (French: `one`, `many`, `other`); Fink lists them.
- **Apostrophes, ellipses and spaces** need no care: type ' and ... as usual. After each merge they become ’ and …, and the non-breaking spaces a language needs are added (French: before `: ; ! ? %` and inside « »).
- **Product names** stay as they are: Coach Poulpy, Poulpy.

## Namespaces

Fink groups messages in three namespaces:

| Namespace | What it holds |
| --- | --- |
| `app` | The app’s screens |
| `domain` | Words shown everywhere: muscles, equipment, units, workout types, step roles, Poulpy’s lines |
| `catalog` | Exercise names, instructions, variation notes and search aliases |

## Translator notes

Messages not listed here mean what their English says.

### app

| Id | Note |
| --- | --- |
| `app.name` | The product name. Never translated. |
| `common.loading` | Spoken by screen readers while a screen loads; never seen. |
| `tab.*` | Tab labels at the bottom of the screen: about 12 characters fit before the text shrinks. One word is best. |
| `tab.live` | The tab for a session recorded from connected devices (treadmill, heart rate band), as it happens. |
| `home.inDays` | `{date}` is a weekday and date, already formatted for the language. |
| `library.sort` | iOS only: the label of the sort menu. |
| `library.all`, `exercises.all` | Filter chip that clears the filter. Agrees with “workouts” and “exercises” respectively. |
| `library.planned`, `library.lastDone`, `workout.planned`, `history.todayDay` | `{date}` is a short date or “today”, already formatted. |
| `exercise.variationPosition`, `exercise.variationsA11y` | `{position}` is already formatted, like “2 of 5”. |
| `builder.less`, `builder.more` | iOS only: buttons that lower or raise a value. Android uses `builder.lessNamed`/`builder.moreNamed`, where `{label}` names the value. |
| `builder.open`, `builder.moreActions` | Accessibility labels; `{name}` is the step’s name. |
| `builder.editValue` | Accessibility label for typing a value; `{label}` names it (sets, weight…). |
| `builder.repeat`, `builder.addRepeat` | A repeat is a group of steps done several times in a row, like intervals. Not “repetition” of a single movement (that is a rep). |
| `builder.untilPressed`, `builder.untilEnd` | A step with no set length: it lasts until the person presses the button, or, on the last step, until the session ends. |
| `player.summary` | `{duration}` is already formatted, like “24 min”. |
| `treadmill.phase` | A block is one part of a treadmill program, like “5 min at 8 km/h”. |
| `treadmill.targetSpeed`, `treadmill.remaining` | `{speed}` and `{time}` are already formatted with their unit. |
| `history.sessionsUnit` | The word alone, shown under a big number. |
| `history.tentaclesLit` | Out of Poulpy’s eight tentacles; see the Poulpy notes below. |
| `live.waitingBody`, `live.connectingOne`, `devices.forgetTitle`, `devices.usedInstead` | `{name}` is a device’s name. |
| `live.finishStillBody`, `live.finishStoppedBody`, `live.finishLostBody` | `{endTime}` is a clock time, already formatted. |
| `live.finishClosedBody` | `{endTime}` is how far into the session it stopped, like “12:40”, already formatted. |
| `live.notificationPaused` | `{elapsed}` is the time recorded so far, like “12:40”. |
| `devices.openSettings` | iOS only: opens the phone’s Settings app. Use the name the phone uses. |
| `devices.allowBluetooth` | Android only. |
| `kind.footPod` | A small sensor clipped to the shoe that measures pace and cadence. |
| `settings.languageNextLaunch` | iOS only. |

### domain

| Id | Note |
| --- | --- |
| `muscleArea.*` | Broad body areas for filter chips; shorter than `muscle.*`. |
| `pattern.*` | Movement patterns, the families exercises are grouped in. `pattern.hinge` bends at the hips with a flat back. `pattern.carry` walks holding a load. |
| `role.*` | The tag on a step in a workout. `role.recover` is easy effort between hard efforts, not a full stop. |
| `label.session` | Shown for a session whose workout was deleted. |
| `label.or`, `label.and` | Join filter labels: “Dumbbells or kettlebell”. |
| `label.demoIntervals` | The name of a built-in treadmill program for trying the app. |
| `count.position` | “3 of 5”, used in many places. |
| `unit.*` | Unit formats. `unit.minutesSeconds` and `unit.hoursMinutes`: the second argument is already two digits (“1 h 05”). |
| `workout.deleteTitle` | `{name}` is the workout’s name, chosen by the person. |
| `poulpy.*` | Poulpy, the octopus mascot, speaking in the first person. He lights one of his eight tentacles per finished session; eight make a full octopus. He is warm and never guilt-trips. His voice is described on the [brand](../../docs/brand.md) page. |

### catalog

| Id | Note |
| --- | --- |
| `<slug>.aliases` | Other names people might search for, comma-separated, never shown. Optional per language: add the words people really type, not a translation of the English list. |
| `<slug>.variationNote` | How this variation differs from its neighbours, shown under the name. |
| `<slug>.instructions.<n>` | One step of how to do the exercise, in order, addressed to the person. |
