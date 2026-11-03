---
status: accepted
date: 2026-11-03
---

# Live plays a mixed workout with the workout player's steps

Live offers any saved workout with at least one walk or run, not only workouts made of walks and runs ([ADR 0013](./0013-workouts-have-a-format-not-a-type.md) said the picker offered the latter). A **mixed workout** is played by the Live recording itself, drawn like the guided player: the same bar, work step and rest, full screen, with Live's numbers on top. We chose this over handing the workout to the guided player with a treadmill strip, because the recording, the belt rules and the finish questions already live in `LiveModel`, and a second owner of the session would have meant two models writing the same event log.

The step the screen shows comes from one shared piece, `PlayerFace`, which the guided `PlayerModel` also uses. Android and iOS share the step views too, so a mixed workout in Live and the same workout in the player cannot drift apart.

## What the treadmill does, step by step

- **A walk or run follows the belt** as in a pure treadmill workout: samples are recorded, a halted belt pauses the recording, a distance target ends when the treadmill reports it. A rest between two walks or runs follows it too.
- **Any other exercise ignores the belt.** No treadmill samples are recorded, a still belt or a lost treadmill neither pauses the recording nor starts the finish question, and the tiles shrink to what the phone is streamed, such as the heart rate.
- **A walk or run reached from another exercise always waits to begin**, as the person may still be on their way to the treadmill, or it may not be connected yet. Following the treadmill with the belt still, it starts when the belt moves, and "Start without it" is the way out. With the belt already moving, or with no treadmill, it shows Start and is timed by the phone. Skip and Done are always there: Done cuts a step short without the feel of skipping it. A workout that opens with a walk or run keeps waiting for the belt in the picker, as Start always did; one that opens with another exercise starts at once.
- **History** keeps one session. Its treadmill totals come from the samples, so from the walks and runs only.

## Consequences

- The wait is a pause the app makes at the step boundary, so the step's clock starts when it begins, and the session clock does not count the wait.
- The console's distance counter may be reset between two walks. `TreadmillRecorder.restart()` makes a distance target count from the next reading, but the summary still reads a session's distance as the counter's growth from its first reading to its last, so a reset in between skews it (marked `// CLEANUP:` in the code).
