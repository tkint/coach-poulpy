---
status: accepted
date: 2026-10-24
---

# Sessions are stored as append-only event logs

Every action during a session (phase started, set logged, pause, skip, sample received) is written to local storage at once as a session event, and the finished session is the fold of its events. This lets a session resume at the same second after the phone kills the app, and makes merging two devices a union of events with no conflicts. Each session's totals are saved beside it when it ends, so History opens without replaying logs.

## Considered options

- **Store only the final result**: smaller, but a crash mid-session loses the session and sync needs conflict rules for results.
