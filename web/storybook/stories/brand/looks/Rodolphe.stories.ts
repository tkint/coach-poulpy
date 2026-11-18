import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyRodolphe from './PoulpyRodolphe.vue'

// Rodolphe, the live part of his page (Rodolphe.mdx), which names it, so it stays out of the sidebar. A draft: nothing is built from it.
const meta = {
  title: 'Brand/Live looks/Rodolphe',
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy as Rodolphe, the red-nosed reindeer: swaying to a tune, his nose glowing on the beat. */
export const Rodolphe: Story = { render: () => ({ components: { PoulpyRodolphe }, template: '<PoulpyRodolphe />' }) }
