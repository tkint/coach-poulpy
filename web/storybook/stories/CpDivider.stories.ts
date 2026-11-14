import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpDivider } from '@coach-poulpy/ui'
import doc from './CpDivider.md?raw'

const meta = {
  title: 'Web/Components/Display/Divider',
  parameters: { docs: { description: { component: doc } } },
  component: CpDivider,
  render: () => ({ components: { CpDivider }, template: '<div class="w-64"><p>Above</p><CpDivider /><p>Below</p></div>' }),
} satisfies Meta<typeof CpDivider>
export default meta
export const Default: StoryObj<typeof meta> = {}
