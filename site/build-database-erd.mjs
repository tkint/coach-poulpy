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

const work = mkdtempSync(join(tmpdir(), 'database-erd-'))
const schema = join(work, 'schema.sql')
writeFileSync(schema, await (await pgDump({ pg: db, args: ['--schema-only'] })).text())

execFileSync(join(here, 'node_modules/.bin/liam'), ['erd', 'build', '--input', schema, '--format', 'postgres'], {
  cwd: work,
  stdio: 'inherit',
})

rmSync(out, { recursive: true, force: true })
mkdirSync(out, { recursive: true })
cpSync(join(work, 'dist'), out, { recursive: true })
rmSync(work, { recursive: true, force: true })
console.log(`Database diagram built from ${files.length} migrations.`)
