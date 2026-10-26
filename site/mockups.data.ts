import { listMockups, type Mockup } from './.vitepress/mockups.mts'

export type { Mockup }

declare const data: Mockup[]
export { data }

export default {
  watch: ['../design/mockups/**/*.html'],
  load: listMockups,
}
