// Fixes the typography a translator can't easily see or type: straight apostrophes, three dots,
// and, in French, the non-breaking spaces around « » and before : ; ! ? %.
// `node i18n/typography.mjs` rewrites the files; `--check` only lists what it would change. The Translation typography workflow runs it on main after each merge.
import fs from 'node:fs'
import path from 'node:path'

const NBSP = ' '
const root = path.join(path.dirname(new URL(import.meta.url).pathname), 'messages')
const check = process.argv.includes('--check')

// No message uses ICU's ' quoting, so every straight apostrophe is prose.
const everywhere = (text) => text.replace(/'/g, '’').replace(/\.\.\./g, '…')

const byLocale = {
  fr: (text) =>
    text
      // An ordinary space, or none after a word or a placeholder, becomes a non-breaking one.
      .replace(/ ([:;!?%»])/g, `${NBSP}$1`)
      .replace(/([\p{L}\d}…])([;!?»])/gu, `$1${NBSP}$2`)
      .replace(/«[ \u00a0]?/g, `«${NBSP}`)
      .replace(/(\d|\}) ?%/g, `$1${NBSP}%`),
}

const fix = (value, locale) => {
  if (typeof value !== 'string') {
    return Object.fromEntries(Object.entries(value).map(([key, inner]) => [key, fix(inner, locale)]))
  }
  const rules = byLocale[locale] ?? ((text) => text)
  return rules(everywhere(value))
}

const changed = []
for (const locale of fs.readdirSync(root)) {
  for (const file of fs.readdirSync(path.join(root, locale))) {
    const where = path.join(root, locale, file)
    const before = fs.readFileSync(where, 'utf8')
    const after = JSON.stringify(fix(JSON.parse(before), locale), null, 2) + '\n'
    if (after === before) continue
    changed.push(path.relative(process.cwd(), where))
    if (!check) fs.writeFileSync(where, after)
  }
}

if (check && changed.length > 0) {
  console.error(`Typography to fix in ${changed.join(', ')}. Run node i18n/typography.mjs and commit.`)
  process.exit(1)
}
