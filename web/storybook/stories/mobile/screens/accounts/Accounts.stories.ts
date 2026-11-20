import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenAccountCheckEmailResent from './account-check-email-resent.html?raw'
import screenAccountCheckEmail from './account-check-email.html?raw'
import screenAccountConfirmed from './account-confirmed.html?raw'
import screenAccountDeleteError from './account-delete-error.html?raw'
import screenAccountDelete from './account-delete.html?raw'
import screenAccountDetailsGuest from './account-details-guest.html?raw'
import screenAccountDetailsPending from './account-details-pending.html?raw'
import screenAccountDetails from './account-details.html?raw'
import screenAccountForgotPasswordSent from './account-forgot-password-sent.html?raw'
import screenAccountForgotPassword from './account-forgot-password.html?raw'
import screenAccountLinkExpired from './account-link-expired.html?raw'
import screenAccountResetPassword from './account-reset-password.html?raw'
import screenAccountSignInError from './account-sign-in-error.html?raw'
import screenAccountSignInMerge from './account-sign-in-merge.html?raw'
import screenAccountSignInResetDone from './account-sign-in-reset-done.html?raw'
import screenAccountSignIn from './account-sign-in.html?raw'
import screenAccountSignOutUnsent from './account-sign-out-unsent.html?raw'
import screenAccountSignOut from './account-sign-out.html?raw'
import screenAccountSignUpConsentSheet from './account-sign-up-consent-sheet.html?raw'
import screenAccountSignUpError from './account-sign-up-error.html?raw'
import screenAccountSignUpOffline from './account-sign-up-offline.html?raw'
import screenAccountSignUp from './account-sign-up.html?raw'

// The Accounts screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Accounts",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Account · check your email, link sent again. */
export const AccountCheckEmailResent: Story = {
  name: "Check your email, link sent again",
  ...screen(screenAccountCheckEmailResent),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 272 } },
}
/** Account · check your email. */
export const AccountCheckEmail: Story = {
  name: "Check your email",
  ...screen(screenAccountCheckEmail),
  parameters: { mockup: { issue: 272 } },
}
/** Account · email confirmed, first upload (sheet). */
export const AccountConfirmed: Story = {
  name: "Email confirmed, first upload (sheet)",
  ...screen(screenAccountConfirmed),
  parameters: { mockup: { issue: 272 } },
}
/** Account · delete account, wrong password. */
export const AccountDeleteError: Story = {
  name: "Delete account, wrong password",
  ...screen(screenAccountDeleteError),
  parameters: { mockup: { issue: 272 } },
}
/** Account · delete account. */
export const AccountDelete: Story = {
  name: "Delete account",
  ...screen(screenAccountDelete),
  parameters: { mockup: { issue: 272 } },
}
/** Account · account, guest: sign up or sign in, from the Account row at the top of Profile. */
export const AccountDetailsGuest: Story = {
  name: "Account, guest",
  ...screen(screenAccountDetailsGuest),
}
/** Account · account, email not confirmed. */
export const AccountDetailsPending: Story = {
  name: "Account, email not confirmed",
  ...screen(screenAccountDetailsPending),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 272 } },
}
/** Account · account, signed in. */
export const AccountDetails: Story = {
  name: "Account, signed in",
  ...screen(screenAccountDetails),
  parameters: { mockup: { issue: 272 } },
}
/** Account · forgot password, link sent. */
export const AccountForgotPasswordSent: Story = {
  name: "Forgot password, link sent",
  ...screen(screenAccountForgotPasswordSent),
  parameters: { mockup: { issue: 272 } },
}
/** Account · forgot password. */
export const AccountForgotPassword: Story = {
  name: "Forgot password",
  ...screen(screenAccountForgotPassword),
  parameters: { mockup: { issue: 272 } },
}
/** Account · link expired. */
export const AccountLinkExpired: Story = {
  name: "Link expired",
  ...screen(screenAccountLinkExpired),
  parameters: { mockup: { issue: 272 } },
}
/** Account · choose a new password. */
export const AccountResetPassword: Story = {
  name: "Choose a new password",
  ...screen(screenAccountResetPassword),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign in, wrong password. */
export const AccountSignInError: Story = {
  name: "Sign in, wrong password",
  ...screen(screenAccountSignInError),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign in, guest data joins. */
export const AccountSignInMerge: Story = {
  name: "Sign in, guest data joins",
  ...screen(screenAccountSignInMerge),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign in, password changed. */
export const AccountSignInResetDone: Story = {
  name: "Sign in, password changed",
  ...screen(screenAccountSignInResetDone),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign in. */
export const AccountSignIn: Story = {
  name: "Sign in",
  ...screen(screenAccountSignIn),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign out, changes not sent. */
export const AccountSignOutUnsent: Story = {
  name: "Sign out, changes not sent",
  ...screen(screenAccountSignOutUnsent),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign out. */
export const AccountSignOut: Story = {
  name: "Sign out",
  ...screen(screenAccountSignOut),
  parameters: { mockup: { issue: 272 } },
}
/** Account · what is stored. */
export const AccountSignUpConsentSheet: Story = {
  name: "What is stored",
  ...screen(screenAccountSignUpConsentSheet),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign up, errors. */
export const AccountSignUpError: Story = {
  name: "Sign up, errors",
  ...screen(screenAccountSignUpError),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign up, offline. */
export const AccountSignUpOffline: Story = {
  name: "Sign up, offline",
  ...screen(screenAccountSignUpOffline),
  parameters: { mockup: { issue: 272 } },
}
/** Account · sign up. */
export const AccountSignUp: Story = {
  name: "Sign up",
  ...screen(screenAccountSignUp),
  parameters: { mockup: { issue: 272 } },
}
