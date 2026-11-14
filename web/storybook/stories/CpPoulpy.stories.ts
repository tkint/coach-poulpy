import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpPoulpy, CpPoulpyCard, CpButton } from '@coach-poulpy/ui'

const meta = { title: 'Poulpy/Poulpy', component: CpPoulpy } satisfies Meta<typeof CpPoulpy>
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

export const EmptyStateCard: StoryObj = {
  render: () => ({
    components: { CpPoulpyCard, CpButton },
    template: '<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>',
  }),
}
