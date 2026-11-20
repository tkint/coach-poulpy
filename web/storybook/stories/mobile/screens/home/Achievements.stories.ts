import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenAchievements from './achievements.html?raw'

// Achievements open from the motivation row on Home.
const meta = {
  title: 'Mobile/Screens/Home/Achievements',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Achievements · opened from the motivation row: Poulpy and the eight tentacles, what is earned, and what is still hidden. */
export const Achievements: Story = { name: 'Achievements', ...screen(screenAchievements) }
