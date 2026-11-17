import type { Meta, StoryObj } from '@storybook/vue3-vite'
import Poulpy3d from '../Poulpy3d.vue'

// Poulpy the DJ, a look (design/poulpy-3d/looks/dj.mjs): a draft for the owner to watch on its page, Dj.mdx.
const meta = {
  title: 'Brand/Looks/The DJ',
  tags: ['draft', '!autodocs'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** The DJ playing: his head bobs to the beat, he scratches one record while the other turns, and notes float up around him. */
export const Playing: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d motion="dj" />' }) }
