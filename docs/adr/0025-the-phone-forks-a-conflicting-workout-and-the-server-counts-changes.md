---
status: accepted
date: 2026-11-07
---

# The phone forks a conflicting workout, and the server counts changes

ADR 0006 keeps both edits of a workout made on two phones. This ADR settles where the copy is made, what a deletion does against an edit, and what the pull cursor is ([Accounts](../accounts.md#sync)).

- **The phone makes the copy.** The server refuses a push whose workout version doesn't follow its latest (409 `stale-base`). The phone pulls, turns its unsent versions into a new workout named after the device, points its own sessions and planned workouts at it, and pushes again. The phone already holds every reference to those versions, so nothing stored on the server is ever rewritten, and the server needs no merge rules beyond "version n + 1 follows n".
- **An edit beats a delete.** A workout deleted on one phone while another pushed a newer version comes back, so a delete never loses someone's work.
- **The cursor is a per-account counter, not a time.** Every row written for an account takes the next number under a lock on the account row. Pulls never skip a row committed late, and the phones' clocks don't matter. Clocks only decide small last-write-wins fields, where a wrong clock costs little.

## Considered options

- **The server makes the copy**: the phone would learn from the push answer that its version 3 now lives under another id. The server would have to rewrite the sessions and planned workouts in the same push that reference it, and the phone would have to rename its rows to match.
- **Delete wins**: simpler, but an edit made on another phone vanishes without a trace.
- **A timestamp cursor**: two transactions committing out of order can hide a row from a pull, and phones' clocks drift.
