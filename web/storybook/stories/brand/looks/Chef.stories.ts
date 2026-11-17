import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyChef from './PoulpyChef.vue'

// The live part of the Brand > Looks > The cook page (Chef.mdx), which names it, so it stays out of the sidebar. Each look
// has its own stories file, and stays a draft until a PR tags it agreed.
const meta = {
  title: 'Brand/Looks/Live/Chef',
  tags: ['!dev', '!autodocs'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy the cook: a tall toque, a big curled moustache and a confident grin, with his idle. */
export const Chef: Story = {
  tags: ['draft'],
  render: () => ({ components: { PoulpyChef }, template: '<PoulpyChef />' }),
}
