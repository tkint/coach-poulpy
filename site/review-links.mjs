// Lists the docs pages a PR changes, with links into its preview, for the "Review in the docs"
// comment the Docs site workflow posts on each PR.
// Usage: node review-links.mjs <base-sha> <head-sha> <preview-url> <built-site-dir>
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import { shared } from './published.mjs'

const [base, head, previewUrl, dist] = process.argv.slice(2)

const groups = {
  mockups: 'Mockups',
  decisions: 'Decisions',
  guidelines: 'Specs and guidelines',
  design: 'Design system and brand',
}

// The page a changed file shows up on, or null when no page shows it.
function pageOf(file) {
  let m
  if ((m = file.match(/^design\/mockups\/(agreed|drafts)\/(\w+)\/(.+)\.html$/))) {
    return { group: 'mockups', page: `mockups/${m[1]}/${m[2]}/${m[3]}` }
  }
  if (file.startsWith('design/mockups/shared/')) return { group: 'mockups', page: 'mockups/' }
  if (file === 'design/tokens.json') return { group: 'design', page: 'tokens' }
  if (file === 'design/build-poulpy.mjs' || file.startsWith('design/poulpy/')) return { group: 'design', page: 'drawings' }
  if (file.startsWith('site/public/brand/')) return { group: 'design', page: 'brand-book' }
  if (!file.endsWith('.md')) return null
  const isShared = shared.some((path) => file === path || file.startsWith(`${path}/`))
  const path = isShared ? file : file.startsWith('site/') ? file.slice('site/'.length) : null
  if (path === null) return null
  const page = path.replace(/\.md$/, '').replace(/(^|\/)index$/, '$1')
  const group = path.startsWith('mockups/') ? 'mockups' : path.startsWith('docs/adr/') ? 'decisions' : /^(docs\/brand|docs\/design-system|brand-book|drawings|tokens)/.test(path) ? 'design' : 'guidelines'
  return { group, page }
}

const htmlOf = (page) => join(dist, page.endsWith('/') || page === '' ? `${page}index.html` : `${page}.html`)
const titleIn = (file) => readFileSync(file, 'utf8').match(/<title>([^<|]*?)(?: \| [^<]*)?<\/title>/)?.[1]
// The page's own title, or for a mockup the title in its file, as the site's mockup pages have none.
function titleOf(page, file) {
  const title = titleIn(htmlOf(page))
  if (title && title !== 'Coach Poulpy') return title
  if (file.startsWith('design/mockups/') && file.endsWith('.html')) return titleIn(file) ?? page
  return page || 'Home'
}

const lines = execFileSync('git', ['diff', '--name-status', '-M', `${base}...${head}`], { encoding: 'utf8' }).split('\n').filter(Boolean)
const found = new Map(Object.keys(groups).map((group) => [group, new Map()]))
const removed = []
for (const line of lines) {
  const [status, ...paths] = line.split('\t')
  const file = paths.at(-1)
  const target = pageOf(file)
  if (!target) continue
  if (status === 'D' || !existsSync(htmlOf(target.page))) {
    removed.push(file)
    continue
  }
  const agreed = status.startsWith('R') && paths[0].includes('/drafts/') && file.includes('/agreed/')
  const verb = status === 'A' ? 'new' : agreed ? 'agreed' : status.startsWith('R') ? 'moved' : 'changed'
  found.get(target.group).set(target.page, { verb, title: titleOf(target.page, file) })
}

const out = ['### Review in the docs', '']
let any = false
for (const [group, pages] of found) {
  if (pages.size === 0) continue
  any = true
  out.push(`**${groups[group]}**`, '')
  for (const [page, { verb, title }] of pages) out.push(`- [${title}](${previewUrl}${page}) (${verb})`)
  out.push('')
}
if (removed.length > 0) {
  any = true
  out.push('**Removed from the site**', '', ...removed.map((file) => `- \`${file}\``), '')
}
if (!any) out.push('This PR changes no page of the docs site.', '')
out.push(`Whole site: ${previewUrl}`)
console.log(out.join('\n'))
