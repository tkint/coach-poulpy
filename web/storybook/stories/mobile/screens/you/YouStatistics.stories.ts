import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenYouGuest from './you-guest.html?raw'
import screenYouPending from './you-pending.html?raw'
import screenYouSignedIn from './you-signed-in.html?raw'
import screenYouEditStatistics from './you-edit-statistics.html?raw'

// You is one scrolling page: the profile card with its badge, then Statistics. The cog opens Settings.
const meta = {
  title: 'Mobile/Screens/You/Statistics',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** You · a guest: the profile card carries the Guest badge; Statistics follow it, with a period switch (7 days, 30 days, Year, All time) and an Edit button; the cog opens Settings. */
export const YouGuest: Story = { name: 'You, guest', ...screen(screenYouGuest) }
/** You · account created, email not confirmed yet. */
export const YouPending: Story = { name: 'You, confirm your email', ...screen(screenYouPending) }
/** You · signed in. */
export const YouSignedIn: Story = { name: 'You, signed in', ...screen(screenYouSignedIn) }
/** Edit statistics · a switch and a handle per block, to choose and order what You shows. */
export const YouEditStatistics: Story = { name: 'Edit statistics', ...screen(screenYouEditStatistics) }
