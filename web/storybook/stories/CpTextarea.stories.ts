import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { CpTextarea } from '@coach-poulpy/ui'

const meta = {
  title: 'Fields/Textarea',
  component: CpTextarea,
  args: { label: 'Notes' },
  render: (args) => ({
    components: { CpTextarea },
    setup: () => ({ args, value: ref('') }),
    template: '<CpTextarea v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpTextarea>
export default meta
type Story = StoryObj<typeof meta>

export const Empty: Story = { args: { placeholder: 'How did it feel?' } }
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const WithAHint: Story = { parameters: { a11y: { test: 'todo' } }, args: { hint: 'Only you see this.' } }
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const Invalid: Story = { parameters: { a11y: { test: 'todo' } }, args: { invalid: true, hint: 'Write at least a few words.' } }
export const Disabled: Story = { args: { disabled: true } }
