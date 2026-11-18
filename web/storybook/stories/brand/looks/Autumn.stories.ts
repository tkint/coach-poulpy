import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyAutumn from './PoulpyAutumn.vue'

// The live part of the autumn look's page (Autumn.mdx beside it), which names it, so it stays out of the sidebar. A draft: the owner
// reviews it in the workshop before the look goes anywhere else.
const meta = {
  title: 'Brand/Live looks/Autumn',
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy in his autumn look: his striped scarf, a gentle sway and maple leaves falling around him. */
export const Autumn: Story = { render: () => ({ components: { PoulpyAutumn }, template: '<PoulpyAutumn />' }) }
