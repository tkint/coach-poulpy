import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CircleCheck } from '@lucide/vue'
import { CpButton, CpCard, CpDialog, CpIcon, CpSuccessNote, CpTable } from '@coach-poulpy/ui'
import AdminShell from './AdminShell.vue'

// The pending registrations of the admin's status screen with a Confirm action per row (issue #382): the list with the
// action, the confirmation dialog, and the list after a confirmation. A redesign of Web/Screens/Admin/Status, so a draft
// until a PR moves it to `agreed` and replaces that story. The mail card above the list is unchanged and left out.
const meta = {
  title: 'Web/Screens/Admin/Status, confirm by hand',
  tags: ['!autodocs', 'draft'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 382 }, a11y: { test: 'todo' } },
} satisfies Meta
export default meta
type Story = StoryObj

const rows = [
  { email: 'léa@example.com', signedUp: '9 Nov, 08:41', expires: '10 Nov, 08:41' },
  { email: 'tom@example.com', signedUp: '8 Nov, 19:12', expires: '9 Nov, 19:12' },
]

const screen = (state: 'list' | 'dialog' | 'done'): Story => ({
  render: () => ({
    components: { AdminShell, CpButton, CpCard, CpDialog, CpIcon, CpSuccessNote, CpTable },
    setup: () => ({ state, CircleCheck, shown: state === 'done' ? rows.slice(1) : rows }),
    template: `<AdminShell title="Status" active="status">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Status</h1>
          <p class="text-base-content/70">What the server can do right now. Only admins see this.</p>
        </div>
        <CpCard title="Pending registrations" description="Accounts that have not confirmed their email yet. No email is sent from here. Confirm one only when you trust its address, for example when the email never arrived.">
          <CpSuccessNote v-if="state === 'done'">léa@example.com is confirmed and can sign in.</CpSuccessNote>
          <CpTable>
            <thead><tr><th>Email</th><th>Signed up</th><th>Link expires</th><th><span class="sr-only">Action</span></th></tr></thead>
            <tbody>
              <tr v-for="p in shown" :key="p.email">
                <td class="break-all">{{ p.email }}</td>
                <td class="whitespace-nowrap">{{ p.signedUp }}</td>
                <td class="whitespace-nowrap">{{ p.expires }}</td>
                <td class="text-end"><CpButton variant="outline" size="sm" class="gap-2"><CpIcon :icon="CircleCheck" size="sm" />Confirm<span class="sr-only"> {{ p.email }}</span></CpButton></td>
              </tr>
            </tbody>
          </CpTable>
        </CpCard>
        <CpDialog v-if="state === 'dialog'" :open="true" title="Confirm léa@example.com?" description="You vouch for an address nobody proved. The account can sign in at once, and its confirmation links stop working. This is written to the audit log.">
          <template #actions>
            <CpButton variant="outline">Cancel</CpButton>
            <CpButton class="gap-2"><CpIcon :icon="CircleCheck" size="sm" />Confirm email</CpButton>
          </template>
        </CpDialog>
      </main></AdminShell>`,
  }),
})
/** The pending list, each row with a Confirm action. */
export const List = screen('list')
/** The confirmation dialog for one row. */
export const Dialog = screen('dialog')
/** After a confirmation: the row is gone and a note says who can sign in now. */
export const Confirmed = screen('done')
