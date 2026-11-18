import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyChef from './PoulpyChef.vue'

// The live part of the cook look's page (Chef.mdx beside it), which names it, so it stays out of the sidebar. A draft: the owner
// reviews it in the workshop before the look goes anywhere else.
const meta = {
  title: 'Brand/Live looks/Chef',
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy the cook: a tall toque, a big curled moustache and a confident grin, with his idle. */
export const Chef: Story = { render: () => ({ components: { PoulpyChef }, template: '<PoulpyChef />' }) }
