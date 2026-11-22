-- What each table and column of the server's database means, shown in the database diagram
-- (docs/database.md). The server never runs this file: build-database-erd.mjs applies it after the
-- migrations. A new table or column from a migration needs its comment here.

-- Accounts and sign-in
COMMENT ON TABLE account IS 'A person''s account. It exists from sign-up; until confirmed_at is set, its phone stays a guest and uploads nothing.';
COMMENT ON COLUMN account.id IS 'UUID of the account.';
COMMENT ON COLUMN account.email IS 'Sign-in address. Unique whatever its case.';
COMMENT ON COLUMN account.password_hash IS 'Argon2id hash in PHC form.';
COMMENT ON COLUMN account.language IS 'Language of the emails sent to the person.';
COMMENT ON COLUMN account.role IS 'member or admin.';
COMMENT ON COLUMN account.health_consent_at IS 'When the person agreed to their health data being stored.';
COMMENT ON COLUMN account.confirmed_at IS 'When the email was confirmed. Null until then.';
COMMENT ON COLUMN account.created_at IS 'When the account was created.';
COMMENT ON COLUMN account.change_count IS 'The last change number given to a row of this account. Sync takes the next one, under a lock on this row.';
COMMENT ON COLUMN account.disabled_at IS 'When an admin disabled the account, which then cannot sign in or refresh. Null while in use.';

COMMENT ON TABLE confirmation_token IS 'The links sent to confirm an email. A token is deleted once used.';
COMMENT ON COLUMN confirmation_token.token_hash IS 'SHA-256 of the token; the token itself is never kept.';
COMMENT ON COLUMN confirmation_token.account_id IS 'The account the link confirms.';
COMMENT ON COLUMN confirmation_token.expires_at IS 'When the link stops working.';

COMMENT ON TABLE confirmation_watch IS 'Lets a phone on "Check your email" ask whether its link was opened. A key is tied to the one link mailed in that sign-up.';
COMMENT ON COLUMN confirmation_watch.token_hash IS 'SHA-256 of the mailed link''s token; only opening that link marks the watch confirmed.';
COMMENT ON COLUMN confirmation_watch.key_hash IS 'SHA-256 of the random key the phone sent at sign-up and asks with.';
COMMENT ON COLUMN confirmation_watch.account_id IS 'The account being confirmed.';
COMMENT ON COLUMN confirmation_watch.confirmed IS 'True once the link was opened.';
COMMENT ON COLUMN confirmation_watch.expires_at IS 'When the watch stops answering.';

COMMENT ON TABLE password_reset_token IS 'A mailed link that sets a new password once, within an hour. Sent only to a confirmed address.';
COMMENT ON COLUMN password_reset_token.token_hash IS 'SHA-256 of the token.';
COMMENT ON COLUMN password_reset_token.account_id IS 'The account whose password the link resets.';
COMMENT ON COLUMN password_reset_token.created_at IS 'When the link was sent; used to limit how many are sent.';
COMMENT ON COLUMN password_reset_token.expires_at IS 'When the link stops working.';

COMMENT ON TABLE sign_in IS 'One phone or browser signed in to an account. Its tokens live and die together.';
COMMENT ON COLUMN sign_in.id IS 'UUID of the sign-in.';
COMMENT ON COLUMN sign_in.account_id IS 'The account signed in to.';
COMMENT ON COLUMN sign_in.device_name IS 'Name of the phone or browser, as shown in the account''s list of sign-ins.';
COMMENT ON COLUMN sign_in.client IS 'What holds the credential, such as app or website.';
COMMENT ON COLUMN sign_in.created_at IS 'When the person signed in.';
COMMENT ON COLUMN sign_in.last_refreshed_at IS 'When its tokens were last refreshed.';
COMMENT ON COLUMN sign_in.revoked_at IS 'When it was signed out or revoked. Null while valid.';

COMMENT ON TABLE access_token IS 'The short-lived token sent with every request.';
COMMENT ON COLUMN access_token.token_hash IS 'SHA-256 of the 256-bit random token.';
COMMENT ON COLUMN access_token.sign_in_id IS 'The sign-in it belongs to.';
COMMENT ON COLUMN access_token.expires_at IS 'When it stops working.';

COMMENT ON TABLE refresh_token IS 'Trades for a new pair of tokens, once. It is kept after use, so a second use is noticed and the whole sign-in revoked.';
COMMENT ON COLUMN refresh_token.token_hash IS 'SHA-256 of the 256-bit random token.';
COMMENT ON COLUMN refresh_token.sign_in_id IS 'The sign-in it belongs to.';
COMMENT ON COLUMN refresh_token.expires_at IS 'When it stops working.';
COMMENT ON COLUMN refresh_token.used_at IS 'When it was used. Null while unused.';

