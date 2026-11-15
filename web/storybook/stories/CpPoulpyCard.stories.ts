import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpPoulpyCard, CpButton } from '@coach-poulpy/ui'

// A pattern: Poulpy with a title, a line and, when there is a next step, one button. For empty lists, no results and a page that is gone.
const meta = {
  title: 'Web/Patterns/Poulpy card',
  component: CpPoulpyCard,
  render: (args: { title: string; body: string; action?: string }) => ({
    components: { CpPoulpyCard, CpButton },
    setup: () => ({ args }),
    template: `<CpPoulpyCard :title="args.title">{{ args.body }}<template v-if="args.action" #actions><CpButton>{{ args.action }}</CpButton></template></CpPoulpyCard>`,
  }),
} satisfies Meta<typeof CpPoulpyCard>
export default meta
type Story = StoryObj<{ title: string; body: string; action?: string }>

/** An empty list: say what is missing and offer the next step. */
export const EmptyList: Story = {
  args: { title: 'No workout yet', body: 'Start with a session of your own.', action: 'Create a workout' },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Create a workout' })).toBeInTheDocument()
  },
}

/** No results for the filters in use: the way out is to change them, so there is no button. */
export const NoResults: Story = { args: { title: 'No account matches', body: 'Change or clear the filters to see more accounts.' } }

/** Something that is gone: the way back is the one button. */
export const Missing: Story = { args: { title: 'This workout is gone', body: 'It may have been deleted.', action: 'Back to workouts' } }
