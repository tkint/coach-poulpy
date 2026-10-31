// Checks the HTML mockups in design/mockups (ADR 0016). Used by CI.
//   node design/check-mockups.mjs
// A mockup must:
//   - carry a <title>, mockup:screen and mockup:section meta tags, and data-component on what it draws
//   - style only with var(--cp-*): no literal colours, and no px value where a spacing, radius or type token has that size
//   - link only files that exist, and load no script or stylesheet from outside the repo (brand fonts are the one exception)
import { readFileSync, readdirSync, existsSync, statSync } from 'node:fs'
import { dirname, join, relative, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const mockups = join(root, 'design/mockups')
const tokens = readFileSync(join(root, 'web/src/styles/tokens.css'), 'utf8')

// px sizes that have a token, so a literal of that size should use it.
const tokenPx = (prefix) =>
  new Set([...tokens.matchAll(new RegExp(`--cp-${prefix}-[a-z]+:\\s*(\\d+)px`, 'g'))].map((m) => m[1]))
const spacing = tokenPx('space')
const radius = tokenPx('radius')
// Body and label sizes are set on buttons and inputs all over a mockup, so only the display, title,
// headline and timer sizes must come from their token.
const fontSizes = new Set([...tokens.matchAll(/--cp-type-(display|title|headline|timer):\s*\d+\s+([\d.]+)px/g)].map((m) => m[2]))

const fontHosts = ['fonts.googleapis.com', 'fonts.gstatic.com']
const problems = []
const fail = (file, message) => problems.push(`${relative(root, file)}: ${message}`)

const walk = (dir) =>
  readdirSync(dir).flatMap((name) => {
    const path = join(dir, name)
    return statSync(path).isDirectory() ? walk(path) : [path]
  })

const checkStyle = (file, css, where) => {
  if (/#[0-9a-fA-F]{3,8}\b|\brgba?\(|\bhsla?\(/.test(css.replace(/&#\d+;/g, ''))) {
    fail(file, `${where}: literal colour, use a var(--cp-*) role`)
  }
  for (const m of css.matchAll(/(?<![\w-])(border-radius|font-size|padding|margin|gap|row-gap|column-gap)\s*:\s*([^;}"]+)/g)) {
    const [, property, value] = m
    for (const px of value.matchAll(/(?<![\w.#-])(\d+(?:\.\d+)?)px/g)) {
      const known = property === 'border-radius' ? radius : property === 'font-size' ? fontSizes : spacing
      if (known.has(px[1])) fail(file, `${where}: ${property}: ${px[1]}px has a token, use var(--cp-*)`)
    }
  }
}

const external = (url) => /^(https?:)?\/\//.test(url)

for (const file of walk(mockups).filter((f) => f.endsWith('.html'))) {
  if (file.includes('/shared/')) continue
  const html = readFileSync(file, 'utf8')
  if (!/<title>[^<]+<\/title>/.test(html)) fail(file, 'missing <title>')
  for (const name of ['mockup:screen', 'mockup:section']) {
    if (!new RegExp(`<meta name="${name}" content="[^"]+"`).test(html)) fail(file, `missing <meta name="${name}">`)
  }
  if (!/data-component="/.test(html)) fail(file, 'no data-component: name what the screen is made of')
  if (/<style[\s>]/.test(html) === false && /<script[^>]*>[^<]+<\/script>/.test(html)) fail(file, 'inline script')

  for (const m of html.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/g)) checkStyle(file, m[1], '<style>')
  for (const m of html.matchAll(/\sstyle="([^"]*)"/g)) checkStyle(file, m[1], 'style attribute')

  for (const m of html.matchAll(/<(link|script|img|a|source)\b[^>]*?\s(href|src)="([^"]*)"/g)) {
    const [, tag, , url] = m
    if (!url || url.startsWith('#') || url.startsWith('data:') || url.startsWith('mailto:')) continue
    if (external(url)) {
      const host = new URL(url.startsWith('//') ? `https:${url}` : url).host
      if (tag === 'a') continue
      if (!fontHosts.includes(host)) fail(file, `${tag} loads ${url} from outside the repo`)
      continue
    }
    const path = resolve(dirname(file), url.split(/[?#]/)[0])
    if (!existsSync(path)) fail(file, `${tag} links ${url}, which does not exist`)
  }
}

if (problems.length) {
  console.error(problems.join('\n'))
  console.error(`\n${problems.length} problem(s) in the mockups.`)
  process.exit(1)
}
console.log('Mockups are consistent.')
