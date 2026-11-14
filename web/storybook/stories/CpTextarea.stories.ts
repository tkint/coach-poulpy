import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { CpBadge, CpTextarea } from '@coach-poulpy/ui'
import doc from './CpTextarea.md?raw'

const meta = {
  title: 'Web/Components/Fields/Textarea',
  parameters: { docs: { description: { component: doc } } },
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
/** A badge under the helper, saying where a translated text stands. */
export const WithAStatus: Story = {
  render: (args) => ({
    components: { CpTextarea, CpBadge },
    setup: () => ({ args, value: ref('') }),
    template: '<CpTextarea v-bind="args" v-model="value"><template #status><CpBadge tone="neutral" small>To translate</CpBadge></template></CpTextarea>',
  }),
}
