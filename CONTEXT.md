# Coach Poulpy

A personal training platform where people build workouts, follow plans, train through guided sessions and watch their progress, coached by Poulpy, a reversible plush octopus.

## Language

### Catalog and workouts

**Exercise**:
One movement from the curated catalog, identified by a slug that never changes.
_Avoid_: Move, drill

**Catalog**:
The curated, versioned set of exercises shipped with the app and updated without an account.
_Avoid_: Exercise database, library (the library is the user's)

**Retired exercise**:
A catalog exercise no longer offered, kept so the workouts that use it still open.
_Avoid_: Deleted exercise

**Variation**:
A catalog exercise that does the same movement made easier or harder, like the knee push-up and the diamond push-up for the push-up. Variations are equals in one group, ordered from easiest to hardest, and each keeps its own slug, records and history.
_Avoid_: Progression, regression, alternative

**Alias**:
Another name people search an exercise by, like "RDL" or "abdos", kept per language in the catalog and never shown.
_Avoid_: Tag, keyword

**Workout type**:
The family a workout belongs to (strength, conditioning, rehab, mobility, treadmill), which decides the targets it accepts, how its steps finish and what it records.
_Avoid_: Sport, category

**Workout**:
A versioned tree of steps of one workout type, ready to be done.
_Avoid_: Routine, program, training

**Step**:
One node of a workout's tree: an exercise with its prescription, a group, or a rest.
_Avoid_: Item, block

**Group**:
A step holding other steps: a sequence, superset, circuit or interval set with rounds.
_Avoid_: Block, round (a round is one pass through a group)

**Prescription**:
What an exercise step asks for: its sets, and per set a target, load, intensity, tempo and the rest after it.
_Avoid_: Target (a target is one part of a prescription), plan

### Plans and scheduling

**Plan**:
A versioned set of weeks, each day holding zero or more workouts, written out in full.
_Avoid_: Program, schedule, routine

**Enrollment**:
One person following one version of a plan from a start date.
_Avoid_: Subscription, assignment

**Calendar entry**:
One workout an enrollment schedules on a date, with a status: planned, done, skipped or moved.
_Avoid_: Appointment, slot

**Planned workout**:
A workout a person put on a day themselves, outside any plan.
_Avoid_: Scheduled session

**Goal**:
A measurable target with an optional deadline, whose progress is computed from sessions.
_Avoid_: Objective

### Training

**Session**:
One workout actually done, from start to end, whether guided, free or recorded from a device only.
_Avoid_: Execution, activity, workout log, training

**Session event**:
One thing that happened during a session (a phase started, a set logged, a pause, a sample), stored in order and never changed.
_Avoid_: Log entry

**Timeline**:
The flat run of work, rest and transition phases a workout compiles to before a session runs it.
_Avoid_: Playlist, sequence

**Phase**:
One stretch of a timeline with its own completion rule: manual, timer, distance or round count.
_Avoid_: Interval (an interval set is a group)

**Cue**:
A prompt the engine raises during a session (countdown, switch sides, next exercise) that each platform turns into sound, vibration or text.
_Avoid_: Notification, alert

### Devices

**Metric source**:
Anything that streams samples into a session; a treadmill today, a heart rate strap later.
_Avoid_: Sensor, integration

**Sample**:
One timestamped reading from a metric source: speed, incline, distance, heart rate or cadence.
_Avoid_: Data point, packet

### Sharing and coaching

**Library**:
A person's own workouts and plans, catalog items they kept, and items shared with them.
_Avoid_: Collection, favourites

**Library entry**:
A shared workout or plan saved in a library, either synced to the author's latest version or as a copy.
_Avoid_: Bookmark, fork

**Coach link**:
The tie an athlete grants a coach, which the athlete can revoke at any time.
_Avoid_: Team, connection

### People

**Guest**:
A person using the app with no account; everything stays on their phone.
_Avoid_: Anonymous user, offline user

### Poulpy

**Poulpy**:
The coach mascot, a reversible plush octopus: blue side when the plan is on track, ember side when a session slipped.
_Avoid_: Avatar, character, octopus

**Mood**:
Poulpy's feeling, one of six shades: proud, happy, recovering on the blue side; annoyed, grumpy, fuming on the ember side.
_Avoid_: State, emotion

**Pose**:
A full drawing of Poulpy for a moment that matters (Champion, Straining, Sleeping...), as opposed to a plain mood portrait.
_Avoid_: Sticker, illustration

**Tentacle**:
One of the eight segments of Poulpy's ring; each finished session lights one, and a lit tentacle never goes dark.
_Avoid_: Point, star, streak

**Full octopus**:
All eight tentacles lit, which shows the Champion pose and starts a new ring.
_Avoid_: Level, badge
