import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpSelectField } from '@coach-poulpy/ui'
import doc from './CpSelectField.md?raw'

const options = [
  { value: 'circuit', text: 'Circuit' },
  { value: 'strength', text: 'Strength' },
  { value: 'mobility', text: 'Mobility' },
]
const meta = {
  title: 'Fields/SelectField',
  component: CpSelectField,
  args: { label: 'Format', options },
  // CLEANUP: the button is named by its label alone, so the value it shows is not part of its accessible name (WCAG 2.5.3);
  // name it by label and value, then drop this exemption.
  parameters: { popover: true, docs: { description: { component: doc } }, a11y: { test: 'todo' } },
  render: (args) => ({
    components: { CpSelectField },
    setup: () => ({ args, value: ref('circuit') }),
    template: '<CpSelectField v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpSelectField>
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
export const ChoosingClosesThePanel: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(canvasElement.querySelector('summary')!)
    await userEvent.click(within(canvasElement).getByRole('option', { name: 'Mobility' }))
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open')
    expect(canvasElement.querySelector('summary')).toHaveTextContent('Mobility')
  },
}
export const EscapeClosesThePanel: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(canvasElement.querySelector('summary')!)
    await userEvent.keyboard('{Escape}')
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open')
  },
}
export const Compact: Story = { args: { compact: true } }
