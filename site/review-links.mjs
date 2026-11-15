// Lists the docs pages and Storybook stories a PR changes, with links into its preview, for the "Review in the docs"
// comment the Docs site workflow posts on each PR.
// Usage: node review-links.mjs <base-sha> <head-sha> <preview-url> <built-site-dir>
// with the PR's body in PR_BODY, to find the issues it closes and the mockup stories drawn for them.
// It's exhaustive so that agents don't have to write a review comment of their own.
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import { shared } from './published.mjs'

const [base, head, previewUrl, dist] = process.argv.slice(2)

const groups = {
  built: 'Mockups this PR builds',
  decisions: 'Decisions',
  guidelines: 'Specs and guidelines',
  design: 'Design system and brand',
}

// The component workshop is published under /storybook/ (ADR 0028). A changed story, its doc or
// mockup helper, or the Vue component of the same name, links the stories it shows (ids as Storybook makes them).
const workshop = 'web/storybook/stories/'
const slug = (text) => text.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
const storyIdOf = (name) => slug(name.replace(/([a-z0-9])([A-Z])/g, '$1 $2'))
const storyFilesOf = (file) => {
  if (file.endsWith('.stories.ts') || file.endsWith('.mdx')) return file.startsWith(workshop) ? [file] : []
  // Generated drawings and the 3D model show on the Brand pages.
  if (file === 'design/build-poulpy.mjs' || file.startsWith('design/poulpy/')) return [`${workshop}brand/Drawings.mdx`, `${workshop}PoulpyReference.stories.ts`]
  if (file.startsWith('design/poulpy-3d/')) return [`${workshop}brand/Poulpy3d.mdx`]
  if (file.startsWith(`${workshop}brand/brand-book/`)) return [`${workshop}brand/BrandBook.mdx`]
  const stem = file.replace(/^.*\//, '').replace(/\.[^.]+$/, '')
  if (file.startsWith('web/ui/src/components/')) return git('ls-files', workshop).split('\n').filter((f) => f.endsWith(`/${stem}.stories.ts`))
  if (!file.startsWith(workshop)) return []
  // A doc or helper next to the stories that use it.
  const dir = file.slice(0, file.lastIndexOf('/') + 1)
  return git('ls-files', dir).split('\n').filter((f) => f.endsWith('.stories.ts') && f.slice(dir.length).indexOf('/') === -1 && new RegExp(`/${stem}(\\.\\w+)?'`).test(show(f)))
}
const show = (file) => {
  try {
    return git('show', `${head}:${file}`)
  } catch {
    return ''
  }
}
// The workshop entry of a story file: its title, a link to its docs page and to each story.
function workshopEntry(file) {
  const source = show(file)
  if (source === '') return null // removed in this PR
  const title = source.match(/(?:title: |<Meta title=)['"]([^'"]+)['"]/)?.[1]
  if (!title) return null
  const id = title.split('/').map(slug).join('-')
  const base = `${previewUrl}storybook/index.html?path=`
  const stories = file.endsWith('.mdx') ? [] : [...source.matchAll(/^export const (\w+)/gm)].map((m) => m[1])
  return { title, docs: `${base}/docs/${id}--docs`, stories: stories.map((name) => [name, `${base}/story/${id}--${storyIdOf(name)}`]) }
}

// The page a changed file shows up on, or null when no page shows it.
function pageOf(file) {
  if (!file.endsWith('.md') || file.includes('[')) return null // a [param].md route has no page of its own
  const isShared = shared.some((path) => file === path || file.startsWith(`${path}/`))
  const path = isShared ? file : file.startsWith('site/') ? file.slice('site/'.length) : null
  if (path === null) return null
  const page = path.replace(/\.md$/, '').replace(/(^|\/)index$/, '$1')
  const group = path.startsWith('docs/adr/') ? 'decisions' : /^(docs\/brand|docs\/design-system|docs\/poulpy-3d)/.test(path) ? 'design' : 'guidelines'
  return { group, page }
}

const htmlOf = (page) => join(dist, page.endsWith('/') || page === '' ? `${page}index.html` : `${page}.html`)
const titleIn = (file) => readFileSync(file, 'utf8').match(/<title>([^<|]*?)(?: \| [^<]*)?<\/title>/)?.[1]
// The page's own title. Pages whose own title is the site's name, named as in the sidebar.
const names = { CLAUDE: 'House rules', CONTEXT: 'Glossary' }
function titleOf(page) {
  if (names[page]) return names[page]
  const title = titleIn(htmlOf(page))
  if (title && title !== 'Coach Poulpy') return title
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

// The agreed mockup stories drawn for an issue the PR closes (`parameters.mockup.issue`, on the story or on its
// file's meta), so an implementation PR links the screens it builds.
function builtMockups() {
  // Only a line that starts with the keyword, as the PR template does, so prose quoting another PR's
  // "Closes #n" doesn't count.
  const closes = new Set([...(process.env.PR_BODY ?? '').matchAll(/^\s*(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?) #(\d+)/gim)].map((m) => m[1]))
  if (closes.size === 0) return []
  const issueIn = (text) => text.match(/mockup: \{ issue: (\d+) \}/)?.[1]
  return git('ls-files', workshop).split('\n').filter((file) => file.endsWith('.stories.ts')).flatMap((file) => {
    const source = show(file)
    if (!/tags: \[[^\]]*'agreed'/.test(source)) return []
    const entry = workshopEntry(file)
    if (!entry) return []
    const [top, ...stories] = source.split(/^(?=export const \w+)/m)
    const fileIssue = issueIn(top)
    return stories.flatMap((chunk) => {
      const name = chunk.match(/^export const (\w+)/)[1]
      const issue = issueIn(chunk) ?? fileIssue
      const link = entry.stories.find(([story]) => story === name)?.[1]
      if (!closes.has(issue) || !link) return []
      return [{ link, title: `${entry.title} › ${chunk.match(/name: ["']([^"']+)["']/)?.[1] ?? name}`, issue }]
    })
  })
}

const lines = execFileSync('git', ['diff', '--name-status', '-M', `${base}...${head}`], { encoding: 'utf8' }).split('\n').filter(Boolean)
const found = new Map(Object.keys(groups).map((group) => [group, new Map()]))
const removed = []
const workshopFiles = new Set()
for (const line of lines) {
  const [status, ...paths] = line.split('\t')
  const file = paths.at(-1)
  if (status !== 'D') storyFilesOf(file).forEach((f) => workshopFiles.add(f))
  const target = pageOf(file)
  if (!target) continue
  if (status === 'D' || !existsSync(htmlOf(target.page))) {
    removed.push(file)
    continue
  }
  const verb = status === 'A' ? 'new' : status.startsWith('R') ? 'moved' : 'changed'
  const detail = file.endsWith('.md') && status !== 'A' ? sectionsOf(file, target.page) : { sections: [], terms: [] }
  found.get(target.group).set(target.page, { verb, title: titleOf(target.page), ...detail })
}

const out = ['### Review in the docs', '']
let any = false
for (const { link, title, issue } of builtMockups()) {
  found.get('built').set(link, { verb: `#${issue}`, title, sections: [], terms: [] })
}
for (const [group, pages] of found) {
  if (pages.size === 0) continue
  any = true
  out.push(`**${groups[group]}**`, '')
  for (const [page, { verb, title, sections, terms }] of pages) {
    out.push(`- [${title}](${page.startsWith('http') ? page : `${previewUrl}${page}`}) (${verb})`)
    if (sections.length > 0) out.push(`  - Sections: ${sections.map(([id, text]) => `[${text}](${previewUrl}${page}#${id})`).join(', ')}`)
    if (terms.length > 0) out.push(`  - Entries: ${terms.join(', ')}`)
  }
  out.push('')
}
const entries = [...workshopFiles].sort().map(workshopEntry).filter(Boolean)
if (entries.length > 0) {
  any = true
  out.push('**Storybook**', '')
  for (const { title, docs, stories } of entries) {
    out.push(`- [${title}](${docs})`)
    if (stories.length > 0) out.push(`  - Stories: ${stories.map(([name, link]) => `[${name}](${link})`).join(', ')}`)
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
