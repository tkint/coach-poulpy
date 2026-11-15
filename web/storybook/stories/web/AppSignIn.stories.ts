import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { CpButton, CpErrorNote, CpPasswordField, CpTextField } from '@coach-poulpy/ui'
import SitePage from '../../../app/src/SitePage.vue'

// The web app's sign-in (issue #355).
const meta = {
  title: 'Web/Screens/App/Sign in',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: { issue: 355 } },
} satisfies Meta
export default meta
type Story = StoryObj

const signIn = (password: string, failed: boolean): Story => ({
  render: () => ({
    components: { SitePage, CpTextField, CpPasswordField, CpButton, CpErrorNote },
    setup: () => ({ email: ref('ana@example.com'), password: ref(password), failed }),
    template: `<SitePage :drawing="failed ? 'mood_happy' : 'pose_standing_tall'">
      <div class="flex flex-col gap-2">
        <h1 class="cp-type-display">Sign in</h1>
        <p>Your account, on the web.</p>
      </div>
      <form class="flex flex-col gap-6" novalidate @submit.prevent>
        <CpTextField v-model="email" label="Email" type="email" autocomplete="username" :invalid="failed" />
        <CpPasswordField v-model="password" label="Password" :invalid="failed" autocomplete="current-password" />
        <CpErrorNote v-if="failed">Wrong email or password.</CpErrorNote>
        <CpButton size="xl" type="submit" class="w-full">Sign in</CpButton>
      </form></SitePage>`,
  }),
})
export const Default: Story = signIn('correct horse', false)
export const WrongPassword: Story = signIn('wrong horse', true)
