import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenAchievements from './achievements.html?raw'

// Achievements open from the motivation row on Home.
// CLEANUP: these screens draw six Lucide icons the apps don't have yet (award, circle-plus, circle-question-mark, key-round, ruler, smartphone). Run `node design/build-icons.mjs --add award circle-plus circle-question-mark key-round ruler smartphone` when the screens are agreed; it needs network access to jsdelivr, which this session's sandbox blocked. They replace the History, You and Settings drafts of #511 and #512 where they overlap.
const meta = {
  title: 'Mobile/Screens/Home/Achievements',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Achievements · opened from the motivation row: Poulpy and the eight tentacles, what is earned, and what is still hidden. */
export const Achievements: Story = { name: 'Achievements', ...screen(screenAchievements) }
