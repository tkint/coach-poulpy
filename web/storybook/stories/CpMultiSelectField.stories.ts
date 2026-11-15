import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpMultiSelectField, cpSizes } from '@coach-poulpy/ui'
import { sizeArgType } from './sizes'
import doc from './CpMultiSelectField.md?raw'

const options = [
  { value: 'quadriceps', text: 'Quadriceps' },
  { value: 'hamstrings', text: 'Hamstrings' },
  { value: 'glutes', text: 'Glutes' },
  { value: 'calves', text: 'Calves' },
]
const meta = {
  title: 'Web/Components/Fields/MultiSelectField',
  parameters: { popover: true, docs: { description: { component: doc } } },
  component: CpMultiSelectField,
  argTypes: { size: sizeArgType },
  args: { label: 'Muscles', placeholder: 'Any muscle', options, noneLabel: 'No muscle matches' },
  render: (args) => ({
    components: { CpMultiSelectField },
    setup: () => ({ args, picked: ref(['quadriceps']) }),
    template: '<CpMultiSelectField v-bind="args" v-model="picked" />',
  }),
} satisfies Meta<typeof CpMultiSelectField>
export default meta
type Story = StoryObj<typeof meta>

/** The panel is shut until the button is used. */
export const Closed: Story = {
  play: async ({ canvasElement }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open')
  },
}
export const Open: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(canvasElement.querySelector('summary')!)
    expect(canvasElement.querySelector('details')).toHaveAttribute('open')
  },
}
export const NothingMatches: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(canvasElement.querySelector('summary')!)
    await userEvent.type(within(canvasElement).getByRole('searchbox'), 'zzz')
    expect(within(canvasElement).getByText('No muscle matches')).toBeVisible()
  },
}
export const EscapeClosesThePanel: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(canvasElement.querySelector('summary')!)
    await userEvent.keyboard('{Escape}')
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open')
  },
}

/** Every step of the size scale, xs to xxl; md is what a component without a `size` draws. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpMultiSelectField },
    setup: () => ({ args, cpSizes, value: ref([]) }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpMultiSelectField v-bind="args" :size="s" v-model="value" /></div></div>',
  }),
}
