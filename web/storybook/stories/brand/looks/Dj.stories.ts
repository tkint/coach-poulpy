import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyDj from './PoulpyDj.vue'

// The live part of the DJ look's page (Dj.mdx beside it), which names it, so it stays out of the sidebar. A draft: the owner
// reviews it in the workshop before the look goes anywhere else.
const meta = {
  title: 'Brand/Live looks/DJ',
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** The DJ playing: his head bobs to the beat, he scratches one record while the other turns, and notes float up around him. */
export const Dj: Story = { render: () => ({ components: { PoulpyDj }, template: '<PoulpyDj />' }) }
