import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect } from 'storybook/test'
import { CpAvatar, CpSidePanel } from '@coach-poulpy/ui'
import doc from './CpSidePanel.md?raw'

const meta = {
  title: 'Overlays/SidePanel',
  parameters: { docs: { description: { component: doc } } },
  component: CpSidePanel,
  args: { title: 'Alex Martin', closeLabel: 'Close' },
  render: (args) => ({
    components: { CpSidePanel, CpAvatar },
    setup: () => ({ args }),
    template: `<CpSidePanel v-bind="args">
      <template #avatar><CpAvatar initials="AM" size="lg" /></template>
      <template #subtitle><span class="text-sm">alex@example.com</span></template>
      <p>Joined in March.</p>
    </CpSidePanel>`,
  }),
} satisfies Meta<typeof CpSidePanel>
export default meta
type Story = StoryObj<typeof meta>

/** A side panel is opened by whoever shows it, so it is open here. */
export const Open: Story = {
  play: async ({ canvasElement }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open')
  },
}
