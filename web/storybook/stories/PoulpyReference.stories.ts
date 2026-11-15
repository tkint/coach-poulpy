import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import doc from './PoulpyReference.md?raw'

const files = import.meta.glob<string>('../../../design/poulpy/*.svg', { eager: true, query: '?url', import: 'default' })
/** A drawing's file name, without its folder and extension, and its readable name. */
const drawings = Object.entries(files).map(([path, src]) => {
  const key = path.slice(path.lastIndexOf('/') + 1, -'.svg'.length)
  return { key, src, name: key.replace(/^(mood|pose)_/, '').replace(/_/g, ' ').replace(/^./, (c) => c.toUpperCase()) }
})
const groups = [
  { heading: 'Moods', items: drawings.filter((one) => one.key.startsWith('mood_')) },
  { heading: 'Poses', items: drawings.filter((one) => one.key.startsWith('pose_')) },
  { heading: 'Loader', items: drawings.filter((one) => one.key.startsWith('loader')) },
]

// Poulpy as a cross-platform reference: every generated drawing. The Vue component is under Web / Components.
const meta = {
  title: 'Core/Poulpy',
  parameters: { docs: { description: { component: doc } } },
  render: () => ({
    setup: () => ({ groups }),
    template: `<div class="flex flex-col gap-8">
      <section v-for="group in groups" :key="group.heading" class="flex flex-col gap-3">
        <h3 class="cp-type-title">{{ group.heading }}</h3>
        <div class="flex flex-wrap gap-6">
          <figure v-for="item in group.items" :key="item.key" class="flex w-32 flex-col items-center gap-2 text-center">
            <img :src="item.src" :alt="item.name" class="h-24 w-24 object-contain" />
            <figcaption><strong>{{ item.name }}</strong><br /><code>{{ item.key }}</code></figcaption>
          </figure>
        </div>
      </section>
    </div>`,
  }),
} satisfies Meta
export default meta
export const Reference: StoryObj<typeof meta> = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByAltText('Happy')).toBeInTheDocument()
  },
}
