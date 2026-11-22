// Builds the server's database diagram: an interactive viewer (Liam ERD) of the schema the server's
// migrations produce, published under /database-erd/ and linked from docs/database.md.
// The migrations are applied to an in-process Postgres (PGlite), dumped as a schema, and rendered,
// so the diagram can't drift from the migrations and needs no database service in CI.
import { execFileSync } from 'node:child_process'
import { cpSync, mkdirSync, mkdtempSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { PGlite } from '@electric-sql/pglite'
import { pgDump } from '@electric-sql/pglite-tools/pg_dump'

const here = dirname(fileURLToPath(import.meta.url))
const migrations = resolve(here, '../server/src/main/sqldelight/migrations')
const out = resolve(here, 'public/database-erd')

const db = new PGlite()
const files = readdirSync(migrations)
  .filter((f) => f.endsWith('.sqm'))
  .sort((a, b) => parseInt(a) - parseInt(b))
for (const file of files) await db.exec(readFileSync(join(migrations, file), 'utf8'))
// What each table and column means, shown in the viewer.
await db.exec(readFileSync(join(here, 'database-comments.sql'), 'utf8'))

// A table or column added by a migration without a comment is listed in the build output.
const { rows: missing } = await db.query(`
  SELECT c.relname || '.' || a.attname AS name
  FROM pg_class c JOIN pg_attribute a ON a.attrelid = c.oid AND a.attnum > 0 AND NOT a.attisdropped
  WHERE c.relkind = 'r' AND c.relnamespace = 'public'::regnamespace AND col_description(c.oid, a.attnum) IS NULL
  UNION ALL
  SELECT c.relname FROM pg_class c
  WHERE c.relkind = 'r' AND c.relnamespace = 'public'::regnamespace AND obj_description(c.oid, 'pg_class') IS NULL
  ORDER BY 1`)
if (missing.length > 0) {
  console.warn(`No comment in site/database-comments.sql for: ${missing.map((row) => row.name).join(', ')}`)
}

const work = mkdtempSync(join(tmpdir(), 'database-erd-'))
const schema = join(work, 'schema.sql')
writeFileSync(schema, await (await pgDump({ pg: db, args: ['--schema-only'] })).text())

execFileSync(join(here, 'node_modules/.bin/liam'), ['erd', 'build', '--input', schema, '--format', 'postgres'], {
  cwd: work,
  stdio: 'inherit',
})

// Liam drops the comments of tables that a later ALTER TABLE touches, so they are put back into the schema
// the viewer loads, from the database's own catalog.
const { rows: comments } = await db.query(`
  SELECT c.relname AS "table", a.attname AS "column", d.description
  FROM pg_description d JOIN pg_class c ON c.oid = d.objoid AND d.classoid = 'pg_class'::regclass
  LEFT JOIN pg_attribute a ON a.attrelid = c.oid AND a.attnum = d.objsubid AND d.objsubid > 0
  WHERE c.relkind = 'r' AND c.relnamespace = 'public'::regnamespace`)
const built = join(work, 'dist/schema.json')
const erd = JSON.parse(readFileSync(built, 'utf8'))
for (const { table, column, description } of comments) {
  const target = column ? erd.tables[table]?.columns[column] : erd.tables[table]
  if (target) target.comment = description
}
writeFileSync(built, JSON.stringify(erd))

rmSync(out, { recursive: true, force: true })
mkdirSync(out, { recursive: true })
cpSync(join(work, 'dist'), out, { recursive: true })
rmSync(work, { recursive: true, force: true })
console.log(`Database diagram built from ${files.length} migrations.`)
