---
status: accepted
date: 2026-11-01
---

# Body measurements are stored metric and shown in the person's units

Weight and height are logged as dated **body measurements**, stored in kilograms and centimetres whatever the person's **unit system**, and converted only when shown or typed. Each log entry is its own row and is added or corrected, never merged, so a chart is a plain read of the log and switching between metric and imperial changes nothing stored. The profile's other fields (birthdate, pronouns, sex assigned at birth) are single values, not a log; age is computed from the birthdate and never stored.

## Consequences

- Conversion and rounding (kg to lb, cm to ft and in, with the half-inch the input allows) live in shared Kotlin with tests, like every decision about data; the apps only render.
- Distances and speeds already follow the unit system the same way; this makes body measurements one more case, not a second mechanism.
- A new SQLDelight migration adds the measurement table. No existing table changes, so existing phones lose nothing and no backward-compatibility code is needed.
- Entries are append-only and per day, in the spirit of [ADR 0003](./0003-sessions-are-append-only-event-logs.md) and [ADR 0006](./0006-sync-keeps-both-edits.md): when sync arrives, two phones' entries both survive.
- The profile stays on the phone, guest-only, until an account exists; nothing here sends it anywhere.
