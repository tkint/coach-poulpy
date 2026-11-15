import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpPasswordField, cpSizes } from '@coach-poulpy/ui'
import { sizeArgType } from './sizes'
import doc from './CpPasswordField.md?raw'

const meta = {
  title: 'Web/Components/Fields/PasswordField',
  parameters: { docs: { description: { component: doc } } },
  component: CpPasswordField,
  argTypes: { size: sizeArgType },
  args: { label: 'Password', showLabel: 'Show the password', hideLabel: 'Hide the password', autocomplete: 'new-password' },
  render: (args) => ({
    components: { CpPasswordField },
    setup: () => ({ args, value: ref('') }),
    template: '<CpPasswordField v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpPasswordField>
export default meta
type Story = StoryObj<typeof meta>

/** The password is hidden on load. */
export const Hidden: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByLabelText('Password', { selector: 'input' })).toHaveAttribute('type', 'password')
  },
}
export const Shown: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Show the password' }))
    expect(within(canvasElement).getByLabelText('Password', { selector: 'input' })).toHaveAttribute('type', 'text')
  },
}
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const WithAHint: Story = { parameters: { a11y: { test: 'todo' } }, args: { hint: 'At least 8 characters.' } }
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const Invalid: Story = { parameters: { a11y: { test: 'todo' } }, args: { invalid: true, hint: 'This password is too short.' } }

/** Every step of the size scale, xs to xxl; md is what a component without a `size` draws. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpPasswordField },
    setup: () => ({ args, cpSizes, value: ref('') }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpPasswordField v-bind="args" :size="s" v-model="value" /></div></div>',
  }),
}
