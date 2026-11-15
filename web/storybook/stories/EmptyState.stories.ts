import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpPoulpyCard, CpButton } from '@coach-poulpy/ui'

// A pattern: Poulpy with a title, a line and an action, for no results and empty lists. Built from CpPoulpyCard and CpButton.
const meta = { title: 'Web/Patterns/Empty state', component: CpPoulpyCard } satisfies Meta<typeof CpPoulpyCard>
export default meta

/** No results or an empty list: Poulpy says what is missing and offers the next step. */
export const EmptyStateCard: StoryObj = {
  render: () => ({
    components: { CpPoulpyCard, CpButton },
    template: '<CpPoulpyCard title="No workout yet">Start with a session of your own.<template #actions><CpButton>Create a workout</CpButton></template></CpPoulpyCard>',
  }),
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByText('No workout yet')).toBeInTheDocument()
  },
}
