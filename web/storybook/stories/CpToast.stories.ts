import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpToast } from '@coach-poulpy/ui'
import doc from './CpToast.md?raw'

const meta = {
  title: 'Web/Components/Display/Toast',
  parameters: { docs: { description: { component: doc } } },
  component: CpToast,
  render: () => ({ components: { CpToast }, template: '<CpToast>Exercise saved</CpToast>' }),
} satisfies Meta<typeof CpToast>
export default meta
type Story = StoryObj<typeof meta>

export const Saved: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('status')).toHaveTextContent('Exercise saved')
  },
}
