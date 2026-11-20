import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHistorySessionDelete from './history-session-delete.html?raw'
import screenHistorySessionShare from './history-session-share.html?raw'

// The History screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Home/History",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** History · delete a session. */
export const HistorySessionDelete: Story = {
  name: "Delete a session",
  ...screen(screenHistorySessionDelete),
  parameters: { mockup: { issue: 104 } },
}
/** History · share a session. */
export const HistorySessionShare: Story = {
  name: "Share a session",
  ...screen(screenHistorySessionShare),
  parameters: { mockup: { issue: 104 } },
}
