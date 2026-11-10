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
_Avoid_: Keyword

**Focus**:
What an exercise is for: strength, cardio, mobility, stretching, warm-up or cool down. An exercise can have several. The exercise list filters by it.
_Avoid_: Category, type (the movement pattern is how it moves, not what it is for)

**Position**:
How the body is placed doing an exercise: standing, seated or on the floor (lying, kneeling, all fours). An exercise can have several.
_Avoid_: Posture, stance

**Tag**:
A fact worth knowing before picking an exercise, worked out from the catalog rather than stored: no jumping, each side, hold, timed. Not an alias, which is only a search name.
_Avoid_: Keyword, label

**Format**:
How a workout is built, chosen when it is created and kept: a **circuit** goes round several exercises, each done once per round, so its exercises have no sets; **sets** does one exercise at a time, all its sets with a rest between them, then the next.
_Avoid_: Workout type, kind, sport, category

**Workout**:
A versioned list of parts in one format, ready to be done.
_Avoid_: Routine, program, training

**Automatic name**:
The name a workout gets when it is saved or imported with a blank one: its first exercise, then "(+2)" for the other exercises it uses ("Squat (+2)"), in the app's language. A workout always has a name; once saved the automatic name is an ordinary name and can be renamed.

**Part**:
One section of a workout, holding its steps: the warm-up (at most one, first), a work part (one or more, "Work 1", "Work 2"), or the cool down (at most one, last).
_Avoid_: Phase (a phase is one stretch of a timeline), block, section

**Step**:
One node of a part's tree: an exercise with its prescription, a group, or a rest. An exercise step has a role: exercise or recover; a rest is a recover step with no exercise.
_Avoid_: Item, block

**Group**:
A step holding other steps: a sequence, superset, circuit or interval set with rounds. The builder calls the group it makes a **Repeat**: steps done a number of times, one level deep.
_Avoid_: Block, round (a round is one pass through a group)

