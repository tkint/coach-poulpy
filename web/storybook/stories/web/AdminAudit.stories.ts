import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpAvatar, CpBadge, CpCard, CpStat, CpStats, CpTable } from '@coach-poulpy/ui'
import AdminShell from './AdminShell.vue'

// The admin shell, showing the audit log (issue #336). Was design/mockups/agreed/web/admin-shell.html.
const meta = {
  title: 'Web/Screens/Admin/Audit log',
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 336 }, a11y: { test: 'todo' } },
} satisfies Meta
export default meta
type Story = StoryObj

const entries = [
  { when: '8 Nov, 14:20', who: 'Ana', what: 'Published catalog', tone: 'primary', target: 'version 12' },
  { when: '8 Nov, 14:02', who: 'Ana', what: 'Disabled account', tone: 'outline', target: '0190d4d2-…-0042' },
  { when: '8 Nov, 13:40', who: 'The server', initial: 'S', what: 'Made admin', tone: 'neutral', target: '0190d4d2-…-0001' },
] as const

export const Default: Story = {
  render: () => ({
    components: { AdminShell, CpAvatar, CpBadge, CpCard, CpStat, CpStats, CpTable },
    setup: () => ({ entries }),
    template: `<AdminShell title="Audit log" active="audit">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Audit log</h1>
          <p class="text-base-content/70">Everything an admin changed, newest first. Only admins see this.</p>
        </div>
        <CpStats>
          <CpStat title="Changes this week" value="12" description="Since Monday" accent />
          <CpStat title="Admins" value="1" description="Ana" />
          <CpStat title="Last change" value="14:20" description="Today, by Ana" />
        </CpStats>
        <CpCard title="Latest changes">
          <CpTable>
            <thead><tr><th>When</th><th>Who</th><th>What</th><th>Target</th></tr></thead>
            <tbody>
              <tr v-for="e in entries" :key="e.when">
                <td class="whitespace-nowrap">{{ e.when }}</td>
                <td><div class="flex items-center gap-3"><CpAvatar :initials="e.initial ?? e.who.charAt(0)" /><span class="whitespace-nowrap">{{ e.who }}</span></div></td>
                <td><CpBadge :tone="e.tone">{{ e.what }}</CpBadge></td>
                <td class="font-mono text-sm">{{ e.target }}</td>
              </tr>
            </tbody>
          </CpTable>
        </CpCard>
      </main></AdminShell>`,
  }),
}
