// Lists the docs pages a PR changes, with links into its preview, for the "Review in the docs"
// comment the Docs site workflow posts on each PR.
// Usage: node review-links.mjs <base-sha> <head-sha> <preview-url> <built-site-dir>
// with the PR's body in PR_BODY, to find the issues it closes and the mockups drawn for them.
// It's exhaustive so that agents don't have to write a review comment of their own.
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import { shared } from './published.mjs'

const [base, head, previewUrl, dist] = process.argv.slice(2)

const groups = {
  built: 'Mockups this PR builds',
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
// Pages whose own title is the site's name, named as in the sidebar.
const names = { CLAUDE: 'House rules', CONTEXT: 'Glossary' }
function titleOf(page, file) {
  if (names[page]) return names[page]
  const title = titleIn(htmlOf(page))
  if (title && title !== 'Coach Poulpy') return title
  if (file.startsWith('design/mockups/') && file.endsWith('.html')) return titleIn(file) ?? page
  return page || 'Home'
}

const git = (...args) => execFileSync('git', args, { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 })

// The sections and glossary terms a changed Markdown file touches, each linked to its anchor.
// Headings are matched to the built page's ids in order, so the anchors are the site's own.
function sectionsOf(file, page) {
  const ids = [...readFileSync(htmlOf(page), 'utf8').matchAll(/<h[23] id="([^"]+)"/g)].map((m) => m[1])
  const source = git('show', `${head}:${file}`).split('\n')
  const headings = []
  let fenced = false
  source.forEach((line, i) => {
    if (/^(```|~~~)/.test(line)) fenced = !fenced
    else if (!fenced && /^#{2,3} /.test(line)) headings.push({ line: i + 1, text: line.replace(/^#+ /, ''), id: ids[headings.length] })
  })
  const changed = []
  for (const [, start, count] of git('diff', '-U0', `${base}...${head}`, '--', file).matchAll(/^@@ [^+]*\+(\d+)(?:,(\d+))? @@/gm)) {
    for (let n = Number(start); n < Number(start) + Number(count ?? 1); n++) changed.push(n)
  }
  const sections = new Map()
  const terms = new Set()
  for (const n of changed) {
    const heading = headings.filter((h) => h.line <= n).at(-1)
    if (heading?.id) sections.set(heading.id, heading.text)
    const line = source[n - 1] ?? ''
    // A glossary row (| term | ... |) or a CONTEXT.md term (**Term**: on the line, or the closest above).
    const row = line.match(/^\|\s*([^|]+?)\s*\|/)
    if (row && !/^-+$/.test(row[1])) terms.add(row[1].replace(/[*`]/g, ''))
    for (let k = n - 1; k >= 0 && source[k].trim() !== '' && !source[k].startsWith('#'); k--) {
      const term = source[k].match(/^\*\*(.+?)\*\*:/)
      if (term) {
        terms.add(term[1])
        break
      }
    }
  }
  return { sections: [...sections], terms: [...terms] }
}

// The agreed mockups drawn for an issue the PR closes, so an implementation PR links the screens it builds.
function builtMockups() {
  const closes = new Set([...(process.env.PR_BODY ?? '').matchAll(/\b(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?) #(\d+)/gi)].map((m) => m[1]))
  if (closes.size === 0) return []
  const dir = 'design/mockups/agreed'
  if (!existsSync(dir)) return []
  return git('ls-files', dir).split('\n').filter((file) => file.endsWith('.html')).flatMap((file) => {
    const issue = readFileSync(file, 'utf8').match(/<meta name="mockup:issue" content="(\d+)"/)?.[1]
    if (!closes.has(issue)) return []
    const page = pageOf(file).page
    return [{ page, title: titleOf(page, file), issue }]
  })
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
  const detail = file.endsWith('.md') && status !== 'A' ? sectionsOf(file, target.page) : { sections: [], terms: [] }
  found.get(target.group).set(target.page, { verb, title: titleOf(target.page, file), ...detail })
}

const out = ['### Review in the docs', '']
let any = false
for (const { page, title, issue } of builtMockups()) {
  if (!found.get('mockups').has(page)) found.get('built').set(page, { verb: `#${issue}`, title, sections: [], terms: [] })
}
for (const [group, pages] of found) {
  if (pages.size === 0) continue
  any = true
  out.push(`**${groups[group]}**`, '')
  for (const [page, { verb, title, sections, terms }] of pages) {
    out.push(`- [${title}](${previewUrl}${page}) (${verb})`)
    if (sections.length > 0) out.push(`  - Sections: ${sections.map(([id, text]) => `[${text}](${previewUrl}${page}#${id})`).join(', ')}`)
    if (terms.length > 0) out.push(`  - Entries: ${terms.join(', ')}`)
  }
  out.push('')
}
if (removed.length > 0) {
  any = true
  out.push('**Removed from the site**', '', ...removed.map((file) => `- \`${file}\``), '')
}
if (!any) out.push('This PR changes no page of the docs site.', '')
out.push(`Whole site: ${previewUrl}`)
console.log(out.join('\n'))
