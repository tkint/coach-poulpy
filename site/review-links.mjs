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

// The page group of a web mockup (keep in step with groupOf in .vitepress/mockup-labels.ts): each web
// app view has its own page, named from the file, and others keep the section in their metadata.
const webGroups = [
  [(screen) => screen === 'admin-shell' || screen.startsWith('admin-audit'), 'Admin · Audit'],
  [(screen) => screen.startsWith('admin-sign-in'), 'Admin · Sign in'],
  [(screen) => screen.startsWith('admin-accounts'), 'Admin · Accounts'],
  [(screen) => screen.startsWith('admin-settings'), 'Admin · Settings'],
  [(screen) => screen.startsWith('admin-status'), 'Admin · Status'],
  [(screen) => screen.startsWith('admin-catalog-exercise'), 'Admin · Exercise'],
  [(screen) => screen.startsWith('admin-catalog'), 'Admin · Catalog'],
  [(screen) => screen.startsWith('app-sign-in'), 'Web app · Sign in'],
  [(screen) => screen.startsWith('app-status'), 'Web app · Status'],
  [(screen) => screen === 'app-profile', 'Web app · Profile'],
  [(screen) => screen === 'app-workouts', 'Web app · Workouts'],
  [(screen) => screen.startsWith('app-workout'), 'Web app · Workout'],
]
const webGroup = (screen, section) => webGroups.find(([matches]) => matches(screen))?.[1] ?? section

// A mockup shows on the page of its section, with the other states of it (keep in step with
// .vitepress/mockup-labels.ts). The file may be gone at the head, so its tag is read from the base then.
function mockupPage(file, stage, device) {
  let html = ''
  for (const ref of [head, base]) {
    try {
      html = git('show', `${ref}:${file}`)
      break
    } catch {}
  }
  const section = html.match(/<meta name="mockup:section" content="([^"]*)"/)?.[1] ?? ''
  const group = device === 'tablet' ? `Tablet · ${section}` : device === 'web' ? webGroup(file.split('/').at(-1).replace(/\.html$/, ''), section) : section
  const slug = group.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
  return `mockups/${device === 'web' ? 'web' : 'mobile'}/${stage}/${slug}`
}

// The page a changed file shows up on, or null when no page shows it.
function pageOf(file) {
  let m
  if ((m = file.match(/^design\/mockups\/(agreed|drafts)\/(\w+)\/(.+)\.html$/))) {
    return { group: 'mockups', page: mockupPage(file, m[1], m[2]) }
  }
  if (file.startsWith('design/mockups/shared/')) return { group: 'mockups', page: 'mockups/' }
  if (file === 'design/tokens.json') return { group: 'design', page: 'tokens' }
  if (file === 'design/build-poulpy.mjs' || file.startsWith('design/poulpy/')) return { group: 'design', page: 'drawings' }
  if (file.startsWith('site/.vitepress/theme/brand-book/')) return { group: 'design', page: 'brand-book' }
  if (!file.endsWith('.md') || file.includes('[')) return null // a [param].md route has no page of its own
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
  // Only a line that starts with the keyword, as the PR template does, so prose quoting another PR's
  // "Closes #n" doesn't count.
  const closes = new Set([...(process.env.PR_BODY ?? '').matchAll(/^\s*(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?) #(\d+)/gim)].map((m) => m[1]))
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
