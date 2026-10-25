---
status: accepted
date: 2026-10-24
---

# Postgres: columns for structure, JSONB for content

Users, library entries, enrollments, calendar entries, coach links, comments and session headers are normal tables with foreign keys. Workout and plan versions and session results are JSONB documents validated by the shared Kotlin types, so a new workout type needs no schema migration. Device samples go to a separate compressed table, since they are large and rarely read.