COMMENT ON TABLE sign_in_failure IS 'Failed sign-ins per address, to slow down password guessing. Not tied to an account.';
COMMENT ON COLUMN sign_in_failure.email IS 'The address that was tried.';
COMMENT ON COLUMN sign_in_failure.failed_at IS 'When the attempt failed.';

-- Synced data: every row below carries the change_number of its account's change count
COMMENT ON TABLE session IS 'A workout session''s header. A deleted session keeps its row, with deleted_at set and no events, so the deletion reaches every phone.';
COMMENT ON COLUMN session.account_id IS 'Owner of the session.';
COMMENT ON COLUMN session.id IS 'UUID made on the phone.';
COMMENT ON COLUMN session.source IS 'How it was done: GUIDED (a workout followed in the player), FREE, or another source.';
COMMENT ON COLUMN session.started_at IS 'When it started.';
COMMENT ON COLUMN session.ended_at IS 'When it ended. Null while unfinished.';
COMMENT ON COLUMN session.workout_id IS 'The workout followed, if any. Not a foreign key: a workout is its versions.';
COMMENT ON COLUMN session.workout_version IS 'The exact version of the workout followed.';
COMMENT ON COLUMN session.title IS 'Name shown in History.';
COMMENT ON COLUMN session.format IS 'The workout''s format when the session started: CIRCUIT or SETS.';
COMMENT ON COLUMN session.totals IS 'JSONB: the phone''s saved SessionTotals (active time, sets done, skipped and planned, distance, heart rate, and so on).';
COMMENT ON COLUMN session.deleted_at IS 'When the session was deleted from History. Null otherwise.';
COMMENT ON COLUMN session.change_number IS 'Position of the last change to this row in the account''s stream of changes.';

COMMENT ON TABLE session_event IS 'One event of a session, never changed once stored. The session''s state is the fold of its events.';
COMMENT ON COLUMN session_event.account_id IS 'Owner of the session.';
COMMENT ON COLUMN session_event.session_id IS 'The session this event belongs to.';
COMMENT ON COLUMN session_event.seq IS 'Place of the event in the session''s log.';
COMMENT ON COLUMN session_event.at IS 'When it happened, by the phone''s clock.';
COMMENT ON COLUMN session_event.payload IS 'JSONB: the event itself, such as started, phaseCompleted, paused, finished or a device sample.';
COMMENT ON COLUMN session_event.change_number IS 'Position of this row in the account''s stream of changes.';

COMMENT ON TABLE workout_version IS 'One version of a workout. A version never changes once stored; a push may only add the version that follows the latest.';
COMMENT ON COLUMN workout_version.account_id IS 'Owner of the workout.';
COMMENT ON COLUMN workout_version.workout_id IS 'UUID of the workout, made on the phone.';
COMMENT ON COLUMN workout_version.version IS 'Version number, from 1.';
COMMENT ON COLUMN workout_version.format IS 'CIRCUIT or SETS.';
COMMENT ON COLUMN workout_version.name IS 'The workout''s name in this version.';
COMMENT ON COLUMN workout_version.content IS 'JSONB: the whole workout (parts, steps, prescriptions), read with the shared Kotlin types.';
COMMENT ON COLUMN workout_version.created_at IS 'When this version was made.';
COMMENT ON COLUMN workout_version.change_number IS 'Position of this row in the account''s stream of changes.';

COMMENT ON TABLE workout_deletion IS 'A workout deleted at a version. A newer version pushed later removes this row: the edit wins.';
COMMENT ON COLUMN workout_deletion.account_id IS 'Owner of the workout.';
COMMENT ON COLUMN workout_deletion.workout_id IS 'The deleted workout.';
COMMENT ON COLUMN workout_deletion.version IS 'The version that was deleted.';
COMMENT ON COLUMN workout_deletion.deleted_at IS 'When it was deleted.';
COMMENT ON COLUMN workout_deletion.change_number IS 'Position of this row in the account''s stream of changes.';

COMMENT ON TABLE planned_workout IS 'A workout planned on a day. Latest write wins; a deletion is a write, kept as a row.';
COMMENT ON COLUMN planned_workout.account_id IS 'Owner of the plan.';
COMMENT ON COLUMN planned_workout.id IS 'Id of this planned workout.';
COMMENT ON COLUMN planned_workout.workout_id IS 'The workout planned.';
COMMENT ON COLUMN planned_workout.day IS 'The day it is planned on.';
COMMENT ON COLUMN planned_workout.created_at IS 'When it was first planned.';
COMMENT ON COLUMN planned_workout.changed_at IS 'The time the phone gave to the latest write; the later one wins.';
COMMENT ON COLUMN planned_workout.deleted IS 'True once taken off its day.';
COMMENT ON COLUMN planned_workout.change_number IS 'Position of this row in the account''s stream of changes.';

