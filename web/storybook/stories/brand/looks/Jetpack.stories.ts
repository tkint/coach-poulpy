import type { Meta, StoryObj } from '@storybook/vue3-vite'
import PoulpyJetpack from './PoulpyJetpack.vue'

// The live part of the jetpack look's page (Jetpack.mdx beside it), which names it, so it stays out of the sidebar. A draft: the
// owner reviews it in the workshop before the look goes anywhere else.
const meta = {
  title: 'Brand/Live looks/Jetpack',
  tags: ['!dev', '!autodocs', 'draft'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy with his jetpack, zooming from side to side, grinning, his tentacles trailing under him. */
export const Jetpack: Story = { render: () => ({ components: { PoulpyJetpack }, template: '<PoulpyJetpack />' }) }
