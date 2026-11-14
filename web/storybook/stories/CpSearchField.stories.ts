import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpSearchField } from '@coach-poulpy/ui'

const meta = {
  title: 'Fields/SearchField',
  component: CpSearchField,
  args: { label: 'Search exercises', clearLabel: 'Clear the search', placeholder: 'Search' },
  render: (args) => ({
    components: { CpSearchField },
    setup: () => ({ args, query: ref(args.modelValue ?? '') }),
    template: '<CpSearchField v-bind="args" v-model="query" />',
  }),
} satisfies Meta<typeof CpSearchField>
export default meta
type Story = StoryObj<typeof meta>

export const Empty: Story = {}
export const Filled: Story = { args: { modelValue: 'squat' } }
export const Compact: Story = { args: { compact: true } }
export const ClearingEmptiesIt: Story = {
  args: { modelValue: 'squat' },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Clear the search' }))
    expect(within(canvasElement).getByRole('searchbox')).toHaveValue('')
  },
}
