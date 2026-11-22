# The database by example

One example row for every table of [the server's database](./database.md), as the server writes it, for anyone reading or querying the data. The four JSONB columns are shown in full: an exercise, a workout, a session's totals and a session's events.

The ids and times are made up. Every row belongs to the same account, `…0001`, which is why the keys line up.

## Accounts and sign-in

```sql
INSERT INTO account (id, email, password_hash, language, role, health_consent_at, confirmed_at, created_at, change_count)
VALUES ('0190d4d2-0000-7000-8000-000000000001', 'ana@example.com',
        '$argon2id$v=19$m=19456,t=2,p=1$c2FsdHNhbHQ$aGFzaGhhc2g', 'fr', 'member',
        '2026-11-07 07:00:00+00', '2026-11-07 07:05:00+00', '2026-11-07 07:00:00+00', 12);

-- Only the SHA-256 of a token is kept, as bytes.
INSERT INTO confirmation_token (token_hash, account_id, expires_at)
VALUES ('\x9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
        '0190d4d2-0000-7000-8000-000000000001', '2026-11-08 07:00:00+00');

INSERT INTO confirmation_watch (token_hash, key_hash, account_id, confirmed, expires_at)
VALUES ('\x9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
        '\x60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752',
        '0190d4d2-0000-7000-8000-000000000001', TRUE, '2026-11-08 07:00:00+00');

INSERT INTO password_reset_token (token_hash, account_id, created_at, expires_at)
VALUES ('\xfd61a03af4f77d870fc21e05e7e80678095c92d808cfb3b5c279ee04c74aca13',
        '0190d4d2-0000-7000-8000-000000000001', '2026-11-20 18:00:00+00', '2026-11-20 19:00:00+00');

-- One phone signed in, and its two tokens.
INSERT INTO sign_in (id, account_id, device_name, client, created_at, last_refreshed_at)
VALUES ('0190d4d3-0000-7000-8000-000000000001', '0190d4d2-0000-7000-8000-000000000001',
        'Ana''s Pixel', 'app', '2026-11-07 07:06:00+00', '2026-11-22 08:00:00+00');

INSERT INTO access_token (token_hash, sign_in_id, expires_at)
VALUES ('\x5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        '0190d4d3-0000-7000-8000-000000000001', '2026-11-22 08:15:00+00');

-- A refresh token works once; used_at is set after that.
INSERT INTO refresh_token (token_hash, sign_in_id, expires_at, used_at)
VALUES ('\x6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b',
        '0190d4d3-0000-7000-8000-000000000001', '2026-12-22 08:00:00+00', NULL);

INSERT INTO sign_in_failure (email, failed_at) VALUES ('ana@example.com', '2026-11-21 21:14:00+00');
```

## Synced data

```sql
-- A session's header; `totals` is JSONB.
INSERT INTO session (account_id, id, source, started_at, ended_at, workout_id, workout_version, title, format, totals, change_number)
VALUES ('0190d4d2-0000-7000-8000-000000000001', '0190e000-0000-7000-8000-000000000001', 'GUIDED',
        '2026-11-21 07:30:00+00', '2026-11-21 07:47:00+00',
        '0190d900-0000-7000-8000-000000000001', 2, 'Morning circuit', 'CIRCUIT',
        '{"active": "PT17M", "setsDone": 5, "setsSkipped": 1, "setsPlanned": 6}', 7);

-- Its events, one row each; `payload` is the event itself, JSONB.
INSERT INTO session_event (account_id, session_id, seq, at, payload, change_number) VALUES
('0190d4d2-0000-7000-8000-000000000001', '0190e000-0000-7000-8000-000000000001', 1, '2026-11-21 07:30:00+00',
 '{"type": "started", "at": "2026-11-21T07:30:00Z"}', 7),
('0190d4d2-0000-7000-8000-000000000001', '0190e000-0000-7000-8000-000000000001', 2, '2026-11-21 07:30:40+00',
 '{"type": "phaseCompleted", "at": "2026-11-21T07:30:40Z", "phaseIndex": 0}', 7),
('0190d4d2-0000-7000-8000-000000000001', '0190e000-0000-7000-8000-000000000001', 3, '2026-11-21 07:31:00+00',
 '{"type": "sample", "at": "2026-11-21T07:31:00Z", "metrics": {"speedKmh": 6.0, "heartRateBpm": 128}}', 7),
('0190d4d2-0000-7000-8000-000000000001', '0190e000-0000-7000-8000-000000000001', 4, '2026-11-21 07:47:00+00',
 '{"type": "finished", "at": "2026-11-21T07:47:00Z"}', 7);

-- A workout version; `content` is the whole workout, JSONB.
INSERT INTO workout_version (account_id, workout_id, version, format, name, content, created_at, change_number)
VALUES ('0190d4d2-0000-7000-8000-000000000001', '0190d900-0000-7000-8000-000000000001', 2, 'CIRCUIT', 'Morning circuit',
        '{"id": "0190d900-0000-7000-8000-000000000001", "version": 2, "name": "Morning circuit", "format": "CIRCUIT",
          "parts": [{"id": "0190d900-0000-7000-8000-0000000000a1", "kind": "WORK", "steps": [
            {"type": "group", "id": "0190d900-0000-7000-8000-0000000000b1", "kind": "CIRCUIT", "rounds": 3,
             "restBetweenRounds": "PT1M", "children": [
              {"type": "exercise", "id": "0190d900-0000-7000-8000-0000000000c1", "exercise": "goblet-squat",
               "prescription": {"sets": [{"target": {"type": "reps", "count": 10}, "load": {"type": "kg", "kg": 12.0}}]}},
              {"type": "rest", "id": "0190d900-0000-7000-8000-0000000000c2", "duration": "PT30S"},
              {"type": "exercise", "id": "0190d900-0000-7000-8000-0000000000c3", "exercise": "plank",
               "prescription": {"sets": [{"target": {"type": "hold", "duration": "PT40S"}}]}}]}]}]}',
        '2026-11-14 09:00:00+00', 5);

-- A workout deleted at version 2; a newer version would remove this row.
INSERT INTO workout_deletion (account_id, workout_id, version, deleted_at, change_number)
VALUES ('0190d4d2-0000-7000-8000-000000000001', '0190d900-0000-7000-8000-000000000002', 2, '2026-11-18 12:00:00+00', 9);

INSERT INTO planned_workout (account_id, id, workout_id, day, created_at, changed_at, deleted, change_number)
VALUES ('0190d4d2-0000-7000-8000-000000000001', '0190e100-0000-7000-8000-000000000001',
        '0190d900-0000-7000-8000-000000000001', '2026-11-24', '2026-11-22 09:00:00+00', '2026-11-22 09:00:00+00', FALSE, 11);

-- Weight is kg, height cm; a deleted entry keeps its row with no value.
INSERT INTO body_measurement (account_id, metric, day, value, changed_at, change_number)
VALUES ('0190d4d2-0000-7000-8000-000000000001', 'WEIGHT', '2026-11-22', 62.4, '2026-11-22 07:10:00+00', 12);

INSERT INTO setting (account_id, key, value, changed_at, change_number)
VALUES ('0190d4d2-0000-7000-8000-000000000001', 'settings.units', 'METRIC', '2026-11-07 07:06:00+00', 3);
```

The workout's `type` fields say which kind of step, target or load each object is. A field left at its default, such as a set's `side: BOTH` or a zero rest, is not written.

## The catalog

```sql
INSERT INTO catalog_version (version, published_at) VALUES (1, '2026-11-01 10:00:00+00');

-- One revision of one exercise; `content` is the exercise, JSONB.
INSERT INTO catalog_exercise_revision (slug, revision, position, content, variation_group, variation_rank, published_in, seq, created_at)
VALUES ('goblet-squat', 1, 3,
        '{"slug": "goblet-squat",
          "name": {"en": "Goblet squat", "fr": "Squat gobelet"},
          "movementPattern": "SQUAT",
          "muscleGroups": ["QUADRICEPS", "GLUTES", "CORE"],
          "equipment": [["DUMBBELL", "KETTLEBELL"]],
          "measurement": "REPS",
          "focus": ["STRENGTH"],
          "positions": ["STANDING"],
          "instructions": [{"en": "Hold the weight against your chest.", "fr": "Tenez la charge contre la poitrine."}]}',
        2, 3, 1, 1, '2026-11-01 09:30:00+00');

-- The one row, created by the migration, holds the last number the publication cursor handed out.
UPDATE catalog_publication SET last_seq = 1 WHERE id = 1;
```

`variation_group` and `variation_rank` repeat what the exercise's group says, so the server can order by them without opening `content`.

## Admin and housekeeping

```sql
INSERT INTO admin_audit (id, actor_id, action, target, at)
VALUES ('0190e200-0000-7000-8000-000000000001', '0190d4d2-0000-7000-8000-000000000001',
        'admin.promote', '0190d4d2-0000-7000-8000-000000000009', '2026-11-08 10:00:00+00');

INSERT INTO settings (name, value, updated_at) VALUES ('mail.from', 'hello@coach-poulpy.com', '2026-11-08 10:05:00+00');

INSERT INTO schema_version (version) VALUES (14);
```
