# Profile and settings

The profile, body logging with charts, and a settings hub that reaches the Devices view. The screens are agreed (see [ADR 0016](./adr/0016-mockups-are-plain-html-in-the-repo.md)) and are built from the pages under Mockups, Agreed, in the site menu. The wording rules behind it are in [Inclusive by default](./brand.md#inclusive-by-default).

## Settings, reworked

Settings stops being two tabs (General, Devices) and becomes a hub, because it now holds more than two things and Devices deserves a row, not a tab.

| Draft | What it shows |
| --- | --- |
| `settings-general` | The hub: a profile card (Poulpy, name, age, height, weight), then **Body** (Weight, Height), **App** (Appearance and units, Devices) and **About** |
| `profile-edit` | The profile fields below, in two groups, About you and Training |
| `profile-sex-sheet` | The sex assigned at birth picker, with its reason |
| `body-weight`, `body-height` | Latest value, chart with 1M / 3M / 1Y / All, change, lowest, highest and the log. Height is drawn in ft and in to show the Imperial unit system |
| `body-log-sheet` | Logging a weight: a stepper in the person's units and a date |
| `settings-preferences` | Theme, Language and Units as selects, moved out of the old General tab |
| `devices-list` | The existing Devices view with a back bar instead of a tab; its detail, add and data source screens stay as agreed |

They replace the old `settings-general` and `devices-list`.

## Profile fields

Everything is optional and stays on the phone. Each field is only here because something uses it.

| Field | Used for | Notes |
| --- | --- | --- |
| Display name | Poulpy's lines and the share card | Default: none, Poulpy says "you" |
| Birthdate | Age, and the heart rate zones | Age is computed, never stored |
| Pronouns | French agreement and Poulpy's lines. A select; choosing Custom shows a text field beneath it | She / her, He / him, They / them, Just my name, Custom (typed by the person) or Prefer not to say. Default neutral; a custom entry is repeated as written and never drives French agreement |
| Sex assigned at birth | Calorie and heart rate estimates only | Female, Male, Intersex, Prefer not to say. Skipped means a neutral average |
| Height, weight | Estimates, and the charts | Logged over time, below |
| Main goal | Which workouts Poulpy suggests first | Stay active, Build strength, Run further, Rehab, Something else |
| Experience | Starting loads and plan suggestions | Just starting, Regular, Experienced |
| Resting heart rate | Heart rate zones | Typed, or read from a band later |
| Body figure (draft) | Which body map is drawn for pain and, later, for the muscles a workout uses | Female or Male. It is a drawing only: separate from sex assigned at birth, never used for estimates, and never asked as gender |
| Pain or rehab | Poulpy's blue side for 72 hours | Where it hurts and until when; see [Character rules](./brand.md#character-rules) |

Suggested for later, not drawn: maximum heart rate override, weekly goal in sessions, dominant side, equipment at home (already a filter), waist and body-fat as optional measurements.

### Why "sex assigned at birth"

The only thing the app needs is a physiological input for estimates, so that is all it asks, and it names it as what it is: assigned at birth, not "biological" or "birth" sex, and not gender. Intersex is offered and Prefer not to say is always available. Gender is never asked, because no feature needs it. See [decision 13](./brand-decisions.md#13-inclusive-by-default) for what was set aside. This wording comes from published inclusive-forms guidance and has not yet been read by LGBTQ+ people; that read is still to do and does not block building.

## Weight and height logging

- Each entry is a date and a value, added from the **Log** button and corrected or deleted from the history list. One entry per day; a second one that day replaces it.
- Charts are lines with a dot per entry on the one accent colour. The ranges are 1M, 3M, 1Y and All. Three tiles under the weight chart give the change over the range, the lowest and the highest.
- Height changes slowly, so its screen says a measure every few months is plenty, and teenagers can log as often as they like.
- A weight going up is never red and going down is never green. Poulpy says nothing about it, there is no BMI and no weight goal unless a later issue adds one the person opts into.
- A line chart is a new component, `line-chart`, drawn in the mockup with an SVG. Every chart has a text alternative naming its first and last value.

## Units

The unit system chosen in Settings decides how weight (kg or lb), height (cm or ft and in) and distance (km or mi) are shown and typed. Everything is stored metric, so switching converts and never rewrites; see [ADR 0017](./adr/0017-body-measurements-are-stored-metric-and-shown-in-the-persons-units.md). The weight pages are drawn Metric and the height page Imperial so both can be reviewed.

## Pain or rehab (draft)

The row is in the profile but nothing sets it yet. The drafts are `profile-edit` (the row filled in, with Poulpy's note) and `profile-pain-sheet` (a front and back body map where it hurts, left and right apart, then for how long: 72 hours by default, 1 week, 2 weeks or until the person clears it). The body map is a new component, `body-map`, meant to be reused later to show the areas an exercise works. It has a female and a male drawing, picked in `profile-body-figure-sheet`, under Mockups, Drafts. Nothing is built until they are agreed. The rules they follow are in [Character rules](./brand.md#character-rules): pain or rehab always shows the blue side and the Patched up pose, never ember, and entries inside the window never count as missed ([decision 8](./brand-decisions.md)).

## Still open

1. Who reads the sex assigned at birth and pronoun wording from the community (see [decision 13](./brand-decisions.md#13-inclusive-by-default)).
2. Should an optional weight goal line exist later, given the no-judgement rule?
