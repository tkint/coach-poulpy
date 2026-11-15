import source from '../../../../../docs/design-system.md?raw'

// The rules are the Markdown file agents read in the repo (docs/design-system.md). The workshop shows it one topic
// per page: each page names the `## ` sections it holds, and the links are rewritten to work from the workshop.

const SITE = 'https://tkint.github.io/coach-poulpy'
const REPO = 'https://github.com/tkint/coach-poulpy/blob/main'

/** The workshop page that shows each section, by the section's anchor. */
const pages: Record<string, string> = {
  'colour-roles': 'colour',
  'mood-shades': 'colour',
  type: 'type',
  'space-radius-motion': 'space-radius-and-motion',
  components: 'components',
  'base-fields': 'components',
  screens: 'screens',
  websites: 'websites',
  icons: 'icons',
  accessibility: 'accessibility',
}
const anchor = (heading: string) => heading.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')

const linked = (markdown: string) =>
  markdown
    .replace(/\]\(#([^)]+)\)/g, (link, id: string) => (pages[id] ? `](?path=/docs/design-system-rules-${pages[id]}--docs)` : link))
    .replace(/\]\(\.\.\/(design\/[^)]*)\)/g, `](${REPO}/$1)`)
    .replace(/\]\(adr\/([^)]*?)\.md\)/g, `](${SITE}/docs/adr/$1)`)
    .replace(/\]\(\.\/([\w-]+)\.md(#[^)]*)?\)/g, `](${SITE}/docs/$1$2)`)
    .replace(new RegExp(`\\]\\(${SITE}/storybook/\\?path=`, 'g'), '](?path=')

const [intro, ...parts] = source.split(/^(?=## )/m)
const sections = new Map(parts.map((part) => [anchor(part.match(/^## (.+)$/m)![1]), part]))

/** The opening of the rules, under the page's title. */
export const rulesIntro = linked(intro.replace(/^# .+\n+/, ''))

/** The named sections of the rules, in the file's order, ready for `<Markdown>`. */
export const rules = (...headings: string[]) =>
  linked(
    headings
      .map((heading) => {
        const part = sections.get(anchor(heading))
        if (!part) throw new Error(`docs/design-system.md has no "## ${heading}" section`)
        return part
      })
      .join('\n'),
  )

/** Every section has a page, so a new `## ` in the file fails the build until it is given one. */
for (const id of sections.keys()) if (!pages[id]) throw new Error(`Give the "${id}" section of docs/design-system.md a workshop page in rules.ts`)
