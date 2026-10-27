---
status: accepted
date: 2026-10-26
---

# Workouts have a format, not a type

Supersedes [ADR 0004](./0004-workout-types-are-modules-over-one-engine.md).

A workout no longer has a workout type (strength, conditioning, rehab, mobility, treadmill). It has a **format**, circuit or sets, chosen when it is created, and is made of **parts**: an optional warm-up, one or more work parts and an optional cool down.

The five type modules had converged on one rule: a time or a hold runs a timer, and reps, a distance or an open target wait for a tap. The type only decided which exercises the builder offered and a label, while people kept mixing a run warm-up with squats or a walk cool down with stretches. What they actually choose is how the workout is built: rounds through several exercises, each done once per round, or one exercise at a time with all its sets. That is the format. Warming up and cooling down are sections of the workout rather than tags on single steps, so they became parts.

The engine reads the completion rule from the target, compiles the parts in order, and tells each phase which part it belongs to. `WorkoutTypeModule`, its registry and the strength, conditioning, rehab and mobility modules are gone; `types/treadmill` stays for the treadmill's own logic (recording, summary, step estimate). What the type used to drive elsewhere comes from the format or from the exercises: the Live picker offers workouts made of walks and runs, and the FIT sport follows the format and the exercises.

Saved workouts were not converted when this landed: the app was only on its author's phones, so the migration removes them and their planned days, and past sessions stay in the history without a format.
