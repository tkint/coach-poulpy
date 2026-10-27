---
status: superseded by 0013
date: 2026-10-24
---

# Workout types are modules over one engine

Strength, conditioning, rehab, mobility and treadmill differ in targets, completion rules, recorded values and metrics. Each is a module behind the `WorkoutTypeModule` interface, and one engine compiles any workout into a flat timeline of phases and runs it as a state machine with an injected clock. A new workout type is a new module, not a change to the engine or the other types.