COMMENT ON TABLE body_measurement IS 'One entry per metric and day, in kg or cm. Latest write wins.';
COMMENT ON COLUMN body_measurement.account_id IS 'Owner of the log.';
COMMENT ON COLUMN body_measurement.metric IS 'WEIGHT (kg) or HEIGHT (cm).';
COMMENT ON COLUMN body_measurement.day IS 'The day of the entry.';
COMMENT ON COLUMN body_measurement.value IS 'The measurement. Null once the entry is deleted.';
COMMENT ON COLUMN body_measurement.changed_at IS 'The time the phone gave to the latest write; the later one wins.';
COMMENT ON COLUMN body_measurement.change_number IS 'Position of this row in the account''s stream of changes.';

COMMENT ON TABLE setting IS 'A person''s synced profile field, unit or language, by the phone''s key. Not the server settings (see settings).';
COMMENT ON COLUMN setting.account_id IS 'Owner of the setting.';
COMMENT ON COLUMN setting.key IS 'The phone''s key, such as profile.name or settings.units.';
COMMENT ON COLUMN setting.value IS 'The value. Null once cleared.';
COMMENT ON COLUMN setting.changed_at IS 'The time the phone gave to the latest write; the later one wins.';
COMMENT ON COLUMN setting.change_number IS 'Position of this row in the account''s stream of changes.';

-- The catalog
COMMENT ON TABLE catalog_exercise_revision IS 'One revision of one exercise. A slug has at most one draft (no seq) and any number of published revisions, which are never changed. The current catalog is each slug''s revision with the highest seq.';
COMMENT ON COLUMN catalog_exercise_revision.slug IS 'The exercise''s stable key. Never changes; a retired exercise stays.';
COMMENT ON COLUMN catalog_exercise_revision.revision IS 'Revision number of this slug, from 1.';
COMMENT ON COLUMN catalog_exercise_revision.position IS 'Place in the catalog''s order.';
COMMENT ON COLUMN catalog_exercise_revision.content IS 'JSONB: the shared Exercise (names, muscles, equipment, instructions, and so on).';
COMMENT ON COLUMN catalog_exercise_revision.variation_group IS 'Number of the variation group the exercise is in, if any.';
COMMENT ON COLUMN catalog_exercise_revision.variation_rank IS 'Place in its variation group, easiest first.';
COMMENT ON COLUMN catalog_exercise_revision.published_in IS 'The catalog version that published it. Unused; see catalog_version.';
COMMENT ON COLUMN catalog_exercise_revision.created_at IS 'When the revision was made.';
COMMENT ON COLUMN catalog_exercise_revision.seq IS 'Place in the stream of published revisions, taken from catalog_publication. Null for the draft.';

COMMENT ON TABLE catalog_publication IS 'The publication cursor. One row; publishing reserves numbers under its lock, so seq follows commit order.';
COMMENT ON COLUMN catalog_publication.id IS 'Always 1.';
COMMENT ON COLUMN catalog_publication.last_seq IS 'The last seq taken.';

COMMENT ON TABLE catalog_version IS 'To be dropped: a numbered published version of the catalog. Nothing reads it now that publications are numbered by seq; a later migration drops it (ADR 0030).';
COMMENT ON COLUMN catalog_version.version IS 'Version number.';
COMMENT ON COLUMN catalog_version.published_at IS 'When it was published.';

-- Admin and housekeeping
COMMENT ON TABLE admin_audit IS 'What an admin did: one row per admin write, kept after the admin''s own account is deleted.';
COMMENT ON COLUMN admin_audit.id IS 'UUID of the entry.';
COMMENT ON COLUMN admin_audit.actor_id IS 'The admin who did it. Null once that account is deleted.';
COMMENT ON COLUMN admin_audit.action IS 'What was done, such as admin.promote.';
COMMENT ON COLUMN admin_audit.target IS 'What changed, such as an account id or an exercise slug.';
COMMENT ON COLUMN admin_audit.at IS 'When it was done.';

COMMENT ON TABLE settings IS 'Server settings an admin changes without a deploy, one row per setting. Only non-secret values; a setting with no row keeps its default from the environment. Not the person''s synced setting.';
COMMENT ON COLUMN settings.name IS 'The setting, such as mail.from.';
COMMENT ON COLUMN settings.value IS 'Its value.';
COMMENT ON COLUMN settings.updated_at IS 'When an admin last changed it.';

COMMENT ON TABLE schema_version IS 'The version of this schema in the database, kept by Database.migrate. One row.';
COMMENT ON COLUMN schema_version.version IS 'The number of the next migration file to apply.';
