import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { Send } from '@lucide/vue'
import { CpButton, CpCard, CpDivider, CpErrorNote, CpSuccessNote, CpTextField } from '@coach-poulpy/ui'
import AdminShell from './AdminShell.vue'

// The admin's settings: the sender of the emails and a test email (issue #336).
const meta = {
  title: 'Web/Screens/Admin/Settings',
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 336 }, a11y: { test: 'todo' } },
} satisfies Meta
export default meta
type Story = StoryObj

const settings = (from: string, outcome?: 'sent' | 'failed' | 'invalid'): Story => ({
  render: () => ({
    components: { AdminShell, CpButton, CpCard, CpDivider, CpErrorNote, CpSuccessNote, CpTextField, Send },
    setup: () => ({ from: ref(from), outcome }),
    template: `<AdminShell title="Settings" active="settings">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Settings</h1>
          <p class="text-base-content/70">Changes take effect on the next email. Every change is written to the audit log.</p>
        </div>
        <CpCard title="Email sender">
          <form class="flex flex-col gap-4" @submit.prevent>
            <CpTextField v-model="from" label="From address" type="email" autocomplete="off"
              :hint="outcome === 'invalid' ? undefined : 'The address emails come from, such as Coach Poulpy <hello@example.com>.'"
              :error="outcome === 'invalid' ? 'This is not an email address. Use the form name@example.com.' : undefined" />
            <div class="flex flex-wrap justify-end gap-2"><CpButton type="submit" size="md">Save</CpButton></div>
          </form>
          <CpDivider class="my-0" />
          <div class="flex flex-col gap-3">
            <div class="flex flex-col gap-1">
              <h3 class="cp-type-label">Test email</h3>
              <p class="text-sm text-base-content/70">Sends a message to ana@example.com with the saved settings.</p>
            </div>
            <div><CpButton variant="outline" size="md" class="gap-2"><Send class="size-5 shrink-0" aria-hidden="true" />Send test email</CpButton></div>
            <CpSuccessNote v-if="outcome === 'sent'">Test email sent to ana@example.com.</CpSuccessNote>
            <CpErrorNote v-if="outcome === 'failed'">The test email was not sent. The mail server said: 535 Authentication failed.</CpErrorNote>
          </div>
          <p class="text-sm text-base-content/70">The mail server (host, port, user and password) is set on the server and is not editable here.</p>
        </CpCard>
      </main></AdminShell>`,
  }),
})
export const Mail = settings('hello@coachpoulpy.example')
export const MailInvalid = settings('hello@', 'invalid')
export const MailTestSent = settings('hello@coachpoulpy.example', 'sent')
export const MailTestFailed = settings('hello@coachpoulpy.example', 'failed')
