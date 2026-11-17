import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyChef from './PoulpyChef.vue'

// The live part of the Brand > Looks > The cook page (Chef.mdx), which names it, so it stays out of the sidebar. Each look
// has its own stories file, and stays a draft until the owner picks a variant and a PR tags it agreed.
const meta = {
  title: 'Brand/Looks/Live/Chef',
  tags: ['!dev', '!autodocs'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy the cook: a tall toque and a curled moustache, his cocky face and his big grin side by side, with his idle. */
export const Chef: Story = {
  tags: ['draft'],
  render: () => ({ components: { PoulpyChef }, template: '<PoulpyChef />' }),
}
