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
const base = '/coach-poulpy/'
const pages = (dir) =>
  readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name)
    if (entry.isDirectory()) return pages(path)
    return entry.name.endsWith('.html') && path !== join(dist, '404.html') ? [relative(dist, path)] : []
  })
// A link like /coach-poulpy/docs/brand#moods is the page docs/brand.html; a folder is its index.
const toPage = (href) => {
  const path = decodeURI(href.split(/[?#]/)[0]).slice(base.length).replace(/\/$/, '/index')
  const page = path === '' ? 'index.html' : path.endsWith('.html') ? path : `${path}.html`
  return existsSync(join(dist, page)) ? page : null
}

const reached = new Set(['index.html'])
const queue = ['index.html']
while (queue.length > 0) {
  const html = readFileSync(join(dist, queue.shift()), 'utf8')
  for (const [, href] of html.matchAll(/href="([^"]+)"/g)) {
    if (!href.startsWith(base)) continue
    const page = toPage(href)
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
