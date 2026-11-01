# Profile and settings

A proposal for review on this page and the draft pages under Mockups, Drafts, in the site menu: the profile, body logging with charts, and a settings hub that reaches the Devices view. Nothing here is built until the drafts are agreed (see [ADR 0016](./adr/0016-mockups-are-plain-html-in-the-repo.md)). The wording rules behind it are in [Inclusive by default](./brand.md#inclusive-by-default).

## Settings, reworked

Settings stops being two tabs (General, Devices) and becomes a hub, because it now holds more than two things and Devices deserves a row, not a tab.

| Draft | What it shows |
| --- | --- |
| `settings-general` | The hub: a profile card (Poulpy, name, age, height, weight), then **Body** (Weight, Height), **App** (Appearance and units, Devices) and **About** |
| `profile-edit` | The profile fields below, in two groups, About you and Training |
| `profile-sex-sheet` | The sex assigned at birth picker, with its reason |
| `body-weight`, `body-height` | Latest value, chart with 1M / 3M / 1Y / All, change, lowest, highest and the log. Height is drawn in ft and in to show the Imperial unit system |
| `body-log-sheet` | Logging a weight: a stepper in the person's units and a date |
| `settings-preferences` | Theme, Language and Units, moved out of the old General tab |
| `devices-list` | The existing Devices view with a back bar instead of a tab; its detail, add and data source screens stay as agreed |

When the drafts are agreed, they replace `settings-general` and `devices-list` in `agreed/`.

## Profile fields

Everything is optional and stays on the phone. Each field is only here because something uses it.

| Field | Used for | Notes |
| --- | --- | --- |
| Display name | Poulpy's lines and the share card | Default: none, Poulpy says "you" |
| Birthdate | Age, and the heart rate zones | Age is computed, never stored |
| Pronouns | French agreement and Poulpy's lines | She / her, He / him, They / them, Just my name. Default neutral |
| Sex assigned at birth | Calorie and heart rate estimates only | Female, Male, Intersex, Prefer not to say. Skipped means a neutral average |
| Height, weight | Estimates, and the charts | Logged over time, below |
| Main goal | Which workouts Poulpy suggests first | Stay active, Build strength, Run further, Rehab, Something else |
| Experience | Starting loads and plan suggestions | Just starting, Regular, Experienced |
| Resting heart rate | Heart rate zones | Typed, or read from a band later |
| Pain or rehab | Poulpy's blue side for 72 hours | Where it hurts and until when; see [Character rules](./brand.md#character-rules) |

Suggested for later, not drawn: maximum heart rate override, weekly goal in sessions, dominant side, equipment at home (already a filter), waist and body-fat as optional measurements.

### Why "sex assigned at birth"

The only thing the app needs is a physiological input for estimates, so that is all it asks, and it names it as what it is: assigned at birth, not "biological" or "birth" sex, and not gender. Intersex is offered and Prefer not to say is always available. Gender is never asked, because no feature needs it. See [decision 13](./brand-decisions.md#13-inclusive-by-default-proposed) for what was set aside. This wording comes from published inclusive-forms guidance and still needs reading by LGBTQ+ people before it ships.

## Weight and height logging

- Each entry is a date and a value, added from the **Log** button and corrected or deleted from the history list. One entry per day; a second one that day replaces it.
- Charts are lines with a dot per entry on the one accent colour. The ranges are 1M, 3M, 1Y and All. Three tiles under the weight chart give the change over the range, the lowest and the highest.
- Height changes slowly, so its screen says a measure every few months is plenty, and teenagers can log as often as they like.
- A weight going up is never red and going down is never green. Poulpy says nothing about it, there is no BMI and no weight goal unless a later issue adds one the person opts into.
- A line chart is a new component, `line-chart`, drawn in the mockup with an SVG. Every chart has a text alternative naming its first and last value.

## Units

The unit system chosen in Settings decides how weight (kg or lb), height (cm or ft and in) and distance (km or mi) are shown and typed. Everything is stored metric, so switching converts and never rewrites; see [ADR 0017](./adr/0017-body-measurements-are-stored-metric-and-shown-in-the-persons-units.md). The weight pages are drawn Metric and the height page Imperial so both can be reviewed.

## Questions for the owner

1. Is the hub right, or should Profile stay a tab beside Devices?
2. Are the four sex assigned at birth options and the pronoun options right, and who reads them from the community?
3. Which of the suggested extra fields (goal, experience, resting heart rate, pain or rehab) should be built first, and which dropped?
4. Should an optional weight goal line exist later, given the no-judgement rule?
