---
status: accepted
date: 2026-10-24
---

# Local-first: the phone is the source of truth

The first release is a guest app for family phones, and later releases must keep working offline mid-session. So each phone's SQLite database (SQLDelight) is the source of truth for its user, and the server, when it arrives, is a replica that merges phones and the website. A guest makes no network calls except optional catalog updates; signing up uploads the local database as it is.

## Considered options

- **Server as the source of truth, phone as a cache**: simpler sync, but nothing works without an account or a connection, which rules out the guest-first release.
