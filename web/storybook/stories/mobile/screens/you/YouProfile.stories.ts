import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenProfile from './profile.html?raw'
import screenProfileSignedIn from './profile-signed-in.html?raw'

// Profile, opened from the profile card on You: one Account row on top, then about you, training and body.
const meta = {
  title: 'Mobile/Screens/You/Profile',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Profile · a guest: one Account row on top (it opens Create an account and Sign in), then about you with a Poulpy face, training and body. */
export const Profile: Story = { name: 'Profile, guest', ...screen(screenProfile) }
/** Profile · signed in: one Account row at the top, showing the email; it opens the existing account screen (password, devices, export, sign out, delete). */
export const ProfileSignedIn: Story = { name: 'Profile, signed in', ...screen(screenProfileSignedIn) }
