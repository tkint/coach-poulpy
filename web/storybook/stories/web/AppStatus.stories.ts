import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpErrorNote, CpSuccessNote } from '@coach-poulpy/ui'
import SitePage from '../../../app/src/SitePage.vue'

// The public status page (issue #340).
const meta = {
  title: 'Web/Screens/App/Status',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: { issue: 340 } },
} satisfies Meta
export default meta
type Story = StoryObj

export const Up: Story = {
  render: () => ({
    components: { SitePage, CpSuccessNote },
    template: `<SitePage drawing="mood_happy"><h1 class="cp-type-display">Status</h1><CpSuccessNote>Coach Poulpy is up.</CpSuccessNote><p>Everything works as expected.</p></SitePage>`,
  }),
}
export const Down: Story = {
  render: () => ({
    components: { SitePage, CpErrorNote },
    template: `<SitePage drawing="mood_happy"><h1 class="cp-type-display">Status</h1><CpErrorNote>Coach Poulpy is down.</CpErrorNote><p>We are on it. Try again in a few minutes.</p></SitePage>`,
  }),
}
