import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpDivider } from '@coach-poulpy/ui'

const meta = {
  title: 'Display/Divider',
  component: CpDivider,
  render: () => ({ components: { CpDivider }, template: '<div class="w-64"><p>Above</p><CpDivider /><p>Below</p></div>' }),
} satisfies Meta<typeof CpDivider>
export default meta
export const Default: StoryObj<typeof meta> = {}
