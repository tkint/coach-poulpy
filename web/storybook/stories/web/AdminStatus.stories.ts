import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpBadge, CpCard, CpErrorNote, CpTable } from '@coach-poulpy/ui'
import AdminShell from './AdminShell.vue'

// The admin's status: the mail server and the pending registrations (issue #336).
const meta = {
  title: 'Web/Screens/Admin/Status',
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 336 }, a11y: { test: 'todo' } },
} satisfies Meta
export default meta
type Story = StoryObj

const pending = [
  { email: 'léa@example.com', signedUp: '9 Nov, 08:41', expires: '10 Nov, 08:41' },
  { email: 'tom@example.com', signedUp: '8 Nov, 19:12', expires: '9 Nov, 19:12' },
]

const status = (mail: 'ok' | 'failed' | 'missing'): Story => ({
  render: () => ({
    components: { AdminShell, CpBadge, CpCard, CpErrorNote, CpTable },
    setup: () => ({ pending, mail }),
    template: `<AdminShell title="Status" active="status">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Status</h1>
          <p class="text-base-content/70">What the server can do right now. Only admins see this.</p>
        </div>
        <CpCard>
          <div class="flex flex-wrap items-center gap-3">
            <h2 class="cp-card-title">Mail</h2>
            <CpBadge :tone="mail === 'missing' ? 'error' : 'success'">{{ mail === 'missing' ? 'Not configured' : 'Configured' }}</CpBadge>
          </div>
          <template v-if="mail === 'missing'">
            <CpErrorNote>No mail server is set on the server, so no email can be sent. Sign-up cannot send its confirmation link.</CpErrorNote>
            <p class="text-sm text-base-content/70">The mail server is set with environment variables on the server, not here.</p>
          </template>
          <template v-else>
            <p class="text-base-content/70">The server sends emails through its mail server.</p>
            <CpErrorNote v-if="mail === 'failed'">The last send failed on 9 Nov, 08:41: 535 Authentication failed.</CpErrorNote>
            <p v-else class="text-sm">Last send: <strong>sent</strong>, 9 Nov, 08:41 (a confirmation email).</p>
          </template>
        </CpCard>
        <CpCard title="Pending registrations" description="Accounts that have not confirmed their email yet. No email is sent from here.">
          <CpTable>
            <thead><tr><th>Email</th><th>Signed up</th><th>Link expires</th></tr></thead>
            <tbody>
              <tr v-for="p in pending" :key="p.email"><td class="break-all">{{ p.email }}</td><td class="whitespace-nowrap">{{ p.signedUp }}</td><td class="whitespace-nowrap">{{ p.expires }}</td></tr>
            </tbody>
          </CpTable>
        </CpCard>
      </main></AdminShell>`,
  }),
})
export const Default = status('ok')
export const MailFailed = status('failed')
export const MailMissing = status('missing')
