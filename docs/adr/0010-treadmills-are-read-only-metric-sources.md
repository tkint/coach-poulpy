---
status: accepted
date: 2026-10-24
---

# Treadmills are read-only metric sources in v1

Bluetooth FTMS treadmills are recorded, never driven: the app reads samples and, when a session follows a planned workout, scores each phase against its targets by elapsed time. Every device is a metric source that streams timestamped samples, so a heart rate strap or a bike later is a new source, not a new feature. The FTMS control point (speed and incline per phase) is already encoded in `device/ftms` but stays unused until a later decision.

## Considered options

- **Control speed and incline from the plan**: the better experience, but riskier across treadmill brands and harder to recover from a dropped link.
