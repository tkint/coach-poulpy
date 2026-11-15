// Checks that an exercise whose content changed carries a higher `revision` (ADR 0030).
//
// An exercise's content is its entry in catalog/data/exercises.json plus its text in
// i18n/messages/<lang>/catalog.json, without the `revision` itself. `revision` is 1 when absent.
// A new exercise needs no bump. Usage in CI: node catalog/tools/check-revisions.mjs <base-ref>

import { execFileSync } from 'node:child_process'
import { readFileSync, readdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const stable = (value) =>
  JSON.stringify(value, (_, v) =>
    v && typeof v === 'object' && !Array.isArray(v)
      ? Object.fromEntries(Object.entries(v).sort(([a], [b]) => (a < b ? -1 : 1)))
      : v,
  )

/** A catalog as `{ exercises: [...], text: { en: { slug: {...} }, fr: ... } }`. */
export function exercisesChanged(base, head) {
  const baseBySlug = new Map(base.exercises.map((e) => [e.slug, e]))
  const failures = []
  for (const exercise of head.exercises) {
    const before = baseBySlug.get(exercise.slug)
    if (!before) continue
    const { revision: beforeRevision = 1, ...beforeStructure } = before
    const { revision: afterRevision = 1, ...afterStructure } = exercise
    const beforeContent = { structure: beforeStructure, text: textOf(base.text, exercise.slug) }
    const afterContent = { structure: afterStructure, text: textOf(head.text, exercise.slug) }
    if (stable(beforeContent) !== stable(afterContent) && afterRevision <= beforeRevision) {
      failures.push({ slug: exercise.slug, revision: afterRevision, needed: beforeRevision + 1 })
    }
  }
  return failures
}

function textOf(text, slug) {
  return Object.fromEntries(Object.entries(text).map(([lang, entries]) => [lang, entries[slug] ?? null]))
}

function load(read) {
  const text = {}
  for (const lang of languages) text[lang] = JSON.parse(read(`i18n/messages/${lang}/catalog.json`))
  return { exercises: JSON.parse(read('catalog/data/exercises.json')).exercises, text }
}

const languages = readdirSync(new URL('../../i18n/messages', import.meta.url))

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const base = process.argv[2]
  if (!base) throw new Error('Usage: check-revisions.mjs <base-ref>')
  const git = (path) => execFileSync('git', ['show', `${base}:${path}`], { encoding: 'utf8', maxBuffer: 1 << 26 })
  const failures = exercisesChanged(load(git), load((path) => readFileSync(path, 'utf8')))
  for (const f of failures) {
    console.log(`::error file=catalog/data/exercises.json::${f.slug} changed but its revision is still ${f.revision}. Set "revision": ${f.needed} on it before merging.`)
  }
  process.exit(failures.length ? 1 : 0)
}
