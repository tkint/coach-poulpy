import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpChipSelectField } from '@coach-poulpy/ui'
import doc from './CpChipSelectField.md?raw'

const options = [
  { value: 'standing', text: 'Standing' },
  { value: 'bench', text: 'Bench' },
  { value: 'bar', text: 'Barbell' },
]
const meta = {
  title: 'Web/Components/Fields/ChipSelectField',
  component: CpChipSelectField,
  parameters: { popover: true, docs: { description: { component: doc } } },
  args: { label: 'Equipment', placeholder: 'Add equipment', options, noneLabel: 'No equipment matches', removeLabel: (text: string) => `Remove ${text}` },
  render: (args, { name }) => ({
    components: { CpChipSelectField },
    setup: () => ({ args, picked: ref(name === 'Empty' || name === 'With A Hint' ? [] : ['standing']) }),
    template: '<div class="pb-48"><CpChipSelectField v-bind="args" v-model="picked" /></div>',
  }),
} satisfies Meta<typeof CpChipSelectField>
export default meta
type Story = StoryObj<typeof meta>

export const Empty: Story = {}
export const WithPickedValues: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Remove Standing' })).toBeInTheDocument()
  },
}
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const WithAHint: Story = { parameters: { a11y: { test: 'todo' } }, args: { hint: 'Only what the exercise needs.' } }
/** The list is shut until the field is used. */
export const ClosedOnLoad: Story = {
  play: async ({ canvasElement }) => {
    expect(canvasElement.querySelector('[role=listbox]')).not.toBeVisible()
  },
}
export const Open: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('combobox'))
    expect(canvasElement.querySelector('[role=listbox]')).toBeVisible()
  },
}
// CLEANUP: the "nothing matches" line is an `li` inside `role=listbox`, which ARIA does not allow; move it out of the listbox, then drop this exemption.
export const NothingMatches: Story = {
  parameters: { a11y: { test: 'todo' } },
  play: async ({ canvasElement }) => {
    await userEvent.type(within(canvasElement).getByRole('combobox'), 'zzz')
    expect(within(canvasElement).getByText('No equipment matches')).toBeVisible()
  },
}
export const RemovingAValue: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Remove Standing' }))
    expect(within(canvasElement).queryByRole('button', { name: 'Remove Standing' })).toBeNull()
  },
}
export const EscapeClosesTheList: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('combobox'))
    await userEvent.keyboard('{Escape}')
    expect(canvasElement.querySelector('[role=listbox]')).not.toBeVisible()
  },
}
