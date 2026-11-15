import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { Check } from '@lucide/vue'
import { CpAvatar, CpBadge, CpButton, CpCard, CpIcon } from '@coach-poulpy/ui'
import AppShell from './AppShell.vue'

// The web app's profile (issue #355).
const meta = {
  title: 'Web/Screens/App/Profile',
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 355 }, a11y: { test: 'todo' } },
} satisfies Meta
export default meta
type Story = StoryObj

export const Default: Story = {
  render: () => ({
    components: { AppShell, CpAvatar, CpBadge, CpButton, CpCard, CpIcon },
    setup: () => ({ Check }),
    template: `<AppShell title="Profile" active="profile">
      <main class="flex flex-col gap-6 p-4 md:p-8">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Profile</h1>
          <p class="text-base-content/70">Your account on Coach Poulpy.</p>
        </div>
        <CpCard>
          <div class="flex items-center gap-4">
            <CpAvatar initials="A" tone="primary" size="lg" />
            <div class="flex min-w-0 flex-col gap-1">
              <h2 class="cp-card-title truncate">ana@example.com</h2>
              <CpBadge class="gap-1"><CpIcon :icon="Check" size="xs" />Email confirmed</CpBadge>
            </div>
          </div>
        </CpCard>
        <CpCard title="Password">
          <p class="text-base-content/70">We email you a link to choose a new password. Every device you are signed in on is signed out when you use it.</p>
          <div class="cp-card-actions"><CpButton size="xl" variant="outline">Email me a reset link</CpButton></div>
        </CpCard>
        <CpCard title="Your data">
          <p class="text-base-content/70">Your workouts, sessions and body log live on your phone and follow your account. To change them, open the app.</p>
        </CpCard>
      </main></AppShell>`,
  }),
}
