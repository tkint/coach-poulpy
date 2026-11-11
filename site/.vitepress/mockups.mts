import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { devices, groupOf, platformOf, platforms, slug, stages } from './mockup-labels'

export { devices, platforms, stages }

// Lists the mockups from the tags each file carries in its <head> (ADR 0016): no index to keep in step.
// Shared by the sidebar, the gallery pages and the one page per app view.

export interface Mockup {
  stage: 'agreed' | 'drafts'
  device: 'phone' | 'tablet' | 'web'
  platform: 'mobile' | 'web'
  screen: string
  title: string
  section: string
  issue: string
  height: number
  /** For a web page that fills the browser window, as a layout with a side drawer does: the window's height in px, which the page then scrolls in. 0 for a page the frame sizes to its content. */
  viewport: number
  /** The file, from the repo root and from the site's public folder. */
  path: string
  /** The app view the mockup belongs to. */
  group: string
  sectionSlug: string
  /** The page for this app view, showing its available states. */
  sectionPath: string
  /** The mockup itself: its place on the app-view page. */
  link: string
}

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../../design/mockups')
const meta = (html: string, name: string) => html.match(new RegExp(`<meta name="mockup:${name}" content="([^"]*)"`))?.[1] ?? ''

export function listMockups(): Mockup[] {
  const mockups: Mockup[] = []
  for (const stage of Object.keys(stages) as Mockup['stage'][]) {
    for (const device of Object.keys(devices) as Mockup['device'][]) {
      const dir = join(root, stage, device)
      if (!existsSync(dir)) continue
      for (const file of readdirSync(dir).filter((f) => f.endsWith('.html')).sort()) {
        const html = readFileSync(join(dir, file), 'utf8')
        const screen = file.replace(/\.html$/, '')
        mockups.push({
          stage,
          device,
          platform: platformOf(device),
          group: '',
          sectionSlug: '',
          sectionPath: '',
          screen,
          title: html.match(/<title>([^<]*)<\/title>/)?.[1] ?? screen,
          section: meta(html, 'section'),
          issue: meta(html, 'issue'),
          height: Number(html.match(/--frame-height:\s*(\d+)px/)?.[1] ?? 844),
          viewport: Number(meta(html, 'viewport') || 0),
          path: `design/mockups/${stage}/${device}/${file}`,
          link: '',
        })
      }
    }
  }
  for (const m of mockups) {
    m.group = groupOf(m)
    m.sectionSlug = slug(m.group)
    m.sectionPath = `/mockups/${m.platform}/${m.stage}/${m.sectionSlug}`
    m.link = `${m.sectionPath}#${m.screen}`
  }
  return mockups
}
