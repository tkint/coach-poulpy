import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyWinter from './PoulpyWinter.vue'

// Winter Poulpy, one of his looks, live. It is a draft (ADR 0029): the owner reviews it on its page (Winter.mdx) before
// anything is built from it.
const meta = {
  title: 'Brand/Poulpy looks/Winter',
  tags: ['draft', '!autodocs'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Winter Poulpy: a beanie and a scarf, snow falling around him and his breath puffing out as mist. */
export const Live: Story = { render: () => ({ components: { PoulpyWinter }, template: '<PoulpyWinter />' }) }
