import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs'
import { dirname, join, posix, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

// A heading's anchor, as VitePress makes it.
const slug = (title: string) => title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')

const here = dirname(fileURLToPath(import.meta.url))
// copy-docs.mjs mirrors the repo's shared Markdown into site/ at the same paths, so a path from
// the site root is also a path from the repo root.
const siteRoot = resolve(here, '..')
const repoRoot = resolve(siteRoot, '..')
const repoUrl = 'https://github.com/tkint/coach-poulpy'
// A PR preview is built under its own folder of the site, given by the Docs site workflow.
const base = process.env.SITE_BASE ?? '/'

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

// The "## " headings of a page, as sidebar entries that jump to them, so a components page lists its
// components in the menu without a second list to keep in step.
const headings = (page: string) =>
  [...readFileSync(join(siteRoot, `${page}.md`), 'utf8').matchAll(/^## (.+)$/gm)].map((m) => ({
    text: m[1],
    link: `/${page}#${slug(m[1])}`,
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
      ['link', { rel: 'icon', href: `${base}poulpy.png` }],
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
    // The web design system (web/ui, ADR 0023) is imported from its source; dedupe keeps its
    // components on the site's own copy of Vue and of the Lucide icons (ADR 0024), since web/ui's
    // own packages are not installed when the site is built.
    vite: { server: { fs: { allow: [repoRoot] } }, resolve: { dedupe: ['vue', '@lucide/vue'] } },
    themeConfig: {
      logo: '/poulpy.png',
      // Pages are grouped by reader (docs/agents/writing-docs.md): Product says what the app does and
      // its rules, Technical how it is built. Design and Brand live in the component workshop
      // (ADR 0028, ADR 0029, ADR 0032), so their menus link into it.
      nav: [
        { text: 'Product', link: '/docs/roadmap' },
        { text: 'Technical', link: '/docs/architecture' },
        {
          text: 'Design',
          items: [
            { text: 'Rules, components and screens', link: '/storybook/index.html?path=/docs/design-system-introduction--docs', target: '_blank', rel: 'noopener' },
            { text: 'Generating tokens and fonts', link: '/design/README' },
            { text: 'Body map shapes: credits', link: '/design/body-map-source/NOTICE' },
          ],
        },
        { text: 'Brand', link: '/storybook/index.html?path=/docs/brand-introduction--docs', target: '_blank', rel: 'noopener' },
        { text: 'Contributing', link: '/CONTRIBUTING' },
        { text: 'Translations', link: '/translations' },
      ],
      // Each section shows only its own menu: a page gets the menu of the longest key its path starts
      // with, so every page of a section is listed under one of its keys. Each menu is one group
      // titled with the section's name.
      sidebar: Object.fromEntries(
        [
          // Technical comes first: VitePress tries the keys with the most slashes first, then in this
          // order, so '/docs/admin-internals' must be tried before Product's '/docs/admin'.
          [
            'Technical',
            ['/docs/architecture', '/docs/the-api', '/docs/sync', '/docs/admin-internals', '/docs/deploying-the-server', '/docs/releasing', '/decisions/', '/docs/adr/', '/docs/research/'],
            [
              { text: 'Architecture', link: '/docs/architecture' },
              { text: 'The API', link: '/docs/the-api' },
              { text: 'Sync', link: '/docs/sync' },
              { text: 'Admin internals', link: '/docs/admin-internals' },
              { text: 'Deploying the server', link: '/docs/deploying-the-server' },
              { text: 'Releasing a beta', link: '/docs/releasing' },
              { text: 'Decisions', link: '/decisions/', collapsed: true, items: adrs },
              { text: 'Research', collapsed: true, items: research },
            ],
          ],
          [
            'Product',
            ['/docs/roadmap', '/CONTEXT', '/docs/accounts', '/docs/profile-and-settings', '/docs/workout-file', '/docs/web-app', '/docs/admin'],
            [
              { text: 'Roadmap', link: '/docs/roadmap' },
              { text: 'Accounts and sign-in', link: '/docs/accounts' },
              { text: 'Profile and settings', link: '/docs/profile-and-settings' },
              { text: 'The workout file', link: '/docs/workout-file' },
              { text: 'The web app', link: '/docs/web-app' },
              { text: 'The admin area', link: '/docs/admin' },
              { text: 'Glossary', link: '/CONTEXT' },
            ],
          ],
          [
            'Design',
            ['/design/'],
            [
              { text: 'Rules, components and screens', link: '/storybook/index.html?path=/docs/design-system-introduction--docs', target: '_blank', rel: 'noopener' },
              { text: 'Generating tokens and fonts', link: '/design/README' },
              { text: 'Body map shapes: credits', link: '/design/body-map-source/NOTICE' },
            ],
          ],
          [
            'Translations',
            ['/translations', '/i18n/'],
            [
              { text: 'Proofread and translate', link: '/translations' },
              { text: 'English rules and notes', link: '/i18n/glossary/en' },
              { text: 'Français : règles et vocabulaire', link: '/i18n/glossary/fr' },
              { text: 'Translation files', link: '/i18n/README' },
            ],
          ],
          [
            'Contributing',
            ['/CONTRIBUTING', '/CLAUDE', '/docs/agents/'],
            [
              { text: 'Issues, skills and PRs', link: '/CONTRIBUTING' },
              { text: 'House rules', link: '/CLAUDE' },
              { text: 'Writing docs', link: '/docs/agents/writing-docs' },
              { text: 'Agent skills', link: '/docs/agents/skills' },
            ],
          ],
        ].flatMap(([title, prefixes, items]) => {
          // The menu opens with the section's title (the same word as the top nav), then its pages.
          const menu = [{ text: title, items }]
          return (prefixes as string[]).map((prefix) => [prefix, menu])
        }),
      ),
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
