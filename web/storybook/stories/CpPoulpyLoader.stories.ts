import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpPoulpyLoader } from '@coach-poulpy/ui'
import doc from './CpPoulpyLoader.md?raw'

const meta = {
  title: 'Web/Components/Display/Poulpy loader',
  component: CpPoulpyLoader,
  argTypes: { size: { control: { type: 'range', min: 16, max: 160, step: 8 } } },
  parameters: { docs: { description: { component: doc } } },
} satisfies Meta<typeof CpPoulpyLoader>
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  play: async ({ canvasElement }) => {
    // A status a screen reader announces, in the page's language.
    expect(within(canvasElement).getByRole('status', { name: 'Loading' })).toBeInTheDocument()
  },
}
export const Large: Story = { args: { size: 112 } }
export const Small: Story = { args: { size: 16 } }
export const SaysWhatLoads: Story = {
  args: { label: 'Loading your sessions' },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('status', { name: 'Loading your sessions' })).toBeInTheDocument()
  },
}
