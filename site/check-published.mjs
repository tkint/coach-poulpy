// Run after `npm run build`. Fails when a guideline isn't on the site: a Markdown file outside
// site/ that published.mjs neither publishes nor lists as unpublished, or a built page that no
// link reaches from the home page.
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync, readdirSync } from 'node:fs'
import { join, relative } from 'node:path'
import { shared, unpublished } from './published.mjs'

const problems = []

const tracked = execFileSync('git', ['ls-files', '--', '*.md', ':!:site/**'], { cwd: '..', encoding: 'utf8' })
  .split('\n')
  .filter(Boolean)
for (const file of tracked) {
  const isShared = shared.some((path) => file === path || file.startsWith(`${path}/`))
  if (!isShared && !(file in unpublished)) {
    problems.push(`${file} isn't published: add it to \`shared\` in site/published.mjs, or to \`unpublished\` with the reason.`)
  }
}

const dist = '.vitepress/dist'
const base = process.env.SITE_BASE ?? '/coach-poulpy/'
const pages = (dir) =>
  readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name)
    if (entry.isDirectory()) return pages(path)
    return entry.name.endsWith('.html') && path !== join(dist, '404.html') ? [relative(dist, path)] : []
  })
// A link like /coach-poulpy/docs/brand#moods is the page docs/brand.html; a folder is its index.
const toPage = (href, from) => {
  // The address a built page is served at, so a relative link resolves the way a browser does.
  const served = new URL(base + from.replace(/(^|\/)index\.html$/, '$1').replace(/\.html$/, ''), 'https://site')
  const url = new URL(href, served)
  if (url.origin !== served.origin || !url.pathname.startsWith(base)) return null
  const path = decodeURI(url.pathname).slice(base.length).replace(/\/$/, '/index')
  const page = path === '' ? 'index.html' : path.endsWith('.html') ? path : `${path}.html`
  return existsSync(join(dist, page)) ? page : null
}

const reached = new Set(['index.html'])
const queue = ['index.html']
while (queue.length > 0) {
  const from = queue.shift()
  const html = readFileSync(join(dist, from), 'utf8')
  // src too: the mockup pages show each mockup's HTML in an iframe.
  for (const [, href] of html.matchAll(/(?:href|src)="([^"]+)"/g)) {
    const page = toPage(href, from)
    if (page && !reached.has(page)) {
      reached.add(page)
      queue.push(page)
    }
  }
}
for (const page of pages(dist)) {
  if (!reached.has(page)) problems.push(`${page} is built but no link reaches it: add it to the sidebar in .vitepress/config.mts.`)
}

if (problems.length > 0) {
  console.error(problems.join('\n'))
  process.exit(1)
}
console.log(`${tracked.length} Markdown files checked, ${reached.size} pages reachable.`)
