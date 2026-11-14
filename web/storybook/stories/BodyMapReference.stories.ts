import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import shapes from '../../../design/body-map.json'
import doc from './BodyMapReference.md?raw'

type Region = { key: string | null; d: string; skin: boolean }
const figures = (['shoulders', 'hips'] as const).map((figure) => ({
  figure,
  views: (['front', 'back'] as const).map((view) => ({ view, regions: (shapes[figure] as Record<'front' | 'back', Region[]>)[view] })),
}))
/** The area names as the web component takes them: the key without its side. */
const areas = (view: 'front' | 'back') => [...new Set(figures[0].views.find((one) => one.view === view)!.regions.flatMap((region) => (region.key ? [region.key.replace(/-(left|right)$/, '')] : [])))].sort()

// The body map as a cross-platform reference: the generated figures and the area names. The Vue component is under Web / Components.
const meta = {
  title: 'Core/Body map',
  parameters: { docs: { description: { component: doc } } },
  render: () => ({
    setup: () => ({ figures, front: areas('front'), back: areas('back') }),
    template: `<div class="flex flex-col gap-8">
      <div v-for="item in figures" :key="item.figure" class="flex flex-col gap-3">
        <h3 class="cp-type-title">{{ item.figure === 'shoulders' ? 'Wider shoulders' : 'Wider hips' }}</h3>
        <div class="cp-body-map">
          <figure v-for="view in item.views" :key="view.view" class="cp-body-view">
            <svg viewBox="0 0 120 268" class="cp-body-figure" aria-hidden="true">
              <path v-for="(region, i) in view.regions" :key="i" :class="region.skin ? 'cp-body-outline' : 'cp-body-part'" :d="region.d" />
            </svg>
            <figcaption>{{ view.view === 'front' ? 'Front' : 'Back' }}</figcaption>
          </figure>
        </div>
      </div>
      <div class="flex flex-wrap gap-8">
        <p><strong>Front areas</strong><br />{{ front.join(', ') }}</p>
        <p><strong>Back areas</strong><br />{{ back.join(', ') }}</p>
      </div>
    </div>`,
  }),
} satisfies Meta
export default meta
export const Reference: StoryObj<typeof meta> = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByText('Wider hips')).toBeInTheDocument()
  },
}
