import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { CpButton, CpErrorNote, CpPasswordField } from '@coach-poulpy/ui'
import SitePage from './SitePage.vue'

// The pages a confirmation or reset link opens (issue #310). Was design/mockups/agreed/web/account-*.html.
const meta = {
  title: 'Web/Screens/Account',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: { issue: 310 } },
} satisfies Meta
export default meta
type Story = StoryObj

const words = (poulpy: string, title: string, intro: string, extra = ''): Story => ({
  render: () => ({
    components: { SitePage },
    template: `<SitePage poulpy="${poulpy}" title="${title}"><template #intro>${intro}</template>${extra}</SitePage>`,
  }),
})

export const EmailConfirmed: Story = {
  render: () => ({
    components: { SitePage },
    template: `<SitePage poulpy="pose_champion" title="Email confirmed">
      <template #intro>Open Coach Poulpy on the phone you signed up with: it signs you in and sends your training.</template>
      <p class="font-bold text-primary">Lifting your training into the cloud. Eight tentacles, no waiting.</p></SitePage>`,
  }),
}
export const LinkExpired = words('pose_thinking', 'This link doesn’t work anymore', 'It may have been used already, or it expired after 24 hours. Sign up again in the app with the same address and we’ll send a new one.')
export const ResetLinkExpired = words('pose_thinking', 'This link doesn’t work anymore', 'It may have been used already, or it expired after an hour. In the app, tap “Forgot your password?” to get a new one.')
export const ResetPasswordSaved = words('pose_standing_tall', 'Password saved', 'Every phone was signed out. Open Coach Poulpy and sign in with your new password.')

const resetForm = (value: string, invalid: boolean, poulpy: string) => ({
  render: () => ({
    components: { SitePage, CpPasswordField, CpButton, CpErrorNote },
    setup: () => ({ password: ref(value), invalid }),
    template: `<SitePage poulpy="${poulpy}" title="Choose a new password">
      <template #intro>Every phone signed in to this account will be signed out.</template>
      <form class="flex flex-col gap-6" @submit.prevent>
        <CpPasswordField v-model="password" label="New password" hint="8 characters or more." autocomplete="new-password" :invalid="invalid" />
        <CpErrorNote v-if="invalid">Use at least 8 characters.</CpErrorNote>
        <CpButton type="submit" class="w-full">Save password</CpButton>
      </form></SitePage>`,
  }),
})
export const ResetPassword: Story = resetForm('a new horse battery', false, 'pose_standing_tall')
export const ResetPasswordError: Story = resetForm('horse', true, 'pose_thinking')
