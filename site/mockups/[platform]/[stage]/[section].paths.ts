import { listMockups } from '../../../.vitepress/mockups.mts'

// One page per section of a stage and platform, showing every state of it one after the other.
export default {
  watch: ['../../../../design/mockups/**/*.html'],
  paths: () => {
    const pages = new Map<string, { platform: string; stage: string; section: string; title: string }>()
    for (const m of listMockups()) pages.set(m.sectionPath, { platform: m.platform, stage: m.stage, section: m.sectionSlug, title: m.group })
    return [...pages.values()].map((params) => ({ params }))
  },
}
