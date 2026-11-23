import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenSessionDelete from './session-delete.html?raw'
import screenSessionShare from './session-share.html?raw'

// Share or delete a past session, opened from its recap on Home (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Home/Sessions/Share and delete",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Delete a session, from its recap. */
export const SessionDelete: Story = {
  name: "Delete a session",
  ...screen(screenSessionDelete),
  parameters: { mockup: { issue: 104 } },
}
/** Share a session, from its recap. */
export const SessionShare: Story = {
  name: "Share a session",
  ...screen(screenSessionShare),
  parameters: { mockup: { issue: 104 } },
}
