# The workout file

A guest can send a **workout** to another guest as a file, through a messaging app, email, AirDrop or Nearby Share. No account or server is involved. The file is a `.poulpyworkout`.

## What is in it

JSON in an envelope:

```json
{
  "format": "coachpoulpy.workout",
  "schemaVersion": 1,
  "workout": { "id": "…", "version": 4, "name": "Morning circuit", "format": "CIRCUIT", "parts": [ … ] }
}
```

- `format` is always `coachpoulpy.workout`. A file without it is not a workout.
- `schemaVersion` is a whole number. This app reads files up to the version it knows; a newer file says to update the app. Later versions keep reading older files.
- `workout` is the app's own workout, as the phone stores it: parts, steps, groups, prescriptions. Exercises are catalog slugs ([ADR 0008](adr/0008-curated-catalog-with-stable-slugs.md)), never copies of the catalog, so the file stays small and the receiver's catalog supplies the names and instructions.

The file is named after the workout, lower case with dashes (`morning-circuit.poulpyworkout`). Its MIME type is `application/vnd.coachpoulpy.workout+json`, and on iOS its type identifier is `ch.coachpoulpy.workout`, conforming to `public.json`. A dedicated extension, not `.json`, lets the app be suggested as the handler.

## Importing

Importing **makes a copy**, saved when the person saves it in the builder: a new id made on the phone ([ADR 0005](adr/0005-ids-are-uuidv7-made-on-the-device.md)), version 1, the same name and parts. It never overwrites or merges into a workout of the library, and saving the same file twice makes two workouts. When the name is already in the library, the copy gets the usual copy name ("Upper body (copy)").

Everything is checked before anything is saved, and a refused file saves nothing:

| Problem | What the person reads |
| --- | --- |
| Not JSON, damaged, or not shaped like a workout | The file can't be read. |
| Another kind of file | It isn't a Coach Poulpy workout. |
| `schemaVersion` newer than the app | Made with a newer version; update the app. |
| A slug the catalog doesn't know | Names the unknown exercises; update the app. A **retired exercise** still imports. |
| The workout breaks the rules of parts, repeats and formats | Incomplete or broken, so it can't be added. |

The logic is shared Kotlin (`WorkoutFile`, `WorkoutImport`) with tests; Android and iOS only hand over bytes and show the builder, or why a file was refused.

## On the phone

- **Export** is in the menu of the workout page and opens the same sheet as a session's, with two choices: **Send to a contact**, which opens the system share sheet with the file (a message, an email or any app), and **Save to device**, which keeps the file in a folder the person picks. Both are on Android and iOS.
- Opening a `.poulpyworkout` file from another app opens it in the builder as an unsaved copy ([#152](https://github.com/tkint/coach-poulpy/issues/152)). Save adds it to the library; leaving adds nothing. There is no separate preview screen. A refused file shows [why it can't be added](https://tkint.github.io/coach-poulpy/storybook/?path=/story/mobile-screens-library-workouts--workout-import-error), an agreed mockup.
