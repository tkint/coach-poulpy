import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyWinter from './PoulpyWinter.vue'

// The live part of the winter look's page (Winter.mdx beside it), which names it, so it stays out of the sidebar. A draft: the
// owner reviews it in the workshop before the look goes anywhere else.
const meta = {
  title: 'Brand/Live looks/Winter',
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Winter Poulpy: ear muffs and a scarf, snow falling around him and his breath puffing out as mist. */
export const Winter: Story = { render: () => ({ components: { PoulpyWinter }, template: '<PoulpyWinter />' }) }
