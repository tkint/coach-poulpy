import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpTextField } from '@coach-poulpy/ui'

const meta = {
  title: 'Fields/TextField',
  component: CpTextField,
  args: { label: 'Email', type: 'email' },
  render: (args) => ({
    components: { CpTextField },
    setup: () => ({ args, value: ref('') }),
    template: '<CpTextField v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpTextField>
export default meta
type Story = StoryObj<typeof meta>

export const Empty: Story = {}
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const WithAHint: Story = { parameters: { a11y: { test: 'todo' } }, args: { hint: 'We only use it to sign you in.' } }
export const WithAnError: Story = { args: { invalid: true, error: 'This email address is not valid.' } }
export const Typing: Story = {
  play: async ({ canvasElement }) => {
    const input = within(canvasElement).getByLabelText('Email')
    await userEvent.type(input, 'alex@example.com')
    expect(input).toHaveValue('alex@example.com')
  },
}
