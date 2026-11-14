import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { CpButton, CpErrorNote, CpPasswordField, CpTextField } from '@coach-poulpy/ui'
import SitePage from './SitePage.vue'

// The admin's sign-in (issue #336). Was design/mockups/agreed/web/admin-sign-in*.html.
const meta = {
  title: 'Web/Screens/Admin/Sign in',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: { issue: 336 } },
} satisfies Meta
export default meta
type Story = StoryObj

const signIn = (password: string, failed: boolean): Story => ({
  render: () => ({
    components: { SitePage, CpTextField, CpPasswordField, CpButton, CpErrorNote },
    setup: () => ({ email: ref('ana@example.com'), password: ref(password), failed }),
    template: `<SitePage :poulpy="failed ? 'mood_happy' : 'pose_standing_tall'" title="Sign in">
      <template #intro>The admin is for the people who look after Coach Poulpy.</template>
      <form class="flex flex-col gap-6" @submit.prevent>
        <CpTextField v-model="email" label="Email" type="email" autocomplete="username" :invalid="failed" />
        <CpPasswordField v-model="password" label="Password" show-label="Show password" hide-label="Hide password" :invalid="failed" />
        <CpErrorNote v-if="failed">Wrong email or password, or this account isn't an admin's.</CpErrorNote>
        <CpButton type="submit" class="w-full">Sign in</CpButton>
      </form></SitePage>`,
  }),
})
export const Default: Story = signIn('correct horse', false)
export const WrongPassword: Story = signIn('wrong horse', true)
