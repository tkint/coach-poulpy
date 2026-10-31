import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs'
import { dirname, join, posix, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'
import { devices, listMockups, stages } from './mockups.mts'

const here = dirname(fileURLToPath(import.meta.url))
// copy-docs.mjs mirrors the repo's shared Markdown into site/ at the same paths, so a path from
// the site root is also a path from the repo root.
const siteRoot = resolve(here, '..')
const repoRoot = resolve(siteRoot, '..')
const repoUrl = 'https://github.com/tkint/coach-poulpy'
// A PR preview is built under its own folder of the site, given by the Docs site workflow.
const base = process.env.SITE_BASE ?? '/coach-poulpy/'

const isPage = (path: string) => existsSync(join(siteRoot, path)) && path.endsWith('.md')

const adrs = readdirSync(join(repoRoot, 'docs/adr'))
  .filter((file) => file.endsWith('.md'))
  .sort()
  .map((file) => {
    const title = readFileSync(join(repoRoot, 'docs/adr', file), 'utf8').match(/^# (.+)$/m)?.[1] ?? file
    return { text: `${file.slice(0, 4)} · ${title}`, link: `/docs/adr/${file.replace(/\.md$/, '')}` }
  })

const research = readdirSync(join(repoRoot, 'docs/research'))
  .filter((file) => file.endsWith('.md'))
  .sort()
  .map((file) => {
    const title = readFileSync(join(repoRoot, 'docs/research', file), 'utf8').match(/^# (.+)$/m)?.[1] ?? file
    return { text: title, link: `/docs/research/${file.replace(/\.md$/, '')}` }
  })

// Mockups › Agreed and Drafts, each listing its screens, so drafts and agreed screens never mix.
const mockups = listMockups()
const mockupSidebar = (Object.keys(stages) as (keyof typeof stages)[]).map((stage) => ({
  text: stages[stage],
  link: `/mockups/${stage}`,
  collapsed: true,
  items: mockups
    .filter((m) => m.stage === stage)
    .map((m) => ({ text: m.device === 'phone' ? m.title : `${devices[m.device]} · ${m.title}`, link: m.link })),
}))

/**
 * A relative link to a file the site doesn't publish (code, scripts, agent docs) becomes a link
 * to that file on GitHub, so the same Markdown works in the repo and on the site.
 */
function linksToGitHub(md: any) {
  md.core.ruler.push('links-to-github', (state: any) => {
    const from = posix.dirname(state.env.relativePath ?? '')
    for (const block of state.tokens) {
      for (const token of block.children ?? []) {
        if (token.type !== 'link_open') continue
        const href: string = token.attrGet('href') ?? ''
        if (/^([a-z]+:|#|\/)/i.test(href)) continue
        const [path, hash] = href.split('#')
        const target = posix.normalize(posix.join(from, path))
        if (isPage(target)) continue
        const file = join(repoRoot, target)
        const isDir = existsSync(file) && statSync(file).isDirectory()
        token.attrSet('href', `${repoUrl}/${isDir ? 'tree' : 'blob'}/main/${target}${hash ? `#${hash}` : ''}`)
      }
    }
  })
}

export default withMermaid(
  defineConfig({
    title: 'Coach Poulpy',
    description: 'Architecture, design system and brand of Coach Poulpy',
    lang: 'en',
    base,
    srcExclude: ['**/node_modules/**', 'README.md'],
    cleanUrls: true,
    head: [
      ['link', { rel: 'icon', href: `${base}poulpy.svg` }],
      ['link', { rel: 'preconnect', href: 'https://fonts.googleapis.com' }],
      ['link', { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' }],
      [
        'link',
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Baloo+2:wght@700;800&family=Nunito:wght@400;500;700;800&display=swap',
        },
      ],
    ],
    markdown: { config: linksToGitHub },
    vite: { server: { fs: { allow: [repoRoot] } } },
    themeConfig: {
      logo: '/poulpy.svg',
      nav: [
        { text: 'Brand', link: '/docs/brand' },
        { text: 'Design system', link: '/docs/design-system' },
        { text: 'Architecture', link: '/docs/architecture' },
        { text: 'Translations', link: '/translations' },
        { text: 'Contributing', link: '/CONTRIBUTING' },
      ],
      sidebar: [
        {
          text: 'Brand',
          items: [
            { text: 'Poulpy and his moods', link: '/docs/brand' },
            { text: 'Drawings', link: '/drawings' },
            { text: 'Brand book', link: '/brand-book' },
            { text: 'Brand decisions', link: '/docs/brand-decisions' },
          ],
        },
        {
          text: 'Design system',
          items: [
            { text: 'How to use it', link: '/docs/design-system' },
            { text: 'Tokens', link: '/tokens' },
            { text: 'Generating tokens and fonts', link: '/design/README' },
            { text: 'Mockups', link: '/mockups/', collapsed: false, items: mockupSidebar },
          ],
        },
        {
          text: 'Architecture',
          items: [
            { text: 'Overview', link: '/docs/architecture' },
            { text: 'Glossary', link: '/CONTEXT' },
            { text: 'Decisions', link: '/decisions/', collapsed: true, items: adrs },
            { text: 'Research', collapsed: true, items: research },
          ],
        },
        {
          text: 'Translations',
          items: [
            { text: 'Proofread and translate', link: '/translations' },
            { text: 'English rules and notes', link: '/i18n/glossary/en' },
            { text: 'Français : règles et vocabulaire', link: '/i18n/glossary/fr' },
            { text: 'Translation files', link: '/i18n/README' },
          ],
        },
        {
          text: 'Contributing',
          items: [
            { text: 'Issues, skills and PRs', link: '/CONTRIBUTING' },
            { text: 'House rules', link: '/CLAUDE' },
            { text: 'Rules by area', link: '/docs/agents/areas' },
            { text: 'Releasing a beta', link: '/docs/releasing' },
            { text: 'Issue tracker', link: '/docs/agents/issue-tracker' },
            { text: 'Triage labels', link: '/docs/agents/triage-labels' },
            { text: 'Domain docs', link: '/docs/agents/domain' },
          ],
        },
      ],
      socialLinks: [{ icon: 'github', link: repoUrl }],
      search: { provider: 'local' },
      editLink: {
        // Runs in the browser, so it can't use this file's constants.
        pattern: ({ filePath }) => {
          // Pages copied from the rest of the repo; keep in step with `shared` in published.mjs.
          const shared = /^(CONTEXT\.md|CONTRIBUTING\.md|CLAUDE\.md|docs\/|design\/|i18n\/)/.test(filePath)
          return `https://github.com/tkint/coach-poulpy/edit/main/${shared ? '' : 'site/'}${filePath}`
        },
        text: 'Edit this page on GitHub',
      },
      outline: [2, 3],
    },
  }),
)
