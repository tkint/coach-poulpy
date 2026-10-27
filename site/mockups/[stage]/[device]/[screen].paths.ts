import { listMockups } from '../../../.vitepress/mockups.mts'

export default {
  watch: ['../../../../design/mockups/**/*.html'],
  paths: () => listMockups().map((m) => ({ params: { stage: m.stage, device: m.device, screen: m.screen, title: m.title } })),
}
