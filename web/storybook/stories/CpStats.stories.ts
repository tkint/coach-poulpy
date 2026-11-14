import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpStat, CpStats } from '@coach-poulpy/ui'

const meta = {
  title: 'Display/Stats',
  component: CpStats,
  render: (args) => ({
    components: { CpStats, CpStat },
    setup: () => ({ args }),
    template: `<CpStats v-bind="args">
      <CpStat title="Sessions" value="12" description="this month" />
      <CpStat title="Streak" value="4 days" accent />
    </CpStats>`,
  }),
} satisfies Meta<typeof CpStats>
export default meta
type Story = StoryObj<typeof meta>

export const Horizontal: Story = {}
export const Vertical: Story = { args: { vertical: true } }
