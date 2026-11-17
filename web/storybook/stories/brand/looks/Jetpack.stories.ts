import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyJetpack from './PoulpyJetpack.vue'

// Poulpy's jetpack look, live. A draft for the owner to watch (tagged `draft`): nothing is built from it until a PR agrees it.
// Its page, Jetpack.mdx, shows the story, so the story stays out of the sidebar.
const meta = {
  title: 'Brand/Poulpy looks/Jetpack',
  component: PoulpyJetpack,
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof PoulpyJetpack>
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy with his jetpack, zooming from side to side, grinning, his tentacles trailing under him. */
export const Flying: Story = {}
