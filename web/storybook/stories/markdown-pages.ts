import brand from './brand/brand.md?raw'
import brandDecisions from './brand/brand-decisions.md?raw'
import designSystem from './design-system/design-system.md?raw'
import poulpy3d from './brand/poulpy-3d.md?raw'

// The design system rules and the brand are Markdown files that live here, in the workshop, and that agents read in the
// repo (design-system/design-system.md, brand/brand.md, brand/brand-decisions.md, brand/poulpy-3d.md; ADR 0032). The workshop
// shows them one topic per page: each page names the `## ` sections it holds, and the links are rewritten to work from the workshop.

const SITE = 'https://tkint.github.io/coach-poulpy'
const REPO = 'https://github.com/tkint/coach-poulpy/blob/main'

const anchor = (heading: string) => heading.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
const docsPage = (id: string) => `?path=/docs/${id}--docs`

/** Each Markdown file shown in the workshop: its text, and the workshop page of each `## ` section by its anchor. */
const files = {
  'design-system': {
    source: designSystem,
    home: 'design-system-introduction',
    pages: {
      'colour-roles': 'design-system-rules-colour',
      'mood-shades': 'design-system-rules-colour',
      type: 'design-system-rules-type',
      'space-radius-motion': 'design-system-rules-space-radius-and-motion',
      components: 'design-system-rules-components',
      'base-fields': 'design-system-rules-components',
      screens: 'design-system-rules-screens',
      websites: 'design-system-rules-websites',
      'web-sizes': 'design-system-rules-websites',
      icons: 'design-system-rules-icons',
      accessibility: 'design-system-rules-accessibility',
    } as Record<string, string>,
  },
  brand: {
    source: brand,
    home: 'brand-introduction',
    pages: {
      'character-rules': 'brand-character',
      tentacles: 'brand-character',
      moods: 'brand-moods',
      'when-each-drawing-shows': 'brand-moods',
      'the-ember-side': 'brand-moods',
      'inclusive-by-default': 'brand-inclusive-by-default',
      voice: 'brand-voice-logo-and-type',
      'logo-and-type': 'brand-voice-logo-and-type',
      'poulpy-in-3d': 'brand-poulpy-in-3d',
      'ideas-for-later': 'brand-ideas-for-later',
    } as Record<string, string>,
  },
  'brand-decisions': { source: brandDecisions, home: 'brand-decisions', pages: {} as Record<string, string> },
  'poulpy-3d': {
    source: poulpy3d,
    home: 'brand-poulpy-in-3d',
    // Each of his moves has a page of its own, so no page runs two of them at once.
    pages: {
      'the-rules-of-the-shape': 'brand-poulpy-in-3d',
      'his-six-moods': 'brand-poulpy-in-3d',
      'his-twelve-poses': 'brand-poulpy-in-3d',
      'his-pictures': 'brand-poulpy-in-3d',
      'his-moves': 'brand-poulpy-in-3d',
      'turning-in-short-steps': 'brand-poulpy-moves-turning',
      'little-jumps': 'brand-poulpy-moves-little-jumps',
      'jumping-around': 'brand-poulpy-moves-jumping-around',
      'the-champion-jumps-for-joy': 'brand-poulpy-moves-the-champion',
      'the-cocky-one-winks': 'brand-poulpy-moves-the-cocky-wink',
      'using-the-model': 'brand-poulpy-in-3d',
      'changing-the-design': 'brand-poulpy-in-3d',
    } as Record<string, string>,
  },
}
type File = keyof typeof files

/** The workshop link for a section of one of the files, or its home page. */
const pageOf = (file: File, hash?: string) => {
  const { home, pages } = files[file]
  return hash && pages[hash] ? docsPage(pages[hash]) : `${docsPage(home)}${hash ? `#${hash}` : ''}`
}

const linked = (file: File, markdown: string) =>
  markdown
    .replace(/\]\(#([^)]+)\)/g, (_, hash: string) => `](${pageOf(file, hash)})`)
    .replace(/\]\((?:\.|\.\.\/(?:brand|design-system))\/([\w-]+)\.md(?:#([^)]*))?\)/g, (link, name: string, hash?: string) =>
      name in files ? `](${pageOf(name as File, hash)})` : link,
    )
    .replace(/\]\(\.\.\/\.\.\/\.\.\/\.\.\/docs\/adr\/\)/g, `](${SITE}/decisions/)`)
    .replace(/\]\(\.\.\/\.\.\/\.\.\/\.\.\/docs\/adr\/([^)]*?)\.md\)/g, `](${SITE}/docs/adr/$1)`)
    .replace(/\]\(\.\.\/\.\.\/\.\.\/\.\.\/docs\/([\w-]+)\.md(#[^)]*)?\)/g, `](${SITE}/docs/$1$2)`)
    .replace(/\]\(\.\.\/\.\.\/\.\.\/\.\.\/([^)]*)\)/g, `](${REPO}/$1)`)
    .replace(new RegExp(`\\]\\(${SITE}/storybook/\\?path=`, 'g'), '](?path=')

/** The Markdown of a file, cut at its `## ` headings, for the pages that show it. */
export function markdownPages(file: File) {
  const { source, pages } = files[file]
  const [intro, ...parts] = source.split(/^(?=## )/m)
  const sections = new Map(parts.map((part) => [anchor(part.match(/^## (.+)$/m)![1]), part]))
  // Every section has a page, so a new `## ` in the file fails the build until it is given one.
  if (Object.keys(pages).length > 0) {
    for (const id of sections.keys()) if (!pages[id]) throw new Error(`Give the "${id}" section of ${file}.md a workshop page in markdown-pages.ts`)
  }
  return {
    /** The opening of the file, under its title. */
    intro: linked(file, intro.replace(/^# .+\n+/, '')),
    /** The whole file, without its title. */
    all: linked(file, source.replace(/^# .+\n+/, '')),
    /** The named sections, in the order given, ready for `<Markdown>`. */
    sections: (...headings: string[]) =>
      linked(
        file,
        headings
          .map((heading) => {
            const part = sections.get(anchor(heading))
            if (!part) throw new Error(`${file}.md has no "## ${heading}" section`)
            return part
          })
          .join('\n'),
      ),
  }
}
