import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

// Lists the mockups from the tags each file carries in its <head> (ADR 0016): no index to keep in step.

export interface Mockup {
  stage: 'drafts' | 'agreed'
  device: 'phone' | 'tablet' | 'web'
  screen: string
  title: string
  section: string
  issue: string
  height: number
  path: string
}

declare const data: Mockup[]
export { data }

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../design/mockups')
const meta = (html: string, name: string) => html.match(new RegExp(`<meta name="mockup:${name}" content="([^"]*)"`))?.[1] ?? ''

export default {
  watch: ['../design/mockups/**/*.html'],
  load(): Mockup[] {
    const mockups: Mockup[] = []
    for (const stage of ['agreed', 'drafts'] as const) {
      for (const device of ['phone', 'tablet', 'web'] as const) {
        const dir = join(root, stage, device)
        if (!existsSync(dir)) continue
        for (const file of readdirSync(dir).filter((f) => f.endsWith('.html')).sort()) {
          const html = readFileSync(join(dir, file), 'utf8')
          mockups.push({
            stage,
            device,
            screen: meta(html, 'screen') || file.replace(/\.html$/, ''),
            title: html.match(/<title>([^<]*)<\/title>/)?.[1] ?? file,
            section: meta(html, 'section'),
            issue: meta(html, 'issue'),
            height: Number(html.match(/--frame-height:\s*(\d+)px/)?.[1] ?? 844),
            path: `design/mockups/${stage}/${device}/${file}`,
          })
        }
      }
    }
    return mockups
  },
}
