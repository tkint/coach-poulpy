---
status: accepted
date: 2026-11-07
---

# The server's schema is SQLDelight migrations on Postgres, applied on start

The server keeps its own Postgres schema ([ADR 0007](./0007-postgres-columns-for-structure-jsonb-for-content.md)), separate from the phone's SQLite one: phones and the server store different things (the server has accounts and tokens, the phone has no owner column), and sync exchanges documents, not tables. Both are written the same way, though: numbered SQLDelight migrations (`.sqm`) and typed queries (`.sq`), in `server/src/main/sqldelight` for the server. The server applies the migrations it lacks every time it starts, before it answers.

## How migrations run

- **The schema comes from the migrations.** `1.sqm` moves version 1 to 2, `2.sqm` 2 to 3, and so on; a new database runs them all in order. A change is always a new file, never an edit of one already merged, because production has run it.
- **The database records its version** in the `schema_version` table. On start, `Database.migrate` reads it and runs the missing files in one transaction, under a Postgres advisory lock. A failed migration rolls back and stops the server, and Docker restarts it, so it never serves a half-migrated schema.
- **A database newer than the server stops it.** Rolling back to an older release after a migration means restoring the dump taken before it ([Deploying the server](../deploying-the-server.md#backups)).
- **Tests run on a real Postgres 17**, the same major version as production, started from binaries by `embedded-postgres`, so they need no Docker.

## Considered options

- **Flyway with plain SQL files.** The usual JVM migration runner, with its own locking and history table. It would mean a second tool beside SQLDelight, and queries written by hand over JDBC with nothing checking them at build time. SQLDelight's runner is the twenty lines of `Database.migrate`.
- **One schema shared with the phone.** The tables differ too much: every server row has an owner, and the phone keeps state the server never sees.

## Consequences

- A schema change on the server is a new `.sqm` file, as on the phone, and its queries fail the build when they no longer match the schema.
- SQLDelight's `verifyMigrations` only checks SQLite, so the server's migrations are checked by running them in the tests instead.
