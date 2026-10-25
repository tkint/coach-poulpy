---
status: accepted
date: 2026-10-24
---

# Sync keeps both conflicting edits

Sessions never conflict (ADR 0003). For workouts and plans, each edit names the version it started from; if the server already has a newer version from another device, the incoming edit is stored as a copy named after its device, and both devices see both. Small fields (calendar entry status, library mode, settings) carry a timestamp per field and the latest write wins. The protocol is two calls: push local changes with their base versions, pull every change since the device's cursor.

## Considered options

- **Last write wins for whole documents**: silently loses a workout edited on two phones.
- **Merge workout trees**: too complex for a rare case.
