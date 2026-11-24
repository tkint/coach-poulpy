import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenAccountPassword from './account-password.html?raw'
import screenAccountPasswordError from './account-password-error.html?raw'
import screenAccountPasswordMismatch from './account-password-mismatch.html?raw'
import screenAccountDevices from './account-devices.html?raw'
import screenAccountExport from './account-export.html?raw'

// The pages behind the three rows of Account, signed in: Password, Signed-in devices and Export my data (#553).
const meta = {
  title: 'Mobile/Screens/You/Accounts/Password, devices and export',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: { issue: 553 }, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Password · the current one and a new one, typed twice; the other phones are signed out, this one stays signed in. */
export const AccountPassword: Story = { name: 'Change password', ...screen(screenAccountPassword) }
/** Password · the current password is wrong, nothing changed. */
export const AccountPasswordError: Story = { name: 'Change password, wrong current password', ...screen(screenAccountPasswordError) }
/** Password · the new password and its repeat differ, so nothing is sent yet. */
export const AccountPasswordMismatch: Story = { name: 'Change password, new passwords differ', ...screen(screenAccountPasswordMismatch) }
/** Signed-in devices · this phone first, then every other phone and browser with when it was last used and Sign out. */
export const AccountDevices: Story = { name: 'Signed-in devices', ...screen(screenAccountDevices) }
/** Export my data · one .zip from the account: JSON, and each session as a FIT file. */
export const AccountExport: Story = { name: 'Export my data', ...screen(screenAccountExport) }
