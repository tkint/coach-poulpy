# Profile and settings

This page defines the profile fields, body-measurement history, units and pain or rehab behavior. The agreed screen layouts and states live in the [Mockups gallery](https://tkint.github.io/coach-poulpy/mockups/), rather than being repeated here ([ADR 0016](./adr/0016-mockups-are-plain-html-in-the-repo.md)). Wording rules are in [Inclusive by default](./brand.md#inclusive-by-default).

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
| Body figure | Which body map is drawn for pain and for the muscles an exercise or a workout works | Wider shoulders or Wider hips, so the names describe the drawing and say nothing about gender. It is a drawing only: separate from sex assigned at birth, never used for estimates, and never asked as gender |
| Pain or rehab | Poulpy's blue side for the chosen time | Where it hurts, on the body map, and for how long; see [Character rules](./brand.md#character-rules) |

Suggested for later, not drawn: maximum heart rate override, weekly goal in sessions, dominant side, equipment at home (already a filter), waist and body-fat as optional measurements.

### Why "sex assigned at birth"

The only thing the app needs is a physiological input for estimates, so that is all it asks, and it names it as what it is: assigned at birth, not "biological" or "birth" sex, and not gender. Intersex is offered and Prefer not to say is always available. Gender is never asked, because no feature needs it. See [decision 13](./brand-decisions.md#13-inclusive-by-default) for what was set aside. This wording comes from published inclusive-forms guidance and has not yet been read by LGBTQ+ people; that read is still to do and does not block building.

## Weight and height logging

- Each entry is a date and a value, added from the **Log** button and corrected or deleted from the history list. One entry per day; a second one that day replaces it.
- Height changes slowly, so its screen says a measure every few months is plenty, and teenagers can log as often as they like.
- A weight going up is never red and going down is never green. Poulpy says nothing about it, there is no BMI and no weight goal unless a later issue adds one the person opts into.
- Every chart has a text alternative naming its first and last value.

## Units

The unit system chosen in Settings decides how weight (kg or lb), height (cm or ft and in) and distance (km or mi) are shown and typed. Everything is stored metric, so switching converts and never rewrites; see [ADR 0017](./adr/0017-body-measurements-are-stored-metric-and-shown-in-the-persons-units.md).

## Pain or rehab

A person can say where it hurts and choose how long the pain window lasts: 72 hours by default, 1 week, 2 weeks or until they clear it. During that window Poulpy stays on the blue side in the Patched up pose, entries never count as missed, and workouts are never filtered. The body map and its accessibility requirements are defined in the [design system](./design-system.md#components); the two body figures are chosen independently of sex assigned at birth and are never used for estimates. See [Character rules](./brand.md#character-rules).

## Still open

1. Who reads the sex assigned at birth and pronoun wording from the community (see [decision 13](./brand-decisions.md#13-inclusive-by-default)).
2. Should an optional weight goal line exist later, given the no-judgement rule?
