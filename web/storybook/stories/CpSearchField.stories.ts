import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpSearchField, cpSizes } from '@coach-poulpy/ui'
import { sizeArgType } from './sizes'
import doc from './CpSearchField.md?raw'

const meta = {
  title: 'Web/Components/Fields/SearchField',
  parameters: { docs: { description: { component: doc } } },
  component: CpSearchField,
  argTypes: { size: sizeArgType },
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

/** Every step of the size scale, xs to xxl; md is what a component without a `size` draws. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpSearchField },
    setup: () => ({ args, cpSizes, value: ref('') }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpSearchField v-bind="args" :size="s" v-model="value" /></div></div>',
  }),
}
