import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpPoulpy } from '@coach-poulpy/ui'
import doc from './CpPoulpy.md?raw'

const meta = { title: 'Web/Components/Display/Poulpy', component: CpPoulpy, parameters: { docs: { description: { component: doc } } } } satisfies Meta<typeof CpPoulpy>
export default meta
type Story = StoryObj<typeof meta>

export const Happy: Story = { args: { drawing: 'mood_happy' } }
/** The blue side of pain and rehab: Poulpy is never guilt. */
export const Recovering: Story = { args: { drawing: 'mood_recovering' } }
export const Grumpy: Story = { args: { drawing: 'mood_grumpy' } }
export const Described: Story = {
  args: { drawing: 'pose_thinking', label: 'Poulpy is thinking' },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByAltText('Poulpy is thinking')).toBeInTheDocument()
  },
}
