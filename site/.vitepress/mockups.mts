import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { devices, stages } from './mockup-labels'

export { devices, stages }

// Lists the mockups from the tags each file carries in its <head> (ADR 0016): no index to keep in step.
// Shared by the sidebar, the gallery pages and the one page per mockup.

export interface Mockup {
  stage: 'agreed' | 'drafts'
  device: 'phone' | 'tablet' | 'web'
  screen: string
  title: string
  section: string
  issue: string
  height: number
  /** The file, from the repo root and from the site's public folder. */
  path: string
  /** The mockup's own page on the site. */
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
          screen,
          title: html.match(/<title>([^<]*)<\/title>/)?.[1] ?? screen,
          section: meta(html, 'section'),
          issue: meta(html, 'issue'),
          height: Number(html.match(/--frame-height:\s*(\d+)px/)?.[1] ?? 844),
          path: `design/mockups/${stage}/${device}/${file}`,
          link: `/mockups/${stage}/${device}/${screen}`,
        })
      }
    }
  }
  return mockups
}