**Repeat**:
The builder's name for a group: "Repeat 4 times" around a run and a walk. It holds exercises and rests, never another repeat.
_Avoid_: Loop, set (a set belongs to one exercise's prescription)

**Until button press**:
A walk or run's duration with no set amount: the step ends when the person taps Next. On the last step it reads **until the end**, and the tap finishes the session.
_Avoid_: Lap, open-ended

**Mixed workout**:
A workout with at least one walk or run and at least one other exercise, such as a run warm-up then squats. Live offers it after the workouts made only of walks and runs, and plays it with the workout player's steps; only the walk and run steps follow the treadmill ([ADR 0018](docs/adr/0018-live-plays-mixed-workouts-with-the-players-steps.md)).
_Avoid_: Hybrid

**Prescription**:
What an exercise step asks for: its sets, and per set a target, load, intensity, tempo and the rest before the next set, none after the last. In a circuit an exercise holds one set, and the repeat around it does the rounds.
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
One workout actually done, from start to end, whether guided, free, recorded from a device only, or a workout with a walk or run followed in Live.
_Avoid_: Execution, activity, workout log, training

In code, the header of a session (its id, source, start and end, workout and notes) is a `SessionRecord`; `Session` is the engine's state machine that folds the event log.

**Session event**:
One thing that happened during a session (a phase started, a set logged, a pause, a sample), stored in order and never changed.
_Avoid_: Log entry

**Session recap**:
What opening a finished session shows: its figures, then each part of the workout with the exercises actually done in it (sets, reps or time, load, what was skipped). It is built from the session's events and never edited. In code, `SessionRecap`.
_Avoid_: Session detail, session summary (that is the figures alone)

**Timeline**:
The flat run of work, rest and transition phases a workout compiles to before a session runs it. A free recording's timeline is open-ended: it grows one manual phase each time the user ends a step, and the pause between two steps is their rest.
_Avoid_: Playlist, sequence

**Phase**:
One stretch of a timeline with its own completion rule: manual, timer, distance or round count. Each phase knows the part it comes from; a part is not a phase.
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

### Profile

**Profile**:
What a person tells the app about themselves, all optional and kept on the phone: a display name, birthdate, pronouns, sex assigned at birth, goal, experience, resting heart rate. Body measurements are logged beside it, not inside it.
_Avoid_: Account (there is none while the app is guest-only), user data

**Body measurement**:
One dated weight or height entry in the person's log. Entries are only added or corrected, never summed, and each chart is drawn from them.
_Avoid_: Weigh-in record, vital

**Unit system**:
Metric or imperial, chosen once in Settings. Everything is stored metric (kg, cm, km) and converted only for display and input, so switching never changes a stored value.
_Avoid_: Locale, measurement preference

**Pain window**:
The time after the person says where it hurts (pain or rehab) during which Poulpy stays on the blue side, in the Patched up pose, and no entry counts as missed. 72 hours by default, or 1 week, 2 weeks, or until the person clears it; setting it again starts the time over.
_Avoid_: Injury mode, sick leave

**Body map**:
A front and a back muscle chart whose areas, left and right apart, light up: where it hurts today when picked, what an exercise works, and, as a heat map, how much a workout works each muscle. Skin has no fill, so it implies no skin tone.
_Avoid_: Anatomy picture, body diagram

**Body figure**:
Which of the two builds the body map is drawn in, Wider shoulders or Wider hips. A drawing choice only: not gender, not linked to sex assigned at birth, never used for estimates.
_Avoid_: Avatar, body type, male or female figure

**Pronouns**:
How the person wants to be addressed, used for French agreement and Poulpy's lines. Never inferred from a name or from sex assigned at birth.
_Avoid_: Gender (the app does not ask for it)

### Sharing and coaching

**Library**:
A person's own workouts and plans, catalog items they kept, and items shared with them.
_Avoid_: Collection, favourites

**Library entry**:
A shared workout or plan saved in a library, either synced to the author's latest version or as a copy.
_Avoid_: Bookmark, fork

**Workout file**:
A workout saved as a `.poulpyworkout` file to send to another guest, which opens it in the app to add a copy to their library.
_Avoid_: Backup

**Share card**:
A picture of a finished session (workout name, date, time, sets and any machine values, with Poulpy) sent through the phone's share sheet. It never shows heart rate, and it is a brag card, not a way to share the workout itself (that is the workout file).
_Avoid_: Screenshot, certificate

**FIT export**:
A finished session written as a `.fit` activity file, saved on the phone from the share sheet of a session (which also offers the session as an image), then uploaded on the websites: Garmin Connect's import page (connect.garmin.com/modern/import-data) and Strava's upload page (strava.com/upload/select). Opening the file in the Garmin Connect app does not import it: the app reads it as a course and asks for a point on a map (ADR 0011). Its FIT sport follows what the session holds: only walks and runs are running (treadmill running when a treadmill sent readings), anything else is strength training for Sets and cardio for Circuit. Strength sets carry their planned reps and weight; a walk or run writes a lap per phase and a record per sample.
_Avoid_: Sync, upload (nothing leaves the phone by itself)

**Coach link**:
The tie an athlete grants a coach, which the athlete can revoke at any time.
_Avoid_: Team, connection

### People

**Guest**:
A person using the app with no account; everything stays on their phone.
_Avoid_: Anonymous user, offline user

**Account**:
A person's identity on the server, made when a guest signs up; their phones and the website sync through it.
_Avoid_: User, profile (the profile is what the person tells the app about themselves)

**Confirmed email**:
An account's address once its owner has tapped the link sent to it; nothing leaves the phone before ([Accounts](docs/accounts.md)).
_Avoid_: Verified account, activated account

**Sign-in**:
One phone or browser signed in to an account, named by its device, with its own tokens; revoking a token or reusing a refresh token ends it alone.
_Avoid_: Session (a session is a workout done), login

**Outbox**:
The changes a signed-in phone has made and not yet pushed to the account; signing out waits for it to be empty ([Accounts](docs/accounts.md#sync)).
_Avoid_: Queue, pending uploads

**Cursor**:
Where a phone's last pull stopped: a number counting every change the server stored for the account, never a time.
_Avoid_: Last sync date, timestamp

**Caller**:
Whoever an API request comes from, as the server sees it: an account, the client it comes through and that client's scopes.
_Avoid_: Principal, user

**Scope**:
One right a token grants, named `resource:action` (`sessions:read`).
_Avoid_: Permission, role (a role, like admin, belongs to the account)

**Server setting**:
A value an admin changes in the admin without a deploy, such as the from-address of the emails; it wins over its default from the environment. Secrets, like the SMTP password, are never one ([The admin area](docs/admin.md#settings)).
_Avoid_: Config, preference (a preference belongs to a person)

**Personal access token**:
A token a person makes in Settings for their own scripts or the MCP server: named, scoped, with an expiry, revocable.
_Avoid_: API key

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
